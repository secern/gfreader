package cn.gfreader.main;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import cn.gfreader.db.Db;
import cn.gfreader.util.FileOperations;

public class MainActivity extends Activity {

	private ListviewAdapter listviewAdapter = null;
	private ArrayList<ListviewItem> adapterArrayList = new ArrayList<ListviewItem>();
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		listView = (ListView) findViewById(R.id.mainListview);

		listviewFillData();
		listviewItemClick();
		listviewItemLongClick();
	}

	/*
	 * 把数据库中的已读记录填充到listview
	 */
	private void listviewFillData() {
		File file;
		String filePath;
		ListviewItem listviewItem;
		adapterArrayList.clear();
		SQLiteDatabase readableDatabase = Db.getInstance(MainActivity.this).getReadableDatabase();
		Cursor cursor = readableDatabase.rawQuery("select * from books", null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				listviewItem = new ListviewItem();
				filePath = cursor.getString(cursor.getColumnIndex("book_path"));
				file = new File(filePath);
				if (file.exists()) {
					listviewItem.file = file;
					listviewItem.bookName = file.getName();
					listviewItem.itemType = ListviewItem.ItemType.BOOK;
					adapterArrayList.add(listviewItem);
				} else {
					listviewItem.bookName = cursor.getString(cursor.getColumnIndex("book_name"));
					listviewItem.itemType = ListviewItem.ItemType.UNKNOW;
					adapterArrayList.add(listviewItem);
				}
			} while (cursor.moveToNext());
		}
		//在记录的结尾添加导入书籍按钮
		listviewItem = new ListviewItem();
		listviewItem.itemType = ListviewItem.ItemType.BUTTON;
		adapterArrayList.add(listviewItem);
		if (null == listviewAdapter) {
			listviewAdapter = new ListviewAdapter(MainActivity.this, R.layout.main_activity_listview_item, adapterArrayList);
			listView.setAdapter(listviewAdapter);
		}else {
			listviewAdapter.notifyDataSetChanged();
		}
	}

	/*
	 * 设置点击书本事件
	 */
	private void listviewItemClick() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListviewItem listviewItem = listviewAdapter.getItem(position);
				String fileExt = listviewItem.bookName.substring(listviewItem.bookName.length() - 3, listviewItem.bookName.length());
				fileExt = fileExt.toLowerCase();
				if ("txt".equals(fileExt)) {
					Intent intent = new Intent(MainActivity.this, cn.gfreader.txt.MainActivity.class);
					intent.putExtra("txt_file_path", listviewItem.file.getAbsolutePath());
					startActivity(intent);
				} else if ("pdf".equals(fileExt)) {
					Intent intent = new Intent(MainActivity.this, cn.gfreader.pdf.MainActivity.class);
					intent.putExtra("pdf_file_path", listviewItem.file.getAbsolutePath());
					startActivity(intent);
				}
			}
		});
	}

	/*
	 * 设置长按书本事件
	 */
	private void listviewItemLongClick() {
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				ListviewItem listviewItem = listviewAdapter.getItem(position);
				new AlertDialog.Builder(MainActivity.this)
				.setTitle("消息")
				.setMessage("确定删除阅读记录？")
				.setPositiveButton("确定", new DeleteBookRecord(listviewItem.file))
				.setNegativeButton("取消", null)
				.show();
				return true;
			}
		});
	}
	
	/*
	 * 删除书本记录。长按书本并确认删除时调用。
	 */
	class DeleteBookRecord implements DialogInterface.OnClickListener{
		private File file;
		public DeleteBookRecord(File file) {
			this.file = file;
		}
		@Override
		public void onClick(DialogInterface dialog, int which) {
			String bookMd5 = FileOperations.GetMd5(file.getAbsolutePath());
			SQLiteDatabase writeableDatabase = Db.getInstance(MainActivity.this).getWritableDatabase();
			writeableDatabase.delete("books", "book_md5=?", new String[]{bookMd5});
			writeableDatabase.delete("text_pagination", "book_md5=?", new String[]{bookMd5});
			writeableDatabase.close();
			listviewFillData();
		}
	}
}
