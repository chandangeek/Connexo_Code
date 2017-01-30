package com.energyict.mdc.io.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.upl.io.SerialComponentService;

import java.util.logging.Level;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-31 (14:14)
 */
public enum MessageSeeds implements MessageSeed {

    COMMUNICATION_INTERRUPTED(20, "communicationInterrupted", "Communication was interrupted: {0}"),
    CONNECTION_TIMEOUT(21, "connectionTimeout", "Connection timeout"),
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
        return SerialComponentService.COMPONENT_NAME;
    }

}