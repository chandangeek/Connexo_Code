/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;

import test.com.energyict.protocolimplv2.sdksample.SDKDeviceAlarmProtocolDialectProperties;

import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link SDKDeviceAlarmProtocolDialectProperties}.
 */

class SDKDeviceAlarmDialectProperties extends CommonDeviceProtocolDialectProperties {

    @Size(max = Table.MAX_STRING_LENGTH)
    private String deviceAlarmEventType;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.deviceAlarmEventType = (String) propertyValues.getProperty(ActualFields.DEVICE_ALARM_EVENT_TYPE.propertySpecName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.DEVICE_ALARM_EVENT_TYPE.propertySpecName(), this.deviceAlarmEventType);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

    enum ActualFields {
        DEVICE_ALARM_EVENT_TYPE("deviceAlarmEventType", "DeviceAlarmEventType", "DEVICE_ALARM_EVENT_TYPE");

        private final String javaName;
        private final String propertySpecName;
        private final String databaseName;

        ActualFields(String javaName, String propertySpecName, String databaseName) {
            this.javaName = javaName;
            this.propertySpecName = propertySpecName;
            this.databaseName = databaseName;
        }

        public String javaName() {
            return this.javaName;
        }

        public String propertySpecName() {
            return this.propertySpecName;
        }

        public String databaseName() {
            return this.databaseName;
        }

        public void addTo(Table table) {
            table
                    .column(this.databaseName())
                    .varChar()
                    .map(this.javaName())
                    .add();
        }
    }
}
