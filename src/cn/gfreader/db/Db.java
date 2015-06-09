package cn.gfreader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class Db {

	private static Db db;
	private static DbHelper dbHelper = null;

	private Db(Context context) {
		dbHelper = new DbHelper(context, "gfreader", null, 1);
	}

	public static Db getInstance(Context context) {
		if (dbHelper == null) {
			db = new Db(context);
		}
		return db;
	}

	public SQLiteDatabase getReadableDatabase() {
		return dbHelper.getReadableDatabase();
	}

	public SQLiteDatabase getWritableDatabase() {
		return dbHelper.getWritableDatabase();
	}
}