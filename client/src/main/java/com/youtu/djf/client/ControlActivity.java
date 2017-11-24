package com.youtu.djf.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.djf.remotecontrol.ConstantConfig;
import com.djf.remotecontrol.Utils;
import com.djf.remotecontrol.client.TcpClientRunnable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ControlActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ControlActivity";
    private TextView mTvHello;
    private Button mBtnConnect;
    private Button mBtnOk;
    private Button mButton2;
    private Button mBtnUp;
    private Button mBtnLeft;
    private Button mBtnRight;
    private Button mBtnVolinc;
    private Button mBtnVoldec;
    private Button mBtnDown;
    private Button mBtnHome;
    private Button mBtnBack;
    TcpClientRunnable tcpClient;
    ExecutorService cachedThreadPool = Executors.newFixedThreadPool(1);
    private MyBroadcastReceiver myBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("tcpClientReceiver");
        registerReceiver(myBroadcastReceiver, intentFilter);

        initView();
        String ip = getIntent().getStringExtra("ip");
        tcpClient = new TcpClientRunnable(ip, ConstantConfig.controlPort);
        cachedThreadPool.execute(tcpClient);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (tcpClient != null) {
            tcpClient.closeSelf();
        }
        unregisterReceiver(myBroadcastReceiver);
    }

    private void initView() {
        mTvHello = (TextView) findViewById(R.id.tv_hello);
        mBtnConnect = (Button) findViewById(R.id.btn_connect);
        mBtnOk = (Button) findViewById(R.id.btn_ok);
        mButton2 = (Button) findViewById(R.id.btn_down);
        mBtnUp = (Button) findViewById(R.id.btn_up);
        mBtnLeft = (Button) findViewById(R.id.btn_left);
        mBtnRight = (Button) findViewById(R.id.btn_right);
        mBtnVolinc = (Button) findViewById(R.id.btn_volinc);
        mBtnVoldec = (Button) findViewById(R.id.btn_voldec);

        mBtnConnect.setOnClickListener(this);
        mBtnOk.setOnClickListener(this);
        mButton2.setOnClickListener(this);
        mBtnUp.setOnClickListener(this);
        mBtnLeft.setOnClickListener(this);
        mBtnRight.setOnClickListener(this);
        mBtnVolinc.setOnClickListener(this);
        mBtnVoldec.setOnClickListener(this);
        mBtnDown = (Button) findViewById(R.id.btn_down);
        mBtnDown.setOnClickListener(this);
        mBtnHome = (Button) findViewById(R.id.btn_home);
        mBtnHome.setOnClickListener(this);
        mBtnBack = (Button) findViewById(R.id.btn_back);
        mBtnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:

                break;
            case R.id.btn_ok:
//                Utils.execShellCmd(KeyEvent.KEYCODE_DPAD_CENTER + "");
                if (tcpClient != null) {
                    tcpClient.send(KeyEvent.KEYCODE_DPAD_CENTER + "");
                }
                break;
            case R.id.btn_down:
//                Utils.execShellCmd(KeyEvent.KEYCODE_DPAD_DOWN + "");
                if (tcpClient != null) {
                    tcpClient.send(KeyEvent.KEYCODE_DPAD_DOWN + "");
                }
                break;
            case R.id.btn_up:
//                Utils.execShellCmd(KeyEvent.KEYCODE_DPAD_UP + "");
                if (tcpClient != null) {
                    tcpClient.send(KeyEvent.KEYCODE_DPAD_UP + "");
                }
                break;
            case R.id.btn_left:
//                Utils.execShellCmd(KeyEvent.KEYCODE_DPAD_LEFT + "");
                if (tcpClient != null) {
                    tcpClient.send(KeyEvent.KEYCODE_DPAD_LEFT + "");
                }
                break;
            case R.id.btn_right:
//                Utils.execShellCmd(KeyEvent.KEYCODE_DPAD_RIGHT + "");
                if (tcpClient != null) {
                    tcpClient.send(KeyEvent.KEYCODE_DPAD_RIGHT + "");
                }
                break;
            case R.id.btn_volinc:
                break;
            case R.id.btn_voldec:
                break;
            case R.id.btn_home:
//                Utils.execShellCmd(KeyEvent.KEYCODE_HOME + "");
                if (tcpClient != null) {
                    tcpClient.send(KeyEvent.KEYCODE_HOME + "");
                }
                break;
            case R.id.btn_back:
                Utils.execShellCmd(KeyEvent.KEYCODE_BACK + "");
                if (tcpClient != null) {
                    tcpClient.send(KeyEvent.KEYCODE_BACK + "");
                }
                break;
        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            switch (mAction) {
                case "tcpClientReceiver":
                    String msg = intent.getStringExtra("tcpClientReceiver");
                    Log.d(TAG, "onReceive: " + msg);
                    int flag = intent.getIntExtra("flag", -1);
                    switch (flag) {
                        case TcpClientRunnable.StartConnect:
                            Log.d(TAG, "onReceive: 开始连接设备");
                            showToast("开始连接设备");
                            break;
                        case TcpClientRunnable.SuccessConnected:
                            Log.d(TAG, "onReceive: 设备连接成功");
                            showToast("设备连接成功");
                            break;
                        case TcpClientRunnable.FailedConnected:
                            Log.d(TAG, "onReceive: 设备连接失败");
                            showToast("设备连接失败");
                            break;
                        case TcpClientRunnable.Connecting:
                            Log.d(TAG, "onReceive: 收到消息");
                            break;
                        case TcpClientRunnable.Disconnected:
                            Log.d(TAG, "onReceive: 连接已断开");
                            showToast("连接已断开");
                            break;
                    }
                    break;
            }
        }
    }

    Toast mToast;
    private void showToast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(msg);
        }
        mToast.show();

    }
}
