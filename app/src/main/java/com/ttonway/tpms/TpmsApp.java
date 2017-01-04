package com.ttonway.tpms;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.ttonway.tpms.usb.BackgroundService;
import com.ttonway.tpms.usb.TpmsDevice;
import com.ttonway.tpms.utils.Utils;

import java.io.File;
import java.io.IOException;

import cn.wch.ch9326driver.CH9326UARTDriver;

public class TpmsApp extends Application {
    private static final String TAG = TpmsApp.class.getSimpleName();

    public static CH9326UARTDriver driver;

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive " + intent);

            if (!BackgroundService.isServiceRunning(context)) {
                BackgroundService.startService(context);
            }
        }
    };

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive " + intent);

            if (intent != null && UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
                TpmsDevice.getInstance(context).openDevice();
            } else if (intent != null && UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
                TpmsDevice.getInstance(context).closeDevice();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate " + Utils.getAppVersion(this));

        saveLogcatToFile(this);

        Utils.setupLocale(this);

        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=58228f78");

        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        registerReceiver(mReceiver, filter);

        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, usbFilter);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        TpmsDevice.getInstance(this).closeDevice();

        unregisterReceiver(mReceiver);
        unregisterReceiver(mUsbReceiver);
    }

    public static void saveLogcatToFile(Context context) {
        String fileName = "logcat_" + System.currentTimeMillis() + ".txt";
        File outputFile = new File(context.getExternalCacheDir(), fileName);
        Log.i(TAG, "save logcat to " + outputFile.getAbsolutePath());
        try {
            @SuppressWarnings("unused")
            Process process = Runtime.getRuntime().exec("logcat -f " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "saveLogcatToFile fail.", e);
        }
    }
}
