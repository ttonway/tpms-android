/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ttonway.tpms.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import com.ttonway.tpms.R;
import com.ttonway.tpms.core.DriverCallback;
import com.ttonway.tpms.core.TpmsDriver;

import java.util.Arrays;
import java.util.List;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeDriver extends TpmsDriver {
    private final static String TAG = BluetoothLeDriver.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private SharedPreferences mSharedPreferences;

    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mWriteCharac;
    private BluetoothGattCharacteristic mNotifyCharac;
    private boolean mNotifyEnabled;

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

                setState(STATE_OPEN);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                setState(STATE_CLOSE);
            } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                Log.i(TAG, "Connecting to GATT server.");
                setState(STATE_OPENING);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                Log.i(TAG, "Disconnecting from GATT server.");
                setState(STATE_CLOSING);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service1 = gatt.getService(Constants.NOTIFY_SERVICE_UUID);
                if (service1 != null) {
                    mNotifyCharac = service1.getCharacteristic(Constants.NOTIFY_CHARACTERISTIC_UUID);
                    if (mNotifyCharac != null) {
                        int prop = mNotifyCharac.getProperties();
                        Log.d(TAG, "Notify Characteristic properties: " + prop);
                    }
                }
                BluetoothGattService service2 = gatt.getService(Constants.WRITE_SERVICE_UUID);
                if (service2 != null) {
                    mWriteCharac = service2.getCharacteristic(Constants.WRITE_CHARACTERISTIC_UUID);
                    if (mWriteCharac != null) {
                        int prop = mWriteCharac.getProperties();
                        Log.d(TAG, "Write Characteristic properties: " + prop);
                    }
                }

                if (mNotifyCharac == null || mWriteCharac == null) {
                    mCallback.onError(ERROR_WRONG_DEVICE);

                } else {
                    setCharacteristicNotification(mNotifyCharac, true);
                    mNotifyEnabled = true;
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                mCallback.onReadData(data, data.length);
            }
        }
    };

    public BluetoothLeDriver(Context context, DriverCallback callback) {
        super(context, callback);

        mSharedPreferences = context.getSharedPreferences("bluetooth", Context.MODE_PRIVATE);
        mBluetoothDeviceAddress = mSharedPreferences.getString("address", null);

        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            Log.e(TAG, "Unable to initialize BluetoothManager.");
            return;
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        }
    }

    public String getBluetoothDeviceAddress() {
        return mBluetoothDeviceAddress;
    }

    public boolean openDevice(String address) {
        if (mBluetoothGatt != null) {
            closeDevice();
        }

        mBluetoothDeviceAddress = address;
        mSharedPreferences.edit().putString("address", address).apply();

        return openDevice();
    }

    @Override
    public String isDriverSupported() {
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return mContext.getString(R.string.ble_not_supported);
        }

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            return mContext.getString(R.string.error_bluetooth_not_supported);
        }

        return null;
    }

    @Override
    public boolean openDevice() {
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (mBluetoothDeviceAddress == null) {
            Log.w(TAG, "Bluetooth address is null.");
            return false;
        }

        if (mState == STATE_OPEN) {
            Log.e(TAG, "Already opened.");
            return true;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                setState(STATE_OPENING);
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBluetoothDeviceAddress);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mContext, true, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        setState(STATE_OPENING);
        return true;
    }

    @Override
    public boolean closeDevice() {
        if (mNotifyEnabled) {
            setCharacteristicNotification(mNotifyCharac, false);
            mNotifyEnabled = false;
        }
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        mWriteCharac = null;
        mNotifyCharac = null;

        setState(STATE_CLOSE);

        return true;
    }

    @Override
    public int WriteData(byte[] bytes, int length) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return -1;
        }
        if (mWriteCharac == null) {
            Log.e(TAG, "Write Characteristic is null");
            return -1;
        }
        byte[] val = Arrays.copyOf(bytes,length);
        mWriteCharac.setValue(val);
        if (mBluetoothGatt.writeCharacteristic(mWriteCharac)) {
            return length;
        } else {
            Log.e(TAG, "writeCharacteristic fail.");
            return -1;
        }
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
