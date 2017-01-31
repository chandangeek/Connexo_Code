/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.energyict.protocolimplv2.dlms.DlmsProperties;
import com.energyict.protocolimplv2.edp.EDPProperties;

import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link EDPSerialDeviceProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-26 (17:29)
 */
public class EDPDeviceProtocolDialectProperties extends SerialDeviceProtocolDialectProperties {

    enum EDPFields {
        SERVER_LOWER_MAC_ADDRESS("serverLowerMacAddress", EDPProperties.SERVER_LOWER_MAC_ADDRESS, "SERVER_LOWER_MAC_ADDRESS") {
            @Override
            public void addTo(Table table) {
                this.addAsBigDecimalColumnTo(table);
            }

            @Override
            public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
                return propertySpecService
                        .bigDecimalSpec()
                        .named(this.propertySpecName(), DlmsProperties.TranslationKeys.SERVER_LOWER_MAC_ADDRESS_TK)
                        .fromThesaurus(thesaurus)
                        .setDefaultValue(BigDecimal.valueOf(16))
                        .finish();
            }
        };

        private final String javaName;
        private final String propertySpecName;
        private final String databaseName;

        EDPFields(String javaName, String propertySpecName, String databaseName) {
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

        public abstract PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus);

        public abstract void addTo(Table table);

        protected void addAsBigDecimalColumnTo(Table table) {
            table
                .column(this.databaseName())
                .number()
                .map(this.javaName())
                .add();
        }

    }

    private BigDecimal serverLowerMacAddress;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        super.copyActualPropertiesFrom(propertyValues);
        this.serverLowerMacAddress = (BigDecimal) propertyValues.getProperty(EDPFields.SERVER_LOWER_MAC_ADDRESS.propertySpecName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        super.copyActualPropertiesTo(propertySetValues);
        this.setPropertyIfNotNull(propertySetValues, EDPFields.SERVER_LOWER_MAC_ADDRESS.propertySpecName(), this.serverLowerMacAddress);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}