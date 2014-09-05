package com.quku;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.quku.activity.MianActivity;

public class BootBroadcastReceiverMusic extends BroadcastReceiver {

	static final String ACTION = "android.intent.action.BOOT_COMPLETED";

	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION)) {
			Intent sayHelloIntent = new Intent(context, MianActivity.class);
			sayHelloIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			context.startActivity(sayHelloIntent);
		}
	}
}
