/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;

import test.com.energyict.protocolimplv2.sdksample.SDKTimeDeviceProtocolDialectProperties;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link SDKTimeDeviceProtocolDialectProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-27 (09:37)
 */
class SDKTimeDialectProperties extends CommonDeviceProtocolDialectProperties {

    private TimeDuration clockOffsetWhenReading;
    private TimeDuration clockOffsetWhenWriting;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.clockOffsetWhenReading = (TimeDuration) propertyValues.getProperty(ActualFields.CLOCK_OFFSET_WHEN_READING.propertySpecName());
        this.clockOffsetWhenWriting = (TimeDuration) propertyValues.getProperty(ActualFields.CLOCK_OFFSET_WHEN_WRITING.propertySpecName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.CLOCK_OFFSET_WHEN_READING.propertySpecName(), this.clockOffsetWhenReading);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.CLOCK_OFFSET_WHEN_WRITING.propertySpecName(), this.clockOffsetWhenWriting);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

    enum ActualFields {
        CLOCK_OFFSET_WHEN_READING("clockOffsetWhenReading", "ClockOffsetWhenReading", "CLOCK_OFFSET_WHEN_READING"),
        CLOCK_OFFSET_WHEN_WRITING("clockOffsetWhenWriting", "ClockOffsetWhenWriting", "CLOCK_OFFSET_WHEN_WRITING");

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
                    .column(this.databaseName() + "VALUE")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(this.javaName() + ".count")
                    .add();
            table
                    .column(this.databaseName() + "UNIT")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(this.javaName() + ".timeUnitCode")
                    .add();
        }

    }
}
