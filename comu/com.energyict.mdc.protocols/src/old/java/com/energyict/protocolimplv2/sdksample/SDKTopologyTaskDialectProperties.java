package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;

import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link SDKTopologyTaskProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-27 (09:37)
 */
class SDKTopologyTaskDialectProperties extends CommonDeviceProtocolDialectProperties {

    @Size(max = Table.MAX_STRING_LENGTH)
    private String slaveOneSerialNumber;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String slaveTwoSerialNumber;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.slaveOneSerialNumber = (String) propertyValues.getProperty(ActualFields.SLAVE_ONE_SERIAL_NUMBER.propertySpecName());
        this.slaveTwoSerialNumber = (String) propertyValues.getProperty(ActualFields.SLAVE_TWO_SERIAL_NUMBER.propertySpecName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.SLAVE_ONE_SERIAL_NUMBER.propertySpecName(), this.slaveOneSerialNumber);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.SLAVE_TWO_SERIAL_NUMBER.propertySpecName(), this.slaveTwoSerialNumber);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

    enum ActualFields {
        SLAVE_ONE_SERIAL_NUMBER("slaveOneSerialNumber", SDKTranslationKeys.SLAVE_ONE_SERIAL_NUMBER, "SlaveOneSerialNumber", "SLAVE_ONE_SERIAL_NUMBER"),
        SLAVE_TWO_SERIAL_NUMBER("slaveTwoSerialNumber", SDKTranslationKeys.SLAVE_TWO_SERIAL_NUMBER, "SlaveTwoSerialNumber", "SLAVE_TWO_SERIAL_NUMBER");

        private final String javaName;
        private final TranslationKey nameTranslationKey;
        private final String propertySpecName;
        private final String databaseName;

        ActualFields(String javaName, TranslationKey nameTranslationKey, String propertySpecName, String databaseName) {
            this.javaName = javaName;
            this.nameTranslationKey = nameTranslationKey;
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