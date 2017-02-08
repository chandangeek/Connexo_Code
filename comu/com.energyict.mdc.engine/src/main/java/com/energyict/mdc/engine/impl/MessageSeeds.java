/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.engine.EngineService;

import java.util.logging.Level;

/**
 * Defines all the {@link MessageSeed}s of the master data module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (16:49)
 */
public enum MessageSeeds implements MessageSeed {

    DEVICE_IS_REQUIRED_FOR_CACHE(100, Keys.DEVICE_IS_REQUIRED_FOR_CACHE, "Cannot create cache without reference to a device", Level.SEVERE),
    COMMAND_NOT_UNIQUE(101, "commandNotUnique", "There is already a {0} command in the current Root", Level.SEVERE),
    ILLEGAL_COMMAND(102, "illegalCommand", "The command {0} is not allowed for {1}", Level.SEVERE),
    MBEAN_OBJECT_FORMAT(103, "mbeanObjectFormat", "MalformedObjectNameException for ComServer {0}", Level.SEVERE),
    COMPOSITE_TYPE_CREATION(104, "compositeTypeCreation", "CompositeType creation failed for class {0}", Level.SEVERE),
    UNEXPECTED_SQL_ERROR(105, "unexpectedSqlError", "Unexpected SQL exception\\: {0}", Level.SEVERE),
    METHOD_ARGUMENT_CAN_NOT_BE_NULL(106, "methodArgumentCannotBeNull", "A null value for the argument {2} of method {1} of class {0} is NOT supported", Level.SEVERE),
    VALIDATION_FAILED(107, "validationFailed", "Validation for attribute {1} of class {0} has previously failed and is now causing the following exception", Level.SEVERE),
    UNRECOGNIZED_ENUM_VALUE(108, "unrecognizedEnumValue", "No value found for ordinal {1} of enumeration class {0}", Level.SEVERE),
    DUPLICATE_FOUND(109, "duplicateFound", "A duplicate ''{0}'' was found when a unique result was expected for ''{1}''", Level.SEVERE),
    COMTASK_NOT_ENABLED_ON_CONFIGURATION(110, "comTaskNotEnabled", "The communication task ''{0}'' is not enabled for execution on devices of configuration ''{1}''", Level.SEVERE),
    UNKNOWN_CLOCKTASK_TYPE(111, "unknownclocktasktype", "Clock action can not be performed due to an unknown type({0}) of the Clock action", Level.SEVERE),
    LOG_ON_FAILED(112, "logOnCommandFailed", "Could not log on the device", Level.SEVERE);

    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
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

    @Override
    public String getModule() {
        return EngineService.COMPONENTNAME;
    }

    public static class Keys {
        public static final String DEVICE_IS_REQUIRED_FOR_CACHE = "DDC.device.required";
    }

}
