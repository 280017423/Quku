package com.quku.entity;

/**
 * 记事本列表实体对象
 * 
 * @author Administrator
 * 
 */
public class MyNoteList {

	private int id;
	private String notelistname;// 文件名

	public String getNotelistname() {
		return notelistname;
	}

	public void setNotelistname(String notelistname) {
		this.notelistname = notelistname;
	}

	private String picPath;// 记事本图片
	private int note_id;// 记事本关联id

	public MyNoteList() {
	};

	public MyNoteList(int id, String picPath, int note_id) {
		this.id = id;
		this.picPath = picPath;
		this.note_id = note_id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPicPath() {
		return picPath;
	}

	public void setPicPath(String picPath) {
		this.picPath = picPath;
	}

	public int getNote_id() {
		return note_id;
	}

	public void setNote_id(int note_id) {
		this.note_id = note_id;
	}
}
