package com.quku.record;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quku.R;
import com.quku.activity.RecordListActivity;
import com.quku.entity.Record;
import com.quku.note2.MyDialogBuilder;

/**
 * 录音文件数据适配器
 * 
 * @author Administrator
 * 
 */
public class MyRecordAdapter extends BaseAdapter {

	private static final int MESSAGE_PLAY_START = 1;
	private static final int MESSAGE_RECORD_DIR_FLUSH = 3;// 刷新录音列表加载目录
	private Handler recordHandler;
	private Context mContext;
	private List<Record> recordList;
	private MyRecordAdapter myAdapter;
	MyDialogBuilder myAlertDialogbd;
	private String selectFilePath;// 当前选择操作的文件

	public String getSelectFilePath() {
		return selectFilePath;
	}

	public void setSelectFilePath(String selectFilePath) {
		this.selectFilePath = selectFilePath;
	}

	public MyRecordAdapter(Context context, List<Record> recordList, Handler recordHandler) {
		myAdapter = this;
		this.mContext = context;
		this.recordList = recordList;
		this.recordHandler = recordHandler;
	}

	@Override
	public int getCount() {
		return recordList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewCache viewCache = null;
		if (null == convertView) {
			viewCache = new ViewCache();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_recordlist_item, null);
			viewCache.recordfilename = (TextView) convertView.findViewById(R.id.recordfilename);
			viewCache.recordfilesize = (TextView) convertView.findViewById(R.id.recordfilesize);
			convertView.setTag(viewCache);
		}
		viewCache = (ViewCache) convertView.getTag();
		convertView.setOnClickListener(new MyOnclickListenner(position, convertView));
		// convertView.setOnLongClickListener(new
		// MyOnLongClickListenner(mContext));
		Record record = recordList.get(position);
		if (record.isSeleted()) {
			convertView.setBackgroundColor(Color.BLUE);
		} else {
			convertView.setBackgroundColor(0);
		}
		if (record.getType() == 0) {// 文件
			viewCache.recordfilesize.setText(record.getFileSize());
		}
		viewCache.recordfilename.setText(record.getFileName());
		return convertView;
	}

	static class ViewCache {
		TextView recordfilename;
		TextView recordfilesize;
	}

	class MyOnclickListenner implements OnClickListener {

		private int position;
		private View view;// 当前操作的view

		public MyOnclickListenner(int position, View convertView) {
			this.position = position;
			this.view = convertView;
		}

		@Override
		public void onClick(View v) {
			if (null == recordList || recordList.size() == 0)
				return;
			Record record = recordList.get(position);
			if (record == null)
				return;
			File _file = new File(record.getFilePath());
			if (_file.isDirectory()) {// 当前文件是目录
				recordList = RecordFileUtil.loadFileList(_file.getPath(), recordList);
				((RecordListActivity) mContext).initLoadPlayList(recordList);
				// 刷新加载listview
				myAdapter.notifyDataSetChanged();
				// 刷新当前目录路径
				Message msg = new Message();
				msg.what = MESSAGE_RECORD_DIR_FLUSH;
				msg.obj = _file.getPath();
				recordHandler.sendMessage(msg);
			} else {
				view.setBackgroundColor(Color.BLUE);
				// 是文件弹出对话框
				myAlertDialogbd = (MyDialogBuilder) new MyDialogBuilder(mContext);
				myAlertDialogbd.setTitle("录音文件操作");
				myAlertDialogbd.setIcon(R.drawable.alert_dialog_icon);
				myAlertDialogbd.setView(null);
				myAlertDialogbd.setItems(R.array.recordfileOprArray, onlongClick);
				setSelectFilePath(record.getFilePath());
				myAlertDialogbd.show();
				for (int i = 0; i < recordList.size(); i++) {
					Record _record = recordList.get(i);
					if (i == position) {
						if (_record.isSeleted()) {
							_record.setSeleted(false);
						} else {
							_record.setSeleted(true);
						}
					} else {
						_record.setSeleted(false);
					}
				}
				// 刷新加载listview
				myAdapter.notifyDataSetChanged();
			}
		}

	}

	class MyOnLongClickListenner implements OnLongClickListener {

		private Context onlongclickContext;

		public MyOnLongClickListenner(Context mContext) {
			this.onlongclickContext = mContext;
		}

		@Override
		public boolean onLongClick(View v) {
			return false;
		}

	}

	/**
	 * 自定义dialog实现实现
	 */
	android.content.DialogInterface.OnClickListener onlongClick = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == 0) {// 播放
				Message msg = new Message();
				msg.what = MESSAGE_PLAY_START;
				msg.obj = getSelectFilePath();
				recordHandler.sendMessage(msg);
			} else if (which == 1) {// 删除

			}
		}

	};

}
