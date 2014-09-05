package com.quku.activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.quku.ColorPickerDialog;
import com.quku.GraphicsActivity;
import com.quku.R;
import com.quku.Utils.SystemDef;
import com.quku.Utils.Utils;
import com.quku.entity.MyNote;
import com.quku.entity.MyNoteList;
import com.quku.note2.MyDialog;
import com.quku.service.MyNoteServiceImpl;

/**
 * 添加记事本（手写）activity
 * 
 * @author Administrator
 * 
 */
public class CreateNoteActivity extends GraphicsActivity implements ColorPickerDialog.OnColorChangedListener {

	private static final String TAG = "AddMyNoteActivity";
	private static final int MESSAGE_NOTIFY_PROGRESS_DIALOG = 1;// 弹出框进度条消息
	private static final int MESSAGE_FLUSH_NOTE_PAGE_NEW_FILE_SAVE_OK = 2;// 刷新页码成功消息
	private static final int MESSAGE_FLUSH_NOTE_PAGE_UP_PAGE_FILE_SAVE_OK = 3;// 刷新页码成功消息
	private static final int MESSAGE_FLUSH_NOTE_PAGE_FILE_REPLACE_OK = 4;// 刷新页码成功消息
	private static final int MESSAGE_FLUSH_NOTE_PAGE_ERROR = 5;// 刷新页码失败消息
	private static final int DIALOG_NOTEBG_REPLACE_CONFIG = 1;// NOTE背景替换确认dialog
	private static final int DIALOG_NOTE_DELETE_CONFIG = 2;// 删除notelist记录的dialog
	private static final int DIALOG_NOTE_BACK_NOTELIST_CONFIG = 3;// 回退到note主界面
	private static final int NEW_FILE_SAVE = 1;// 文件保存
	private static final int FILE_REPLACE = 2;// 文件替换
	private static final int UP_PAGE_FILE_SAVE = 3;// 上一页文件保存
	private String makeDir = Environment.getExternalStorageDirectory().getPath() + "/tflash/musicpaper/原创乐谱/";
	/** view 控件定义 */
	private Context myContext;
	LinearLayout mLlBack;// 回到备忘录主界面
	Button fontButton;
	Button colorButton;
	Button parseButton;
	private TextView mTvTitle;
	Button clearButton;
	LinearLayout upPageLayout;
	LinearLayout nextPageLayout;
	ImageButton upPageButton;
	TextView currentPageNum;
	TextView totalPageNum;
	ImageButton nextPageButton;
	// ImageView notebackgroud;
	LinearLayout imageLinearLayout;
	ImageButton switchbg;// 背景切换
	EditText notelistname;// 画板文件名
	LinearLayout mLlFinish;// 备忘录完成按钮
	private TextView mTvRight;
	Button noteDelete;// 删除
	// 初始化对象
	private MyNote myNote = null;
	private List<MyNoteList> myNoteList = null;

	/** 画笔全局变量定义 */
	private Bitmap _mBitmap;
	private Canvas _mCanvas;
	private Path _mPath;
	private Paint _mBitmapPaint;// 画布的画笔
	private Paint _mPaint;// 真实的画笔
	private float _mX, _mY;// 临时点坐标
	private static final float TOUCH_TOLERANCE = 5;
	private int _myColor;
	public static int picAllViewWide = SystemDef.System.IMG_PICTURE_WIDTH;
	public static int picAllViewHigh = 1170;
	// 记录Path路径的对象
	private DrawPath dp;

	private class DrawPath {
		public Path path;// 路径
		public Paint paint;// 画笔
	}

	private MyView myView;// 画板view
	private int paintSize = 5;// 字体粗细
	private View _view;// 当前页面view
	private MaskFilter mEmboss;
	private MaskFilter mBlur;
	// 乐谱背景颜色
	private int noteMusiceBackImgs[] = {
			R.drawable.note_wirte_bg_white,
			R.drawable.note_wirte_bg_pruple,
			R.drawable.note_wirte_bg_grap,
			R.drawable.note_wirte_bg_shallyellow };
	// 记事本背景颜色
	private int noteBackImgs[] = {
			R.drawable.note_write_empty_bg,
			R.drawable.note_write_grid_bg,
			R.drawable.note_write_steak_bg };

	/**
	 * 二维数据，第一元素为小图标，第二元素数组为大图标
	 */
	private int note_write_small_music_icons[][] = {
			{
					R.drawable.note_write_choice_music_a,
					R.drawable.note_write_choice_music_b,
					R.drawable.note_write_choice_music_c,
					R.drawable.note_write_choice_music_d },
			{
					R.drawable.note_wirte_bg_white,
					R.drawable.note_wirte_bg_shallyellow,
					R.drawable.note_wirte_bg_pruple,
					R.drawable.note_wirte_bg_grap } };
	private int note_write_small_note_icons[][] = {
			{
					R.drawable.note_wirte_choice_note_a,
					R.drawable.note_wirte_choice_note_b,
					R.drawable.note_wirte_choice_note_c },
			{ R.drawable.note_write_steak_bg, R.drawable.note_write_empty_bg, R.drawable.note_write_grid_bg } };

	private Handler myHander = null;// 自定义handler刷新view
	private int noteWrite_Type = 0;// 乐谱背景类型（乐谱、记事两种类型）
	// popupWindow定义
	private PopupWindow popuWindow;
	private int currentBackground;// 当前画板背景资源id
	private MyNoteServiceImpl myNoteService;
	private InputMethodManager imm;// 软键盘
	MyDialog myAlertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myContext = this;
		myNoteService = new MyNoteServiceImpl(this);
		_view = (View) LayoutInflater.from(this).inflate(R.layout.activity_note_create, null);// 获取View
		initDataLoad();// 初始化数据
		initPaintBackground();// 初始化加载画板背景
		initPopupWindow();// 加载popupwindow背景
		setContentView(_view);// 设置当前activity View
		initView();// 初始化view 控件
		initMyPaint();// 初始化画笔
		handlerMessage();// handler message处理
		imm = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
	}

	/*
	 * 初始化数据
	 */
	private void initDataLoad() {
		// 新建备忘录
		if (SystemDef.NoteWrite.NOTE_INSERT.equals(this.getIntent().getAction())) {
			myNote = new MyNote();
			myNoteList = new ArrayList<MyNoteList>();
		}
		// 编辑备忘录，则加载当前备忘录已经存在的数据
		else if (SystemDef.NoteWrite.NOTE_EDITE.equals(this.getIntent().getAction())) {
			MyNote _myNote = (MyNote) this.getIntent().getSerializableExtra("myNote");
			if (null != _myNote) {
				// 根据_myNote查询list记录
				_myNote = myNoteService.getMyNote(_myNote);
				if (null != _myNote.getNoteList()) {
					myNoteList = _myNote.getNoteList();
				} else {
					myNoteList = new ArrayList<MyNoteList>();
				}
				myNote = _myNote;
			} else {
				myNote = new MyNote();
				myNoteList = new ArrayList<MyNoteList>();
			}

		}
	}

	/*
	 * 加载popupwindow背景
	 */
	private void initPopupWindow() {
		View popuView = this.getLayoutInflater().inflate(R.layout.layout_notewrite_choice_popup, null);
		createPopupView(popuView);
		popuWindow = new PopupWindow(popuView, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		popuWindow.setBackgroundDrawable(getResources().getDrawable(R.color.alpha_black));
		popuWindow.setOutsideTouchable(true);// 不能在没有焦点的时候使用
	}

	/*
	 * 创作乐谱
	 * 
	 * @return
	 */
	private void createPopupView(View popuView) {
		LinearLayout choice_bg_layout = (LinearLayout) popuView.findViewById(R.id.choice_bg_layout);
		int[] array = null;
		if (noteWrite_Type == 1) {
			array = note_write_small_music_icons[0];
		} else if (noteWrite_Type == 2) {
			array = note_write_small_note_icons[0];
		}
		LinearLayout.LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		if (null != array) {
			int index = 0;
			for (int resId : array) {
				ImageButton imgButton = new ImageButton(this);
				imgButton.setId(resId);
				imgButton.setImageResource(resId);
				imgButton.setScaleType(ImageView.ScaleType.FIT_XY);
				imgButton.setPadding(10, 10, 10, 10);
				// 添加监听事件
				imgButton.setOnClickListener(new MyPopupWindowListenner(index, noteWrite_Type));
				choice_bg_layout.addView(imgButton, params);
				index++;
			}
		}
	}

	/**
	 * 背景选择popupWindow弹出框监听事件
	 * 
	 * @author Administrator
	 * 
	 */
	class MyPopupWindowListenner implements android.view.View.OnClickListener {
		private int index;
		private int myType;

		public MyPopupWindowListenner(int index, int myType) {
			this.index = index;
			this.myType = myType;
		}

		@Override
		public void onClick(View v) {
			// 刷新画板背景
			if (myView.isMyViewIsEdit()) {// 编辑状态
				popuWindow.dismiss();
				Utils.getToast(CreateNoteActivity.this, "当前为编辑状态，不能更换背景");
				return;
			} else {// 不为编辑状态
				removeDialog(DIALOG_NOTEBG_REPLACE_CONFIG);
				/**
				 * 1、需要判断是否进行背景替换 2、确定之后才进行背景替换
				 */
				Bundle b = new Bundle();
				b.putInt("currentIndex", index);
				b.putInt("myType", myType);
				showDialog(DIALOG_NOTEBG_REPLACE_CONFIG, b);// 弹出带数据的dialog
			}
			popuWindow.dismiss();
		}

	}

	/**
	 * 重写oncreateDialog方法
	 */
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
			case DIALOG_NOTEBG_REPLACE_CONFIG:// note背景替换时的确认dialog
				dialogReplaceNoteBg(args);
				break;
			case DIALOG_NOTE_DELETE_CONFIG:// 删除notelist记录是的dialog
				dialogDelNoteList();
				break;
			case DIALOG_NOTE_BACK_NOTELIST_CONFIG:
				dialogBackNoteMain();
				break;
		}
		return super.onCreateDialog(id, args);
	}

	/*
	 * 替换note背景时的确认dialog
	 * 
	 * @return
	 */
	private void dialogBackNoteMain() {
		myAlertDialog = (MyDialog) new MyDialog(myContext);
		myAlertDialog.setIcon(R.drawable.alert_dialog_icon);
		myAlertDialog.setTitle("提示");
		myAlertDialog.setButton("不保存", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 当前需要清空
				CreateNoteActivity.this.finish();
			}
		});
		myAlertDialog.setButton2("保存后返回", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveNote2DB();
			}
		});
		myAlertDialog.show();
	}

	/*
	 * 替换note背景时的确认dialog
	 * 
	 * @return
	 */
	private void dialogDelNoteList() {
		myAlertDialog = (MyDialog) new MyDialog(myContext);
		myAlertDialog.setIcon(R.drawable.alert_dialog_icon);
		myAlertDialog.setTitle("提示");
		myAlertDialog.setMessage("删除当前页面，删除之后将不能恢复？");
		myAlertDialog.setButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (myNoteList.size() > 0) {// 有页码
					int currentPage = Integer.parseInt(currentPageNum.getText().toString());
					if (myNoteList.size() == 1) {// 当前只有一个画板时
						if (currentPage > 1) {// 说明为新建画板界面，不管是编辑或者非编辑状态，将直接回到第一张画板
							// 直接跳转到第一页码，当前不需要做删除操作
							loadMyView(CreateNoteActivity.this,
									Drawable.createFromPath(myNoteList.get(0).getPicPath()), false);
							notelistname.setText(myNoteList.get(0).getNotelistname());// 刷新文件名
						} else {// 当前为预览画板状态
							myNoteList.remove(0);// 直接删除第一个位置，但不先清除文件
													// ，在返回或者确认保存后再做处理
							loadMyView(CreateNoteActivity.this, getResources().getDrawable(getCurrentBackground()),
									true);
							notelistname.setText("");// 刷新文件名
						}
						flushTotalPage("+");
						flushCurrentPage("1");
					} else {// 当前有多个画板，存在多个页码的情况下处理
							// 判断需要加载后一张画板还是加载前一张画板（默认加载后一张画板）

						// 说明已经到了最后一页了，当前只有一种场景是画板预览时的最后一页的画板,删除当前画板，加载上一张画板
						if (currentPage == myNoteList.size() && currentPage > 0) {
							myNoteList.remove(myNoteList.size() - 1);// 删除当前画板
							loadMyView(CreateNoteActivity.this,
									Drawable.createFromPath(myNoteList.get(myNoteList.size() - 1).getPicPath()), false);
							flushTotalPage(String.valueOf(currentPage - 1));
							flushCurrentPage(String.valueOf(currentPage - 1));
							notelistname.setText(myNoteList.get(myNoteList.size() - 1).getNotelistname());// 刷新文件名
							if (myNoteList.size() <= 1) {
								flushTotalPage("+");
							}
						} else if (currentPage > myNoteList.size()) {// 说明为新建页面，直接加载一张画板，不需要删除
							loadMyView(CreateNoteActivity.this,
									Drawable.createFromPath(myNoteList.get(myNoteList.size() - 1).getPicPath()), false);
							flushTotalPage(String.valueOf(currentPage - 1));
							flushCurrentPage(String.valueOf(currentPage - 1));
							notelistname.setText(myNoteList.get(myNoteList.size() - 1).getNotelistname());// 刷新文件名
							if (myNoteList.size() <= 1) {
								flushTotalPage("+");
							}
						} else if (currentPage < myNoteList.size()) {
							if (currentPage <= 0) {
								loadMyView(CreateNoteActivity.this, getResources().getDrawable(getCurrentBackground()),
										true);
								flushTotalPage("+");
								flushCurrentPage("1");
								notelistname.setText("");// 刷新文件名
							} else {// 加载一下张
								loadMyView(CreateNoteActivity.this,
										Drawable.createFromPath(myNoteList.get(currentPage).getPicPath()), false);
								flushTotalPage(String.valueOf(myNoteList.size() - 1));
								flushCurrentPage(String.valueOf(currentPage));
								notelistname.setText(myNoteList.get(currentPage).getNotelistname());// 刷新文件名
								myNoteList.remove(currentPage - 1);
								if (myNoteList.size() <= 1) {
									flushTotalPage("+");
								}
							}
						}
					}
				} else {// 新添加的,直接恢复到默认背景
					loadMyView(CreateNoteActivity.this, getResources().getDrawable(getCurrentBackground()), true);
					flushTotalPage("+");
					flushCurrentPage("1");
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
	 * 替换note背景时的确认dialog
	 * 
	 * @return
	 */
	private void dialogReplaceNoteBg(Bundle bundle) {
		final int index = bundle.getInt("currentIndex");
		final int myType = bundle.getInt("myType");
		System.out.println(" dialog index = " + index + "\t myType = " + myType);
		myAlertDialog = (MyDialog) new MyDialog(myContext);
		myAlertDialog.setTitle(R.string.alert_config_note_bg_title);
		myAlertDialog.setButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (myType == 1) {
					loadMyView(CreateNoteActivity.this,
							getResources().getDrawable(note_write_small_music_icons[1][index]), true);
					setCurrentBackground(note_write_small_music_icons[1][index]);// 记录当前背景
				} else {
					loadMyView(CreateNoteActivity.this,
							getResources().getDrawable(note_write_small_note_icons[1][index]), true);
					setCurrentBackground(note_write_small_note_icons[1][index]);// 记录当前背景
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
	 * 根据图片路径创建drawble对象
	 * 
	 * @param path
	 * 
	 * @return
	 */
	private Drawable createDrawable(String path) {
		File file = new File(path);
		// 判断当前图片是否存在
		if (file.exists()) {
			Drawable drawable = Drawable.createFromPath(path);
			if (null != drawable)
				return drawable;
		}
		return null;
	}

	/*
	 * 初始化画板背景
	 */
	private void initPaintBackground() {
		int type = this.getIntent().getIntExtra("type", 0);// 不管为新建还是编辑都需要传type
		// 新建note为默认背景，编辑的话需要加载note已经存在的背景,需要区分当前操作
		switch (type) {
			case SystemDef.NoteWrite.TYPE_MAKEMUSIC:
				if (null != myNoteList && myNoteList.size() > 0) {// 说明为编辑状态
					loadMyView(this.getBaseContext(), createDrawable(myNoteList.get(0).getPicPath()), false);// 初始化画板背景图片
					// setCurrentBackground(noteMusiceBackImgs[0]);//记录当前背景,主要用来新建时的背景
				} else {
					loadMyView(this.getBaseContext(), getResources().getDrawable(noteMusiceBackImgs[0]), true);// 初始化画板背景图片
				}
				setCurrentBackground(noteMusiceBackImgs[0]);// 记录当前背景
				noteWrite_Type = 1;
				myNote.setNoteType(type);
				break;
			case SystemDef.NoteWrite.TYPE_EMPTY:
				if (null != myNoteList && myNoteList.size() > 0) {// 说明为编辑状态
					loadMyView(this.getBaseContext(), createDrawable(myNoteList.get(0).getPicPath()), false);// 初始化画板背景图片
					// setCurrentBackground(noteMusiceBackImgs[0]);//记录当前背景,主要用来新建时的背景
				} else {
					loadMyView(this.getBaseContext(), getResources().getDrawable(noteBackImgs[0]), true);// 初始化画板背景图片
				}
				myNote.setNoteType(type);
				noteWrite_Type = 2;
				setCurrentBackground(noteBackImgs[0]);// 记录当前背景
				break;
			case SystemDef.NoteWrite.TYPE_GRID:
				if (null != myNoteList && myNoteList.size() > 0) {// 说明为编辑状态
					loadMyView(this.getBaseContext(), createDrawable(myNoteList.get(0).getPicPath()), false);// 初始化画板背景图片
					// setCurrentBackground(noteMusiceBackImgs[0]);//记录当前背景,主要用来新建时的背景
				} else {
					loadMyView(this.getBaseContext(), getResources().getDrawable(noteBackImgs[1]), true);// 初始化画板背景图片
				}
				myNote.setNoteType(type);
				noteWrite_Type = 2;
				setCurrentBackground(noteBackImgs[1]);// 记录当前背景
				break;
			case SystemDef.NoteWrite.TYPE_STREAK:
				if (null != myNoteList && myNoteList.size() > 0) {// 说明为编辑状态
					loadMyView(this.getBaseContext(), createDrawable(myNoteList.get(0).getPicPath()), false);// 初始化画板背景图片
					// setCurrentBackground(noteMusiceBackImgs[0]);//记录当前背景,主要用来新建时的背景
				} else {
					loadMyView(this.getBaseContext(), getResources().getDrawable(noteBackImgs[2]), true);// 初始化画板背景图片
				}
				myNote.setNoteType(type);
				noteWrite_Type = 2;
				setCurrentBackground(noteBackImgs[2]);// 记录当前背景
				break;
		}
	}

	/**
	 * 获取当前新建记录时的背景图片资源id
	 * 
	 * @return
	 */
	public int getCurrentBackground() {
		return currentBackground;
	}

	/**
	 * 设置当前新建记录时的背景图片资源id
	 * 
	 * @param currentBackground
	 */
	public void setCurrentBackground(int currentBackground) {
		this.currentBackground = currentBackground;
	}

	private void handlerMessage() {
		// 实例handler
		myHander = new Handler() {
			// handler的消息处理
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
					case MESSAGE_NOTIFY_PROGRESS_DIALOG:// 加载进度弹出框消息
						break;
					case MESSAGE_FLUSH_NOTE_PAGE_NEW_FILE_SAVE_OK:// 刷新页码成功
						String filePath = (String) msg.getData().get("path");
						// 当前信息保存
						MyNoteList noteList = new MyNoteList();
						noteList.setPicPath(filePath);
						if (!"".equals(notelistname.getText().toString())) {
							noteList.setNotelistname(notelistname.getText().toString());
						}
						myNoteList.add(noteList);
						// 更换新背景
						loadMyView(CreateNoteActivity.this, getResources().getDrawable(getCurrentBackground()), true);
						// 刷当前页码数
						flushCurrentPage(String.valueOf(Integer.parseInt(currentPageNum.getText().toString()) + 1));
						// 刷新总页码数
						if (myNoteList.size() == SystemDef.NoteWrite.NOTE_WIRTE_TOTAL_NUMBER_MAX) {// 最大页码限制数
							flushTotalPage(String.valueOf(myNoteList.size()));
						} else {
							flushTotalPage("+");
						}
						notelistname.setText("");
						break;
					case MESSAGE_FLUSH_NOTE_PAGE_FILE_REPLACE_OK:
						// //更换新背景
						// try {
						// int current = Integer.parseInt(currentPageNum
						// .getText().toString());
						// String _path = myNoteList.get(current - 1)
						// .getPicPath();
						// loadMyView(AddMyNoteActivity.this,
						// Drawable.createFromPath(_path));
						// } catch (Exception e) {
						// Log.d(SystemDef.Debug.TAG, TAG +
						// " handlerMessage MESSAGE_FLUSH_NOTE_PAGE_FILE_REPLACE_OK exception = "
						// + e.getMessage());
						// }
						break;
					case MESSAGE_FLUSH_NOTE_PAGE_UP_PAGE_FILE_SAVE_OK:// 切换到上一页时，当前页面编辑后保存消息
						// String filePath1 = (String)msg.getData().get("path");
						// //当前信息保存
						// MyNoteList noteList1 = new MyNoteList();
						// noteList1.setPicPath(filePath1);
						// myNoteList.add(noteList1);

						break;
					case MESSAGE_FLUSH_NOTE_PAGE_ERROR:
						String errorInfo = msg.getData().getString("error");
						Utils.getToast(CreateNoteActivity.this, errorInfo);
						break;
				}
			}
		};
	}

	// 刷新当前页码
	private void flushCurrentPage(String value) {
		currentPageNum.setText(value);
	}

	// 刷新总页码数
	private void flushTotalPage(String value) {
		totalPageNum.setText(value);
	}

	/*
	 * 加载画板背景图片
	 * 
	 * @param context
	 * 
	 * @param drawable
	 * 
	 * @param myViewIsEmptyView 当前画板是否为空白画板
	 */
	private void loadMyView(Context context, Drawable drawable, boolean myViewIsEmptyView) {
		myView = new MyView(context, drawable, myViewIsEmptyView);
		imageLinearLayout = (LinearLayout) _view.findViewById(R.id.imageLinearLayout);
		imageLinearLayout.removeAllViews();
		imageLinearLayout.addView(myView);
		_view.postInvalidate();
	}

	/*
	 * 初始化画笔
	 */
	private void initMyPaint() {
		_mPaint = new Paint();
		_mPaint.setAntiAlias(true);
		_mPaint.setStyle(Paint.Style.STROKE);
		_mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
		_mPaint.setStrokeCap(Paint.Cap.SQUARE);// 形状
		_mPaint.setStrokeWidth(5);// 画笔宽度
		mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 6, 3.5f);
		mBlur = new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL);
	}

	/*
	 * 初始化view
	 */
	private void initView() {
		noteDelete = (Button) this.findViewById(R.id.noteDelete);
		notelistname = (EditText) this.findViewById(R.id.notelistname);
		mLlFinish = (LinearLayout) this.findViewById(R.id.title_with_back_title_btn_right);
		mTvRight = (TextView) findViewById(R.id.tv_title_with_right);
		switchbg = (ImageButton) this.findViewById(R.id.switchbg);// 背景切换
		mLlBack = (LinearLayout) this.findViewById(R.id.title_with_back_title_btn_left);
		fontButton = (Button) this.findViewById(R.id.notefont);
		colorButton = (Button) this.findViewById(R.id.notecolor);
		parseButton = (Button) this.findViewById(R.id.noteparse);
		clearButton = (Button) this.findViewById(R.id.noteClear);
		mTvTitle = (TextView) findViewById(R.id.title_with_back_title_btn_mid);
		mTvTitle.setText("乐谱创作");
		upPageButton = (ImageButton) this.findViewById(R.id.uppage);
		currentPageNum = (TextView) this.findViewById(R.id.currentpagenum);
		nextPageButton = (ImageButton) this.findViewById(R.id.nextpage);
		totalPageNum = (TextView) this.findViewById(R.id.totalpagenum);
		mTvRight.setText("完成");
		// 初始化页码
		// upPageButton.setText(" < ");
		currentPageNum.setText("1");
		totalPageNum.setText("+");
		// nextPageButton.setText("  >");
		if (myNoteList.size() > 1) {// 编辑状态下，重新刷新总页码数
			flushTotalPage(String.valueOf(myNoteList.size()));
		}
		noteDelete.setOnClickListener(new MybuttonListener());
		mLlFinish.setOnClickListener(new MybuttonListener());
		switchbg.setOnClickListener(new MybuttonListener());
		mLlBack.setOnClickListener(new MybuttonListener());
		fontButton.setOnClickListener(new MybuttonListener());
		colorButton.setOnClickListener(new MybuttonListener());
		parseButton.setOnClickListener(new MybuttonListener());
		clearButton.setOnClickListener(new MybuttonListener());
		// upPageLayout.setOnClickListener(new MybuttonListener());
		// nextPageLayout.setOnClickListener(new MybuttonListener());

		upPageButton.setOnClickListener(new MybuttonListener());
		nextPageButton.setOnClickListener(new MybuttonListener());

		// 设置title和文件名
		if (null != myNote.getTitle()) {
			notelistname.setText(myNote.getTitle());
		}
		if (myNoteList.size() > 0) {
			if (null != myNoteList.get(0).getNotelistname()) {
				notelistname.setText(myNoteList.get(0).getNotelistname());
			}
		}
	}

	/*
	 * 保存备忘录入库
	 */
	private void saveNote2DB() {
		if (null == myNote || null == myNoteList) {
			return;
		}
		int currentNum = Integer.parseInt(currentPageNum.getText().toString());
		String totalPageText = totalPageNum.getText().toString();
		if (currentNum == 1 && myNoteList.size() == 0) {// 第一页编辑处理保存
			if (myView.isMyViewIsEdit()) {// 编辑过直接保存
				if ("".equals(notelistname.getText().toString())) {
					Utils.getToast(myContext, "请先为当前画板定义文件名");
					notelistname.requestFocus();
					return;
				}
				newSaveFile(notelistname.getText().toString());
			} else {
				Utils.getToast(myContext, "不能添加空白页面，请先编辑！！！");
				return;
			}
		} else {
			if (totalPageText.equals("+")) {// 已经在最后一页，新建页面的编辑
				if (myView.isMyViewIsEdit()) {// 编辑过直接保存
					if ("".equals(notelistname.getText().toString())) {
						Utils.getToast(myContext, "请先为当前画板定义文件名");
						notelistname.requestFocus();
						return;
					}
					newSaveFile(notelistname.getText().toString());
				}
			} else {// 不为最后一页，编辑过，则需要替换当前页面
				if (myView.isMyViewIsEdit()) {// 编辑替换
					try {
						if ("".equals(notelistname.getText().toString())) {
							Utils.getToast(myContext, "请先为当前画板定义文件名");
							notelistname.requestFocus();
							return;
						}
						// 获取当前图片路径
						String currentImgPath = myNoteList.get(currentNum - 2).getPicPath();
						Drawable drawable = Drawable.createFromPath(currentImgPath);
						if (null == drawable) {
							Utils.getToast(CreateNoteActivity.this, "数据异常，请进行清空或者删除操作");
							return;
						}
						// 替换
						new SaveImageThread(currentImgPath, drawable, FILE_REPLACE).start();
					} catch (Exception e) {
						Log.d(SystemDef.Debug.TAG, TAG + " saveNote2DB myNoteList size = " + (myNoteList.size())
								+ "currentNum = " + (currentNum - 2) + "  exception = " + e.getMessage());
					}
				}
			}
		}

		// 需要判断当前note的title和notelist的文件名
		if ("".equals(notelistname.getText().toString())) {
			Utils.getToast(myContext, "还未定义标题");
			notelistname.requestFocus();
			return;
		}
		int temp_index = 0;
		StringBuffer strBuffer = new StringBuffer();
		for (MyNoteList _noteList : myNoteList) {
			String noteFileName = _noteList.getNotelistname();
			if (null == noteFileName || "".equals(noteFileName)) {
				strBuffer.append("第" + (temp_index + 1) + "页，未定义文件 名").append("\n");
			}
			temp_index++;
		}
		// 判断notelist记录是否有文件名
		if (strBuffer.length() > 0) {
			Utils.getToast(myContext, strBuffer.toString());
			return;
		}
		// /**
		// * 不管新增还是编辑，统一用新增入库:
		// * 1、清空之前数据
		// * 2、组装myNote对象
		// */
		// List<MyNoteList> orgiMyNoteList = null;//原有的mynotelist记录
		// 删除原有记录
		if (null != myNote && 0 != myNote.getId()) {// id不为0，说明是编辑
			MyNote _tempMyNote = myNoteService.getMyNote(new MyNote(myNote.getId(), null, null));
			if (null != _tempMyNote) {
				// 清空mynote
				myNoteService.deleteMyNote(myNote.getId());
				List<MyNoteList> _tempMyNoteList = _tempMyNote.getNoteList();
				if (null != _tempMyNoteList && _tempMyNoteList.size() > 0) {
					// orgiMyNoteList = _tempMyNoteList;
					// 清空mynotelist
					myNoteService.clearMyNoteListAll(myNote.getId());
				}
			}
		}
		if (null == myNoteList) {
			Utils.getToast(myContext, "当前没有要保存的信息");
			return;
		}
		myAlertDialog = (MyDialog) new MyDialog(myContext);
		myAlertDialog.setIcon(R.drawable.alert_dialog_icon);
		myAlertDialog.setTitle("是否保存");
		myAlertDialog.setButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				/**
				 * 开始处理数据入库: 1、先添加myNote信息 2、根据myNote入库id再添加myNoteList信息
				 */
				myNote.setTitle(notelistname.getText().toString());
				myNote.setNoteList(myNoteList);
				if (null != myNoteService.insert(myNote)) {
					Utils.getToast(CreateNoteActivity.this, "操作成功");
					// Intent intent = new Intent();
					// intent.setClass(AddMyNoteActivity.this,
					// MyNoteListActivity.class);
					// AddMyNoteActivity.this.startActivity(intent);
					CreateNoteActivity.this.finish();
				} else {
					Utils.getToast(CreateNoteActivity.this, "操作失败");
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

	/**
	 * 自定义按钮的监听类
	 * 
	 * @author Administrator
	 * 
	 */
	class MybuttonListener implements android.view.View.OnClickListener {

		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.title_with_back_title_btn_right:
					if ("".equals(notelistname.getText().toString())) {
						Utils.getToast(CreateNoteActivity.this, "标题不能为空");
						return;
					}
					if (myView.isMyViewIsEmptyView() && !myView.isMyViewIsEdit()) {
						Utils.getToast(CreateNoteActivity.this, "不能添加空白页面，请先编辑！！！");
						return;
					}
					saveNote2DB();
					break;
				case R.id.switchbg:// 切换画板背景
					if (popuWindow != null) {
						popuWindow.showAtLocation(notelistname, Gravity.CENTER, 0, 0);
					}
					break;
				case R.id.title_with_back_title_btn_left:// 回到备忘录主页
					showDialog(DIALOG_NOTE_BACK_NOTELIST_CONFIG);
					break;
				case R.id.notefont:// 字体
					setFont();
					break;
				case R.id.noteClear:// 清空
					noteClear();
					break;
				case R.id.noteDelete:// 删除
					noteDelete();
					break;
				case R.id.noteparse:// 橡皮檫
					_mPaint.setStrokeWidth(50);
					_mPaint.setColor(Color.WHITE);
					_mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
					break;
				case R.id.notecolor:// 颜色
					_mPaint.setXfermode(null);
					_mPaint.setAlpha(0xFF);
					showColorDialog();
					break;
				case R.id.uppage:// 上一页
					upPage();
					break;
				case R.id.nextpage:// 下一页
					nextPage();
					break;
			}

		}

		/*
		 * 删除当前页面 1、删除当前页面，如果有下页将直接展示下一页，并刷新页码 2、删除当前页面，如果没有下页，将直接展示默认界面，并刷新页码
		 */
		private void noteDelete() {
			showDialog(DIALOG_NOTE_DELETE_CONFIG);
		}

		/**
		 * 清除当前页 1、如果已经保存了的页面，则删除，并且从list集合中清掉 2、如果为新建界面还未保存，则恢复默认背景画板
		 */
		private void noteClear() {
			// if(myView.isMyViewIsEmptyView()){//如果为空白页，则直接恢复到默认画板背景
			loadMyView(CreateNoteActivity.this, getResources().getDrawable(getCurrentBackground()), true);
			// }
		}
	}

	/*
	 * 上一页
	 */
	private void upPage() {
		int currentPage = 0;
		try {
			// 判断是否为首页
			currentPage = Integer.parseInt(currentPageNum.getText().toString());
			if (currentPage <= 1) {
				Utils.getToast(CreateNoteActivity.this, "已经是最后一页");
				return;
			}
			// 如果不在第一页
			if (null != myNoteList && myNoteList.size() > 0) {// 需要判断备忘录记录列表信息
				// 判断当前页面是否编辑，如果编辑了，则需要保存
				if (myView.isMyViewIsEdit()) {
					// newSaveFile();
					// 如果当前是最后一页，需要保存当前页面
					if (currentPage > myNoteList.size()) {
						if ("".equals(notelistname.getText().toString())) {
							Utils.getToast(this, "请先为当前画板定义文件名");
							notelistname.requestFocus();
							return;
						}
						upPageSaveFile(notelistname.getText().toString());// 回退到上一页时，当前页面修改了，保存当前页面
					} else {// 如果不是最后一页，则替换当前页
						if ("".equals(notelistname.getText().toString())) {
							Utils.getToast(this, "请先为当前画板定义文件名");
							notelistname.requestFocus();
							return;
						}
						// 获取当前图片路径
						String currentImgPath = myNoteList.get(currentPage - 1).getPicPath();
						// 替换是不能用原先的画板背景，不然导致修改的和原先的重叠了,必须用默认背景
						// 修改屏蔽 Drawable drawable
						// =Drawable.createFromPath(currentImgPath);
						// if(null ==drawable ){
						// Utils.getToast(AddMyNoteActivity.this,
						// "数据异常，请进行清空或者删除操作");
						// return;
						// }
						// 替换
						new SaveImageThread(
								currentImgPath, getResources().getDrawable(getCurrentBackground()), FILE_REPLACE)
								.start();
						// 设置当前文件名
						if (!"".equals(notelistname.getText().toString())) {
							myNoteList.get(currentPage - 1).setNotelistname(notelistname.getText().toString());
						}
					}

				} else if (myView.isMyViewIsEmptyView()) {// 如果当前页面未空白页，则提示需要编辑
					Utils.getToast(this, "不能添加空白页面，请先编辑！！！");
					return;
				}
				// Thread.sleep(1000);//更新图片时间
				// 回到上一页
				currentPageNum.setText(String.valueOf(currentPage - 1));
				String picPath = myNoteList.get(currentPage - 2).getPicPath();
				if (null != myNoteList.get(currentPage - 2).getNotelistname()) {
					notelistname.setText(myNoteList.get(currentPage - 2).getNotelistname());
				}
				loadMyView(this, Drawable.createFromPath(picPath), false);
				totalPageNum.setText(String.valueOf(myNoteList.size()));
			}

		} catch (Exception e) {
			Log.d(SystemDef.Debug.TAG, TAG + " upPage exception = " + e.getMessage());
			return;
		}
	}

	/*
	 * 下一页
	 */
	private void nextPage() {
		int currentNum = 0;
		try {
			currentNum = Integer.parseInt(currentPageNum.getText().toString());
			if (currentNum >= 10) {
				Utils.getToast(CreateNoteActivity.this, "新增页面<10/10>");
				return;
			}
		} catch (Exception e) {
			Log.d(SystemDef.Debug.TAG, TAG + " nextPage exception = " + e.getMessage());
			return;
		}
		/**
		 * 1、判断总页码值，如果 为“+”，说明当前页为最后一页，并且可以新增记录,新增前需要判断当前画板view状态（是否为空白）
		 * 2、如果总页码值不为"+",则说明可以浏览下一个画板view，浏览到最后一页时需要判断
		 */
		String totalPageText = totalPageNum.getText().toString();
		if ("+".equals(totalPageText)) {
			// 判断当前界面是已经存在还是新编辑的
			if (!myView.isMyViewIsEmptyView() && myView.isMyViewIsEdit()) {// 非空白、编辑，则替换
				// 获取当前图片路径
				String currentImgPath = myNoteList.get(currentNum - 1).getPicPath();
				// 替换
				new SaveImageThread(currentImgPath, getResources().getDrawable(getCurrentBackground()), FILE_REPLACE)
						.start();
				// 设置当前文件名
				if (!"".equals(notelistname.getText().toString())) {
					myNoteList.get(currentNum - 1).setNotelistname(notelistname.getText().toString());
				}
				loadMyView(CreateNoteActivity.this, getResources().getDrawable(getCurrentBackground()), true);
				flushCurrentPage(String.valueOf(currentNum + 1));
				flushTotalPage("+");
				notelistname.setText("");
			} else if (myView.isMyViewIsEmptyView() && myView.isMyViewIsEdit()) {// 空白、编辑，则新增
				if ("".equals(notelistname.getText().toString())) {
					Utils.getToast(this, "请先为当前画板定义文件名");
					notelistname.requestFocus();
					return;
				}
				newSaveFile(notelistname.getText().toString());// 保存当前图片
			} else if (myView.isMyViewIsEmptyView() && !myView.isMyViewIsEdit()) {// 空白、非编辑
				if ("".equals(notelistname.getText().toString())) {
					Utils.getToast(this, "请先为当前画板定义文件名");
					notelistname.requestFocus();
					return;
				}
			} else if (!myView.isMyViewIsEmptyView() && !myView.isMyViewIsEdit()) {// 非空白、非编辑
				notelistname.setText("");// 加载前先清空文件名
				if (myNoteList.size() > currentNum) {
					try {
						// 加载下一张画板
						loadMyView(CreateNoteActivity.this,
								Drawable.createFromPath(myNoteList.get(currentNum).getPicPath()), false);
						if (null != myNoteList.get(currentNum).getNotelistname()) {
							notelistname.setText(myNoteList.get(currentNum).getNotelistname());
						}
						// 刷新页码
						flushCurrentPage(String.valueOf(currentNum + 1));
						if (currentNum + 1 == myNoteList.size()) {
							flushTotalPage("+");
						}
					} catch (Exception e) {

					}
				} else {
					loadMyView(CreateNoteActivity.this, getResources().getDrawable(getCurrentBackground()), true);
					flushCurrentPage(String.valueOf(currentNum + 1));
					flushTotalPage("+");
					notelistname.setText("");
				}
			}

		} else {// 下一页浏览
				// 判断是否为最后一页
			if (myNoteList.size() == currentNum || myNoteList.size() == 0) {
				Utils.getToast(this, "已经是最后一页");
				return;
			}
			if (myView.isMyViewIsEmptyView()) {// 如果为空白画板，提示编辑
				Utils.getToast(this, "不能添加空白页面，请先编辑！！！");
				return;
			}
			// 浏览下一页
			if (myView.isMyViewIsEdit()) {// 如果当前界面编辑过，需要替换
				if ("".equals(notelistname.getText().toString())) {
					Utils.getToast(this, "请先为当前画板定义文件名");
					notelistname.requestFocus();
					return;
				}
				// 获取当前图片路径
				String currentImgPath = myNoteList.get(currentNum - 1).getPicPath();
				// 替换
				new SaveImageThread(currentImgPath, getResources().getDrawable(getCurrentBackground()), FILE_REPLACE)
						.start();
				// 设置当前文件名
				if (!"".equals(notelistname.getText().toString())) {
					myNoteList.get(currentNum - 1).setNotelistname(notelistname.getText().toString());
				}
			}
			notelistname.setText("");// 加载前先清空文件名
			// 加载下一张画板
			loadMyView(CreateNoteActivity.this, Drawable.createFromPath(myNoteList.get(currentNum).getPicPath()), false);
			if (null != myNoteList.get(currentNum).getNotelistname()) {
				notelistname.setText(myNoteList.get(currentNum).getNotelistname());
			}
			// 刷新页码
			flushCurrentPage(String.valueOf(currentNum + 1));
			if (currentNum + 1 == myNoteList.size()) {
				flushTotalPage("+");
			}
		}
	}

	/*
	 * 保存图片
	 */
	private void newSaveFile(String fileName) {
		String saveFilePath = makeDir + fileName + ".png";
		// 保存
		new SaveImageThread(saveFilePath, getResources().getDrawable(getCurrentBackground()), NEW_FILE_SAVE).start();

	}

	/**
	 * 回到上一页时的文件保存
	 */
	private void upPageSaveFile(String fileName) {
		// 保存当前页面画板
		String saveFilePath = makeDir + fileName + ".png";
		// 保存
		new SaveImageThread(saveFilePath, getResources().getDrawable(getCurrentBackground()), UP_PAGE_FILE_SAVE)
				.start();
	}

	// 检测sdcard状态
	private boolean storageStatus() {
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return false;
		}
		return true;
	}

	// 检测sdcar空间
	private boolean storegeSpace() {
		File f = Environment.getExternalStorageDirectory();
		if (null != f) {
			if (f.getFreeSpace() > 1024) {// 1M
				return true;
			}
		}
		return false;
	}

	class SaveImageThread extends Thread {
		private String outFilePath;
		private Drawable drawable;
		private int type;// 操作类型

		public SaveImageThread(String outFilePath, Drawable drawable, int type) {
			this.outFilePath = outFilePath;
			this.drawable = drawable;
			this.type = type;
		}

		@Override
		public void run() {
			if (!storageStatus()) {
				sendFailedMessage("存储卡未正常启动，请检查存储卡状态");
				return;
			}
			if (!storegeSpace()) {
				sendFailedMessage("存储卡空间不足");
				return;
			}
			// 先保存当前图片
			saveCanvasImage(outFilePath, drawable, type);
		}
	}

	/*
	 * 发送异步消息
	 * 
	 * @param msg
	 */
	private void sendMessage(Message msg) {
		myHander.sendMessage(msg);
	}

	/*
	 * 发送错误消息
	 * 
	 * @param errorInfo
	 */
	private void sendFailedMessage(String errorInfo) {
		Message msg = new Message();
		msg.what = MESSAGE_FLUSH_NOTE_PAGE_ERROR;
		Bundle b = new Bundle();
		b.putString("error", errorInfo);
		msg.setData(b);
		sendMessage(msg);
	}

	/*
	 * 设置字体
	 */
	private void setFont() {
		_mPaint.setXfermode(null);
		_mPaint.setAlpha(0xFF);
		if (_mPaint.getMaskFilter() != mEmboss) {
			_mPaint.setMaskFilter(mEmboss);
		} else {
			_mPaint.setMaskFilter(null);
		}
		final SeekBar seekBar = new SeekBar(CreateNoteActivity.this);
		seekBar.setMax(10);// 设置最大刻度
		seekBar.setProgress(paintSize);// 设置当前刻度
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
				System.out.println(" onProgressChanged progress = " + progress);
				paintSize = progress;
			}

			public void onStartTrackingTouch(SeekBar seekBar) {// 开始拖动
			}

			public void onStopTrackingTouch(SeekBar seekBar) {// 结束拖动
			}
		});
		myAlertDialog = (MyDialog) new MyDialog(myContext);
		myAlertDialog.setTitle("选择笔迹粗细");
		myAlertDialog.setView(seekBar);
		myAlertDialog.setButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (_mBitmapPaint != null) {
					_mBitmapPaint.reset();
				}
				_mPaint = new Paint();
				_mPaint.setAntiAlias(true);
				_mPaint.setStyle(Paint.Style.STROKE);
				_mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
				_mPaint.setStrokeCap(Paint.Cap.SQUARE);// 形状
				if (_myColor != 0) {
					_mPaint.setColor(_myColor);
				}
				_mPaint.setStrokeWidth(paintSize);
				_mPaint.setXfermode(null);
				_mCanvas = new Canvas(_mBitmap);
				_mBitmapPaint = new Paint(Paint.DITHER_FLAG);

			}
		});
		myAlertDialog.setButton2("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}

		});
		myAlertDialog.show();

	}

	@Override
	public void colorChanged(int color) {
		// isClear = false;
		_myColor = color;
		_mPaint.setColor(color);
	}

	/**
	 * 颜色选择器dialog
	 */
	public void showColorDialog() {
		// isClear = false;
		new ColorPickerDialog(this, this, _mPaint.getColor()).show();
	}

	/**
	 * 保存图片
	 * 
	 * @param outFilePath
	 *            输入文件
	 * @param drawable
	 *            背景图
	 */
	private void saveCanvasImage(String outFilePath, Drawable drawable, int type) {
		/* 临时画布用来保存图片和笔记 */
		Canvas mCanvasTmp = null;
		Bitmap mBitmapTmp;

		Bitmap bit;
		/* 保存画布 */
		_mCanvas.save(Canvas.ALL_SAVE_FLAG);
		_mCanvas.restore();
		/* 创建临时画布 */
		mBitmapTmp = Bitmap.createBitmap(picAllViewWide, picAllViewHigh, Bitmap.Config.ARGB_8888);
		mCanvasTmp = new Canvas(mBitmapTmp);
		/* 创建要保存的图片mBitmap */
		// bit = BitmapFactory.decodeResource(getResources(),
		// R.drawable.bodymarginthin);
		BitmapDrawable db = (BitmapDrawable) drawable;
		bit = db.getBitmap();
		/* 画图片 */
		mCanvasTmp.drawBitmap(bit, 0, 0, null);
		/* 画笔迹 */
		mCanvasTmp.drawBitmap(_mBitmap, 0, 0, null);
		File myfile = new File(makeDir);
		if (!myfile.exists())
			myfile.mkdir();
		/* 使用临时画布mBitmapTmp生成文件 */
		File f = new File(outFilePath);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			fos.getFD().sync();// 同步
			mBitmapTmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			switch (type) {
				case NEW_FILE_SAVE:
					Message msg = new Message();
					msg.what = MESSAGE_FLUSH_NOTE_PAGE_NEW_FILE_SAVE_OK;
					Bundle b = new Bundle();
					b.putString("path", f.getPath());
					msg.setData(b);
					myHander.sendMessage(msg);// 发送消息
					break;
				case FILE_REPLACE:
					// Message msg1 = new Message();
					// msg1.what = MESSAGE_FLUSH_NOTE_PAGE_FILE_REPLACE_OK;
					// Bundle b1 = new Bundle();
					// b1.putString("path", f.getPath());
					// msg1.setData(b1);
					// myHander.sendMessage(msg1);//发送消息
					break;
				case UP_PAGE_FILE_SAVE:
					// //当前信息保存
					MyNoteList noteList1 = new MyNoteList();
					noteList1.setPicPath(f.getPath());
					String myFileName = f.getName().substring(0, f.getName().lastIndexOf("."));
					noteList1.setNotelistname(myFileName);
					myNoteList.add(noteList1);
					break;
				default:
					break;
			}

		} catch (FileNotFoundException e) {
			Log.d(SystemDef.Debug.TAG, TAG + " saveCanvasImage exception = " + e.getMessage());
			// myHander.sendEmptyMessage(MESSAGE_FLUSH_NOTE_PAGE_ERROR);
			sendFailedMessage("图片处理失败");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
				mBitmapTmp.recycle();// 释放临时bitmap
			} catch (IOException e) {
				// e.printStackTrace();
				Log.d(SystemDef.Debug.TAG, TAG + " saveCanvasImage close exception = " + e.getMessage());
			}
		}
	}

	public boolean copyfile(File originPath, File finalPath) {
		FileInputStream read = null;
		FileOutputStream write = null;
		BufferedInputStream bread = null;
		BufferedOutputStream bwrite = null;
		try {
			read = new FileInputStream(originPath);
			bread = new BufferedInputStream(read);
			write = new FileOutputStream(finalPath);
			bwrite = new BufferedOutputStream(write);
			int index = 0;
			while ((index = bread.read()) != -1) {
				bwrite.write(index);
			}
			return true;
		} catch (IOException ex) {
			return false;
		} finally {
			try {
				bread.close();
				bwrite.close();
			} catch (IOException ex) {
				return false;
			}
		}
	}

	/**
	 * 自定义画板的VIew
	 * 
	 * @author Administrator
	 * 
	 */
	public class MyView extends View {

		private boolean myViewIsEdit;// myview的触摸状态，用户翻页时判断当前页是否编辑状态，默认为false
		/**
		 * 判断当前画板是否为空白页 1、如果加载为已经存在的画板图片，标记不为空白 2、如果为新建画板，并且画板为未编辑状态，则标记为空白
		 */
		private boolean myViewIsEmptyView;

		public boolean isMyViewIsEmptyView() {
			return myViewIsEmptyView;
		}

		public void setMyViewIsEmptyView(boolean myViewIsEmptyView) {
			this.myViewIsEmptyView = myViewIsEmptyView;
		}

		public boolean isMyViewIsEdit() {
			return myViewIsEdit;
		}

		public void setMyViewIsEdit(boolean myViewIsEdit) {
			this.myViewIsEdit = myViewIsEdit;
		}

		public MyView(Context c, Drawable drawable, boolean myViewIsEmptyView) {
			super(c);
			myViewIsEdit = false;
			this.myViewIsEmptyView = myViewIsEmptyView;
			this.setBackgroundDrawable(drawable);
			_mBitmapPaint = new Paint(Paint.DITHER_FLAG);
			_mBitmap = Bitmap.createBitmap(picAllViewWide, picAllViewHigh, Bitmap.Config.ARGB_8888);
			// 保存一次一次绘制出来的图形
			_mCanvas = new Canvas(_mBitmap);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
		}

		protected void onDraw(Canvas mbCanvas) {
			// 将前面已经画过得显示出来
			mbCanvas.drawBitmap(_mBitmap, 0, 0, _mBitmapPaint);
			if (_mPath != null) {
				// 实时的显示
				mbCanvas.drawPath(_mPath, _mPaint);
			}
		}

		private void touch_start(float x, float y) {
			_mPath.moveTo(x, y);
			_mX = x;
			_mY = y;
		}

		private void touch_move(float x, float y) {
			// float dx = Math.abs(x–mX);
			// float dy = Math.abs(mY – y);
			float dx = Math.abs(x - _mX);
			float dy = Math.abs(_mY - y);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				// 从x1,y1到x2,y2画一条贝塞尔曲线，更平滑(直接用mPath.lineTo也是可以的)
				_mPath.quadTo(_mX, _mY, (x + _mX) / 2, (y + _mY) / 2);
				_mX = x;
				_mY = y;
			}
		}

		private void touch_up() {
			_mPath.lineTo(_mX, _mY);
			_mCanvas.drawPath(_mPath, _mPaint);
			// 将一条完整的路径保存下来(相当于入栈操作)
			// savePath.add(dp);
			_mPath = null;// 重新置空
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// 每次down下去重新new一个Path
					_mPath = new Path();
					// 每一次记录的路径对象是不一样的
					dp = new DrawPath();
					dp.path = _mPath;
					dp.paint = _mPaint;
					touch_start(x, y);
					invalidate();
					break;
				case MotionEvent.ACTION_MOVE:
					touch_move(x, y);
					invalidate();
					break;
				case MotionEvent.ACTION_UP:
					if (null != popuWindow && popuWindow.isShowing()) {
						popuWindow.dismiss();
					}
					if (imm.isActive()) {
						imm.hideSoftInputFromWindow(CreateNoteActivity.this.getCurrentFocus().getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
					}
					// 只要有触摸事件，则表示当前view已经被编辑
					this.myViewIsEdit = true;
					touch_up();
					invalidate();
					break;
			}
			return true;
		}

	}

	@Override
	public void onBackPressed() {
		if (null != popuWindow && popuWindow.isShowing()) {
			popuWindow.dismiss();
			return;
		}
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myNoteService != null) {
			if (null != myNoteService.getDh()) {
				myNoteService.getDh().close();
			}
		}
	}

}
