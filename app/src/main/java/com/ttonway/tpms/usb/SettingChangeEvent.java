package com.ttonway.tpms.usb;

/**
 * Created by ttonway on 2016/11/11.
 */
public class SettingChangeEvent {
    public final byte command;

    public SettingChangeEvent(byte command) {
        this.command = command;
    }
}
