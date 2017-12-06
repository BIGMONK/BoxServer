package com.youtu.djf.client;

import android.app.Application;

import com.djf.remotecontrol.CommandMsgBean;
import com.djf.remotecontrol.RemoteUtils;

/**
 * Created by djf on 2017/11/24.
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RemoteUtils.init(this,false, CommandMsgBean.DEVICE_UNKNOWN, true);
    }
}
