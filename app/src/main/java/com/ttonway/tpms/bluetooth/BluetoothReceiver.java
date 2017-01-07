package com.ttonway.tpms.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.ttonway.tpms.core.BackgroundService;
import com.ttonway.tpms.core.TpmsDevice;

/**
 * Created by ttonway on 2017/1/7.
 */

public class BluetoothReceiver extends BroadcastReceiver {
    private static final String TAG = BluetoothReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive " + intent);

        if (!BackgroundService.isServiceRunning(context)) {
            BackgroundService.startService(context);
        }

        if (TextUtils.equals(intent.getAction(), BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            if (state == BluetoothAdapter.STATE_ON) {
                TpmsDevice.getInstance(context).openDevice();
            } else if (state == BluetoothAdapter.STATE_OFF) {
                TpmsDevice.getInstance(context).closeDevice();
            }
        }
    }
}
