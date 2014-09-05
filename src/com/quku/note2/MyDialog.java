package com.quku.note2;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class MyDialog extends AlertDialog{

	private Context context;
	public MyDialog(Context context) {
		super(context);
		this.context = context;
	}
	
	protected MyDialog(Context context,int theme) {
		super(context,theme);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setCanceledOnTouchOutside(false);
		getWindow().getDecorView().setSystemUiVisibility(4);  //SYSTEM_UI_FLAG_SHOW_FULLSCREEN = 0x00000004;//隐藏状态栏
	}
	
	public void setText(CharSequence title){
		super.setTitle(title);
	}
	public void setText(int titleId){
		super.setTitle(titleId);
	}
	public void setMessage(CharSequence message){
		super.setMessage(message);
	}
	public void setIcon(Drawable icon){
		super.setIcon(icon);
	}
	public void setIcon(int resId) {
		super.setIcon(resId);
	}
}
