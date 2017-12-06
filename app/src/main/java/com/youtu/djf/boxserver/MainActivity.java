package com.youtu.djf.boxserver;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.TextView;

import com.djf.remotecontrol.DeviceUtils;
import com.djf.remotecontrol.NetworkUtils;
import com.djf.remotecontrol.RemoteUtils;

public class MainActivity extends AppCompatActivity {
    private TextView tvIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvIp = (TextView) findViewById(R.id.tv_ip);
        tvIp.setText("MAC=" + DeviceUtils.getMacAddress()
                + "  IP=" + NetworkUtils.getIPAddress(true));
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }
}
