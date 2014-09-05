package com.quku.service;

import java.util.List;

import com.quku.entity.MyNote;
import com.quku.entity.MyNoteList;

/**
 * 记事本接口
 * @author Administrator
 *
 */
public interface MyNoteService {

	MyNote insert(MyNote note);
	boolean update(MyNote note);
	boolean updateNoteList(MyNoteList noteList);
	List<MyNote> query();
	MyNote getMyNote(MyNote note);
	boolean deleteMyNote(int id);
	boolean deleteMyNoteList(int id);
	boolean clearMyNoteListAll(int note_id);
}
