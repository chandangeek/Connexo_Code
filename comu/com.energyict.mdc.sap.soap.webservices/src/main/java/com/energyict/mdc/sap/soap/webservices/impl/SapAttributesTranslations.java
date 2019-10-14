/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum SapAttributesTranslations  implements TranslationKey {

        SAP_DEVICE_UTIL_DEVICE_ID("UtilitiesDeviceID", "Utilities Device ID"),
        SAP_UTIL_MEASUREMENT_TASK_ID("UtilitiesMeasurementTaskID", "Local reference number"),
        SAP_TIME_SERIES_ID("SapUtilitiesTimeSeriesID", "Time series ID");

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
