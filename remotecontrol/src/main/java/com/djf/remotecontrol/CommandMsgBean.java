package com.djf.remotecontrol;

import android.view.KeyEvent;

/**
 * Created by djf on 2017/11/28.
 */

public class CommandMsgBean {
    /**
     * 指令类型  按键 文字 设备   系统 其他
     */
    public static final int KEYEVENT = 1, TEXT = 2, DEVICE = 3, SYSTEM = 4, OTHER = 5,
            POWER_CODE = 888, REBOOT_CODE = 999, RES_UP = 10, RES_DOWN = 11,PING=12,PANG=13;
    private String MatchKey;
    private int type, keycode;
    private String msg, device, mac, ip;

    public static final int DEVICE_FAMILY=31,DEVICE_COACH=32,DEVICE_BOAT=33,DEVICE_UNKNOWN=0;

    public static CommandMsgBean Home = new CommandMsgBean(SYSTEM, KeyEvent.KEYCODE_HOME, null,
            null, null);

    public static CommandMsgBean UP = new CommandMsgBean(KEYEVENT, KeyEvent.KEYCODE_DPAD_UP,
            null, null, null), DOWN = new CommandMsgBean(KEYEVENT, KeyEvent.KEYCODE_DPAD_DOWN,
            null, null, null), LEFT = new CommandMsgBean(KEYEVENT, KeyEvent.KEYCODE_DPAD_LEFT,
            null, null, null), RIGHT = new CommandMsgBean(KEYEVENT, KeyEvent.KEYCODE_DPAD_RIGHT,
            null, null, null), BACK = new CommandMsgBean(KEYEVENT, KeyEvent.KEYCODE_BACK, null,
            null, null), OK = new CommandMsgBean(KEYEVENT, KeyEvent.KEYCODE_DPAD_CENTER, null,
            null, null), VolUp = new CommandMsgBean(KEYEVENT, KeyEvent.KEYCODE_VOLUME_UP,
            null, null, null), VolDown = new CommandMsgBean(KEYEVENT, KeyEvent
            .KEYCODE_VOLUME_DOWN, null, null, null);

    public static CommandMsgBean PowerOff = new CommandMsgBean(OTHER, POWER_CODE, null, null,
            null), Reboot = new CommandMsgBean(OTHER, REBOOT_CODE, null, null, null), ResUp = new
            CommandMsgBean(OTHER, RES_UP, null, null, null), ResDown = new CommandMsgBean(OTHER,
            RES_DOWN, null, null, null),BeatPing = new CommandMsgBean(OTHER,
            PING, null, null, null),BeatPang = new CommandMsgBean(OTHER,
            PANG, null, null, null);


    public CommandMsgBean(int type, int keycode, String device, String mac, String ip) {
        this.MatchKey = ConstantConfig.KEY;
        this.type = type;
        this.keycode = keycode;
        this.device = device;
        this.mac = mac;
        this.ip = ip;
    }

    public int getKeycode() {
        return keycode;
    }

    public void setKeycode(int keycode) {
        this.keycode = keycode;
    }

    public String getMatchKey() {
        return MatchKey;
    }

    public void setMatchKey(String matchKey) {
        this.MatchKey = matchKey;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "CommandMsgBean{" +
                "MatchKey='" + MatchKey + '\'' +
                ", type=" + type +
                ", keycode=" + keycode +
                ", msg='" + msg + '\'' +
                ", device='" + device + '\'' +
                ", mac='" + mac + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
