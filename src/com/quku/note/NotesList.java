package com.quku.note;

/*
 * Copyright (C) 2007 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.quku.R;
import com.quku.activity.MianActivity;
import com.quku.note.NotePad.NoteColumns;

/**
 * Displays a list of notes. Will display notes from the {@link Uri} provided in
 * the intent if there is one, otherwise defaults to displaying the contents of
 * the {@link NoteProvider}
 */
public class NotesList extends ListActivity {
	private static final String TAG = "NotesList";
	private Button delItemBtn;
	private ImageView delImageView;

	/**
	 * The columns we are interested in from the database
	 */
	private static final String[] PROJECTION = new String[] { NoteColumns._ID, // 0
			NoteColumns.TITLE, // 1
			NoteColumns.CREATED_DATE,// 2
			NoteColumns.MODIFIED_DATE,// 3
			NoteColumns.NOTE,// 4
	};

	/** The index of the title column */
	private static final int COLUMN_INDEX_ID = 0;
	private static final int COLUMN_INDEX_TITLE = 1;
	private static final int COLUMN_INDEX_CREATED_DATE = 2;
	private static final int COLUMN_INDEX_MODIFIED_DATE = 3;
	private static final int COLUMN_INDEX_NOTE = 4;

	private TextView bodytitle;
	private ListView listView;
	private Button addButton;
	private Button cancelButton;
	private Button exitButton;// 退出
	private TextAdapter textAdapter;
	private Cursor mCursor;
	private ArrayList<String> idlist;
	private ArrayList<String> titlelist;
	private ArrayList<String> datelist;
	private ArrayList<String> notelist;
	private ArrayList<String> modifiedlist;
	private Calendar calendar;
	private boolean deleteButtonShow = false;
	private LinearLayout searchlayout;
	private EditText searchText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().getDecorView().setSystemUiVisibility(4);
		setContentView(R.layout.note_main);
		bodytitle = (TextView) findViewById(R.id.bodytitle);
		listView = (ListView) findViewById(android.R.id.list);
		addButton = (Button) findViewById(R.id.addbutton);
		cancelButton = (Button) findViewById(R.id.cancelbutton);
		searchlayout = (LinearLayout) findViewById(R.id.searchlayout);
		searchText = (EditText) findViewById(R.id.searchtext);
		exitButton = (Button) findViewById(R.id.exitButton);
		exitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(NotesList.this, MianActivity.class);
				NotesList.this.startActivity(intent);
				NotesList.this.finish();
			}
		});
		setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

		// If no data was given in the intent (because we were started
		// as a MAIN activity), then use our default content provider.
		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(NoteColumns.CONTENT_URI);
		} else {
			// addlist();
		}
		iteratorCursor();// 得到Cursor，并组装适配器数据
		textAdapter = new TextAdapter(this);
		listView.setAdapter(textAdapter);

		listView.setOnTouchListener(new ListviewOnTouchListener());
		addButton.setOnClickListener(new ButtonOnClickListener());
		cancelButton.setOnClickListener(new ButtonOnClickListener());
		searchText.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				int mEvent = event.getAction();
				searchText.setEnabled(true);
				switch (mEvent) {
					case MotionEvent.ACTION_UP:
						Intent searchIntent = new Intent();
						searchIntent.setClass(NotesList.this, Searchwindow.class);
						NotesList.this.startActivity(searchIntent);
						searchlayout.setVisibility(View.GONE);
						break;
					default:
						break;
				}
				return false;
			}
		});
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		addlist();
		super.onRestart();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		addlist();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		addlist();
		super.onResume();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * 遍历Cursor数据，组装适配器数据
	 */
	private void iteratorCursor() {
		mCursor = managedQuery(getIntent().getData(), PROJECTION, null, null, NoteColumns.DEFAULT_SORT_ORDER);
		if (mCursor != null) {
			idlist = new ArrayList<String>();
			titlelist = new ArrayList<String>();
			datelist = new ArrayList<String>();
			notelist = new ArrayList<String>();
			modifiedlist = new ArrayList<String>();
			while (mCursor.moveToNext()) {
				String id = mCursor.getString(COLUMN_INDEX_ID);
				String title = mCursor.getString(COLUMN_INDEX_TITLE);
				String note = mCursor.getString(COLUMN_INDEX_NOTE);
				String modifiedate = mCursor.getString(COLUMN_INDEX_CREATED_DATE);
				// String dateTime = mCursor
				// .getString(COLUMN_INDEX_CREATED_DATE);
				// 设置时间为中国
				calendar = Calendar.getInstance(Locale.CHINA);
				long dateTime = Long.parseLong(mCursor.getString(COLUMN_INDEX_CREATED_DATE));
				String dateString = getDateString(calendar, dateTime);
				idlist.add(id);
				titlelist.add(title);
				datelist.add(dateString);
				notelist.add(note);
				modifiedlist.add(modifiedate);
			}
		}
		if (titlelist.size() > 0) {
			String title = getString(R.string.notes_title);
			String size = "(" + titlelist.size() + ")";
			bodytitle.setText(title + size);
		} else {
			bodytitle.setText(getString(R.string.notes_title));
		}
	}

	/**
	 * adpter适配器
	 * 
	 */
	public class TextAdapter extends BaseAdapter {

		private Context mContext = null;

		public TextAdapter(Context context) {
			this.mContext = context;
		}

		public int getCount() {
			return titlelist.size();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final int curPosition = position;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.item, null);
				ItemViewCache itemCache = new ItemViewCache();
				itemCache.mtitleView = (TextView) convertView.findViewById(R.id.title);
				itemCache.mbodyView = (TextView) convertView.findViewById(R.id.date);
				itemCache.mtitleView.setText(titlelist.get(position));
				itemCache.mbodyView.setText(datelist.get(position));
				convertView.setTag(itemCache);
			}
			final View row = convertView;

			// 当个itemOnTonch事件
			row.setOnTouchListener(new OnTouchListener() {

				float downPointX = 0;
				float downPointY = 0;
				boolean isMoved = false;

				public boolean onTouch(View v, MotionEvent event) {
					if (deleteButtonShow) {
						return false;
					}
					int action = event.getAction();
					float x = event.getX();
					float y = event.getY();
					switch (action) {
						case MotionEvent.ACTION_DOWN:
							isMoved = false;
							downPointX = x;
							downPointY = y;
							break;
						case MotionEvent.ACTION_MOVE:
							float deltaX = Math.abs(downPointX - x);
							float deltaY = (downPointY - y);
							if (deltaX > 70) {
								isMoved = true;
								final Button deletebutton = (Button) row.findViewById(R.id.deletebutton);
								delItemBtn = deletebutton;
								final ImageView arrow = (ImageView) row.findViewById(R.id.arrow);
								delImageView = arrow;
								if (!deleteButtonShow) {
									unVisiable(delItemBtn, delImageView, true);
									delItemBtn.setOnClickListener(new OnClickListener() {
										public void onClick(View v) {
											unVisiable(delItemBtn, delImageView, false);
											delete(curPosition);
										}
									});
								}
							}
							if (deltaY < -30) {
								isMoved = true;
								searchlayout.setVisibility(View.VISIBLE);
							}
							if (deltaY > 30) {
								isMoved = true;
								searchlayout.setVisibility(View.GONE);
							}
							break;
						case MotionEvent.ACTION_UP:
							if ((!isMoved) && (!deleteButtonShow)) {
								onItemClick(curPosition);
							}
							break;

						default:
							break;
					}

					return true;
				}
			});

			ItemViewCache ivc = (ItemViewCache) convertView.getTag();
			ivc.mtitleView.setText(titlelist.get(position));
			ivc.mbodyView.setText(datelist.get(position));
			return convertView;
		}

		/**
		 * 当个Item选中事件
		 * 
		 * @param position
		 */
		private void onItemClick(final int position) {
			if (mCursor == null) {
				return;
			}
			int rowId = mCursor.getInt(mCursor.getColumnIndexOrThrow(NotePad.NoteColumns._ID));

			Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), rowId);
			String action = getIntent().getAction();
			if (deleteButtonShow) {
				unVisiable(delItemBtn, delImageView, false);
			} else {
				if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
					// The caller is waiting for us to return a note selected by
					// the user. The have clicked on one, so return it now.
					setResult(RESULT_OK, new Intent().setData(noteUri));
				} else {
					// Launch activity to view/edit the currently selected item
					Intent intent = new Intent(Intent.ACTION_EDIT, noteUri);
					intent.putExtra("titlelist", (Serializable) titlelist);
					intent.putExtra("notelist", (Serializable) notelist);
					intent.putExtra("modifiedlist", (Serializable) modifiedlist);
					intent.putExtra("idlist", (Serializable) idlist);
					intent.putExtra("id", position);
					startActivity(intent);
					finish();
				}
			}
		}

		/**
		 * 删除选中项
		 * 
		 * @param position
		 */
		private void delete(final int position) {

			if (mCursor == null) {
				return;
			}

			System.out.println("ondelete moveTo =" + mCursor.moveToPosition(position));
			int rowId = mCursor.getInt(mCursor.getColumnIndexOrThrow(NotePad.NoteColumns._ID));

			Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), rowId);
			getContentResolver().delete(noteUri, null, null);
			addlist();
		}

	}

	/**
	 * 隐藏/显示处理控件
	 * 
	 * @param view1
	 *            ListItem删除按钮
	 * @param view2
	 *            ListItem标识图标(>)
	 */
	private void unVisiable(View view1, View view2, Boolean flg) {
		Button delItemBtn = (Button) view1;
		ImageView delImageView = (ImageView) view2;
		if (flg) {
			delItemBtn.setVisibility(View.VISIBLE);
			delImageView.setVisibility(View.GONE);
			addButton.setVisibility(View.GONE);
			cancelButton.setVisibility(View.VISIBLE);
			deleteButtonShow = true;
		} else {
			delItemBtn.setVisibility(View.GONE);
			delImageView.setVisibility(View.VISIBLE);
			addButton.setVisibility(View.VISIBLE);
			cancelButton.setVisibility(View.GONE);
			deleteButtonShow = false;
		}

	}

	/**
	 * 适配器优化
	 * 
	 * @author Administrator
	 * 
	 */
	private static class ItemViewCache {
		public TextView mtitleView;
		public TextView mbodyView;
	}

	/**
	 * 监听添加按钮
	 * 
	 * @author Administrator
	 * 
	 */
	public class ButtonOnClickListener implements OnClickListener {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
				case R.id.addbutton:
					startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
					finish();
					break;
				case R.id.cancelbutton:
					delItemBtn.setVisibility(View.GONE);
					delImageView.setVisibility(View.VISIBLE);
					cancelButton.setVisibility(View.GONE);
					addButton.setVisibility(View.VISIBLE);
					deleteButtonShow = false;
					break;
			}
		}
	}

	/**
	 * listview OnTouch事件
	 * 
	 * @author Administrator
	 * 
	 */
	public class ListviewOnTouchListener implements OnTouchListener {
		float ldPointX = 0;
		float ldPointY = 0;

		public boolean onTouch(View v, MotionEvent event) {
			int listaction = event.getAction();
			float x = event.getX();
			float y = event.getY();
			boolean iMove = false;
			switch (listaction) {
				case MotionEvent.ACTION_DOWN:
					if (deleteButtonShow) {
						unVisiable(delItemBtn, delImageView, false);
					}
					iMove = false;
					ldPointX = x;
					ldPointY = y;
					break;
				case MotionEvent.ACTION_MOVE:
					float _x = Math.abs(ldPointX - x);
					float _y = ldPointY - y;
					if (_x > 70 || _y > 100 || _y < -100) {
						iMove = true;
					}
					if (_y < -50) {
						iMove = true;
						searchlayout.setVisibility(View.VISIBLE);
					}
					if (_y > 50) {
						iMove = true;
						searchlayout.setVisibility(View.GONE);
					}
					break;
				case MotionEvent.ACTION_UP:
					if (!iMove) {
					}
					break;
				default:
					break;
			}
			return false;
		}
	}

	/**
	 * 
	 * 刷新ListView适配器
	 */
	private void addlist() {
		iteratorCursor();// 重新刷新Cursor
		textAdapter.notifyDataSetChanged();// 通知刷新适配器
	}

	/**
	 * 获取时间
	 * 
	 * @param calendar
	 * @param ceartmillis
	 * @return
	 */
	public String getDateString(Calendar calendar, long ceartmillis) {
		String dateString;
		Date date = new Date(ceartmillis);
		Date newdate = new Date();
		calendar.setTime(date);
		int _dateday = newdate.getDate() - calendar.get(Calendar.DAY_OF_MONTH);
		if (_dateday == 0) {
			if (calendar.get(Calendar.MINUTE) < 10) {
				dateString = "" + calendar.get(Calendar.HOUR_OF_DAY) + ":0" + calendar.get(Calendar.MINUTE);
			} else {
				dateString = "" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
			}
		} else if (_dateday == 1) {
			dateString = getText(R.string.yesterday).toString();
		} else if (_dateday > 1 && _dateday < 7) {
			int week = calendar.get(Calendar.DAY_OF_WEEK);
			switch (week) {
				case 1:
					dateString = getText(R.string.week7).toString();
					break;
				case 2:
					dateString = getText(R.string.week1).toString();
					break;
				case 3:
					dateString = getText(R.string.week2).toString();
					break;
				case 4:
					dateString = getText(R.string.week3).toString();
					break;
				case 5:
					dateString = getText(R.string.week4).toString();
					break;
				case 6:
					dateString = getText(R.string.week5).toString();
					break;
				case 7:
					dateString = getText(R.string.week6).toString();
					break;
				default:
					dateString = "";
					break;
			}
		} else {
			dateString = "" + (calendar.get(Calendar.MONTH) + 1) + getText(R.string.month)
					+ calendar.get(Calendar.DAY_OF_MONTH) + getText(R.string.day);
		}
		return dateString;
	}
}
