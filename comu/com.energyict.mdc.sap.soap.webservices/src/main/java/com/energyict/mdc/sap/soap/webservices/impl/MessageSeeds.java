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
    INTERVAL_INVALID(5, "wrongInterval", "Invalid interval [{0},{1})"),
    NO_WEB_SERVICE_ENDPOINTS(6, "NoWebServiceEndpoints", "No published web service endpoint is found to send the request."),

    // Custom property set
    CAN_NOT_BE_EMPTY(1001, Keys.CAN_NOT_BE_EMPTY, "This field is required"),
    FIELD_TOO_LONG(1002, Keys.FIELD_TOO_LONG, "Field must not exceed {max} characters"),
    UNKNOWN_PROPERTY(1003, Keys.UNKNOWN_PROPERTY, "Unknown property {name}"),
    FIELD_HAS_UNEXPECTED_SIZE(1004, Keys.FIELD_HAS_UNEXPECTED_SIZE, "Field should have from {min} to {max} characters"),

    // Service call
    COULD_NOT_FIND_SERVICE_CALL_TYPE(2001, "CouldNotFindServiceCallType", "Couldn''t find service call type {0} having version {1}."),
    COULD_NOT_FIND_ACTIVE_CPS(2002, "CouldNotFindActiveCPS", "Couldn''t find active custom property set {0}."),
    COULD_NOT_FIND_DOMAIN_EXTENSION(2003, "CouldNotFindDEForSC", "Couldn''t find domain extension for service call."),
    INVALID_READING_REASON_CODE(2004, "InvalidReadingReasonCode", "Invalid reading reason code {0}."),
    SERVICE_CALL_WAS_CANCELLED(2005, "ServiceCallWasCancelled", "Service call was cancelled"),
    REGISTER_SERVICE_CALL_WAS_CANCELLED(2006, "RegisterServiceCallWasCancelled", "Service call for register ''{0}'' was cancelled"),

    // Web services
    NO_REPLY_ADDRESS(3001, "NoReplyAddress", "Reply address is required"),
    NO_END_POINT_WITH_URL(3002, "NoEndPointConfiguredWithURL", "No end point configuration is found by URL ''{0}''."),
    SYNC_MODE_NOT_SUPPORTED(3003, "SyncModeNotSupported", "Synchronous mode is not supported for multiple objects"),
    NO_PUBLISHED_END_POINT_WITH_URL(3004, "NoPublishedEndPointConfiguredWithURL", "No published end point configuration is found by URL ''{0}''."),
    NO_NECESSARY_OUTBOUND_END_POINT(3005, "NoNecessaryOutboundEndPoint", "No necessary outbound end point configuration is found by name ''{0}''."),

    // Device
    NO_DEVICE_FOUND_BY_SAP_ID(4001, "NoDeviceFoundBySapId", "No device found with SAP id ''{0}''"),
    NO_HEAD_END_INTERFACE_FOUND(4002, "NoHeadEndInterfaceFound", "No head end interface found for device with id ''{0}''"),
    LRN_NOT_FOUND_FOR_CHANNEL(4003, "LRNNotFoundForChannel", "Logical Register Number isn''t found for reading type ''{0}'' of device ''{1}'' in the export time window."),
    NO_DEVICE_FOUND_BY_SERIAL_ID(4004, "NoDeviceFoundBySerialId", "No device found with serial id ''{0}''"),
    SEVERAL_DEVICES(4005, "SeveralDevices", "There are several devices with serial id ''{0}''"),
    DEVICE_ALREADY_HAS_SAP_IDENTIFIER(4006, "DeviceAlreadyHasSAPIdentifier", "Device with serial id ''{0}'' already has SAP identifier"),
    REGISTER_NOT_FOUND(4007, "RegisterNotFound", "Register ''{0}'' not found"),
    REGISTER_HAS_LRN_YET(4008, "RegisterHasLrnYet", "Register ''{0}'' has LRN yet (range is ''{1}'')"),
    FAILED_DATA_SOURCE(4009, "FailedDataSources", "The following data sources are failed: ''{0}''"),
    NO_ANY_LRN_ON_DEVICE(4010,"NoAnyLrnOnDevice", "No any LRN on device ''{0}''"),
    DEVICE_NOT_IN_OPERATIONAL_STAGE(4011, "DeviceNotInOperationalStage", "Device {0} is not operational"),
    CHANNEL_NOT_FOUND(4012, "ChannelNotFound", "Channel ''{0}'' not found on ''{1}-min'' interval"),
    SEVERAL_CHANNELS(4013, "SeveralChannels", "There are several channels with obis code ''{0}''"),
    CHANNEL_HAS_LRN_YET(4014, "ChannelHasLrnYet", "Channel ''{0}'' has LRN yet (range is ''{1}'')"),

    // Status change request
    INVALID_CATEGORY_CODE(5001, "InvalidCategoryCode", "Invalid category code for device with id ''{0}''"),

    // Meter reading request
    INVALID_METER_READING_DOCUMENT(6001, "InvalidMeterReadingDocument", "[MeterReadingDocumentId: {0}] Invalid meter reading document"),
    UNSUPPORTED_REASON_CODE(6002, "UnsupportedReasonCode", "[MeterReadingDocumentId: {0}] Unsupported reason code or reason code does not support bulk request"),

    CHANNEL_IS_NOT_FOUND(7000, "ChannelIsNotFound", "Channel for LRN ''{0}'' isn''t found."),
    LRN_IS_ASSIGNED_TO_REGISTER(7001, "LRNIsAssignedToRegister", "LRN ''{0}'' is assigned to a register, not to a channel."),
    PROFILE_ID_IS_ALREADY_SET(7002, "ProfileIdIsAlreadySet", "Profile ID ''{0}'' is already set for channel ''{1}''."),
    INVALID_TIME_PERIOD(7003, "InvalidTimePeriod", "MeasurementTaskAssignmentRole time period is invalid."),
    TIME_PERIODS_ARE_INTERSECTED(7004, "TimePeriodsAreIntersected", "MeasurementTaskAssignmentRole time periods are intersected."),
    LRN_IS_NOT_UNIQUE(7005, "LRNIsNotUnique", "LRN ''{0}'' isn''t unique within time period [''{1}''..''{2}'']."),
    PROPERTY_IS_NOT_SET(7006, "PropertyNotSet", "Property ''{0}'' isn''t set."),
    LRN_AND_PROFILE_ID_NOT_FOUND_FOR_CHANNEL(7007, "LrnAndProfileIdNotFoundForChannel", "LRN and Profile ID isn''t found for reading type ''{0}'' of device ''{1}'' in the export time window."),
    UNKNOWN_ERROR(7008, "ExceptionGenerated", "Unknown error: ''{0}''."),

    //Micro checks
    AT_LEAST_ONE_LRN_WAS_SET(10001,"AtLeastOneLrnWasSet", "At least one LRN was set on the device");

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
    }
}