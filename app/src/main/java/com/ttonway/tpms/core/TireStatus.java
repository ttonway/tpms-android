package com.ttonway.tpms.core;

import android.content.SharedPreferences;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ttonway on 2016/11/7.
 */
public class TireStatus {
    public static final int PRESSURE_NORMAL = 0;
    public static final int PRESSURE_LOW = 1;
    public static final int PRESSURE_HIGH = 2;
    public static final int PRESSURE_ERROR = 3;
    public static final int PRESSURE_NO_SIGNAL = 21;// custom
    public static final int PRESSURE_LEAKING = 22;// custom

    public static final int BATTERY_NORMAL = 0;
    public static final int BATTERY_LOW = 1;

    public static final int TEMPERATURE_NORMAL = 0;
    public static final int TEMPERATURE_HIGH = 1;

    public final String name;

    public boolean inited = false;

    public int pressureStatus;
    public int batteryStatus;
    public int temperatureStatus;

    private float pressure;//Bar
    private int battery;//mV
    private int temperature;//摄氏度

    boolean pressureBreak;
    boolean batteryBreak;
    boolean temperatureBreak;
    List<Pair<Long, Float>> pressureHistories = new ArrayList<>();
    long lastUpdateTime;

    public TireStatus(String name) {
        this.name = name;

        pressure = Float.MAX_VALUE;
        battery = Integer.MIN_VALUE;
        temperature = Integer.MIN_VALUE;
    }

    @Override
    public String toString() {
        return "TireStatus{" +
                pressure + "Bar[" + pressureStatus + "], " +
                temperature + "%C[" + temperatureStatus + "], " +
                battery + "mV[" + batteryStatus + "]" +
                '}';
    }

    public float getPressure() {
        return pressure;
    }

    public int getBattery() {
        return battery;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setPressure(float pressure) {
        if (pressure > 4.5) {
            // ignored
        } else {
            if (this.pressure == Float.MAX_VALUE) {
                // goon
            } else if (pressureBreak) {
                pressureBreak = false;
                // goon
            } else {
                if (Math.abs(this.pressure - pressure) > 0.5f) {
                    pressureBreak = true;
                    return;
                } else {
                    pressureBreak = false;
                    // goon
                }
            }

            this.pressure = pressure;

            long now = System.currentTimeMillis();
            Pair<Long, Float> pair = new Pair<>(now, pressure);
            this.pressureHistories.add(pair);
            List<Pair<Long, Float>> deletes = new ArrayList<>();
            for (Pair<Long, Float> e : this.pressureHistories) {
                if (now - e.first > /* 1min */60000) {
                    deletes.add(e);
                } else {
                    break;
                }
            }
            this.pressureHistories.removeAll(deletes);
        }
    }

    public void setTemperature(int temperature) {
        if (temperature > 100) {
            temperature = 0;
        }

        if (this.temperature == Integer.MIN_VALUE) {
            // goon
        } else if (temperatureBreak) {
            temperatureBreak = false;
            // goon
        } else {
            if (Math.abs(this.temperature - temperature) > 20) {
                temperatureBreak = true;
                return;
            } else {
                temperatureBreak = false;
                // goon
            }
        }

        this.temperature = temperature;
    }

    public void setBattery(int battery) {

        if (this.battery == Integer.MIN_VALUE) {
            // goon
        } else if (batteryBreak) {
            batteryBreak = false;
            // goon
        } else {
            if (Math.abs(this.battery - battery) > 200) {
                batteryBreak = true;
                return;
            } else {
                batteryBreak = false;
                // goon
            }
        }


        this.battery = battery;
    }

    boolean isLeaking() {
        if (pressureHistories.size() == 0)
            return false;

        Pair<Long, Float> first = pressureHistories.get(0);
        if (first.second - this.pressure > 0.3f) {
            boolean leaking = true;
            float lastValue = Float.MAX_VALUE;
            for (Pair<Long, Float> e : pressureHistories) {
                if (e.second <= lastValue) {
                    lastValue = e.second;
                } else {
                    leaking = false;
                    break;
                }
            }
            if (leaking) {
                return true;
            }
        }
        return false;
    }

    public void setValues(TireStatus status) {
        this.inited = status.inited;

        this.pressureStatus = status.pressureStatus;
        this.batteryStatus = status.batteryStatus;
        this.temperatureStatus = status.temperatureStatus;

        this.pressure = status.pressure;
        this.battery = status.battery;
        this.temperature = status.temperature;

        this.pressureBreak = status.pressureBreak;
        this.batteryBreak = status.batteryBreak;
        this.temperatureBreak = status.temperatureBreak;
        this.pressureHistories.clear();
        this.pressureHistories.addAll(status.pressureHistories);
        this.lastUpdateTime = status.lastUpdateTime;
    }

    void readTireStatus(SharedPreferences sp) {
        String prefix = this.name;
        if (sp.contains(prefix + "-pressure-status")) {
            this.inited = true;
        } else {
            this.inited = false;
            return;
        }
        this.pressureStatus = sp.getInt(prefix + "-pressure-status", 0);
        this.batteryStatus = sp.getInt(prefix + "-battery-status", 0);
        this.temperatureStatus = sp.getInt(prefix + "-temperature-status", 0);
        this.pressure = sp.getFloat(prefix + "-pressure", Float.MAX_VALUE);
        this.battery = sp.getInt(prefix + "-battery", Integer.MIN_VALUE);
        this.temperature = sp.getInt(prefix + "-temperature", Integer.MIN_VALUE);

        this.pressureBreak = this.batteryBreak = this.temperatureBreak = false;
        this.pressureHistories.clear();
        this.lastUpdateTime = 0;
    }

    void writeTireStatus(SharedPreferences sp) {
        if (this.inited) {
            String prefix = this.name;
            sp.edit()
                    .putInt(prefix + "-pressure-status", this.pressureStatus)
                    .putInt(prefix + "-battery-status", this.batteryStatus)
                    .putInt(prefix + "-temperature-status", this.temperatureStatus)
                    .putFloat(prefix + "-pressure", this.pressure)
                    .putInt(prefix + "-battery", this.battery)
                    .putInt(prefix + "-temperature", this.temperature)
                    .apply();
        }
    }
}
