package com.quku.note;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.quku.R;
import com.quku.note.LijieSurfaceView.MeshFinishListener;
import com.quku.note.NotePad.NoteColumns;
import com.quku.note.PageWidget.OnPageScrollFinishedListener;

public class NoteEditor extends Activity implements MeshFinishListener, OnPageScrollFinishedListener {
	private static final String TAG = "NoteEditor";

	private static final String[] PROJECTION = new String[] { NoteColumns._ID, // 0
			NoteColumns.NOTE, // 1
			NoteColumns.TITLE, // 2
			NoteColumns.CREATED_DATE,// 3
			NoteColumns.MODIFIED_DATE,// 4
	};
	private static final int COLUMN_INDEX_ID = 0;
	/** The index of the note column */
	private static final int COLUMN_INDEX_NOTE = 1;
	/** The index of the title column */
	private static final int COLUMN_INDEX_TITLE = 2;
	/** The index of the created date column */
	private static final int COLUMN_INDEX_CREATED_DATE = 3;

	// This is our state data that is stored when freezing.
	private static final String ORIGINAL_CONTENT = "origContent";

	// The different distinct states the activity can be run in.
	private static final int STATE_EDIT = 0;
	private static final int STATE_INSERT = 1;

	private int mState;
	private int my_year, my_month, my_day, my_hour, my_minute, my_second;
	private Calendar calendar;
	private Uri mUri;
	private Cursor mCursor, allCursor;
	private ViewGroup mContentView;
	private TextView editorTitle, dataTitle;
	private EditText mText;
	private String mOriginalContent;
	private Button bodyButton, addnoteButton, finishButton;
	private ImageView arrowLeftView, trashView, arrowRightView;
	// private ImageView emailView;
	private ArrayList<String> idlist = new ArrayList<String>();
	private ArrayList<String> titlelist = new ArrayList<String>();
	private ArrayList<String> notelist = new ArrayList<String>();
	private ArrayList<String> modifiedlist = new ArrayList<String>();
	private int id;
	private PageWidget mPageWidget;
	private Dialog deleteDialog;
	private LijieSurfaceView mMeshView;
	private LinearLayout mianLinLayout;

	/**
	 * A custom EditText that draws lines between each line of text that is
	 * displayed.
	 */
	public static class LinedEditText extends EditText {
		private Rect mRect;
		private Paint mPaint;

		// we need this constructor for LayoutInflater
		public LinedEditText(Context context, AttributeSet attrs) {
			super(context, attrs);

			mRect = new Rect();
			mPaint = new Paint();
			mPaint.setStyle(Paint.Style.FILL);
			mPaint.setStrokeWidth(2);
			mPaint.setColor(0x80AAAAAA);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().getDecorView().setSystemUiVisibility(4);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContentView = (ViewGroup) inflater.inflate(R.layout.note_editor, null);
		setContentView(mContentView);

		mianLinLayout = (LinearLayout) findViewById(R.id.mianlayout);
		mText = (EditText) findViewById(R.id.note);
		mText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				return false;
			}
		});
		editorTitle = (TextView) findViewById(R.id.editortitle);
		dataTitle = (TextView) findViewById(R.id.datatitle);
		bodyButton = (Button) findViewById(R.id.bodybutton);
		finishButton = (Button) findViewById(R.id.finishbutton);
		addnoteButton = (Button) findViewById(R.id.addnotebutton);
		arrowLeftView = (ImageView) findViewById(R.id.arrowleftview);
		// emailView = (ImageView) findViewById(R.id.emailview);
		trashView = (ImageView) findViewById(R.id.trashview);
		arrowRightView = (ImageView) findViewById(R.id.arrowrightview);

		arrowLeftView.setBackgroundResource(R.drawable.arrowleft);
		// emailView.setBackgroundResource(R.drawable.emailsend);
		trashView.setBackgroundResource(R.drawable.trash);
		arrowRightView.setBackgroundResource(R.drawable.arrowright);

		// 设置时间为中国
		calendar = Calendar.getInstance(Locale.CHINA);
		// 设置时间格式为24小时
		// time_picker.setIs24HourView(true);
		// 获取日期
		my_year = calendar.get(Calendar.YEAR);
		my_month = calendar.get(Calendar.MONTH); // 注意下，月份是从0开始的，要Calendar.MONTH+1才可以的
		my_day = calendar.get(Calendar.DAY_OF_MONTH);
		my_hour = calendar.get(Calendar.HOUR_OF_DAY);
		my_minute = calendar.get(Calendar.MINUTE);
		my_second = calendar.get(Calendar.SECOND);

		// If an instance of this activity had previously stopped, we can
		final Intent intent = getIntent();
		titlelist = (ArrayList<String>) intent.getSerializableExtra("titlelist");
		notelist = (ArrayList<String>) intent.getSerializableExtra("notelist");
		modifiedlist = (ArrayList<String>) intent.getSerializableExtra("modifiedlist");
		idlist = (ArrayList<String>) intent.getSerializableExtra("idlist");
		id = intent.getIntExtra("id", 0);
		// Do some setup based on the action being performed.
		final String action = intent.getAction();
		if (Intent.ACTION_EDIT.equals(action)) {
			// Requested to edit: set that state, and the data being edited.
			mState = STATE_EDIT;
			mUri = intent.getData();
			finishButton.setVisibility(View.GONE);
			addnoteButton.setVisibility(View.VISIBLE);
			int isend = 0;
			if (null != titlelist) {
				isend = titlelist.size() - 1;
			}
			if (id == 0) {
				arrowLeftView.setBackgroundResource(R.drawable.arrowleft_enable);
			}
			if (id == isend) {
				arrowRightView.setBackgroundResource(R.drawable.arrowright_enable);
			}
		} else if (Intent.ACTION_INSERT.equals(action)) {
			// Requested to insert: set that state, and create a new entry
			// in the container.
			mText.setFocusable(true);
			mState = STATE_INSERT;
			mUri = getContentResolver().insert(intent.getData(), null);
			arrowLeftView.setBackgroundResource(R.drawable.arrowleft_enable);
			arrowRightView.setBackgroundResource(R.drawable.arrowright_enable);
			arrowLeftView.setEnabled(false);
			arrowRightView.setEnabled(false);
			trashView.setEnabled(false);
			if (mUri == null) {
				Log.e(TAG, "Failed to insert new note into " + getIntent().getData());
				finish();
				return;
			}
			setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));

		} else {
			finish();
			return;
		}

		// Get the note!
		mCursor = managedQuery(mUri, PROJECTION, null, null, null);
		if (savedInstanceState != null) {
			mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
		}
		bodyButton.setOnClickListener(new MyOnClickListener());
		addnoteButton.setOnClickListener(new MyOnClickListener());
		finishButton.setOnClickListener(new MyOnClickListener());

		arrowLeftView.setOnClickListener(new MyOnClickListener());
		trashView.setOnClickListener(new MyOnClickListener());
		arrowRightView.setOnClickListener(new MyOnClickListener());

		arrowLeftView.setOnTouchListener(new MyOnTouchListener());
		trashView.setOnTouchListener(new MyOnTouchListener());
		arrowRightView.setOnTouchListener(new MyOnTouchListener());
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		return super.dispatchTouchEvent(event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mCursor != null && mCursor.getCount() != 0) {
			if (mState == STATE_EDIT) {
				if (mCursor.getCount() != 0) {
					String title = mCursor.getString(COLUMN_INDEX_TITLE);
					editorTitle.setText(title);
					Resources res = getResources();
					// String text =
					// String.format(res.getString(R.string.title_edit),title);
					// SimpleDateFormat df = new
					// SimpleDateFormat("yyyyMMddhhmmss");
					long dateMillis = Long.parseLong(mCursor.getString(COLUMN_INDEX_CREATED_DATE));
					calendar = Calendar.getInstance(Locale.CHINA);
					String dateString = getDateString(calendar, dateMillis);
					dataTitle.setText(dateString);
				}
				// setTitle(text);
			} else if (mState == STATE_INSERT) {
				// 显示时间
				if (my_minute < 10) {
					dataTitle.setText("        " + getText(R.string.today).toString()
							+ "                              " + (my_month + 1) + getText(R.string.month) + my_day
							+ getText(R.string.day) + "   " + my_hour + ":0" + my_minute);
				} else {
					dataTitle.setText("        " + getText(R.string.today).toString()
							+ "                              " + (my_month + 1) + getText(R.string.month) + my_day
							+ getText(R.string.day) + "   " + my_hour + ":" + my_minute);
				}
				editorTitle.setText(R.string.notes_edit_title);
				mText.addTextChangedListener(new EditTextWatcher());
				// setTitle(getText(R.string.title_create));
			}

			// This is a little tricky: we may be resumed after previously being
			// paused/stopped. We want to put the new text in the text view,
			// but leave the user where they were (retain the cursor position
			// etc). This version of setText does that for us.
			String note = mCursor.getString(COLUMN_INDEX_NOTE);
			mText.setTextKeepState(note);

			// If we hadn't previously retrieved the original text, do so
			// now. This allows the user to revert their changes.
			if (mOriginalContent == null) {
				mOriginalContent = note;
			}

		} else {
			// setTitle(getText(R.string.error_title));
			mText.setText(getText(R.string.error_message));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Save away the original text, so we still have it if the activity
		// needs to be killed while paused.
		outState.putString(ORIGINAL_CONTENT, mOriginalContent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// The user is going somewhere, so make sure changes are saved

		String text = mText.getText().toString().trim();
		int length = text.length();

		// If this activity is finished, and there is no text, then we simply
		// delete the note entry.
		// Note that we do this both for editing and inserting... it would be
		// reasonable to only do it when inserting.
		if (length == 0) {
			setResult(RESULT_CANCELED);
			deleteNote();
		} else {
			saveNote();
		}
	}

	private final void saveNote() {
		// Make sure their current
		// changes are safely saved away in the provider. We don't need
		// to do this if only editing.
		if (mCursor != null) {
			// Get out updates into the provider.
			ContentValues values = new ContentValues();

			// Bump the modification time to now.
			values.put(NoteColumns.MODIFIED_DATE, System.currentTimeMillis());

			String mtext = mText.getText().toString().trim();
			int length = mtext.length();
			if (mState == STATE_INSERT) {
				String title = mtext.substring(0, Math.min(16, length));
				if (length > 16) {
					int lastSpace = title.lastIndexOf(' ');
					if (lastSpace > 0) {
						title = title.substring(0, lastSpace);
					}
				}
				values.put(NoteColumns.TITLE, title);
			}
			String title = mtext.substring(0, Math.min(16, length));
			if (length > 16) {
				int lastSpace = title.lastIndexOf(' ');
				if (lastSpace > 0) {
					title = title.substring(0, lastSpace);
				}
			}
			values.put(NoteColumns.TITLE, title);
			values.put(NoteColumns.NOTE, mtext);
			try {
				getContentResolver().update(mUri, values, null, null);
			} catch (NullPointerException e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}

	/**
	 * Take care of canceling work on a note. Deletes the note if we had created
	 * it, otherwise reverts to the original text.
	 */
	private final void cancelNote() {
		if (mCursor != null) {
			if (mState == STATE_EDIT) {
				// Put the original note text back into the database
				mCursor.close();
				mCursor = null;
				ContentValues values = new ContentValues();
				values.put(NoteColumns.NOTE, mOriginalContent);
				getContentResolver().update(mUri, values, null, null);
			} else if (mState == STATE_INSERT) {
				// We inserted an empty note, make sure to delete it
				deleteNote();
			}
		}
		setResult(RESULT_CANCELED);
		finish();
	}

	/**
	 * Take care of deleting a note. Simply deletes the entry.
	 */
	private final void deleteNote() {
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
			getContentResolver().delete(mUri, null, null);
			// mText.setText("");
			// if (my_minute < 10) {
			// dataTitle.setText("        "
			// + getText(R.string.today).toString()
			// + "                              " + (my_month + 1)
			// + getText(R.string.month) + my_day
			// + getText(R.string.day) + "   " + my_hour + ":0"
			// + my_minute);
			// } else {
			// dataTitle.setText("        "
			// + getText(R.string.today).toString()
			// + "                              " + (my_month + 1)
			// + getText(R.string.month) + my_day
			// + getText(R.string.day) + "   " + my_hour + ":"
			// + my_minute);
			// }
			// editorTitle.setText(R.string.notes_edit_title);

		}
	}

	/**
	 * 
	 * 点击事件的监听
	 * 
	 * @author Administrator
	 * 
	 */
	public class MyOnClickListener implements OnClickListener {

		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.bodybutton:
					Intent intentmain = new Intent();
					intentmain.setClass(NoteEditor.this, NotesList.class);
					NoteEditor.this.startActivity(intentmain);
					finish();
					break;
				case R.id.finishbutton:
					String text = mText.getText().toString().trim();
					int length = text.length();
					if (text.equals("") || length == 0) {
						setResult(RESULT_CANCELED);
						deleteNote();
						Intent mintent = new Intent();
						mintent.setClass(NoteEditor.this, NotesList.class);
						NoteEditor.this.startActivity(mintent);
						finish();
					} else {
						trashView.setEnabled(true);
						saveNote();
						finishButton.setVisibility(View.GONE);
						addnoteButton.setVisibility(View.VISIBLE);
						addList();
					}
					break;
				case R.id.addnotebutton:
					mText.setFocusable(true);
					startActivity(new Intent(Intent.ACTION_INSERT, NoteColumns.CONTENT_URI));
					finish();
					break;
				default:
					break;
			}
		}
	}

	private class MyDialogOnClickListener implements OnClickListener {

		public void onClick(View view) {
			switch (view.getId()) {
				case R.id.deletedeletebutton:
					deleteDialog.dismiss();
					deleteNote();
					if (id >= 0 && (id + 1) < titlelist.size() && (titlelist.size() != 1)) {
						int i = id + 1;
						int noteid = Integer.valueOf(idlist.get(i));
						mUri = ContentUris.withAppendedId(NotePad.NoteColumns.CONTENT_URI, noteid);
						iteratorCursor();
						Intent intent = new Intent(Intent.ACTION_EDIT, mUri);
						intent.putExtra("titlelist", (Serializable) titlelist);
						intent.putExtra("notelist", (Serializable) notelist);
						intent.putExtra("modifiedlist", (Serializable) modifiedlist);
						intent.putExtra("idlist", (Serializable) idlist);
						intent.putExtra("id", i);
						NoteEditor.this.startActivity(intent);
						finish();
					} else if (id >= 0 && (id + 1) <= titlelist.size() && (titlelist.size() != 1)) {
						int i = id - 1;
						int noteid = Integer.valueOf(idlist.get(i));
						mUri = ContentUris.withAppendedId(NotePad.NoteColumns.CONTENT_URI, noteid);
						iteratorCursor();
						Intent intent = new Intent(Intent.ACTION_EDIT, mUri);
						intent.putExtra("titlelist", (Serializable) titlelist);
						intent.putExtra("notelist", (Serializable) notelist);
						intent.putExtra("modifiedlist", (Serializable) modifiedlist);
						intent.putExtra("idlist", (Serializable) idlist);
						intent.putExtra("id", i);
						NoteEditor.this.startActivity(intent);
						finish();
					} else {
						Intent intentmain = new Intent();
						intentmain.setClass(NoteEditor.this, NotesList.class);
						NoteEditor.this.startActivity(intentmain);
						finish();
					}
					break;
				case R.id.deletecancelbutton:
					deleteDialog.dismiss();
					break;
				default:
					break;
			}

		}

	}

	/**
	 * 监听按钮按下事件
	 * 
	 * @author winner
	 * 
	 */
	private class MyOnTouchListener implements OnTouchListener {

		public boolean onTouch(View view, MotionEvent event) {
			int mEvent = event.getAction();
			switch (view.getId()) {
				case R.id.arrowleftview:
					switch (mEvent) {
						case MotionEvent.ACTION_DOWN:
							view.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrowleft_down));
							break;
						case MotionEvent.ACTION_MOVE:

							break;
						case MotionEvent.ACTION_UP:
							view.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrowleft));
							if (id > 0 && id <= titlelist.size() && (titlelist.size() != 0) && (titlelist != null)) {
								int i = --id;
								int noteid = Integer.valueOf(idlist.get(i));
								String editTitle = titlelist.get(i);
								String editText = notelist.get(i);
								long datatitleText = Long.parseLong(modifiedlist.get(i));
								String dateText = getDateString(calendar, datatitleText);
								dataTitle.setText(dateText);
								mText.setText(editText);
								editorTitle.setText(editTitle);
								mUri = ContentUris.withAppendedId(NotePad.NoteColumns.CONTENT_URI, noteid);
								arrowRightView.setEnabled(true);
								arrowRightView.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrowright));
							} else {
								arrowLeftView.setEnabled(false);
								arrowLeftView.setBackgroundDrawable(getResources().getDrawable(
										R.drawable.arrowleft_enable));
							}

							break;

						default:
							break;
					}
					break;
				// case R.id.emailview:
				// switch (mEvent) {
				// case MotionEvent.ACTION_DOWN:
				// view.setBackgroundResource(R.drawable.emailsend_down);
				// break;
				// case MotionEvent.ACTION_MOVE:
				//
				// break;
				// case MotionEvent.ACTION_UP:
				// view.setBackgroundDrawable(getResources().getDrawable(R.drawable.emailsend));
				// emailDialog = new Dialog(NoteEditor.this,
				// android.R.style.Theme_Translucent_NoTitleBar);
				// emailDialog.setContentView(R.layout.dialog_email);
				// emailDialog.show();
				// final Button emailSendButton = (Button)
				// emailDialog.findViewById(R.id.emailsendbutton);
				// final Button emailPrintButton = (Button)
				// emailDialog.findViewById(R.id.emailprintbutton);
				// final Button emailCancelButton = (Button)
				// emailDialog.findViewById(R.id.emailcancelbutton);
				// emailSendButton.setOnClickListener(new
				// MyDialogOnClickListener());
				// emailPrintButton.setOnClickListener(new
				// MyDialogOnClickListener());
				// emailCancelButton.setOnClickListener(new
				// MyDialogOnClickListener());
				// break;
				//
				// default:
				// break;
				// }
				// break;
				case R.id.trashview:
					switch (mEvent) {
						case MotionEvent.ACTION_DOWN:
							view.setBackgroundResource(R.drawable.trash_down);
							break;
						case MotionEvent.ACTION_MOVE:

							break;
						case MotionEvent.ACTION_UP:
							view.setBackgroundDrawable(getResources().getDrawable(R.drawable.trash));

							deleteDialog = new Dialog(NoteEditor.this, android.R.style.Theme_Translucent_NoTitleBar);
							deleteDialog.setContentView(R.layout.dialog_delete);
							deleteDialog.show();
							final Button deleteDeleteButton = (Button) deleteDialog
									.findViewById(R.id.deletedeletebutton);
							final Button deleteCancelButton = (Button) deleteDialog
									.findViewById(R.id.deletecancelbutton);
							deleteDeleteButton.setOnClickListener(new MyDialogOnClickListener());
							deleteCancelButton.setOnClickListener(new MyDialogOnClickListener());
							break;

						default:
							break;
					}
					break;
				case R.id.arrowrightview:
					switch (mEvent) {
						case MotionEvent.ACTION_DOWN:
							view.setBackgroundResource(R.drawable.arrowright_down);
							break;
						case MotionEvent.ACTION_MOVE:

							break;
						case MotionEvent.ACTION_UP:
							view.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrowright));
							if (id >= 0 && id < (titlelist.size() - 1)
									&& (titlelist.size() != 0 && (titlelist != null))) {

								// mContentView.setDrawingCacheEnabled(true);
								// Bitmap bitmap =
								// mContentView.getDrawingCache();
								// mPageWidget = new PageWidget(NoteEditor.this,
								// bitmap);
								// mContentView.addView(mPageWidget, new
								// ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
								// ViewGroup.LayoutParams.MATCH_PARENT));
								// mPageWidget.setBitmaps(bitmap, bitmap);
								// mPageWidget.autoPaly();
								// mPageWidget.setOnPageScrollFinishedListener(NoteEditor.this);
								// bitmap.recycle();
								// mContentView.destroyDrawingCache();

								int i = ++id;
								int noteid = Integer.valueOf(idlist.get(i));
								String editTitle = titlelist.get(i);
								String editText = notelist.get(i);
								long datatitleText = Long.parseLong(modifiedlist.get(i));
								String dateText = getDateString(calendar, datatitleText);
								dataTitle.setText(dateText);
								mText.setText(editText);
								editorTitle.setText(editTitle);
								mUri = ContentUris.withAppendedId(NotePad.NoteColumns.CONTENT_URI, noteid);
								arrowLeftView.setEnabled(true);
								arrowLeftView.setBackgroundDrawable(getResources().getDrawable(R.drawable.arrowleft));
							} else {
								arrowRightView.setEnabled(false);
								arrowRightView.setBackgroundDrawable(getResources().getDrawable(
										R.drawable.arrowright_enable));
							}

							break;

						default:
							break;
					}
					break;
			}
			return false;
		}

	}

	/**
	 * 监听搜索框内容改变
	 * 
	 * @author Administrator
	 * 
	 */
	public class EditTextWatcher implements TextWatcher {
		public void afterTextChanged(Editable s) {
			if (s.toString().trim().equals("")) {
				editorTitle.setText(R.string.notes_edit_title);
			} else {
				if (s.toString().trim().length() > 16) {
					String editTitle = s.toString().trim().substring(0, 16);
					editorTitle.setText(editTitle);
				} else {
					editorTitle.setText(s.toString().trim());
				}
			}
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

	}

	/**
	 * 根据毫秒数得到时间
	 * 
	 * @param calendar
	 * @param ceartmillis
	 * @return
	 */
	public String getDateString(Calendar calendar, long ceartmillis) {
		String dateText;
		Date date = new Date(ceartmillis);
		calendar.setTime(date);
		Date newdate = new Date();
		int _dateday = newdate.getDate() - calendar.get(Calendar.DAY_OF_MONTH);
		if (_dateday == 0) {
			if (calendar.get(Calendar.MINUTE) < 10) {
				dateText = "        " + getText(R.string.today).toString() + "                                  "
						+ (calendar.get(Calendar.MONTH) + 1) + getText(R.string.month)
						+ calendar.get(Calendar.DAY_OF_MONTH) + getText(R.string.day) + "   "
						+ calendar.get(Calendar.HOUR_OF_DAY) + ":0" + calendar.get(Calendar.MINUTE);
			} else {
				dateText = "        " + getText(R.string.today).toString() + "                                  "
						+ (calendar.get(Calendar.MONTH) + 1) + getText(R.string.month)
						+ calendar.get(Calendar.DAY_OF_MONTH) + getText(R.string.day) + "   "
						+ calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
			}
		} else if (_dateday == 1) {
			if (calendar.get(Calendar.MINUTE) < 10) {
				dateText = "        " + getText(R.string.yesterday).toString() + "                              "
						+ (calendar.get(Calendar.MONTH) + 1) + getText(R.string.month)
						+ calendar.get(Calendar.DAY_OF_MONTH) + getText(R.string.day) + "   "
						+ calendar.get(Calendar.HOUR_OF_DAY) + ":0" + calendar.get(Calendar.MINUTE);
			} else {
				dateText = "        " + getText(R.string.yesterday).toString() + "                              "
						+ (calendar.get(Calendar.MONTH) + 1) + getText(R.string.month)
						+ calendar.get(Calendar.DAY_OF_MONTH) + getText(R.string.day) + "   "
						+ calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
			}
		} else if (_dateday > 1) {
			if (calendar.get(Calendar.MINUTE) < 10) {
				dateText = "        " + _dateday + getText(R.string.daybeffer).toString()
						+ "                           " + (calendar.get(Calendar.MONTH) + 1) + getText(R.string.month)
						+ calendar.get(Calendar.DAY_OF_MONTH) + getText(R.string.day) + "   "
						+ calendar.get(Calendar.HOUR_OF_DAY) + ":0" + calendar.get(Calendar.MINUTE);
			} else {
				dateText = "        " + _dateday + getText(R.string.daybeffer).toString()
						+ "                           " + (calendar.get(Calendar.MONTH) + 1) + getText(R.string.month)
						+ calendar.get(Calendar.DAY_OF_MONTH) + getText(R.string.day) + "   "
						+ calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
			}
		} else {
			if (calendar.get(Calendar.MINUTE) < 10) {
				dateText = "                                               " + (calendar.get(Calendar.MONTH) + 1)
						+ getText(R.string.month) + calendar.get(Calendar.DAY_OF_MONTH) + getText(R.string.day) + "   "
						+ calendar.get(Calendar.HOUR_OF_DAY) + ":0" + calendar.get(Calendar.MINUTE);
			} else {
				dateText = "                                                " + (calendar.get(Calendar.MONTH) + 1)
						+ getText(R.string.month) + calendar.get(Calendar.DAY_OF_MONTH) + getText(R.string.day) + "   "
						+ calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
			}
		}
		return dateText;

	}

	/**
	 * 
	 * 得到list数据
	 */
	public void addList() {
		allCursor = managedQuery(NoteColumns.CONTENT_URI, PROJECTION, null, null, NoteColumns.DEFAULT_SORT_ORDER);
		if (allCursor != null) {
			idlist = new ArrayList<String>();
			titlelist = new ArrayList<String>();
			notelist = new ArrayList<String>();
			modifiedlist = new ArrayList<String>();
			while (allCursor.moveToNext()) {
				String id = mCursor.getString(COLUMN_INDEX_ID);
				String title = allCursor.getString(COLUMN_INDEX_TITLE);
				String note = allCursor.getString(COLUMN_INDEX_NOTE);
				String modifiedate = allCursor.getString(COLUMN_INDEX_CREATED_DATE);
				idlist.add(id);
				titlelist.add(title);
				notelist.add(note);
				modifiedlist.add(modifiedate);
			}
		}
	}

	public void onFinished() {
		if (mMeshView != null) {
			mContentView.removeView(mMeshView);
		}
		mMeshView = null;
	}

	public void onPageScrollFinished(View v) {
		if (mPageWidget != null) {
			mContentView.removeView(mPageWidget);
			mPageWidget = null;
		}
	}

	/**
	 * 遍历Cursor数据，组装适配器数据
	 */
	private void iteratorCursor() {
		Cursor myCursor = managedQuery(NoteColumns.CONTENT_URI, PROJECTION, null, null, NoteColumns.DEFAULT_SORT_ORDER);
		if (myCursor != null) {
			idlist = new ArrayList<String>();
			titlelist = new ArrayList<String>();
			notelist = new ArrayList<String>();
			modifiedlist = new ArrayList<String>();
			while (myCursor.moveToNext()) {
				String id = myCursor.getString(COLUMN_INDEX_ID);
				String title = myCursor.getString(COLUMN_INDEX_TITLE);
				String note = myCursor.getString(COLUMN_INDEX_NOTE);
				String modifiedate = myCursor.getString(COLUMN_INDEX_CREATED_DATE);
				idlist.add(id);
				titlelist.add(title);
				notelist.add(note);
				modifiedlist.add(modifiedate);
			}
		}
	}
}
