package com.djf.remotecontrol.client;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.djf.remotecontrol.CommandMsgBean;
import com.djf.remotecontrol.ConstantConfig;
import com.djf.remotecontrol.LogUtils;
import com.djf.remotecontrol.RemoteUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.djf.remotecontrol.ConstantConfig.broadcastPort;

/**
 * Created by djf on 2017/11/24.
 */

public class ClientSearchService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    public class PlayBinder extends Binder {
        public ClientSearchService getService() {
            return ClientSearchService.this;
        }
    }


    private static SerachEventCallback<Integer> mCallback;

    public static void startService(Context context, SerachEventCallback eventCallback) {
        mCallback = eventCallback;
        Intent intent = new Intent(context, ClientSearchService.class);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mCallback != null) {
            mCallback.onEvent(SerachEventCallback.ServiceStart);//服务启动
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private boolean isSearching;
    List<CommandMsgBean> results = new ArrayList<>();

    public List<CommandMsgBean> getResults() {
        return results;
    }

    public void searchDevices() {
        if (isSearching) {
            if (mCallback != null) {
                mCallback.onEvent(SerachEventCallback.Searching);//正在扫描
            }
        } else {
            new AsyncTask<Void, Void, List<CommandMsgBean>>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    results.clear();
                    if (mCallback != null) {
                        mCallback.onEvent(SerachEventCallback.StartSearch);//开始扫描
                    }
                    isSearching = true;
                }

                @Override
                protected List<CommandMsgBean> doInBackground(Void... params) {
                    sendUDPBroadcast(results);
                    publishProgress();
                    return null;
                }

                @Override
                protected void onPostExecute(List<CommandMsgBean> serverDevices) {
                    super.onPostExecute(serverDevices);
                    isSearching = false;
                    if (mCallback != null) {
                        mCallback.onEvent(SerachEventCallback.EndSearch);//扫描结束
                    }
                }

                @Override
                protected void onProgressUpdate(Void... values) {
                    super.onProgressUpdate(values);
                    if (mCallback != null) {
                        mCallback.onEvent(SerachEventCallback.Searching);//正在扫描
                    }
                }
            }.execute();
        }
    }

    private DatagramPacket searchPacket;
    List<String> keys = new ArrayList<>();

    private void sendUDPBroadcast(List<CommandMsgBean> results) {
        keys.clear();
        LogUtils.d(TAG, "sendUDPBroadcast: 开始" + System.currentTimeMillis());
        int i = 0, period = 3000, times = 5;
        MulticastSocket socket = null;
        try {
            if(socket == null){
                socket = new MulticastSocket(null);
                socket.setReuseAddress(true);
                socket.setTimeToLive(1);
                socket.bind(new InetSocketAddress(ConstantConfig.broadcastPort));
            }
            socket.setSoTimeout(period);
            if (searchPacket == null) {
                InetAddress address = InetAddress.getByName("255.255.255.255");
                String string = ConstantConfig.UDPBroadcastTAG+":" + android.os.Build.PRODUCT;
                byte[] data = string.getBytes();
                searchPacket = new DatagramPacket(data, data.length, address, broadcastPort);
            }
            socket.send(searchPacket);
        } catch (Exception e) {
            LogUtils.d(TAG, "sendUDPBroadcast: "+e.getMessage());
            e.printStackTrace();
        }
        byte data[] = new byte[1024];
        while (socket != null && i < times) {
            i++;
            DatagramPacket packet_re = new DatagramPacket(data, data.length);
            try {
                LogUtils.e(TAG, ">>>receive_packet_begin");
                socket.receive(packet_re);
                LogUtils.e(TAG, ">>>receive_packet_end");
                String result = new String(packet_re.getData(), packet_re.getOffset(), packet_re.getLength());
                LogUtils.v(TAG, "result--->" + result);
                //等待接受广播响应信息
                if (RemoteUtils.isGoodJson(result)) {
                    CommandMsgBean serverDevice = RemoteUtils.getObject(result, CommandMsgBean.class);
                    if (serverDevice != null && serverDevice.getType() == CommandMsgBean.DEVICE) {
//                        serverDevice.setIp(packet_re.getAddress().getHostAddress());
                        if (!keys.contains(serverDevice.getIp())) {
                            keys.add(serverDevice.getIp());
                            results.add(serverDevice);
                        }
                    }
                }
                Thread.sleep(100);
                socket.send(searchPacket);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (socket != null)
            socket.close();
        LogUtils.d(TAG, "sendUDPBroadcast: 结束" + System.currentTimeMillis());
    }

}
