/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

public enum AdditionalProperties {

    REGISTER_SEARCH_ATTEMPTS("com.elster.jupiter.sap.registersearchattempts", 3),
    REGISTER_SEARCH_INTERVAL("com.elster.jupiter.sap.registersearchinterval", 5),
    READING_DATE_WINDOW("com.elster.jupiter.sap.readingdatewindow", 4320),
    READING_COLLECTION_INTERVAL("com.elster.jupiter.sap.readingcollectioninterval", 60),
    CONFIRMATION_TIMEOUT("com.elster.jupiter.sap.confirmationtimeout", 5),
    CHECK_SCHEDULED_REQUESTS_FREQUENCY("com.elster.jupiter.sap.checkscheduledrequestsfrequency", 60),
    CHECK_SCHEDULED_READING_ATTEMPTS("com.elster.jupiter.sap.checkscheduledreadingattempts", 3),
    CHECK_SCHEDULED_READING_INTERVAL("com.elster.jupiter.sap.checkscheduledreadinginterval", 1440),
    CHECK_CONFIRMATION_TIMEOUT_FREQUENCY("com.elster.jupiter.sap.checkconfirmationtimeoutfrequency", 1),
    SCHEDULED_METER_READING_DATE_SHIFT_PERIODIC("com.elster.jupiter.sap.sheduledmeterreadingdateshift.periodic", 1),
    ;

    private String key;
    private Integer defaultValue;

    AdditionalProperties(String key, Integer defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public Integer getDefaultValue() {
        return defaultValue;
    }
}