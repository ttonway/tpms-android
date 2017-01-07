package com.ttonway.tpms.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.google.common.eventbus.EventBus;
import com.ttonway.tpms.SPManager;
import com.ttonway.tpms.bluetooth.BluetoothLeDriver;
import com.ttonway.tpms.usb.UsbDriver;
import com.ttonway.tpms.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * Created by ttonway on 2016/11/7.
 */
public class TpmsDevice implements DriverCallback {
    private static final String TAG = TpmsDevice.class.getSimpleName();

    public static final byte TIRE_NONE = Byte.MIN_VALUE;
    public static final byte TIRE_LEFT_FRONT = 0;
    public static final byte TIRE_RIGHT_FRONT = 1;
    public static final byte TIRE_RIGHT_END = 2;
    public static final byte TIRE_LEFT_END = 3;

    public static final byte CMD_START_TIRE_MATCH = (byte) 0x02;
    public static final byte CMD_STOP_TIRE_MATCH = (byte) 0x03;
    public static final byte CMD_EXCHANGE_TIRE = (byte) 0x05;
    public static final byte CMD_SAVE_SETTING = (byte) 0x06;
    public static final byte CMD_QUERY_SETTING = (byte) 0x07;

    private static TpmsDevice instance = null;

    TpmsDriver mDriver;
    EventBus mEventBus;
    SharedPreferences mPreferences;

    Handler mHandler = new Handler();
    boolean mHasError = false;
    boolean mNeedSettings = false;

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
        this.mDriver = DriverFactory.newDriver(context, this);
        this.mEventBus = new EventBus();
        this.mPreferences = context.getSharedPreferences("device", Context.MODE_PRIVATE);

        initData();
    }

    public TpmsDriver getTpmsDriver() {
        return mDriver;
    }

    public boolean isBluetoothEnabled() {
        return mDriver instanceof BluetoothLeDriver;
    }

    public boolean isUSBEnabled() {
        return mDriver instanceof UsbDriver;
    }

    public int getState() {
        return mDriver.getState();
    }

    void initData() {
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

    public void clearData() {
        mPreferences.edit().clear().apply();

        initData();
    }

    public void registerReceiver(Object object) {
        mEventBus.register(object);
    }

    public void unregisterReceiver(Object object) {
        mEventBus.unregister(object);
    }

    public boolean openDevice() {
        Log.d(TAG, "openDevice");
        if (mDriver.openDevice()) {
//            querySettings();
            mNeedSettings = true;

            mHasError = false;

            return true;
        }

        return false;
    }

    public void closeDevice() {
        Log.d(TAG, "closeDevice");
        mDriver.closeDevice();

        synchronized (mCommands) {
            for (WriteCommand cmd : mCommands) {
                mHandler.removeCallbacks(cmd);
            }
            mCommands.clear();
        }

    }

    public void closeDeviceSafely() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                closeDevice();
            }
        });
    }

    public boolean isOpen() {
        return mDriver.getState() == TpmsDriver.STATE_OPEN;
    }

    public boolean hasError() {
        return mHasError;
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

        Log.d(TAG, "[WriteData] " + Utils.toHexString(buf, index));
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
        postCommand(new WriteCommand(CMD_START_TIRE_MATCH, new byte[]{tire}));
    }

    public void stopTireMatch() {
        postCommand(new WriteCommand(CMD_STOP_TIRE_MATCH, new byte[0]));
    }

    public void exchangeTire(byte tire1, byte tire2) {
        postCommand(new WriteCommand(CMD_EXCHANGE_TIRE, new byte[]{tire1, tire2}));
    }

    public boolean isSettingsChanged(float lowLimit, float highLimit, int tempLimit) {
        byte[] buf = new byte[3];
        buf[0] = (byte) (lowLimit / 0.1f + 0.5f);
        buf[1] = (byte) (highLimit / 0.1f + 0.5f);
        buf[2] = (byte) tempLimit;

        byte[] buf2 = new byte[3];
        buf2[0] = (byte) (mPressureLowLimit / 0.1f + 0.5f);
        buf2[1] = (byte) (mPressureHighLimit / 0.1f + 0.5f);
        buf2[2] = (byte) mTemperatureLimit;

        return !Arrays.equals(buf, buf2);
    }

    public void saveSettings(float lowLimit, float highLimit, int tempLimit) {
        byte[] buf = new byte[3];
        buf[0] = (byte) (lowLimit / 0.1f + 0.5f);
        buf[1] = (byte) (highLimit / 0.1f + 0.5f);
        buf[2] = (byte) tempLimit;
        postCommand(new WriteCommand(CMD_SAVE_SETTING, buf));
    }

    private void querySettings() {
//        postCommand(new WriteCommand(CMD_QUERY_SETTING, new byte[0], 16000));
        postCommand(new WriteCommand(CMD_QUERY_SETTING, new byte[0]));
    }

    @Override
    public void onStateChanged(int state) {
        String s = null;
        switch (state) {
            case TpmsDriver.STATE_OPEN:
                s = "OPEN";

                if (mNeedSettings) {
                    querySettings();
                    mNeedSettings = false;
                }
                break;
            case TpmsDriver.STATE_OPENING:
                s = "OPENING";
                break;
            case TpmsDriver.STATE_CLOSE:
                s = "CLOSE";
                break;
            case TpmsDriver.STATE_CLOSING:
                s = "CLOSING";
                break;
        }
        Log.d(TAG, "onStateChanged " + s);
        mEventBus.post(new StateChangeEvent());
    }

    @Override
    public void onError(int error) {
        Log.d(TAG, "onError " + error);
        mEventBus.post(new ErrorEvent(error));

        closeDevice();
    }

    @Override
    public void onReadData(byte[] buf, int length) {
        String recv = Utils.toHexString(buf, length);
        Log.d(TAG, "[ReadData] " + recv);
        try {
            onReadCommand(buf, length);
        } catch (Exception e) {
            Log.e(TAG, "onReadCommand fail.", e);
        }
    }

    public void onReadCommand(byte[] buf, int length) {
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
        final byte[] data = Arrays.copyOfRange(buf, 3, length - 2);
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
                String str = Utils.toHexString(array, 4);

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

                mEventBus.post(new SettingChangeEvent(CMD_SAVE_SETTING));
                break;
            }
            // 查询应答
            case 0x08: {
                mPressureLowLimit = 0.1f * (int) data[0];
                mPressureHighLimit = 0.1f * (int) data[1];
                mTemperatureLimit = (int) data[2];
                removeCommands(CMD_QUERY_SETTING, new byte[0]);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        writeData((byte) 0x08, data);
                    }
                });

                mPreferences.edit()
                        .putFloat("pressure-low-limit", mPressureLowLimit)
                        .putFloat("pressure-high-limit", mPressureHighLimit)
                        .putInt("temperature-high-limit", mTemperatureLimit)
                        .apply();

                mEventBus.post(new SettingChangeEvent(CMD_QUERY_SETTING));
                break;
            }
            default: {
                Log.e(TAG, "Unhandled frame.");
                break;
            }
        }
    }


    private class WriteCommand implements Runnable {
        byte command;
        byte[] data;

        int tryCount = 0;
        final long delay;

        public WriteCommand(byte command, byte[] data) {
            this(command, data, 1000);
        }

        public WriteCommand(byte command, byte[] data, long delay) {
            this.command = command;
            this.data = data;
            this.delay = delay;
        }

        @Override
        public void run() {
//            if (this.command == CMD_QUERY_SETTING) {
//                tryCount++;
//                mHandler.postDelayed(this, delay);
//                if (tryCount > 1) {
//                    mHasError = true;
//                    Log.e(TAG, "command " + this.command + " timeout");
//                    mEventBus.post(new ErrorEvent(this.command));
//                }
//                return;
//            }

//            if (this.command == CMD_QUERY_SETTING) {
//                tryCount++;
//                writeData(this.command, this.data);
//                mHandler.postDelayed(this, delay);
//                if (tryCount > 1) {
//                    mEventBus.post(new ErrorEvent(this.command));
//                }
//                return;
//            }

            if (tryCount < 3) {
                tryCount++;
                if (writeData(this.command, this.data)) {
                    mHandler.postDelayed(this, delay);
                } else {
                    Log.e(TAG, "writeData fail.");
//                    removeCommand(this);
//                    mEventBus.post(new ErrorEvent());
                    mHandler.postDelayed(this, delay);
                }
            } else {
                removeCommand(this);
                mHasError = true;
                Log.e(TAG, "command " + this.command + " timeout");
                mEventBus.post(new ErrorEvent(this.command));
            }
        }
    }
}
