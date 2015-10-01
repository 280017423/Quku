package com.quku.activity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quku.R;
import com.quku.Utils.SystemDef;
import com.quku.Utils.Utils;
import com.quku.camera.CameraActivity;
import com.quku.note2.MyDialog;

/**
 * 主界面activity
 * 
 * @author Administrator
 * 
 */
public class MianActivity extends Activity implements OnClickListener {
	private static final int WINDOWMANAGER_LAYOUT_PARAM_INIT = 1;// 悬浮框初使化标记
	private static final int WINDOWMANAGER_LAYOUT_PARAM_ONLOAD = 2;// 悬浮框录音标记
	private static final int RECORD_MESSAGE_RECORDING = 3;// 录音中
	private static final int RECORD_MESSAGE_RECORD_STOP = 4;// 停止录音，录音重命名
	private static final int RECORD_MESSAGE_RECORD_STOP_RENAME = 5;// 修改文件名后，刷新录音label

	private MyDialog myAlertDialog;
	private SharedPreferences systemPreference;
	private Context mContext;
	/** 录音操作定义 */
	private WindowManager wm;
	private View recordFloatView;// 录音悬浮View
	private Button openRecord;
	private TextView floatViewTime;
	private View recordView;// 录音操作view
	private WindowManager.LayoutParams wmParams = null;
	public static int recordStatus = 0;// 录音状态
	private TextView recordStatusText;
	private TextView recordTimer;
	private TextView recordSaveFile;
	private Button recordlistbtn;
	private Button recordPlay;// 开始录音
	private Button recordStop;// 停止录音
	private Button end2Play;// 录音结束后播放
	private Button recordViewHide;// 录音布局隐藏
	private int second = 0;// 秒
	private int minute = 0;// 分钟
	private int hours = 0;// 小时
	/** 计时器 **/
	private Timer timer;
	private MediaRecorder mMediaRecorder;
	private File myRecAudioFile;// 录音文件保存的文件对象
	/** 是否暂停标志位 **/
	private boolean isRecoring;
	private String rootDir = SystemDef.System.FLUSHCARD
			+ SystemDef.System.ROOTDIR + "/" + SystemDef.Record.RECORD_DIR;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initVariables();
	}

	private void initVariables() {
		mContext = this;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		systemPreference = getSharedPreferences(
				SystemDef.System.PREF_VERSION_NAME, MODE_PRIVATE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		enableFloatView();// 开启录音悬浮框
	}

	private void enableFloatView() {
		if (null != wm) {
			if (null != recordFloatView) {
				recordFloatView.setVisibility(View.VISIBLE);
			}
		} else {
			wm = (WindowManager) getApplicationContext().getSystemService(
					WINDOW_SERVICE);
			recordFloatView = View.inflate(mContext,
					R.layout.layout_floatview_init, null);
			initFloatView();
			wmParams = initWinParams(WINDOWMANAGER_LAYOUT_PARAM_INIT);
			wm.addView(recordFloatView, wmParams); // 创建View
		}
	}

	private void initFloatView() {
		openRecord = (Button) recordFloatView.findViewById(R.id.openRecord);
		floatViewTime = (TextView) recordFloatView
				.findViewById(R.id.floatViewTime);
		openRecord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				recordView = View.inflate(mContext, R.layout.layout_floatview,
						null);
				wmParams = initWinParams(WINDOWMANAGER_LAYOUT_PARAM_ONLOAD);
				recordFloatView.setVisibility(View.INVISIBLE);
				initRevoidView();
				wm.addView(recordView, wmParams);
				// 记录状态绘制录音view
				if (isRecoring) {
					setRecording();
				}
			}
		});
	}

	private void sdcardStatus() {
		File file = new File(rootDir);
		if (!file.exists()) {
			file.mkdir();
		}
	}

	/**
	 * 加载录音布局
	 */
	private void initRevoidView() {
		recordlistbtn = (Button) recordView.findViewById(R.id.recordlistbtn);
		recordSaveFile = (TextView) recordView
				.findViewById(R.id.recordsavefile);
		recordStatusText = (TextView) recordView
				.findViewById(R.id.recordstatus);
		recordTimer = (TextView) recordView.findViewById(R.id.recordtime);
		recordPlay = (Button) recordView.findViewById(R.id.btnrecord);
		recordStop = (Button) recordView.findViewById(R.id.recordstop);
		end2Play = (Button) recordView.findViewById(R.id.end2play);
		recordViewHide = (Button) recordView.findViewById(R.id.btnhide);
		recordlistbtn.setOnClickListener(new MyRecordBtnListenner());
		recordPlay.setOnClickListener(new MyRecordBtnListenner());
		recordStop.setOnClickListener(new MyRecordBtnListenner());
		end2Play.setOnClickListener(new MyRecordBtnListenner());
		recordViewHide.setOnClickListener(new MyRecordBtnListenner());
		recordPlay.setEnabled(true);
		recordStop.setEnabled(false);
		sdcardStatus();// 初使化录音 目录
	}

	class MyRecordBtnListenner implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.recordlistbtn:// 录音列表
				recordView.setVisibility(View.INVISIBLE);
				recordFloatView.setVisibility(View.VISIBLE);
				Intent intent = new Intent();
				intent.setClass(mContext, RecordListActivity.class);
				mContext.startActivity(intent);
				break;
			case R.id.btnrecord:// 开始录音
				start();
				recordView.setVisibility(View.INVISIBLE);
				recordFloatView.setVisibility(View.VISIBLE);
				break;
			case R.id.recordstop:// 暂停录音/保存
				recordPlay2Stop();
				break;
			case R.id.end2play:// 录音完毕后，录音文件播放
				if (null != myRecAudioFile) {
					Intent intentRecordList = new Intent();
					intentRecordList.setAction("recordFile2Pay");
					intentRecordList.putExtra("recordFilePath",
							myRecAudioFile.getPath());
					intentRecordList.setClass(MianActivity.this,
							RecordListActivity.class);
					MianActivity.this.startActivity(intentRecordList);
				}
				break;
			case R.id.btnhide:// 录音布局隐藏
				recordView.setVisibility(View.INVISIBLE);
				recordFloatView.setVisibility(View.VISIBLE);
				break;
			}
		}

	}

	private String getTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH：mm：ss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String time = formatter.format(curDate);
		return time;
	}

	protected void recordPlay2Stop() {
		System.out.println(" recodeStop.....");
		if (mMediaRecorder != null) {
			System.out.println(" 停止录音");
			// 停止录音
			mMediaRecorder.stop();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
		recordPlay.setBackgroundResource(R.drawable.btn_rekam);
		recordPlay.setEnabled(true);
		recordStop.setEnabled(false);
		timer.cancel();
		isRecoring = false;
		// 重置录音 时间显示
		hours = 0;
		minute = 0;
		second = 0;
		recordhandler.sendEmptyMessage(RECORD_MESSAGE_RECORD_STOP);
		// 弹出重命名对话框
		reRecordFileDialog();

	}

	/**
	 * 录音时间显示处理
	 */
	Handler recordhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case RECORD_MESSAGE_RECORDING:// 录音中
				openRecord.setText("录音中...");
				recordTimer.setText(getCurrentTime());
				floatViewTime.setText(getCurrentTime());
				break;
			case RECORD_MESSAGE_RECORD_STOP:// 录音暂停
				openRecord.setText("录音");
				recordStatusText.setText("");// 未录音
				recordTimer.setText("00:00:00");
				floatViewTime.setText("00:00:00");
				floatViewTime.setVisibility(View.INVISIBLE);
				break;
			case RECORD_MESSAGE_RECORD_STOP_RENAME:
				openRecord.setText("录音");
				String info = (String) msg.obj;
				recordSaveFile.setText(info);
				end2Play.setVisibility(View.VISIBLE);
				break;
			}
		}
	};

	private String getCurrentTime() {
		String secondStr = second >= 10 ? String.valueOf(second) : "0" + second;
		String minuteStr = minute >= 10 ? String.valueOf(minute) : "0" + minute;
		String hoursStr = hours >= 10 ? String.valueOf(hours) : "0" + hours;
		String time = new StringBuffer(hoursStr).append(":").append(minuteStr)
				.append(":").append(secondStr).toString();
		return time;
	}

	/**
	 * timer执行任务计时器
	 */
	private void startTimeSchedule() {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				second++;
				if (second >= 60) {
					second = 0;
					minute++;
					if (minute >= 60) {
						hours++;
					}
				}
				recordhandler.sendEmptyMessage(RECORD_MESSAGE_RECORDING);
			}
		};
		timer = new Timer();
		timer.schedule(timerTask, 1000, 1000);

	}

	/**
	 * 启动录音
	 */
	private void start() {
		recordSaveFile.setText("");
		end2Play.setVisibility(View.INVISIBLE);
		floatViewTime.setVisibility(View.VISIBLE);
		// 启动timer计时器
		startTimeSchedule();
		try {
			String mMinute1 = getTime();
			myRecAudioFile = new File(rootDir + "/" + mMinute1
					+ SystemDef.Record.RECORD_SUFFIX);
			mMediaRecorder = new MediaRecorder();
			// 设置录音为麦克风
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			// 录音文件保存这里
			mMediaRecorder.setOutputFile(myRecAudioFile.getAbsolutePath());
			mMediaRecorder.prepare();
			mMediaRecorder.start();
			// mMediaRecorder01.getMaxAmplitude();
			// mMediaRecorder01.getAudioSourceMax();
			mMediaRecorder.setOnInfoListener(new OnInfoListener() {

				@Override
				public void onInfo(MediaRecorder mr, int what, int extra) {
					int a = mr.getMaxAmplitude();
					Toast.makeText(mContext, a, Toast.LENGTH_LONG).show();
				}
			});
			isRecoring = true;
			setRecording();
			System.out.println(" isRecoring = " + (isRecoring));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 录音状态设置
	 */
	private void setRecording() {
		recordStatusText.setText("录音中...");
		recordPlay.setBackgroundResource(R.drawable.btn_rekam_disabled);
		recordPlay.setEnabled(false);
		recordStop.setEnabled(true);
	}

	private WindowManager.LayoutParams initWinParams(int type) {
		wmParams = new WindowManager.LayoutParams();
		wmParams.type = 2002;// 这些窗口通常放在上面所有的应用程序，但后面的状态栏。在多用户系统上显示所有用户的窗口。
		// wmParams.type = 2003; // 这里是关键，你也可以试试2003
		// 系统窗口，如低功耗警报。这些窗口的应用程序窗口的顶部。在多用户系统中只显示拥有用户的窗口。
		// wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY; ;
		wmParams.format = 1;
		wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		wmParams.gravity = Gravity.TOP | Gravity.RIGHT;
		switch (type) {
		case WINDOWMANAGER_LAYOUT_PARAM_INIT:
			wmParams.width = 210;
			wmParams.height = 50;
			wmParams.x = 0;
			wmParams.y = 50;
			break;
		case WINDOWMANAGER_LAYOUT_PARAM_ONLOAD:
			wmParams.width = 500;
			wmParams.height = 170;
			wmParams.x = 0;
			wmParams.y = 50;
			break;
		}
		return wmParams;
	}

	private void hideRecordView() {
		System.out.println(" recordView = " + recordView);
		if (null != recordFloatView) {
			recordFloatView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibtn_my_compose:// 我的乐谱
		case R.id.ibtn_my_compose2:// 我的乐谱
			startActivity(new Intent(MianActivity.this,
					MyMusicSheetActivity.class));
			break;
		case R.id.ibtn_compose_manager:// 文件管理
		case R.id.ibtn_compose_manager2:// 文件管理
			hideRecordView();
			Intent intentFM = new Intent();
			intentFM.setClass(MianActivity.this, FileManagerActivity.class);
			intentFM.setAction(SystemDef.FileManager.FM_ACTION_FILE_BROWSE);
			startActivity(intentFM);
			break;
		case R.id.ibtn_down_compose:// 乐谱下载
		case R.id.ibtn_down_compose2:// 乐谱下载
			Toast.makeText(mContext, "开发中", Toast.LENGTH_LONG).show();
			break;
		case R.id.ibtn_take_compose:// 制作乐谱
		case R.id.ibtn_take_compose2:// 制作乐谱
			hideRecordView();
			startActivity(new Intent(MianActivity.this, CameraActivity.class));
			break;
		case R.id.ibtn_operation:// 使用说明
			hideRecordView();
			startActivity(new Intent(MianActivity.this,
					OpenWebViewActivity.class));
			break;
		case R.id.ibtn_create_compose:// 我的备忘录
			hideRecordView();
			startActivity(new Intent(MianActivity.this,
					MyNoteListActivity.class));
			break;
		case R.id.ibtn_my_pc:// 安卓电脑
			finish();
			break;
		case R.id.ibtn_record_audio:// 录音管理
			Intent intentRM = new Intent();
			intentRM.setClass(MianActivity.this, RecordListActivity.class);
			intentRM.setAction(SystemDef.FileManager.FM_ACTION_FILE_BROWSE);
			startActivity(intentRM);
			break;
		case R.id.ibtn_dictionary:// 电子词典
			Toast.makeText(mContext, "开发中", Toast.LENGTH_LONG).show();
			break;
		case R.id.ibtn_course:// 精品课程
			Toast.makeText(mContext, "开发中", Toast.LENGTH_LONG).show();
			break;
		case R.id.ibtn_device_manager:
			startActivity(new Intent(MianActivity.this, MoreActivity.class));
			break;
		default:
			break;
		}
	}

	/**
	 * 重置录音文件名
	 */
	private void reRecordFileDialog() {
		LayoutInflater factory1 = LayoutInflater.from(mContext);
		final View textEntryView1 = factory1.inflate(
				R.layout.alert_dialog_text_entry, null);
		final EditText e1 = (EditText) textEntryView1
				.findViewById(R.id.username_edit);
		if (myRecAudioFile.exists()) {
			String myFileName = myRecAudioFile.getName().substring(0,
					myRecAudioFile.getName().lastIndexOf("."));
			e1.setText(myFileName);
		}
		/*
		 * else { return; }
		 */
		e1.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().length() > 30) {
					Utils.getToast(mContext, "文件名太长");
					return;
				}
			}
		});
		String title = myRecAudioFile.getName() + "文件"
				+ getString(R.string.alert_dialog_rename_file_title);
		if (null != myAlertDialog) {
			myAlertDialog.dismiss();
		}
		if (null != myAlertDialog && myAlertDialog.isShowing()) {
			myAlertDialog.dismiss();
		}
		myAlertDialog = (MyDialog) new MyDialog(mContext);
		myAlertDialog.setTitle(title);
		myAlertDialog.setIcon(R.drawable.alert_dialog_icon);
		myAlertDialog.setView(textEntryView1);
		myAlertDialog.setButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String text = e1.getText().toString();
				if (text.equals("") || text.length() == 0) {
					Utils.getToast(mContext, "文件名不能为空");
					return;
				}
				String orgiFileName = myRecAudioFile.getName();// 原文件名
				String subfix = orgiFileName.substring(
						orgiFileName.lastIndexOf(".") + 1,
						orgiFileName.length());
				File file1 = new File(rootDir + "/" + text + "." + subfix);
				if (!file1.exists()) {
					// 获取原文件后缀名
					// 重命名操作
					if (myRecAudioFile.renameTo(file1)) {
						Utils.getToast(mContext, "文件重命名成功");
						Message msg = new Message();
						msg.what = RECORD_MESSAGE_RECORD_STOP_RENAME;
						msg.obj = file1.getName();
						recordhandler.sendMessage(msg);
						myRecAudioFile = file1;
						// 成功处理
						return;
					} else {
						Utils.getToast(mContext, "文件重命名失败");
						return;
					}
				} else {
					if (!orgiFileName.equals(file1.getName())) {
						Utils.getToast(mContext, "当前文件已经存在");
						return;
					}
					// 成功处理
					Message msg = new Message();
					msg.what = RECORD_MESSAGE_RECORD_STOP_RENAME;
					msg.obj = file1.getName();
					recordhandler.sendMessage(msg);
				}
			}
		});
		myAlertDialog.setButton2("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		myAlertDialog.show();
	}

	protected void onDestroy() {
		if (myAlertDialog != null && myAlertDialog.isShowing()) {
			myAlertDialog.dismiss();
		}
		// 停止录音
		if (mMediaRecorder != null) {
			System.out.println(" 停止录音");
			// 停止录音
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
		if (null != recordPlay) {
			recordPlay.setBackgroundResource(R.drawable.btn_rekam);
			recordPlay.setEnabled(true);
		}
		if (null != recordStop) {
			recordStop.setEnabled(false);
		}
		if (null != timer) {
			timer.cancel();
			timer = null;
		}
		isRecoring = false;
		// 重置录音 时间显示
		hours = 0;
		minute = 0;
		second = 0;
		if (wm != null) {
			wm = null;
		}
		if (null != recordFloatView) {
			recordFloatView = null;
		}
		if (null != recordView) {
			recordView = null;
		}
		super.onDestroy();
	};
}
