package com.quku.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.quku.R;
import com.quku.Utils.SystemDef;

/**
 * 添加note（手写）选择activity
 * 
 * @author Administrator
 * 
 */
public class NoteChoiceActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().getDecorView().setSystemUiVisibility(4);
		setContentView(R.layout.activity_notewrite_choice);
		initView();
	}

	private void initView() {
		TextView mTvTitle = (TextView) findViewById(R.id.title_with_back_title_btn_mid);
		mTvTitle.setText("我的乐谱");

		findViewById(R.id.title_with_back_title_btn_left).setOnClickListener(
				this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		intent.setClass(NoteChoiceActivity.this, CreateNoteActivity.class);
		intent.setAction(SystemDef.NoteWrite.NOTE_INSERT);
		switch (v.getId()) {
		case R.id.title_with_back_title_btn_left:
			finish();
			break;
		case R.id.note_write_music:
			intent.putExtra("type", SystemDef.NoteWrite.TYPE_MAKEMUSIC);
			startActivity(intent);
			break;
		case R.id.note_write_empty:
			intent.putExtra("type", SystemDef.NoteWrite.TYPE_EMPTY);
			startActivity(intent);
			break;
		case R.id.note_write_grid:
			intent.putExtra("type", SystemDef.NoteWrite.TYPE_GRID);
			startActivity(intent);
			break;
		case R.id.note_write_streak:
			intent.putExtra("type", SystemDef.NoteWrite.TYPE_STREAK);
			startActivity(intent);
			break;
		}
	}
}
