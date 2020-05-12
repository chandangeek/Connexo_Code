package com.elster.jupiter.systemproperties.impl;

import com.elster.jupiter.systemproperties.SystemPropertyService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    FIELD_TOO_LONG(1, Keys.FIELD_TOO_LONG, "Field mustn''t exceed {max} characters");

    private final int number;
    private final String key;
    private final String defaultFormat;

    MessageSeeds(int number, String key, String defaultFormat) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getModule() {
        return SystemPropertyService.COMPONENT_NAME;
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
        return Level.SEVERE;
    }

    static final class Keys {
        private Keys() {
        }

        public static final String FIELD_TOO_LONG = "FieldTooLong";
    }

}
