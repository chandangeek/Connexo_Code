/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.common.dialects;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;

import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimplv2.edmi.dialects.CommonEDMIDeviceProtocolDialect;
import com.energyict.protocolimplv2.edmi.dialects.ModemDeviceProtocolDialect;
import com.energyict.protocolimplv2.edmi.dialects.TcpDeviceProtocolDialect;
import com.energyict.protocolimplv2.edmi.dialects.UdpDeviceProtocolDialect;
import com.energyict.protocolimplv2.edmi.mk10.properties.MK10ConfigurationSupport;

import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link TcpDeviceProtocolDialect}, {@link UdpDeviceProtocolDialect}
 * and {@link ModemDeviceProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-26 (14:35)
 */
class EdmiDeviceProtocolDialectProperties extends CommonDeviceProtocolDialectProperties {

    private static final String RETRIES_JAVA_NAME = "retries";
    private static final String TIMEOUT_JAVA_NAME = "timeoutMillis";
    private static final String FORCED_DELAY_JAVA_NAME = "forcedDelay";
    private static final String CONNECTION_MODE_JAVA_NAME = "connectionMode";
    private static final String COUNT_JAVA_SUFFIX = ".count";
    private static final String UNIT_JAVA_SUFFIX = ".timeUnitCode";

    private static final String RETRIES_DATABASE_NAME = "RETRIES";
    private static final String TIMEOUT_DATABASE_NAME = "TIMEOUTMILLIS";
    private static final String FORCED_DELAY_DATABASE_NAME = "FORCED_DELAY";
    private static final String CONNECTION_MODE_DATABASE_NAME = "CONNECTION_MODE";
    private static final String COUNT_DATABASE_SUFFIX = "VALUE";
    private static final String UNIT_DATABASE_SUFFIX = "UNIT";

    private BigDecimal retries;
    private TimeDuration timeoutMillis;
    private TimeDuration forcedDelay;
    private String connectionMode;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.retries = (BigDecimal) propertyValues.getProperty(ActualFields.RETRIES.propertySpecName());
        this.timeoutMillis = (TimeDuration) propertyValues.getProperty(ActualFields.TIMEOUT_PROPERTY.propertySpecName());
        this.forcedDelay = (TimeDuration) propertyValues.getProperty(ActualFields.FORCED_DELAY.propertySpecName());
        this.connectionMode = (String) propertyValues.getProperty(ActualFields.CONNECTION_TYPE.propertySpecName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.RETRIES.propertySpecName(), this.retries);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.TIMEOUT_PROPERTY.propertySpecName(), this.timeoutMillis);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.FORCED_DELAY.propertySpecName(), this.forcedDelay);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.CONNECTION_TYPE.propertySpecName(), this.connectionMode);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

    enum ActualFields {
        RETRIES(RETRIES_JAVA_NAME, DlmsProtocolProperties.RETRIES, RETRIES_DATABASE_NAME) {
            @Override
            public void addTo(Table table) {
                this.addAsBigDecimalColumnTo(table);
            }
        },
        TIMEOUT_PROPERTY(TIMEOUT_JAVA_NAME, DlmsProtocolProperties.TIMEOUT, TIMEOUT_DATABASE_NAME) {
            @Override
            public void addTo(Table table) {
                this.addAsTimeDurationColumnTo(table);
            }
        },
        FORCED_DELAY(FORCED_DELAY_JAVA_NAME, DlmsProtocolProperties.FORCED_DELAY, FORCED_DELAY_DATABASE_NAME) {
            @Override
            public void addTo(Table table) {
                this.addAsTimeDurationColumnTo(table);
            }
        },
        CONNECTION_TYPE(CONNECTION_MODE_JAVA_NAME, CommonEDMIDeviceProtocolDialect.CONNECTION_MODE, CONNECTION_MODE_DATABASE_NAME) {
            @Override
            public void addTo(Table table) {
                this.addAsStringColumnToWithVersionSince(table, Version.version(10, 8));
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

        protected void addAsStringColumnToWithVersionSince(Table table, Version version) {
            table
                    .column(this.databaseName())
                    .varChar(2000)
                    .map(this.javaName())
                    .since(version)
                    .add();
        }

        protected void addAsBigDecimalColumnTo(Table table) {
            table
                    .column(this.databaseName())
                    .number()
                    .map(this.javaName())
                    .add();
        }

        protected void addAsTimeDurationColumnTo(Table table) {
            table
                    .column(this.databaseName() + COUNT_DATABASE_SUFFIX)
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(this.javaName() + COUNT_JAVA_SUFFIX)
                    .add();
            table
                    .column(this.databaseName() + UNIT_DATABASE_SUFFIX)
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(this.javaName() + UNIT_JAVA_SUFFIX)
                    .add();
        }
    }
}