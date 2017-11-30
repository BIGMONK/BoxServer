package com.djf.remotecontrol.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.djf.remotecontrol.ConstantConfig;
import com.djf.remotecontrol.LogUtils;
import com.djf.remotecontrol.RemoteUtils;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d(TAG, "onReceive: 收到开机广播");
        if (RemoteUtils.isRk3288()) {
            Intent intent1 = new Intent(context, TVService.class);
            context.startService(intent1);
        }
    }

}
