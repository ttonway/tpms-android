package com.ttonway.tpms;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class SPManager {
    public static final int LANGUAGE_SIMPLIFIED_CHINESE = 0;
    public static final int LANGUAGE_TRADITIONAL_CHINESE = 1;
    public static final int LANGUAGE_ENGLISH = 2;

    public static final int UNIT_BAR = 0;
    public static final int UNIT_PSI = 1;
    public static final int UNIT_KPA = 2;
    public static final int UNIT_KG = 3;

    public static final int TEMP_UNIT_CELSIUS = 0;
    public static final int TEMP_UNIT_FAHRENHEIT = 1;

    public static final float PRESSURE_UPPER_LIMIT_MIN = 2.5f;
    public static final float PRESSURE_UPPER_LIMIT_MAX = 4.5f;
    public static final float PRESSURE_UPPER_LIMIT_RANGE = PRESSURE_UPPER_LIMIT_MAX - PRESSURE_UPPER_LIMIT_MIN;
    public static final float PRESSURE_UPPER_LIMIT_DEFAULT = 3.2f;
    public static final float PRESSURE_LOWER_LIMIT_MIN = 1.0f;
    public static final float PRESSURE_LOWER_LIMIT_MAX = 2.5f;
    public static final float PRESSURE_LOWER_LIMIT_RANGE = PRESSURE_LOWER_LIMIT_MAX - PRESSURE_LOWER_LIMIT_MIN;
    public static final float PRESSURE_LOWER_LIMIT_DEFAULT = 1.8f;
    public static final int TEMP_UPPER_LIMIT_MIN = 60;
    public static final int TEMP_UPPER_LIMIT_MAX = 99;
    public static final int TEMP_UPPER_LIMIT_RANGE = TEMP_UPPER_LIMIT_MAX - TEMP_UPPER_LIMIT_MIN;
    public static final int TEMP_UPPER_LIMIT_DEFAULT = 70;

    public static final String KEY_LANGUAGE = "app.language";
    public static final String KEY_PRESSURE_UNIT = "app.pressure-unit";
    public static final String KEY_TEMP_UNIT = "app.temp-unit";
    public static final String KEY_PRESSURE_UPPER_LIMIT = "app.pressure-upper-limit";
    public static final String KEY_PRESSURE_LOWER_LIMIT = "app.pressure-lower-limit";
    public static final String KEY_TEMP_UPPER_LIMIT = "app.temp-upper-limit";

    private static final String SP_NAME = "setting";

    public static boolean contains(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME,
                Context.MODE_PRIVATE);
        return sp.contains(key);
    }

    public static void setString(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME,
                Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    public static String getString(Context context, String key,
                                   String defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME,
                Context.MODE_PRIVATE);
        return sp.getString(key, defaultValue);
    }

    public static boolean getBoolean(Context context, String key,
                                     boolean defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME,
                Context.MODE_PRIVATE);
        return sp.getBoolean(key, defaultValue);
    }

    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME,
                Context.MODE_PRIVATE);
        sp.edit().putBoolean(key, value).apply();
    }

    public static int getInt(Context context, String key, int defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME,
                Context.MODE_PRIVATE);
        return sp.getInt(key, defaultValue);
    }

    public static void setInt(Context context, String key, int value) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME,
                Context.MODE_PRIVATE);
        sp.edit().putInt(key, value).apply();
    }

    public static float getFloat(Context context, String key, float defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME,
                Context.MODE_PRIVATE);
        return sp.getFloat(key, defaultValue);
    }

    public static void setFloat(Context context, String key, float value) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME,
                Context.MODE_PRIVATE);
        sp.edit().putFloat(key, value).apply();
    }

    public static Locale getCurrentLocale(Context context) {
        int lan = SPManager.getInt(context, SPManager.KEY_LANGUAGE, SPManager.LANGUAGE_SIMPLIFIED_CHINESE);
        switch (lan) {
            case LANGUAGE_SIMPLIFIED_CHINESE:
                return Locale.SIMPLIFIED_CHINESE;
            case LANGUAGE_TRADITIONAL_CHINESE:
                return Locale.TRADITIONAL_CHINESE;
            case LANGUAGE_ENGLISH:
                return Locale.ENGLISH;
            default:
                return null;
        }
    }


}
