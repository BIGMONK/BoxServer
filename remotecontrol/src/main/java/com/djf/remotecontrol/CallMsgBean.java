package com.djf.remotecontrol;

/**
 * Created by djf on 2017/11/24.
 */

public class CallMsgBean {
    public static String MatchKey="youtu";
    /**
     * 1  搜索请求  2  搜索响应  3  客户端控制指令
     */
    private int type;
    private String keyMatch;
    private String callIp;
    private String callDevice;
    private String callMac;
    private int keyCode;

    public CallMsgBean(int type, String callIp, String callDevice, String callMac, int keyCode) {
        this.keyMatch =MatchKey;
        this.type = type;
        this.callIp = callIp;
        this.callDevice = callDevice;
        this.callMac = callMac;
        this.keyCode = keyCode;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getKeyMatch() {
        return keyMatch;
    }

    public void setKeyMatch(String keyMatch) {
        this.keyMatch = keyMatch;
    }

    public String getCallIp() {
        return callIp;
    }

    public void setCallIp(String callIp) {
        this.callIp = callIp;
    }

    public String getCallDevice() {
        return callDevice;
    }

    public void setCallDevice(String callDevice) {
        this.callDevice = callDevice;
    }

    public String getCallMac() {
        return callMac;
    }

    public void setCallMac(String callMac) {
        this.callMac = callMac;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    @Override
    public String toString() {
        return "CallMsgBean{" +
                "type=" + type +
                ", keyMatch='" + keyMatch + '\'' +
                ", callIp='" + callIp + '\'' +
                ", callDevice='" + callDevice + '\'' +
                ", callMac='" + callMac + '\'' +
                ", keyCode=" + keyCode +
                '}';
    }
}
