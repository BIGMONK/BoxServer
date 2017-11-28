package com.djf.remotecontrol.server;

import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.djf.remotecontrol.RemoteUtils;

import java.lang.reflect.Method;

/**
 * Created by djf on 2017/11/27.
 */
public class MonitorInput {
    private static Method mInjectMethod;
    private static InputManager mInputManager;
    private static MonitorInput mInstance = new MonitorInput();


    public static MonitorInput getInstance() {
        if (mInputManager == null) {
            init();
        }
        return mInstance;
    }

    private static boolean init() {
        try {
            Class[] arrayOfClass = new Class[2];
            arrayOfClass[0] = InputEvent.class;
            arrayOfClass[1] = Integer.TYPE;
            mInjectMethod = InputManager.class.getDeclaredMethod("injectInputEvent", arrayOfClass);
            mInputManager = ((InputManager) InputManager.class.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]));
            return true;
        } catch (Exception localException) {
        }
        return false;
    }

    private static void injectKeyEvent(InputEvent paramInputEvent) {
        try {
            Method localMethod = mInjectMethod;
            InputManager localInputManager = mInputManager;
            Object[] arrayOfObject = new Object[2];
            arrayOfObject[0] = paramInputEvent;
            arrayOfObject[1] = Integer.valueOf(2);
            localMethod.invoke(localInputManager, arrayOfObject);
            return;
        } catch (Exception localException) {
        }
    }


    //不能跨进程：当程序后台运行时无效
    public void sendKeyDownUpEvent(int paramInt) {
        long l = SystemClock.uptimeMillis();
        injectKeyEvent(new KeyEvent(l, l, KeyEvent.ACTION_DOWN, paramInt, 0, 0, -1, 0, 0, 257));
        try {
            Thread.sleep(50L);
            injectKeyEvent(new KeyEvent(l, l, KeyEvent.ACTION_UP, paramInt, 0, 0, -1, 0, 0, 257));
            return;
        } catch (InterruptedException localInterruptedException) {
            localInterruptedException.printStackTrace();
            RemoteUtils.execShellCmd("input keyevent " + paramInt);
        }
    }

    public void sendLongPressEvent(int paramInt1, int paramInt2) {
        long l = SystemClock.uptimeMillis();
        injectKeyEvent(new KeyEvent(l, l, paramInt2, paramInt1, 0, 0, -1, 0, 0, 257));
    }

    public void sendPointerDownUpEvent(int paramInt1, int paramInt2) {
        MotionEvent localMotionEvent1 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 0, paramInt1, paramInt2, 0);
        localMotionEvent1.setSource(4098);
        injectKeyEvent(localMotionEvent1);
        MotionEvent localMotionEvent2 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), 1, paramInt1, paramInt2, 0);
        localMotionEvent2.setSource(4098);
        injectKeyEvent(localMotionEvent2);
    }
}
