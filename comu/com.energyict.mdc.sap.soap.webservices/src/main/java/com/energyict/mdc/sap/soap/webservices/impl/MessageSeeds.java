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
    INVALID_MESSAGE_FORMAT(1, "InvalidMessageFormat", "Invalid message format"),
    MESSAGE_ALREADY_EXISTS(2, "MessageAlreadyExists", "Message already exists"),
    UNEXPECTED_CONFIRMATION_MESSAGE(3, "UnexpectedConfirmationMessage", "Received confirmation message for unknown request with UUID {0}."),
    WEB_SERVICE_ENDPOINTS_NOT_PROCESSED(4, "WebServiceEndpointsNotProcessed", "Failed to properly send request to the following web service endpoint(s): {0}."),
    INTERVAL_INVALID(5, "wrongInterval", "Invalid interval [{0},{1})."),
    NO_WEB_SERVICE_ENDPOINTS(6, "NoWebServiceEndpoints", "No published web service endpoint is found to send the request."),

    // Custom property set
    CAN_NOT_BE_EMPTY(1001, Keys.CAN_NOT_BE_EMPTY, "This field is required"),
    FIELD_TOO_LONG(1002, Keys.FIELD_TOO_LONG, "Field must not exceed {max} characters"),
    UNKNOWN_PROPERTY(1003, Keys.UNKNOWN_PROPERTY, "Unknown property {name}"),
    FIELD_HAS_UNEXPECTED_SIZE(1004, Keys.FIELD_HAS_UNEXPECTED_SIZE, "Field should have from {min} to {max} characters"),
    CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER(1005, Keys.CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER, "The custom attribute set ''{0}'' is not editable by current user."),
    DEVICE_IDENTIFIER_MUST_BE_UNIQUE(1006, Keys.DEVICE_IDENTIFIER_MUST_BE_UNIQUE, "Device identifier must be unique."),

    // Service call
    COULD_NOT_FIND_SERVICE_CALL_TYPE(2001, "CouldNotFindServiceCallType", "Couldn''t find service call type {0} having version {1}."),
    COULD_NOT_FIND_ACTIVE_CPS(2002, "CouldNotFindActiveCPS", "Couldn''t find active custom property set {0}."),
    COULD_NOT_FIND_DOMAIN_EXTENSION(2003, "CouldNotFindDEForSC", "Couldn''t find domain extension for service call."),
    INVALID_READING_REASON_CODE(2004, "InvalidReadingReasonCode", "Invalid reading reason code {0}."),
    SERVICE_CALL_WAS_CANCELLED(2005, "ServiceCallWasCancelled", "Service call was cancelled."),
    REGISTER_SERVICE_CALL_WAS_CANCELLED(2006, "RegisterServiceCallWasCancelled", "Service call for register ''{0}'' was cancelled."),

    // Web services
    NO_REPLY_ADDRESS(3001, "NoReplyAddress", "Reply address is required"),
    NO_END_POINT_WITH_URL(3002, "NoEndPointConfiguredWithURL", "No end point configuration is found by URL ''{0}''."),
    SYNC_MODE_NOT_SUPPORTED(3003, "SyncModeNotSupported", "Synchronous mode is not supported for multiple objects"),
    NO_PUBLISHED_END_POINT_WITH_URL(3004, "NoPublishedEndPointConfiguredWithURL", "No published end point configuration is found by URL ''{0}''."),
    NO_REQUIRED_OUTBOUND_END_POINT(3005, "NoRequiredOutboundEndPoint", "No required outbound end point configuration is found by name ''{0}''."),

    // Device
    NO_DEVICE_FOUND_BY_SAP_ID(4001, "NoDeviceFoundBySapId", "No device found with SAP device identifier ''{0}''."),
    NO_HEAD_END_INTERFACE_FOUND(4002, "NoHeadEndInterfaceFound", "No head end interface found for device with id ''{0}''."),
    LRN_NOT_FOUND_FOR_CHANNEL(4003, "LRNNotFoundForChannel", "Logical Register Number isn''t found for reading type ''{0}'' of device ''{1}'' in the export time window."),
    SEVERAL_DEVICES(4005, "SeveralDevices", "There are several devices with serial id ''{0}''."),
    DEVICE_ALREADY_HAS_SAP_IDENTIFIER(4006, "DeviceAlreadyHasSAPIdentifier", "Device with serial id ''{0}'' already has SAP device identifier."),
    REGISTER_NOT_FOUND(4007, "RegisterNotFound", "Register ''{0}'' not found"),
    REGISTER_ALREADY_HAS_LRN(4008, "RegisterAlreadyHasLrn", "Register ''{0}'' already has LRN (range is ''{1}'')"),
    FAILED_DATA_SOURCE(4009, "FailedDataSources", "The following data sources are failed: {0}."),
    NO_ANY_LRN_ON_DEVICE(4010,"NoAnyLrnOnDevice", "No any LRN on device ''{0}''."),
    DEVICE_NOT_IN_OPERATIONAL_STAGE(4011, "DeviceNotInOperationalStage", "Device {0} isn''t in operational stage."),
    CHANNEL_NOT_FOUND(4012, "ChannelNotFound", "Channel ''{0}'' not found on ''{1}-min'' interval"),
    SEVERAL_CHANNELS(4013, "SeveralChannels", "There are several channels with obis code ''{0}''"),
    CHANNEL_ALREADY_HAS_LRN(4014, "ChannelAlreadyHasLrn", "Channel ''{0}'' already has LRN (range is ''{1}'')"),
    NO_SUCH_DEVICE(4015, "NoSuchDevice", "No device with id ''{0}''."),
    NO_DEVICE_TYPE_FOUND(4016, "NoDeviceTypeFound", "No device type found with name ''{0}''."),
    NO_REGISTER_TYPE_FOUND(4017, "NoRegisterTypeFound", "No register type found with obis code ''{0}''"),
    NO_LOAD_PROFILE_TYPE_FOUND(4018, "NoLoadProfileTypeFound", "No load profile type found with obis code ''{0}''"),
    NO_REGISTER_SPEC_FOUND(4019, "NoRegisterSpecFound", "No register spec found with obis code ''{0}''"),
    NO_CHANNEL_SPEC_FOUND(4020, "NoChannelSpecFound", "No channel spec found with obis code ''{0}''"),
    NO_DEFAULT_DEVICE_CONFIGURATION(4021, "NoDefaultDeviceConfiguration", "No default device configuration for device type ''{0}''."),
    SAP_DEVICE_IDENTIFIER_MUST_BE_UNIQUE(4022, "sapDeviceIdentifierMustBeUnique", "SAP device identifier must be unique."),

    // Status change request
    INVALID_CATEGORY_CODE(5001, "InvalidCategoryCode", "Invalid category code for device with id ''{0}''"),
    ERROR_CANCELLING_STATUS_CHANGE_REQUEST(5002, "ErrorCancellingStatusChangeRequest", "Error while cancelling status change request: ''{0}''."),
    ERROR_CANCELLING_STATUS_CHANGE_REQUEST_LOG(5002, "ErrorCancellingStatusChangeRequestLog", "''{0}'' of ''{1}'' status change requests are cancelled. ''{2}'' status change requests are not cancelled."),
    ERROR_CANCELLING_STATUS_CHANGE_REQUEST_NO_REQUESTS(5003, "ErrorCancellingStatusChangeRequestNoRequests", "No status change requests are found to cancel with id ''{0}'' and category code ''{1}''."),
    ERROR_CANCELLING_STATUS_CHANGE_REQUEST_ALREADY_PROCESSED(5004, "ErrorCancellingStatusChangeRequestAlreadyProcessed", "Status change request with id ''{0}'' and category code ''{1}'' is already processed."),

    // Meter reading request
    INVALID_METER_READING_DOCUMENT(6001, "InvalidMeterReadingDocument", "[MeterReadingDocumentId: {0}] Invalid meter reading document"),
    UNSUPPORTED_REASON_CODE(6002, "UnsupportedReasonCode", "[MeterReadingDocumentId: {0}] Unsupported reason code or reason code does not support bulk request"),

    CHANNEL_IS_NOT_FOUND(7000, "ChannelIsNotFound", "Couldn''t find channel with LRN ''{0}''."),
    PROFILE_ID_IS_ALREADY_SET(7001, "ProfileIdIsAlreadySet", "Profile id ''{0}'' is already set for channel ''{1}''."),
    INVALID_TIME_PERIOD(7002, "InvalidTimePeriod", "Measurement task assignment time period is invalid."),
    TIME_PERIODS_INTERSECT(7003, "TimePeriodsIntersect", "Measurement task assignment time periods intersect."),
    LRN_IS_NOT_UNIQUE(7004, "LRNIsNotUnique", "LRN ''{0}'' isn''t unique within time period [{1},{2})."),
    PROPERTY_IS_NOT_SET(7005, "PropertyNotSet", "Property ''{0}'' isn''t set."),
    ENDPOINTS_NOT_FOUND(7006, "EndpointsNotFound", "Couldn''t find active endpoints for webservices: ''{0}''."),
    ENDPOINT_BY_NAME_NOT_FOUND(7007, "EndpointByNameNotFound", "Couldn''t find active endpoint with name ''{0}''."),
    DEVICE_IS_NOT_ACTIVE(7008, "deviceIsNotActive", "Device ''{0}'' isn''t in active state."),
    ERROR_PROCESSING_MTA_REQUEST(7009, "ErrorProcessingMTARequest", "Error while processing measurement task assignment change request: ''{0}''"),

    //Micro checks
    AT_LEAST_ONE_LRN_WAS_SET(10001,"AtLeastOneLrnWasSet", "No LRN has been set on the device.");

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
        public static final String CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER = "CustomPropertySetIsNotEditableByUser";
        public static final String DEVICE_IDENTIFIER_MUST_BE_UNIQUE = "DeviceIdentifierMustBeUnique";
    }
}