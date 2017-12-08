package com.youtu.djf.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.djf.remotecontrol.CommandMsgBean;
import com.djf.remotecontrol.ConstantConfig;
import com.djf.remotecontrol.DeviceUtils;
import com.djf.remotecontrol.LogUtils;
import com.djf.remotecontrol.NetworkUtils;
import com.djf.remotecontrol.RemoteUtils;
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
    private Button mBtnPower;
    private Button mBtnReboot;
    private Button mBtnTest;
    private EditText mTvCode;

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
        cachedThreadPool.shutdownNow();
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
        mBtnPower = (Button) findViewById(R.id.btn_power);
        mBtnPower.setOnClickListener(this);
        mBtnReboot = (Button) findViewById(R.id.btn_reboot);
        mBtnReboot.setOnClickListener(this);
        mBtnTest = (Button) findViewById(R.id.btn_test);
        mBtnTest.setOnClickListener(this);
        mTvCode = (EditText) findViewById(R.id.tv_code);
        mTvCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:

                break;
            case R.id.btn_ok:
                if (tcpClient != null) {
                    tcpClient.send(RemoteUtils.getStringFromJson(
                             CommandMsgBean.OK));
                }
                break;
            case R.id.btn_down:
                if (tcpClient != null) {
                    tcpClient.send(RemoteUtils.getStringFromJson(
                            CommandMsgBean.DOWN));
                }
                break;
            case R.id.btn_up:
                if (tcpClient != null) {
                    tcpClient.send(RemoteUtils.getStringFromJson(
                            CommandMsgBean.UP));
                }
                break;
            case R.id.btn_left:
                if (tcpClient != null) {
                    tcpClient.send(RemoteUtils.getStringFromJson(
                            CommandMsgBean.LEFT));
                }
                break;
            case R.id.btn_right:
                if (tcpClient != null) {
                    tcpClient.send(RemoteUtils.getStringFromJson(
                            CommandMsgBean.RIGHT));
                }
                break;
            case R.id.btn_home:
                if (tcpClient != null) {
                    tcpClient.send(RemoteUtils.getStringFromJson(
                           CommandMsgBean.Home));
                }
                break;
            case R.id.btn_back:
                if (tcpClient != null) {
                    tcpClient.send(RemoteUtils.getStringFromJson(
                            CommandMsgBean.BACK));
                }
                break;
            case R.id.btn_volinc:
                if (tcpClient != null) {
                    tcpClient.send(RemoteUtils.getStringFromJson(
                            CommandMsgBean.VolUp));
                }
                break;
            case R.id.btn_voldec:
                if (tcpClient != null) {
                    tcpClient.send(RemoteUtils.getStringFromJson(
                            CommandMsgBean.VolDown));
                }
                break;
            case R.id.btn_power:
                if (tcpClient != null) {
                    tcpClient.send(RemoteUtils.getStringFromJson(
                            CommandMsgBean.PowerOff));
                }
                break;
            case R.id.btn_reboot:
                if (tcpClient != null) {
                    tcpClient.send(RemoteUtils.getStringFromJson(
                            CommandMsgBean.Reboot));
                }
                break;
            case R.id.btn_test:
                submit();
                break;
        }
    }

    private void submit() {
        // validate
        String code = mTvCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "测试键值null", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tcpClient != null) {
            tcpClient.send(RemoteUtils.getStringFromJson(
                    new CommandMsgBean(CommandMsgBean.KEYEVENT
                            , Integer.parseInt(code)
                            , Build.MODEL, DeviceUtils.getMacAddress()
                            , NetworkUtils.getIPAddress(true)
                    )));
        }

    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            switch (mAction) {
                case "tcpClientReceiver":
                    String msg = intent.getStringExtra("tcpClientReceiver");
                    LogUtils.d(TAG, "onReceive: " + msg);
                    int flag = intent.getIntExtra("flag", -1);
                    switch (flag) {
                        case ConstantConfig.StartConnect:
                            LogUtils.d(TAG, "onReceive: 开始连接设备");
                            showToast("开始连接设备");
                            break;
                        case ConstantConfig.SuccessConnected:
                            LogUtils.d(TAG, "onReceive: 设备连接成功");
                            showToast("设备连接成功");
                            break;
                        case ConstantConfig.FailedConnected:
                            LogUtils.d(TAG, "onReceive: 设备连接失败");
                            showToast("设备连接失败");
                            break;
                        case ConstantConfig.Connecting:
                            LogUtils.d(TAG, "onReceive: 收到消息");
                            break;
                        case ConstantConfig.Disconnected:
                            LogUtils.d(TAG, "onReceive: 连接已断开");
                            showToast("连接已断开");
                            finish();
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
