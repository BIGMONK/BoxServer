package com.test.RemoteService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.annotation.TargetApi;
import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.SimpleAdapter;

import com.test.remotecontrol_phone.R;


@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class RemotecontrolActivity extends Activity {
    Button btnLeft;
    Button btnRight;
    ListView mDeviceList;

    boolean flag = false;
    boolean connect_click = false;
    boolean capsLock = false;
    boolean mute = false;

    private int break_time = 0;
    private int connect_time = 3;

    private Socket socket_9999 = null;
    private InetAddress serverAddress_s;
    private String str_serverAddress;
    private DatagramSocket socket_7777;

    private List<String> list = new ArrayList<String>();
    private List<String> listAddress = new ArrayList<String>();


    ListView myList = null;

    /*计时器*/
    private Timer mTimer;
    private MyTimerTask mTimerTask;


    private boolean isonline = false;
    private BufferedReader m9999In;
    private PrintWriter m9999Out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String strVer = Build.VERSION.RELEASE;
        ;
        strVer = strVer.substring(0, 3).trim();
        float fv = Float.valueOf(strVer);
        if (fv > 2.3) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectNetwork() // 这里可以替换为detectAll() 就包括了磁盘读写和网络I/O
                    .build());

        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_remotecontrol);


        mDeviceList = (ListView) findViewById(R.id.device_list);

        mDeviceList.setOnItemClickListener(mItemClickListener);
        btnLeft = (Button) findViewById(R.id.control_left);
        btnLeft.setOnClickListener(mOnClickListener);
        btnRight = (Button) findViewById(R.id.control_right);
        btnRight.setOnClickListener(mOnClickListener);

        ConnectThread mConnectThread = new ConnectThread();
        mConnectThread.start();
        Toast.makeText(RemotecontrolActivity.this, "请确保client与server端在同一网络（wifi）内!", Toast.LENGTH_SHORT).show();

    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            if (v == btnLeft) {
                send_key("key " + KeyEvent.KEYCODE_DPAD_LEFT);

            } else if (v == btnRight) {
                send_key("key " + KeyEvent.KEYCODE_DPAD_RIGHT);

            }

        }
    };


    private void send_key(String string) {
        if (m9999Out != null) {
            m9999Out.println(string);
        } else {
            Toast.makeText(RemotecontrolActivity.this, "设备尚未链接!", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

	/*
     * 1.打开连接PopWindow
	 * 2.没有发现设备，正在重新搜索
	 * 3.没有发现设备，检查网络
	 * 4.心跳包断开
	 */

    public final static int HANDLER_FIND_DEVICES = 1;
    public final static int HANDLER_NODEVICES_RCONNECT = 2;
    public final static int HANDLER_NODEVICES_CHECKNETWORK = 3;
    public final static int HANDLER_DISCONNECT = 4;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_FIND_DEVICES:
                    Log.v("", "====>HANDLER_OPENPOPUWINDOW");
                    mDeviceList.setAdapter(getMenuAdapter());
                    break;

                case HANDLER_NODEVICES_CHECKNETWORK:
                    Toast.makeText(RemotecontrolActivity.this, "未搜寻到设备!", Toast.LENGTH_SHORT).show();

                    break;
                case HANDLER_DISCONNECT:
                    Toast.makeText(RemotecontrolActivity.this, "设备已断开，重新搜索！!", Toast.LENGTH_SHORT).show();
                    list.clear();
                    listAddress.clear();
                    ConnectThread rConnectThread = new ConnectThread();
                    rConnectThread.start();
                    break;
            }
        }
    };

    public class ConnectThread extends Thread {
        @Override
        public void run() {
            Log.v("", "list.size=" + list.size());
            Message msg = mHandler.obtainMessage();
            while (list.size() < 1 && connect_time > 0) {
                Log.v("", "===>connect_time=" + connect_time);
                socket_connect();
                connect_time--;
            }

            if (list.size() > 0) {
                msg.what = HANDLER_FIND_DEVICES;
            } else {
                msg.what = HANDLER_NODEVICES_CHECKNETWORK;
            }
            mHandler.sendMessage(msg);
            connect_time = 3;

        }
    }


    /*
     * 获取连接列表
     */
    private ListAdapter getMenuAdapter() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < list.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemText", (String) list.get(i));
            Log.d("tag", "list name:" + (String) list.get(i));
            data.add(map);
        }
        SimpleAdapter simperAdapter = new SimpleAdapter(this, data,
                R.layout.item_menu, new String[]{"itemText"},
                new int[]{R.id.item_text});
        return simperAdapter;
    }

    /*
     * 寻找局域网中的TV设备
     */
    private void socket_connect() {
        Log.d("TAG", "btnServerStart");
        try {

            socket_7777 = new DatagramSocket(7777);
            Log.v("tag", "socket_7777");
            InetAddress serverAddress = InetAddress.getByName("255.255.255.255");

            String str = "is_remoteService ?";
            byte data[] = str.getBytes();  //把传输内容分解成字节

            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, 7777);
            Log.v("TAG", "packet");
            Log.v("TAG", "socket_7777_create::success");

            //调用socket对象的send方法，发送数据
            socket_7777.send(packet);
            Log.v("TAG", "sendsuccess");
        } catch (Exception e) {
            e.printStackTrace();
            Log.v("TAG", "socket_7777_create::fail");

        }

        Log.d("TAG", "chuangjian");
        byte data[] = new byte[1024];
        listAddress.clear();

        //创建一个空的DatagramPacket对象
        //DatagramPacket packet_re = new DatagramPacket(data,data.length);
        //使用receive方法接收客户端所发送的数据，
        //如果客户端没有发送数据，该进程就停滞在这里
        while (true) {
            DatagramPacket packet_re = new DatagramPacket(data, data.length);
            try {

                socket_7777.setSoTimeout(3000);
                Log.e("TAG", ">>>receive_packet_begin");
                socket_7777.receive(packet_re);
                Log.e("TAG", ">>>receive_packet_end");

                String result = new String(packet_re.getData(), packet_re.getOffset(), packet_re.getLength());
                Log.v("TAG", "result--->" + result);
                String address = packet_re.getAddress().getHostAddress().toString();
                Log.v("TAG", "address--->" + address);


                if (result.indexOf("is_remoteService") == -1 && "is_remoteService".indexOf(result) == -1) {
                    list.add(result);
                    listAddress.add(address);
                }

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

        }
        socket_7777.close();
    }


    OnItemClickListener mItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {

            connect_click = true;
            try {
                serverAddress_s = InetAddress.getByName((String) listAddress.get(arg2));
                str_serverAddress = (String) listAddress.get(arg2);
                Log.v("TAG", "str_address===" + str_serverAddress);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            sock_init();
            Log.v("TAG", "====>sock_init_sucess");
            Log.v("TAG", (String) list.get(arg2));


            try {
                m9999Out = new PrintWriter(socket_9999.getOutputStream(), true);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            try {
                m9999In = new BufferedReader(new InputStreamReader(socket_9999.getInputStream()));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            isonline = true;
            mTimer = new Timer();
            //            IsTimer = new Timer();
            mTimerTask = new MyTimerTask();
            //启动定时器每5秒响应一次
            mTimer.schedule(mTimerTask, 0, 5000);

            // TODO
            SockreceiveThread mSockreceiveThread = new SockreceiveThread();
            mSockreceiveThread.start();

            Toast.makeText(RemotecontrolActivity.this, "已链接设备:" + str_serverAddress, Toast.LENGTH_SHORT).show();
        }
    };

    private void sock_init() {
        /*
         * socket 初始化 9999
		 */
        try {
            //socket_9999 = new Socket("192.168.1.142", 9999);
            socket_9999 = new Socket(serverAddress_s, 9999);
        } catch (UnknownHostException e1) {
            Log.v("TAG", "unknownhostexception");
            e1.printStackTrace();
        } catch (IOException e1) {
            Log.v("TAG", "ioexception");
            e1.printStackTrace();
        }

    }

    class SockreceiveThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    Log.v("tag", "vol_thread_Begin");
                    String recvStr = m9999In.readLine();
                    Log.v("tag", "recvStr==>" + recvStr);
                    if (recvStr.equalsIgnoreCase("online")) {
                        isonline = true;
                    }

                    Log.v("tag", "vol_thread");
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            Log.v("tag", "============================================================================");
            Log.v("tag", "============================================================================");
            Log.v("tag", "timer test!");
            char[] b = new char[10];

            Log.v("tag", "====>Isol===>" + isonline);
            if (!isonline) {
                break_time++;
                if (break_time > 2) {
                    break_time = 0;

                    Message msg = mHandler.obtainMessage();
                    msg.what = HANDLER_DISCONNECT;
                    mHandler.sendMessage(msg);
                }
                Log.v("tag", "=============");
                Log.v("tag", "===连接断开===");
                Log.v("tag", "=============");
                Log.v("tag", "============================================================================");
                Log.v("tag", "============================================================================");
            } else {
                break_time = 0;
            }

            isonline = false;
            m9999Out.println("online");
            Log.v("tag", "is online request send");

        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (connect_click) {
            try {
                socket_9999.close();

                if (null != mTimer)
                    mTimer.cancel();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        System.exit(0);

    }


}
