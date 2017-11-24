package com.djf.remotecontrol.server;

import android.text.TextUtils;
import android.util.Log;

import com.djf.remotecontrol.ServerDevice;
import com.djf.remotecontrol.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "TcpServerRunnable closeSelf  IOException "
                        + this.toString()
                        + "  serverSocket=" + serverSocket.toString()
                        + "  " + e.toString());

            }
        }
    }

    private Socket getSocket(ServerSocket serverSocket) {
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "TcpServerRunnable run getSocket IOException  " + this.toString()
                    + "  serverSocket=" + serverSocket.toString()
                    + "  " + e.toString());
            return null;
        }
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(30000);
            while (isListen) {
                Log.i(TAG, "run: 开始监听..." + this.toString()
                        + "  serverSocket=" + serverSocket.toString());
                Socket socket = getSocket(serverSocket);
                if (socket != null) {
                    new ServerSocketThread(socket);
                }
            }
            serverSocket.close();
        } catch (SocketException e) {
            e.printStackTrace();
            Log.d(TAG, "TcpServerRunnable run  SocketTimeoutException  " + this.toString()
                    + "  serverSocket=" + serverSocket.toString()
                    + "  " + e.toString());

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "TcpServerRunnable run  IOException  " + this.toString()
                    + "  serverSocket=" + serverSocket.toString()
                    + "  " + e.toString());
        }
    }

    ExecutorService cachedThreadPool = Executors.newFixedThreadPool(3);

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
            Log.d(TAG, "ServerSocketThread:检测到新的客户端联入,ip:" + ip + "  "
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
                Log.d(TAG, "ServerSocketThread  SocketException "
                        + this.toString()
                        + "  socket=" + socket.toString()
                        + "  " + e.toString()
                );

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "ServerSocketThread  IOException "
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
            ServerDevice serverDevice = null;
            int rcvLen;
            SST.add(this);
            while (isRun && is != null) {
                try {
                    Log.d(TAG, "ServerSocketThread run: socket.isClosed()=" + socket.isClosed()
                            + "  socket.isInputShutdown()=" + socket.isInputShutdown()
                    );
                    if ((rcvLen = is.read(buff)) != -1) {
                        rcvMsg = new String(buff, 0, rcvLen, "utf-8").trim();
                        Log.i(TAG, "ServerSocketThread run:收到消息:" + rcvMsg);
                        if (Utils.isNumeric(rcvMsg)) {
                            Log.i(TAG, "ServerSocketThread run:收到消息:isNumeric ");
                            final int keyCode = Integer.parseInt(rcvMsg);
                            cachedThreadPool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "run: onKeyEvent=" + keyCode);
                                    Utils.execShellCmd("input keyevent " + keyCode);
                                }
                            });
                        } else {
                            //收到客户端的设备消息表示遥控器已连接
                            if (Utils.isGoodJson(rcvMsg)) {
                                serverDevice = Utils.getObject(rcvMsg, ServerDevice.class);
                                if (serverDevice != null && !TextUtils.isEmpty(serverDevice.getName())) {
                                    Log.d(TAG, "遥控:" + serverDevice.getName() + "已连接");
                                }
                            }
                        }
                    } else {
                        Log.i(TAG, "ServerSocketThread run: 收到消息:客户端关闭时read不阻塞 read返回-1  "
                                + "  rcvLen=" + rcvLen
                                + "  is=" + is.toString());
                        isRun = false;
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                    Log.d(TAG, "ServerSocketThread run SocketException  "
                            + this.toString()
                            + "  socket=" + socket.toString()
                            + "  " + e.toString()
                    );
                    String cause = e.getCause().toString();
                    if (cause.contains("recvfrom failed: ECONNRESET (Connection reset by peer)")) {
                        isRun = false;//客户端主动断开 会抛异常
                        Log.d(TAG, "ServerSocketThread run SocketException  "
                                + this.toString()
                                + "  客户端断开连接"
                        );

                    }
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                    Log.d(TAG, "ServerSocketThread run SocketTimeoutException  "
                            + this.toString()
                            + "  socket=" + socket.toString()
                            + "  " + e.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Log.d(TAG, "ServerSocketThread run UnsupportedEncodingException "
                            + this.toString()
                            + "  socket=" + socket.toString()
                            + "  " + e.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "ServerSocketThread run IOException "
                            + this.toString()
                            + "  socket=" + socket.toString()
                            + "  " + e.toString());
                }
            }
            try {
                socket.close();
                SST.remove(this);
                Log.i(TAG, "run: 断开连接 " + this.toString() + "  socket=" + socket.toString());
                Log.d(TAG, "遥控:" + serverDevice.getName() + "已断开");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "ServerSocketThread run IOException "
                        + this.toString() + "  socket=" + socket.toString()
                        + "  " + e.toString());
            }
        }
    }

}
