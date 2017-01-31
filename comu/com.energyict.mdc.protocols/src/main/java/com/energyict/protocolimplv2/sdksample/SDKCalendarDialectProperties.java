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
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link SDKCalendarProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-06-17 (12:41)
 */
class SDKCalendarDialectProperties extends CommonDeviceProtocolDialectProperties {

    enum ActualFields {
        ACTIVE_CALENDAR_NAME("activeCalendarName", SDKTranslationKeys.ACTIVE_CALENDAR_NAME, "ActiveCalendar", "ACTIVE_CALENDAR"),
        PASSIVE_CALENDAR_NAME("passiveCalendarName", SDKTranslationKeys.ACTIVE_CALENDAR_NAME, "PassiveCalendar", "PASSIVE_CALENDAR");

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
    private String activeCalendarName;
    @Size(max=Table.MAX_STRING_LENGTH)
    private String passiveCalendarName;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.activeCalendarName = (String) propertyValues.getProperty(ActualFields.ACTIVE_CALENDAR_NAME.propertySpecName());
        this.passiveCalendarName = (String) propertyValues.getProperty(ActualFields.PASSIVE_CALENDAR_NAME.propertySpecName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.ACTIVE_CALENDAR_NAME.propertySpecName(), this.activeCalendarName);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.PASSIVE_CALENDAR_NAME.propertySpecName(), this.passiveCalendarName);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}