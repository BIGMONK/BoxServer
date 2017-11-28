package com.djf.remotecontrol;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.djf.remotecontrol.client.ClientSearchService;
import com.djf.remotecontrol.server.TVService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.Key;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by djf on 2017/11/23.
 */

public class Utils {
    private static final String TAG = "Utils";
    private static DataOutputStream dataOutputStream;
    private static Process process;
    private static Context mContext;
    private static Gson mGson = new GsonBuilder().create();
    private static ClientSearchService msearchService;

    public static ClientSearchService getSearchService() {
        return msearchService;
    }

    public static void setSearchService(ClientSearchService searchService) {
        msearchService = searchService;
    }

    /**
     * 方法描述：判断某一Service是否正在运行
     *
     * @param context     上下文
     * @param serviceName Service的全路径： 包名 + service的类名
     * @return true 表示正在运行，false 表示没有运行
     */
    public static boolean isServiceRunning(Context context, String serviceName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(200);
        if (runningServiceInfos.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServiceInfos) {
//            Log.d(TAG, "isServiceRunning: "+serviceInfo.service.getClassName());
            if (serviceInfo.service.getClassName().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    public static String getStringFromJson(Object o) {
        return mGson.toJson(o);
    }

    /**
     * @param <T>
     * @param <T>
     * @param jsonString
     * @param cls
     * @return
     */
    public static <t, T> T getObject(String jsonString, Class<T> cls) {
        T t = null;
        try {
            if (isGoodJson(jsonString))
                t = mGson.fromJson(jsonString, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }


    public static void init(Context context, boolean isBox) {
        mContext = context;
        Log.d(TAG, "init: 初始化");
        if ("rk3288".equals(android.os.Build.MODEL) && isBox) {
            mContext.startService(new Intent(mContext, TVService.class));
        }
    }

    public static Context getmContext() {
        return mContext;
    }

    private static JsonParser jsonParser = new JsonParser();

    public static boolean isGoodJson(String json) {

        try {
            jsonParser.parse(json);
            return true;
        } catch (JsonParseException e) {
            System.out.println("bad json: " + json);
            return false;
        }
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }


    public static void simulateKey(final int type, final int KeyCode) {
        new Thread() {
            @Override
            public void run() {
                if (type == CommandMsgBean.KEYEVENT && KeyEvent.KEYCODE_HOME == KeyCode) {
                    Utils.execShellCmd("input keyevent " + KeyCode);
                    return;
                }

                try {
                    Instrumentation inst = new Instrumentation();
                    if (CommandMsgBean.KEYEVENT == type)
                        inst.sendKeyDownUpSync(KeyCode);
                    else
                        inst.sendCharacterSync(KeyCode);
                } catch (Exception e) {
                    Log.e(TAG, "Exception when simulateKey:" + e.toString());
                    if (CommandMsgBean.KEYEVENT == type)
                        Utils.execShellCmd("input keyevent " + KeyCode);
                    else
                        Utils.execShellCmd("input text " + KeyCode);

                }

            }
        }.start();
    }



    /**
     * 执行shell命令
     *
     * @param cmd
     */
    public static void execShellCmd(String cmd) {
        Log.d(TAG, "execShellCmd: " + cmd);
        try {
            if (dataOutputStream == null) {
                // 申请获取root权限，这一步很重要，不然会没有作用
                Log.d(TAG, "execShellCmd: init");
                Runtime run = Runtime.getRuntime();
                process = run.exec("su");
                // 获取输出流
                dataOutputStream = new DataOutputStream(process.getOutputStream());
            }
            dataOutputStream.writeBytes(cmd + "\n");
            dataOutputStream.flush();
            Log.d(TAG, "execShellCmd: flush");
        } catch (Throwable t) {
            t.printStackTrace();
            try {
                dataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static Toast mToast;

    public static void showToast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(msg);
        }
        mToast.show();
    }
}
