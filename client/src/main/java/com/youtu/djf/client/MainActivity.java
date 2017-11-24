package com.youtu.djf.client;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.djf.remotecontrol.ServerDevice;
import com.djf.remotecontrol.Utils;
import com.djf.remotecontrol.client.ClientSearchService;
import com.djf.remotecontrol.client.DeviceAdapter;
import com.djf.remotecontrol.client.SerachEventCallback;
import com.djf.remotecontrol.client.TcpClientRunnable;

public class MainActivity extends AppCompatActivity implements SerachEventCallback<Integer>, View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
    private TextView mTvHello;
    private Button mBtnSearch;
    private ListView mLvResult;
    private DeviceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();


        startSearchService(this, this);

    }

    private void startSearchService(Context context, SerachEventCallback<Integer> callback) {
        Toast.makeText(this, "正在启动服务", Toast.LENGTH_SHORT).show();
        ClientSearchService.startService(context, callback);
    }

    PlayServiceConnection mPlayServiceConnection;

    private void bindService() {
        Intent intent = new Intent();
        intent.setClass(this, ClientSearchService.class);
        mPlayServiceConnection = new PlayServiceConnection();
        bindService(intent, mPlayServiceConnection, Context.BIND_AUTO_CREATE);
    }

    ProgressDialog progressBar;

    @Override
    public void onEvent(Integer integer) {
        switch (integer) {
            case SerachEventCallback.ServiceStart:
                Log.d(TAG, "onEvent: " + "服务启动");
                bindService();
                break;
            case SerachEventCallback.StartSearch:
                Log.d(TAG, "onEvent: 开始广播搜索"+Thread.currentThread().getName());
                if(adapter!=null){
                    adapter.notifyDataSetChanged();
                }
                if (progressBar==null){
                    progressBar=new ProgressDialog(this);
                    progressBar.setMessage("正在搜索...");
                }
                progressBar.show();
                break;
            case SerachEventCallback.EndSearch:
                Log.d(TAG, "onEvent: 结束广播搜索" + playService.getResults().size());
                if (playService.getResults().size() > 0) {
                    adapter = new DeviceAdapter(this, playService.getResults());
                    mLvResult.setAdapter(adapter);
                    Toast.makeText(this,"搜索结束",Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this,"没有找到可用设备",Toast.LENGTH_SHORT).show();
                }
                progressBar.cancel();
                break;
            case SerachEventCallback.Searching:
                Log.d(TAG, "onEvent: 正在广播搜索");

                break;
        }
    }

    ClientSearchService playService;

    private void initView() {
        mTvHello = (TextView) findViewById(R.id.tv_hello);
        mBtnSearch = (Button) findViewById(R.id.btn_search);
        mLvResult = (ListView) findViewById(R.id.lv_result);
        mLvResult.setOnItemClickListener(this);
        mBtnSearch.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                if (playService != null) {
                    String name = playService.getClass().getName();
                    Log.d(TAG, "onClick: " + name);
                    //com.djf.remotecontrol.client.ClientSearchService
                    //com.youtu.djf.client.ClientSearchService
                    if (!Utils.isServiceRunning(this, name)) {
                        startSearchService(this, this);
                    } else {
                        playService.searchDevices();
                    }
                } else {
                    startSearchService(this, this);
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, ControlActivity.class);
        intent.putExtra("ip", playService.getResults().get(position).getIp());
        startActivity(intent);

    }


    private class PlayServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playService = ((ClientSearchService.PlayBinder) service).getService();
            if (playService != null) {
                Log.d(TAG, "onServiceConnected: 获取服务成功");
                Utils.setSearchService(playService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}
