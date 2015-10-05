package com.quku.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.quku.DetialGallery;
import com.quku.ImageAdapter;
import com.quku.PaintLine;
import com.quku.R;

public class PictureBrowers extends Activity {

	public ArrayList<String> picList = new ArrayList<String>();
	TextView intervalBtnNum[] = new TextView[11];
	TextView intervalBtnNumFujia1;
	TextView intervalBtnNumFujia2;
	TextView intervalBottom[] = new TextView[2];
	Bitmap curBitmap, nextBitmap;

	public float tempo;
	public float gukesudu;

	public int section;
	public int gukejiepai;
	public int pp;
	public int pp2;

	public boolean isJiePai = false;

	int xiaojianju = 1;
	int dajianju = 3;

	AlertDialog XXX;
	Spinner spinner;
	Handler handler;
	Timer mytimer;

	private SoundPool sndHigh;
	private SoundPool sndMiddle;
	private SoundPool sndLow;
	private int hitOfHigh;
	private int hitOfMiddle;
	private int hitOfLow;
	private ImageAdapter adapter;

	public Button jiePaiButton;
	public Button mBtn_metronome;

	class MyTimerTask extends TimerTask {
		public void run() {
			Message message = handler.obtainMessage(1);
			handler.sendMessage(message);
		}
	};

	int W = LayoutParams.WRAP_CONTENT;
	private LinearLayout.LayoutParams LY_P = new LinearLayout.LayoutParams(
			LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	private LinearLayout.LayoutParams LY_GALLERY = new LinearLayout.LayoutParams(
			PaintLine.picAllViewWide, PaintLine.picAllViewHigh);

	public static String selectFilename;
	public int picOrder;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		selectFilename = this.getIntent().getStringExtra("filename");
		Log.d("aaa", "selectFilename:" + selectFilename);

		getPiclList(selectFilename);
		setContentView(R.layout.activity_picture_browers);

		String pictureNameIndex = this.getIntent().getStringExtra("nameIndex");
		Log.d("aaa", "pictureNameIndex:" + pictureNameIndex);
		int pictureIndex;
		try {
			pictureIndex = Integer.parseInt(pictureNameIndex) - 1;
			if (pictureIndex == picList.size()) {
				pictureIndex = pictureIndex - 1;
			}
		} catch (java.lang.NumberFormatException e) {
			pictureIndex = 0;
		}
		System.out.println(pictureIndex + "pictureIndex");
		if (pictureIndex > picList.size()) {
			Toast.makeText(PictureBrowers.this, "所选图片的编号已超出该文件夹里的图片总数",
					Toast.LENGTH_LONG).show();
			pictureIndex = 0;
		}
		Collections.sort(picList);
		LinearLayout galleryLayout = (LinearLayout) findViewById(R.id.gallery_layout);
		final DetialGallery gallery = new DetialGallery(this);
		Button returnBtn = (Button) findViewById(R.id.btnBack);
		Button editBtn = (Button) findViewById(R.id.btnedit);
		Button btn_record = (Button) findViewById(R.id.btn_record);
		galleryLayout.addView(gallery, LY_P);
		jiePaiButton = (Button) findViewById(R.id.jiePaiButton);
		mBtn_metronome = (Button) findViewById(R.id.btn_metronome);
		gallery.setLayoutParams(LY_GALLERY);
		adapter = new ImageAdapter(this, picList);
		gallery.setSpacing(5);
		gallery.setAdapter(adapter);
		gallery.setPadding(0, 0, 0, 0);
		gallery.setSelection(pictureIndex);

		gallery.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					return true;
				}
				return false;
			}
		});

		int i = 0;
		picOrder = 0;
		while (i < picList.size()) {
			if (picList.get(i).toString().equals(selectFilename)) {
				picOrder = i;
				break;
			}
			i++;
		}

		intervalBtnNumFujia1 = new TextView(this);
		intervalBtnNumFujia1.setText("");
		intervalBtnNumFujia1.setWidth(dajianju);
		intervalBtnNumFujia1.setBackgroundColor(Color.BLACK);
		intervalBtnNumFujia2 = new TextView(this);
		intervalBtnNumFujia2.setText("");
		intervalBtnNumFujia2.setWidth(dajianju);
		intervalBtnNumFujia2.setBackgroundColor(Color.BLACK);

		intervalBtnNum[10] = new TextView(this);
		intervalBtnNum[10].setText("");
		intervalBtnNum[10].setWidth(xiaojianju);
		intervalBtnNum[10].setBackgroundColor(Color.BLACK);
		btn_record.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				adapter.toRightPager();
			}
		});

		returnBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		editBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				String currentEditFile = picList.get(ImageAdapter.pposition)
						.toString();
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("filename", selectFilename);
				intent.putExtras(bundle);
				intent.setClass(PictureBrowers.this, PaintLine.class);
				intent.putExtra("currentfilename", currentEditFile);
				String indexName = String.valueOf(ImageAdapter.pposition + 1);
				intent.putExtra("indexName", indexName);
				startActivity(intent);
			}
		});

		jiePaiButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setJiePaiButton();
			}
		});

		sndHigh = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
		sndMiddle = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
		sndLow = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);

		hitOfHigh = sndHigh.load(getBaseContext(), R.drawable.high, 0);
		hitOfMiddle = sndMiddle.load(getBaseContext(), R.drawable.middle, 0);
		hitOfLow = sndLow.load(getBaseContext(), R.drawable.low, 0);

		for (int hh = 0; hh < 2; hh++) {
			intervalBottom[hh] = new TextView(this);
			intervalBottom[hh].setText("");
			intervalBottom[hh].setWidth(8);
			intervalBottom[hh].setBackgroundColor(Color.BLACK);
		}
	}

	public void setJiePaiButton() {
		if (isJiePai) {
			if (mytimer != null) {
				mytimer.cancel();
				mytimer = null;
			}
			mBtn_metronome.setVisibility(View.GONE);
			isJiePai = false;
		} else {
			handler = new Handler() {
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case 1:
						mBtn_metronome.setVisibility(View.VISIBLE);
						if (pp == 1) {
							mBtn_metronome
									.setBackgroundResource(R.drawable.signalpicture1);
						}

						if (pp == 2) {
							mBtn_metronome
									.setBackgroundResource(R.drawable.signalpicture2);
						}

						if (pp == 3) {
							mBtn_metronome
									.setBackgroundResource(R.drawable.signalpicture3);
						}
						if (pp == 4) {
							mBtn_metronome
									.setBackgroundResource(R.drawable.signalpicture4);
						}

						if (pp == 1)
							sndHigh.play(hitOfHigh, 1, 1, 0, 0, 1);
						else
							sndLow.play(hitOfLow, 1, 1, 0, 0, 1);
						if (pp != section) {
							pp++;
						} else {
							pp = 1;
						}
						break;
					}
					super.handleMessage(msg);
				}
			};

			AlertDialog.Builder dialog = new AlertDialog.Builder(
					PictureBrowers.this);
			dialog.setTitle("请设置节拍器参数");

			LinearLayout layoutgu = (LinearLayout) getLayoutInflater().inflate(
					R.layout.recordlayout, null);

			SeekBar seekBar = (SeekBar) layoutgu.findViewById(R.id.sek1);
			seekBar.setMax(208);
			seekBar.setProgress(1);

			final TextView txt1 = (TextView) layoutgu.findViewById(R.id.txt1);

			seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromTouch) {
					txt1.setText("您所设置的节拍数为：" + String.valueOf(progress)
							+ " 拍每分钟");
					gukesudu = (float) (60.0 / progress);
				}

				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});

			dialog.setView(layoutgu);

			dialog.setIcon(android.R.drawable.btn_star);// ͼ��

			dialog.setNeutralButton("3/4拍",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							gukejiepai = 3;
							if (mytimer != null) {
								mytimer.cancel();
								mBtn_metronome.setVisibility(View.GONE);
							}
							if ((gukesudu != 0) && (gukejiepai != 0)) {
								tempo = gukesudu;
								section = gukejiepai;
								mytimer = new Timer();
								float tempFloat = tempo * 1000;
								mytimer.schedule(new MyTimerTask(), 0,
										(long) tempFloat);
								isJiePai = true;
							} else {
								Toast.makeText(PictureBrowers.this,
										"请选择上述节拍器的参数", Toast.LENGTH_LONG)
										.show();
							}
						}
					})
					.setNegativeButton("4/4拍",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0,
										int arg1) {
									gukejiepai = 4;
									if (mytimer != null) {
										mytimer.cancel();
										mBtn_metronome.setVisibility(View.GONE);
									}
									if ((gukesudu != 0) && (gukejiepai != 0)) {
										tempo = gukesudu;
										section = gukejiepai;
										mytimer = new Timer();
										float tempFloat = tempo * 1000;
										mytimer.schedule(new MyTimerTask(), 0,
												(long) tempFloat);
										isJiePai = true;
									} else {
										Toast.makeText(PictureBrowers.this,
												"请选择上述节拍器的参数",
												Toast.LENGTH_LONG).show();
									}
								}
							})
					.setPositiveButton("2/4 拍",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0,
										int arg1) {
									gukejiepai = 2;
									if (mytimer != null) {
										mytimer.cancel();
										mBtn_metronome.setVisibility(View.GONE);
									}
									if ((gukesudu != 0) && (gukejiepai != 0)) {
										tempo = gukesudu;
										section = gukejiepai;
										mytimer = new Timer();
										float tempFloat = tempo * 1000;
										mytimer.schedule(new MyTimerTask(), 0,
												(long) tempFloat);
										isJiePai = true;
									} else {
										Toast.makeText(PictureBrowers.this,
												"请选择上述节拍器的参数",
												Toast.LENGTH_LONG).show();

									}

								}
							}).show();
		}
	}

	public void getPiclList(String filepath) {
		File file = new File(filepath);
		for (File currentFile : file.getParentFile().listFiles()) {
			if ((currentFile.getName().endsWith(".jpg"))
					|| (currentFile.getName().endsWith(".JPG"))
					|| (currentFile.getName().endsWith(".png"))
					|| (currentFile.getName().endsWith(".gif"))
					|| (currentFile.getName().endsWith(".bmp"))) {
				if (!(currentFile.getName().endsWith("_origin.JPG"))) {
					if (!(currentFile.getName().endsWith("_origin.jpg"))) {
						picList.add(currentFile.getAbsolutePath());
					}
				}
			}
		}
	}

	public void onPause() {
		if (mytimer != null) {
			mytimer.cancel();
			sndHigh.pause(hitOfHigh);
			sndLow.pause(hitOfLow);
		}
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			adapter.toRightPager();
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			adapter.toLeftPager();
		}
		return super.onKeyDown(keyCode, event);
	}
}
