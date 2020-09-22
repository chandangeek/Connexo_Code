/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    // General
    OK_RESULT(0, "OkMessageFormat", "OK", Level.INFO),
    INVALID_MESSAGE_FORMAT(1, "InvalidMessageFormat", "Invalid message format. Missing attributes: {0}."),
    MESSAGE_ALREADY_EXISTS(2, "MessageAlreadyExists", "Message with the same ID or UUID is currently being processed."),
    UNEXPECTED_CONFIRMATION_MESSAGE(3, "UnexpectedConfirmationMessage", "Received confirmation message for unknown request with UUID {0}."),
    WEB_SERVICE_ENDPOINTS_NOT_PROCESSED(4, "WebServiceEndpointsNotProcessed", "Failed to send message to the following web service endpoint(s): {0}."),
    NO_WEB_SERVICE_ENDPOINTS(6, "NoWebServiceEndpoints", "No published web service endpoint is found to send the request."),
    ERROR_LOADING_PROPERTY(7, "ErrorLoadingProperty", "Error while loading property ''{0}'': ''{1}''."),
    UNEXPECTED_EXCEPTION(8, "UnexpectedException", "Exception occurred while processing request : ''{0}''."),
    MISSING_REQUIRED_TAG(9, "NoRequiredTag", "Tag ''{0}'' is required."),
    PARTIALLY_SUCCESSFUL(10, "PartialSuccessful", "Partially successful request."),
    BULK_REQUEST_WAS_FAILED(11, "BulkRequestWasFailed", "Bulk request has failed."),
    BULK_ITEM_PROCESSING_WAS_NOT_STARTED(12, "BulkItemProcessingWasNotStarted", "Bulk item processing hasn''t started due to request issue."),
    UNKNOWN_ERROR(13, "UnknownError", "Unknown error."),
    REQUEST_WAS_FAILED(14, "RequestWasFailed", "Request has failed."),
    ONE_OF_MRD_IS_INVALID(15, "OneOfMrdIsInvalid", "One of the meter reading document has invalid message format"),
    DATA_EXPORT_TASK_WAS_INTERRUPTED(16, "DataExportTaskWasInterrupted", "Data export task was interrupted while waiting for data export confirmation."),
    SEVERITY_CODE_AND_ERROR_MESSAGE(17, "SeverityCodeAndErrorMessage", "Severity code {0}: {1}."),
    NO_SEVERITY_CODE_AND_ERROR_MESSAGE(18, "NoSeverityCodeAndErrorMessage", "No severity code: {0}."),

    // Custom property set
    CAN_NOT_BE_EMPTY(1001, Keys.CAN_NOT_BE_EMPTY, "This field is required"),
    FIELD_TOO_LONG(1002, Keys.FIELD_TOO_LONG, "Field must not exceed {max} characters"),
    UNKNOWN_PROPERTY(1003, Keys.UNKNOWN_PROPERTY, "Unknown property {name}"),
    FIELD_HAS_UNEXPECTED_SIZE(1004, Keys.FIELD_HAS_UNEXPECTED_SIZE, "Field should have from {min} to {max} characters"),
    DEVICE_IDENTIFIER_MUST_BE_UNIQUE(1006, Keys.DEVICE_IDENTIFIER_MUST_BE_UNIQUE, "Device identifier must be unique."),

    // Service call
    COULD_NOT_FIND_SERVICE_CALL_TYPE(2001, "CouldNotFindServiceCallType", "Couldn''t find service call type {0} having version {1}."),
    COULD_NOT_FIND_DOMAIN_EXTENSION(2003, "CouldNotFindDEForSC", "Couldn''t find domain extension for service call."),
    INVALID_READING_REASON_CODE(2004, "InvalidReadingReasonCode", "Invalid reading reason code {0}."),
    REQUEST_CANCELLED(2005, "RequestCancelled", "Request cancelled."),
    REGISTER_SERVICE_CALL_WAS_CANCELLED(2006, "RegisterServiceCallWasCancelled", "Service call for register ''{0}'' was cancelled."),
    REGISTER_LRN_SERVICE_CALL_WAS_CANCELLED(2007, "RegisterLrnServiceCallWasCancelled", "Service call for register with LRN ''{0}'' was cancelled."),
    REQUEST_CANCELLED_MANUALLY(2008, "RequestCancelledManually", "Request cancelled manually."),
    REQUEST_CANCELLED_BY_SAP(2009, "RequestCancelledBySap", "Request cancelled by SAP."),

    // Web services
    NO_REPLY_ADDRESS(3001, "NoReplyAddress", "Reply address is required"),
    NO_END_POINT_WITH_URL(3002, "NoEndPointConfiguredWithURL", "No end point configuration is found by URL ''{0}''."),
    SYNC_MODE_NOT_SUPPORTED(3003, "SyncModeNotSupported", "Synchronous mode is not supported for multiple objects"),
    NO_PUBLISHED_END_POINT_WITH_URL(3004, "NoPublishedEndPointConfiguredWithURL", "No published end point configuration is found by URL ''{0}''."),
    NO_REQUIRED_OUTBOUND_END_POINT(3005, "NoRequiredOutboundEndPoint", "No required outbound end point configuration is found by name ''{0}''."),

    // Device
    // Period is not needed for some messages as these messages are used as part of message.
    NO_DEVICE_FOUND_BY_SAP_ID(4001, "NoDeviceFoundBySapId", "No device found with SAP device identifier ''{0}''."),
    NO_HEAD_END_INTERFACE_FOUND(4002, "NoHeadEndInterfaceFound", "No head end interface found for device with id ''{0}''."),
    LRN_NOT_FOUND_FOR_CHANNEL(4003, "LRNNotFoundForChannel", "Logical Register Number isn''t found for reading type ''{0}'' of device ''{1}'' in the export time window."),
    REGISTER_NOT_FOUND(4007, "RegisterNotFound", "Register ''{0}'' not found"),
    FAILED_DATA_SOURCE(4009, "FailedDataSources", "The following LRN are not set: {0}."),
    NO_ANY_LRN_ON_DEVICE(4010, "NoAnyLrnOnDevice", "No any LRN on device ''{0}''."),
    DEVICE_NOT_IN_OPERATIONAL_STAGE(4011, "DeviceNotInOperationalStage", "Device {0} isn''t in operational stage."),
    CHANNEL_NOT_FOUND(4012, "ChannelNotFound", "Channel ''{0}'' not found on ''{1}-min'' interval"),
    SEVERAL_CHANNELS(4013, "SeveralChannels", "There are several channels with obis code ''{0}''"),
    NO_DEVICE_TYPE_FOUND(4016, "NoDeviceTypeFound", "No device type found with name ''{0}''."),
    NO_DEFAULT_DEVICE_CONFIGURATION(4021, "NoDefaultDeviceConfiguration", "No default device configuration for device type ''{0}''."),
    SAP_DEVICE_IDENTIFIER_MUST_BE_UNIQUE(4022, "sapDeviceIdentifierMustBeUnique", "SAP device identifier must be unique."),
    DEVICE_TYPE_IS_NOT_MAPPED(4023, "DeviceTypeIsNotMapped", "There is no device type mapped to material id ''{0}''. Please check com.elster.jupiter.sap.device.types.mapping property."),
    NO_OBIS_OR_READING_TYPE_KIND(4024, "NoObisOrReadingTypeKind", "UtilitiesObjectIdentificationSystemCodeText (reading type OBIS code) or UtilitiesDivisionCategoryCode (reading type kind) must be specified"),
    NO_UTILITIES_MEASUREMENT_RECURRENCE_CODE_MAPPING(4025, "NoUtilitiesMeasurementRecurrenceCodeMapping",
            "There is no mapping of UtilitiesMeasurementRecurrenceCode ''{0}'' to a reading type period in the configuration property ''{1}''"),
    SEVERAL_DATA_SOURCES_WITH_OBIS(4026, "SeveralDataSourcesWithObis",
            "Multiple data sources with ''{0}'' (''{1}'',''{2}'') period, OBIS code ''{3}'' and SAP CAS are found. Please check the device configuration or precise the request"),
    SEVERAL_DATA_SOURCES_WITH_KIND(4027, "SeveralDataSourcesWithKind",
            "Multiple data sources with ''{0}'' (''{1}'',''{2}'') period, CIM code pattern  ''{3}''  and SAP CAS are found. Please check the device configuration or precise the request"),
    SEVERAL_DATA_SOURCES_WITH_OBIS_OR_KIND(4028, "SeveralDataSourcesWithObisAndKind",
            "Multiple data sources with ''{0}'' (''{1}'',''{2}'') period, CIM code pattern ''{3}''  or OBIS code ''{4}'', and SAP CAS are found. Please check the device configuration or precise the request"),
    NO_DATA_SOURCES_WITH_OBIS(4029, "NoDataSourcesWithObis",
            "Data sources with ''{0}'' (''{1}'',''{2}'') period, OBIS code ''{3}'' and SAP CAS aren''t found. Please check the device configuration or precise the request"),
    NO_DATA_SOURCES_WITH_KIND(4030, "NoDataSourcesWithKind",
            "Data sources with ''{0}'' (''{1}'',''{2}'') period, CIM code pattern ''{3}'' and SAP CAS aren''t found. Please check the device configuration or precise the request"),
    NO_DATA_SOURCES_WITH_OBIS_OR_KIND(4031, "NoDataSourcesWithObisAndKind",
            "Data sources with ''{0}'' (''{1}'',''{2}'') period, CIM code pattern ''{3}'' or OBIS code ''{4}'', and SAP CAS aren''t found. Please check the device configuration or precise the request"),
    NO_UTILITIES_DIVISION_CATEGORY_CODE_MAPPING(4032, "NoUtilitiesDivisionCategoryCodeMapping",
            "There is no mapping of UtilitiesDivisionCategoryCode = ''{0}'' to CIM code pattern in the configuration property ''{1}''"),
    ERROR_PROCESSING_METER_CREATE_REQUEST(4033, "ErrorProcessingMeterCreateRequest", "Error while processing meter create request: ''{0}''."),
    ERROR_PROCESSING_METER_REGISTER_CREATE_REQUEST(4034, "ErrorProcessingMeterRegisterCreateRequest",
            "Error while processing meter register create request: ''{0}''."),
    START_DATE_IS_BEFORE_SHIPMENT_DATE(4035, "StartDateIsBeforeShipmentDate", "Start date is before meter shipment date"),
    ERROR_PROCESSING_METER_LOCATION_NOTIFICATION(4036, "ErrorProcessingMeterLocationNotification",
            "Error while processing meter location notification: ''{0}''."),
    ERROR_PROCESSING_METER_POD_NOTIFICATION(4037, "ErrorProcessingMeterPodNotification",
            "Error while processing meter pod notification: ''{0}''."),
    DIFFERENT_DEVICE_TYPE(4038, "DifferentDeviceType", "Existent device has different device type than the one mapped to material id ''{0}''." +
            " Please check ''com.elster.jupiter.sap.device.types.mapping'' property."),

    // Status change request
    INVALID_CATEGORY_CODE(5001, "InvalidCategoryCode", "Invalid category code for device with id ''{0}''"),
    ERROR_CANCELLING_STATUS_CHANGE_REQUEST(5002, "ErrorCancellingStatusChangeRequest", "Error while cancelling status change request: ''{0}''."),
    ERROR_CANCELLING_STATUS_CHANGE_REQUEST_LOG(5003, "ErrorCancellingStatusChangeRequestLog",
            "''{0}'' of ''{1}'' status change requests per device are cancelled. ''{2}'' status change requests aren''t cancelled."),
    ERROR_CANCELLING_STATUS_CHANGE_REQUEST_NO_REQUESTS(5004, "ErrorCancellingStatusChangeRequestNoRequests",
            "No status change requests are found to cancel with id ''{0}'' and category code ''{1}''."),
    ERROR_CANCELLING_STATUS_CHANGE_REQUEST_ALREADY_PROCESSED(5005, "ErrorCancellingStatusChangeRequestAlreadyProcessed",
            "Status change request with id ''{0}'' and category code ''{1}'' is already in final state."),
    CHANNEL_REGISTER_IS_NOT_FOUND(5006, "ChannelRegisterIsNotFound", "The channel/register isn''t found."),
    DEVICE_IS_NOT_FOUND(5007, "DeviceIsNotFound", "The device isn''t found."),
    COM_TASK_COULD_NOT_BE_LOCATED(5008, "ComTaskCouldNotBeLocated", "A communication task to execute the device messages couldn''t be located"),
    READING_TYPE_IS_NOT_FOUND(5009, "ReadingTypeIsNotFound", "The reading type isn''t found."),

    // Meter reading request
    INVALID_METER_READING_DOCUMENT(6001, "InvalidMeterReadingDocument", "[MeterReadingDocumentId: {0}] Invalid message format."),
    UNSUPPORTED_REASON_CODE(6002, "UnsupportedReasonCode", "Unsupported reason code"),
    NO_METER_READING_DOCUMENT(6003, "NoMeterReadingDocument", "No meter reading document found with id ''{0}''."),
    METER_READING_DOCUMENT_IS_PROCESSED(6004, "MeterReadingDocumentIsProcessed", "Meter reading document is processed."),
    ERROR_CANCELLING_METER_READING_DOCUMENT(6005, "ErrorCancellingMeterReadingDocument", "Error while cancelling meter reading document: ''{0}''."),

    CHANNEL_IS_NOT_FOUND(7000, "ChannelIsNotFound", "Couldn''t find channel with LRN ''{0}''."),
    PROFILE_ID_IS_ALREADY_SET(7001, "ProfileIdIsAlreadySet", "Profile id ''{0}'' is already set for channel ''{1}''."),
    INVALID_TIME_PERIOD(7002, "InvalidTimePeriod", "Measurement task assignment time period is invalid."),
    TIME_PERIODS_INTERSECT(7003, "TimePeriodsIntersect", "Measurement task assignment time periods intersect."),
    LRN_IS_NOT_UNIQUE(7004, "LRNIsNotUnique", "LRN ''{0}'' isn''t unique within time period [{1},{2})."),
    PROPERTY_IS_NOT_SET(7005, "PropertyNotSet", "Property ''{0}'' isn''t set."),
    ENDPOINTS_NOT_FOUND(7006, "EndpointsNotFound", "Couldn''t find active endpoints for webservices: ''{0}''."),
    ENDPOINT_BY_NAME_NOT_FOUND(7007, "EndpointByNameNotFound", "Couldn''t find active endpoint with name ''{0}''."),
    ERROR_PROCESSING_MTA_REQUEST(7009, "ErrorProcessingMTARequest", "Error while processing measurement task assignment change request: ''{0}''"),
    ERROR_PROCESSING_METER_REPLACEMENT_REQUEST(7010, "ErrorProcessingMeterReplacementRequest", "Error while processing meter replacement request: ''{0}''."),

    // Smart meter events
    EVENT_CONFIRMED(8000, "EventConfirmed", "Confirmed smart meter event creation request with UUID {0}.", Level.INFO),
    EVENT_FAILED_TO_CONFIRM(8001, "EventFailedToConfirm", "Failed to confirm smart meter event creation request with UUID {0}: {1}"),
    EVENT_NO_ERROR_MESSAGE_PROVIDED(8002, "EventNoErrorMessageProvided", "No message provided."),

    // Micro checks
    AT_LEAST_ONE_LRN_WAS_SET(10001, "AtLeastOneLrnWasSet", "No LRN has been set on the device."),

    NO_REGISTERED_NOTIFICATION_ENDPOINT(10101, "NoRegisteredNotificationEndPoint", "No registered notification end point is found by id ''{0}''."),
    DEVICE_ID_ATTRIBUTE_IS_NOT_SET(10102, "DeviceIdAttributeIsNotSet", "''Device identifier'' attribute isn''t set on Device SAP info CAS."),
    NO_LRN(10103, "NoLrn", "No LRN is available on current or future data sources on the device."),
    REQUEST_SENDING_HAS_FAILED(10104, "RequestSendingHasFailed", "The request sending has failed. See web service history for details."),
    ;

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, Level.SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return WebServiceActivator.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public String getDefaultFormat(Object... args) {
        return MessageFormat.format(defaultFormat, args);
    }

    @Override
    public Level getLevel() {
        return level;
    }

    public String code() {
        return String.valueOf(number);
    }

    public String translate(Thesaurus thesaurus, Object... args) {
        return thesaurus.getSimpleFormat(this).format(args);
    }

    public static final class Keys {
        public static final String FIELD_TOO_LONG = "FieldTooLong";
        public static final String THIS_FIELD_IS_REQUIRED = "ThisFieldIsRequired";
        public static final String CAN_NOT_BE_EMPTY = "CanNotBeEmpty";
        public static final String FIELD_HAS_UNEXPECTED_SIZE = "FieldHasUnexpectedSize";
        public static final String UNKNOWN_PROPERTY = "UnknownProperty";
        public static final String DEVICE_IDENTIFIER_MUST_BE_UNIQUE = "DeviceIdentifierMustBeUnique";
    }
}
