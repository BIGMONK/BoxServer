package com.djf.remotecontrol.server;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;

import com.djf.remotecontrol.CommandMsgBean;
import com.djf.remotecontrol.ConstantConfig;
import com.djf.remotecontrol.DeviceUtils;
import com.djf.remotecontrol.LogUtils;
import com.djf.remotecontrol.NetworkUtils;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by djf on 2017/11/23.
 */

public class TVService extends Service {
    private static final String TAG = "TVService";
    ExecutorService cachedThreadPool = Executors.newFixedThreadPool(3);
    private TcpServerRunnable tcpServerRunnable;

    public TVService() {
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG,"onCreate");
        listenSoketOpen();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d(TAG,"onStartCommand");
        startSocketServerRunnable(ConstantConfig.controlPort);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 启动客户端接入监听和消息监听线程
     * @param port
     */
    private void startSocketServerRunnable(int port) {
        tcpServerRunnable = new TcpServerRunnable(port);
        cachedThreadPool.execute(tcpServerRunnable);
    }
    /**
     * 关闭客户端接入监听和消息监听线程
     */
    private void stopSocketServerRunnable(){
        if (tcpServerRunnable!=null){
            tcpServerRunnable.closeSelf();
            tcpServerRunnable=null;
        }
    }

    /**
     * 监听客户端查找广播
     */
    private void listenSoketOpen() {
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                MulticastSocket mListenerSocket;
                byte[] buffer = new byte[1024];
                try {
                    mListenerSocket = new MulticastSocket(ConstantConfig.broadcastPort);
                } catch (IOException e) {
                    e.printStackTrace();
                    LogUtils.e(TAG, "new DatagramSocket() err!");
                    return;
                }
                while (true) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        LogUtils.d(TAG, "listenSoketOpen receive");
                        mListenerSocket.receive(packet);
                        InetAddress address = packet.getAddress();
                        String result = new String(packet.getData(), packet.getOffset(), packet.getLength());
                        LogUtils.d(TAG, "收到客户端广播数据：" + result + "----" + packet.getSocketAddress());
                        if (result.startsWith(ConstantConfig.UDPBroadcastTAG)) {
                            //服务端收到广播数据之后回复服务端信息
                            String mDeviceName =  new GsonBuilder().create().toJson(
                                    new CommandMsgBean( CommandMsgBean.DEVICE
                                            , -1
                                            , Build.MODEL, DeviceUtils.getMacAddress()
                                            , NetworkUtils.getIPAddress(true)
                                    )
                            );
                            packet = new DatagramPacket(mDeviceName.getBytes(), 0,
                                    mDeviceName.getBytes().length,
                                    address, packet.getPort());
                            mListenerSocket.send(packet);
                            LogUtils.d(TAG, "响应客户端广播数据："+new String(packet.getData(), packet.getOffset(), packet.getLength()));
                        } else {
                            LogUtils.e(TAG, "not is remoteServiec ! err!");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        cachedThreadPool.shutdownNow();
    }
}