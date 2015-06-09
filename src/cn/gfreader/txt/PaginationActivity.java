package cn.gfreader.txt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.gfreader.db.Db;
import cn.gfreader.main.R;
import cn.gfreader.util.FileCharsetDetector;
import cn.gfreader.util.FileOperations;
import cn.gfreader.util.Messages;

public class PaginationActivity extends Activity {

	private File txtFile;
	private TextViewExt textViewExt;
	private TextView txtPaginationPercent;
	private String bookMd5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.txt_pagination);

		// 初始化TXT
		Bundle bundle = getIntent().getExtras();
		if (null != bundle) {
			String txtFilePath = bundle.getString("txt_file_path");
			txtFile = new File(txtFilePath);
		}
		if (txtFile.exists() == false) {
			Messages.msgAndBackToHome("读取PDF文件失败", this);
			return;
		}
		bookMd5 = FileOperations.GetMd5(txtFile.getAbsolutePath());

		// 判断该书是否分页完成，如果分页完成，跳转到主活动
		SQLiteDatabase readableDatabase = Db.getInstance(PaginationActivity.this).getReadableDatabase();
		Cursor cursor = readableDatabase.query("books", new String[] { "is_pagination_finish" }, "book_md5=?", new String[] { bookMd5 }, null, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			String val = cursor.getString(cursor.getColumnIndex("is_pagination_finish"));
			if (Integer.parseInt(val) == 1) {
				gotoTxtMainActivity();
			}
		} else {
			Messages.msgAndBackToHome("数据库错误", PaginationActivity.this);
			return;
		}
		cursor.close();
		readableDatabase.close();

		// 删除分页表中的数据
		SQLiteDatabase writableDatabase = Db.getInstance(PaginationActivity.this).getWritableDatabase();
		writableDatabase.delete("text_pagination", "book_md5=?", new String[] { bookMd5 });
		writableDatabase.close();

		// 界面初始化
		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.txtPagination);
		textViewExt = createTextviewext();
		relativeLayout.addView(textViewExt);
		LayoutParams layoutParams = textViewExt.getLayoutParams();
		layoutParams.width = LayoutParams.MATCH_PARENT;
		layoutParams.height = LayoutParams.MATCH_PARENT;
		textViewExt.setLayoutParams(layoutParams);

		txtPaginationPercent = createTextviewext();
		relativeLayout.addView(txtPaginationPercent);
		layoutParams = txtPaginationPercent.getLayoutParams();
		layoutParams.width = LayoutParams.MATCH_PARENT;
		layoutParams.height = LayoutParams.MATCH_PARENT;
		txtPaginationPercent.setLayoutParams(layoutParams);
		txtPaginationPercent.setX(0);
		txtPaginationPercent.setY(0);
		txtPaginationPercent.setText("正在分页...");
		txtPaginationPercent.setGravity(Gravity.CENTER);

		// 执行分页
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				pagination();
			}
		}, 2000);
	}

	private void pagination() {
		String charset = getCharset();
		try {
			// 取得文本文件的字符数
			int textCount;
			RandomAccessFile randomAccessFile = new RandomAccessFile(txtFile, "r");
			FileChannel fileChannel = randomAccessFile.getChannel();
			byte[] bytes = new byte[(int) fileChannel.size()];
			MappedByteBuffer mappedByteBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
			mappedByteBuffer.get(bytes);
			textCount = new String(bytes, charset).length();
			mappedByteBuffer.clear();
			bytes = null;
			fileChannel.close();
			randomAccessFile.close();

			// 数据库
			SQLiteDatabase writableDatabase = Db.getInstance(PaginationActivity.this).getWritableDatabase();
			ContentValues contentValues = new ContentValues();

			// 读取文件
			int i = 0, pageCharsCount, pageNum = 1;
			InputStream inputStream = new FileInputStream(txtFile);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset));
			CharBuffer charBuffer = CharBuffer.allocate(textCount);
			bufferedReader.read(charBuffer);
			charBuffer.position(i);
			while (charBuffer.length() > 0) {
				textViewExt.setText(charBuffer);
				pageCharsCount = textViewExt.getTextCount();
				contentValues.put("book_md5", bookMd5);
				contentValues.put("page_num", pageNum);
				contentValues.put("start_index", i);
				contentValues.put("end_index", i + pageCharsCount);
				writableDatabase.insert("text_pagination", null, contentValues);
				contentValues.clear();
				pageNum++;
				i += pageCharsCount;
				charBuffer.position(i);
			}
			charBuffer = null;
			bufferedReader.close();
			inputStream.close();

			txtPaginationPercent.setText("分页完成");

			// 更新数据库
			contentValues.put("is_pagination_finish", 1);
			writableDatabase.update("books", contentValues, "book_md5=?", new String[] { bookMd5 });
			contentValues = null;
			writableDatabase.close();

			// 跳转到主活动
			gotoTxtMainActivity();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getCharset() {
		String charset = null;
		try {
			charset = new FileCharsetDetector().guestFileEncoding(txtFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return charset;
	}

	private void gotoTxtMainActivity() {
		Intent intent = new Intent(PaginationActivity.this, cn.gfreader.txt.MainActivity.class);
		intent.putExtra("txt_file_path", txtFile.getAbsolutePath());
		startActivity(intent);
	}

	private TextViewExt createTextviewext() {
		Config config = new Config(PaginationActivity.this);
		TextViewExt textViewExt = new TextViewExt(PaginationActivity.this);
		textViewExt.setTextSize(config.getInt("readview_textSize"));
		textViewExt.setPadding(config.getInt("readview_paddingLeft"), config.getInt("readview_paddingTop"), config.getInt("readview_paddingRight"), config.getInt("readview_paddingBottom"));
		textViewExt.setBackground(getBackground("background/" + config.getString("readview_bg")));
		textViewExt.setLineSpacing(0, config.getFloat("readview_lineSpacingMultiplier"));
		return textViewExt;
	}

	private BitmapDrawable getBackground(String bgName) {
		InputStream inputStream = null;
		try {
			inputStream = getAssets().open(bgName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		BitmapDrawable bitmapDrawable = new BitmapDrawable(inputStream);
		return bitmapDrawable;
	}
}
