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
    SAP_UTILITIES_DEVICE_ERP_SMART_METER_CREATE_REQUEST_C_IN("SAP UtilitiesDeviceERPSmartMeterCreateRequest_C_In"),
    SAP_UTILITIES_DEVICE_ERP_SMART_METER_BULK_CREATE_REQUEST_C_IN("SAP UtilitiesDeviceERPSmartMeterBulkCreateRequest_C_In"),
    SAP_UTILITIES_DEVICE_ERP_SMART_METER_REGISTER_CREATE_REQUEST_C_IN("SAP UtilitiesDeviceERPSmartMeterRegisterCreateRequest_C_In"),
    SAP_UTILITIES_DEVICE_ERP_SMART_METER_REGISTER_BULK_CREATE_REQUEST_C_IN("SAP UtilitiesDeviceERPSmartMeterRegisterBulkCreateRequest_C_In"),
    SAP_UTILITIES_DEVICE_ERP_SMART_METER_LOCATION_NOTIFICATION_C_IN("SAP UtilitiesDeviceERPSmartMeterLocationNotification_C_In"),
    SAP_UTILITIES_DEVICE_ERP_SMART_METER_LOCATION_BULK_NOTIFICATION_C_IN("SAP UtilitiesDeviceERPSmartMeterLocationBulkNotification_C_In"),
    SAP_POINT_OF_DELIVERY_ASSIGNED_NOTIFICATION_C_IN
            ("SAP PointOfDeliveryAssignedNotification_C_In"),
    SAP_POINT_OF_DELIVERY_BULK_ASSIGNED_NOTIFICATION_C_IN
            ("SAP PointOfDeliveryBulkAssignedNotification_C_In"),
    ;

    private String name;

    InboundServices(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}