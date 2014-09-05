package com.quku.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.quku.MyApplication;

public class BaseActivity extends FragmentActivity {
	private static List<Activity> mListAct = new ArrayList<Activity>();
	public BaseActivity mAct;
	public MyApplication mApp;

	public void clearOtherAct() {
		Iterator<Activity> localIterator = mListAct.iterator();
		while (true) {
			if (!localIterator.hasNext())
				return;
			Activity localActivity = (Activity) localIterator.next();
			if ((localActivity == this.mAct) || (localActivity.isFinishing()))
				continue;
			localActivity.finish();
		}
	}

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		this.mAct = this;
		this.mApp = ((MyApplication) getApplication());
		mListAct.add(this.mAct);
	}

	public void toMainAct() {
		for (int i = 0;; ++i) {
			if (i >= mListAct.size()) {
				System.gc();
				return;
			}
			Activity localActivity = (Activity) mListAct.get(i);
			if (localActivity.getClass().getSimpleName().equals("DanteFirstPage"))
				continue;
			localActivity.finish();
		}
	}
}