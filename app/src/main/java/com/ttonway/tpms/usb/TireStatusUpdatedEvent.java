package com.ttonway.tpms.usb;

/**
 * Created by ttonway on 2016/11/11.
 */
public class TireStatusUpdatedEvent {
    public final byte tire;

    public TireStatusUpdatedEvent(byte tire) {
        this.tire = tire;
    }
}
