package com.ttonway.tpms.core;

/**
 * Created by ttonway on 2017/1/2.
 */
public interface DriverCallback {

    void onStateChanged(int state);

    void onError(int error);

    void onReadData(byte[] buf, int length);
}
