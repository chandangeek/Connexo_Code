package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum DeviceSearchModelTranslationKeys implements TranslationKey {
    MRID("mRID", "mRID"),
    SERIALNUMBER("serialNumber", "Serial number"),
    DEVICETYPENAME("deviceTypeName", "Device type"),
    DEVICETYPEID("deviceTypeId", "Device type id"),
    DEVICECONFIGURATIONNAME("deviceConfigurationName", "Device configuration"),
    DEVICECONFIGURATIONID("deviceConfigurationId", "Device configuration id"),
    DEVICEPROTOCOLPLUGGEABLECLASSID("deviceProtocolPluggeableClassId", "Device protocol pluggable class id"),
    YEAROFCERTIFICATION("yearOfCertification", "Year of certification"),
    BATCH("batch", "Batch"),
    MASTERDEVICEMRID("masterDevicemRID", "Master device mRID"),
    MASTERDEVICEID("masterDeviceId", "Master device id"),
    NBROFDATACOLLECTIONISSUES("nbrOfDataCollectionIssues", "Number of data collection issues"),
    OPENDATAVALIDATIONISSUE("openDataValidationIssue", "Open data validation issue"),
    HASREGISTERS("hasRegisters", "Has registers"),
    HASLOGBOOKS("hasLogBooks", "Has logbooks"),
    HASLOADPROFILES("hasLoadProfiles", "Has load profiles"),
    ISDIRECTLYADDRESSED("isDirectlyAddressed", "Is directly addressed"),
    ISGATEWAY("isGateway", "Is gateway"),
    SERVICECATEGORY("serviceCategory", "Service category"),
    STATE("state", "State"),
    ;

    private String key;
    private String translation;

    DeviceSearchModelTranslationKeys(String key, String translation) {
        this.key = Keys.PREFIX + key;
        this.translation = translation;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.translation;
    }

    public static class Keys {
        public static final String PREFIX = "device.search.model.";

        private Keys() {
        }
    }
}