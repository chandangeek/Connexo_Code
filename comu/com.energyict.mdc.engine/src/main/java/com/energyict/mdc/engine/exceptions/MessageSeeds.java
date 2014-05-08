package com.energyict.mdc.engine.exceptions;

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

    DEVICE_CACHE_SERIALIZATION(101, Constants.DEVICE_CACHE_NOT_SERIALIZABLE,"The device cache '{0}' could not be serialized", Level.SEVERE),
    DEVICE_CACHE_DESERIALIZATION(102, Constants.DEVICE_CACHE_NOT_DESERIALIZABLE,"The device cache '{0}' could not be deserialized", Level.SEVERE),
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

    public static class Constants {
        public static final String PHYSICAL_GATEWAY_STILL_IN_USE = "DDC.device.delete.linked.physical.gateway";
        public static final String COMMUNICATION_GATEWAY_STILL_IN_USE = "DDC.device.delete.linked.communication.gateway";
        public static final String DEVICE_IS_REQUIRED_FOR_CACHE = "DDC.device.required";
        public static final String DEVICE_CACHE_NOT_SERIALIZABLE = "DDC.device.cache.not.serializable";
        public static final String DEVICE_CACHE_NOT_DESERIALIZABLE = "DDC.device.cache.not.deserializable";

    }

}
