package com.ttonway.tpms.usb;

/**
 * Created by ttonway on 2016/11/7.
 */
public class TireStatus {
    public static final int PRESSURE_NORMAL = 0;
    public static final int PRESSURE_LOW = 1;
    public static final int PRESSURE_HIGH = 2;
    public static final int PRESSURE_ERROR = 3;

    public static final int BATTERY_NORMAL = 0;
    public static final int BATTERY_LOW = 1;

    public static final int TEMPERATURE_NORMAL = 0;
    public static final int TEMPERATURE_HIGH = 1;

    
    public int pressureStatus;
    public int batteryStatus;
    public int temperatureStatus;

    public float pressure;//Bar
    public int battery;//mV
    public int temperature;//摄氏度
}
