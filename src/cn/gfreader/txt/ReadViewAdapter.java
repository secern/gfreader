package cn.gfreader.txt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cn.gfreader.db.Db;
import cn.gfreader.util.FileCharsetDetector;
import cn.gfreader.util.FileOperations;

public class ReadViewAdapter {

	public int curPageNum, viewWidth, viewHeight, pagesCount;
	public String bookMd5;
	public File txtFile;
	private Context mContext;

	public ReadViewAdapter(Activity activity, Context context, File txtFile, int viewAreaWidth, int viewAreaHeight) {
		this.mContext = context;
		this.txtFile = txtFile;
		this.viewWidth = viewAreaWidth;
		this.viewHeight = viewAreaHeight;

		bookMd5 = FileOperations.GetMd5(txtFile.getAbsolutePath());

		// 取得总页数
		SQLiteDatabase readableDatabase = Db.getInstance(mContext).getReadableDatabase();
		Cursor cursor = readableDatabase.rawQuery("select count(*) from text_pagination where book_md5=?", new String[] { bookMd5 });
		cursor.moveToFirst();
		pagesCount = cursor.getInt(0);
		cursor.close();
		readableDatabase.close();
	}

	public Map<String, String> getPages(int curPageNum) {
		Map<String, String> map = new HashMap<String, String>();
		int pageStartCharIndex, pageEndCharIndex;

		// 取得当前页
		SQLiteDatabase readableDatabase = Db.getInstance(mContext).getReadableDatabase();
		Cursor cursor = readableDatabase.query("text_pagination", new String[] { "page_num", "start_index", "end_index" }, "book_md5=? and page_num=?", new String[] { bookMd5, String.valueOf(curPageNum) }, null, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			pageStartCharIndex = cursor.getInt(cursor.getColumnIndex("start_index"));
			pageEndCharIndex = cursor.getInt(cursor.getColumnIndex("end_index"));
			map.put("cur", getFileText(pageStartCharIndex, pageEndCharIndex));
		}

		// 取得上一页
		cursor = readableDatabase.query("text_pagination", new String[] { "page_num", "start_index", "end_index" }, "book_md5=? and page_num=?", new String[] { bookMd5, String.valueOf(curPageNum - 1) }, null, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			pageStartCharIndex = cursor.getInt(cursor.getColumnIndex("start_index"));
			pageEndCharIndex = cursor.getInt(cursor.getColumnIndex("end_index"));
			map.put("prev", getFileText(pageStartCharIndex, pageEndCharIndex));
		}

		// 取得下一页
		cursor = readableDatabase.query("text_pagination", new String[] { "page_num", "start_index", "end_index" }, "book_md5=? and page_num=?", new String[] { bookMd5, String.valueOf(curPageNum + 1) }, null, null, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			pageStartCharIndex = cursor.getInt(cursor.getColumnIndex("start_index"));
			pageEndCharIndex = cursor.getInt(cursor.getColumnIndex("end_index"));
			map.put("next", getFileText(pageStartCharIndex, pageEndCharIndex));
		}

		cursor.close();
		readableDatabase.close();

		return map;
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

	/*
	 * 取得文本文件中的部分字符
	 */
	private String getFileText(int start, int end) {
		String resultString = null;
		String charset = getCharset();
		if (null == charset || charset.isEmpty()) {
			return null;
		}
		try {
			RandomAccessFile randomAccessFile = new RandomAccessFile(txtFile, "r");
			FileChannel fileChannel = randomAccessFile.getChannel();
			MappedByteBuffer mappedByteBuffer = fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size());
			byte[] bytes = new byte[(int) fileChannel.size()];
			mappedByteBuffer.get(bytes);
			String tempString = new String(bytes, charset);
			resultString = tempString.substring(start, end);
			tempString = null;
			bytes = null;
			mappedByteBuffer = null;
			fileChannel.close();
			randomAccessFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultString;
	}
}
