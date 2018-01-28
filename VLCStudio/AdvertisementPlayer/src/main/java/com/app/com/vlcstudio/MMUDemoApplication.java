package com.app.com.vlcstudio;

import android.app.Application;
import android.util.Log;

import com.alimama.mobile.sdk.config.MMUSDKFactory;
import com.alimama.mobile.sdk.config.system.MMLog;

public class MMUDemoApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		//初始化 并注册集成广告样式的Activity
		MMUSDKFactory.getMMUSDK().init(this);
		MMUSDKFactory.registerAcvitity(AdvertisementPlayerActivity.class);
	}
}


