/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl;

public enum AdditionalProperties {

    OBJECT_SEARCH_ATTEMPTS("com.elster.jupiter.sap.objectsearchattempts", 2),
    OBJECT_SEARCH_INTERVAL("com.elster.jupiter.sap.objectsearchinterval", 1),
    READING_DATE_WINDOW("com.elster.jupiter.sap.readingdatewindow", 4320),
    READING_COLLECTION_INTERVAL("com.elster.jupiter.sap.readingcollectioninterval", 60),
    CONFIRMATION_TIMEOUT("com.elster.jupiter.sap.confirmationtimeout", 5),
    CHECK_SCHEDULED_REQUESTS_FREQUENCY("com.elster.jupiter.sap.checkscheduledrequestsfrequency", 60),
    CHECK_SCHEDULED_READING_ATTEMPTS("com.elster.jupiter.sap.checkscheduledreadingattempts", 3),
    CHECK_SCHEDULED_READING_INTERVAL("com.elster.jupiter.sap.checkscheduledreadinginterval", 1440),
    CHECK_CONFIRMATION_TIMEOUT_FREQUENCY("com.elster.jupiter.sap.checkconfirmationtimeoutfrequency", 1),
    SCHEDULED_METER_READING_DATE_SHIFT_PERIODIC("com.elster.jupiter.sap.sheduledmeterreadingdateshift.periodic", 1),
    LRN_END_INTERVAL("com.elster.jupiter.sap.lrnendinterval", 1440),
    CHECK_STATUS_CHANGE_TIMEOUT("com.elster.jupiter.sap.statuschangetimeout", 60),
    CHECK_STATUS_CHANGE_FREQUENCY("com.elster.jupiter.sap.statuschangetaskfrequency", 30),
    UPDATE_SAP_EXPORT_TASK_PROPERTY("com.elster.jupiter.sap.updatesapexporttaskinterval", 7),
    BACKWARD_READING_DATE_WINDOW("com.elster.jupiter.sap.backwardreadingdatewindow", 0),
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