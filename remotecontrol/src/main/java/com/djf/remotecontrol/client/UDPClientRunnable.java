package com.djf.remotecontrol.client;

import com.djf.remotecontrol.CommandMsgBean;
import com.djf.remotecontrol.ConstantConfig;
import com.djf.remotecontrol.LogUtils;
import com.djf.remotecontrol.RemoteUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

import static com.djf.remotecontrol.ConstantConfig.broadcastPort;

/**
 * Created by djf on 2017/12/8.
 */

public class UDPClientRunnable implements Runnable {
    private static final String TAG = "UDPClientRunnable";
    private MulticastSocket socket;
    private boolean isRun;
    private SerachEventCallback<Integer> mCallback;
    ExecutorService exec = Executors.newCachedThreadPool();
    ;//无界线程池，可以进行自动线程回收

    @Override
    public void run() {
        createSocket();
    }

    public void setSearchListenEvent(SerachEventCallback<Integer> eventCallback) {
        this.mCallback = eventCallback;
    }

    private void createSocket() {
        isRun = true;
        try {
            socket = new MulticastSocket(null);
            socket.setReuseAddress(true);
            socket.setTimeToLive(1);
            socket.bind(new InetSocketAddress(broadcastPort));
            socket.setSoTimeout(3000);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte data[] = new byte[1024];
        while (isRun && socket != null) {
            DatagramPacket packet_re = new DatagramPacket(data, data.length);
            try {
                LogUtils.e(TAG, ">>>receive_packet_begin");
                socket.receive(packet_re);
                LogUtils.e(TAG, ">>>receive_packet_end");
                String result = new String(packet_re.getData(), packet_re.getOffset(), packet_re
                        .getLength());
                LogUtils.v(TAG, "result--->" + result);
                //等待接受广播响应信息
                if (RemoteUtils.isGoodJson(result)) {
                    CommandMsgBean serverDevice = RemoteUtils.getObject(result, CommandMsgBean
                            .class);
                    if (serverDevice != null && serverDevice.getType() == CommandMsgBean.DEVICE) {
                        if (!keys.contains(serverDevice.getIp())) {
                            keys.add(serverDevice.getIp());
                            results.add(serverDevice);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void closeSelf() {
        isRun = false;
        if (socket != null) {
            socket.close();
        }
    }

    private DatagramPacket searchPacket;
    List<String> keys = new ArrayList<>();

    public List<CommandMsgBean> getResults() {
        return results;
    }

    List<CommandMsgBean> results = new ArrayList<>();
    boolean isBroadcasting;

    int mtimes, mperiod;

    /**
     *
     * @param period    广播周期
     * @param times     广播次数
     */
    public void sendUDPBroadcast(int period, int times) {
        if (isBroadcasting) {
            if (mCallback != null) {
                mCallback.onEvent(SerachEventCallback.Searching);//正在扫描
            }
            return;
        }
        if (times <= 0) {
            mtimes = 5;
        } else {
            mtimes = times;
        }
        if (period < 100) {
            mperiod = 100;
        } else {
            mperiod = period;
        }
        isBroadcasting = true;
        keys.clear();
        results.clear();
        LogUtils.d(TAG, "sendUDPBroadcast: 开始" + System.currentTimeMillis());
        if (mCallback != null) {
            mCallback.onEvent(SerachEventCallback.StartSearch);//开始扫描
        }
        try {
            if (searchPacket == null) {
                InetAddress address = InetAddress.getByName("255.255.255.255");
                String string = ConstantConfig.UDPBroadcastTAG + ":" + android.os.Build.MODEL;
                byte[] data = string.getBytes();
                searchPacket = new DatagramPacket(data, data.length, address, broadcastPort);
            }
            LogUtils.dTag(TAG, "发送广播:" + mtimes);
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    while (mtimes-- > 0 && socket != null) {
                        try {
                            socket.send(searchPacket);
                            LogUtils.dTag(TAG, "发送广播:" + new String(searchPacket.getData()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(mperiod);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    isBroadcasting = false;
                    if (mCallback != null) {
                        mCallback.onEvent(SerachEventCallback.EndSearch);//扫描结束
                    }
                }
            });

        } catch (Exception e) {
            LogUtils.d(TAG, "sendUDPBroadcast Exception: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
