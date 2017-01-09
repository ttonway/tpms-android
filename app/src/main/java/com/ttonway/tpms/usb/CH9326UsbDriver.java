package com.ttonway.tpms.usb;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.ttonway.tpms.R;
import com.ttonway.tpms.TpmsApp;
import com.ttonway.tpms.core.DriverCallback;
import com.ttonway.tpms.core.TpmsDriver;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;
import cn.wch.ch9326driver.CH9326UARTDriver;

/**
 * Created by ttonway on 2017/1/2.
 */
public class CH9326UsbDriver extends TpmsDriver {
    private static final String TAG = CH9326UsbDriver.class.getSimpleName();

    private static final String ACTION_USB_PERMISSION = "cn.wch.CH9326UARTDemoDriver.USB_PERMISSION";

    CH9326UARTDriver mDriver;
    ReadThread mReadThread;

    public CH9326UsbDriver(Context context, DriverCallback callback) {
        super(context, callback);
        TpmsApp.driver2 = new CH9326UARTDriver(
                (UsbManager) context.getSystemService(Context.USB_SERVICE), context.getApplicationContext(),
                ACTION_USB_PERMISSION);
        this.mDriver = TpmsApp.driver2;
    }

    @Override
    public String isDriverSupported() {
        if (!mDriver.UsbFeatureSupported()) {// 判断系统是否支持USB HOST
            return mContext.getString(R.string.alert_message_usb_host_unavailable);
        }
        return null;
    }

    @Override
    public boolean openDevice() {
        Log.d(TAG, "openDevice");
        if (mState == STATE_OPEN) {
            Log.e(TAG, "Already opened.");
            return true;
        }

        // ResumeUsbList方法用于枚举CH34X设备以及打开相关设备
        if (!mDriver.ResumeUsbList()) {
            Log.e(TAG, "ResumeUsbList fail.");
            mDriver.CloseDevice();
            return false;
        }


        /**
         * baudRate :01 = 300bps,02 = 600bps,03 = 1200bps,04 = 2400bps,05 = 4800bps,06 =
         * 9600(default)bps,07 = 14400bps,08 = 19200bps,09 = 28800bps,10 = 38400bps,11 = 57600bps,12 = 76800bps,13 = 115200bps
         *
         * dataBits :01 = 5bit data bit,02 = 6bit data bit,03 = 7bit data bit,04 = 8bit data bit(default)
         *
         * stopBits :01 = 1bit stop bit(default),02 = 2bit stop bit
         *
         * parity :01 = odd,02 = even,03 = space,04 = none(default)
         */
        int baudRate = 6;
        int dataBit = 4;
        int stopBit = 1;
        int parity = 4;
        if (!mDriver.SetConfig(baudRate, dataBit, stopBit, parity)) {
            Log.e(TAG, "SetConfig fail.");
            return false;
        }

        this.mReadThread = new ReadThread();
        this.mReadThread.start();

        setState(STATE_OPEN);

        return true;
    }

    @Override
    public boolean closeDevice() {
        if (mState == STATE_OPEN) {
            mReadThread.stopRunning();
            mReadThread = null;

            mDriver.CloseDevice();

            setState(STATE_CLOSE);
        }

        return true;
    }

    @Override
    public int WriteData(byte[] buf, int length) {
        return mDriver.WriteData(buf, length);
    }


    private class ReadThread extends Thread {

        private boolean mRunning = true;

        public void run() {
            byte[] buffer = new byte[64];

            while (true) {
                if (!mRunning) {
                    break;
                }

                int length = mDriver.ReadData(buffer, 64);
                if (length > 0) {
                    mCallback.onReadData(buffer, length);
                }
            }
        }

        public void stopRunning() {
            mRunning = false;
        }
    }


}
