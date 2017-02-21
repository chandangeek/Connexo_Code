package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.protocolimplv2.common.CommonV2TranslationKeys;

import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link TcpDeviceProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-26 (14:35)
 */
class TcpDeviceProtocolDialectProperties extends CommonDeviceProtocolDialectProperties {

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
        // Nothing to validate
    }

    enum ActualFields {
        RETRIES("retries", DlmsProtocolProperties.RETRIES, CommonV2TranslationKeys.RETRIES, "RETRIES") {
            @Override
            public void addTo(Table table) {
                this.addAsBigDecimalColumnTo(table);
            }
        },
        TIMEOUT_PROPERTY("timeoutMillis", DlmsProtocolProperties.TIMEOUT, CommonV2TranslationKeys.TIMEOUT, "TIMEOUTMILLIS") {
            @Override
            public void addTo(Table table) {
                this.addAsTimeDurationColumnTo(table);
            }
        },
        //Legacy column, removed in 10.3
        DELAY_AFTER_ERROR("delayAfterError", DlmsProtocolProperties.DELAY_AFTER_ERROR, CommonV2TranslationKeys.DELAY_AFTER_ERROR, "DELAY_AFTER_ERROR") {
            @Override
            public void addTo(Table table) {
                this.addAsTimeDurationColumnToWithVersionUpTo(table, Version.version(10, 3));
            }
        },
        //New column, added in 10.3
        ROUND_TRIP_CORRECTION("roundTripCorrection", DlmsProtocolProperties.ROUND_TRIP_CORRECTION, CommonV2TranslationKeys.ROUNDTRIP_CORRECTION, "ROUND_TRIP_CORRECTION") {
            @Override
            public void addTo(Table table) {
                this.addAsBigDecimalColumnToWithVersionSince(table, Version.version(10, 3));
            }
        };

        private final String javaName;
        private final String propertySpecName;
        private final TranslationKey translationKey;
        private final String databaseName;

        ActualFields(String javaName, String propertySpecName, TranslationKey translationKey, String databaseName) {
            this.javaName = javaName;
            this.propertySpecName = propertySpecName;
            this.translationKey = translationKey;
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

        protected void addAsBigDecimalColumnToWithVersionSince(Table table, Version since) {
            table
                    .column(this.databaseName())
                    .number()
                    .map(this.javaName())
                    .since(since)
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

        protected void addAsTimeDurationColumnToWithVersionUpTo(Table table, Version upTo) {
            table
                    .column(this.databaseName() + "VALUE")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(this.javaName() + ".count")
                    .upTo(upTo)
                    .add();
            table
                    .column(this.databaseName() + "UNIT")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(this.javaName() + ".timeUnitCode")
                    .upTo(upTo)
                    .add();
        }
    }
}