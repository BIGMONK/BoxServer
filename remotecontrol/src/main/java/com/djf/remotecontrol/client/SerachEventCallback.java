package com.djf.remotecontrol.client;

/**
 * Created by djf on 2017/11/24.
 */


public interface SerachEventCallback<T> {
    public int ServiceStart=1,
    Searching=4,
    StartSearch=2,
    EndSearch=3;
    void onEvent(T t);
}

