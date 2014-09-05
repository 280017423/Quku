package com.quku.note2;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

public class MyDialogBuilder extends AlertDialog.Builder {
public MyDialogBuilder(Context arg0, int arg1) {
		super(arg0, arg1);
	}

	public MyDialogBuilder(Context arg0) {
		super(arg0);
		this.setCancelable(true);
	}

	@Override
	public Builder setItems(int itemsId, OnClickListener listener) {
		return super.setItems(itemsId, listener);
	}

}
