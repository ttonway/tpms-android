package com.ttonway.tpms.usb;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.ttonway.tpms.R;
import com.ttonway.tpms.TpmsApp;
import com.ttonway.tpms.core.DriverCallback;
import com.ttonway.tpms.core.TpmsDriver;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

/**
 * Created by ttonway on 2017/1/2.
 */
public class UsbDriver extends TpmsDriver {
    private static final String TAG = UsbDriver.class.getSimpleName();

    private static final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";

    CH34xUARTDriver mDriver;
    ReadThread mReadThread;

    public UsbDriver(Context context, DriverCallback callback) {
        super(context, callback);
        TpmsApp.driver = new CH34xUARTDriver(
                (UsbManager) context.getSystemService(Context.USB_SERVICE), context.getApplicationContext(),
                ACTION_USB_PERMISSION);
        this.mDriver = TpmsApp.driver;
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

        //对串口设备进行初始化操作
        if (!mDriver.UartInit()) {
            Log.e(TAG, "UartInit fail.");
            return false;
        }

        //配置串口波特率，函数说明可参照编程手册
        int baudRate = 9600;
        byte dataBit = 8;
        byte stopBit = 1;
        byte parity = 0;
        byte flowControl = 0;

        if (!mDriver.SetConfig(baudRate, dataBit, stopBit, parity, flowControl)) {
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
