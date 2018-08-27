/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;

import com.energyict.dlms.common.DlmsProtocolProperties;

import java.math.BigDecimal;

public class GatewayTcpDeviceProtocolDialectProperties extends CommonDeviceProtocolDialectProperties {

    private BigDecimal retries;
    private TimeDuration timeoutMillis;
    private BigDecimal roundTripCorrection;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.retries = (BigDecimal) propertyValues.getProperty(ActualFields.RETRIES.propertySpecName());
        this.timeoutMillis = (TimeDuration) propertyValues.getProperty(ActualFields.TIMEOUT_PROPERTY.propertySpecName());
        this.roundTripCorrection = (BigDecimal) propertyValues.getProperty(ActualFields.ROUND_TRIP_CORRECTION.propertySpecName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.RETRIES.propertySpecName(), this.retries);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.TIMEOUT_PROPERTY.propertySpecName(), this.timeoutMillis);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.ROUND_TRIP_CORRECTION.propertySpecName(), this.roundTripCorrection);
    }

    @Override
    public void validateDelete() {

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
        ROUND_TRIP_CORRECTION("roundTripCorrection", DlmsProtocolProperties.ROUND_TRIP_CORRECTION, "ROUND_TRIP_CORRECTION") {
            @Override
            public void addTo(Table table) {
                this.addAsBigDecimalColumnTo(table);
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