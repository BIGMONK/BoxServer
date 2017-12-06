package com.djf.remotecontrol;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.djf.remotecontrol.client.ClientSearchService;
import com.djf.remotecontrol.server.TVService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by djf on 2017/11/23.
 */

public class RemoteUtils {
    private static final String TAG = "RemoteUtils";
    private static DataOutputStream dataOutputStream;
    private static Process process;
    private static Context mContext;
    private static Gson mGson = new GsonBuilder().create();
    private static ClientSearchService msearchService;

    public static int getmBoxType() {
        return mBoxType;
    }

    public static void setmBoxType(int mBoxType) {
        RemoteUtils.mBoxType = mBoxType;
    }

    private static int mBoxType = 0;

    /**
     * 远程控制初始化
     *
     * @param context
     * @param isBox   是盒子端还是app端
     * @param logout  是否打印日志
     */
    public static void init(Context context, boolean isBox, int boxType, boolean logout) {
        mContext = context;
        rHandler = new Handler();
        rMainThread = Thread.currentThread();
        LogUtils.getConfig().setGlobalTag("remote").setConsoleSwitch(logout);
        LogUtils.d(TAG, "init: 初始化");
        if (isRk3288() && isBox) {
            mBoxType = boxType;
            mContext.startService(new Intent(mContext, TVService.class));
        }
    }

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
//            LogUtils.d(TAG, "isServiceRunning: "+serviceInfo.service.getClassName());
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


    private static Thread rMainThread;
    private static Handler rHandler;

    public static Thread getrMainThread() {
        return rMainThread;
    }

    public static Handler getrHandler() {
        return rHandler;
    }


    public static boolean isRk3288() {
        if ((ConstantConfig.RK3288.equalsIgnoreCase(android.os.Build.MODEL)
                || ConstantConfig.RK3288.equalsIgnoreCase(Build.DEVICE))) {
            return true;
        }
        return false;
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
            LogUtils.d(TAG, "bad json: " + json);
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
                if (type == CommandMsgBean.SYSTEM) {
                    RemoteUtils.execShellCmd("input keyevent " + KeyCode);
                    return;
                }

                try {
                    Instrumentation inst = new Instrumentation();
                    if (CommandMsgBean.KEYEVENT == type)
                        inst.sendKeyDownUpSync(KeyCode);
                    else if (CommandMsgBean.TEXT == type)
                        inst.sendCharacterSync(KeyCode);
                } catch (Exception e) {
                    LogUtils.e(TAG, "Exception when simulateKey:" + e.toString());
                    if (CommandMsgBean.KEYEVENT == type)
                        RemoteUtils.execShellCmd("input keyevent " + KeyCode);
                    else if (CommandMsgBean.TEXT == type)
                        RemoteUtils.execShellCmd("input text " + KeyCode);

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
        LogUtils.d(TAG, "execShellCmd: " + cmd);
        try {
            if (dataOutputStream == null) {
                // 申请获取root权限，这一步很重要，不然会没有作用
                LogUtils.d(TAG, "execShellCmd: init");
                Runtime run = Runtime.getRuntime();
                process = run.exec("su");
                // 获取输出流
                dataOutputStream = new DataOutputStream(process.getOutputStream());
            }
            dataOutputStream.writeBytes(cmd + "\n");
            dataOutputStream.flush();
            LogUtils.d(TAG, "execShellCmd: flush");
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


    private static void setProperty(String key, String value) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, "unknown"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }

    public static void powerOff() {
        setProperty("sys.powerctl", "shutdown");
    }
}
