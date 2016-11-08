package com.ttonway.tpms;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

public class MyApp extends Application {
    private static final String TAG = MyApp.class.getSimpleName();

    public static CH34xUARTDriver driver;// 需要将CH34x的驱动类写在APP类下面，使得帮助类的生命周期与整个应用程序的生命周期是相同的

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        saveLogcatToFile(this);
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
