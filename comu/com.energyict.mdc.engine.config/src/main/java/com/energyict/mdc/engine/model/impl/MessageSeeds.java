package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.engine.model.EngineModelService;
import java.util.logging.Level;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-23 (11:00)
 */
public enum MessageSeeds implements MessageSeed {
    SHOULD_BE_AT_LEAST(1,"ValueTooSmall", "Minimal acceptable value is {min}, was {value}", Level.SEVERE),
    CAN_NOT_BE_EMPTY(2, "CanNotBeEmpty", "Required property, can not be empty", Level.SEVERE),
    VALUE_NOT_IN_RANGE(3, "ValueNotInRange", "{value} not in range {min} to {max}", Level.SEVERE),
    INVALID_URL(4, "InvalidURL", "{value} is not a valid URL", Level.SEVERE),
    INVALID_CHARS(4, "InvalidChars", "contains invalid chars, should obey {regex}", Level.SEVERE),


    DUPLICATE_COMSERVER(101, "DuplicateComServer", "ComServer with name {value} already exists", Level.SEVERE),
    OBSOLETE_COMSERVER_CANT_BE_UPDATED(103, "comserver.noUpdateAllowed", "Obsolete ComServers can no longer be updated", Level.SEVERE),
    ONLINE_COMSERVER_STILL_REFERENCED(104, "MDC.OnlineComServerXStillReferenced", "Online Comserver is still referenced by remote comserver(s)", Level.SEVERE)
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
        return EngineModelService.COMPONENT_NAME;
    }

}