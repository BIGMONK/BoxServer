package com.youtu.djf.boxserver;

import android.app.Application;

import com.djf.remotecontrol.Utils;

/**
 * Created by djf on 2017/11/24.
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this,true);
    }
}
