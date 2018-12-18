/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

public enum InboundServices {

    SAP_STATUS_CHANGE_REQUEST_CREATE("SapStatusChangeRequestCreate"),

    SAP_METER_READING_CREATE_REQUEST("SapMeterReadingRequest"),
    SAP_METER_READING_CREATE_BULK_REQUEST("SapMeterReadingBulkRequest"),
    SAP_METER_READING_RESULT_CONFIRMATION("SapMeterReadingResultConfirmation"),
    SAP_METER_READING_BULK_RESULT_CONFIRMATION("SapMeterReadingBulkResultConfirmation"),

    ;

    private String name;

    InboundServices(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}