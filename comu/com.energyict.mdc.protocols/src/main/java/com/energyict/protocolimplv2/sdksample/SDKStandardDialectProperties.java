/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.sdksample;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link SDKStandardProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-27 (09:37)
 */
class SDKStandardDialectProperties extends CommonDeviceProtocolDialectProperties {

    enum ActualFields {
        DO_SOME_THING("doSomething", "DoSomeThing", "DO_SOME_THING");

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
                    .booleanSpec()
                    .named(SDKTranslationKeys.DO_SOME_THING)
                    .fromThesaurus(thesaurus)
                    .finish();
        }

        public void addTo(Table table) {
            table
                .column(this.databaseName())
                .number().conversion(ColumnConversion.NUMBER2BOOLEAN)
                .map(this.javaName())
                .add();
        }

    }

    private boolean doSomething;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        Boolean flag = (Boolean) propertyValues.getProperty(ActualFields.DO_SOME_THING.propertySpecName());
        this.doSomething = flag != null && flag.booleanValue();
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.DO_SOME_THING.propertySpecName(), this.doSomething);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}