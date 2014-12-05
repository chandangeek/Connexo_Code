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

    DEVICE_IS_STILL_LINKED_AS_PHYSICAL_GATEWAY(1, Keys.DEVICE_IS_STILL_LINKED_AS_PHYSICAL_GATEWAY, "You can not remove device '{0}' because it is still used as a physical gateway for '{1}'", Level.SEVERE),
    DEVICE_IS_STILL_LINKED_AS_COMMUNICATION_GATEWAY(2, Keys.DEVICE_IS_STILL_LINKED_AS_COMMUNICATION_GATEWAY, "You can not remove device '{0}' because it is still used as a communication gateway for '{1}'", Level.SEVERE),
    DEVICE_CANNOT_BE_GATEWAY_FOR_ITSELF(3, Keys.DEVICE_CANNOT_BE_GATEWAY_FOR_ITSELF, "A device cannot be its own gateway", Level.SEVERE),
    COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE(4, Keys.COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE, "You can not make comtaskexecution {0} for device {1} obsolete because it is currently execution on comserver {2}", Level.SEVERE),
    ;

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
        public static final String FIELD_TOO_LONG = "invalidFieldLength";
        public static final String DEVICE_IS_STILL_LINKED_AS_PHYSICAL_GATEWAY = "device.delete.linked.physical.gateway";
        public static final String DEVICE_IS_STILL_LINKED_AS_COMMUNICATION_GATEWAY = "device.delete.linked.communication.gateway";
        public static final String DEVICE_CANNOT_BE_GATEWAY_FOR_ITSELF = "gateway.not.origin";
        public static final String COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE = "comTaskExecutionCannotObsoleteCurrentlyExecuting";
    }

}