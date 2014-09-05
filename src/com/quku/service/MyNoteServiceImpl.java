package com.quku.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.quku.Utils.SystemDef;
import com.quku.Utils.Utils;
import com.quku.db.DatabaseHelper;
import com.quku.entity.MyNote;
import com.quku.entity.MyNoteList;

public class MyNoteServiceImpl implements MyNoteService {
	
	private Context context;
	DatabaseHelper dh = null;
	public DatabaseHelper getDh() {
		return dh;
	}

	public void setDh(DatabaseHelper dh) {
		this.dh = dh;
	}

	public MyNoteServiceImpl(Context context){
		this.context = context;
		dh = new DatabaseHelper(context, SystemDef.Database.DATABASE_MYNOTE, 2);
	}

	@Override
	public MyNote insert(MyNote note) {
		if(null == note || null == note.getNoteList())return null;
		note.setNoteTime(Utils.data2String(new Date(), SystemDef.DateFormact.FORMACT_YMDHMS));
		ContentValues cv = new ContentValues();
		//向该对象当中插入键值对，其中键是列名，值是希望插入到这一列的值，值必须和数据库当中的数据类型一致
		cv.put("title", note.getTitle());
		cv.put("noteType", note.getNoteType());
		cv.put("notetime", note.getNoteTime());
		SQLiteDatabase db = dh.getWritableDatabase();
		//插入Mynote表
		long row = db.insert(SystemDef.Database.TABLE_MYNOTE, null, cv);
		if(row == 0){
			return null;
		}
		//插入mynoteList表
		for(MyNoteList noteList : note.getNoteList()){
			cv = new ContentValues();
			cv.put("picPath", noteList.getPicPath());
			cv.put("note_id", row);
			cv.put("notelistname", noteList.getNotelistname());
			long insertRow = db.insert(SystemDef.Database.TABLE_MYNOTELIST, null, cv);
			if(insertRow == 0){
				return null;
			}
		}
		return note;
	}

	@Override
	public boolean update(MyNote note) {
		if(null == note)return false;
		ContentValues cv = new ContentValues();
		//想该对象当中插入键值对，其中键是列名，值是希望插入到这一列的值，值必须和数据库当中的数据类型一致
		cv.put("title", note.getTitle());
		cv.put("noteType", note.getNoteType());
		cv.put("notetime", note.getNoteTime());
		SQLiteDatabase db = dh.getWritableDatabase();
		//第一个参数是要更新的表名
		//第二个参数是一个ContentValeus对象
		//第三个参数是where子句
		String str [] = {String.valueOf(note.getId())};
		long result = 0;
		try {
			result = db.update(SystemDef.Database.TABLE_MYNOTE, cv, "id=?", str);
		} catch (Exception e) {
			Log.d(SystemDef.Debug.TAG, "MyNoteServiceImple update exceptin = " + e.getMessage());
			return false;
		}
		if(result == 0)return false;
		return true;
	}

	@Override
	public List<MyNote> query() {
		SQLiteDatabase db = dh.getReadableDatabase();
		Cursor cursor = db.query(SystemDef.Database.TABLE_MYNOTE, new String []{"id","title","noteType","notetime"}, null, null, null, null, "notetime desc");
		try {
			List<MyNote> noteList = new ArrayList<MyNote>();
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int id = Integer.parseInt(cursor.getString(cursor
							.getColumnIndex("id")));
					String title = cursor.getString(cursor
							.getColumnIndex("title"));
					String noteTime = cursor.getString(cursor
							.getColumnIndex("notetime"));
					int noteType = Integer.parseInt(cursor.getString(cursor
							.getColumnIndex("noteType")));
					MyNote myNote = new MyNote(id, title, noteTime);
					myNote.setNoteType(noteType);
					noteList.add(myNote);
				}
				return noteList;
			}
		} catch (Exception e) {
			Log.d(SystemDef.Debug.TAG, "MyNoteServiceImple query exceptin = " + e.getMessage());
		}finally{
			cursor.close();
		}
		return null;
	}

	@Override
	public MyNote getMyNote(MyNote note) {
		SQLiteDatabase db = dh.getReadableDatabase();
		Cursor cursor = db.query(SystemDef.Database.TABLE_MYNOTE, new String []{"id","title","noteType","notetime"}, "id=?", new String[]{String.valueOf(note.getId())}, null, null, null);
		MyNote myNote = null;
		try {
			if (cursor != null) {
				myNote = new MyNote();
				while (cursor.moveToNext()) {
					int id = Integer.parseInt(cursor.getString(cursor
							.getColumnIndex("id")));
					String title = cursor.getString(cursor
							.getColumnIndex("title"));
					int noteType = Integer.parseInt(cursor.getString(cursor
							.getColumnIndex("noteType")));
					String noteTime = cursor.getString(cursor
							.getColumnIndex("notetime"));
					myNote.setId(id);
					myNote.setTitle(title);
					myNote.setNoteTime(noteTime);
					myNote.setNoteType(noteType);
					//查询记事本列表
					Cursor cursorNoteList = db.query(SystemDef.Database.TABLE_MYNOTELIST, new String []{"id","notelistname","picPath","note_id"}, "note_id=?", new String[]{String.valueOf(note.getId())}, null, null, null);
					if (cursorNoteList != null) {
						List<MyNoteList> mynoteList = new ArrayList<MyNoteList>();
						while (cursorNoteList.moveToNext()) {
							int notelistId = Integer.parseInt(cursorNoteList.getString(cursorNoteList
									.getColumnIndex("id")));
							String notelistname = cursorNoteList.getString(cursorNoteList
									.getColumnIndex("notelistname"));
							String picPath = cursorNoteList.getString(cursorNoteList
									.getColumnIndex("picPath"));
							int note_id = Integer.parseInt(cursorNoteList.getString(cursorNoteList
									.getColumnIndex("note_id")));
							MyNoteList myl = new MyNoteList(notelistId,picPath,note_id);
							myl.setNotelistname(notelistname);
							mynoteList.add(myl);
						}
						myNote.setNoteList(mynoteList);
						cursorNoteList.close();
				}
				return myNote;
				}
			}
		} catch (Exception e) {
			Log.d(SystemDef.Debug.TAG, "MyNoteServiceImple getMyNote exceptin = " + e.getMessage());
		}finally{
			cursor.close();
		}
		return null;
	}

	@Override
	public boolean updateNoteList(MyNoteList noteList) {
		if(null == noteList)return false;
		ContentValues cv = new ContentValues();
		//想该对象当中插入键值对，其中键是列名，值是希望插入到这一列的值，值必须和数据库当中的数据类型一致
		cv.put("notelistname", noteList.getNotelistname());
		cv.put("picPath", noteList.getPicPath());
		cv.put("note_id", noteList.getNote_id());
		SQLiteDatabase db = dh.getWritableDatabase();
		//第一个参数是要更新的表名
		//第二个参数是一个ContentValeus对象
		//第三个参数是where子句
		String str [] = {String.valueOf(noteList.getId())};
		long result = db.update(SystemDef.Database.TABLE_MYNOTELIST, cv, "id=?", str);
		if(result == 0)return false;
		return true;
	}

	@Override
	public boolean deleteMyNote(int id) {
		if(id == 0)return false;
		SQLiteDatabase db = dh.getWritableDatabase();
		String str [] = {String.valueOf(id)};
		long result = db.delete(SystemDef.Database.TABLE_MYNOTE, "id=?", str);
		if(result == 0)return false;
		return true;
	}

	@Override
	public boolean deleteMyNoteList(int id) {
		if(id == 0)return false;
		SQLiteDatabase db = dh.getWritableDatabase();
		String str [] = {String.valueOf(id)};
		long result = db.delete(SystemDef.Database.TABLE_MYNOTELIST, "id=?", str);
		if(result == 0)return false;
		return true;
	}

	@Override
	public boolean clearMyNoteListAll(int note_id) {
		if(note_id == 0)return false;
		SQLiteDatabase db = dh.getWritableDatabase();
		String str [] = {String.valueOf(note_id)};
		long result = db.delete(SystemDef.Database.TABLE_MYNOTELIST, "note_id=?", str);
		if(result == 0)return false;
		return true;
	}

}
