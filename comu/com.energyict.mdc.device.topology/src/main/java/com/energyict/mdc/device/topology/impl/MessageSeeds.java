package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

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
    }

}