/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.sap.soap.webservices.impl.messagehandlers.SAPRegisteredNotificationOnDeviceMessageHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterPodNotificationCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceLocationNotificationCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.PodNotificationCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceLocationNotificationCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.MasterConnectionStatusChangeCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterUtilitiesDeviceMeterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.UtilitiesDeviceMeterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.task.CheckConfirmationTimeoutHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.impl.task.CheckScheduledRequestHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.impl.task.CheckStatusChangeCancellationHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.impl.task.SearchDataSourceHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.impl.task.ConnectionStatusChangeMessageHandlerFactory;
import com.energyict.mdc.sap.soap.webservices.impl.task.UpdateSapExportTaskHandlerFactory;

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
    EXTRA_DATA_SOURCE("extraDataSource", "Extra data source"),
    FUTURE_CASE("futureCase", "Future case"),
    PROCESSING_DATE("processingDate", "Processing date"),
    REQUEST_ID("requestID", "Request ID"),
    UUID("UUID", "UUID"),
    CREATE_REQUEST("createRequest", "Create request"),
    REFERENCE_ID("referenceID", "Reference ID"),
    REFERENCE_UUID("referenceUUID", "Reference UUID"),
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
    OBIS("obis", "OBIS code"),
    START_DATE("startDate", "Time slice start date"),
    END_DATE("endDate", "Time slice end date"),
    RETURN_CODE("returnCode", "Return code"),
    SERIAL_ID("serialId", "Serial id"),
    RECURRENCE_CODE("recurrenceCode", "Recurrence Code"),
    CONNECTION_STATUS_CHANGE_MESSAGE_HANDLER(ConnectionStatusChangeMessageHandlerFactory.TASK_SUBSCRIBER,
            ConnectionStatusChangeMessageHandlerFactory.TASK_SUBSCRIBER_DISPLAYNAME),
    EXPORTER("exporter", "Exporter"),
    EXPORTER_DESCRIPTION("exporterDescription", "Specifies the type of exporter to be used to select and export readings"),
    DEVICE_TYPE("deviceType", "Device type"),
    SHIPMENT_DATE("shipmentDate", "Shipment date"),
    DEACTIVATION_DATE("deactivationDate", "Deactivation date"),
    MANUFACTURER("manufacturer", "Manufacturer"),
    MANUFACTURER_SERIAL_ID("manufacturerSerialId", "Manufacturer serial id"),
    CHANNEL_OR_REGISTER_ID("channelOrRegisterId", "Channel/register id"),
    MATERIAL_ID("materialId", "Material id"),
    NEXT_READING_ATTEMPT_DATE("nextReadingAttemptDate", "Next reading attempt date"),
    READING_ATTEMPT("readingAttempt", "Reading attempt"),
    CANCELLED_BY_SAP("cancelledBySap", "Cancelled by SAP"),
    CANCELLED_BY_SAP_DESCRIPTION("cancelledBySapDescription", "The property is used to distinguish service call cancelled manually in Connexo / by SAP"),
    TIME_ZONE("timeZone", "Time zone"),
    COM_TASK_EXECUTION_ID("comTaskExecutionId", "Communication task execution id"),
    DIVISION_CATEGORY("divisionCategory", "Division category code"),
    REQUESTED_SCHEDULED_READING_DATE("requestedScheduledReadingDate", "Requested scheduled reading date"),
    AT_LEAST_ONE_OF("atLeastOneOf", "at least one of"),
    LOCATION_ID("locationId", "Location ID"),
    INSTALLATION_NUMBER("installationNumber", "Installation number"),
    POD_ID("podId", "Point of delivery ID"),
    METER_READING_DATE_TIME("meterReadingDateTime", "Meter reading timestamp"),
    METER_READING_VALUE("meterReadingValue", "Meter reading value"),
    REGISTER_ID("registerId", "Register id"),
    TOTAL_DIGIT_NUMBER_VALUE("totalDigitNumberValue", "Total digit number value"),
    FRACTION_DIGIT_NUMBER_VALUE("fractionDigitNumberValue", "Fraction digit number value"),
    ACTIVATION_GROUP_AMI_FUNCTIONS("activationGroupAMIFunctions", "Activation Group AMI Functions"),
    METER_FUNCTION_GROUP("meterFunctionGroup", "Meter function group"),
    ATTRIBUTE_MESSAGE("attributeMessage", "Attribute message"),
    CHARACTERISTICS_ID("characteristicsId", "Characteristics id"),
    CHARACTERISTICS_VALUE("characteristicsValue", "Characteristics value"),
    DIVISION_CATEGORY_CODE("divisionCategoryCode", "Division category code"),


    // Tasks
    SEARCH_DATA_SOURCE_SUBSCRIBER_NAME(SearchDataSourceHandlerFactory.SEARCH_DATA_SOURCE_TASK_SUBSCRIBER, SearchDataSourceHandlerFactory.SEARCH_DATA_SOURCE_TASK_DISPLAYNAME),
    CHECK_CONFIRMATION_TIMEOUT_SUBSCRIBER_NAME(CheckConfirmationTimeoutHandlerFactory.CHECK_CONFIRMATION_TIMEOUT_TASK_SUBSCRIBER, CheckConfirmationTimeoutHandlerFactory.CHECK_CONFIRMATION_TIMEOUT_TASK_DISPLAYNAME),
    CHECK_SCHEDULED_REQUEST_SUBSCRIBER_NAME(CheckScheduledRequestHandlerFactory.CHECK_SCHEDULED_REQUEST_TASK_SUBSCRIBER, CheckScheduledRequestHandlerFactory.CHECK_SCHEDULED_REQUEST_TASK_DISPLAYNAME),
    UPDATE_SAP_EXPORT_TASK_SUBSCRIBER_NAME(UpdateSapExportTaskHandlerFactory.UPDATE_SAP_EXPORT_TASK_SUBSCRIBER, UpdateSapExportTaskHandlerFactory.UPDATE_SAP_EXPORT_TASK_DISPLAYNAME),
    CHECK_STATUS_CHANGE_CANCELLATION_TASK_SUBSCRIBER_NAME(CheckStatusChangeCancellationHandlerFactory.CHECK_STATUS_CHANGE_CANCELLATION_TASK_SUBSCRIBER, CheckStatusChangeCancellationHandlerFactory.CHECK_STATUS_CHANGE_CANCELLATION_TASK_DISPLAYNAME),

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
    MASTER_CONNECTION_STATUS_CHANGE_CPS("servicecall.cps.master.connection.status.change",
            MasterConnectionStatusChangeCustomPropertySet.class.getSimpleName()),
    MASTER_UTILITIES_DEVICE_LOCATION_NOTIFICATION_CPS("servicecall.cps.master.utilities.device.location.notification",
            MasterUtilitiesDeviceLocationNotificationCustomPropertySet.class.getSimpleName()),
    UTILITIES_DEVICE_LOCATION_NOTIFICATION_CPS("servicecall.cps.utilities.device.location.notification",
            UtilitiesDeviceLocationNotificationCustomPropertySet.class.getSimpleName()),
    MASTER_POD_NOTIFICATION_CPS("servicecall.cps.master.pod.notification",
            MasterPodNotificationCustomPropertySet.class.getSimpleName()),
    POD_NOTIFICATION_CPS("servicecall.cps.pod.notification",
            PodNotificationCustomPropertySet.class.getSimpleName()),
    MASTER_UTILITIES_DEVICE_METER_CHANGE_CPS("servicecall.cps.master.utilities.device.meter.change.request", MasterUtilitiesDeviceMeterChangeRequestCustomPropertySet.class.getSimpleName()),
    UTILITIES_DEVICE_METER_CHANGE_CPS("servicecall.cps.master.utilities.device.meter.change.request", UtilitiesDeviceMeterChangeRequestCustomPropertySet.class.getSimpleName()),

    //Micro checks
    COMMUNICATION("sap.microchecks.category.maintenance", "Communication"),
    AT_LEAST_ONE_LRN_WAS_SET("sap.microchecks.AtLeastOneLrnWasSet", "At least one LRN was set"),
    AT_LEAST_ONE_LRN_WAS_SET_DESCRIPTION("sap.microchecks.AtLeastOneLrnWasSet.description", "Check if at least one Logical Register Number was set on the device"),

    SAPREGISTEREDNOTIFICATION_SUBSCRIBER(SAPRegisteredNotificationOnDeviceMessageHandlerFactory.BULK_SAPREGISTEREDNOTIFICATION_QUEUE_SUBSCRIBER, SAPRegisteredNotificationOnDeviceMessageHandlerFactory.BULK_SAPREGISTEREDNOTIFICATION_QUEUE_DISPLAYNAME);

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
