package com.quku.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.quku.R;

public class MoreActivity extends Activity implements OnClickListener {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more);
		TextView mTvTitle = (TextView) findViewById(R.id.title_with_back_title_btn_mid);
		mTvTitle.setText("乐谱系统设置");
		findViewById(R.id.title_with_back_title_btn_left).setOnClickListener(
				this);
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
}
