package cn.gfreader.txt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import cn.gfreader.db.Db;
import cn.gfreader.util.Messages;

public class ReadView extends RelativeLayout {

	private int curPageNum, lastPageNum = 0;
	private String pressUpTip = "";
	private Context mContext;
	private ReadViewAdapter readViewAdapter;
	private ToolbarBottom toolbarBottom;
	private TextViewExt prevTextViewExt, curTextViewExt, nextTextViewExt;
	private float animationX, touchX, clickX;
	private TranslateAnimation translateAnimation;
	private LoadPageStatus loadPageStatus = LoadPageStatus.WAIT;

	private enum LoadPageStatus {
		LOADING, LOADED, WAIT
	};

	public ReadView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public void setAdapter(ReadViewAdapter readViewAdapter, ToolbarBottom toolbarBottom) {
		this.readViewAdapter = readViewAdapter;

		// 取得当前页和最后一页的页数
		SQLiteDatabase readableDatabase = Db.getInstance(mContext).getReadableDatabase();
		Cursor cursor = readableDatabase.query("books", new String[] { "current_pagenum" }, "book_md5=?", new String[] { readViewAdapter.bookMd5 }, null, null, null);
		if (cursor.getCount() <= 0) {
			Messages.msgAndBackToHome("该书记录不存在", mContext);
			return;
		}
		cursor.moveToFirst();
		curPageNum = cursor.getInt(cursor.getColumnIndex("current_pagenum"));
		cursor = readableDatabase.query("text_pagination", new String[] { "page_num" }, "book_md5=?", new String[] { readViewAdapter.bookMd5 }, null, null, "id desc", "1");
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			lastPageNum = cursor.getInt(cursor.getColumnIndex("page_num"));
		}

		// 取得页面内容并赋值
		Map<String, String> map = readViewAdapter.getPages(curPageNum);
		prevTextViewExt = createTextviewext();
		curTextViewExt = createTextviewext();
		nextTextViewExt = createTextviewext();

		prevTextViewExt.setText(map.get("prev"));
		curTextViewExt.setText(map.get("cur"));
		nextTextViewExt.setText(map.get("next"));

		addView(nextTextViewExt);
		addView(curTextViewExt);
		addView(prevTextViewExt);

		// 设置为充满屏幕
		android.view.ViewGroup.LayoutParams layoutParams = prevTextViewExt.getLayoutParams();
		layoutParams.width = LayoutParams.MATCH_PARENT;
		layoutParams.height = LayoutParams.MATCH_PARENT;
		prevTextViewExt.setLayoutParams(layoutParams);

		layoutParams = curTextViewExt.getLayoutParams();
		layoutParams.width = LayoutParams.MATCH_PARENT;
		layoutParams.height = LayoutParams.MATCH_PARENT;
		curTextViewExt.setLayoutParams(layoutParams);

		layoutParams = nextTextViewExt.getLayoutParams();
		layoutParams.width = LayoutParams.MATCH_PARENT;
		layoutParams.height = LayoutParams.MATCH_PARENT;
		nextTextViewExt.setLayoutParams(layoutParams);

		animationX = -1 * readViewAdapter.viewWidth;
		prevTextViewExt.setX(animationX);// 把前一页隐藏于左边

		this.toolbarBottom = toolbarBottom;
		toolbarBottom.seekBarInit(curPageNum);
	}

	public void moveToPage(int pageNum) {
		curPageNum = --pageNum;
		moveToNextPage();
	}

	/*
	 * 加载上一页
	 */
	private void moveToPrevPage() {
		if (curPageNum <= 1) {
			Messages.msg("当前是第一页", mContext);
			loadPageStatus = LoadPageStatus.LOADED;
			return;
		}
		curPageNum -= 2;
		moveToNextPage();
	}

	/*
	 * 加载下一页
	 */
	private void moveToNextPage() {
		String tip = "";
		if (curPageNum + 1 > lastPageNum) {
			tip = "当前是最后一页";
		} else if (curPageNum + 1 < 1) {
			tip = "当前是第一页";
		}
		if ("".equals(tip) == false) {
			Messages.msg(tip, mContext);
			loadPageStatus = LoadPageStatus.LOADED;
			return;
		}

		curPageNum++;
		toolbarBottom.setSeekbarProgress(curPageNum);

		updateDbPageNum(curPageNum);

		removeView(prevTextViewExt);
		removeView(curTextViewExt);
		removeView(nextTextViewExt);

		Map<String, String> map = readViewAdapter.getPages(curPageNum);
		prevTextViewExt = createTextviewext();
		curTextViewExt = createTextviewext();
		nextTextViewExt = createTextviewext();

		prevTextViewExt.setText(map.get("prev"));
		curTextViewExt.setText(map.get("cur"));
		nextTextViewExt.setText(map.get("next"));

		addView(nextTextViewExt);
		addView(curTextViewExt);
		addView(prevTextViewExt);

		// 设置为充满屏幕
		android.view.ViewGroup.LayoutParams layoutParams = prevTextViewExt.getLayoutParams();
		layoutParams.width = LayoutParams.MATCH_PARENT;
		layoutParams.height = LayoutParams.MATCH_PARENT;
		prevTextViewExt.setLayoutParams(layoutParams);

		layoutParams = curTextViewExt.getLayoutParams();
		layoutParams.width = LayoutParams.MATCH_PARENT;
		layoutParams.height = LayoutParams.MATCH_PARENT;
		curTextViewExt.setLayoutParams(layoutParams);

		layoutParams = nextTextViewExt.getLayoutParams();
		layoutParams.width = LayoutParams.MATCH_PARENT;
		layoutParams.height = LayoutParams.MATCH_PARENT;
		nextTextViewExt.setLayoutParams(layoutParams);

		animationX = -1 * readViewAdapter.viewWidth;
		prevTextViewExt.setX(animationX);// 把前一页隐藏于左边

		loadPageStatus = LoadPageStatus.LOADED;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		if (loadPageStatus == LoadPageStatus.LOADING) {
			return true;
		}

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchX = ev.getX();
			clickX = ev.getX();
			break;
		case MotionEvent.ACTION_MOVE:
			if (loadPageStatus == LoadPageStatus.LOADED) {
				touchX = ev.getX();
				loadPageStatus = LoadPageStatus.WAIT;
			}
			if (ev.getX() - touchX > 5) {// 手指向右移动
				if (curPageNum - 1 <= 0) {
					pressUpTip = "当前是第一页";
					break;
				}
				animationX = readViewAdapter.viewWidth * -1 + ev.getX() - touchX;
				if (-1 * animationX / readViewAdapter.viewWidth <= 0.666) {
					loadPageStatus = LoadPageStatus.LOADING;
					moveToPrevPageAnimation(animationX);
				} else {
					prevTextViewExt.setX(animationX);
					invalidate();
				}
			} else if (touchX - ev.getX() > 5) {// 手指向左移动
				if (curPageNum + 1 > lastPageNum) {
					pressUpTip = "当前是最后一页";
					break;
				}
				animationX = -1 * (touchX - ev.getX());
				if (animationX / readViewAdapter.viewWidth <= -0.333) {
					loadPageStatus = LoadPageStatus.LOADING;
					moveToNextPageAnimation(animationX);
				} else {
					curTextViewExt.setX(animationX);
					invalidate();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			// 如果页面没有滑动到触发加载其它页的位置，复位
			if (loadPageStatus == LoadPageStatus.WAIT) {
				animationX = 0;
				prevTextViewExt.setX((-1 * readViewAdapter.viewWidth));
				curTextViewExt.setX(0);
				invalidate();
			}
			if (Math.abs(clickX - ev.getX()) <= 5) {
				if (clickX / readViewAdapter.viewWidth <= 0.4) {// 点击了左边
					loadPageStatus = LoadPageStatus.LOADING;
					moveToPrevPage();
				}
				if (clickX / readViewAdapter.viewWidth > 0.4 && clickX / readViewAdapter.viewWidth < 0.6) {// 点击了中间
					toolbarBottom.visibleToggle();
				}
				if (clickX / readViewAdapter.viewWidth >= 0.6) {// 点击了右边
					loadPageStatus = LoadPageStatus.LOADING;
					moveToNextPage();
				}
			}
			if ("".equals(pressUpTip) == false) {
				Messages.msg(pressUpTip, mContext);
				pressUpTip = "";
			}
			break;
		default:
			break;
		}
		return true;
	}

	/*
	 * 如果手指划动的目的是阅读上一页，当把页面划到屏幕的三分之一处时，触发划到上一页的动画
	 */
	private void moveToPrevPageAnimation(float curX) {
		translateAnimation = new TranslateAnimation(0, (curX * -1), 0, 0);
		translateAnimation.setDuration(200);
		translateAnimation.setFillAfter(true);
		translateAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				prevTextViewExt.clearAnimation();
				animationX = 0;
				prevTextViewExt.setX(animationX);
				moveToPrevPage();
			}
		});
		prevTextViewExt.startAnimation(translateAnimation);
		invalidate();
	}

	/*
	 * 如果手指划动的目的是阅读下一页，当把页面划到屏幕的三分之一处时，触发划到下一页的动画
	 */
	private void moveToNextPageAnimation(float curX) {
		translateAnimation = new TranslateAnimation(0, (-1 * (readViewAdapter.viewWidth + curX)), 0, 0);
		translateAnimation.setDuration(200);
		translateAnimation.setFillAfter(true);
		translateAnimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				curTextViewExt.clearAnimation();
				animationX = -1 * readViewAdapter.viewWidth;
				curTextViewExt.setX(animationX);
				moveToNextPage();
			}
		});
		curTextViewExt.startAnimation(translateAnimation);
		invalidate();
	}

	private void updateDbPageNum(int pageNum) {
		SQLiteDatabase writeableDatabase = Db.getInstance(mContext).getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("current_pagenum", pageNum);
		writeableDatabase.update("books", contentValues, "book_md5=?", new String[] { readViewAdapter.bookMd5 });
		contentValues.clear();
		writeableDatabase.close();
	}

	private TextViewExt createTextviewext() {
		Config config = new Config(mContext);
		TextViewExt textViewExt = new TextViewExt(mContext);
		textViewExt.setTextSize(config.getInt("readview_textSize"));
		textViewExt.setPadding(config.getInt("readview_paddingLeft"), config.getInt("readview_paddingTop"), config.getInt("readview_paddingRight"), config.getInt("readview_paddingBottom"));
		textViewExt.setBackground(getBackground("background/" + config.getString("readview_bg")));
		textViewExt.setLineSpacing(0, config.getFloat("readview_lineSpacingMultiplier"));
		return textViewExt;
	}

	private BitmapDrawable getBackground(String bgName) {
		InputStream inputStream = null;
		try {
			inputStream = mContext.getAssets().open(bgName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		BitmapDrawable bitmapDrawable = new BitmapDrawable(inputStream);
		return bitmapDrawable;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		// 如果手指把页面划到了左边或者右边的三分之一内，绘制页面的阴影
		if ((animationX < 0 && animationX > -1 * readViewAdapter.viewWidth * 0.333) || (animationX < -1 * readViewAdapter.viewWidth * 0.666 && animationX > -1 * readViewAdapter.viewWidth)) {
			RectF rectF = new RectF((animationX + readViewAdapter.viewWidth), 0, readViewAdapter.viewWidth, readViewAdapter.viewHeight);
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			LinearGradient linearGradient = new LinearGradient((animationX + readViewAdapter.viewWidth), 0, (animationX + readViewAdapter.viewWidth + 20), 0, 0x33000000, 0x00000000, TileMode.CLAMP);
			paint.setShader(linearGradient);
			paint.setStyle(Style.FILL);
			canvas.drawRect(rectF, paint);
		}
	}
}
