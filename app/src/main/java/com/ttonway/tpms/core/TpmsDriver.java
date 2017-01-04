package com.ttonway.tpms.core;

import android.content.Context;

/**
 * Created by ttonway on 2017/1/2.
 */

public abstract class TpmsDriver {
    public static final int STATE_CLOSE = 0;
    public static final int STATE_OPENING = 1;
    public static final int STATE_OPEN = 2;
    public static final int STATE_CLOSING = 3;

    public static final int ERROR_WRONG_DEVICE = 1;
    public static final int ERROR_TIMEOUT = 2;

    protected Context mContext;
    protected int mState;
    protected DriverCallback mCallback;

    public TpmsDriver(Context context,DriverCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
    }

    public synchronized void setState(int state) {
        this.mState = state;

        this.mCallback.onStateChanged(state);
    }

    public synchronized int getState() {
        return mState;
    }

    abstract public String isDriverSupported();

    abstract public boolean openDevice();

    abstract public boolean closeDevice();

    abstract public int WriteData(byte[] bytes, int length);
}
