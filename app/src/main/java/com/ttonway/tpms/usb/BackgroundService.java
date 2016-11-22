package com.ttonway.tpms.usb;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.common.eventbus.Subscribe;
import com.ttonway.tpms.R;
import com.ttonway.tpms.utils.AlertHelper;
import com.ttonway.tpms.utils.MediaPlayerQueue;

/**
 * Created by ttonway on 2016/11/21.
 */
public class BackgroundService extends Service {
    private static final String TAG = BackgroundService.class.getSimpleName();

    public static final String ACTION_START = "com.ttonway.tpms.ACTION_START_TMPS";

    public static void startService(Context context) {
        context.startService(new Intent(ACTION_START));
    }

    public static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (info.service.getClassName().equals(BackgroundService.class.getName())) {
                return true;
            }
        }
        return false;
    }

    Handler mHandler;

    TpmsDevice device;


    @Subscribe
    public void onTimeout(TimeoutEvent e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showAlertMessage(getString(R.string.alert_message_usb_io_error));
            }
        });
    }

    @Subscribe
    public void onStatusUpdated(final TireStatusUpdatedEvent e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                if (e.tire != TpmsDevice.TIRE_NONE) {
                    TireStatus status = device.getTireStatus(e.tire);
                    AlertHelper helper = new AlertHelper(BackgroundService.this).process(e.tire, status);
                    if (helper.hasAlert()) {
                        for (Integer id : helper.getVoices()) {
                            MediaPlayerQueue.getInstance().addresource(id.intValue());
                        }

                        if (device.hasForegroundReceiver()) {
                            return;
                        }

                        StringBuilder sb = new StringBuilder();
                        for (Integer id : helper.getMessages()) {
                            sb.append(getString(id.intValue()));
                        }
                        showAlertMessage(sb.toString());
                    }
                }
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mHandler = new Handler();
        MediaPlayerQueue.getInstance().initialize(this);

        device = TpmsDevice.getInstance(this);
        device.openDevice();
        device.registerReceiver(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand " + intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        MediaPlayerQueue.getInstance().release();

        device.unregisterReceiver(this);
    }

    void showAlertMessage(String message) {
        final Dialog dialog = new Dialog(getApplicationContext(), R.style.CustomDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_custom, null, false);
        TextView textView = (TextView) view.findViewById(R.id.text1);
        textView.setText(message);
        view.findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.btn2).setVisibility(View.GONE);

        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new BitmapDrawable());
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
