package com.ttonway.tpms.usb;

/**
 * Created by ttonway on 2016/11/11.
 */
public class TimeoutEvent {

    public final byte command;

    public TimeoutEvent(byte command) {
        this.command = command;
    }
}
