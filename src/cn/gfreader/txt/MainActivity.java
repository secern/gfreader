package cn.gfreader.txt;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Window;
import cn.gfreader.db.Db;
import cn.gfreader.main.R;
import cn.gfreader.util.FileOperations;
import cn.gfreader.util.Messages;

public class MainActivity extends Activity {

	private File txtFile;
	private boolean isReaderViewInit = false;
	private String bookMd5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.txt_main);

		// 初始化TXT
		Bundle bundle = getIntent().getExtras();
		if (null != bundle) {
			String txtFilePath = bundle.getString("txt_file_path");
			txtFile = new File(txtFilePath);
		}
		if (txtFile.exists() == false) {
			Messages.msgAndBackToHome("TXT文件不存在", MainActivity.this);
			return;
		}
		bookMd5 = FileOperations.GetMd5(txtFile.getAbsolutePath());
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && isReaderViewInit == false) {
			isReaderViewInit = true;
			// 获取用户绘制区域的宽高
			Rect viewAreaRect = new Rect();
			getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(viewAreaRect);

			// 判断该书是否分页完成，没有就跳转到分页
			SQLiteDatabase readableDatabase = Db.getInstance(MainActivity.this).getReadableDatabase();
			Cursor cursor = readableDatabase.query("books", new String[] { "is_pagination_finish" }, "book_md5=?", new String[] { bookMd5 }, null, null, null);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				String val = cursor.getString(cursor.getColumnIndex("is_pagination_finish"));
				Intent intent;
				if (Integer.parseInt(val) == 1) {
					intent = new Intent(MainActivity.this, ReadActivity.class);
				} else {
					intent = new Intent(MainActivity.this, PaginationActivity.class);
				}
				intent.putExtra("txt_file_path", txtFile.getAbsolutePath());
				startActivity(intent);
			} else {
				Messages.msgAndBackToHome("数据库错误", MainActivity.this);
				return;
			}
			cursor.close();
			readableDatabase.close();
		}
	}
}
