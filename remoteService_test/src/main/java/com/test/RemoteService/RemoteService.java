package com.test.RemoteService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import android.util.DisplayMetrics;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.content.ActivityNotFoundException;
import android.os.Process;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import java.util.List;

import java.lang.Object;
import java.util.UUID;
import android.os.SystemClock;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Binder;
import android.os.Handler;
import android.os.UserHandle;


public class RemoteService extends Service {
	static final int CONNECTION_MODE_BT = 0;
	static final int CONNECTION_MODE_WIFI = 1;

	private static final String TAG = "RemoteService TEST";
	private ServerSocket mWifiServerSocket;
	private DatagramSocket mListenerSocket;
	private ArrayList<Remote> mRemotes = new ArrayList<Remote>();
	private String mDeviceName;
	private boolean mFlag = true;
	private InputMethodManager mInputMethodManager;

	private ActivityManager mActivityManager;
	
	/* 一些常量，代表服务器的名称 */
	public static final String PROTOCOL_SCHEME_L2CAP = "btl2cap";
	public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";

	private MyBinder mBinder = new MyBinder();  


	@Override
	public void onCreate() {
		super.onCreate();		
		mActivityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 

		new WifiServerThread().start();

		listenerSocketOpen();

	}


	private void listenerSocketOpen() {
		new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				byte[] buffer = new byte[64];	
				try {
					mListenerSocket = new DatagramSocket(7777);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e(TAG, "new DatagramSocket(7777) err!");
					return;
				}			
				while(mFlag){
					try {
						DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
						mListenerSocket.receive(packet);						
						InetAddress address = packet.getAddress();
						System.out.println(address.getHostAddress() + " "+new String(packet.getData()));
						String revStr = new String(packet.getData());
						if(revStr.indexOf("is_remoteService ?")==0){

							mDeviceName = android.os.Build.MODEL;
							packet = new DatagramPacket(mDeviceName.getBytes(), 0, mDeviceName.getBytes().length, address, packet.getPort());
							mListenerSocket.send(packet);
						}else{
							Log.e(TAG, "not is remoteServiec ! err!");
						}				
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}			
		}).start();
	}

	class MyBinder extends Binder {  
		RemoteService getService() {  
			// Return this instance of LocalService so clients can call public methods  
			return RemoteService.this;  
		}

	} 

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	private class WifiServerThread extends Thread {
		@Override
		public void run() {
			try {
				mWifiServerSocket = new ServerSocket(9999, 3);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
			while(mFlag){
				try {	
					Socket client = mWifiServerSocket.accept();
					Remote newRemote = new Remote(client);//每接收到一个TCP连接都建立一个Remote节点单独处理
					mRemotes.add(newRemote);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
		

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			mFlag = false;
			mWifiServerSocket.close();
			for(Remote remote:mRemotes){
				remote.release();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
	}
	

	private class Remote{
		private Object mSocket;
		private BufferedReader mIn = null;
		private PrintWriter mOut = null;
		private boolean mFlag = true;
		Remote(Object socket){
			mSocket = socket;			
			try {
				mIn = new BufferedReader(new InputStreamReader(((Socket)mSocket).getInputStream()));
				mOut = new PrintWriter(new OutputStreamWriter(((Socket)mSocket).getOutputStream()), true);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}		
	
			ReadThread thread = new ReadThread(Remote.this, mIn, mOut);
			thread.start();
		}
		public void release(){
			mFlag = false;
			try {
				mIn.close();
				mOut.close();
				((Socket)mSocket).close();				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	
		
	}


	private class ReadThread extends Thread {
		private BufferedReader mIn = null;
		private PrintWriter mOut = null;
		private String recvStr = "null";	 
	    private Remote mRemoteNode;

		public ReadThread(Remote remoteNode, BufferedReader in, PrintWriter out){
			mIn = in;
			mOut = out;
			mRemoteNode = remoteNode;			
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub		
			Log.d(TAG, "new client["+mRemotes.size()+"]");
			Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
			Process.setCanSelfBackground(false);
			
			while(mFlag)
			{
				try {			
					String mrecvStr = mIn.readLine();
					Log.d(TAG, "read: "+mrecvStr);
					if(mrecvStr == null || mrecvStr == "null"){
						Log.w(TAG, "null is received!");
						mRemoteNode.release();
						mRemotes.remove(mRemoteNode);
						break;
					}
					String[] mstrs = mrecvStr.split("\n");	
					int i = 0;					
					while( i < mstrs.length)
					{
						recvStr = mstrs[i];							
						i++;
						if (i >= 2)
						{
							Log.d(TAG, "Multicommand send in one time!");
						}
						if(recvStr.equalsIgnoreCase("online")){
							String str = "online";
							mOut.println(str);										
						}else{							
							String[] strs = recvStr.split(" ",5);
							if(strs.length <= 1){
								Log.e(TAG,"error cmd!");
								continue;
							}
					
							if( strs[0].equalsIgnoreCase("key")) {
								int keyCode = Integer.parseInt(strs[1]);
								VKey.sendKeyEventToAndroid(keyCode);
								Log.d(TAG, "new sendKeyEvent keyCode:"+keyCode);					 					      	
							}else{
								Log.w(TAG,"Unknow Command:"+recvStr);
							}
						}	
					}				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					mRemoteNode.release();
					mRemotes.remove(mRemoteNode);
					return;
				}
			}
		}
	}


}


