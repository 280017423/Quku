package com.quku.activity;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.quku.R;
import com.quku.adapter.ShelfAdapter;
import com.quku.adapter.ShelfAdapter.onClickListener;

public class PDFComposeFragment extends BaseFragment implements
		android.view.View.OnClickListener, OnCheckedChangeListener,
		onClickListener {

	private ListView mLvCompose;
	private Button mLlHomeLayout;
	private Button mLlLastLayout;
	private RadioGroup mRgComposeType;
	private EditText mEtAddCompose;
	private ShelfAdapter mShelfAdapter;
	private List<File> mFileList;
	public File mRootFile = new File(Environment.getExternalStorageDirectory()
			.getPath() + "/tflash/musicpdf/");
	public File mCurrentDir = mRootFile;
	public File mChooseDir = mRootFile;

	public View onCreateView(LayoutInflater paramLayoutInflater,
			ViewGroup paramViewGroup, Bundle paramBundle) {
		View localView = paramLayoutInflater.inflate(
				R.layout.fragment_image_compose, null);
		initView(localView);
		setRbData(mRootFile);
		return localView;
	}

	public void initView(View paramView) {
		mLvCompose = ((ListView) paramView.findViewById(R.id.lvCompose));
		mLlHomeLayout = ((Button) paramView.findViewById(R.id.btn_home));
		mLlLastLayout = ((Button) paramView.findViewById(R.id.btn_back));
		mFileList = new ArrayList<File>();
		mShelfAdapter = new ShelfAdapter(getActivity(), mFileList, this);
		mRgComposeType = ((RadioGroup) paramView
				.findViewById(R.id.rgComposeType));
		mLvCompose.setAdapter(mShelfAdapter);
		mRgComposeType.setOnCheckedChangeListener(this);
		mLlHomeLayout.setOnClickListener(this);
		mLlLastLayout.setOnClickListener(this);
	}

	private void setRbData(File path) {
		File[] arrayOfFile = path.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		if (arrayOfFile != null) {
			Arrays.sort(arrayOfFile, new Comparator<File>() {

				@Override
				public int compare(File paramFile1, File paramFile2) {
					return (int) (paramFile1.getName().compareTo(paramFile2
							.getName()));
				}

			});
			mRgComposeType.removeAllViews();
			for (int i = 0; i < arrayOfFile.length; i++) {
				addRadioButton(arrayOfFile[i]);
			}
			if (mRgComposeType.getChildCount() > 0
					&& this.mRgComposeType.getChildAt(0) instanceof RadioButton) {
				((RadioButton) mRgComposeType.getChildAt(0)).setChecked(true);
			}
		}
		createAddIBtn();
	}

	public void setListViewData(File paramFile) {
		if (paramFile.isDirectory()) {
			mFileList.clear();
			File[] files = paramFile.listFiles(new FileFilter() {
				public boolean accept(File file) {
					boolean isOk = false;
					if (file.isDirectory()) {
						isOk = true;
					}
					String str = file.getPath();
					int i = str.lastIndexOf('.');
					if (i != -1) {
						str = str.substring(i + 1, str.length());
						if (str.equalsIgnoreCase("PDF")) {
							isOk = true;
						}
					}
					return isOk;
				}
			});
			if (null != files) {
				mFileList.addAll(Arrays.asList(files));
			}
			mShelfAdapter.notifyDataSetChanged();
		}
	}

	public RadioButton addRadioButton(File paramFile) {
		return addRadioButton(paramFile.getName(),
				mRgComposeType.getChildCount() - 1);
	}

	public RadioButton addRadioButton(String paramString, int paramInt) {
		RadioButton localRadioButton = (RadioButton) View.inflate(
				getActivity(), R.layout.rb_composetype, null);
		localRadioButton.setText(paramString);
		this.mRgComposeType.addView(localRadioButton, paramInt);
		RadioGroup.LayoutParams localLayoutParams = (RadioGroup.LayoutParams) localRadioButton
				.getLayoutParams();
		localLayoutParams.setMargins(0, 25, 0, 0);
		localLayoutParams.width = -1;
		localRadioButton.setTag(paramString);
		localRadioButton.setLayoutParams(localLayoutParams);
		return localRadioButton;
	}

	public void createAddIBtn() {
		ImageButton localImageButton = new ImageButton(getActivity());
		localImageButton.setBackgroundResource(0);
		localImageButton.setImageResource(R.drawable.composetype_add);
		this.mRgComposeType.addView(localImageButton);
		RadioGroup.LayoutParams localLayoutParams = (RadioGroup.LayoutParams) localImageButton
				.getLayoutParams();
		localLayoutParams.setMargins(0, 25, 0, 0);
		localImageButton.setLayoutParams(localLayoutParams);
		localImageButton
				.setOnClickListener(new android.view.View.OnClickListener() {

					@Override
					public void onClick(View v) {
						showAddDialog();
					}
				});
	}

	public void showAddDialog() {
		AlertDialog.Builder localBuilder = new AlertDialog.Builder(getActivity());
		View localView = View.inflate(getActivity(),
				R.layout.dialog_new_dir, null);
		mEtAddCompose = ((EditText) localView.findViewById(R.id.etAddCompose));
		mEtAddCompose.setText("");
		localBuilder.setTitle("添加乐谱").setView(localView)
				.setPositiveButton("确定", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mkDir();
					}

				}).setNegativeButton("取消", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}

				}).create().show();
	}

	private void mkDir() {
		String str = this.mEtAddCompose.getText().toString().trim();
		if (null == str || "".equals(str)) {
			return;
		}
		File localFile = new File(mCurrentDir.getPath() + "/" + str);
		if (!localFile.exists()) {
			localFile.mkdir();
			addRadioButton(localFile);
		} else {
			Toast.makeText(getActivity(), "目录已存在", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			lastStep();
			break;
		case R.id.btn_home:
			getActivity().finish();
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		RadioButton rButton = (RadioButton) group.findViewById(checkedId);
		String path = (String) rButton.getTag();
		Log.d("aaa", "当前的一级目录是：" + mCurrentDir.getPath());
		mChooseDir = new File(mCurrentDir.getPath() + "/" + path);
		Log.d("aaa", "当前的二级目录是：" + mCurrentDir.getPath() + "/" + path);
		setListViewData(mChooseDir);
	}

	public void lastStep() {
		if (mRootFile.getPath().equals(mCurrentDir.getPath())) {
			Toast.makeText(getActivity(), "已经是根目录", Toast.LENGTH_LONG).show();
			return;
		}
		mCurrentDir = mCurrentDir.getParentFile();
		mChooseDir = mChooseDir.getParentFile();
		setRbData(mCurrentDir);
	}

	@Override
	public void onClick(View v, File file) {
		if (!file.exists()) {
			return;
		}
		if (file.isFile()) {
			try {
				Intent localIntent = new Intent("android.intent.action.VIEW");
				localIntent.setDataAndType(Uri.fromFile(file),
						"application/pdf");
				PDFComposeFragment.this.startActivity(localIntent);
				return;
			} catch (Exception localException) {
				while (true)
					Toast.makeText(getActivity(), "没有软件可以打开该文件",
							Toast.LENGTH_LONG).show();
			}
		} else {
			mCurrentDir = mChooseDir;
			mChooseDir = file;
			setRbData(mCurrentDir);

		}

	}
}