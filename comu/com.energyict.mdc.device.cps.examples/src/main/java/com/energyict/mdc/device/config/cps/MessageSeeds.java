package com.energyict.mdc.device.config.cps;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;


public enum MessageSeeds implements MessageSeed {
    WRONG_PREPAY_AND_METERMECHANISM_EXCEPTION(1, Keys.WRONG_PREPAY_AND_METERMECHANISM_EXCEPTION, "/'Prepay/' value of usage point or /'Meter mechanism/' of meter are not correct");

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
        return CustomPropertySetsDemoInstaller.COMPONENT_NAME;
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

    public static final class Keys {
        public static final String WRONG_PREPAY_AND_METERMECHANISM_EXCEPTION = "IncorrectValuesForCustomValidations";
    }
}