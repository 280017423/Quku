package com.quku.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.quku.R;
import com.quku.Utils.SystemDef;
import com.quku.entity.MyNote;
import com.quku.entity.MyNoteList;
import com.quku.service.MyNoteServiceImpl;

/**
 * 记事本（手写）列表界面
 * 
 * @author Administrator
 * 
 */
public class MyNoteListActivity extends Activity {
	private static final String TAG = "MyNoteListActivity";
	private TextView mTvTitle;
	private TextView mTvAdd;
	private ListView listView;
	private TextView cancelButton;
	private LinearLayout mLlBack;
	private LinearLayout mLlRight;
	private TextAdapter textAdapter;
	private List<MyNote> myNoteList = new ArrayList<MyNote>();
	private boolean deleteButtonShow = false;
	private Button delItemBtn;
	private ImageView delImageView;
	private MyNoteServiceImpl noteService = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_note_list);
		noteService = new MyNoteServiceImpl(this);
		initView();
		loadData();
	}

	/*
	 * 初始化view
	 */
	private void initView() {
		mTvTitle = (TextView) findViewById(R.id.title_with_back_title_btn_mid);
		mTvAdd = (TextView) findViewById(R.id.tv_title_with_right);
		listView = (ListView) findViewById(android.R.id.list);
		cancelButton = (TextView) findViewById(R.id.tv_title_with_right1);
		mLlBack = (LinearLayout) findViewById(R.id.title_with_back_title_btn_left);
		mLlRight = (LinearLayout) findViewById(R.id.title_with_back_title_btn_right);
		mLlRight.setOnClickListener(new MyButtonListenner());
		cancelButton.setOnClickListener(new MyButtonListenner());
		mLlBack.setOnClickListener(new MyButtonListenner());
		textAdapter = new TextAdapter(this);
		listView.setAdapter(textAdapter);
		mTvAdd.setText("添加");
		mTvTitle.setText("选择模版");
	}

	class MyButtonListenner implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.title_with_back_title_btn_right:// 新增
				Intent intent2 = new Intent();
				intent2.setClass(MyNoteListActivity.this,
						NoteChoiceActivity.class);
				MyNoteListActivity.this.startActivity(intent2);
				break;
			case R.id.title_with_back_title_btn_left:// 回到主菜单
				finish();
				break;
			case R.id.tv_title_with_right1:// 编辑时取消按钮
				delItemBtn.setVisibility(View.GONE);
				delImageView.setVisibility(View.VISIBLE);
				mLlRight.setVisibility(View.VISIBLE);
				cancelButton.setVisibility(View.GONE);
				deleteButtonShow = false;
				break;
			}
		}

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		loadData();
	}

	/*
	 * 加载数据
	 */
	private void loadData() {
		List<MyNote> noteList = noteService.query();
		if (null != noteList && noteList.size() > 0) {
			myNoteList = noteList;
		}
		textAdapter.notifyDataSetChanged();
	}

	private static class ItemViewCache {
		public TextView mtitleView;
		public TextView mbodyView;
	}

	/**
	 * 自定义adpter适配器
	 */
	public class TextAdapter extends BaseAdapter {

		private Context mContext = null;

		public TextAdapter(Context context) {
			this.mContext = context;
		}

		public int getCount() {
			return myNoteList.size();
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
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.item, null);
				ItemViewCache itemCache = new ItemViewCache();
				itemCache.mtitleView = (TextView) convertView
						.findViewById(R.id.title);
				itemCache.mbodyView = (TextView) convertView
						.findViewById(R.id.date);
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
							final Button deletebutton = (Button) row
									.findViewById(R.id.deletebutton);
							delItemBtn = deletebutton;
							final ImageView arrow = (ImageView) row
									.findViewById(R.id.arrow);
							delImageView = arrow;
							if (!deleteButtonShow) {
								unVisiable(delItemBtn, delImageView, true);
								delItemBtn
										.setOnClickListener(new OnClickListener() {
											public void onClick(View v) {
												unVisiable(delItemBtn,
														delImageView, false);
												delete(curPosition);
												loadData();// 刷新数据
											}
										});
							}
						}
						if (deltaY < -30) {
							isMoved = true;
							// searchlayout.setVisibility(View.VISIBLE);
						}
						if (deltaY > 30) {
							isMoved = true;
							// searchlayout.setVisibility(View.GONE);
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
			MyNote note = myNoteList.get(position);
			ivc.mtitleView.setText(note.getTitle());
			ivc.mbodyView.setText(note.getNoteTime());
			return convertView;
		}

		/**
		 * noteList当个Item选中事件
		 * 
		 * @param position
		 */
		private void onItemClick(final int position) {
			try {
				MyNote _myNote = myNoteList.get(position);// 选中每一项
				if (null != _myNote) {
					Intent intent = new Intent();
					intent.setClass(MyNoteListActivity.this,
							CreateNoteActivity.class);
					intent.putExtra("myNote", _myNote);
					intent.putExtra("type", _myNote.getNoteType());
					intent.setAction(SystemDef.NoteWrite.NOTE_EDITE);
					MyNoteListActivity.this.startActivity(intent);
					// MyNoteListActivity.this.finish();
				}
			} catch (Exception e) {
				Log.d(SystemDef.Debug.TAG,
						TAG + " onItemClick exception=" + e.getMessage());
			}

		}

		/**
		 * 删除选中项
		 * 
		 * @param position
		 */
		private void delete(final int position) {
			if (null == myNoteList || myNoteList.size() == 0)
				return;
			MyNote note = myNoteList.get(position);
			noteService.deleteMyNote(note.getId());
			MyNote _tempMyNote = noteService.getMyNote(new MyNote(note.getId(),
					null, null));
			if (null != _tempMyNote) {
				// 清空mynote
				List<MyNoteList> _tempMyNoteList = _tempMyNote.getNoteList();
				if (null != _tempMyNoteList && _tempMyNoteList.size() > 0) {
					// 清空mynotelist
					for (MyNoteList orgi_nml : _tempMyNoteList) {// 原有的数据集合
						File file = new File(orgi_nml.getPicPath());
						if (null != file && file.exists()) {
							file.delete();
						}
					}
					noteService.clearMyNoteListAll(note.getId());
				}

			}
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
			mLlRight.setVisibility(View.GONE);
			cancelButton.setVisibility(View.VISIBLE);
			deleteButtonShow = true;
		} else {
			delItemBtn.setVisibility(View.GONE);
			delImageView.setVisibility(View.VISIBLE);
			mLlRight.setVisibility(View.VISIBLE);
			cancelButton.setVisibility(View.GONE);
			deleteButtonShow = false;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		noteService.getDh().close();
	}

}