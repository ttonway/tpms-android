package com.ttonway.tpms.usb;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.common.eventbus.EventBus;
import com.ttonway.tpms.TpmsApp;
import com.ttonway.tpms.SPManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

/**
 * Created by ttonway on 2016/11/7.
 */
public class TpmsDevice {
    private static final String TAG = TpmsDevice.class.getSimpleName();

    private static final String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";

    public static final byte TIRE_NONE = Byte.MIN_VALUE;
    public static final byte TIRE_LEFT_FRONT = 0;
    public static final byte TIRE_RIGHT_FRONT = 1;
    public static final byte TIRE_RIGHT_END = 2;
    public static final byte TIRE_LEFT_END = 3;

    private static TpmsDevice instance = null;

    CH34xUARTDriver mDriver;
    EventBus mEventBus;
    SharedPreferences mPreferences;

    final List<Object> mReceivers = new ArrayList<>();

    Handler mHandler = new Handler();
    ReadThread mReadThread;
    boolean mOpen;

    final List<WriteCommand> mCommands = new ArrayList<>();

    public float mPressureLowLimit;
    public float mPressureHighLimit;
    public int mTemperatureLimit;
    public TireStatus mLeftFront = new TireStatus("left-front");
    public TireStatus mRightFront = new TireStatus("right-front");
    public TireStatus mRightEnd = new TireStatus("right-end");
    public TireStatus mLeftEnd = new TireStatus("left-end");

    public static synchronized TpmsDevice getInstance(Context context) {
        if (instance == null) {
            instance = new TpmsDevice(context);
        }
        return instance;
    }

    private TpmsDevice(Context context) {
        TpmsApp.driver = new CH34xUARTDriver(
                (UsbManager) context.getSystemService(Context.USB_SERVICE), context.getApplicationContext(),
                ACTION_USB_PERMISSION);
        this.mDriver = TpmsApp.driver;
        this.mEventBus = new EventBus();
        this.mPreferences = context.getSharedPreferences("device", Context.MODE_PRIVATE);

        mPressureLowLimit = mPreferences.getFloat("pressure-low-limit", SPManager.PRESSURE_LOWER_LIMIT_DEFAULT);
        mPressureHighLimit = mPreferences.getFloat("pressure-high-limit", SPManager.PRESSURE_UPPER_LIMIT_DEFAULT);
        mTemperatureLimit = mPreferences.getInt("temperature-high-limit", SPManager.TEMP_UPPER_LIMIT_DEFAULT);
        readTireStatus(mPreferences, mLeftFront);
        readTireStatus(mPreferences, mRightFront);
        readTireStatus(mPreferences, mRightEnd);
        readTireStatus(mPreferences, mLeftEnd);
    }

    void readTireStatus(SharedPreferences sp, TireStatus status) {
        String prefix = status.name;
        if (sp.contains(prefix + "-pressure-status")) {
            status.inited = true;
        } else {
            status.inited = false;
            return;
        }
        status.pressureStatus = sp.getInt(prefix + "-pressure-status", 0);
        status.batteryStatus = sp.getInt(prefix + "-battery-status", 0);
        status.temperatureStatus = sp.getInt(prefix + "-temperature-status", 0);
        status.pressure = sp.getFloat(prefix + "-pressure", 0.f);
        status.battery = sp.getInt(prefix + "-battery", 0);
        status.temperature = sp.getInt(prefix + "-temperature", 0);
    }

    void writeTireStatus(SharedPreferences sp, TireStatus status) {
        if (status.inited) {
            String prefix = status.name;
            sp.edit()
                    .putInt(prefix + "-pressure-status", status.pressureStatus)
                    .putInt(prefix + "-battery-status", status.batteryStatus)
                    .putInt(prefix + "-temperature-status", status.temperatureStatus)
                    .putFloat(prefix + "-pressure", status.pressure)
                    .putInt(prefix + "-battery", status.battery)
                    .putInt(prefix + "-temperature", status.temperature)
                    .apply();
        }
    }

    public void registerReceiver(Object object) {
        mReceivers.add(object);
        mEventBus.register(object);
    }

    public void unregisterReceiver(Object object) {
        mReceivers.remove(object);
        mEventBus.unregister(object);
    }

    public boolean hasForegroundReceiver() {
        for (Object obj : mReceivers) {
            if (obj instanceof Activity || obj instanceof Fragment) {
                return true;
            }
        }
        return false;
    }

    public boolean openDevice() {
        Log.d(TAG, "openDevice");
        if (mOpen) {
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
        mOpen = true;
        return true;
    }

    public void closeDevice() {
        Log.d(TAG, "closeDevice");
        if (mOpen) {
            mReadThread.stopRunning();
            mReadThread = null;

            mDriver.CloseDevice();
            mOpen = false;
        }
    }

    public boolean isOpen() {
        return mOpen;
    }

    private byte makeParity(byte[] buf, int length) {
        byte parity = 0;
        for (int i = 0; i < length; i++) {
            parity = (byte) (parity ^ buf[i]);
        }
        return parity;
    }

    private boolean writeData(byte cmd, byte[] data) {
        byte[] buf = new byte[64];
        int index = 0;
        buf[index++] = (byte) 0xFC;
        buf[index++] = (byte) (1 + data.length + 2);
        buf[index++] = cmd;
        for (byte b : data) {
            buf[index++] = b;
        }
        buf[index++] = makeParity(buf, index);
        buf[index++] = (byte) 0xAA;

        Log.d(TAG, "[WriteData] " + toHexString(buf, index));
        int retval = mDriver.WriteData(buf, index);
        return retval >= 0;
    }

    public TireStatus getTireStatus(byte tire) {
        switch (tire) {
            case TIRE_LEFT_FRONT:
                return mLeftFront;
            case TIRE_RIGHT_FRONT:
                return mRightFront;
            case TIRE_RIGHT_END:
                return mRightEnd;
            case TIRE_LEFT_END:
                return mLeftEnd;
            default:
                return null;
        }
    }

    private void postCommand(WriteCommand command) {
        synchronized (mCommands) {
            mCommands.add(command);
        }
        mHandler.post(command);
    }

    private void removeCommand(WriteCommand command) {
        synchronized (mCommands) {
            mCommands.remove(command);
        }
        mHandler.removeCallbacks(command);
    }

    private void removeCommands(byte cmd, byte[] data) {
        synchronized (mCommands) {
            Iterator<WriteCommand> iter = mCommands.iterator();
            while (iter.hasNext()) {
                WriteCommand c = iter.next();
                if (c.command == cmd && Arrays.equals(c.data, data)) {
                    iter.remove();
                    mHandler.removeCallbacks(c);
                }
            }
        }
    }

    public void startTireMatch(byte tire) {
        postCommand(new WriteCommand((byte) 0x02, new byte[]{tire}));
    }

    public void stopTireMatch() {
        postCommand(new WriteCommand((byte) 0x03, new byte[0]));
    }

    public void exchangeTire(byte tire1, byte tire2) {
        postCommand(new WriteCommand((byte) 0x05, new byte[]{tire1, tire2}));
    }

    public void saveSettings(float lowLimit, float highLimit, int tempLimit) {
        byte[] buf = new byte[3];
        buf[0] = (byte) (lowLimit / 0.1f);
        buf[1] = (byte) (highLimit / 0.1f);
        buf[2] = (byte) tempLimit;
        postCommand(new WriteCommand((byte) 0x06, buf));
    }

    public void querySettings() {
        postCommand(new WriteCommand((byte) 0x07, new byte[0]));
    }

    private void onReadData(byte[] buf, int length) {
        if (length < 5) {
            Log.e(TAG, "[onReadData] length too short.");
            return;
        }
        if (buf[0] != (byte) 0xFC || buf[length - 1] != (byte) 0xAA) {
            Log.e(TAG, "[onReadData] wrong tags.");
            return;
        }
        if (buf[1] != length - 2) {
            Log.e(TAG, "[onReadData] wrong frame length.");
            return;
        }
        byte parity = makeParity(buf, length - 2);
        if (buf[length - 2] != parity) {
            Log.e(TAG, "[onReadData] wrong parity.");
            return;
        }

        byte cmd = buf[2];
        byte[] data = Arrays.copyOfRange(buf, 3, length - 2);
        switch (cmd) {
            // 数据帧
            case 0x01: {
                byte tire = data[0];
                byte alarm = data[1];
                byte temp = data[2];
                byte pressure = data[3];
                byte battery = data[4];
                TireStatus status = getTireStatus(tire);
                status.inited = true;
                status.pressureStatus = alarm & 0x03;
                status.batteryStatus = (alarm >> 2) & 0x01;
                status.temperatureStatus = (alarm >> 3) & 0x01;
                status.pressure = 0.1f * (int) pressure;
                status.temperature = (int) temp;
                if (status.temperature > 100) {
                    status.temperature = 0;
                }
                status.battery = 100 * (int) battery;

                writeTireStatus(mPreferences, status);

                mEventBus.post(new TireStatusUpdatedEvent(tire));
                break;
            }
            // 学习配对(ACK)
            case 0x02: {
                removeCommands(cmd, data);
                break;
            }
            // 退出学习配对(ACK)
            case 0x03: {
                removeCommands(cmd, data);
                break;
            }
            // 学习成功
            case 0x04: {
                byte tire = data[0];
                byte[] array = Arrays.copyOfRange(data, 1, 5);
                String str = toHexString(array, 4);

                mEventBus.post(new TireMatchedEvent(tire, str));
                break;
            }
            // 换轮模式(ACK)
            case 0x05: {
                byte tire1 = data[0];
                byte tire2 = data[1];
                TireStatus status1 = getTireStatus(tire1);
                TireStatus status2 = getTireStatus(tire2);
                TireStatus statusTemp = new TireStatus(null);
                statusTemp.setValues(status1);
                status1.setValues(status2);
                status2.setValues(statusTemp);
                removeCommands(cmd, data);

                writeTireStatus(mPreferences, status1);
                writeTireStatus(mPreferences, status2);

                mEventBus.post(new TireStatusUpdatedEvent(TpmsDevice.TIRE_NONE));
                break;
            }
            // 参数设置(ACK)
            case 0x06: {
                mPressureLowLimit = 0.1f * (int) data[0];
                mPressureHighLimit = 0.1f * (int) data[1];
                mTemperatureLimit = (int) data[2];
                removeCommands(cmd, data);

                mPreferences.edit()
                        .putFloat("pressure-low-limit", mPressureLowLimit)
                        .putFloat("pressure-high-limit", mPressureHighLimit)
                        .putInt("temperature-high-limit", mTemperatureLimit)
                        .apply();

                mEventBus.post(new SettingChangeEvent());
                break;
            }
            // 查询应答
            case 0x08: {
                mPressureLowLimit = 0.1f * (int) data[0];
                mPressureHighLimit = 0.1f * (int) data[1];
                mTemperatureLimit = (int) data[2];
                removeCommands((byte) 0x07, new byte[0]);

                mPreferences.edit()
                        .putFloat("pressure-low-limit", mPressureLowLimit)
                        .putFloat("pressure-high-limit", mPressureHighLimit)
                        .putInt("temperature-high-limit", mTemperatureLimit)
                        .apply();

                mEventBus.post(new SettingChangeEvent());
                break;
            }
            default: {
                Log.e(TAG, "Unhandled frame.");
                break;
            }
        }
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
                    String recv = toHexString(buffer, length);
                    Log.d(TAG, "[ReadData] " + recv);
                    try {
                        onReadData(buffer, length);
                    } catch (Exception e) {
                        Log.e(TAG, "onReadData fail.", e);
                    }
                }
            }
        }

        public void stopRunning() {
            mRunning = false;
        }
    }

    /**
     * 将byte[]数组转化为String类型
     *
     * @param arg    需要转换的byte[]数组
     * @param length 需要转换的数组长度
     * @return 转换后的String队形
     */
    private String toHexString(byte[] arg, int length) {
        String result = new String();
        if (arg != null) {
            for (int i = 0; i < length; i++) {
                result = result
                        + (Integer.toHexString(
                        arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0"
                        + Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])
                        : Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])) + " ";
            }
            return result;
        }
        return "";
    }

    /**
     * 将String转化为byte[]数组
     *
     * @param arg 需要转换的String对象
     * @return 转换后的byte[]数组
     */
    private byte[] toByteArray(String arg) {
        if (arg != null) {
            /* 1.先去除String中的' '，然后将String转换为char数组 */
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }
            /* 将char数组中的值转成一个实际的十进制数组 */
            int EvenLength = (length % 2 == 0) ? length : length + 1;
            if (EvenLength != 0) {
                int[] data = new int[EvenLength];
                data[EvenLength - 1] = 0;
                for (int i = 0; i < length; i++) {
                    if (NewArray[i] >= '0' && NewArray[i] <= '9') {
                        data[i] = NewArray[i] - '0';
                    } else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
                        data[i] = NewArray[i] - 'a' + 10;
                    } else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
                        data[i] = NewArray[i] - 'A' + 10;
                    }
                }
                /* 将 每个char的值每两个组成一个16进制数据 */
                byte[] byteArray = new byte[EvenLength / 2];
                for (int i = 0; i < EvenLength / 2; i++) {
                    byteArray[i] = (byte) (data[i * 2] * 16 + data[i * 2 + 1]);
                }
                return byteArray;
            }
        }
        return new byte[]{};
    }

    private class WriteCommand implements Runnable {
        byte command;
        byte[] data;

        int tryCount = 0;

        public WriteCommand(byte command, byte[] data) {
            this.command = command;
            this.data = data;
        }

        @Override
        public void run() {
            if (tryCount < 3) {
                tryCount++;
                if (writeData(this.command, this.data)) {
                    mHandler.postDelayed(this, 1000);
                } else {
                    Log.e(TAG, "writeData fail.");
                    removeCommand(this);
                    mEventBus.post(new TimeoutEvent());
                }
            } else {
                removeCommand(this);
                mEventBus.post(new TimeoutEvent());
            }
        }
    }
}
