package com.quku.activity;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.quku.R;
import com.quku.Utils.HanziToPinyin;
import com.quku.Utils.SystemDef;
import com.quku.Utils.Utils;
import com.quku.adapter.AutoSearchAdapter;
import com.quku.adapter.MyComposeViewPagerAdapter;
import com.quku.entity.MyFileInfo;

public class MyMusicSheetActivity extends BaseActivity implements OnClickListener {

	private static final String TAG = "MyMusicSheetActivity";
	private static final int MESSAGE_SEARCHDATA_LOAD_FINISH = 1;// 加载完毕
	private static final int MESSAGE_SEARCHDATA_LOAD_FLUSH = 2;// 加载刷新
	int F = LayoutParams.FILL_PARENT;
	int W = LayoutParams.WRAP_CONTENT;
	private static final int BUTTON_HEIGHT = 55;
	private static final int BUTTON_WIDTH = 150;
	/* 图片根目录地址 */
	public String Url2 = Environment.getExternalStorageDirectory().getPath() + "/tflash/musicpaper/";
	/* 作为文件地址缓存 */
	public String UrlTemp;
	public String UrlFirMenu;
	public String UrlFourMenu;
	/* 二级菜单地址 */
	public String UrlSecMenu;
	/* 三级菜单地址 */
	public String UrlThdMenu;
	/* 最终视频地址 */
	public String picName;
	public String UrlReturn = "";

	public LinearLayout LY;
	public LinearLayout LY_UP;
	public LinearLayout LY_DOWN;
	public LinearLayout LY_DOWN_RIGHT;
	public LinearLayout LY_DOWN_RIGHT2;
	public LinearLayout LY_DOWN_RIGHT3;
	public LinearLayout LY_DOWN_RIGHT4;
	public LinearLayout LY_DOWN_LEFT;
	public LinearLayout LY_DOWN_LEFT_DOWN;

	public boolean fourclick = false;
	// 提供数据的字符串数组
	private String[] nations = { "china:123", "Chile:123", "Canada:123", "Australia:123" };
	// private List<String> searchList;//搜索文件数据
	List<String> chineseList = new ArrayList<String>();
	List<String> pinyinList = new ArrayList<String>();
	private Map<String, String> fileMap = new HashMap<String, String>();;
	private AutoSearchAdapter cAdapter;
	private Thread loadSearchDataThred;
	private LinearLayout mLlBack;
	private ViewPager mViewPager;
	private AutoCompleteTextView mAutoCompleteTextView;
	private List<File> mRootDirsList;
	private MyComposeViewPagerAdapter mViewPagerAdapter;
	private List<Fragment> mListFragment;
	private ImageComposeFragment imageComposeFragment;
	private PDFComposeFragment pdfComposeFragment;
	private int tablineWidth;
	private ImageView mIbTabLine;
	private int mCurrentPager;
	private int mCurrentIndex;
	private AnimationListener animationListener = new Animation.AnimationListener() {
		public void onAnimationEnd(Animation paramAnimation) {
			LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) mIbTabLine.getLayoutParams();
			localLayoutParams.setMargins(mCurrentIndex * tablineWidth, 0, 0, 0);
			mIbTabLine.setLayoutParams(localLayoutParams);
		}

		public void onAnimationRepeat(Animation paramAnimation) {
		}

		public void onAnimationStart(Animation paramAnimation) {
		}
	};
	private Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MESSAGE_SEARCHDATA_LOAD_FINISH:
				case MESSAGE_SEARCHDATA_LOAD_FLUSH:
					cAdapter.notifyDataSetChanged();
					break;
			}
			super.handleMessage(msg);
		}

	};

	OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
		public void onPageScrollStateChanged(int paramInt) {
			Log.e("page", paramInt + "  onPageScrollStateChanged");
		}

		public void onPageScrolled(int paramInt1, float paramFloat, int paramInt2) {
			Log.e("page", paramInt1 + "  onPageScrolled" + "  " + paramFloat + "==" + paramInt2);
		}

		public void onPageSelected(int paramInt) {
			mCurrentPager = paramInt;
			Log.e("page", paramInt + "  onPageSelected");
			TranslateAnimation mTranslateAnim = new TranslateAnimation(mCurrentIndex * tablineWidth, paramInt
					* mIbTabLine.getWidth(), 0.0F, 0.0F);
			mTranslateAnim.setAnimationListener(animationListener);
			mIbTabLine.startAnimation(mTranslateAnim);
			mCurrentIndex = paramInt;
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_music_sheet);
		initVariables();
		initViews();
		setListener();
		getRootDirs();
		if (null == loadSearchDataThred) {
			loadSearchDataThred = new Thread(new LoadSearchData());
		}
		if (!loadSearchDataThred.isAlive()) {
			loadSearchDataThred.start();
		}

	}

	private void setListener() {
		mLlBack.setOnClickListener(this);
		mAutoCompleteTextView.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				return false;
			}
		});
		mAutoCompleteTextView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String fileName = mAutoCompleteTextView.getText().toString();
				/**
				 * 判断是文件还是文件夹 1、是文文件，直接获取文件信息 2、是文件夹，则获取文件夹下面的第一个文件
				 */
				String filePath = fileMap.get(fileName);
				if (null != filePath && !"".equals(filePath)) {
					MyFileInfo myi = getFileInfoIntent(fileName, filePath);
					if (null != myi) {
						Intent intent = new Intent();
						intent.setClass(MyMusicSheetActivity.this, PictureBrowers.class);
						intent.putExtra("nameIndex", myi.getFileIndex());
						intent.putExtra("filename", myi.getFilePath());
						MyMusicSheetActivity.this.startActivity(intent);
						return;
					}
				}
				// 搜索不到内容时，默认跳转官方下载乐谱文件
				else {
					Intent intent = new Intent();
					intent.setAction("android.intent.action.VIEW");
					Uri content_url = Uri.parse("http://www.quku.so");
					intent.setData(content_url);
					intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
					startActivity(intent);
					mAutoCompleteTextView.setText("");
				}
			}
		});
		mViewPager.setOnPageChangeListener(onPageChangeListener);
	}

	private void initViews() {
		mIbTabLine = ((ImageView) findViewById(R.id.ivTabline));
		mLlBack = (LinearLayout) findViewById(R.id.title_with_back_title_btn_left);
		mViewPager = ((ViewPager) findViewById(R.id.viewPager));
		mAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.act_search);
		mAutoCompleteTextView.setAdapter(cAdapter);
		mViewPager.setAdapter(mViewPagerAdapter);

		LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) this.mIbTabLine.getLayoutParams();
		localLayoutParams.width = this.tablineWidth;
		this.mIbTabLine.setLayoutParams(localLayoutParams);
	}

	private void initVariables() {
		mListFragment = new ArrayList<Fragment>();
		imageComposeFragment = new ImageComposeFragment();
		pdfComposeFragment = new PDFComposeFragment();
		mListFragment.add(imageComposeFragment);
		mListFragment.add(pdfComposeFragment);
		tablineWidth = (getWindowManager().getDefaultDisplay().getWidth() / 2);
		mRootDirsList = new ArrayList<File>();
		cAdapter = new AutoSearchAdapter<String>(this, R.layout.search_item, R.id.tvname, chineseList, pinyinList);
		mViewPagerAdapter = new MyComposeViewPagerAdapter(getSupportFragmentManager(), mListFragment);
	}

	private void getRootDirs() {
		mRootDirsList = getRootDirs(new File(Url2));
	}

	public List<File> getRootDirs(File file) {
		File[] files = file.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if (file.isDirectory() && !file.getName().contains(".nomedia")) {
					return true;
				} else {
					return false;
				}
			}
		});
		return Arrays.asList(files);
	}

	/* 使用CreateButton函数自动创建按钮 */
	public Button CreateButton(final String name) {
		String realName = "";
		if (name.contains(".jpg") || name.contains(".png")) {
			int index = name.lastIndexOf(".");
			realName = name.substring(0, index);
		}
		final Button Result = new Button(this);
		Result.setText(realName);
		// Result.setText(name);
		Result.setTextSize(15);
		Result.setWidth(BUTTON_WIDTH);
		Result.setHeight(BUTTON_HEIGHT);
		Result.setBackgroundResource(R.drawable.buttonbg4);
		Result.setOnClickListener(new Button.OnClickListener() {
			/* 启动图片浏览的ACTIVITY并且把当前文件的名称传过去 */
			public void onClick(View v) {
				if (fourclick) {
					File myFile = new File(UrlFourMenu + name);
					if (myFile.exists() && myFile.isFile()) {
						String filePath = myFile.getPath();
						MyFileInfo myi = getFileInfoIntent(name, filePath);
						if (null != myi) {
							Intent intent = new Intent();
							intent.setClass(MyMusicSheetActivity.this, PictureBrowers.class);
							intent.putExtra("nameIndex", myi.getFileIndex());
							intent.putExtra("filename", myi.getFilePath());
							MyMusicSheetActivity.this.startActivity(intent);
							return;
						}
					}

				} else {
					picName = UrlThdMenu + name;
					String nameIndex = "";
					// 根据当前文件名判断文件当前列表索引
					try {
						// 获取文件名
						if (name.contains("-")) {// 包含"-",将截取文件名中的位置索引
							int tempIndex = name.lastIndexOf(".");
							nameIndex = name.substring(0, tempIndex);
							// 得到文件名中数字
							nameIndex = nameIndex.substring(nameIndex.lastIndexOf("-") + 1, nameIndex.length());
						} else {// 不包含“-”，直接在目录中获取文件的索引号
							nameIndex = String.valueOf(getFileIndex(picName, name));
						}
					} catch (Exception e) {
						nameIndex = "1";// 如果异常，直接赋值为1
					}
					Intent intent = new Intent();
					intent.setClass(MyMusicSheetActivity.this, PictureBrowers.class);
					intent.putExtra("filename", picName);
					intent.putExtra("nameIndex", nameIndex);
					startActivity(intent);
					// finish();
					// }
				}
			}
		});
		return Result;
	}

	private int getFileIndex(String path, String name) {
		File file = new File(path);
		int index = 1;
		if (null != file && file.exists() && file.getParentFile().exists()) {// 当前文件存在，并且文件父类存在
			try {
				ArrayList<String> fileList = new ArrayList<String>();
				for (File _file : file.getParentFile().listFiles()) {
					if (_file.exists() && _file.isFile()) {
						fileList.add(_file.getName());
					}
				}
				if (fileList.size() > 0) {
					Collections.sort(fileList);
					for (String fileName : fileList) {
						if (fileName.equals(name)) {
							return index;
						}
						index++;
					}
				}
			} catch (Exception e) {
				Log.d(SystemDef.Debug.TAG, TAG + "getFileIndex exception = " + e.getMessage());
				return 1;
			}
		}
		return index;
	}

	class LoadSearchData implements Runnable {

		@Override
		public void run() {
			try {
				Thread.sleep(1000);// 延时1秒钟加载
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (null == chineseList | chineseList.size() == 0) {
				explorerDir(Url2);
				myHandler.sendEmptyMessage(MESSAGE_SEARCHDATA_LOAD_FINISH);
			}
		}

	}

	/**
	 * 递归遍历目录文件
	 * 
	 * @param source
	 */
	private void explorerDir(String source) {
		File fileSource = new File(source);
		/**
		 * 1.源目录是否存在 2.获取目录下所有文件及目录
		 */
		if (fileSource.exists() && fileSource.isDirectory()) {// 源目录存在
			File[] files = fileSource.listFiles();// 获取目录下所有文件和目录
			for (File file1 : files) {// 遍历file数组,获取下面所有文件和目录
				String sourceFilePath = file1.getPath();// 源文件的路径
				if (file1.isFile() && (file1.getName().endsWith(".jpg") || file1.getName().endsWith(".png"))) {// 是文件
					String fileName = file1.getName().substring(0, file1.getName().lastIndexOf("."));
					chineseList.add(fileName);
					pinyinList.add(HanziToPinyin.getInstance().getPinYin(fileName));
					fileMap.put(fileName, sourceFilePath);
					String dirName = file1.getParentFile().getName();
					// 加载文件上级的目录文件夹
					if (!chineseList.contains(dirName) && !fileMap.containsKey(dirName)) {
						chineseList.add(dirName);
						pinyinList.add(HanziToPinyin.getInstance().getPinYin(dirName));
						fileMap.put(dirName, file1.getParentFile().getPath());
					}

				} else {// 是目录
					for (int i = 1000; i < 10000; i += 1000) {
						if (chineseList.size() % i == 0) {
							myHandler.sendEmptyMessage(MESSAGE_SEARCHDATA_LOAD_FLUSH);
						}
					}
					explorerDir(sourceFilePath);// 递归方法(调用自身)
				}
			}
		}
	}

	public MyFileInfo getFileInfoIntent(String fileName, String filePath) {
		String subName = "1";
		/**
		 * 判断是文件还是文件夹 1、是文文件，直接获取文件信息 2、是文件夹，则获取文件夹下面的第一个文件
		 */
		File _myFile = new File(filePath);
		if (null == _myFile || !_myFile.exists()) {
			Utils.getToast(MyMusicSheetActivity.this, "当前文件已不存在，请重新选择");
			return null;
		}
		try {

			if (_myFile.isFile()) {// 是文件
				if (null != fileName && !"".equals(fileName)) {
					if (fileName.contains("-")) {// 包含“-”，根据文件名获取文件位置索引
						// 判断是否包含"."
						if (fileName.contains(".")) {// 先截取后缀名
							fileName = fileName.substring(0, fileName.lastIndexOf("."));
						}
						subName = fileName.substring(fileName.lastIndexOf("-") + 1, fileName.length());
					} else {// 不包含"-",将根据文件名获取文件在目录中的位置
						subName = String.valueOf(getFileIndex(filePath, _myFile.getName()));
					}
				}
			}
		} catch (Exception e) {
			subName = "1";
		}
		if (_myFile.isDirectory()) {// 是目录
			if (_myFile.listFiles().length == 0) {
				Utils.getToast(MyMusicSheetActivity.this, "当前目录下不存在乐谱文件，请重新选择");
				return null;
			}
			int i = 1;
			boolean flag = false;
			for (File _file : _myFile.listFiles()) {
				if (_file.isFile()
						&& (_file.getName().contains(".jpg") || _file.getName().contains(".png")
								&& !_file.getName().endsWith("_origin.jpg"))) {
					subName = String.valueOf(i);
					filePath = _file.getPath();
					flag = true;
					break;
				}
				i++;
			}
			if (!flag) {
				Utils.getToast(MyMusicSheetActivity.this, "当前目录下不存在乐谱文件，请重新选择");
				return null;
			}
		}
		MyFileInfo fileInfo = new MyFileInfo(subName, filePath);
		return fileInfo;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
			Bundle bunde = data.getExtras();
			UrlReturn = bunde.getString("UrlReturn");
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void GetVideoDeleteList(final LinkedList<String> NameList, File file) {
		file.listFiles(new FileFilter() {
			public boolean accept(File file) {
				NameList.add(file.getName());
				return true;
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.title_with_back_title_btn_left:
				finish();
				break;

			default:
				break;
		}
	}

	@Override
	protected void onDestroy() {
		if (null != LY)
			LY.removeAllViews();
		if (null != chineseList)
			chineseList.clear();
		if (null != pinyinList)
			pinyinList.clear();
		if (null != fileMap)
			fileMap.clear();
		if (!loadSearchDataThred.isInterrupted()) {
			loadSearchDataThred.interrupt();
		}
		super.onDestroy();
	}

}