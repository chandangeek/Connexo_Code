/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum MessageSeeds implements MessageSeed {
    NO_DEFAULT_REALM(1001, "domain.no.default", "No default domain found."),
    NO_REALM_FOUND(1002, "domain.not.found", "No domain found with the name {0}."),
    FIELD_CAN_NOT_BE_EMPTY(1003, Keys.FIELD_CAN_NOT_BE_EMPTY, "This field is required"),
    FIELD_SIZE_BETWEEN_1_AND_80(1004, Keys.FIELD_SIZE_BETWEEN_1_AND_80, "Field text length should be between 1 and 80 symbols"),
    ONLY_ONE_DEFAULT_KEY_PER_LOCALE_ALLOWED(1005, Keys.ONLY_ONE_DEFAULT_KEY_PER_LOCALE_ALLOWED, "Only one default key per locale is allowed"),
    DUPLICATE_GROUP_NAME(1006, Keys.DUPLICATE_GROUP_NAME, "Duplicate name"),
    NO_REALMID_FOUND(1007, "domain.id.not.found", "No domain found with the id {0}."),
    DUPLICATE_USER_DIRECTORY(1008, Keys.DUPLICATE_USER_DIRECTORY, "Duplicate name"),
    FIELD_SIZE_BETWEEN_1_AND_4000(1009, Keys.FIELD_SIZE_BETWEEN_1_AND_4000, "Field text length should be between 1 and 4000 symbols"),
    FIELD_SIZE_BETWEEN_1_AND_128(1010, Keys.FIELD_SIZE_BETWEEN_1_AND_128, "Field text length should be between 1 and 128 symbols"),
    NO_LDAP_FOUND(1011, "ldap.not.found", "Connection to LDAP failed."),
    FAIL_ACTIVATE_USER(1012, "fail.activate.user", "Fail to activate user."),
    UNDERLYING_IO_EXCEPTION(1013, Keys.UNDERLYING_IO_EXCEPTION, "Underlying IO Exception"),
    FIELD_SIZE_BETWEEN_1_AND_4(1014, Keys.FIELD_SIZE_BETWEEN_1_AND_4, "Field text length should be between 1 and 4 symbols"),
    FIELD_SIZE_BETWEEN_1_AND_3(1015, Keys.FIELD_SIZE_BETWEEN_1_AND_3, "Field text length should be between 1 and 3 symbols"),
    FIELD_SIZE_BETWEEN_1_AND_10(1016, Keys.FIELD_SIZE_BETWEEN_1_AND_10, "Field text length should be between 1 and 10 symbols"),
    FIELD_SIZE_BETWEEN_1_AND_64(1017, Keys.FIELD_SIZE_BETWEEN_1_AND_64, "Field text length should be between 1 and 64 symbols"),
    FIELD_SIZE_BETWEEN_1_AND_65(1018, Keys.FIELD_SIZE_BETWEEN_1_AND_65, "Field text length should be between 1 and 65 symbols"),
    FIELD_SIZE_BETWEEN_1_AND_256(1019, Keys.FIELD_SIZE_BETWEEN_1_AND_256, "Field text length should be between 1 and 256 symbols"),
    DUPLICATE_WORKGROUP_NAME(1020, Keys.DUPLICATE_WORKGROUP_NAME, "Duplicate name"),
    INSUFFICIENT_PRIVILEGES(1021, "InsufficientPrivileges", "The user does not have the needed privileges to perform the requested action.");

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
        public static final String FIELD_SIZE_BETWEEN_1_AND_3 = "FieldSizeBetween1and3";
        public static final String FIELD_SIZE_BETWEEN_1_AND_4 = "FieldSizeBetween1and4";
        public static final String FIELD_SIZE_BETWEEN_1_AND_10 = "FieldSizeBetween1and10";
        public static final String FIELD_SIZE_BETWEEN_1_AND_64 = "FieldSizeBetween1and64";
        public static final String FIELD_SIZE_BETWEEN_1_AND_65 = "FieldSizeBetween1and65";
        public static final String FIELD_SIZE_BETWEEN_1_AND_80 = "FieldSizeBetween1and80";
        public static final String FIELD_SIZE_BETWEEN_1_AND_128 = "FieldSizeBetween1and128";
        public static final String FIELD_SIZE_BETWEEN_1_AND_256 = "FieldSizeBetween1and256";
        public static final String FIELD_SIZE_BETWEEN_1_AND_4000 = "FieldSizeBetween1and4000";
        public static final String ONLY_ONE_DEFAULT_KEY_PER_LOCALE_ALLOWED = "OnlyOneDefaultKeyPerLocaleAllowed";
        public static final String DUPLICATE_GROUP_NAME = "DuplicateGroupName";
        public static final String DUPLICATE_WORKGROUP_NAME = "DuplicateWorkGroupName";
        public static final String DUPLICATE_USER_DIRECTORY = "Duplicate name";
        public static final String UNDERLYING_IO_EXCEPTION = "UnderlyingIO";
    }

}