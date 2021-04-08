/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;

import test.com.energyict.protocolimplv2.sdksample.SDKCreditTaskProtocolDialectProperties;

import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link SDKCreditTaskProtocolDialectProperties}.
 *
 * @author dborisov H403395 dmitriy.borisov@orioninc.com
 * @since 8/04/2021 - 13:10
 */
class SDKCreditDialectProperties extends CommonDeviceProtocolDialectProperties {

    @Size(max = Table.MAX_STRING_LENGTH)
    private String creditType;
    @Size(max = Table.MAX_STRING_LENGTH)
    private String creditAmount;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.creditType     = (String) propertyValues.getProperty(ActualFields.CREDIT_TYPE.propertySpecName());
        this.creditAmount   = (String) propertyValues.getProperty(ActualFields.CREDIT_AMOUNT.propertySpecName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.CREDIT_TYPE.propertySpecName(), this.creditType);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.CREDIT_AMOUNT.propertySpecName(), this.creditAmount);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

    enum ActualFields {
        CREDIT_TYPE("creditType", "creditType", "CREDIT_TYPE"),
        CREDIT_AMOUNT("creditAmount", "creditAmount", "CREDIT_AMOUNT");

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