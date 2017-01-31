/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;

import javax.validation.constraints.Size;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link SDKLoadProfileProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-27 (09:37)
 */
class SDKLoadProfileDialectProperties extends CommonDeviceProtocolDialectProperties {

    enum ActualFields {
        NOT_SUPPORTED_LOAD_PROFILE("notSupportedLoadProfile", "NotSupportedLoadProfile", "NOT_SUPPORTED_LOAD_PROFILE");

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

        public PropertySpec propertySpec(PropertySpecService propertySpecService, Thesaurus thesaurus) {
            return propertySpecService
                    .obisCodeSpec()
                    .named(SDKTranslationKeys.NOT_SUPPORTED_LOAD_PROFILE)
                    .fromThesaurus(thesaurus)
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
    private String notSupportedLoadProfile;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        ObisCode notSupportedLoadProfileObisCode = (ObisCode) propertyValues.getProperty(ActualFields.NOT_SUPPORTED_LOAD_PROFILE.propertySpecName());
        if (notSupportedLoadProfileObisCode != null) {
            this.notSupportedLoadProfile = notSupportedLoadProfileObisCode.toString();
        }
        else {
            this.notSupportedLoadProfile = null;
        }
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.NOT_SUPPORTED_LOAD_PROFILE.propertySpecName(), ObisCode.fromString(this.notSupportedLoadProfile));
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}