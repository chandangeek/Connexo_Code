/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.dialects;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;

import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link AbntOpticalDeviceProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-26 (14:35)
 */
class AbntDeviceProtocolDialectProperties extends CommonDeviceProtocolDialectProperties {

    private BigDecimal retries;
    private TimeDuration timeoutMillis;
    private TimeDuration forcedDelay;
    private TimeDuration delayAfterError;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.retries = (BigDecimal) propertyValues.getProperty(ActualFields.RETRIES.propertySpecName());
        this.timeoutMillis = (TimeDuration) propertyValues.getProperty(ActualFields.TIMEOUT_PROPERTY.propertySpecName());
        this.forcedDelay = (TimeDuration) propertyValues.getProperty(ActualFields.FORCED_DELAY.propertySpecName());
        this.delayAfterError = (TimeDuration) propertyValues.getProperty(ActualFields.DELAY_AFTER_ERROR.propertySpecName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.RETRIES.propertySpecName(), this.retries);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.TIMEOUT_PROPERTY.propertySpecName(), this.timeoutMillis);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.FORCED_DELAY.propertySpecName(), this.forcedDelay);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.DELAY_AFTER_ERROR.propertySpecName(), this.delayAfterError);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

    enum ActualFields {
        RETRIES("retries", DlmsProtocolProperties.RETRIES, "RETRIES") {
            @Override
            public void addTo(Table table) {
                this.addAsBigDecimalColumnTo(table);
            }
        },
        TIMEOUT_PROPERTY("timeoutMillis", DlmsProtocolProperties.TIMEOUT, "TIMEOUTMILLIS") {
            @Override
            public void addTo(Table table) {
                this.addAsTimeDurationColumnTo(table);
            }
        },
        FORCED_DELAY("forcedDelay", DlmsProtocolProperties.FORCED_DELAY, "FORCED_DELAY") {
            @Override
            public void addTo(Table table) {
                this.addAsTimeDurationColumnTo(table);
            }
        },
        DELAY_AFTER_ERROR("delayAfterError", DlmsProtocolProperties.DELAY_AFTER_ERROR, "DELAY_AFTER_ERROR") {
            @Override
            public void addTo(Table table) {
                this.addAsTimeDurationColumnTo(table);
            }
        };

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

        public abstract void addTo(Table table);

        protected void addAsBigDecimalColumnTo(Table table) {
            table
                    .column(this.databaseName())
                    .number()
                    .map(this.javaName())
                    .add();
        }

        protected void addAsTimeDurationColumnTo(Table table) {
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