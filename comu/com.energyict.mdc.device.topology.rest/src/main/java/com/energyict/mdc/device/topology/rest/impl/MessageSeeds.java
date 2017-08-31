package com.energyict.mdc.device.topology.rest.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    DEVICE_NOT_FOUND(1, "deviceXNotFound", "No device with name {0} found"),
    DEVICE_NOT_FOUND_IN_ANY_GRAPH(2, "NoNodeFoundForDeviceX", "No node information found for device with name {0}");

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
        return TopologyGraphApplication.COMPONENT_NAME;
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

}