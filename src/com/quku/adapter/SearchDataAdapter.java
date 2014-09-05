package com.quku.adapter;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quku.R;

public class SearchDataAdapter extends ArrayAdapter<String> {

	private int resource;

	public SearchDataAdapter(Context context, int resourceId, List<String> objects) {
		super(context, resourceId, objects);
		resource = resourceId;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout itemLayout;
		// 获取数据
		String value = (String) getItem(position);
		// 系统显示列表时，首先实例化一个适配器（这里将实例化自定义的适配器）。
		// 当手动完成适配时，必须手动映射数据，这需要重写getView（）方法。
		// 系统在绘制列表的每一行的时候将调用此方法。
		// getView()有三个参数，
		// position表示将显示的是第几行，
		// covertView是从布局文件中inflate来的布局。
		// 我们用LayoutInflater的方法将定义好的image_item.xml文件提取成View实例用来显示。
		// 然后将xml文件中的各个组件实例化（简单的findViewById()方法）。
		// 这样便可以将数据对应到各个组件上了。
		//
		if (convertView == null) {
			itemLayout = new LinearLayout(getContext());
			// 看一下android文档中关于LayoutInflater的定义吧
			// This class is used to instantiate layout XML file into its
			// corresponding View objects.
			// It is never be used directly -- use getLayoutInflater() or
			// getSystemService(String)
			// to retrieve a standard LayoutInflater instance that is already
			// hooked up to the current
			// context and correctly configured for the device you are running
			// on. . For example:
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
			vi.inflate(resource, itemLayout, true);
			// 这个每次select images时都被调用
			Log.d("Adapter", "convertView is null now");
		} else {
			// 很奇怪基本没被调用过
			itemLayout = (LinearLayout) convertView;
			Log.d("Adapter", "convertView is not null now");
		}

		// 填充自定义数据
		TextView textView1 = (TextView) itemLayout.findViewById(R.id.tvname);
		// TextView textView2 = (TextView) itemLayout.findViewById(R.id.tvtype);
		textView1.setText(value);
		// textView2.setText(value);
		return itemLayout;
	}
}
