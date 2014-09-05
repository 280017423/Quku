package com.quku.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class AutoSearchAdapter<T> extends BaseAdapter implements Filterable {

	private List<T> mObjects;
	private List<T> mObjects2;
	private final Object mLock = new Object();
	private int mResource;
	private int mDropDownResource;
	private int mFieldId = 0;
	private boolean mNotifyOnChange = true;
	private Context mContext;
	private ArrayList<T> mOriginalValues;
	private ArrayFilter mFilter;
	private LayoutInflater mInflater;

	public AutoSearchAdapter(Context context, int textViewResourceId) {

		init(context, textViewResourceId, 0, new ArrayList<T>(),
				new ArrayList<T>());

	}

	public AutoSearchAdapter(Context context, int resource,
			int textViewResourceId) {

		init(context, resource, textViewResourceId, new ArrayList<T>(),
				new ArrayList<T>());

	}

	public AutoSearchAdapter(Context context, int textViewResourceId,
			T[] objects, T[] objects2) {

		init(context, textViewResourceId, 0, Arrays.asList(objects),
				Arrays.asList(objects2));

	}

	public AutoSearchAdapter(Context context, int resource,
			int textViewResourceId, T[] objects, T[] objects2) {

		init(context, resource, textViewResourceId, Arrays.asList(objects),
				Arrays.asList(objects2));

	}

	public AutoSearchAdapter(Context context, int textViewResourceId,
			List<T> objects, List<T> objects2) {

		init(context, textViewResourceId, 0, objects, objects2);

	}

	public AutoSearchAdapter(Context context, int resource,
			int textViewResourceId, List<T> objects, List<T> objects2) {

		init(context, resource, textViewResourceId, objects, objects2);

	}

	public void add(T object) {

		if (mOriginalValues != null) {

			synchronized (mLock) {

				mOriginalValues.add(object);

				if (mNotifyOnChange)
					notifyDataSetChanged();

			}

		} else {

			mObjects.add(object);

			if (mNotifyOnChange)
				notifyDataSetChanged();

		}

	}

	public void insert(T object, int index) {

		if (mOriginalValues != null) {

			synchronized (mLock) {

				mOriginalValues.add(index, object);

				if (mNotifyOnChange)
					notifyDataSetChanged();

			}

		} else {

			mObjects.add(index, object);

			if (mNotifyOnChange)
				notifyDataSetChanged();

		}

	}

	public void remove(T object) {

		if (mOriginalValues != null) {

			synchronized (mLock) {

				mOriginalValues.remove(object);

			}

		} else {

			mObjects.remove(object);

		}

		if (mNotifyOnChange)
			notifyDataSetChanged();

	}

	public void clear() {

		if (mOriginalValues != null) {

			synchronized (mLock) {

				mOriginalValues.clear();

			}

		} else {

			mObjects.clear();

		}

		if (mNotifyOnChange)
			notifyDataSetChanged();

	}

	public void sort(Comparator<? super T> comparator) {

		Collections.sort(mObjects, comparator);

		if (mNotifyOnChange)
			notifyDataSetChanged();

	}

	@Override
	public void notifyDataSetChanged() {

		super.notifyDataSetChanged();

		mNotifyOnChange = true;

	}

	public void setNotifyOnChange(boolean notifyOnChange) {

		mNotifyOnChange = notifyOnChange;

	}

	private void init(Context context, int resource, int textViewResourceId,
			List<T> objects, List<T> objects2) {

		mContext = context;

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mResource = mDropDownResource = resource;

		mObjects = objects;

		mObjects2 = objects2;

		mFieldId = textViewResourceId;

	}

	public Context getContext() {

		return mContext;

	}

	public int getCount() {

		return mObjects.size();

	}

	public T getItem(int position) {

		return mObjects.get(position);

	}

	public int getPosition(T item) {

		return mObjects.indexOf(item);

	}

	public long getItemId(int position) {

		return position;

	}

	public View getView(int position, View convertView, ViewGroup parent) {

		return createViewFromResource(position, convertView, parent, mResource);

	}

	private View createViewFromResource(int position, View convertView,
			ViewGroup parent, int resource) {
		View view;
		TextView text;
		if (convertView == null) {
			view = mInflater.inflate(resource, parent, false);
		} else {
			view = convertView;
		}
		try {

			if (mFieldId == 0) {

				// If no custom field is assigned, assume the whole resource is
				// a TextView

				text = (TextView) view;

			} else {

				// Otherwise, find the TextView field within the layout

				text = (TextView) view.findViewById(mFieldId);

			}

		} catch (ClassCastException e) {

			Log.e("ArrayAdapter",
					"You must supply a resource ID for a TextView");

			throw new IllegalStateException(

			"ArrayAdapter requires the resource ID to be a TextView", e);

		}

		text.setText(getItem(position).toString());

		return view;

	}

	public void setDropDownViewResource(int resource) {

		this.mDropDownResource = resource;

	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {

		return createViewFromResource(position, convertView, parent,
				mDropDownResource);

	}

	public static ArrayAdapter<CharSequence> createFromResource(
			Context context,

			int textArrayResId, int textViewResId) {

		CharSequence[] strings = context.getResources().getTextArray(
				textArrayResId);

		return new ArrayAdapter<CharSequence>(context, textViewResId, strings);

	}

	public Filter getFilter() {

		if (mFilter == null) {

			mFilter = new ArrayFilter();

		}

		return mFilter;

	}

	private class ArrayFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence prefix) {

			FilterResults results = new FilterResults();

			if (mOriginalValues == null) {

				synchronized (mLock) {

					mOriginalValues = new ArrayList<T>(mObjects);

				}

			}

			if (prefix == null || prefix.length() == 0) {

				synchronized (mLock) {

					ArrayList<T> list = new ArrayList<T>(mOriginalValues);

					results.values = list;

					results.count = list.size();

				}

			} else {

				String prefixString = prefix.toString().toLowerCase();

				final ArrayList<T> values = mOriginalValues;

				final int count = values.size();

				final ArrayList<T> newValues = new ArrayList<T>(count);

				for (int i = 0; i < count; i++) {

					final T value = values.get(i);

					final String valueText = value.toString().toLowerCase();

					final T value2 = mObjects2.get(i);

					final String valueText2 = value2.toString().toLowerCase();

					// 查找拼音
					// if (valueText2.startsWith(prefixString)) {
					if (valueText2.contains(prefixString)) {
						newValues.add(value);
						// 查找汉字
						// } else if (valueText.startsWith(prefixString)) {
					} else if (valueText.contains(prefixString)) {
						newValues.add(value);
					} else {
						// 添加汉字关联
						final String[] words = valueText.split(" ");
						final int wordCount = words.length;
						for (int k = 0; k < wordCount; k++) {
							if (words[k].startsWith(prefixString)) {
								newValues.add(value);
								break;
							}
						}
						// 添加拼音关联汉字
						final String[] words2 = valueText2.split(" ");

						final int wordCount2 = words2.length;

						for (int k = 0; k < wordCount2; k++) {

							if (words2[k].startsWith(prefixString)) {

								newValues.add(value);

								break;

							}
						}
					}
				}
				if (newValues.size() == 0) {// 没有搜索到，添加提示
					ArrayList<T> _newValues = new ArrayList<T>(1);
					_newValues.add((T) "无当前乐谱，查找请上www.quku.so");
					results.values = _newValues;
					results.count = _newValues.size();
				} else {
					results.values = newValues;
					results.count = newValues.size();
				}
			}
			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {

			mObjects = (List<T>) results.values;

			if (results.count > 0) {

				notifyDataSetChanged();

			} else {

				notifyDataSetInvalidated();

			}

		}

	}

}
