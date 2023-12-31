/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.i18n;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;

import java.util.logging.Level;

public enum MessageSeeds implements MessageSeed {

    EVENT_BAD_DATA_NO_DEVICE(1, "EventBadDataNoDevice", "Unable to process issue creation event because target device (id = {0}) wasn't found", Level.SEVERE),
    EVENT_BAD_DATA_NO_KORE_DEVICE(2, "EventBadDataNoEndDevice", "Unable to process issue creation event because target kore device (amrId = {0}) wasn't found", Level.SEVERE),
    EVENT_BAD_DATA_NO_TIMESTAMP(3, "EventBadDataNoTimestamp", "Unable to process issue creation event because event timestamp cannot be obtained", Level.SEVERE),
    EVENT_BAD_DATA_NO_RELATIVE_PERIOD(4, "EventBadDataNoRelativePeriod", "Unable to process issue creation event because relative period cannot be obtained", Level.SEVERE),

    INVALID_NUMBER_OF_ARGUMENTS(9, "invalid.number.of.arguments", "Invalid number of arguments {0}, expected {1} ", Level.SEVERE),
    INVALID_ARGUMENT(10, "invalid.argument", "Invalid argument {0}", Level.SEVERE),
    DEVICE_TYPE_IN_USE(11, "deviceTypeInUseByIssueCreationRule", "Device type ''{0}'' is in use by an issue creation rule", Level.SEVERE),
    RELATIVE_PERIOD_IN_USE(12, "relativePeriodInUseByIssueCreationRule", "The relative period ''{0}'' is still in use by an issue creation rule", Level.SEVERE),
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
        return IssueDataCollectionService.COMPONENT_NAME;
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
        return this.level;
    }

    public static final class Keys {
        private Keys() {
        }

        public static final String LAST_CHECKED_CANNOT_BE_NULL = "lastChecked.null";
        public static final String LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED = "lastChecked.after.currentLastChecked";
        public static final String DUPLICATE_READINGTYPE_ON_METROLOGY_CONTRACT = "duplicateReadingTypeOnMetrologyContract";
        public static final String FIELD_IS_REQUIRED = "thisFieldIsRequired";
        public static final String FIELD_TOO_LONG = "thisFieldIsTooLong";
    }
}