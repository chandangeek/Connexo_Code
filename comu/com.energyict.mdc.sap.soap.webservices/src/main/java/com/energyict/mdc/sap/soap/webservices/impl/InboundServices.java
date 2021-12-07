/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

public enum InboundServices {

    SAP_STATUS_CHANGE_REQUEST_CREATE("SAP ConnectionStatusChangeRequest"),

    SAP_METER_READING_CREATE_REQUEST("SAP MeterReadingRequest"),
    SAP_METER_READING_CREATE_BULK_REQUEST("SAP MeterReadingBulkRequest"),
    SAP_METER_READING_RESULT_CONFIRMATION("SAP MeterReadingResultConfirmation"),
    SAP_METER_READING_BULK_RESULT_CONFIRMATION("SAP MeterReadingBulkResultConfirmation"),
    SAP_UTILITIES_DEVICE_ERP_SMART_METER_CREATE_REQUEST_C_IN("SAP SmartMeterCreateRequest"),
    SAP_UTILITIES_DEVICE_ERP_SMART_METER_BULK_CREATE_REQUEST_C_IN("SAP SmartMeterBulkCreateRequest"),
    SAP_UTILITIES_DEVICE_ERP_SMART_METER_REGISTER_CREATE_REQUEST_C_IN("SAP SmartMeterRegisterCreateRequest"),
    SAP_UTILITIES_DEVICE_ERP_SMART_METER_REGISTER_BULK_CREATE_REQUEST_C_IN("SAP SmartMeterRegisterBulkCreateRequest"),
    SAP_UTILITIES_DEVICE_ERP_SMART_METER_LOCATION_NOTIFICATION_C_IN("SAP SmartMeterLocationNotification"),
    SAP_UTILITIES_DEVICE_ERP_SMART_METER_LOCATION_BULK_NOTIFICATION_C_IN("SAP SmartMeterLocationBulkNotification"),
    SAP_POINT_OF_DELIVERY_ASSIGNED_NOTIFICATION_C_IN
            ("SAP PointOfDeliveryAssignedNotification"),
    SAP_POINT_OF_DELIVERY_BULK_ASSIGNED_NOTIFICATION_C_IN
            ("SAP PointOfDeliveryBulkAssignedNotification"),
    SAP_MEASUREMENT_TASK_ASSIGNMENT_CHANGE_REQUEST("SAP MeasurementTaskAssignmentChangeRequest"),
    SAP_SMART_METER_METER_READING_DOCUMENT_ERP_CANCELLATION_CONFIRMATION("SAP MeterReadingCancellationRequest"),
    SAP_SMART_METER_METER_READING_DOCUMENT_ERP_BULK_CANCELLATION_CONFIRMATION("SAP MeterReadingBulkCancellationRequest"),
    SAP_METER_REGISTER_CHANGE_REQUEST("SAP MeterRegisterChangeRequest"),
    SAP_METER_REGISTER_BULK_CHANGE_REQUEST("SAP MeterRegisterBulkChangeRequest"),
    SAP_STATUS_CHANGE_REQUEST_CANCELLATION("SAP ConnectionStatusChangeCancellationRequest"),
    SAP_STATUS_CHANGE_REQUEST_BULK_CREATE("SAP ConnectionStatusChangeBulkRequest"),
    SAP_METER_READING_RESULT_CREATE_REQUEST("SAP SmartMeterReadingResultCreateRequest"),
    SAP_UTILITIES_DEVICE_ERP_SMART_METER_CHANGE_REQUEST_C_IN("SAP SmartMeterChangeRequest"),
    ;

    private String name;

    InboundServices(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}