package com.quku.note;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.quku.R;
import com.quku.note.NotePad.NoteColumns;

public class Searchwindow extends Activity {

	private EditText searchEditText;
	private Button searchCancelButton;
	private ListView searchListView;
	private LinearLayout listviewLinearLayout;
	private Button delItemBtn;
	private ImageView delImageView;
	private searchTextAdapter searchTextAdapter;
	private Cursor mCursor;
	private ArrayList<String> searchtitlelist = new ArrayList<String>();
	private ArrayList<String> searchdatelist = new ArrayList<String>();
	private Calendar calendar;
	private boolean deleteButtonShow = false;
	private boolean haveDate = false;
	private Drawable mIconClear, mIconsearch;
	private View mView;
	WindowManager.LayoutParams lp;

	/**
	 * The columns we are interested in from the database
	 */
	private static final String[] PROJECTION = new String[] { NoteColumns._ID, // 0
			NoteColumns.TITLE, // 1
			NoteColumns.CREATED_DATE,// 2
			NoteColumns.MODIFIED_DATE,// 3
	};

	/** The index of the title column */
	private static final int COLUMN_INDEX_TITLE = 1;
	private static final int COLUMN_INDEX_CREATED_DATE = 2;
	private static final int COLUMN_INDEX_MODIFIED_DATE = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.searchwindow);
		final Resources res = getResources();
		mIconsearch = res.getDrawable(R.drawable.searchbox);
		mIconClear = res.getDrawable(R.drawable.close);

		searchEditText = (EditText) findViewById(R.id.searchedittext);
		searchCancelButton = (Button) findViewById(R.id.searchcancelbotton);
		searchListView = (ListView) findViewById(android.R.id.list);
		listviewLinearLayout = (LinearLayout) findViewById(R.id.listlayout);

		// If no data was given in the intent (because we were started
		// as a MAIN activity), then use our default content provider.
		Intent intent = getIntent();
		if (intent.getData() == null) {
			intent.setData(NoteColumns.CONTENT_URI);
		} else {
			// addlist();
		}
		searchCancelButton.setOnClickListener(new MyOnClickListener());
		searchEditText.addTextChangedListener(new SearchEditTextWatcher());
		searchEditText.setOnTouchListener(new MySearchTextOnTouchListener());
		searchListView.setOnTouchListener(new ListviewOnTouch());
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		float x = ev.getX();
		float y = ev.getY();
		if (!haveDate) {
			if (y < 150) {
				return super.dispatchTouchEvent(ev);
			} else {
				finish();
				return true;
			}
		} else {
			return super.dispatchTouchEvent(ev);
		}
	}

	/**
	 * edit监听事件
	 * 
	 * @author Administrator
	 * 
	 */
	public class SearchEditTextWatcher implements TextWatcher {
		public void afterTextChanged(Editable s) {
			searchtitlelist.clear();
			searchdatelist.clear();
			String searchText = s.toString().trim();
			mCursor = managedQuery(getIntent().getData(), PROJECTION, null, null, NoteColumns.DEFAULT_SORT_ORDER);
			if (searchText.equals("")) {
				searchEditText.setCompoundDrawablesWithIntrinsicBounds(mIconsearch, null, null, null);
				searchTextAdapter = new searchTextAdapter(Searchwindow.this);
				searchListView.setAdapter(searchTextAdapter);
				addlist();
				haveDate = false;
				listviewLinearLayout.setBackgroundColor(Color.DKGRAY);
			} else {
				searchEditText.setCompoundDrawablesWithIntrinsicBounds(mIconsearch, null, mIconClear, null);
				if (mCursor != null) {
					while (mCursor.moveToNext()) {
						String title = mCursor.getString(COLUMN_INDEX_TITLE);
						System.out.println(title.startsWith(s.toString()));
						if (title.startsWith(s.toString())) {
							// 设置时间为中国
							calendar = Calendar.getInstance(Locale.CHINA);
							long dateTime = Long.parseLong(mCursor.getString(COLUMN_INDEX_CREATED_DATE));
							Date date = new Date(dateTime);
							calendar.setTime(date);
							String dateString;
							if (calendar.get(Calendar.MINUTE) < 10) {
								dateString = "" + calendar.get(Calendar.HOUR_OF_DAY) + ":0"
										+ calendar.get(Calendar.MINUTE);
							} else {
								dateString = "" + calendar.get(Calendar.HOUR_OF_DAY) + ":"
										+ calendar.get(Calendar.MINUTE);
							}
							searchtitlelist.add(title);
							searchdatelist.add(dateString);
						}
					}
					if (searchtitlelist.size() > 0) {
						haveDate = true;
						listviewLinearLayout.setBackgroundResource(R.drawable.body);
					}
					searchTextAdapter = new searchTextAdapter(Searchwindow.this);
					searchListView.setAdapter(searchTextAdapter);
					addlist();
				}
			}

		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

	}

	/**
	 * adpter适配器
	 * 
	 */
	public class searchTextAdapter extends BaseAdapter {

		private Context mContext = null;

		public searchTextAdapter(Context context) {
			this.mContext = context;

		}

		public int getCount() {
			return searchtitlelist.size();
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
				itemCache.mtitleView.setText(searchtitlelist.get(position));
				itemCache.mbodyView.setText(searchdatelist.get(position));
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
							// System.out.println("deltaY" + deltaY);
							if (deltaX > 70) {
								isMoved = true;
								final Button deletebutton = (Button) row.findViewById(R.id.deletebutton);
								delItemBtn = deletebutton;
								final ImageView arrow = (ImageView) row.findViewById(R.id.arrow);
								delImageView = arrow;
								if (!deleteButtonShow) {
									searchCancelButton.setVisibility(View.GONE);
									searchEditText.setWidth(500);
									delItemBtn.setVisibility(View.VISIBLE);
									delImageView.setVisibility(View.GONE);
									deleteButtonShow = true;
									delItemBtn.setOnClickListener(new OnClickListener() {

										public void onClick(View v) {
											delItemBtn.setVisibility(View.GONE);
											delImageView.setVisibility(View.VISIBLE);
											delete(curPosition);
											searchtitlelist.remove(curPosition);
											searchdatelist.remove(curPosition);
											addlist();
										}
									});
								}
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
			ivc.mtitleView.setText(searchtitlelist.get(position));
			ivc.mbodyView.setText(searchdatelist.get(position));
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

			System.out.println("onListItemClick moveTo =" + mCursor.moveToPosition(position));
			// 组装搜索的结果，跳转到文本显示界面
			// while (mCursor.moveToNext()) {
			// String id = mCursor.getString(COLUMN_INDEX_ID);
			// String title = mCursor.getString(COLUMN_INDEX_TITLE);
			// String note = mCursor.getString(COLUMN_INDEX_NOTE);
			// String modifiedate =
			// mCursor.getString(COLUMN_INDEX_CREATED_DATE);
			// // String dateTime = mCursor
			// // .getString(COLUMN_INDEX_CREATED_DATE);
			// // 设置时间为中国
			// calendar = Calendar.getInstance(Locale.CHINA);
			// long dateTime = Long.parseLong(mCursor
			// .getString(COLUMN_INDEX_CREATED_DATE));
			// String dateString = getDateString(calendar, dateTime);
			// idlist.add(id);
			// titlelist.add(title);
			// datelist.add(dateString);
			// notelist.add(note);
			// modifiedlist.add(modifiedate);
			// }
			int rowId = mCursor.getInt(mCursor.getColumnIndexOrThrow(NotePad.NoteColumns._ID));

			Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), rowId);
			System.out.println(" listview onlistItemClick noteUri=" + noteUri);

			String action = getIntent().getAction();
			System.out.println(" listview onlistItemClick action=" + action);
			if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
				// The caller is waiting for us to return a note selected by
				// the user. The have clicked on one, so return it now.
				setResult(RESULT_OK, new Intent().setData(noteUri));
			} else {
				// Launch activity to view/edit the currently selected item
				Intent intent = new Intent(Intent.ACTION_EDIT, noteUri);
				// intent.putExtra("detailInfo", "true");
				startActivity(intent);
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

	/*
	 * 刷新ListView适配器
	 */
	private void addlist() {
		searchTextAdapter.notifyDataSetChanged();
	}

	public class ListviewOnTouch implements OnTouchListener {
		float ldPointX = 0;
		float ldPointY = 0;

		public boolean onTouch(View v, MotionEvent event) {
			if (deleteButtonShow) {
				searchEditText.setWidth(300);
				searchCancelButton.setVisibility(View.VISIBLE);
				delItemBtn.setVisibility(View.GONE);// 屏蔽delete删除按钮
				delImageView.setVisibility(View.VISIBLE);
				deleteButtonShow = false;

			}
			int listaction = event.getAction();
			float x = event.getX();
			float y = event.getY();
			boolean iMove = false;
			switch (listaction) {
				case MotionEvent.ACTION_DOWN:
					iMove = false;
					ldPointX = x;
					ldPointY = y;
					break;
				case MotionEvent.ACTION_MOVE:
					float _x = Math.abs(ldPointX - x);
					float _y = ldPointY - y;
					if (_x > 5 || _y > 5 || _y < -5) {
						iMove = true;
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

	public class MyOnClickListener implements OnClickListener {

		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.searchcancelbotton:
					finish();
					break;

				default:
					break;
			}
		}

	}

	public class MySearchTextOnTouchListener implements OnTouchListener {

		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					int curX = (int) event.getX();
					if (curX > (v.getWidth() - 50) && !TextUtils.isEmpty(searchEditText.getText())) {
						searchEditText.setText("");
					}
					break;

				default:
					break;
			}
			return false;
		}

	}
}
