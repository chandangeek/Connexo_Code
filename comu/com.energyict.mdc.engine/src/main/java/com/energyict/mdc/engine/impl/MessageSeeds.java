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
public enum MessageSeeds implements MessageSeed, com.energyict.mdc.upl.nls.MessageSeed {

    DEVICE_IS_REQUIRED_FOR_CACHE(100, Keys.DEVICE_IS_REQUIRED_FOR_CACHE, "Cannot create cache without reference to a device", Level.SEVERE),
    COMMAND_NOT_UNIQUE(101, Keys.COMMAND_NOT_UNIQUE, "There is already a {0} command in the current Root", Level.SEVERE),
    ILLEGAL_COMMAND(102, Keys.ILLEGAL_COMMAND, "The command {0} is not allowed for {1}", Level.SEVERE),
    MBEAN_OBJECT_FORMAT(103, Keys.MBEAN_OBJECT_FORMAT, "MalformedObjectNameException for ComServer {0}", Level.SEVERE),
    COMPOSITE_TYPE_CREATION(104, Keys.COMPOSITE_TYPE_CREATION, "CompositeType creation failed for class {0}", Level.SEVERE),
    UNEXPECTED_SQL_ERROR(105, Keys.UNEXPECTED_SQL_ERROR, "Unexpected SQL exception\\: {0}", Level.SEVERE),
    METHOD_ARGUMENT_CAN_NOT_BE_NULL(106, Keys.METHOD_ARGUMENT_CAN_NOT_BE_NULL, "A null value for the argument {2} of method {1} of class {0} is NOT supported", Level.SEVERE),
    VALIDATION_FAILED(107, Keys.VALIDATION_FAILED, "Validation for attribute {1} of class {0} has previously failed and is now causing the following exception", Level.SEVERE),
    UNRECOGNIZED_ENUM_VALUE(108, Keys.UNRECOGNIZED_ENUM_VALUE, "No value found for ordinal {1} of enumeration class {0}", Level.SEVERE),
    DUPLICATE_FOUND(109, Keys.DUPLICATE_FOUND, "A duplicate ''{0}'' was found when a unique result was expected for ''{1}''", Level.SEVERE),
    COMTASK_NOT_ENABLED_ON_CONFIGURATION(110, Keys.COMTASK_NOT_ENABLED_ON_CONFIGURATION, "The communication task ''{0}'' is not enabled for execution on devices of configuration ''{1}''", Level.SEVERE),
    UNKNOWN_CLOCKTASK_TYPE(111, Keys.UNKNOWN_CLOCKTASK_TYPE, "Clock action can not be performed due to an unknown type({0}) of the Clock action", Level.SEVERE),
    LOG_ON_FAILED(112, Keys.LOG_ON_FAILED, "The logical connect to a device failed: {0}", Level.SEVERE),
    DISCONNECT_FAILED(113, Keys.DISCONNECT_FAILED, "Disconnect of connection failed: {0}", Level.SEVERE),
    INVALID_INBOUND_SERVLET_PROTOCOL(114, Keys.INVALID_INBOUND_SERVLET_PROTOCOL, "Inbound protocol ''{0}'', that is linked to the inbound servlet port, should support servlet communication", Level.SEVERE),
    FAILED_TO_FETCH_DEVICE_OWNING_SECURITY_PROPERTY_SET(115, Keys.FAILED_TO_FETCH_DEVICE_OWNING_SECURITY_PROPERTY_SET, "Failed to fetch the device owning security property set {0}", Level.SEVERE),
    JSON_PARSING_ERROR(116, Keys.JSON_PARSING_ERROR, "Unexpected JSON Object parsing error\\: {0}", Level.SEVERE),
    UNEXPECTED_WEBSOCKET_ERROR(117, Keys.UNEXPECTED_WEBSOCKET_ERROR, "Unexpected websocket exception\\: {0}", Level.SEVERE),
    WEBSOCKET_CLOSED(118, Keys.UNEXPECTED_WEBSOCKET_ERROR, "Websocket closed. Reconnect, please", Level.WARNING);

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
        public static final String COMMAND_NOT_UNIQUE = "commandNotUnique";
        public static final String ILLEGAL_COMMAND = "illegalCommand";
        public static final String MBEAN_OBJECT_FORMAT = "mbeanObjectFormat";
        public static final String COMPOSITE_TYPE_CREATION = "compositeTypeCreation";
        public static final String UNEXPECTED_SQL_ERROR = "unexpectedSqlError";
        public static final String METHOD_ARGUMENT_CAN_NOT_BE_NULL = "methodArgumentCannotBeNull";
        public static final String VALIDATION_FAILED = "validationFailed";
        public static final String UNRECOGNIZED_ENUM_VALUE = "unrecognizedEnumValue";
        public static final String DUPLICATE_FOUND = "duplicateFound";
        public static final String COMTASK_NOT_ENABLED_ON_CONFIGURATION = "comTaskNotEnabled";
        public static final String UNKNOWN_CLOCKTASK_TYPE = "unknownclocktasktype";
        public static final String LOG_ON_FAILED = "logOnCommandFailed";
        public static final String DISCONNECT_FAILED = "disconnectFailed";
        public static final String INVALID_INBOUND_SERVLET_PROTOCOL = "invalidInboundServletProtocol";
        public static final String FAILED_TO_FETCH_DEVICE_OWNING_SECURITY_PROPERTY_SET = "failedToFetchDeviceForSecSet";
        public static final String JSON_PARSING_ERROR = "jsonParsingError";
        public static final String UNEXPECTED_WEBSOCKET_ERROR = "unexpectedWebSocketError";
    }
}