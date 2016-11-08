package com.ttonway.tpms.usb;

/**
 * Created by ttonway on 2016/11/7.
 */
public interface TpmsCallback {

    void onTimeout();

    void onSettingsChanged();

    void onStatusUpdate();

    void onTireMatched(byte tire, String value);
}
