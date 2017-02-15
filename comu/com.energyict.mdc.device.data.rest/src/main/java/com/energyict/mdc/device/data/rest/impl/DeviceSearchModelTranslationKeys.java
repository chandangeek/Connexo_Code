/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum DeviceSearchModelTranslationKeys implements TranslationKey {
    NAME("name", "Name"),
    MRID("mRID", "MRID"),
    SERIALNUMBER("serialNumber", "Serial number"),
    DEVICE_TYPE_NAME("deviceTypeName", "Device type"),
    DEVICE_CONFIGURATION_NAME("deviceConfigurationName", "Device configuration"),
    STATE("state", "State"),
    BATCH("batch", "Batch"),
    HAS_OPEN_DATA_COLLECTION_ISSUES("hasOpenDataCollectionIssues", "Data collection issues"),
    SERVICE_CATEGORY("serviceCategory", "Service category"),
    USAGE_POINT("usagePoint", "Usage point"),
    YEAR_OF_CERTIFICATION("yearOfCertification", "Year of certification"),
    ESTIMATION_ACTIVE("estimationActive", "Data estimation"),
    MASTER_DEVICE_NAME("masterDeviceName", "Master device"),
    SHIPMENT_DATE("shipmentDate", "Shipment date"),
    INSTALLATION_DATE("installationDate", "Installation date"),
    DEACTIVATION_DATE("deactivationDate", "Deactivation date"),
    DECOMMISSION_DATE("decommissionDate", "Decommissioning date"),
    VALIDATION_ACTIVE("validationActive", "Data validation"),
    HAS_OPEN_DATA_VALIDATION_ISSUES("hasOpenDataValidationIssues", "Data validation issues"),
    LOCATION("location", "Location"),
    DEVICE_DATA_STATE_ACTIVE("deviceDataStateActive", "Active"),
    DEVICE_DATA_STATE_INACTIVE("deviceDataStateInactive", "Inactive")
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