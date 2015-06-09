package cn.gfreader.pdf;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import cn.gfreader.db.Db;
import cn.gfreader.main.R;
import cn.gfreader.util.FileOperations;
import cn.gfreader.util.Messages;

import com.ant.liao.GifView;
import com.ant.liao.GifView.GifImageType;

public class MainActivity extends Activity {

	private Db db;
	private Pdf pdf;
	private ListView listView;
	private ArrayList<ListviewItem> arrayList = new ArrayList<ListviewItem>();
	private ListviewAdapter listviewAdapter;
	private String pdfDocMd5;
	private int curFrontpageNum = 0, curLastpageNum = -1; // 页码从0开始
	private boolean isNeedLoadPrevPage = false, isNeedLoadNextPage = false, isListviewInit = false;
	private int viewAreaWidth, viewAreaHeight;
	private LinearLayout loadingLayoutTop, loadingLayoutBottom;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pdf_main);

		loadingLayoutTop = (LinearLayout) findViewById(R.id.pdfLoadingLayoutTop);
		loadingLayoutBottom = (LinearLayout) findViewById(R.id.pdfLoadingLayoutBottom);
		loadingTipInit();

		listView = (ListView) findViewById(R.id.pdfListview);
		db = Db.getInstance(MainActivity.this);

		// 初始化PDF
		Bundle bundle = getIntent().getExtras();
		if (null == bundle) {
			Messages.msgAndBackToHome("PDF参数错误", MainActivity.this);
			return;
		}
		File pdfFile = new File(bundle.getString("pdf_file_path"));
		if (pdfFile.exists() == false) {
			Messages.msgAndBackToHome("PDF参数错误", MainActivity.this);
			return;
		}
		String pdfFilePath = pdfFile.getAbsolutePath();
		pdf = new Pdf(MainActivity.this, pdfFilePath);
		pdfDocMd5 = FileOperations.GetMd5(pdfFilePath);

		// 为listview设置触摸事件
		listView.setOnTouchListener(new ListviewTouch());

		// 从数据库中取出当前页
		SQLiteDatabase readableDatabase = db.getReadableDatabase();
		Cursor cursor = readableDatabase.query("books", null, "book_md5 = ?", new String[] { pdfDocMd5 }, null, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			curFrontpageNum = cursor.getInt(cursor.getColumnIndex("current_pagenum"));
			curLastpageNum = curFrontpageNum - 1;
		}
		cursor.close();
		readableDatabase.close();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO 自动生成的方法存根
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && isListviewInit == false) {
			// 获取用户绘制区域的宽高
			Rect viewAreaRect = new Rect();
			getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(viewAreaRect);
			viewAreaWidth = viewAreaRect.width();
			viewAreaHeight = viewAreaRect.height();

			listviewInit();
			isListviewInit = true;
		}
	}

	/*
	 * 初始化listview，根据当前读到的页数，取出PDF页面显示到屏幕
	 */
	private void listviewInit() {
		float listviewY = listView.getY();
		int listviewHeight = getListviewHeight();
		if (listviewY + listviewHeight >= viewAreaHeight) {
			return;
		}
		if (++curLastpageNum >= pdf.getPageCount()) {
			curLastpageNum--;
			return;
		}
		Bitmap bitmap = pdf.getPage(curLastpageNum);
		int listviewItemHeight = viewAreaWidth * bitmap.getHeight() / bitmap.getWidth();
		ListviewItem listviewItem = new ListviewItem();
		listviewItem.setBitmap(bitmap);
		listviewItem.setWidth(viewAreaWidth);
		listviewItem.setHeight(listviewItemHeight);
		arrayList.add(listviewItem);
		if (listviewAdapter == null) {
			listviewAdapter = new ListviewAdapter(MainActivity.this, R.layout.pdf_listview_item, arrayList);
			listView.setAdapter(listviewAdapter);
		} else {
			listviewAdapter.notifyDataSetChanged();
		}
		updateListviewHeight();
		listviewInit();
	}

	/*
	 * 设置数据库中PDF的当前页码
	 */
	private void updateDbCurrentPagenum(int pagenum) {
		SQLiteDatabase writableDatabase = db.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("current_pagenum", pagenum);
		writableDatabase.update("books", contentValues, "book_md5 = ?", new String[] { pdfDocMd5 });
		contentValues.clear();
		writableDatabase.close();
	}

	private void loadPages() {
		int listviewHeight = getListviewHeight();
		int listviewWidth = getListviewMaxWidth();
		float listviewY = listView.getY();
		// 加载上一页
		if (isNeedLoadPrevPage == true) {
			if (curFrontpageNum > 0) {
				curFrontpageNum--;
				Bitmap bitmap = pdf.getPage(curFrontpageNum);
				int listviewItemHeight = listviewWidth * bitmap.getHeight() / bitmap.getWidth();
				ListviewItem listviewItem = new ListviewItem();
				listviewItem.setBitmap(bitmap);
				listviewItem.setWidth(listviewWidth);
				listviewItem.setHeight(listviewItemHeight);
				arrayList.add(0, listviewItem);
				listviewAdapter.notifyDataSetChanged();
				updateListviewWidth();
				updateListviewHeight();
				updateDbCurrentPagenum(curFrontpageNum);
			}

			// 定位到顶部
			listView.setY(0);
			loadingLayoutTop.setVisibility(View.GONE);
			isNeedLoadPrevPage = false;
		}
		// 加载下一页
		if (isNeedLoadNextPage == true) {
			if (curLastpageNum < (pdf.getPageCount() - 1)) {
				curLastpageNum++;
				Bitmap bitmap = pdf.getPage(curLastpageNum);
				int listviewItemHeight = listviewWidth * bitmap.getHeight() / bitmap.getWidth();
				ListviewItem listviewItem = new ListviewItem();
				listviewItem.setBitmap(bitmap);
				listviewItem.setWidth(listviewWidth);
				listviewItem.setHeight(listviewItemHeight);
				arrayList.add(listviewItem);
				listviewAdapter.notifyDataSetChanged();
				updateListviewWidth();
				updateListviewHeight();
				listviewHeight += bitmap.getHeight();
				updateDbCurrentPagenum(curLastpageNum);
			}

			isNeedLoadNextPage = false;
			loadingLayoutBottom.setVisibility(View.GONE);
			// 定位到PDF的底部
			if (listviewY + listviewHeight - viewAreaHeight < 0 && listviewHeight > viewAreaHeight) {
				listviewY = viewAreaHeight - listviewHeight;
				listView.setY(listviewY);
			}
		}
	}

	/*
	 * 初始化正在加载提示
	 */
	private void loadingTipInit() {
		GifView gifViewTop = (GifView) findViewById(R.id.pdfLoadingImgTop);
		gifViewTop.setGifImage(R.drawable.loading);
		gifViewTop.setGifImageType(GifImageType.COVER);

		GifView gifViewBottom = (GifView) findViewById(R.id.pdfLoadingImgBottom);
		gifViewBottom.setGifImage(R.drawable.loading);
		gifViewBottom.setGifImageType(GifImageType.COVER);
	}

	/*
	 * 设置"正在加载"的上、下填充
	 */
	private void setLoadingTipStatus(int topDistance, int bottomDistance) {
		float listviewY = listView.getY();
		int topPaddingVal = (int) (0.3 * listviewY);
		if (listviewY > 0 && listviewY <= topDistance) {
			isNeedLoadPrevPage = true;
			loadingLayoutTop.setVisibility(View.VISIBLE);
			loadingLayoutTop.setPadding(0, topPaddingVal, 0, 0);
		} else {
			loadingLayoutTop.setVisibility(View.GONE);
		}

		int listviewHeight = getListviewHeight();
		loadingLayoutBottom = (LinearLayout) findViewById(R.id.pdfLoadingLayoutBottom);
		int reallyBottomDistance = (int) ((listviewY + listviewHeight - viewAreaHeight) * -1);
		if (reallyBottomDistance > 0 && reallyBottomDistance <= bottomDistance) { // 如果文档底端到窗口底端的距离处于区间
			isNeedLoadNextPage = true;
			loadingLayoutBottom.setVisibility(View.VISIBLE);
			int bottomPaddingVal = (int) (reallyBottomDistance * 0.3);
			loadingLayoutBottom.setPadding(0, 0, 0, bottomPaddingVal);
		} else {
			loadingLayoutBottom.setVisibility(View.GONE);
		}
	}

	/*
	 * 根据项目数更新listview的高度
	 */
	private void updateListviewHeight() {
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = getListviewHeight();
		listView.setLayoutParams(params);
	}

	/*
	 * 更新listview的宽度
	 */
	private void updateListviewWidth() {
		int listviewWidth = getListviewMaxWidth();
		updateListviewWidth(listviewWidth);
	}

	/*
	 * 把listview的宽度改为目标宽度
	 */
	private void updateListviewWidth(int targetWidth) {
		ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
		ListviewItem listviewItem;
		int targetHeight;
		Bitmap bitmap;

		for (int i = 0; i < arrayList.size(); i++) {
			bitmaps.add(arrayList.get(i).getBitmap());
		}
		arrayList.removeAll(arrayList);
		listviewAdapter.notifyDataSetChanged();

		for (int i = 0; i < bitmaps.size(); i++) {
			bitmap = bitmaps.get(i);
			targetHeight = bitmap.getHeight() * targetWidth / bitmap.getWidth();
			listviewItem = new ListviewItem();
			listviewItem.setWidth(targetWidth);
			listviewItem.setHeight(targetHeight);
			listviewItem.setBitmap(bitmap);
			arrayList.add(listviewItem);
		}
		listviewAdapter.notifyDataSetChanged();

		LayoutParams layoutParams = listView.getLayoutParams();
		layoutParams.width = targetWidth;
		listView.setLayoutParams(layoutParams);
	}

	/*
	 * 取得listview的最大宽度
	 */
	private int getListviewMaxWidth() {
		if (listviewAdapter == null || listviewAdapter.getCount() <= 0) {
			return 0;
		}
		int maxWidth = 0;
		for (int i = 0; i < listviewAdapter.getCount(); i++) {
			View listItem = listviewAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			if (listItem.getMeasuredWidth() > maxWidth) {
				maxWidth = listItem.getMeasuredWidth();
			}
		}
		return maxWidth;
	}

	/*
	 * 取得listview的高度
	 */
	private int getListviewHeight() {
		if (listviewAdapter == null || listviewAdapter.getCount() <= 0) {
			return 0;
		}
		int totalHeight = 0;
		int listviewItemCount = listviewAdapter.getCount();
		for (int i = 0; i < listviewItemCount; i++) {
			View listItem = listviewAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		int listviewHeight = totalHeight + (listView.getDividerHeight() * (listviewItemCount - 1));
		return listviewHeight;
	}

	/*
	 * 实现listview的触摸事件接口
	 */
	class ListviewTouch implements OnTouchListener {

		private float fingerOneTouchStartX, fingerOneTouchStartY, fingerOneTouchMoveX, fingerOneTouchMoveY, fingerOneXDistanceAbs, fingerOneYDistanceAbs;
		private float listviewX, listviewY;
		private float twoFingersOffsetX, twoFingersOffsetY, currentFingersDistance, lastFingersDistance = -1;
		private double fingerOnePointsDistance, fingerOnePointsRadian;
		private int listviewWidth, listviewHeight;

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			listviewX = listView.getX();
			listviewY = listView.getY();
			listviewHeight = getListviewHeight();
			listviewWidth = getListviewMaxWidth();

			// 如果是一根手指
			if (event.getPointerCount() == 1) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:

					fingerOneTouchStartX = event.getX();
					fingerOneTouchStartY = event.getY();

					break;

				case MotionEvent.ACTION_MOVE:

					// 计算移动距离和角度
					fingerOneTouchMoveX = event.getX();
					fingerOneTouchMoveY = event.getY();
					fingerOneXDistanceAbs = Math.abs(fingerOneTouchStartX - fingerOneTouchMoveX);
					fingerOneYDistanceAbs = Math.abs(fingerOneTouchStartY - fingerOneTouchMoveY);
					fingerOnePointsDistance = Math.sqrt(Math.pow(fingerOneXDistanceAbs, 2) + Math.pow(fingerOneYDistanceAbs, 2));
					fingerOnePointsRadian = fingerOneXDistanceAbs / fingerOnePointsDistance;
					// 水平方向移动
					if (fingerOnePointsRadian >= 0.5) {
						if (fingerOneTouchMoveX > fingerOneTouchStartX) { // 向右移动
							listView.setX(listviewX + fingerOneXDistanceAbs);
						} else { // 向左移动
							listView.setX(listviewX - fingerOneXDistanceAbs);
						}
					} else {
						if (fingerOneTouchMoveY > fingerOneTouchStartY) { // 向下移动
							// 拖拽时，文档顶端离屏幕顶端最大为150px
							if ((listviewY + fingerOneYDistanceAbs) <= 150) {
								listviewY = listviewY + fingerOneYDistanceAbs;
								listView.setY(listviewY);
							}
						} else { // 向上移动
							if ((listviewY - fingerOneYDistanceAbs + listviewHeight - viewAreaHeight) > -150) {
								listviewY = listviewY - fingerOneYDistanceAbs;
								listView.setY(listviewY);
							}
						}
					}

					// 设置“正在加载”的状态
					setLoadingTipStatus(150, 150);

					break;

				case MotionEvent.ACTION_UP:
					loadPages();
					if (listviewWidth < viewAreaWidth) {
						listviewWidth = viewAreaWidth;
						listviewX = 0;
						listView.setX(listviewX);
						updateListviewWidth(listviewWidth);
						updateListviewHeight();
					}
					if (listviewX > 0) {
						listviewX = 0;
						listView.setX(listviewX);
					}
					if (listviewX < 0 && listviewX + listviewWidth < viewAreaWidth) {
						listviewX = viewAreaWidth - listviewWidth;
						listView.setX(listviewX);
					}
					break;
				default:
					break;
				}
			}

			// 如果是2根手指
			if (event.getPointerCount() == 2) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					break;
				case MotionEvent.ACTION_MOVE:
					twoFingersOffsetX = event.getX(0) - event.getX(1);
					twoFingersOffsetY = event.getY(0) - event.getY(1);
					// 取得两根手指之间的距离
					currentFingersDistance = (float) Math.sqrt(twoFingersOffsetX * twoFingersOffsetX + twoFingersOffsetY * twoFingersOffsetY);
					if (lastFingersDistance < 0) {
						lastFingersDistance = currentFingersDistance;
					} else {
						if (currentFingersDistance - lastFingersDistance > 5) {// 放大
							listviewWidth = (int) (1.009f * listviewWidth);
							updateListviewWidth(listviewWidth);
						} else if (lastFingersDistance - currentFingersDistance > 5) {// 缩小
							listviewWidth = (int) (0.993f * listviewWidth);
							updateListviewWidth(listviewWidth);
						}
						updateListviewHeight();
						lastFingersDistance = currentFingersDistance;
					}
					break;

				case MotionEvent.ACTION_UP:
					break;

				default:
					break;
				}
			}

			return true;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(MainActivity.this, cn.gfreader.main.MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
