/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import ch.iec.tc57._2011.schema.message.ErrorType;

import java.text.DecimalFormat;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    EMPTY_LIST(1, "NoElementsInList", "The list of ''{0}'' cannot be empty."),
    UNSUPPORTED_BULK_OPERATION(2, "UnsupportedBulkOperation", "Bulk operation is not supported on ''{0}'', only first element is processed.", Level.WARNING),
    MISSING_ELEMENT(3, "NoRequiredElement", "Element ''{0}'' is required."),
    ELEMENT_BY_REFERENCE_NOT_FOUND(4, "ElementByRefNotFound", "Element by reference ''{0}'' is not found in ''{1}''."),
    UNSUPPORTED_LIST_SIZE(5, "UnsupportedListSize", "The list of ''{0}'' has unsupported size. Must be of size {1}."),
    EMPTY_ELEMENT(6, "EmptyElement", "Element ''{0}'' is empty or contains only white spaces."),
    MISSING_MRID_OR_NAME_FOR_ELEMENT(7, "MissingMridOrNameForElement", "Either element ''mRID'' or ''Names'' is required under ''{0}'' for identification purpose."),
    MISSING_MRID_OR_NAME_WITH_TYPE_FOR_ELEMENT(8, "MissingMridOrNameWithTypeForElement",
            "Either element ''mRID'' or ''Names'' with ''NameType.name'' = ''{0}'' is required under ''{1}'' for identification purpose."),
    UNSUPPORTED_ELEMENT(9, "UnsupportedElement", "Element ''{0}'' under ''{1}'' is not supported."),
    UNSUPPORTED_VALUE(10, "UnsupportedValue", "Element ''{0}'' contains unsupported value ''{1}''. Must be one of: {2}."),

    // meter config
    UNABLE_TO_CREATE_DEVICE(1001, "UnableToCreateDevice", "Unable to create device"),
    DEVICE_IDENTIFIER_MISSING(1002, "DeviceIdentifierMissing", "''Name'' or ''serialNumber'' or ''mRID'' must be specified in the payload."),
    NO_SUCH_DEVICE_TYPE(1003, "NoSuchDeviceType", "No such device type: ''{0}''."),
    NO_SUCH_DEVICE_CONFIGURATION(1004, "NoSuchDeviceConfiguration", "No such device configuration: ''{0}''."),
    NO_METER_WITH_MRID(1005, "NoMeterWithMRID", "No meter is found by MRID ''{0}''."),
    NO_METER_WITH_NAME(1006, "NoMeterWithName", "No meter is found by name ''{0}''."),

    // usage point config
    UNABLE_TO_CREATE_USAGE_POINT(2001, "UnableToCreateUsagePoint", "Unable to create usage point"),
    NO_SERVICE_CATEGORY_FOUND(2002, "NoServiceCategoryFound", "No active service category is found for ''{0}''."),
    NO_SERVICE_KIND_FOUND(2003, "NoServiceKindFound", "No service kind is found for ''{0}''."),
    NO_METROLOGY_CONFIGURATION_WITH_NAME(2004, "NoMetrologyConfigWithName", "No metrology configuration suitable for usage point is found by name ''{0}''."),
    UNABLE_TO_UPDATE_USAGE_POINT(2005, "UnableToUpdateUsagePoint", "Unable to update usage point"),
    NO_USAGE_POINT_WITH_MRID(2006, "NoUsagePointWithMRID", "No usage point is found by MRID ''{0}''."),
    NO_USAGE_POINT_WITH_NAME(2007, "NoUsagePointWithName", "No usage point is found by name ''{0}''."),
    NO_USAGE_POINT_STATE_WITH_NAME(2008, "NoUsagePointStateWithName", "No usage point state ''{0}'' is found in current life cycle."),
    NO_AVAILABLE_TRANSITION_TO_STATE(2009, "NoAvailableTransitionToState", "No transition is available to state ''{0}''."),
    USAGE_POINT_IS_ALREADY_IN_STATE(2010, "UsagePointIsAlreadyInState", "Usage point is already in state ''{0}''."),
    TRANSITION_CHECK_FAILED(2011, "TransitionCheckFailed", "Transition can''t be performed due to failed transition check ''{0}'': {1}"),
    TRANSITION_ACTION_FAILED(2012, "TransitionActionFailed", "Transition can''t be performed due to failed transition action ''{0}'': {1}"),
    NO_CONNECTION_STATE_FOUND(2013, "NoConnectionStateFound", "No supported connection state is found for ''{0}''."),
    NO_STATE_FOUND_IN_LIFE_CYCLE(1024, "StateNotFound", "The life cycle: ''{0}'' doesn''t contain usage point state: ''{1}''"),
    LIFE_CYCLE_NOT_FOUND(1024, "StateNotFound", "The life cycle: ''{0}'' doesn''t exist."),
    UNABLE_TO_GET_USAGE_POINT(2014, "UnableToGetUsagePoint", "Unable to get usage point"),

    // meter readings
    UNABLE_TO_GET_READINGS(3001, "UnableToGetReadings", "Unable to get readings"),
    NO_PURPOSES_WITH_NAMES(3002, "NoPurposesWithNames", "No metrology purposes are found for names: {0}."),
    INVALID_OR_EMPTY_TIME_PERIOD(3003, "InvalidOrEmptyTimePeriod",
            "Can''t construct a valid time period: provided start ''{0}'' is after or coincides with the end ''{1}''."),

    // master data linkage
    UNABLE_TO_LINK_METER(4001, "UnableToLinkMeter", "Unable to link meter to usage point"),
    UNABLE_TO_UNLINK_METER(4002, "UnableToUnlinkMeter", "Unable to unlink meter from usage point"),
    SAME_USAGE_POINT_ALREADY_LINKED(4003, "SameUsagePointAlreadyLinked",
            "Meter ''{0}'' is already linked to usage point ''{1}'' at the given time ''{2}''."),
    METER_AND_USAGE_POINT_NOT_LINKED(4004, "MeterAndUsagePointNotLinked",
            "Meter ''{0}'' is not linked to usage point ''{1}'' at the given time ''{2}''."),
    NO_METER_ROLE_WITH_KEY(4005, "NoMeterRoleWithKey", "No meter role is found by key ''{0}''."),
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
        return CIMInboundSoapEndpointsActivator.COMPONENT_NAME;
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
}
