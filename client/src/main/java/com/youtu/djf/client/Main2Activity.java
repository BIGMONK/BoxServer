package com.youtu.djf.client;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.djf.remotecontrol.CommandMsgBean;
import com.djf.remotecontrol.LogUtils;
import com.djf.remotecontrol.client.DeviceAdapter;
import com.djf.remotecontrol.client.SerachEventCallback;
import com.djf.remotecontrol.client.UDPClientRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main2Activity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        View.OnClickListener, SerachEventCallback<Integer> {
    private static final String TAG = "Main2Activity";
    private Button mBtnSearch;
    private ListView mLvResult;
    private DeviceAdapter adapter;
    UDPClientRunnable runnable;
    ExecutorService exec;//无界线程池，可以进行自动线程回收
    private ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mBtnSearch = (Button) findViewById(R.id.btn_search);
        mLvResult = (ListView) findViewById(R.id.lv_result);
        mLvResult.setOnItemClickListener(this);
        mBtnSearch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                search();
                break;
        }
    }

    private void search() {
        if (runnable == null) {
            synchronized (UDPClientRunnable.class) {
                if (runnable == null) {
                    runnable = new UDPClientRunnable();
                    runnable.setSearchListenEvent(this);
                    if (exec == null) {
                        exec = Executors.newCachedThreadPool();
                    }
                    exec.execute(runnable);
                }
            }
        }
        runnable.sendUDPBroadcast(500, 5);
    }

    List<CommandMsgBean> results=new ArrayList<>();

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (runnable != null) {
            runnable.closeSelf();
            runnable = null;
        }
        Intent intent = new Intent(this, ControlActivity.class);
        intent.putExtra("ip", results.get(position).getIp());
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (runnable != null) {
            runnable.closeSelf();
            runnable = null;
        }
        if (exec != null) {
            exec.shutdownNow();
        }
    }

    @Override
    public void onEvent(Integer integer) {
        switch (integer) {
            case SerachEventCallback.StartSearch:
                LogUtils.d(TAG, "onEvent: 开始广播搜索");
                Toast.makeText(this, "开始搜索", Toast.LENGTH_SHORT).show();
                if (results != null)
                    results.clear();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                if (progressBar == null) {
                    progressBar = new ProgressDialog(this);
                    progressBar.setMessage("正在搜索...");
                }
                progressBar.show();
                break;
            case SerachEventCallback.EndSearch:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Main2Activity.this, "结束广播搜索", Toast.LENGTH_SHORT).show();
                        results.addAll(runnable.getResults());
                        LogUtils.d(TAG, "onEvent: 结束广播搜索" + results.size());

                        if (adapter == null) {
                            adapter = new DeviceAdapter(Main2Activity.this, results);
                            mLvResult.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                            if (results.size() <= 0)
                                Toast.makeText(Main2Activity.this, "没有找到可用设备", Toast
                                        .LENGTH_SHORT).show();
                        }
                        progressBar.cancel();
                    }
                });


                break;
            case SerachEventCallback.Searching:
                LogUtils.d(TAG, "onEvent: 正在广播搜索");
                Toast.makeText(this, "正在广播搜索", Toast.LENGTH_SHORT).show();

                break;
        }
    }
}
