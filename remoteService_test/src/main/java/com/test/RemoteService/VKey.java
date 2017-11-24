package com.test.RemoteService;

import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;

import android.hardware.input.InputManager;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.os.SystemClock;

public class VKey {

    public static boolean sendKeyEventToAndroid(int userKey) {
        Log.d(TAG, "sendKeyEventToAndroid() start");
        boolean result = false;

        if (userKey != -1) {
            //TODO: 以下代码必须依存系统源码编译
        final KeyEvent evd = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, userKey, 0,
                0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_FROM_SYSTEM | KeyEvent.FLAG_VIRTUAL_HARD_KEY,
                InputDevice.SOURCE_KEYBOARD);
        final KeyEvent evu = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, userKey, 0,
                0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_FROM_SYSTEM | KeyEvent.FLAG_VIRTUAL_HARD_KEY,
                InputDevice.SOURCE_KEYBOARD);
            InputManager.getInstance().injectInputEvent(evd,
            InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
            InputManager.getInstance().injectInputEvent(evu,
            InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
            // mIWindowManager.injectKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, key), false);
            // mIWindowManager.injectKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, key), false);
            result = true;
        }

        Log.d(TAG, "sendKeyEventToAndroid() end");

        return result;
    }


  
    /**
     * @brief 日志标记。
     */
    private static final String TAG = VKey.class.getSimpleName();
}
