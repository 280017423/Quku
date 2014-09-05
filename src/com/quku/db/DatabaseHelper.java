package com.quku.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.quku.Utils.SystemDef;

public class DatabaseHelper extends SQLiteOpenHelper {

	// 在SQLiteOepnHelper的子类当中，必须有该构造函数
	public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
		// 必须通过super调用父类当中的构造函数
		super(context, name, factory, version);
	}

	public DatabaseHelper(Context context, String name) {
		this(context, name, SystemDef.Database.VERSION);
	}

	public DatabaseHelper(Context context, String name, int version) {
		this(context, name, null, version);
	}

	// 该函数是在第一次创建数据库的时候执行,实际上是在第一次得到SQLiteDatabse对象的时候，才会调用这个方法
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		System.out.println("create a Database");
		// execSQL函数用于执行SQL语句,创建表
		createTable(db);
	}

	/*
	 * 创建表
	 * 
	 * @param db
	 */
	private void createTable(SQLiteDatabase db) {
		String myNoteTableSql = "create table tb_myNote(id integer primary key autoincrement," + "title varchar(50),"
				+ "noteType integer," + "notetime varchar(50));";// 创建记事本的sql语句
		String myNoteListSql = "create table tb_myNoteList(id integer primary key autoincrement,"
				+ "notelistname varchar(50)," + "picPath varchar(100)," + "note_id integer);";// 创建记事本列表的sql语句
		db.execSQL(myNoteTableSql);
		db.execSQL(myNoteListSql);
		// for(int i = 1 ; i< 10;i++){
		// String insertsql =
		// "insert into tb_myNote(title,noteType,notetime) values('测试" +
		// i+"',1,'2012-12-29 10:10:10')";
		// // MyNote mynote = new MyNote(0, "测试"+i, "2012-12-29 10:10:10");
		// db.execSQL(insertsql);
		// }
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		System.out.println("update a Database");
	}

}
