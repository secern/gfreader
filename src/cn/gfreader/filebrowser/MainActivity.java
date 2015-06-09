package cn.gfreader.filebrowser;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import cn.gfreader.db.Db;
import cn.gfreader.main.R;
import cn.gfreader.util.FileOperations;
import cn.gfreader.util.Messages;

public class MainActivity extends Activity {

	private ArrayList<ListviewItem> listviewItems = new ArrayList<ListviewItem>();
	private ListviewAdapter listviewAdapter;
	private ListView listView;
	private String curDirectory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filebrowser_main);

		listView = (ListView) findViewById(R.id.fileBrowserListview);

		File rootFileDir = new File("/");
		updateListview(rootFileDir);

		listviewItemClick();

		returnButtonClick();
	}

	private void returnButtonClick() {
		findViewById(R.id.fileBrowserDirReturn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//如果是根目录，返回
				if (curDirectory.equals(Environment.getExternalStorageDirectory().toString())) {
					return;
				}
				int index = curDirectory.lastIndexOf(new String("/"));
				String dirString = curDirectory.substring(0, index);//取得上级路径
				File file = new File(dirString);
				if (file.exists()) {
					updateListview(file);
				}
			}
		});
	}

	/*
	 * 设置文件列表的点击事件
	 */
	private void listviewItemClick() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListviewItem listviewItem = listviewAdapter.getItem(position);

				// 判断数据库中是否存在该书的记录
				if (listviewItem.fileType == ListviewItem.FileType.PDF || listviewItem.fileType == ListviewItem.FileType.TXT) {
					int curPageNum = listviewItem.fileType == ListviewItem.FileType.PDF ? 0 : 1;
					String bookMd5 = FileOperations.GetMd5(listviewItem.filePath);
					SQLiteDatabase readableDatabase = Db.getInstance(MainActivity.this).getReadableDatabase();
					Cursor cursor = readableDatabase.rawQuery("select * from books where book_md5=?", new String[] { bookMd5 });
					if (cursor.getCount() <= 0) {
						SQLiteDatabase writableDatabase = Db.getInstance(MainActivity.this).getWritableDatabase();
						ContentValues contentValues = new ContentValues();
						contentValues.put("book_name", listviewItem.fileName);
						contentValues.put("book_md5", bookMd5);
						contentValues.put("book_path", listviewItem.filePath);
						contentValues.put("current_pagenum", curPageNum);
						writableDatabase.insert("books", null, contentValues);
						contentValues.clear();
						writableDatabase.close();
					}
					cursor.close();
					readableDatabase.close();
				}

				if (listviewItem.fileType == ListviewItem.FileType.PDF) {
					Intent intent = new Intent(MainActivity.this, cn.gfreader.pdf.MainActivity.class);
					intent.putExtra("pdf_file_path", listviewItem.filePath);
					startActivity(intent);
				} else if (listviewItem.fileType == ListviewItem.FileType.TXT) {
					Intent intent = new Intent(MainActivity.this, cn.gfreader.txt.MainActivity.class);
					intent.putExtra("txt_file_path", listviewItem.filePath);
					startActivity(intent);
				} else if (listviewItem.fileType == ListviewItem.FileType.UNKNOW) {
					Messages.msgAndBackToHome("无法打开该文件", MainActivity.this);
				} else if (listviewItem.fileType == ListviewItem.FileType.DIRECTORY) {
					File file = new File(listviewItem.filePath);
					updateListview(file);
				}
			}
		});
	}

	/*
	 * 更新文件列表
	 */
	private void updateListview(File fileDir) {
		if (fileDir.isDirectory() == false) {
			return;
		}
		// 更新路径框中显示的路径
		String string;
		String esDir = Environment.getExternalStorageDirectory().toString();
		TextViewShowLast textViewShowLast = (TextViewShowLast) findViewById(R.id.fileBrowserFilepathTextview);
		if (fileDir.getAbsolutePath().equals(esDir)) {
			string = fileDir.getAbsolutePath().replaceAll(esDir, "/");
		} else {
			string = fileDir.getAbsolutePath().replaceAll(esDir, "");
		}
		textViewShowLast.setText(string);
		curDirectory = fileDir.getAbsolutePath();

		listviewItems.removeAll(listviewItems);
		File[] files = fileDir.listFiles();
		ListviewItem listviewItem;
		String fileExt;
		for (int i = 0; i < files.length; i++) {
			listviewItem = new ListviewItem();
			listviewItem.fileName = files[i].getName();
			listviewItem.filePath = files[i].getAbsolutePath();
			if (files[i].isDirectory()) {
				listviewItem.fileType = ListviewItem.FileType.DIRECTORY;
			} else {
				fileExt = listviewItem.fileName.substring((listviewItem.fileName.length() - 3), listviewItem.fileName.length());
				if (fileExt != null && fileExt.isEmpty() == false) {
					fileExt = fileExt.toLowerCase();
					if ("txt".equals(fileExt)) {
						listviewItem.fileType = ListviewItem.FileType.TXT;
					} else if ("pdf".equals(fileExt)) {
						listviewItem.fileType = ListviewItem.FileType.PDF;
					} else {
						listviewItem.fileType = ListviewItem.FileType.UNKNOW;
					}
				}
			}
			listviewItems.add(listviewItem);
		}
		if (listviewAdapter == null) {
			listviewAdapter = new ListviewAdapter(MainActivity.this, R.layout.filebrowser_listview_item, listviewItems);
			listView.setAdapter(listviewAdapter);
		} else {
			listviewAdapter.notifyDataSetChanged();
		}
	}
}
