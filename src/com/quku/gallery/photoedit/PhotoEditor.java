package com.quku.gallery.photoedit;

/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.quku.R;
import com.quku.note2.MyDialog;

/**
 * Main activity of the photo editor that opens a photo and prepares tools for
 * photo editing.
 */
public class PhotoEditor extends Activity {

	private static final int DIALOG_PHOTOEDIT_BACK = 1;// 回退到文件管理器界面
	private static final String SAVE_URI_KEY = "save_uri";
	private Uri sourceUri;
	private Uri saveUri;
	private FilterStack filterStack;
	private ActionBar actionBar;
	private EffectsBar effectsBar;
	public String viewFilePath;// 编辑的文件路径，要替换的文件路径
	public Button btnExit;// 退出的button按钮
	MyDialog myAlertDialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().getDecorView().setSystemUiVisibility(4); // SYSTEM_UI_FLAG_SHOW_FULLSCREEN
																// =
																// 0x00000004;//隐藏状态栏
		setContentView(R.layout.photoeditor_main);
		SpinnerProgressDialog.initialize((ViewGroup) findViewById(R.id.toolbar));
		Intent intent = getIntent();

		if (Intent.ACTION_EDIT.equalsIgnoreCase(intent.getAction())) {
			sourceUri = intent.getData();// 获取url对象数据，用来显示要预览的图片
			// 获取要保存的路径， 用户另存为或者替换
			viewFilePath = intent.getStringExtra("editPath");
		}

		actionBar = (ActionBar) findViewById(R.id.action_bar);
		// 已经屏蔽了状态栏，需要添加退出按妞才能返回
		btnExit = (Button) actionBar.findViewById(R.id.exit);
		btnExit.setOnClickListener(new MyOnclickListenner());

		filterStack = new FilterStack((PhotoView) findViewById(R.id.photo_view), new FilterStack.StackListener() {

			@Override
			public void onStackChanged(boolean canUndo, boolean canRedo) {
				actionBar.updateButtons(canUndo, canRedo);
			}
		}, savedInstanceState);
		if (savedInstanceState != null) {
			saveUri = savedInstanceState.getParcelable(SAVE_URI_KEY);
			actionBar.updateSave(saveUri == null);
		}

		// Effects-bar is initially disabled until photo is successfully loaded.
		effectsBar = (EffectsBar) findViewById(R.id.effects_bar);
		effectsBar.initialize(filterStack);
		effectsBar.setEnabled(false);

		actionBar.setClickRunnable(R.id.undo_button, createUndoRedoRunnable(true));
		actionBar.setClickRunnable(R.id.redo_button, createUndoRedoRunnable(false));
		// 自定更改图片保存路径，根据预览
		actionBar.setClickRunnable(R.id.save_button, createSaveRunnable(viewFilePath));
		actionBar.setClickRunnable(R.id.share_button, createShareRunnable());
		actionBar.setClickRunnable(R.id.action_bar_back, createBackRunnable());
	}

	/**
	 * 自定义按钮监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class MyOnclickListenner implements OnClickListener {

		@Override
		public void onClick(View v) {
			showDialog(DIALOG_PHOTOEDIT_BACK);
		}

	}

	/**
	 * 重写oncreateDialog方法
	 */
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
			case DIALOG_PHOTOEDIT_BACK:
				dialogBack();
				break;
		}
		return super.onCreateDialog(id, args);
	}

	private void dialogBack() {
		myAlertDialog = (MyDialog) new MyDialog(PhotoEditor.this);
		myAlertDialog.setIcon(R.drawable.alert_dialog_icon);
		myAlertDialog.setTitle("提示");
		myAlertDialog.setButton("退出", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 当前需要清空
				PhotoEditor.this.finish();
			}
		});
		myAlertDialog.setButton2("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		myAlertDialog.show();
	}

	private void openPhoto() {
		SpinnerProgressDialog.showDialog();
		LoadScreennailTask.Callback callback = new LoadScreennailTask.Callback() {

			@Override
			public void onComplete(final Bitmap result) {
				filterStack.setPhotoSource(result, new OnDoneCallback() {

					@Override
					public void onDone() {
						SpinnerProgressDialog.dismissDialog();
						effectsBar.setEnabled(result != null);
					}
				});
			}
		};
		new LoadScreennailTask(this, callback).execute(sourceUri);
	}

	private Runnable createUndoRedoRunnable(final boolean undo) {
		return new Runnable() {

			@Override
			public void run() {
				effectsBar.exit(new Runnable() {

					@Override
					public void run() {
						SpinnerProgressDialog.showDialog();
						OnDoneCallback callback = new OnDoneCallback() {

							@Override
							public void onDone() {
								SpinnerProgressDialog.dismissDialog();
							}
						};
						if (undo) {
							filterStack.undo(callback);
						} else {
							filterStack.redo(callback);
						}
					}
				});
			}
		};
	}

	private Runnable createSaveRunnable(String filePath) {
		return new Runnable() {

			@Override
			public void run() {
				effectsBar.exit(new Runnable() {

					@Override
					public void run() {
						SpinnerProgressDialog.showDialog();
						filterStack.getOutputBitmap(new OnDoneBitmapCallback() {

							@Override
							public void onDone(Bitmap bitmap) {
								SaveCopyTask.Callback callback = new SaveCopyTask.Callback() {

									@Override
									public void onComplete(Uri result) {
										SpinnerProgressDialog.dismissDialog();
										saveUri = result;
										actionBar.updateSave(saveUri == null);
									}
								};
								// 增加预览的图片路径
								new SaveCopyTask(PhotoEditor.this, sourceUri, callback, viewFilePath).execute(bitmap);
							}
						});
					}
				});
			}
		};
	}

	private Runnable createShareRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				effectsBar.exit(new Runnable() {

					@Override
					public void run() {
						if (saveUri != null) {
							Intent intent = new Intent(Intent.ACTION_SEND);
							intent.putExtra(Intent.EXTRA_STREAM, saveUri);
							intent.setType("image/*");
							startActivity(intent);
						}
					}
				});
			}
		};
	}

	private Runnable createBackRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				// Exit effects or go back to the previous activity on pressing
				// back button.
				if (!effectsBar.exit(null)) {
					// Pop-up a dialog to save unsaved photo.
					if (actionBar.canSave()) {
						new YesNoCancelDialogBuilder(PhotoEditor.this, new Runnable() {

							@Override
							public void run() {
								actionBar.clickSave();
							}
						}, new Runnable() {

							@Override
							public void run() {
								finish();
							}
						}, R.string.save_photo).show();
					} else {
						finish();
					}
				}
			}
		};
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		filterStack.saveStacks(outState);
		outState.putParcelable(SAVE_URI_KEY, saveUri);
	}

	@Override
	public void onBackPressed() {
		actionBar.clickBack();
	}

	@Override
	protected void onPause() {
		super.onPause();
		filterStack.onPause();
		// Dismiss any running progress dialog as all operations are paused.
		SpinnerProgressDialog.dismissDialog();
	}

	@Override
	protected void onResume() {
		super.onResume();
		filterStack.onResume();
		openPhoto();
	}
}
