package cn.gfreader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

	public DbHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO 自动生成的构造函数存根
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO 自动生成的方法存根
		String sql = "create table books("
				+ "id integer primary key, "
				+ "book_name text not null, "
				+ "book_md5 text not null, "
				+ "book_path text, "
				+ "current_pagenum integer default 0, "
				+ "is_pagination_finish integer default 0,"
				+ "unique(book_md5) "
				+ ")";
		db.execSQL(sql);
		
		sql = "create table text_pagination("
				+ "id integer primary key,"
				+ "book_md5 text,"
				+ "page_num integer,"
				+ "start_index integer,"
				+ "end_index integer"
				+ ")";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO 自动生成的方法存根

	}

}
