package com.energyict.mdc.device.data;

import com.elster.jupiter.nls.TranslationKey;

public enum DeviceAttributesTranslations implements TranslationKey {

        DEVICE_NAME("deviceName", "Device name"),
        DEVICE_MRID("mrID", "Device mrID"),
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
