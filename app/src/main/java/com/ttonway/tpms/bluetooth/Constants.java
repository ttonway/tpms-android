/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ttonway.tpms.bluetooth;

import java.util.UUID;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class Constants {
    public static String NOTIFY_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static String WRITE_SERVICE = "0000ffe5-0000-1000-8000-00805f9b34fb";

    public static String NOTIFY_CHARACTERISTIC = "0000ffe4-0000-1000-8000-00805f9b34fb";
    public static String WRITE_CHARACTERISTIC = "0000ffe9-0000-1000-8000-00805f9b34fb";


    public static UUID NOTIFY_SERVICE_UUID = UUID.fromString(NOTIFY_SERVICE);
    public static UUID WRITE_SERVICE_UUID = UUID.fromString(WRITE_SERVICE);
    public static UUID[] TPMS_SERVICE_UUIDS = new UUID[]{NOTIFY_SERVICE_UUID, WRITE_SERVICE_UUID};

    public static UUID NOTIFY_CHARACTERISTIC_UUID = UUID.fromString(NOTIFY_CHARACTERISTIC);
    public static UUID WRITE_CHARACTERISTIC_UUID = UUID.fromString(WRITE_CHARACTERISTIC);
}
