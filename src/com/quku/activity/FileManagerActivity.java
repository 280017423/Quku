package com.quku.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quku.R;
import com.quku.Utils.FileUtil;
import com.quku.Utils.SystemDef;
import com.quku.Utils.Utils;
import com.quku.camera.CameraActivity;
import com.quku.entity.FileSelect;
import com.quku.gallery.photoedit.PhotoEditor;
import com.quku.note2.MyDialog;
import com.quku.note2.MyDialogBuilder;

/**
 * 文件操作列表
 * 
 * @author zou.sq
 */
public class FileManagerActivity extends Activity {
	private static final int DIALOG_CREATE_DIR = 1;// 创建目录
	private static final int DIALOG_DEL_FILE_OK_CANCEL = 2;// 删除文件弹框
	private static final int DIALOG_DEL_DIR_OK_CANCEL = 3;// 删除文件目录弹框
	private static final int DIALOG_RENAME_FILE = 4;// 重命名文件弹框
	private static final int DIALOG_RENAME_DIR = 5;// 重命名目录弹框
	private static final int DIALOG_SAVE_FILE = 6;// 保存文件
	private static final int LOAD_TYPE_BROWSE = 1;
	private static final int LOAD_TYPE_MAKEMUSIC = 2;
	private static final int PROGRESS_DIALOG_DISMESS_MESSAGE = 7;
	private static final int PROGRESS_DIALOG_PASTER_FINISH_MESSAGE = 8;
	private int _type;// 类型
	ListView filelistView;
	TextView tvFilePath;
	LinearLayout tool_home = null;
	LinearLayout tool_level_up;
	LinearLayout tool_new_folder;
	LinearLayout tool_bar_continue;
	LinearLayout tool_bar_save_file;
	LinearLayout tool_paste_file;
	private List<FileSelect> currentFileList;// 存放当前文件的文件list列表
	private String makeMusiceDir = SystemDef.System.CUSTOMPATH;// 当前目录
	private String rootDir = SystemDef.System.FLUSHCARD
			+ SystemDef.System.ROOTDIR;
	private MyAdatper myAdapter;
	private int selectPosition = 0;// 当前选中的list中位置
	private String selectFileName;// 选中的文件
	private String filePath;// 要保存的乐谱路径
	private boolean isCopyStatus;// copy状态
	private List<String> copyPathList = new ArrayList<String>();// 复制路径存放
	Handler fileProgressHandler = null;
	public int mySelectPosition;

	public void setCurrentPosition(int position) {
		mySelectPosition = position;
	}

	public boolean isCopyStatus() {
		return isCopyStatus;
	}

	public void setCopyStatus(boolean isCopyStatus) {
		this.isCopyStatus = isCopyStatus;
	}

	public String getSelectFileName() {
		return selectFileName;
	}

	public void setSelectFileName(String selectFileName) {
		this.selectFileName = selectFileName;
	}

	View view;
	MyDialog myAlertDialog;
	MyDialogBuilder myAlertDialogbd;
	ProgressDialog pro_dialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = LayoutInflater.from(this).inflate(
				R.layout.activity_file_manager, null);
		setContentView(view);
		initView(view);
		filePath = getIntent().getStringExtra("photoPath");
	}

	private void initView(View view) {
		tool_home = (LinearLayout) this.findViewById(R.id.tool_home);
		tool_level_up = (LinearLayout) this.findViewById(R.id.tool_level_up);
		tool_new_folder = (LinearLayout) this
				.findViewById(R.id.tool_new_folder);
		tool_bar_continue = (LinearLayout) this
				.findViewById(R.id.tool_bar_continue);
		tool_bar_save_file = (LinearLayout) this
				.findViewById(R.id.tool_bar_save_file);
		tool_paste_file = (LinearLayout) this
				.findViewById(R.id.tool_paste_file);
		tool_paste_file.setVisibility(View.GONE);// 粘贴，初始化不可见
		filelistView = (ListView) view.findViewById(R.id.filelist);
		tvFilePath = (TextView) view.findViewById(R.id.currentpath);
		tool_home.setOnClickListener(new MyButtonListen());
		tool_level_up.setOnClickListener(new MyButtonListen());
		tool_new_folder.setOnClickListener(new MyButtonListen());
		tool_bar_continue.setOnClickListener(new MyButtonListen());
		tool_bar_save_file.setOnClickListener(new MyButtonListen());
		tool_paste_file.setOnClickListener(new MyButtonListen());
		intentAction();
		flushDirPath();
		myAdapter = new MyAdatper(this, currentFileList);
		filelistView.setAdapter(myAdapter);
		fileProgressHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case PROGRESS_DIALOG_DISMESS_MESSAGE:
					if (null != pro_dialog && pro_dialog.isShowing()) {
						pro_dialog.dismiss();
					}
					break;
				case PROGRESS_DIALOG_PASTER_FINISH_MESSAGE:
					if (null != pro_dialog && pro_dialog.isShowing()) {
						pro_dialog.dismiss();
					}
					tool_paste_file.setVisibility(View.GONE);// 粘贴完毕后，粘贴按钮隐藏不可见
					loadFileList(rootDir + makeMusiceDir);
					// 刷新当前目录
					flushListView();
					break;
				}
				super.handleMessage(msg);
			}

		};
	}

	private void intentAction() {
		// 文件管理文件浏览
		if (SystemDef.FileManager.FM_ACTION_FILE_BROWSE.equals(this.getIntent()
				.getAction())) {
			tool_bar_save_file.setVisibility(View.GONE);
			tool_bar_continue.setVisibility(View.GONE);
			makeMusiceDir = "";
			this.loadFileList2(rootDir + makeMusiceDir);
			_type = LOAD_TYPE_BROWSE;
			// 加载list文件数据
		}
		// 自拍乐谱文件保存
		else if (SystemDef.FileManager.FM_ACTION_SAVE_FILE.equals(this
				.getIntent().getAction())) {
			this.loadFileList(rootDir + makeMusiceDir);
			_type = LOAD_TYPE_MAKEMUSIC;
		}

	}

	void clearFile() {
		if (null != filePath) {
			File file = new File(filePath);
			if (file.exists()) {
				file.delete();
			}
		}
	}

	/**
	 * 按钮监听事件
	 * 
	 * @author Administrator
	 * 
	 */
	class MyButtonListen implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tool_home:
				clearFile();
				finish();
				break;
			case R.id.tool_new_folder:
				// 获取当前目录
				removeDialog(DIALOG_CREATE_DIR);
				showDialog(DIALOG_CREATE_DIR);
				break;
			case R.id.tool_level_up:
				if ("/".equals(makeMusiceDir)
						|| ("/" + SystemDef.System.CUSTOMPATH)
								.equals(makeMusiceDir)
						|| ("/" + SystemDef.System.CUSTOMPATH + "/")
								.equals(makeMusiceDir)) {
					// Toast.makeText(FileOperatorActivity.this,
					// "已经是根目录，不能返回", Toast.LENGTH_SHORT).show();
					toastShow("已经是根目录，不能返回");
				} else {
					int index = makeMusiceDir.lastIndexOf("/");
					makeMusiceDir = makeMusiceDir.substring(0, index);
					if ("/".equals(makeMusiceDir)) {
						loadFileList2(rootDir + makeMusiceDir);
					} else {
						loadFileList(rootDir + makeMusiceDir);
					}
					flushDirPath();// 刷新文件目录
					// 刷新listview
					flushListView();
				}
				break;
			case R.id.tool_bar_continue:
				Intent intent = new Intent();
				intent.setClass(FileManagerActivity.this, CameraActivity.class);
				FileManagerActivity.this.startActivity(intent);
				clearFile();
				FileManagerActivity.this.finish();
				break;
			case R.id.tool_bar_save_file:
				if (null == filePath) {
					toastShow("乐谱文件不存在");
					return;
				}
				// 将文件拷贝到当前目录下
				removeDialog(DIALOG_SAVE_FILE);
				showDialog(DIALOG_SAVE_FILE);
				break;
			case R.id.tool_paste_file:// 粘贴
				String currentDir = rootDir + makeMusiceDir;
				pasteOperator(new File(currentDir), copyPathList.get(0));
				Utils.getToast(FileManagerActivity.this, currentDir);
				break;
			}
		}

	}

	/**
	 * toast提示
	 * 
	 * @param text
	 */
	private void toastShow(String text) {
		Toast.makeText(FileManagerActivity.this, text, Toast.LENGTH_SHORT)
				.show();
	}

	private boolean copyFile(File src, File destFile) {

		if (!src.exists())
			return false;// 原目录不存在
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(src);
			out = new FileOutputStream(destFile);
			byte[] buf = new byte[1024 * 100];
			int b = 0;
			while ((b = in.read(buf)) != -1) {
				out.write(buf, 0, b);
			}
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				out.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_CREATE_DIR:// 创建文件目录
			createDir();
			break;
		case DIALOG_DEL_FILE_OK_CANCEL:// 删除文件确认弹框
			dialogDelFileConfig();
			break;
		case DIALOG_DEL_DIR_OK_CANCEL:// 删除文件目录确认弹框
			return dialogDelDirConfig();
		case DIALOG_RENAME_FILE:// 重命名文件
			renameFile();
			break;
		case DIALOG_RENAME_DIR:// 重命名目录
			renameDir();
			break;
		case DIALOG_SAVE_FILE:
			saveFile();
			break;
		}
		return super.onCreateDialog(id);
	}

	private void flushDirPath() {
		tvFilePath.setText(makeMusiceDir);
	}

	/*
	 * 保存文件
	 */
	private void saveFile() {
		LayoutInflater factory = LayoutInflater.from(FileManagerActivity.this);
		final View textEntryView = factory.inflate(
				R.layout.alert_dialog_savefile_entry, null);
		final EditText e = (EditText) textEntryView
				.findViewById(R.id.savefile_edit);
		e.addTextChangedListener(new TextWatcher() {

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
				if (s.toString().length() > 20) {
					// Toast.makeText(FileOperatorActivity.this, "目录名太长",
					// Toast.LENGTH_SHORT).show();
					toastShow("文件名太长");
					return;
				}
			}
		});
		myAlertDialog = (MyDialog) new MyDialog(FileManagerActivity.this);
		myAlertDialog.setTitle(R.string.alert_dialog_savefile_title);
		myAlertDialog.setIcon(R.drawable.alert_dialog_icon);
		myAlertDialog.setView(textEntryView);
		myAlertDialog.setButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String text = e.getText().toString();
				if (text.equals("") || text.length() == 0) {
					toastShow("文件名不能为空");
					return;
				} else {
					// 保存文件为jpg格式
					File file = new File(rootDir + makeMusiceDir + "/" + text
							+ ".jpg");
					if (!file.exists()) {
						loadProgress().show();
						File src = new File(filePath);
						if (copyFile(src, file)) {
							toastShow("文件保存成功");
							// 刷新当前目录
							loadFileList(rootDir + makeMusiceDir);
							flushListView();
							tool_bar_save_file.setVisibility(View.GONE);
							fileProgressHandler
									.sendEmptyMessage(PROGRESS_DIALOG_DISMESS_MESSAGE);
							return;
						}
						fileProgressHandler
								.sendEmptyMessage(PROGRESS_DIALOG_DISMESS_MESSAGE);
						toastShow("文件保存失败");
					} else {
						toastShow("文件已经存在");
					}
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

	/*
	 * 创建目录
	 */
	private void createDir() {
		LayoutInflater factory = LayoutInflater.from(FileManagerActivity.this);
		final View textEntryView = factory.inflate(
				R.layout.alert_dialog_text_entry, null);
		final EditText e = (EditText) textEntryView
				.findViewById(R.id.username_edit);
		e.addTextChangedListener(new TextWatcher() {

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
				if (s.toString().length() > 20) {
					// Toast.makeText(FileOperatorActivity.this, "目录名太长",
					// Toast.LENGTH_SHORT).show();
					toastShow("目录名太长");
					return;
				}
			}
		});
		myAlertDialog = (MyDialog) new MyDialog(FileManagerActivity.this);
		myAlertDialog.setTitle(R.string.fileList_createDir);
		myAlertDialog.setIcon(R.drawable.alert_dialog_icon);
		myAlertDialog.setView(textEntryView);
		myAlertDialog.setButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String text = e.getText().toString();
				if (text.equals("") || text.length() == 0) {
					toastShow("目录名不能为空");
					return;
				} else {
					// 文件目录创建层次判断，大于4层时将不能继续创建
					String tempName = "";
					if (makeMusiceDir.startsWith("//")) {

						tempName = makeMusiceDir.replace("//", "/");
					} else {
						tempName = makeMusiceDir;
					}
					if (!"".equals(tempName) && tempName.split("/").length == 5) {
						Utils.getToast(FileManagerActivity.this,
								"文件目录过多，请重新操作...");
						return;
					}
					File file = new File(rootDir + makeMusiceDir + "/" + text);
					if (!file.exists()) {
						if (file.mkdir()) {
							toastShow("目录创建成功");
							// 刷新当前目录
							loadFileList(rootDir + makeMusiceDir);
							flushListView();
						}
					} else {
						toastShow("目录已经存在");
					}
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

	/*
	 * 删除文件确认弹框
	 * 
	 * @return
	 */
	private void dialogDelFileConfig() {
		myAlertDialog = (MyDialog) new MyDialog(FileManagerActivity.this);
		myAlertDialog.setTitle(R.string.alert_dialog_del_file_title);
		myAlertDialog.setIcon(R.drawable.alert_dialog_icon);
		myAlertDialog.setButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String fileName = getSelectFileName();// 当前选中的文件名
				File file = new File(rootDir + makeMusiceDir + "/" + fileName);
				if (file.exists() && file.isFile()) {// 是文件
					loadProgress().show();
					if (file.delete()) {
						fileProgressHandler
								.sendEmptyMessage(PROGRESS_DIALOG_DISMESS_MESSAGE);
						toastShow(fileName + "文件删除成功");
						// 刷新数据
						loadFileList(rootDir + makeMusiceDir);
						flushListView();
					}
				} else {
					toastShow(fileName + "操作失败");
					fileProgressHandler
							.sendEmptyMessage(PROGRESS_DIALOG_DISMESS_MESSAGE);
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

	private ProgressDialog loadProgress() {
		if (null == pro_dialog) {
			pro_dialog = new ProgressDialog(FileManagerActivity.this);
		}
		pro_dialog.setMessage("正在处理中,请稍后...");
		pro_dialog.setIndeterminate(true);
		pro_dialog.setCancelable(false);
		return pro_dialog;
	}

	/*
	 * 删除文件目录确认弹框
	 * 
	 * @return
	 */
	private AlertDialog dialogDelDirConfig() {
		return new AlertDialog.Builder(FileManagerActivity.this)
				.setIcon(R.drawable.alert_dialog_icon)
				.setTitle(R.string.alert_dialog_del_dir_title)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String fileName = getSelectFileName();// 当前选中的文件名
								File file = new File(rootDir + makeMusiceDir
										+ "/" + fileName);

								if (file.exists() && file.isDirectory()) {// 是文件
									loadProgress().show();
									deleteFileDir(file.getPath());
									toastShow(fileName + "目录删除成功");
									loadFileList(rootDir + makeMusiceDir);
									flushListView();
									fileProgressHandler
											.sendEmptyMessage(PROGRESS_DIALOG_DISMESS_MESSAGE);
								} else {
									toastShow(fileName + "操作失败");
									fileProgressHandler
											.sendEmptyMessage(PROGRESS_DIALOG_DISMESS_MESSAGE);
								}

							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).create();
	}

	private void renameDir() {
		LayoutInflater factory1 = LayoutInflater.from(FileManagerActivity.this);
		final View textEntryView1 = factory1.inflate(
				R.layout.alert_dialog_text_entry, null);
		final EditText e1 = (EditText) textEntryView1
				.findViewById(R.id.username_edit);
		final File file = new File(rootDir + makeMusiceDir + "/"
				+ getSelectFileName());
		e1.setText(file.getName());
		if (!file.exists())
			return;
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
					// Toast.makeText(FileOperatorActivity.this, "目录名太长",
					// Toast.LENGTH_SHORT).show();
					toastShow("目录名太长");
					return;
				}
			}
		});
		String title = "";
		if (file.isFile()) {
			title = getSelectFileName() + "文件"
					+ getString(R.string.alert_dialog_rename_file_title);
		} else if (file.isDirectory()) {
			title = getSelectFileName() + "目录"
					+ getString(R.string.alert_dialog_rename_file_title);
		}
		myAlertDialog = (MyDialog) new MyDialog(FileManagerActivity.this);
		myAlertDialog.setTitle(title);
		myAlertDialog.setIcon(R.drawable.alert_dialog_icon);
		myAlertDialog.setView(textEntryView1);
		myAlertDialog.setButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String text = e1.getText().toString();
				if (text.equals("") || text.length() == 0) {
					// Toast.makeText(FileOperatorActivity.this, "目录名不能为空",
					// Toast.LENGTH_SHORT).show();
					toastShow("目录名不能为空");
					return;
				} else {
					File file1 = new File(rootDir + makeMusiceDir + "/" + text);
					if (!file1.exists()) {
						loadProgress().show();
						// 重命名操作
						if (file.renameTo(file1)) {
							// Toast.makeText(FileOperatorActivity.this,
							// file.getName() +"目录重命名成功",
							// Toast.LENGTH_SHORT).show();
							toastShow(file.getName() + "目录重命名成功");
							loadFileList(rootDir + makeMusiceDir);
							flushListView();
							fileProgressHandler
									.sendEmptyMessage(PROGRESS_DIALOG_DISMESS_MESSAGE);
						} else {
							// Toast.makeText(FileOperatorActivity.this,
							// file.getName() + "目录重命名失败",
							// Toast.LENGTH_SHORT).show();
							toastShow(file.getName() + "目录重命名失败");
							fileProgressHandler
									.sendEmptyMessage(PROGRESS_DIALOG_DISMESS_MESSAGE);
						}
					} else {
						// Toast.makeText(FileOperatorActivity.this, "当前目录已经存在",
						// Toast.LENGTH_SHORT).show();
						toastShow("当前目录已经存在");
					}
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

	/*
	 * 重命名方法
	 * 
	 * @return
	 */
	private void renameFile() {
		LayoutInflater factory1 = LayoutInflater.from(FileManagerActivity.this);
		final View textEntryView1 = factory1.inflate(
				R.layout.alert_dialog_text_entry, null);
		final EditText e1 = (EditText) textEntryView1
				.findViewById(R.id.username_edit);
		final File file = new File(rootDir + makeMusiceDir + "/"
				+ getSelectFileName());
		if (file.isFile()) {
			String myFileName = getSelectFileName().substring(0,
					getSelectFileName().lastIndexOf("."));
			e1.setText(myFileName);
		} else {
			e1.setText(file.getName());
		}
		if (!file.exists())
			return;
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
					// Toast.makeText(FileOperatorActivity.this, "文件名太长",
					// Toast.LENGTH_SHORT).show();
					toastShow("文件名太长");
					return;
				}
			}
		});
		String title = "";
		if (file.isFile()) {
			title = getSelectFileName() + "文件"
					+ getString(R.string.alert_dialog_rename_file_title);
		} else if (file.isDirectory()) {
			title = getSelectFileName() + "目录"
					+ getString(R.string.alert_dialog_rename_file_title);
		}
		myAlertDialog = (MyDialog) new MyDialog(FileManagerActivity.this);
		myAlertDialog.setTitle(title);
		myAlertDialog.setIcon(R.drawable.alert_dialog_icon);
		myAlertDialog.setView(textEntryView1);
		myAlertDialog.setButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String text = e1.getText().toString();
				if (text.equals("") || text.length() == 0) {
					toastShow("文件名不能为空");
					return;
				} else {
					String orgiFileName = file.getName();// 原文件名
					String subfix = orgiFileName.substring(
							orgiFileName.lastIndexOf(".") + 1,
							orgiFileName.length());
					File file1 = new File(rootDir + makeMusiceDir + "/" + text
							+ "." + subfix);
					if (!file1.exists()) {
						// 获取原文件后缀名
						// 重命名操作
						loadProgress().show();
						if (file.renameTo(file1)) {
							toastShow(file.getName() + "文件重命名成功");
							loadFileList(rootDir + makeMusiceDir);
							flushListView();
							fileProgressHandler
									.sendEmptyMessage(PROGRESS_DIALOG_DISMESS_MESSAGE);
						} else {
							toastShow(file.getName() + "文件重命名失败");
							fileProgressHandler
									.sendEmptyMessage(PROGRESS_DIALOG_DISMESS_MESSAGE);
						}
					} else {
						toastShow("当前文件已经存在");
					}
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

	/*
	 * 删除文件
	 * 
	 * @param path
	 */
	private void deleteFileDir(String path) {
		File file = new File(path);
		if (file.isFile()) {
			file.delete();
		} else if (file.isDirectory()) {
			for (File _file : file.listFiles()) {
				if (_file.isFile()) {
					_file.delete();
				} else {
					deleteFileDir(_file.getPath());

				}
			}
			file.delete();
		}
	}

	/*
	 * 获取当前目录下的文件（目录或者文件）
	 * 
	 * @param path
	 */
	private void loadFileList(String path) {
		File file = new File(path);
		currentFileList = new ArrayList<FileSelect>();
		if (file.isDirectory() && file.listFiles().length > 0) {
			for (File file1 : file.listFiles()) {
				if (file1.getPath().endsWith(".nomedia")) {
					continue;
				}
				FileSelect fs = new FileSelect(file1.getName(), false);
				currentFileList.add(fs);
			}
		}
	}

	/*
	 * 获取目录文件（目录或者文件）
	 * 
	 * @param path
	 */
	private void loadFileList2(String path) {
		File file = new File(path);
		currentFileList = new ArrayList<FileSelect>();
		if (file.isDirectory() && file.listFiles().length > 0) {
			for (File file1 : file.listFiles()) {
				if (file1.getPath().endsWith(".nomedia")) {
					continue;
				}
				// if(SystemDef.System.CUSTOMPATH.equals(file1.getName()) ||
				// SystemDef.System.NOTEPATH.equals(file1.getName())){
				FileSelect fs = new FileSelect(file1.getName(), false);
				currentFileList.add(fs);
				// }
			}
		}
	}

	private boolean isExtisFile(String path) {
		File file = new File(path);
		if (file.isDirectory() && file.listFiles().length > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 自定义dialog实现实现
	 */
	android.content.DialogInterface.OnClickListener onlongClick = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			setSelectFileName(currentFileList.get(mySelectPosition).getPath());// 设置当前选中的文件名
			File file = new File(rootDir + makeMusiceDir + "/"
					+ getSelectFileName());
			/*
			 * 放开操作权限 if("/".equals(makeMusiceDir)){
			 * Utils.getToast(FileOperatorActivity.this, "当前文件不能编辑！！！"); return;
			 * }
			 */
			if (which == 0) {// 删除
				// 判断是文件还是目录
				if (file.isFile()) {
					// 首先弹出友情提示框，提示是否删除当前文件，或者目录
					removeDialog(DIALOG_DEL_FILE_OK_CANCEL);
					showDialog(DIALOG_DEL_FILE_OK_CANCEL);
				} else if (file.isDirectory()) {
					removeDialog(DIALOG_DEL_DIR_OK_CANCEL);
					showDialog(DIALOG_DEL_DIR_OK_CANCEL);
				}
			} else if (which == 1) {// 重命名
				// 文件、文件夹的重命名
				// 判断是文件还是目录
				if (file.isFile()) {
					// 首先弹出友情提示框，提示是否删除当前文件，或者目录
					setSelectFileName(currentFileList.get(mySelectPosition)
							.getPath());
					removeDialog(DIALOG_RENAME_FILE);
					showDialog(DIALOG_RENAME_FILE);
				} else if (file.isDirectory()) {
					setSelectFileName(currentFileList.get(mySelectPosition)
							.getPath());
					removeDialog(DIALOG_RENAME_DIR);
					showDialog(DIALOG_RENAME_DIR);
				}
			} else if (which == 2) {// 复制
				setCopyStatus(true);// 设置复制状态为true
				// 将选中的文件、文件夹路径加入复制队列中
				if (copyPathList.size() > 0) {
					copyPathList.set(0, rootDir + makeMusiceDir + "/"
							+ getSelectFileName());// 重置索引为0项数据
				} else {
					copyPathList.add(rootDir + makeMusiceDir + "/"
							+ getSelectFileName());
				}
				tool_paste_file.setVisibility(View.VISIBLE);// 复制成功后，粘贴按钮可见
			} else if (which == 3) {// 粘贴(在复制状态下才有粘贴选项)
				setCopyStatus(false);// 清除复制状态
				// 获取复制的文件路径
				if (copyPathList.size() > 0) {// 当且存在时才进行操作
					String copyPath = copyPathList.get(0);
					// 当前sdcard状态
					pasteOperator(file, copyPath);
				}
			}
		}
	};

	class MyFilePasterThread implements Runnable {
		private File destFile;
		private File copyfile;

		public MyFilePasterThread(File destFile, File copyfile) {
			this.destFile = destFile;
			this.copyfile = copyfile;
		}

		@Override
		public void run() {
			filePaster(destFile, copyfile);
			fileProgressHandler
					.sendEmptyMessage(PROGRESS_DIALOG_PASTER_FINISH_MESSAGE);
		}

	}

	/**
	 * 文件转帖
	 * 
	 * @param destFile
	 * @param sourcePath
	 */
	private void pasteOperator(File destFile, String sourcePath) {
		File copyfile = new File(sourcePath);
		loadProgress().show();
		new Thread(new MyFilePasterThread(destFile, copyfile)).start();
	}

	private void filePaster(File destFile, File copyfile) {
		if (copyfile.exists() && copyfile.isFile()) {
			FileUtil.copyFile(copyfile.getPath(), destFile.getPath() + "/"
					+ copyfile.getName());
		} else if (copyfile.exists() && copyfile.isDirectory()) {
			FileUtil.copyFolder(copyfile.getPath(), destFile.getPath() + "/"
					+ copyfile.getName());
		} else {
			Utils.getToast(FileManagerActivity.this, "找不到要复制的文件，请检测当前文件是否存在");
		}
	}

	/**
	 * 自定义适配器
	 * 
	 * @author Administrator
	 * 
	 */
	class MyAdatper extends BaseAdapter {

		private Context context;

		MyAdatper(Context context, List<FileSelect> list) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return currentFileList.size();
		}

		@Override
		public Object getItem(int position) {
			return currentFileList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewCache viewCache = null;
			if (null == convertView) {
				viewCache = new ViewCache();
				convertView = LayoutInflater.from(context).inflate(
						R.layout.layout_file_list_item, null);
				viewCache.textView1 = (TextView) convertView
						.findViewById(R.id.textid);
				convertView.setTag(viewCache);
				// 刷新适配器后，将当前选中的目录刷新，只让选中的item背景为蓝色，其它恢复原色（只能选中一个item）
			} else {
				viewCache = (ViewCache) convertView.getTag();
			}
			convertView.setOnClickListener(new MyOnclickListener(context,
					convertView, position, currentFileList));// 初始化
			convertView.setOnLongClickListener(new MyOnLongClickListener(
					context, convertView, position));
			FileSelect fs = (FileSelect) currentFileList.get(position);
			if (fs.isSlect()) {
				convertView.setBackgroundColor(Color.BLUE);
			} else {
				convertView.setBackgroundColor(0);
			}
			viewCache.textView1.setText(fs.getPath());
			return convertView;
		}

	}

	/**
	 * 自定义长按监听事件
	 * 
	 * @author Administrator
	 * 
	 */
	class MyOnLongClickListener implements OnLongClickListener {

		private Context context;
		private View convertView;
		private int position;

		public MyOnLongClickListener(Context context, View convertView,
				int position) {
			this.context = context;
			this.convertView = convertView;
			this.position = position;
		}

		@Override
		public boolean onLongClick(View v) {
			String fileName = "";
			if (currentFileList != null && currentFileList.size() > 0) {
				fileName = currentFileList.get(position).getPath();
			}
			String operatorTitle = fileName
					+ getString(R.string.fileList_title);
			myAlertDialogbd = (MyDialogBuilder) new MyDialogBuilder(
					FileManagerActivity.this);
			myAlertDialogbd.setTitle(operatorTitle);
			myAlertDialogbd.setIcon(R.drawable.alert_dialog_icon);
			myAlertDialogbd.setView(null);
			// 获取当前选种长按的文件对象
			File myFile = new File(rootDir + makeMusiceDir + "/" + fileName);
			setCurrentPosition(position);// 设置当前选中的文件索引
			if (isCopyStatus && null != myFile && myFile.isDirectory()) {// 当前为copy状态并且当前文件是目录时才能粘贴
				myAlertDialogbd.setItems(R.array.fileOprArray2, onlongClick);
			} else {
				myAlertDialogbd.setItems(R.array.fileOprArray, onlongClick);
			}
			myAlertDialogbd.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			myAlertDialogbd.show();
			return false;
		}

	}

	/**
	 * 自定义监听事件
	 * 
	 * @author Administrator
	 * 
	 */
	class MyOnclickListener implements OnClickListener {
		private View view;// 选中view
		private Context context;// 上下文
		private int position;// 当前选中的位置

		public MyOnclickListener(Context context, View view, int position,
				List<FileSelect> list) {
			this.view = view;
			this.context = context;
			this.position = position;
			// this.list = list;
		}

		@Override
		public void onClick(View v) {
			if (null == currentFileList || currentFileList.size() == 0)
				return;
			FileSelect currentFs = currentFileList.get(position);
			if (currentFs == null)
				return;
			File _file = new File(rootDir + makeMusiceDir + "/"
					+ currentFs.getPath());
			if (_file.isDirectory()) {// 当前文件是目录
				loadFileList(rootDir + makeMusiceDir + "/"
						+ currentFs.getPath());
				// 刷新当前目录
				makeMusiceDir = makeMusiceDir + "/" + _file.getName();
				flushDirPath();
				flushListView();
			} else {// 文件
				if (_file.getPath().endsWith(".jpg")
						|| _file.getPath().endsWith(".png")) {
					// 需要判断是否为图片文件才跳转预览
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_EDIT);
					intent.setData(Uri.fromFile(_file));
					intent.putExtra("editPath", _file.getPath());
					intent.setClass(context, PhotoEditor.class);
					context.startActivity(intent);
				} else {
					Utils.getToast(FileManagerActivity.this, "当前文件不能预览！！！");
				}
			}
		}
	}

	private void flushListView() {
		if (null != myAdapter)
			myAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		if (null != pro_dialog) {
			if (pro_dialog.isShowing()) {
				pro_dialog.dismiss();
			}
			pro_dialog = null;
		}
		super.onDestroy();
	}

	static class ViewCache {
		TextView textView1;
	}

}
