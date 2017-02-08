/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.topology.TopologyService;

import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Defines all the {@link MessageSeed}s of the device topology module.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (10:58)
 */
public enum MessageSeeds implements MessageSeed {

    VALUE_IS_REQUIRED_KEY(1, Keys.VALUE_IS_REQUIRED_KEY, "A value is required for attribute {0}"),
    FIELD_TOO_LONG(2, Keys.FIELD_TOO_LONG, "The value {0} provided for attribute {1} is too long"),
    DEVICE_IS_STILL_LINKED_AS_PHYSICAL_GATEWAY(100, Keys.DEVICE_IS_STILL_LINKED_AS_PHYSICAL_GATEWAY, "You can not remove device {0} because it is still used as a physical gateway for {1}"),
    DEVICE_IS_STILL_LINKED_AS_COMMUNICATION_GATEWAY(101, Keys.DEVICE_IS_STILL_LINKED_AS_COMMUNICATION_GATEWAY, "You can not remove device {0} because it is still used as a communication gateway for {1}"),
    DEVICE_CANNOT_BE_PHYSICAL_GATEWAY_FOR_ITSELF(102, Keys.DEVICE_CANNOT_BE_PHYSICAL_GATEWAY_FOR_ITSELF, "A device cannot be its own gateway"),
    @SuppressWarnings("unused")
    COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE(103, Keys.COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE, "You can not make comtaskexecution {0} for device {1} obsolete because it is currently executing on comserver {2}"),
    INVALID_IPV6_ADDRESS(104, Keys.INVALID_IPV6_ADDRESS, "Invalid IPv6 address"),

    DATA_LOGGER_LINK_EXCEPTION_NO_FREE_DATALOGGER_CHANNEL(1001, DataLoggerLinkException.NO_FREE_DATA_LOGGER_CHANNEL, "All channels of the data Logger {0} are used"),
    DATA_LOGGER_LINK_EXCEPTION_NO_DATA_LOGGER_CHANNEL_FOR_READING_TYPE_X(1002, DataLoggerLinkException.NO_DATA_LOGGER_CHANNEL_FOR_READING_TYPE_X, "No channel with reading type {0} found for Data logger {1}"),
    DATA_LOGGER_LINK_EXCEPTION_DEVICE_NOT_LINKED(1003, DataLoggerLinkException.DEVICE_NOT_LINKED, "Device {0} was not linked"),
    DATA_LOGGER_LINK_EXCEPTION_NO_MAPPING_FOR_ALL_SLAVE_CHANNELS(1004, DataLoggerLinkException.NO_MAPPING_FOR_ALL_SLAVE_CHANNELS, "All channels and registers of the slave should be included in the mapping"),
    DATA_LOGGER_LINK_EXCEPTION_DATALOGGER_CHANNEL_ALREADY_REFERENCED(1005, Keys.DATA_LOGGER_CHANNEL_ALREADY_REFERENCED, "The channel {0} is referenced by another slave channel"),
    DATA_LOGGER_LINK_EXCEPTION_NO_PHYSICAL_CHANNEL_FOR_READING_TYPE_X(1006, DataLoggerLinkException.NO_PHYSICAL_CHANNEL_FOR_READING_TYPE_X, "No physical channel found for reading type {0}"),
    DATA_LOGGER_LINK_INVALID_TERMINATION_DATE(1007, Keys.INVALID_TERMINATION_DATE, "You can not unlink a slave before it's linking date"),
    DATA_LOGGER_SLAVE_NOT_LINKED_AT(1008, Keys.DATA_LOGGER_SLAVE_NOT_LINKED_AT, "Slave {0} is not linked at {1}"),
    DATA_LOGGER_UNIQUE_KEY_VIOLATION(1009, Keys.DATA_LOGGER_UNIQUE_KEY_VIOLATION, "You have already linked this slave ''{0}'' to this datalogger ''{1}'' at this timestamp ''{2}''. Please select another linking date"),
    DATA_LOGGER_SLAVE_WAS_ALREADY_LINKED(1010, Keys.DATA_LOGGER_SLAVE_WAS_ALREADY_LINKED, "The slave ''{0}'' was already linked to a datalogger ''{1}'' at the given timestamp ''{2}''"),
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

    static MessageSeeds forKey(String key){
        return Stream.of(values()).filter(x -> x.getKey().equals(key)).findFirst().orElseThrow(() -> new IllegalArgumentException("Invalid key: "+key));
    }

    @Override
    public String getModule() {
        return TopologyService.COMPONENT_NAME;
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


    public static class Keys {
        public static final String VALUE_IS_REQUIRED_KEY = "X.value.required";
        public static final String FIELD_TOO_LONG = "invalidFieldLength";
        public static final String DEVICE_IS_STILL_LINKED_AS_PHYSICAL_GATEWAY = "device.delete.linked.physical.gateway";
        public static final String DEVICE_IS_STILL_LINKED_AS_COMMUNICATION_GATEWAY = "device.delete.linked.communication.gateway";
        public static final String DEVICE_CANNOT_BE_PHYSICAL_GATEWAY_FOR_ITSELF = "gateway.not.origin";
        public static final String COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE = "comTaskExecutionCannotObsoleteCurrentlyExecuting";
        public static final String INVALID_IPV6_ADDRESS = "g3.ipv6Adress.invalid";
        public static final String DEVICE_CANNOT_BE_DATA_LOGGER_FOR_ITSELF = "datalogger.not.datalogger.slave";
        public static final String NOT_A_DATALOGGER_SLAVE_DEVICE = "dataLogger.no.dataLoggerSlaveDevice";
        public static final String GATEWAY_NOT_DATALOGGER_ENABLED = "gateway.not.datalogger.enabled";
        public static final String NOT_ALL_SLAVE_CHANNELS_INCLUDED = "datalogger.not.all.slave.channels.included";
        public static final String DATA_LOGGER_CHANNEL_ALREADY_REFERENCED = "datalogger.channel.already.referenced";
        public static final String NO_PHYSICAL_CHANNEL_FOR_READING_TYPE_X = "DataLoggerLinkException.noPhysicalSlaveChannelForReadingTypeX";
        public static final String INVALID_TERMINATION_DATE = "DataLoggerLinkException.invalid.termination.date";
        public static final String DATA_LOGGER_SLAVE_NOT_LINKED_AT = "DataLoggerLinkException.slave.already.unlinked";
        public static final String DATA_LOGGER_UNIQUE_KEY_VIOLATION = "DataLoggerLinkException.unique.key.violation";
        public static final String DATA_LOGGER_SLAVE_WAS_ALREADY_LINKED = "DataLoggerLinkException.slave.was.already.linked";
    }

}