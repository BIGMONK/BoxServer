package com.djf.remotecontrol;

import android.view.KeyEvent;

/**
 * Created by djf on 2017/11/28.
 */

public class CommandMsgBean {
    /**
     * 指令类型  按键 文字 设备   系统 其他
     */
    public static final int KEYEVENT = 1, TEXT = 2, DEVICE = 3,SYSTEM=4, OTHER = 5,POWER_CODE=888,REBOOT_CODE=999;
    private String MatchKey;
    private int type, keycode;
    private String msg, device, mac, ip;

    public static CommandMsgBean Home=new CommandMsgBean(SYSTEM,KeyEvent.KEYCODE_HOME,null,null,null);

    public static CommandMsgBean UP=new CommandMsgBean(KEYEVENT, KeyEvent.KEYCODE_DPAD_UP,null,null,null);
    public static CommandMsgBean DOWN=new CommandMsgBean(KEYEVENT, KeyEvent.KEYCODE_DPAD_DOWN,null,null,null);
    public static CommandMsgBean LEFT=new CommandMsgBean(KEYEVENT, KeyEvent.KEYCODE_DPAD_LEFT,null,null,null);
    public static CommandMsgBean RIGHT=new CommandMsgBean(KEYEVENT, KeyEvent.KEYCODE_DPAD_RIGHT,null,null,null);
    public static CommandMsgBean BACK=new CommandMsgBean(KEYEVENT, KeyEvent.KEYCODE_BACK,null,null,null);
    public static CommandMsgBean OK=new CommandMsgBean(KEYEVENT, KeyEvent.KEYCODE_DPAD_CENTER,null,null,null);

    public static CommandMsgBean VolUp=new CommandMsgBean(KEYEVENT, KeyEvent.KEYCODE_VOLUME_UP,null,null,null);
    public static CommandMsgBean VolDown=new CommandMsgBean(KEYEVENT, KeyEvent.KEYCODE_VOLUME_DOWN,null,null,null);

    public static CommandMsgBean PowerOff=new CommandMsgBean(OTHER,POWER_CODE,null,null,null);

    public static CommandMsgBean Reboot=new CommandMsgBean(OTHER,REBOOT_CODE,null,null,null);

    public CommandMsgBean(int type, String msg, String device, String mac, String ip) {
        this.MatchKey = ConstantConfig.KEY;
        this.type = type;
        this.msg = msg;
        this.device = device;
        this.mac = mac;
        this.ip = ip;
    }

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
