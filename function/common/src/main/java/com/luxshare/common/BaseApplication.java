package com.luxshare.common;

import android.app.Application;

import androidx.multidex.MultiDex;

public class BaseApplication extends Application {

    private static BaseApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        // Android 5.0只有一个Dex，为了兼容5.0,需要执行以下代码
        MultiDex.install(this);
    }

    public static BaseApplication getApplication(){
        return application;
    }
}
