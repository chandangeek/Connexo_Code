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

    EMPTY_LIST(1, "NoElementsInList", "The list of ''{0}'' cannot be empty"),
    UNSUPPORTED_BULK_OPERATION(2, "UnsupportedBulkOperation", "Bulk operation is not supported on ''{0}'', only first element is processed", Level.WARNING),
    MISSING_ELEMENT(3, "NoRequiredElement", "Element ''{0}'' is required"),
    ELEMENT_BY_REFERENCE_NOT_FOUND(4, "ElementByRefNotFound", "Element by reference ''{0}'' is not found in ''{1}''"),

    // meter config
    UNABLE_TO_CREATE_DEVICE(1001, "UnableToCreateDevice", "Unable to create device"),
    UNABLE_TO_CHANGE_DEVICE(1002, "UnableToChangeDevice", "Unable to change device"),
    DEVICE_IDENTIFIER_MISSING(1003, "DeviceIdentifierMissing", "''Name'' or ''serialNumber'' or ''mRID'' must be specified in the payload"),
    NO_SUCH_DEVICE_TYPE(1004, "NoSuchDeviceType", "No such device type: ''{0}''"),
    NO_SUCH_DEVICE_CONFIGURATION(1005, "NoSuchDeviceConfiguration", "No such device configuration: ''{0}''"),
    NO_DEVICE_WITH_NAME(1006, "NoDeviceWithName", "No device found with name ''{0}''"),
    NO_DEVICE_WITH_MRID(1007, "NoDeviceWithMRID", "No device found with mrid ''{0}''"),
    NOT_VALID_MULTIPLIER_REASON(1008, "NotValidMultiplierReason", "''{0}'' is not a valid multiplier reason"),
    NOT_VALID_STATUS_REASON(1009, "NotValidStatusReason", "''{0}'' is not a valid status reason"),
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