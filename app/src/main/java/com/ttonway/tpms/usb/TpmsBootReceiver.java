package com.ttonway.tpms.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by ttonway on 2016/11/21.
 */
public class TpmsBootReceiver extends BroadcastReceiver {
    private static final String TAG = TpmsBootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive " + intent);

        if (!BackgroundService.isServiceRunning(context)) {
            BackgroundService.startService(context);
        }
    }
}
