package com.quku;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

/**
 * 音乐乐谱使用说明下一页处理类
 * @author Administrator
 */
public class UserHelpNextPagActivity extends Activity {

	Button enter_back;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().getDecorView().setSystemUiVisibility(4);  //SYSTEM_UI_FLAG_SHOW_FULLSCREEN = 0x00000004;//隐藏状态栏
		setContentView(R.layout.usernextpaghelp);
		enter_back = (Button)findViewById(R.id.enter_back);
		enter_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent intent = new Intent();
//				intent.setClass(UserHelpNextPagActivity.this, UserHelpActivity.class);
//				startActivity(intent);
				UserHelpNextPagActivity.this.finish();
//				overridePendingTransition(R.anim.out_to_left, R.anim.in_from_right);  
			}
		});
	}
}
