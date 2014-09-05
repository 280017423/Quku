package com.quku;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

/**
 * 音乐乐谱使用说明类
 * @author Administrator
 *
 */
public class UserHelpActivity extends Activity {

	Button enter_back;
	Button enter_next_page;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().getDecorView().setSystemUiVisibility(4);  //SYSTEM_UI_FLAG_SHOW_FULLSCREEN = 0x00000004;//隐藏状态栏
		setContentView(R.layout.userhelp);
		enter_back = (Button)findViewById(R.id.enter_back);
		enter_next_page  = (Button)findViewById(R.id.enter_back_next_page);
		enter_next_page.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(UserHelpActivity.this, UserHelpNextPagActivity.class);
				startActivity(intent);
				//设置切换动画，从右边进入，左边退出               
//				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);  
//				UserHelpActivity.this.finish();
			}
		});
		enter_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent intent = new Intent();
//				intent.setClass(UserHelpActivity.this, DanteFirstPage.class);
//				startActivity(intent);
				//设置切换动画，从右边进入，左边退出               
				UserHelpActivity.this.finish();
//				overridePendingTransition(R.anim.out_to_left, R.anim.in_from_right);  
			}
		});
	}
}
