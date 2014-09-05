package com.quku.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

	private LinearLayout mLlBack;
	private ImageButton note_write_music;
	private ImageButton note_write_grid;
	private ImageButton note_write_empty;
	private ImageButton note_write_streak;
	private TextView mTvTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().getDecorView().setSystemUiVisibility(4);
		setContentView(R.layout.activity_notewrite_choice);
		initView();
	}

	private void initView() {
		mLlBack = (LinearLayout) this.findViewById(R.id.title_with_back_title_btn_left);
		mTvTitle = (TextView) findViewById(R.id.title_with_back_title_btn_mid);
		mTvTitle.setText("我的乐谱");
		note_write_music = (ImageButton) findViewById(R.id.note_write_music);
		note_write_grid = (ImageButton) findViewById(R.id.note_write_grid);
		note_write_empty = (ImageButton) findViewById(R.id.note_write_empty);
		note_write_streak = (ImageButton) findViewById(R.id.note_write_streak);

		mLlBack.setOnClickListener(this);
		note_write_music.setOnClickListener(this);
		note_write_empty.setOnClickListener(this);
		note_write_grid.setOnClickListener(this);
		note_write_streak.setOnClickListener(this);

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

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
}
