/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed, TranslationKey {

    CAN_NOT_BE_EMPTY(1, Constants.NAME_REQUIRED_KEY, "This field is required", Level.SEVERE),
    FIELD_TOO_LONG(2, Constants.FIELD_TOO_LONG, "Field length must not exceed {max} characters", Level.SEVERE),
    DUPLICATE_END_DEVICE_GROUP(3, Constants.DUPLICATE_NAME, "Name must be unique", Level.SEVERE),
    SEARCH_DOMAIN_NOT_FOUND(1001, "searchDomainNotFound", "Search domain with id = ''{0}'' not found", Level.SEVERE),
    INVALID_SEARCH_CRITERIA(1002, "invalidSearchCriteria", "Invalid search criteria", Level.SEVERE),
    NO_QUERY_PROVIDER_FOUND(1003, "noQueryProviderFound", "No query provider with name ", Level.SEVERE),
    GROUP_IS_USED_BY_ANOTHER_GROUP(1004, "groupIsUsedByAnotherGroup", "The group is used by another group", Level.SEVERE),
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
        return MeteringGroupsService.COMPONENTNAME;
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

    public enum Constants {
        ;
        public static final String NAME_REQUIRED_KEY = "CanNotBeEmpty";
        public static final String FIELD_TOO_LONG = "FieldTooLong";
        public static final String DUPLICATE_NAME = "DuplicateEndDeviceGroupName";
    }
}
