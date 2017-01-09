package com.ttonway.tpms;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.ttonway.tpms.core.BackgroundService;
import com.ttonway.tpms.core.TpmsDevice;
import com.ttonway.tpms.utils.Utils;

import java.io.File;
import java.io.IOException;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;
import cn.wch.ch9326driver.CH9326UARTDriver;

public class TpmsApp extends Application {
    private static final String TAG = TpmsApp.class.getSimpleName();

    // 需要将CH34x的驱动类写在APP类下面，使得帮助类的生命周期与整个应用程序的生命周期是相同的
    public static CH34xUARTDriver driver1;
    public static CH9326UARTDriver driver2;

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
        Log.i(TAG, "onCreate");

        saveLogcatToFile(this);

        Log.i(TAG, "App Version: " + Utils.getAppVersion(this) + "(" + Utils.getAppVersionCode(this) + ")");
        Log.i(TAG, "BuildConfig: {applicationId=" + BuildConfig.APPLICATION_ID + ", buildType=" + BuildConfig.BUILD_TYPE + ", flavor=" + BuildConfig.FLAVOR
                + ", debug=" + BuildConfig.DEBUG + ", versionName=" + BuildConfig.VERSION_NAME + ", versionCode=" + BuildConfig.VERSION_CODE + "}");
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        Log.i(TAG, "DisplayMetrics: " + metrics);

        Utils.setupLocale(this);

        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        registerReceiver(mReceiver, filter);

        if (TpmsDevice.getInstance(this).isUSBEnabled()) {
            IntentFilter usbFilter = new IntentFilter();
            usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            registerReceiver(mUsbReceiver, usbFilter);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        TpmsDevice.getInstance(this).closeDevice();

        unregisterReceiver(mReceiver);
        if (TpmsDevice.getInstance(this).isUSBEnabled()) {
            unregisterReceiver(mUsbReceiver);
        }
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
