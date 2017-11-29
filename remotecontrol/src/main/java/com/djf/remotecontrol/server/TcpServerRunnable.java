package com.djf.remotecontrol.server;

import android.text.TextUtils;

import com.djf.remotecontrol.CommandMsgBean;
import com.djf.remotecontrol.LogUtils;
import com.djf.remotecontrol.RemoteUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TcpServerRunnable implements Runnable {
    private String TAG = "TcpServerRunnable";
    private int port;
    private boolean isListen = true;   //线程监听标志位
    public ArrayList<ServerSocketThread> SST = new ArrayList<ServerSocketThread>();
    private ServerSocket serverSocket;

    public String getLocalSocketAdd() {
        if (serverSocket == null) {
            return "serverSocket=null";
        }
        return serverSocket.getLocalSocketAddress().toString();
    }

    public TcpServerRunnable(int port) {
        this.port = port;
    }

    //更改监听标志位
    public void setIsListen(boolean b) {
        isListen = b;
    }

    public void closeSelf() {
        isListen = false;
        for (ServerSocketThread s : SST) {
            s.isRun = false;
        }
        SST.clear();
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            LogUtils.d(TAG, "TcpServerRunnable closeSelf Runable："
                    + this.toString()
                    + "  IOException：" + e.toString());
            e.printStackTrace();
        }

    }

    private Socket getSocket(ServerSocket serverSocket) {
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            LogUtils.d(TAG, "遥控器接入监听异常TcpServerRunnable run getSocket ：" + this.toString()
                    + "  serverSocket：" + serverSocket.toString()
                    + "  IOException：" + e.toString());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(30000);
            while (isListen) {
                LogUtils.i(TAG, "遥控器接入监听开始：" + this.toString()
                        + "  serverSocket=" + serverSocket.toString());
                Socket socket = getSocket(serverSocket);
                if (socket != null) {
                    new ServerSocketThread(socket);
                }
            }
            serverSocket.close();
        } catch (Exception e) {
            LogUtils.e(TAG, "遥控器接入监听异常：" + this.toString()
                    + "  Exception：" + e.toString());
            e.printStackTrace();
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    ExecutorService cachedThreadPool = Executors.newFixedThreadPool(3);

    /**
     * 远程命令执行线程
     */
    public class ServerSocketThread extends Thread {
        public Socket socket = null;
        private PrintWriter pw;
        private InputStream is = null;
        private OutputStream os = null;
        private String ip = null;
        private boolean isRun = true;

        ServerSocketThread(Socket socket) {
            this.socket = socket;
            ip = socket.getInetAddress().toString();
            LogUtils.d(TAG, "ServerSocketThread:检测到新的客户端联入,ip:" + ip + "  "
                    + "  socket=" + socket.toString()
                    + this.toString()
            );
            try {
                socket.setSoTimeout(10000);
                os = socket.getOutputStream();
                is = socket.getInputStream();
                pw = new PrintWriter(os, true);
                send("服务器已收到:" + socket.getRemoteSocketAddress() + "的请求并建立连接");
                start();
            } catch (SocketException e) {
                e.printStackTrace();
                LogUtils.d(TAG, "ServerSocketThread  SocketException "
                        + this.toString()
                        + "  socket=" + socket.toString()
                        + "  " + e.toString()
                );

            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.d(TAG, "ServerSocketThread  IOException "
                        + this.toString()
                        + "  socket=" + socket.toString()
                        + "  " + e.toString());

            }
        }

        public void send(String msg) {
            pw.println(msg);
            pw.flush(); //强制送出数据
        }

        @Override
        public void run() {
            byte buff[] = new byte[1024];
            String rcvMsg;
            CommandMsgBean serverDevice = null;
            int rcvLen;
            SST.add(this);

            while (isRun && is != null) {
                try {
                    LogUtils.d(TAG, "ServerSocketThread run: read before");
                    if ((rcvLen = is.read(buff)) != -1) {
                        rcvMsg = new String(buff, 0, rcvLen, "utf-8").trim();
                        LogUtils.i(TAG, "ServerSocketThread run:收到消息:" + rcvMsg);
                        if (RemoteUtils.isGoodJson(rcvMsg)) {
                            CommandMsgBean bean = RemoteUtils.getObject(rcvMsg, CommandMsgBean.class);
                            switch (bean.getType()) {
                                case CommandMsgBean.KEYEVENT:
                                case CommandMsgBean.SYSTEM:
                                    RemoteUtils.simulateKey(bean.getType(), bean.getKeycode());
                                case CommandMsgBean.TEXT:
                                    break;
                                case CommandMsgBean.DEVICE:
                                    serverDevice = RemoteUtils.getObject(rcvMsg, CommandMsgBean.class);
                                    if (serverDevice != null && !TextUtils.isEmpty(serverDevice.getDevice())) {
                                        LogUtils.d(TAG, "遥控:" + serverDevice.getDevice() + "-->" + serverDevice.getMac() + "已连接");
                                    }
                                    break;
                                case CommandMsgBean.OTHER:
                                    switch (bean.getKeycode()){
                                        case CommandMsgBean.POWER_CODE://关机命令总是重启，(＃￣～￣＃)
//                                            RemoteUtils.execShellCmd(" reboot -p");
                                            RemoteUtils.powerOff();
                                            break;
                                        case CommandMsgBean.REBOOT_CODE:
                                            RemoteUtils.execShellCmd(" reboot");
                                            break;
                                    }
                                    break;
                            }
                        } else {
                            LogUtils.i(TAG, "ServerSocketThread run:收到未识别消息:" + rcvMsg);
                        }
                    } else {
                        LogUtils.i(TAG, "ServerSocketThread run: 客户端关闭时read不阻塞 read返回-1  "
                                + "  rcvLen=" + rcvLen
                                + "  is=" + is.toString());
                        isRun = false;
                    }
                } catch (Exception e) {
                    LogUtils.d(TAG, "远程命令执行线程ServerSocketThread:" + this.toString()
                            + "   run Exception：" + e.toString()
                            + "  socket=" + socket.toString());
                    e.printStackTrace();
                }
            }
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (pw != null)
                pw.close();

            try {
                if (os != null)
                    os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (socket != null)
                    socket.close();
                SST.remove(this);
                LogUtils.i(TAG, "run: 断开连接 " + this.toString());
                LogUtils.d(TAG, "遥控:" + serverDevice.getDevice() + "已断开");
            } catch (IOException e) {
                LogUtils.d(TAG, "远程命令执行线程关闭连接ServerSocketThread:" + this.toString()
                        + "   run Exception：" + e.toString());
                e.printStackTrace();
            }
        }
    }

}
