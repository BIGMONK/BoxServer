package com.djf.remotecontrol.server;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.djf.remotecontrol.ConstantConfig;
import com.djf.remotecontrol.ServerDevice;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by djf on 2017/11/23.
 */

public class TVService extends Service {
    private static final String TAG = "TVService";
    ExecutorService cachedThreadPool = Executors.newFixedThreadPool(3);

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
        listenSoketOpen();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startSocketServer(ConstantConfig.controlPort);
        return super.onStartCommand(intent, flags, startId);
    }

    private void startSocketServer(int port) {
        cachedThreadPool.execute(new TcpServerRunnable(port));
    }

    /**
     * 监听客户端查找广播
     */
    private void listenSoketOpen() {
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                DatagramSocket mListenerSocket;
                byte[] buffer = new byte[1024];
                try {
                    mListenerSocket = new DatagramSocket(ConstantConfig.broadcastPort);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "new DatagramSocket() err!");
                    return;
                }
                while (true) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        Log.d(TAG, "listenSoketOpen receive");
                        mListenerSocket.receive(packet);
                        InetAddress address = packet.getAddress();
                        String result = new String(packet.getData(), packet.getOffset(), packet.getLength());
                        Log.d(TAG, "收到客户端数据：" + result + "----" + packet.getSocketAddress());
                        if (result.startsWith("call remote")) {
                            String mDeviceName =  new GsonBuilder().create().toJson(new ServerDevice(Build.DEVICE,null,null));
                            packet = new DatagramPacket(mDeviceName.getBytes(), 0,
                                    mDeviceName.getBytes().length,
                                    address, packet.getPort());
                            mListenerSocket.send(packet);
                            Log.d(TAG, "响应客户端数据："+new String(packet.getData(), packet.getOffset(), packet.getLength()));
                        } else {
                            Log.e(TAG, "not is remoteServiec ! err!");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}