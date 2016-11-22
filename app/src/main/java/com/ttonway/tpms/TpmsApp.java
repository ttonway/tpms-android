package com.ttonway.tpms;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.ttonway.tpms.usb.BackgroundService;
import com.ttonway.tpms.usb.TpmsDevice;

import java.io.File;
import java.io.IOException;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

public class TpmsApp extends Application {
    private static final String TAG = TpmsApp.class.getSimpleName();

    public static CH34xUARTDriver driver;// 需要将CH34x的驱动类写在APP类下面，使得帮助类的生命周期与整个应用程序的生命周期是相同的

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive " + intent);

            if (!BackgroundService.isServiceRunning(context)) {
                BackgroundService.startService(context);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        saveLogcatToFile(this);

        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=58228f78");

        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        TpmsDevice.getInstance(this).closeDevice();

        unregisterReceiver(mReceiver);
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
