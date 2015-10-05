package com.quku.activity;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;

import com.quku.R;

@SuppressLint("SetJavaScriptEnabled")
public class UserGuideActivity extends Activity {
	private WebView mWebView;
	private MediaPlayer mMediaPlayer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_guide);
		initVariables();
		initWebView();
		shootSound();
	}

	private void initVariables() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	private void initWebView() {
		mWebView = (WebView) this.findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setPluginState(PluginState.ON);
		mWebView.setWebChromeClient(new WebChromeClient());
		mWebView.loadUrl("file:///android_asset/help/help.html");
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
		}
		finish();
	}

	/**
	 * 播放系统拍照声音 .
	 */
	public void shootSound() {
		mMediaPlayer = MediaPlayer.create(this, R.raw.backmusic);
		try {
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
			}
			mMediaPlayer.prepare();
			mMediaPlayer.start();
			mMediaPlayer.setLooping(true);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}