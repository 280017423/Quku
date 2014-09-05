package com.quku.adapter;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quku.R;

public class ShelfAdapter extends BaseAdapter {

	private Context mContext;
	private List<File> mModels;
	private onClickListener mListener;

	public ShelfAdapter(Context context, List<File> files,
			onClickListener listener) {
		mContext = context;
		mModels = files;
		mListener = listener;
	}

	@Override
	public int getCount() {

		if (null != mModels) {
			int size = mModels.size();
			if (0 == size % 3) {
				return size / 3;
			}
			return size / 3 + 1;
		} else {
			return 3;
		}

	}

	@Override
	public Object getItem(int position) {

		return position;
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		File file1 = null;
		File file2 = null;
		File file3 = null;
		if (position * 3 < mModels.size()) {
			file1 = mModels.get(position * 3);
		}
		if (position * 3 + 1 < mModels.size()) {
			file2 = mModels.get(position * 3 + 1);
		}
		if (position * 3 + 2 < mModels.size()) {
			file3 = mModels.get(position * 3 + 2);
		}
		viewHode view = new viewHode();
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.listview_item_mycompose, null);
			view.textView_1 = (TextView) convertView
					.findViewById(R.id.tvCompose1);
			view.textView_2 = (TextView) convertView
					.findViewById(R.id.tvCompose2);
			view.textView_3 = (TextView) convertView
					.findViewById(R.id.tvCompose3);
			convertView.setTag(view);
		} else {
			view = (viewHode) convertView.getTag();
		}
		setTextView(file1, view.textView_1);
		setTextView(file2, view.textView_2);
		setTextView(file3, view.textView_3);
		return convertView;
	}

	private void setTextView(File file1, TextView view) {
		Drawable drawableDir = mContext.getResources().getDrawable(
				R.drawable.compose_dir);
		Drawable drawableFile = mContext.getResources().getDrawable(
				R.drawable.compose);
		if (null != file1) {
			view.setVisibility(View.VISIBLE);
			if (file1.isDirectory()) {
				view.setCompoundDrawablesWithIntrinsicBounds(null, drawableDir,
						null, null);
			} else {
				view.setCompoundDrawablesWithIntrinsicBounds(null,
						drawableFile, null, null);
			}
			final File temp = file1;
			view.setText(temp.getName());
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.onClick(v, temp);
				}
			});
		} else {
			view.setVisibility(View.INVISIBLE);
		}
	}

	class viewHode {
		TextView textView_1;
		TextView textView_2;
		TextView textView_3;
	}

	public interface onClickListener {
		public void onClick(View v, File file);
	}

}