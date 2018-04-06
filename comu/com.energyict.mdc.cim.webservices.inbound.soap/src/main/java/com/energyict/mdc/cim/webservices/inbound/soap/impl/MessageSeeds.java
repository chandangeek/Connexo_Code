/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import ch.iec.tc57._2011.schema.message.ErrorType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.DecimalFormat;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    EMPTY_LIST(1, "NoElementsInList", "The list of ''{0}'' cannot be empty"),
    UNSUPPORTED_BULK_OPERATION(2, "UnsupportedBulkOperation", "Bulk operation is not supported on ''{0}'', only first element is processed", Level.WARNING),
    MISSING_ELEMENT(3, "NoRequiredElement", "Element ''{0}'' is required"),
    ELEMENT_BY_REFERENCE_NOT_FOUND(4, "ElementByRefNotFound", "Element by reference ''{0}'' is not found in ''{1}''"),
    UNSUPPORTED_LIST_SIZE(5, "UnsupportedListSize", "The list of ''{0}'' has unsupported size. Must be of size {1}"),
    EMPTY_ELEMENT(6, "EmptyElement", "Element ''{0}'' is empty or contains only white spaces"),
    MISSING_MRID_OR_NAME_FOR_ELEMENT(7, "MissingMridOrNameForElement", "Either element ''mRID'' or ''Names'' is required under ''{0}'' for identification purpose"),
    MISSING_MRID_OR_NAME_WITH_TYPE_FOR_ELEMENT(8, "MissingMridOrNameWithTypeForElement",
            "Either element ''mRID'' or ''Names'' with ''NameType.name'' = ''{0}'' is required under ''{1}'' for identification purpose"),
    UNSUPPORTED_ELEMENT(9, "UnsupportedElement", "Element ''{0}'' under ''{1}'' is not supported"),
    UNSUPPORTED_VALUE(10, "UnsupportedValue", "Element ''{0}'' contains unsupported value ''{1}''. Must be one of: {2}"),

    // meter config
    UNABLE_TO_CREATE_DEVICE(1001, "UnableToCreateDevice", "Unable to create device"),
    UNABLE_TO_CHANGE_DEVICE(1002, "UnableToChangeDevice", "Unable to change device"),
    CREATE_DEVICE_IDENTIFIER_MISSING(1003, "CreateDeviceIdentifierMissing", "''Name'' or ''serialNumber'' or ''mRID'' must be specified in the payload"),
    CHANGE_DEVICE_IDENTIFIER_MISSING(1004, "ChangeDeviceIdentifierMissing", "''Name'' must be specified in the payload"),
    NO_SUCH_DEVICE_TYPE(1005, "NoSuchDeviceType", "No such device type: ''{0}''"),
    NO_SUCH_DEVICE_CONFIGURATION(1006, "NoSuchDeviceConfiguration", "No such device configuration: ''{0}''"),
    NO_DEVICE_WITH_NAME(1007, "NoDeviceWithName", "No device found with name ''{0}''"),
    NO_DEVICE_WITH_MRID(1008, "NoDeviceWithMRID", "No device found with mrid ''{0}''"),
    NOT_VALID_MULTIPLIER_REASON(1009, "NotValidMultiplierReason", "''{0}'' is not a valid multiplier reason"),
    NOT_VALID_STATUS_REASON(1010, "NotValidStatusReason", "''{0}'' is not a valid status reason"),
    UNABLE_TO_CHANGE_DEVICE_STATE(1011, "UnableToChangeDeviceState", "Cannot update the device with ''{0}'' payload state"),

    // get end device events
    UNABLE_TO_GET_END_DEVICE_EVENTS(2001, "UnableToGetEndDeviceEvents", "Unable to get end device events"),
    END_DEVICE_IDENTIFIER_MISSING(2002, "EndDeviceIdentifierMissing", "At least one of ''mRID'' or ''Name'' must be specified in the request."),
    INVALID_OR_EMPTY_TIME_PERIOD(2003, "InvalidOrEmptyTimePeriod",
            "Can''t construct a valid time period: provided start ''{0}'' is after or coincides with the end ''{1}''."),

    // created/closed end device events
    INVALID_CREATED_END_DEVICE_EVENTS(3001, "InvalidCreatedEndDeviceEvents", "Invalid CreatedEndDeviceEvents is received"),
    INVALID_CLOSED_END_DEVICE_EVENTS(3002, "InvalidClosedEndDeviceEvents", "Invalid ClosedEndDeviceEvents is received"),
    INVALID_SEVERITY(3003, "InvalidSeverity", "EndDeviceEvent.Severity should be equal to 'alarm'"),
    NO_LOGBOOK_WITH_OBIS_CODE_AND_DEVICE(3004, "NoLogBookWithObisCodeAndDevice", "No LogBook is found by obis code ''{0}'' and meter's ID ''{1}''."),
    NO_END_DEVICE_EVENT_TYPE_WITH_REF(3005, "NoEndDeviceEventTypeWithRef", "No end device event type is found with ref ''{0}''"),
    NO_OBIS_CODE_CONFIGURED(3006, "NoObisCodeConfigured", "Obis code is not configured for the web service end point"),
    NO_END_POINT_WITH_WEBSERVICE_NAME(3007, "NoEndPointConfigured", "No end point configuration is found by web service name ''{0}''."),

    // async
    COULD_NOT_FIND_SERVICE_CALL_TYPE(4001, "CouldNotFindServiceCallType", "Could not find service call type {0} having version {1}"),
    NO_REPLY_ADDRESS(4002, "NoReplyAddress", "Reply address is required"),
    NO_END_POINT_WITH_URL(4003, "NoEndPointConfiguredWithURL", "No end point configuration is found by URL ''{0}''."),
    SYNC_MODE_NOT_SUPPORTED(4004, "SyncModeNotSupported", "Synchronous mode is not supported for multiple objects"),
    NO_PUBLISHED_END_POINT_WITH_URL(4005, "NoPublishedEndPointConfiguredWithURL", "No published end point configuration is found by URL ''{0}''."),

    NAME_MUST_BE_UNIQUE(5001, "NameMustBeUnique", "Name and serial number must be unique."),
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
        return InboundSoapEndpointsActivator.COMPONENT_NAME;
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

    @Override
    public Level getLevel() {
        return level;
    }

    public String translate(Thesaurus thesaurus, Object... args) {
        return thesaurus.getSimpleFormat(this).format(args);
    }

    public String getErrorCode() {
        return getModule() + new DecimalFormat("0000").format(number);
    }

    public ErrorType.Level getErrorTypeLevel() {
        if (Level.SEVERE.equals(getLevel())) {
            return ErrorType.Level.FATAL;
        } else if (Level.WARNING.equals(getLevel())) {
            return ErrorType.Level.WARNING;
        } else {
            return ErrorType.Level.INFORM;
        }
    }

    public static final class Keys {
        public static final String FIELD_TOO_LONG = "FieldTooLong";
        public static final String THIS_FIELD_IS_REQUIRED = "ThisFieldIsRequired";
    }
}