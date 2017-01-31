/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;

import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link SDKBreakerProtocolDialect}.
 *
 * @author sva
 * @since 8/04/2016 - 13:10
 */
class SDKBreakerDialectProperties extends CommonDeviceProtocolDialectProperties {

    enum ActualFields {
        BREAKER_STATUS("breakerStatus", SDKTranslationKeys.BREAKER_STATUS, "breakerStatus", "BREAKER_STATUS");

        private static final String CONNECTED = "connected";
        private static final String DISCONNECTED = "disconnected";
        private static final String ARMED = "armed";
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

        public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService
                    .stringSpec()
                    .named(this.propertySpecName(), nameTranslationKey)
                    .fromThesaurus(thesaurus)
                    .addValues(CONNECTED, DISCONNECTED, ARMED)
                    .setDefaultValue(CONNECTED)
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

    @Size(max = Table.MAX_STRING_LENGTH)
    private String breakerStatus;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.breakerStatus = (String) propertyValues.getProperty(ActualFields.BREAKER_STATUS.propertySpecName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.BREAKER_STATUS.propertySpecName(), this.breakerStatus);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }
}