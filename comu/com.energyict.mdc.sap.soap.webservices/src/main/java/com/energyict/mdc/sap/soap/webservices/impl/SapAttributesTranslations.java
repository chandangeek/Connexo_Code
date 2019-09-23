package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum SapAttributesTranslations  implements TranslationKey {

        DEVICE_NAME("UtilitiesDeviceID", "Utilities Device ID"),
        DEVICE_MRID("UtilitiesMeasurementTaskID", "Utilities Measurement Task ID"),
        DEVICE_SERIAL_NUMBER("SapSerialID", "Serial ID");

        private final String key;
        private final String defaultFormat;

        SapAttributesTranslations(String key, String defaultFormat) {
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
