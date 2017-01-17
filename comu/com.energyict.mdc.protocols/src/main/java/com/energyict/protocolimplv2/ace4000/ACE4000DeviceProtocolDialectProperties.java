package com.energyict.protocolimplv2.ace4000;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;
import com.energyict.mdc.tasks.ACE4000DeviceProtocolDialect;

import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link ACE4000DeviceProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-26 (11:47)
 */
class ACE4000DeviceProtocolDialectProperties extends CommonDeviceProtocolDialectProperties {

    private BigDecimal timeoutMillis;
    private BigDecimal retries;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.timeoutMillis = (BigDecimal) propertyValues.getProperty(ActualFields.TIMEOUT_PROPERTY.propertySpecName());
        this.retries = (BigDecimal) propertyValues.getProperty(ActualFields.RETRIES.propertySpecName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.TIMEOUT_PROPERTY.propertySpecName(), this.timeoutMillis);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.RETRIES.propertySpecName(), this.retries);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

    enum ActualFields {
        TIMEOUT_PROPERTY("timeoutMillis", ACE4000Properties.TranslationKeys.TIMEOUT, "TIMEOUTMILLIS"),
        RETRIES("retries", ACE4000Properties.TranslationKeys.RETRIES, "RETRIES");

        private final String javaName;
        private final TranslationKey nameTranslationKey;
        private final String databaseName;

        ActualFields(String javaName, TranslationKey nameTranslationKey, String databaseName) {
            this.javaName = javaName;
            this.nameTranslationKey = nameTranslationKey;
            this.databaseName = databaseName;
        }

        public String javaName() {
            return this.javaName;
        }

        public String propertySpecName() {
            return this.javaName();
        }

        public String databaseName() {
            return this.databaseName;
        }

        public void addTo(Table table) {
            table
                    .column(this.databaseName())
                    .number()
                    .map(this.javaName())
                    .add();
        }
    }
}