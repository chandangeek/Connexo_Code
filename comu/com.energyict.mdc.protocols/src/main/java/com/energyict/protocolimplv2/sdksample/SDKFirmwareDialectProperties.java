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
 * Provides an implementation for the {@link PersistentDomainExtension} interface for {@link SDKFirmwareProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-27 (09:37)
 */
class SDKFirmwareDialectProperties extends CommonDeviceProtocolDialectProperties {

    enum ActualFields {
        ACTIVE_METER_FIRMWARE_VERSION("activeMeterFirmwareVersion", SDKTranslationKeys.ACTIVE_METER_FIRMWARE_VERSION, "ActiveMeterFirmwareVersion", "ACTIVE_METER_FIRMWARE_VERSION"),
        PASSIVE_METER_FIRMWARE_VERSION("passiveMeterFirmwareVersion", SDKTranslationKeys.PASSIVE_METER_FIRMWARE_VERSION, "PassiveMeterFirmwareVersion", "PASSIVE_METER_FIRMWARE_VERSION"),
        ACTIVE_COMMUNICATION_FIRMWARE_VERSION("activeCommunicationFirmwareVersion", SDKTranslationKeys.ACTIVE_COMMUNICATION_FIRMWARE_VERSION, "ActiveCommunicationFirmwareVersion", "ACTIVE_COMM_FIRMWARE_VERSION"),
        PASSIVE_COMMUNICATION_FIRMWARE_VERSION("passiveCommunicationFirmwareVersion", SDKTranslationKeys.PASSIVE_COMMUNICATION_FIRMWARE_VERSION, "PassiveCommunicationFirmwareVersion", "PASSIVE_COMM_FIRMWARE_VERSION");

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
    private String activeMeterFirmwareVersion;
    @Size(max=Table.MAX_STRING_LENGTH)
    private String passiveMeterFirmwareVersion;
    @Size(max=Table.MAX_STRING_LENGTH)
    private String activeCommunicationFirmwareVersion;
    @Size(max=Table.MAX_STRING_LENGTH)
    private String passiveCommunicationFirmwareVersion;

    @Override
    protected void copyActualPropertiesFrom(CustomPropertySetValues propertyValues) {
        this.activeMeterFirmwareVersion = (String) propertyValues.getProperty(ActualFields.ACTIVE_METER_FIRMWARE_VERSION.propertySpecName());
        this.passiveMeterFirmwareVersion = (String) propertyValues.getProperty(ActualFields.PASSIVE_METER_FIRMWARE_VERSION.propertySpecName());
        this.activeCommunicationFirmwareVersion = (String) propertyValues.getProperty(ActualFields.ACTIVE_COMMUNICATION_FIRMWARE_VERSION.propertySpecName());
        this.passiveCommunicationFirmwareVersion = (String) propertyValues.getProperty(ActualFields.PASSIVE_COMMUNICATION_FIRMWARE_VERSION.propertySpecName());
    }

    @Override
    protected void copyActualPropertiesTo(CustomPropertySetValues propertySetValues) {
        this.setPropertyIfNotNull(propertySetValues, ActualFields.ACTIVE_METER_FIRMWARE_VERSION.propertySpecName(), this.activeMeterFirmwareVersion);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.PASSIVE_METER_FIRMWARE_VERSION.propertySpecName(), this.passiveMeterFirmwareVersion);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.ACTIVE_COMMUNICATION_FIRMWARE_VERSION.propertySpecName(), this.activeCommunicationFirmwareVersion);
        this.setPropertyIfNotNull(propertySetValues, ActualFields.PASSIVE_COMMUNICATION_FIRMWARE_VERSION.propertySpecName(), this.passiveCommunicationFirmwareVersion);
    }

    @Override
    public void validateDelete() {
        // Nothing to validate
    }

}