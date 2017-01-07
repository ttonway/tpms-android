package com.ttonway.tpms.core;

import android.content.Context;

import com.ttonway.tpms.BuildConfig;
import com.ttonway.tpms.bluetooth.BluetoothLeDriver;
import com.ttonway.tpms.usb.UsbDriver;

/**
 * Created by ttonway on 2017/1/7.
 */

public class DriverFactory {

    public static TpmsDriver newDriver(Context context, DriverCallback callback) {

        if (BuildConfig.FLAVOR.equals("bluetooth")) {
            return new BluetoothLeDriver(context, callback);
        } else if (BuildConfig.FLAVOR.equals("usb")) {
            return new UsbDriver(context, callback);
        }
        return null;
    }
}
