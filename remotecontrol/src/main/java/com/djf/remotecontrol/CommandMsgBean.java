package com.djf.remotecontrol;

/**
 * Created by djf on 2017/11/28.
 */

public class CommandMsgBean {
    public static final int KEYEVENT = 1, TEXT = 2, DEVICE = 3, OTHER = 4;
    private String MatchKey;
    private int type, keycode;
    private String msg, device, mac, ip;

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
