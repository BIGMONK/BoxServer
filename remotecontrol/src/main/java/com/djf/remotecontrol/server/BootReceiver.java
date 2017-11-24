package com.djf.remotecontrol.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: 收到开机广播");
        if (("rk3288").equals(Build.DEVICE)) {
            Intent intent1 = new Intent(context, TVService.class);
            context.startService(intent1);
        }
    }

}
