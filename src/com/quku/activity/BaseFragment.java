package com.quku.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BaseFragment extends Fragment {
	public BaseActivity mAct;

	public void onAttach(Activity paramActivity) {
		super.onAttach(paramActivity);
		this.mAct = ((BaseActivity) paramActivity);
	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
	}

	public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
		return super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
	}

	public void onDestroy() {
		super.onDestroy();
	}

	public void onDestroyView() {
		super.onDestroyView();
	}

	public void onDetach() {
		super.onDetach();
	}

	public void onPause() {
		super.onPause();
	}

	public void onResume() {
		super.onResume();
	}

	public void onStart() {
		super.onStart();
	}

	public void onStop() {
		super.onStop();
	}

	public void onViewCreated(View paramView, Bundle paramBundle) {
		super.onViewCreated(paramView, paramBundle);
	}
}