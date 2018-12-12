/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;


public enum MessageSeeds implements MessageSeed {

    CAN_NOT_BE_EMPTY(1, Keys.CAN_NOT_BE_EMPTY, "This field is required"),
    FIELD_TOO_LONG(2, Keys.FIELD_TOO_LONG, "Field must not exceed {max} characters"),
    CAN_NOT_BE_NULL(3, Keys.CAN_NOT_BE__NULL, "This field can not be null"),
    NUMBER_MIN_VALUE(4, Keys.NUMBER_MIN_VALUE, "Minimum acceptable value is {value}"),
    NUMBER_MAX_VALUE(5, Keys.NUMBER_MAX_VALUE, "Maximum acceptable value is {value}"),
    CURRENT_USER_IS_NOT_ALLOWED_TO_EDIT(6, Keys.CURRENT_USER_IS_NOT_ALLOWED_TO_EDIT, "The current user is not allowed to edit values of the custom property set"),
    NOT_NUMBER(7, Keys.NOT_NUMBER, "This field must be a number"),
    QUANTITY_MIN_VALUE(8, Keys.QUANTITY_MIN_VALUE, "Minimum acceptable value is {min}"),
    INVALID_VALUE(9, Keys.INVALID_VALUE, "Value must be between {min} and {max}"),
    INVALID_MULTIPLIER(10, Keys.INVALID_MULTIPLIER, "Multiplier must be between {min} and {max}"),
    INVALID_UNIT(11, Keys.INVALID_UNIT, "Invalid unit");

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
        return MeteringCustomPropertySetsDemoInstaller.COMPONENT_NAME;
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
        public static final String CAN_NOT_BE__NULL = "CannotBeNull";
        public static final String CAN_NOT_BE_EMPTY = "CanNotBeEmpty";
        public static final String FIELD_TOO_LONG = "FieldTooLong";
        public static final String NUMBER_MIN_VALUE = "NumberMinValue";
        public static final String QUANTITY_MIN_VALUE = "QuantityMinValue";
        public static final String NUMBER_MAX_VALUE = "NumberMaxValue";
        public static final String CURRENT_USER_IS_NOT_ALLOWED_TO_EDIT = "cps.edit.notAllowed";
        public static final String NOT_NUMBER = "NotNumber";
        public static final String INVALID_VALUE = "invalidValue";
        public static final String INVALID_MULTIPLIER = "invalidMultiplier";
        public static final String INVALID_UNIT = "invalidUnit";
    }
}