package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

/**
 * Models the {@link MessageSeed}s of the Custom Property Set bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-10 (14:28)
 */
public enum MessageSeeds implements MessageSeed, TranslationKey {

    CAN_NOT_BE_EMPTY(1, Keys.CAN_NOT_BE_EMPTY, "This field can not be empty"),
    FIELD_TOO_LONG(2, Keys.FIELD_TOO_LONG, "Field must not exceed {max} characters"),
    EDIT_HISTORICAL_VALUES_NOT_SUPPORTED(3, Keys.EDIT_HISTORICAL_VALUES_NOT_SUPPORTED, "Editing of historical values is currently not supported"),
    CAN_NOT_BE_NULL(4, Keys.CAN_NOT_BE__NULL, "This field can not be null"),
    DUPLICATE_CUSTOM_PROPERTY_SET(5, Keys.DUPLICATE_CUSTOM_PROPERTY_SET, "The id must be unique"),
    CURRENT_USER_IS_NOT_ALLOWED_TO_EDIT(7, Keys.CURRENT_USER_IS_NOT_ALLOWED_TO_EDIT, "The current user is not allowed to edit values of the custom property set");

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
        return CustomPropertySetService.COMPONENT_NAME;
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

    static final class Keys {
        public static final String CAN_NOT_BE__NULL = "CannotBeNull";
        public static final String CAN_NOT_BE_EMPTY = "CanNotBeEmpty";
        public static final String FIELD_TOO_LONG = "FieldTooLong";
        public static final String EDIT_HISTORICAL_VALUES_NOT_SUPPORTED = "edit.historical.values.not.supported";
        public static final String DUPLICATE_CUSTOM_PROPERTY_SET = "cps.duplicate";
        public static final String CURRENT_USER_IS_NOT_ALLOWED_TO_EDIT = "cps.edit.notAllowed";
    }
}