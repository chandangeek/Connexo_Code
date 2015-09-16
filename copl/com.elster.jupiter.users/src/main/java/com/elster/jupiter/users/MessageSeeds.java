package com.elster.jupiter.users;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    NO_DEFAULT_REALM(1001, "domain.no.default", "No default domain found.", Level.SEVERE),
    NO_REALM_FOUND(1002, "domain.not.found", "No domain found with the name {0}.", Level.SEVERE),
    NO_REALMID_FOUND(1007, "domain.not.found", "No domain found with the id {0}.", Level.SEVERE),
    FIELD_CAN_NOT_BE_EMPTY(1003, Keys.FIELD_CAN_NOT_BE_EMPTY, "Field can't be empty", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_80(1004, Keys.FIELD_SIZE_BETWEEN_1_AND_80, "Field's text length should be between 1 and 80 symbols", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_128(1010, Keys.FIELD_SIZE_BETWEEN_1_AND_128, "Field's text length should be between 1 and 128 symbols", Level.SEVERE),
    FIELD_SIZE_BETWEEN_1_AND_4000(1009, Keys.FIELD_SIZE_BETWEEN_1_AND_4000, "Field's text length should be between 1 and 4000 symbols", Level.SEVERE),
    ONLY_ONE_DEFAULT_KEY_PER_LOCALE_ALLOWED(1005, Keys.ONLY_ONE_DEFAULT_KEY_PER_LOCALE_ALLOWED, "Only one default key per locale is allowed", Level.SEVERE),
    DUPLICATE_GROUP_NAME(1006, Keys.DUPLICATE_GROUP_NAME, "Duplicate name", Level.SEVERE),
    DUPLICATE_USER_DIRECTORY(1008,Keys.DUPLICATE_USER_DIRECTORY,"Duplicate name", Level.SEVERE)
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
    public String getModule() {
        return UserService.COMPONENTNAME;
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

    public void log(Logger logger, Thesaurus thesaurus, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args));
    }

    public void log(Logger logger, Thesaurus thesaurus, Throwable t, Object... args) {
        NlsMessageFormat format = thesaurus.getFormat(this);
        logger.log(getLevel(), format.format(args), t);
    }

    public enum Keys {
        ;

        public static final String FIELD_CAN_NOT_BE_EMPTY = "FieldCanNotBeEmpty";
        public static final String FIELD_SIZE_BETWEEN_1_AND_80 = "FieldSizeBetween1and80";
        public static final String FIELD_SIZE_BETWEEN_1_AND_128 = "FieldSizeBetween1and128";
        public static final String FIELD_SIZE_BETWEEN_1_AND_4000 = "FieldSizeBetween1and4000";
        public static final String ONLY_ONE_DEFAULT_KEY_PER_LOCALE_ALLOWED = "OnlyOneDefaultKeyPerLocaleAllowed";
        public static final String DUPLICATE_GROUP_NAME = "DuplicateGroupName";
        public static final String DUPLICATE_USER_DIRECTORY="Duplicate name";
    }
}
