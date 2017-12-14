/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

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

    // end device events
    UNABLE_TO_GET_END_DEVICE_EVENTS(2001, "UnableToGetEndDeviceEvents", "Unable to get end device events"),
    NO_END_DEVICE_WITH_MRID(2002, "NoEndDeviceWithMRID", "No end device is found by MRID ''{0}''."),
    NO_END_DEVICE_WITH_NAME(2003, "NoEndDeviceWithName", "No end device is found by name ''{0}''."),
    END_DEVICE_IDENTIFIER_MISSING(2004, "EndDeviceIdentifierMissing", "At least one ''mRID'' or ''Name'' must be specified in the request."),
    INVALID_OR_EMPTY_TIME_PERIOD(2005, "InvalidOrEmptyTimePeriod",
            "Can''t construct a valid time period: provided start ''{0}'' is after or coincides with the end ''{1}''."),

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
}