package com.quku.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 记事本实体对象
 * 
 * @author zou.sq
 * 
 */
public class MyNote implements Serializable {

	private static final long serialVersionUID = 914400811424754443L;
	private int id;
	private String title;
	private String noteTime;
	private int noteType;// 记事本类型
	private List<MyNoteList> noteList;

	public MyNote() {
	};

	public MyNote(int id, String title, String noteTime) {
		this.id = id;
		this.title = title;
		this.noteTime = noteTime;
	}

	public int getNoteType() {
		return noteType;
	}

	public void setNoteType(int noteType) {
		this.noteType = noteType;
	}

	public List<MyNoteList> getNoteList() {
		return noteList;
	}

	public void setNoteList(List<MyNoteList> noteList) {
		this.noteList = noteList;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNoteTime() {
		return noteTime;
	}

	public void setNoteTime(String noteTime) {
		this.noteTime = noteTime;
	}
}
