package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.util.exception.MessageSeed;

import aQute.bnd.annotation.ProviderType;

import java.util.logging.Level;

/**
 * Defines all the {@link MessageSeed}s of the PKI module.
 */
@ProviderType
public enum MessageSeeds implements MessageSeed {
    NAME_IS_REQUIRED(1000, Keys.NAME_REQUIRED, "The name is required"),
    NAME_IS_UNIQUE(1001, Keys.NAME_UNIQUE, "Name must be unique"),
    FIELD_TOO_LONG(1002, Keys.FIELD_TOO_LONG, "Field length must not exceed {max} characters"),
    FIELD_IS_REQUIRED(1003, Keys.FIELD_IS_REQUIRED, "This field is required"),
    NO_SUCH_ENCRYPTION_METHOD(1004, Keys.NO_SUCH_ENCRYPTION_METHOD, "Storage method is unknown or not registered yet"),
    DUPLICATE_KEY_ENCRYPTION_REGISTRATION(1005, Keys.DUPLICATE_KEY_ENCRYPTION_REGISTRATION, "A key storage method with the same name for this type of elements has already been registered");

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
        return PkiService.COMPONENTNAME;
    }

    public static class Keys {
        public static final String NAME_REQUIRED = "X.name.required";
        public static final String NAME_UNIQUE = "X.name.unique";
        public static final String FIELD_TOO_LONG = "fieldTooLong";
        public static final String FIELD_IS_REQUIRED = "field.required";
        public static final String NO_SUCH_ENCRYPTION_METHOD = "no.such.encryption.method";
        public static final String DUPLICATE_KEY_ENCRYPTION_REGISTRATION = "key.encryption.duplication";
    }

}