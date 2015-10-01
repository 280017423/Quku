package com.quku.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.quku.Utils.SystemDef;

public class DatabaseHelper extends SQLiteOpenHelper {
	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	public DatabaseHelper(Context context, String name) {
		this(context, name, SystemDef.Database.VERSION);
	}

	public DatabaseHelper(Context context, String name, int version) {
		this(context, name, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTable(db);
	}

	private void createTable(SQLiteDatabase db) {
		String myNoteTableSql = "create table tb_myNote(id integer primary key autoincrement,"
				+ "title varchar(50),"
				+ "noteType integer,"
				+ "notetime varchar(50));";// 创建记事本的sql语句
		String myNoteListSql = "create table tb_myNoteList(id integer primary key autoincrement,"
				+ "notelistname varchar(50),"
				+ "picPath varchar(100),"
				+ "note_id integer);";// 创建记事本列表的sql语句
		db.execSQL(myNoteTableSql);
		db.execSQL(myNoteListSql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		System.out.println("update a Database");
	}

}
