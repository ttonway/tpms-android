package com.ttonway.tpms.core;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.common.eventbus.Subscribe;
import com.ttonway.tpms.R;
import com.ttonway.tpms.ui.MainActivity;
import com.ttonway.tpms.utils.AlertHelper;
import com.ttonway.tpms.utils.MediaPlayerQueue;
import com.ttonway.tpms.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ttonway on 2016/11/21.
 */
public class BackgroundService extends Service {
    private static final String TAG = BackgroundService.class.getSimpleName();

    public static final String ACTION_START = "com.ttonway.tpms.ACTION_START_TMPS";

    public static void startService(Context context) {
        Intent intent = new Intent(context, BackgroundService.class);
        context.startService(intent);
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
    NotificationManager mNotificationManager;

    TpmsDevice device;

    List<Dialog> mTimeoutDialogs = new ArrayList<>();


    @Subscribe
    public void onTimeout(final ErrorEvent e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (e.command == TpmsDevice.CMD_QUERY_SETTING && !Utils.isAppOnForeground(BackgroundService.this)) {
                    return;
                }
                final Dialog dialog = showAlertMessage(getString(R.string.alert_message_usb_io_error));
                if (e.command == TpmsDevice.CMD_QUERY_SETTING) {
                    Log.d(TAG, "add timeout dialog " + dialog);
                    mTimeoutDialogs.add(dialog);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mTimeoutDialogs.remove(dialog);
                            dialog.dismiss();
                        }
                    }, 3000);
                }
            }
        });
    }

    @Subscribe
    public void onSettingChanged(final SettingChangeEvent e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (e.command == TpmsDevice.CMD_QUERY_SETTING) {
                    Log.d(TAG, "dismiss timeout dialogs " + mTimeoutDialogs);
                    for (Dialog dialog : mTimeoutDialogs) {
                        dialog.dismiss();
                    }
                    mTimeoutDialogs.clear();
                }
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

                        if (Utils.isAppOnForeground(BackgroundService.this)) {
                            return;
                        }

                        StringBuilder sb = new StringBuilder();
                        for (Integer id : helper.getMessages()) {
                            sb.append(BackgroundService.this.getString(id.intValue()));
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
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        MediaPlayerQueue.getInstance().initialize(this);

        device = TpmsDevice.getInstance(this);
        device.openDevice();
        device.registerReceiver(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand " + intent);

        Utils.setupLocale(this);

        startForeground(R.id.service_notification, getNotification());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        MediaPlayerQueue.getInstance().release();

        device.unregisterReceiver(this);

        stopForeground(true);
    }

    Notification getNotification() {
        String text = getString(R.string.notification_service_running);
        Intent notificationIntent = new Intent(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setClass(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setContentIntent(pendingIntent);
        return builder.build();
    }

    Dialog showAlertMessage(String message) {
        Log.d(TAG, "showAlertMessage " + message);
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_custom, null, false);
        TextView textView = (TextView) view.findViewById(R.id.text1);
        textView.setText(message);
        view.findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setClass(BackgroundService.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                startActivity(intent);
            }
        });
        view.findViewById(R.id.btn2).setVisibility(View.GONE);

        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new BitmapDrawable());
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();


//        Intent notificationIntent = new Intent(Intent.ACTION_MAIN);
//        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        notificationIntent.setClass(this, MainActivity.class);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        builder.setSmallIcon(R.mipmap.ic_launcher)
//                .setTicker(message)
//                .setWhen(System.currentTimeMillis())
//                .setContentTitle(getString(R.string.app_name))
//                .setContentText(message)
//                .setContentIntent(pendingIntent);
//        mNotificationManager.notify(R.id.service_notification, builder.build());
        return dialog;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
