package com.ttonway.tpms.core;

/**
 * Created by ttonway on 2016/11/11.
 */
public class TireMatchedEvent {
    public final byte tire;
    public final String value;

    public TireMatchedEvent(byte tire, String value) {
        this.tire = tire;
        this.value = value;
    }
}
