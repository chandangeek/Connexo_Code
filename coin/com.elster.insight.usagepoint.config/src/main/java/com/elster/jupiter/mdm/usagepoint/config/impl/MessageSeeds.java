/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

import static java.util.logging.Level.SEVERE;

public enum MessageSeeds implements MessageSeed {
    DUPLICATE_VALIDATION_RULE_USAGE(1001, Keys.DUPLICATE_VALIDATION_RULE_USAGE, "Rule set is already linked to metrology configuration"),;
    private final int number;
    private final String key;
    private final String defaultFormat;
    private final Level level;

    MessageSeeds(int number, String key, String defaultFormat) {
        this(number, key, defaultFormat, SEVERE);
    }

    MessageSeeds(int number, String key, String defaultFormat, Level level) {
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
        this.level = level;
    }

    @Override
    public String getModule() {
        return UsagePointConfigurationServiceImpl.COMPONENTNAME;
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
        public static final String DUPLICATE_VALIDATION_RULE_USAGE = "duplicateValidationRuleUsage";

    }
}