package com.ttonway.tpms.core;

/**
 * Created by ttonway on 2016/11/11.
 */
public class ErrorEvent {

    public final byte command;
    public final int cause;

    public ErrorEvent(byte command) {
        this.command = command;
        this.cause = TpmsDriver.ERROR_TIMEOUT;
    }

    public ErrorEvent(int cause) {
        this.cause = cause;
        this.command = 0;
    }
}
