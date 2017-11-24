package com.djf.remotecontrol;

/**
 * Created by djf on 2017/11/24.
 */

public class ServerDevice {
    String name, ip,mac;

    public ServerDevice(String name, String ip,String mac) {
        this.name = name;
        this.mac=mac;
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "ServerDevice{" +
                "name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", mac='" + mac + '\'' +
                '}';
    }
}
