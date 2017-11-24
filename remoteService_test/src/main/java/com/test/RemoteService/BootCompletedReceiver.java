package com.test.RemoteService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent intent2 = new Intent(context, RemoteService.class);
		context.startService(intent2);
	}
}
