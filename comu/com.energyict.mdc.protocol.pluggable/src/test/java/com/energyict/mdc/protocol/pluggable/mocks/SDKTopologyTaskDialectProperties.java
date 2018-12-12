/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.mocks;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;

import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link SDKTopologyTaskProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-30 (12:35)
 */
class SDKTopologyTaskDialectProperties extends CommonDeviceProtocolDialectProperties {

    enum ActualFields {
        SLAVE_ONE_SERIAL_NUMBER("slaveOneSerialNumber", "SlaveOneSerialNumber", "SLAVE_ONE_SERIAL_NUMBER"),
        SLAVE_TWO_SERIAL_NUMBER("slaveTwoSerialNumber", "SlaveTwoSerialNumber", "SLAVE_TWO_SERIAL_NUMBER");

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
                    .stringSpec()
                    .named(this.propertySpecName(), this.propertySpecName())
                    .describedAs(this.propertySpecName())
                    .finish();
        }

        public void addTo(Table table) {
            table
                .column(this.databaseName())
                .varChar()
                .map(this.javaName())
                .add();
        }

    }

    @Size(max=Table.MAX_STRING_LENGTH)
    private String slaveOneSerialNumber;
    @Size(max=Table.MAX_STRING_LENGTH)
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

}