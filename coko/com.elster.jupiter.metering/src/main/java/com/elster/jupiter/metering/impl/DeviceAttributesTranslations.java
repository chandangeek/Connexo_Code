/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.metering.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum DeviceAttributesTranslations implements TranslationKey {

        DEVICE_NAME("deviceName", "Device name"),
        DEVICE_MRID("mrID", "Device MRID"),
        DEVICE_SERIAL_ID("SerialID", "Serial ID"),
        DEVICE_SERIAL_NUMBER("serialNumber", "Device serial number");

        private final String key;
        private final String defaultFormat;

        DeviceAttributesTranslations(String key, String defaultFormat) {
            this.key = key;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }

}
