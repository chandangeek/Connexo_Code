/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.custompropertyset;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.text.MessageFormat;
import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {
    // General
    INTERVAL_INVALID(5, "wrongInterval", "Invalid interval [{0},{1})."),

    // Custom property set
    FIELD_TOO_LONG(1002, Keys.FIELD_TOO_LONG, "Field must not exceed {max} characters"),
    CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER(1005, Keys.CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER, "The custom attribute set ''{0}'' is not editable by current user."),
    DEVICE_IDENTIFIER_MUST_BE_UNIQUE(1006, Keys.DEVICE_IDENTIFIER_MUST_BE_UNIQUE, "Device identifier must be unique."),

    // Service call
    COULD_NOT_FIND_ACTIVE_CPS(2002, "CouldNotFindActiveCPS", "Couldn''t find active custom property set {0}."),

    // Device
    DEVICE_ALREADY_HAS_SAP_IDENTIFIER(4006, "DeviceAlreadyHasSAPIdentifier", "Device with serial id ''{0}'' already has SAP device identifier."),
    REGISTER_ALREADY_HAS_LRN(4008, "RegisterAlreadyHasLrn", "Register ''{0}'' already has LRN (range is ''{1}'')"),
    CHANNEL_ALREADY_HAS_LRN(4014, "ChannelAlreadyHasLrn", "Channel ''{0}'' already has LRN (range is ''{1}'')"),
    NO_SUCH_DEVICE(4015, "NoSuchDevice", "No device with id ''{0}''."),
    NO_DEVICE_TYPE_FOUND(4016, "NoDeviceTypeFound", "No device type found with name ''{0}''."),
    NO_REGISTER_TYPE_FOUND(4017, "NoRegisterTypeFound", "No register type found with obis code ''{0}''"),
    NO_LOAD_PROFILE_TYPE_FOUND(4018, "NoLoadProfileTypeFound", "No load profile type found with reading type ''{0}''"),
    NO_REGISTER_SPEC_FOUND(4019, "NoRegisterSpecFound", "No register spec found with reading type ''{0}''"),
    NO_CHANNEL_SPEC_FOUND(4020, "NoChannelSpecFound", "No channel spec found with obis code ''{0}''"),
    DATASOURCE_NOT_FOUND(4024, "DataSourceNotFound", "Couldn''t find data source on device ''{0}'' by LRN ''{1}'' for specified end date ''{2}''."),
    INVALID_END_DATE(4025, "InvalidEndDate", "Received end date ''{0}'' isn''t in existing custom property set range ''{1}''."),

    DEVICE_IS_NOT_ACTIVE(7008, "DeviceIsNotActive", "Device ''{0}'' isn''t in active state.");

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
        return SAPCustomPropertySetsImpl.COMPONENT_NAME;
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
        public static final String CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER = "CustomPropertySetIsNotEditableByUser";
        public static final String DEVICE_IDENTIFIER_MUST_BE_UNIQUE = "DeviceIdentifierMustBeUnique";
    }
}
