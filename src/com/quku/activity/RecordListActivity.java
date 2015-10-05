package com.quku.activity;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.quku.R;
import com.quku.Utils.SystemDef;
import com.quku.Utils.Utils;
import com.quku.entity.Record;
import com.quku.record.MyRecordAdapter;
import com.quku.record.RecordFileUtil;

/**
 * 录音列表界面
 * 
 * @author zou.sq
 * 
 */
public class RecordListActivity extends Activity {
	private static final int MESSAGE_PLAY_START = 1;// 通知开始播放消息
	private static final int MESSAGE_PLAYING_TIME = 2;// 播放中刷新消息
	private static final int MESSAGE_RECORD_DIR_FLUSH = 3;// 刷新录音列表加载目录
	private static final int MESSAGE_RECORD_DATA_FLUSH = 4;// 刷新录音列表数据
	private Context mContext;
	/** 录音播放操作 **/
	private Button btnPlay;
	private Button btnPre;
	private Button btnNext;
	private SeekBar record_progress;
	private TextView time_current;
	private TextView time_total;
	/** 文件导航列表操作变量 **/
	private LinearLayout mLlBack;
	private LinearLayout mLlRight;
	private TextView mTvTitle;
	private TextView mTvRight;
	// private LinearLayout record_tools_new_floder;
	private ListView recordFileList;
	private String rootDir = SystemDef.System.FLUSHCARD
			+ SystemDef.System.ROOTDIR + "/" + SystemDef.Record.RECORD_DIR;
	private List<Record> currentFileList;// 存放当前文件的文件list列表
	public LinkedList<String> playList = new LinkedList<String>();// 播放录音文件列表，用于切换录音文件
	private String currentDir;// 当前录音文件目录
	private MyRecordAdapter recordAdapter;
	private MediaPlayer mPlayer = null;
	private Timer timer;
	private TimerTask timerTask;
	private String playRecordFile;
	private boolean isCharging;

	public String getPlayRecordFile() {
		return playRecordFile;
	}

	public void setPlayRecordFile(String playRecordFile) {
		this.playRecordFile = playRecordFile;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		getWindow().getDecorView().setSystemUiVisibility(4);
		this.setContentView(R.layout.activity_record_list);
	}

	Handler recordHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MESSAGE_PLAY_START:
				setPlayRecordFile((String) msg.obj);
				startPlaying();
				break;
			case MESSAGE_PLAYING_TIME:
				int progress = msg.arg1;
				record_progress.setProgress(progress);
				time_current.setText(showTime(progress));
				break;
			case MESSAGE_RECORD_DIR_FLUSH:
				currentDir = (String) msg.obj;
				break;
			case MESSAGE_RECORD_DATA_FLUSH:
				if (null != recordAdapter) {
					recordAdapter.notifyDataSetChanged();
				}
				break;
			}
		}
	};

	@Override
	protected void onResume() {
		initView();
		if (null != this.getIntent()
				&& "recordFile2Pay".equals(this.getIntent().getAction())) {
			String recordPath = this.getIntent().getStringExtra(
					"recordFilePath");
			if (null != recordPath) {
				setPlayRecordFile(recordPath);
				startPlaying();
			}
		}
		super.onResume();
	}

	private void initView() {
		mTvTitle = (TextView) findViewById(R.id.title_with_back_title_btn_mid);
		mLlBack = (LinearLayout) this
				.findViewById(R.id.title_with_back_title_btn_left);
		mLlRight = (LinearLayout) this
				.findViewById(R.id.title_with_back_title_btn_right);
		mTvRight = (TextView) findViewById(R.id.tv_title_with_right);
		mLlBack.setOnClickListener(new MyOnclickListenner());
		mLlRight.setOnClickListener(new MyOnclickListenner());
		// record_tools_new_floder.setOnClickListener(new MyOnclickListenner());
		btnPlay = (Button) this.findViewById(R.id.record_play);
		btnPre = (Button) this.findViewById(R.id.record_play_pre);
		btnNext = (Button) this.findViewById(R.id.record_play_next);
		btnPlay.setOnClickListener(new MyOnclickListenner());
		btnPre.setOnClickListener(new MyOnclickListenner());
		btnNext.setOnClickListener(new MyOnclickListenner());
		record_progress = (SeekBar) this
				.findViewById(R.id.recordcontroller_progress);
		record_progress.setOnSeekBarChangeListener(new MySeekBarListenner());
		time_current = (TextView) this.findViewById(R.id.time_current);
		time_total = (TextView) this.findViewById(R.id.time_total);
		recordFileList = (ListView) this.findViewById(R.id.recordfilelist);
		currentFileList = RecordFileUtil.loadFileList(rootDir, currentFileList);// 初始化加载目录及文件
		initLoadPlayList(currentFileList);// 刷新播放列表文件
		recordAdapter = new MyRecordAdapter(mContext, currentFileList,
				recordHandler);
		recordFileList.setAdapter(recordAdapter);
		currentDir = rootDir;// 初始化目录为当前加载文件的根目录
		mTvTitle.setText("录音列表");
		mTvRight.setText("上一级");
	}

	public void initLoadPlayList(List<Record> recordDirList) {
		if (null != recordDirList && recordDirList.size() > 0) {
			playList.clear();
			if (null != getPlayRecordFile()) {// 默认加载传过来的录音文件路径
				playList.add(getPlayRecordFile());
			}
			for (Record record : recordDirList) {
				if (record.getType() == 0) {
					playList.add(record.getFilePath());
				}
			}
		}
	}

	class MySeekBarListenner implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			isCharging = true;
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			if (null != mPlayer && null != seekBar) {
				mPlayer.seekTo(seekBar.getProgress());
				isCharging = false;
			}
		}
	}

	class MyOnclickListenner implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.title_with_back_title_btn_left:// 返回
				finish();
				break;
			case R.id.title_with_back_title_btn_right:// 上一层
				if (currentDir.equals(rootDir)) {// 已经到跟目录，不能在返回
					Utils.getToast(mContext, "已经到跟目录，不能在返回");
				} else {
					// 获取父目录
					File file = new File(currentDir);
					File parentFile = file.getParentFile();
					if (null != parentFile && parentFile.isDirectory()) {
						if (null != currentFileList) {
							currentFileList = null;
						}
						currentFileList = RecordFileUtil.loadFileList(
								parentFile.getPath(), currentFileList);// 初始化加载目录及文件
						initLoadPlayList(currentFileList);// 刷新播放列表文件
						currentDir = parentFile.getPath();
						recordAdapter = new MyRecordAdapter(mContext,
								currentFileList, recordHandler);
						recordFileList.setAdapter(recordAdapter);
						recordAdapter.notifyDataSetChanged();
					}
				}
				break;
			case R.id.record_play:// 播放
				if (null != mPlayer) {// 第一次加载后，就不会为空
					if (mPlayer.isPlaying()) {
						mPlayer.pause();
						// btnPlay.setBackgroundResource(R.drawable.record_stop_btn);
					} else {// 没有播放，说已经已经暂停
							// 直接调用播放
							// btnPlay.setBackgroundResource(R.drawable.record_play_btn);
						mPlayer.start();
						mPlayer.seekTo(mPlayer.getCurrentPosition());
						scheduleTime();
					}
				} else {
					startPlaying();
				}
				break;
			case R.id.record_play_pre:// 上一首
				int preIndex = 0;
				if (null != playList && playList.size() > 0) {
					if (null != getPlayRecordFile()) {
						preIndex = playList.indexOf(getPlayRecordFile());// 当前播放录音文件的索引
					}
				}
				preIndex--;
				if (preIndex >= 0 && preIndex < playList.size() - 1) {
					setPlayRecordFile(playList.get(preIndex));
					startPlaying();
				}
				break;
			case R.id.record_play_next:// 下一首
				int nextIndex = 0;
				if (null != playList && playList.size() > 0) {
					if (null != getPlayRecordFile()) {
						nextIndex = playList.indexOf(getPlayRecordFile());// 当前播放录音文件的索引
					}
				}
				nextIndex++;
				if (nextIndex >= 0 && nextIndex < playList.size() - 1) {
					setPlayRecordFile(playList.get(nextIndex));
					startPlaying();
				}
				break;
			}
		}
	}

	@Override
	protected void onDestroy() {
		// 释放资源
		if (null != currentFileList) {
			currentFileList.clear();
			currentFileList = null;
		}
		if (mPlayer != null) {
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
			mPlayer.release();
			mPlayer = null;
		}
		if (null != timer) {
			timer.cancel();
			timer = null;
		}
		super.onDestroy();

	}

	private void scheduleTime() {
		try {

			timerTask = new TimerTask() {
				@Override
				public void run() {
					if (isCharging) {// 正在快进或快退时，直接退出
						return;
					}
					if (null != mPlayer && mPlayer.isPlaying()) {
						int progress = mPlayer.getCurrentPosition();
						Message msg = new Message();
						msg.what = MESSAGE_PLAYING_TIME;
						msg.arg1 = progress;
						sendMessage(msg);
					}
				}
			};
		} catch (Exception e) {

		}
		timer = new Timer();
		timer.schedule(timerTask, 500, 500);
	}

	/**
	 * MediaPlayer 播放完毕
	 * 
	 * @author Administrator
	 * 
	 */
	class MyPlayerCompletionListenner implements OnCompletionListener {

		@Override
		public void onCompletion(MediaPlayer mp) {
			timer.cancel();
			record_progress.setProgress(0);
			time_current.setText(showTime(0));
			btnPlay.setBackgroundResource(R.drawable.record_play_btn);
		}

	}

	private void startPlaying() {
		if (null == getPlayRecordFile()) {// 未选择文件播放
			return;
		}
		btnPlay.setBackgroundResource(R.drawable.record_stop_btn);
		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(new MyPlayerCompletionListenner());
		try {
			// 设置要播放的文件
			System.out.println("getPlayFile =  " + getPlayRecordFile());
			mPlayer.setDataSource(getPlayRecordFile());
			mPlayer.prepare(); // 预加载
			scheduleTime();
			int total = mPlayer.getDuration();
			record_progress.setMax(total);
			String time = showTime(total);
			time_total.setText(time);
			mPlayer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 视频文件时间显示,从系统获取的时间是毫秒级的，我们需要将它转化为00分00秒的格式;
	private String showTime(int time) {
		time /= 1000; // 先转换成秒
		int minute = time / 60; // 转换成分钟
		int hour = minute / 60; // 转换成小时
		int second = time % 60; // 对秒取余，舍掉毫秒，保留秒
		minute %= 60; // 对分钟取余，获取整数分钟
		if (hour > 0) {
			return String.format("%02d:%02d:%02d", hour, minute, second);
		}
		return String.format("%02d:%02d:%02d", 0, minute, second);
	}

	private void sendMessage(Message msg) {
		recordHandler.sendMessage(msg);
	}
}
