package com.ttonway.tpms.core;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.ttonway.tpms.BuildConfig;
import com.ttonway.tpms.bluetooth.BluetoothLeDriver;
import com.ttonway.tpms.usb.CH34xUsbDriver;
import com.ttonway.tpms.usb.CH9326UsbDriver;

import java.util.HashMap;

/**
 * Created by ttonway on 2017/1/7.
 */

public class DriverFactory {
    private static final String TAG = DriverFactory.class.getSimpleName();


//    private static int mUSBDriverVer;

    public static TpmsDriver newDriver(Context context, DriverCallback callback) {

        if (BuildConfig.FLAVOR.startsWith("bluetooth")) {
            return new BluetoothLeDriver(context, callback);
        } else if (BuildConfig.FLAVOR.startsWith("usb34x")) {
            return new CH34xUsbDriver(context, callback);
        } else if (BuildConfig.FLAVOR.startsWith("usb9326")) {
//            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
//            HashMap<String, UsbDevice> map = usbManager.getDeviceList();
//            for (UsbDevice device : map.values()) {
//                Log.i(TAG, "dName: " + device.getDeviceName());
//                Log.i(TAG, "vid: " + device.getVendorId() + " pid: " + device.getProductId());
//                if (device.getVendorId() == 6790) {
//                    if (device.getProductId() == 29987 || device.getProductId() == 21795) {
//                        mUSBDriverVer = 1;
//                        return new CH34xUsbDriver(context, callback);
//                    } else if (device.getProductId() == 57360) {
//                        mUSBDriverVer = 2;
//                        return new CH9326UsbDriver(context, callback);
//                    }
//                }
//            }
//            mUSBDriverVer = 2;
            return new CH9326UsbDriver(context, callback);
        }
        return null;
    }

//    public static boolean needChangeDriver(Context context) {
//        if (BuildConfig.FLAVOR.equals("usb")) {
//            int v = 2;
//            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
//            HashMap<String, UsbDevice> map = usbManager.getDeviceList();
//            for (UsbDevice device : map.values()) {
//                Log.i(TAG, "dName: " + device.getDeviceName());
//                Log.i(TAG, "vid: " + device.getVendorId() + " pid: " + device.getProductId());
//                if (device.getVendorId() == 6790) {
//                    if (device.getProductId() == 29987 || device.getProductId() == 21795) {
//                        v = 1;
//                    } else if (device.getProductId() == 57360) {
//                        v = 2;
//                    }
//                }
//            }
//            if (v != mUSBDriverVer) {
//                return true;
//            } else {
//                return false;
//            }
//        } else {
//            return false;
//        }
//    }
}
