/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.mocks;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link SDKTimeDeviceProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-30 (12:43)
 */
class SDKTimeDialectProperties extends CommonDeviceProtocolDialectProperties {

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

        public PropertySpec propertySpec(PropertySpecService propertySpecService) {
            return propertySpecService
                    .specForValuesOf(new TimeDurationValueFactory())
                    .named(this.propertySpecName(), this.propertySpecName())
                    .describedAs(this.propertySpecName())
                    .finish();
        }

        public void addTo(Table table) {
            table
                .column(this.databaseName() + "VALUE")
                .number()
                .map(this.javaName() + ".count")
                .add();
            table
                .column(this.databaseName() + "UNIT")
                .number()
                .map(this.javaName() + ".timeUnitCode")
                .add();
        }

    }

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

}