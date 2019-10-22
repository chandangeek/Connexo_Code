/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.task.CheckConfirmationTimeoutHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.impl.task.CheckScheduledRequestHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.impl.task.SearchDataSourceHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.impl.task.ConnectionStatusChangeMessageHandlerFactory;

public enum TranslationKeys implements TranslationKey {

    TIMEOUT_PROPERTY(DataExportWebService.TIMEOUT_PROPERTY_KEY, "Timeout"),
    TIMEOUT_DESCRIPTION(DataExportWebService.TIMEOUT_PROPERTY_KEY + ".description", "Set a liberal timeout greater than 10 seconds but below 1 day to avoid system overload."),

    // Service calls
    DOMAIN_NAME("serviceCall", "Service call"),
    PARENT_SERVICE_CALL("parentServiceCall", "Parent service call"),
    METER_READING_DOCUMENT_ID("meterReadingDocumentId", "Meter reading document ID"),
    DEVICE_ID("deviceId", "Device ID"),
    DEVICE_NAME("deviceName", "Device name"),
    LRN("lrn", "LRN"),
    READING_REASON_CODE("readingReasonCode", "Reading reason code"),
    SCHEDULED_READING_DATE("scheduledReadingDate", "Scheduled reading date"),
    DATA_SOURCE("dataSource", "Data source"),
    FUTURE_CASE("futureCase", "Future case"),
    PROCESSING_DATE("processingDate", "Processing date"),
    REQUEST_UUID("requestID", "Request ID"),
    REFERENCE_ID("referenceID", "Reference ID"),
    ATTEMPT_NUMBER("attemptNumber", "Attempt number"),
    ACTUAL_READING_DATE("actualReadingDate", "Actual reading date"),
    READING("reading", "Reading"),
    CONFIRMATION_TIME("confirmationTime", "Confirmation time"),
    CALLS_SUCCESS("callsSuccess", "Success calls counter"),
    CALLS_ERROR("callsError", "Error calls counter"),
    CALLS_EXPECTED("callsExpected", "Expected calls counter"),
    ERROR_MESSAGE("errorMessage", "Error message"),
    ERROR_CODE("errorCode", "Error code"),
    CONFIRMATION_URL("confirmationUrl", "Confirmation URL"),
    RESULT_URL("resultUrl", "Result URL"),
    BULK("bulk", "Bulk"),
    ID("id", "ID"),
    CATEGORY_CODE("categoryCode", "Category code"),
    REASON_CODE("reasonCode", "Reason code"),
    PROCESS_DATE("processDate", "Planned processing date"),
    OBIS("obis","OBIS code"),
    START_DATE("startDate","Time slice start date"),
    END_DATE("endDate","Time slice end date"),
    RETURN_CODE("returnCode", "Return code"),
    SERIAL_ID("serialId", "Serial id"),
    INTERVAL("interval","Interval length"),
    CONNECTION_STATUS_CHANGE_MESSAGE_HANDLER(ConnectionStatusChangeMessageHandlerFactory.TASK_SUBSCRIBER,
            ConnectionStatusChangeMessageHandlerFactory.TASK_SUBSCRIBER_DISPLAYNAME),
    PROFILE_ID("profileId", "Profile id"),
    EXPORTER("exporter", "Exporter"),
    EXPORTER_DESCRIPTION("exporterDescription", "Specifies the type of exporter to be used to select and export readings"),
    DEVICE_TYPE("deviceType", "Device type"),
    SHIPMENT_DATE("shipmentDate", "Shipment date"),
    MANUFACTURER("manufacturer", "Manufacturer"),
    MODEL_NUMBER("modelNumber", "Model number"),
    CHANNEL_OR_REGISTER_ID("channelOrRegisterId", "Channel/register id"),
    MATERIAL_ID("materialId", "Material id"),

    // Tasks
    SEARCH_DATA_SOURCE_SUBSCRIBER_NAME(SearchDataSourceHandlerFactory.SEARCH_DATA_SOURCE_TASK_SUBSCRIBER, SearchDataSourceHandlerFactory.SEARCH_DATA_SOURCE_TASK_DISPLAYNAME),
    CHECK_CONFIRMATION_TIMEOUT_SUBSCRIBER_NAME(CheckConfirmationTimeoutHandlerFactory.CHECK_CONFIRMATION_TIMEOUT_TASK_SUBSCRIBER, CheckConfirmationTimeoutHandlerFactory.CHECK_CONFIRMATION_TIMEOUT_TASK_DISPLAYNAME),
    CHECK_SCHEDULED_REQUEST_SUBSCRIBER_NAME(CheckScheduledRequestHandlerFactory.CHECK_SCHEDULED_REQUEST_TASK_SUBSCRIBER, CheckScheduledRequestHandlerFactory.CHECK_SCHEDULED_REQUEST_TASK_DISPLAYNAME),

    // CPS
    CONNECTION_STATUS_CHANGE_CPS("servicecall.cps.connection.status.change",
            ConnectionStatusChangeCustomPropertySet.class.getSimpleName()),
    MASTER_UTILITIES_DEVICE_CREATE_REQUEST_CPS("servicecall.cps.master.utilities.device.create.request",
            MasterUtilitiesDeviceCreateRequestCustomPropertySet.class.getSimpleName()),
    MASTER_UTILITIES_DEVICE_REGISTER_CREATE_REQUEST_CPS("servicecall.cps.master.utilities.device.register.create.request",
            MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet.class.getSimpleName()),
    SUB_MASTER_UTILITIES_DEVICE_REGISTER_CREATE_REQUEST_CPS("servicecall.cps.sub.master.utilities.device.register.create.request",
            SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet.class.getSimpleName()),
    UTILITIES_DEVICE_CREATE_REQUEST_CPS("servicecall.cps.utilities.device.create.request",
            UtilitiesDeviceCreateRequestCustomPropertySet.class.getSimpleName()),
    UTILITIES_DEVICE_REGISTER_CREATE_REQUEST_CPS("servicecall.cps.utilities.device.register.create.request",
            UtilitiesDeviceRegisterCreateRequestCustomPropertySet.class.getSimpleName()),

    //Micro checks
    COMMUNICATION("sap.microchecks.category.maintenance", "Communication"),
    AT_LEAST_ONE_LRN_WAS_SET("sap.microchecks.AtLeastOneLrnWasSet", "At least one LRN was set"),
    AT_LEAST_ONE_LRN_WAS_SET_DESCRIPTION("sap.microchecks.AtLeastOneLrnWasSet.description", "Check if at least one Logical Register Number was set on the device"),
    ;

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
