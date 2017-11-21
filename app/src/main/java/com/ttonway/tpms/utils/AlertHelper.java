package com.ttonway.tpms.utils;

import android.content.Context;

import com.ttonway.tpms.core.TireStatus;
import com.ttonway.tpms.core.TpmsDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ttonway on 2016/11/21.
 */
public class AlertHelper {

    Context mContext;
    List<Integer> mVoices;
    List<Integer> mMessages;

    public AlertHelper(Context context) {
        this.mContext = context;
        this.mVoices = new ArrayList<>();
        this.mMessages = new ArrayList<>();
    }

    public AlertHelper process(byte tire, TireStatus status) {
        String prefix = "voice_";
        String prefix2 = "alert_message_";
        switch (tire) {
            case TpmsDevice.TIRE_LEFT_FRONT:
                prefix += "tire1_";
                prefix2 += "tire1_";
                break;
            case TpmsDevice.TIRE_RIGHT_FRONT:
                prefix += "tire3_";
                prefix2 += "tire3_";
                break;
            case TpmsDevice.TIRE_RIGHT_END:
                prefix += "tire4_";
                prefix2 += "tire4_";
                break;
            case TpmsDevice.TIRE_LEFT_END:
                prefix += "tire2_";
                prefix2 += "tire2_";
                break;
        }

        switch (status.pressureStatus) {
            case TireStatus.PRESSURE_HIGH:
                mVoices.add(getRawId(prefix + "pressure_high"));
                mMessages.add(getStringId(prefix2 + "pressure_high"));
                break;
            case TireStatus.PRESSURE_LOW:
                mVoices.add(getRawId(prefix + "pressure_low"));
                mMessages.add(getStringId(prefix2 + "pressure_low"));
                break;
            case TireStatus.PRESSURE_ERROR:
                mVoices.add(getRawId(prefix + "pressure_error"));
                mMessages.add(getStringId(prefix2 + "pressure_error"));
                break;
            case TireStatus.PRESSURE_LEAKING:
                mVoices.add(getRawId(prefix + "leaking"));
                mMessages.add(getStringId(prefix2 + "leaking"));
                break;
        }
        if (status.temperatureStatus == TireStatus.TEMPERATURE_HIGH) {
            mVoices.add(getRawId(prefix + "temp_high"));
            mMessages.add(getStringId(prefix2 + "temp_high"));
        }
        if (status.batteryStatus == TireStatus.BATTERY_LOW) {
            mVoices.add(getRawId(prefix + "battery_low"));
            mMessages.add(getStringId(prefix2 + "battery_low"));
        }

        return this;
    }

    int getStringId(String strName) {
        return mContext.getResources().getIdentifier(strName, "string", mContext.getPackageName());
    }

    int getRawId(String strName) {
        return mContext.getResources().getIdentifier(strName, "raw", mContext.getPackageName());
    }

    public List<Integer> getVoices() {
        return mVoices;
    }

    public List<Integer> getMessages() {
        return mMessages;
    }

    public boolean hasAlert() {
        return mMessages.size() > 0;
    }
}
