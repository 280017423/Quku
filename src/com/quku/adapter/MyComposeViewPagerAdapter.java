package com.quku.adapter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MyComposeViewPagerAdapter extends FragmentPagerAdapter {
	private List<Fragment> mListFragment;

	public MyComposeViewPagerAdapter(FragmentManager paramFragmentManager, List<Fragment> paramList) {
		super(paramFragmentManager);
		this.mListFragment = paramList;
	}

	public int getCount() {
		return this.mListFragment.size();
	}

	public Fragment getItem(int paramInt) {
		return (Fragment) this.mListFragment.get(paramInt);
	}
}