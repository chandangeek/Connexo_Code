/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    LENGTH_EXCEEDS_MAXIMUM(1,"NumberOfCharactersExceedsMaximumX", "This field should not exceed {1} characters."),
    INVALID_HEX_LENGTH(2,"ValueShouldHaveLengthX", "This field should have a length of {1} characters."),
    INVALID_HEX_CHARACTERS(3, "ValueContainsIllegalCharacters", "This field should only contain hexadecimal characters.");

    private final int number;
    private final String key;
    private final String defaultFormat;

    MessageSeeds(int number, String key, String defaultFormat){
        this.number = number;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getModule() {
        return PropertySpecServiceImpl.COMPONENT;
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
        return Level.SEVERE;
    }

}
