package com.quku.activity;

import java.io.IOException;

import com.quku.R;
import com.quku.R.id;
import com.quku.R.layout;
import com.quku.R.raw;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;

public class OpenWebViewActivity extends Activity {
    /** Called when the activity is first created. */
	
	WebView webView;
	MediaPlayer mp= null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().getDecorView().setSystemUiVisibility(4); //屏蔽状态栏
        setContentView(R.layout.layout_help);
        webView = (WebView)this.findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        //NARROW_COLUMNS：可能的话使所有列的宽度不超过屏幕宽度
//        webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN); 
		// mWebView.getSettings().setPluginsEnabled(true);
        webView.getSettings().setPluginState(PluginState.ON);
        webView.setWebChromeClient(new WebChromeClient());
//        webView.setInitialScale(100);
        webView.loadUrl("file:///android_asset/help/help.html");
        shootSound();
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	if(mp !=null){  
    		mp.stop();  
    	}
    	this.finish();
    }
    
    
    /** 
	 *   播放系统拍照声音 
	. */  
	public void shootSound(){  
		mp = MediaPlayer.create(this,R.raw.backmusic);  
		try {
			if (mp != null) {
				mp.stop();
			}
			mp.prepare();
			mp.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}  
/*    private void set(WebSettings settings){
    	DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        if (mDensity == 120) {
        settings.setDefaultZoom(ZoomDensity.CLOSE);
        }else if (mDensity == 160) {
        settings.setDefaultZoom(ZoomDensity.MEDIUM);
        }else if (mDensity == 240) {
        settings.setDefaultZoom(ZoomDensity.FAR);
        }
    }*/
}