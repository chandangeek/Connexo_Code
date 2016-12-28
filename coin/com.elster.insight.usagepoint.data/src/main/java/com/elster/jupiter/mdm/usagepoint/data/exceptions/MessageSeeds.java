package com.elster.jupiter.mdm.usagepoint.data.exceptions;


import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataModelService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

import static java.util.logging.Level.SEVERE;

/**
 * Defines all the {@link MessageSeed MessageSeeds} of the usage point data module.
 */
public enum MessageSeeds implements MessageSeed {
    LAST_CHECKED_CANNOT_BE_NULL(1, Keys.LAST_CHECKED_CANNOT_BE_NULL, "The new last checked timestamp cannot be null."),
    LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED(2, Keys.LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED,
            "The new last checked {2,date,yyyy-MM-dd HH:mm:ss} cannot be after current last checked {1,date,yyyy-MM-dd HH:mm:ss}."),
    DUPLICATE_READINGTYPE_ON_METROLOGY_CONTRACT(3, Keys.DUPLICATE_READINGTYPE_ON_METROLOGY_CONTRACT,
            "Same reading type deliverable appear several times on metrology contract with id {0}."),
    METROLOGYCONTRACT_IS_NOT_LINKED_TO_USAGEPOINT(4, "MetrologyPurposeNotLinkedToUsagePoint", "Metrology contract with id {0} is not found on usage point {1}."),
    FIELD_IS_REQUIRED(5, Keys.FIELD_IS_REQUIRED, "This field is required", Level.SEVERE);

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
        return UsagePointDataModelService.COMPONENT_NAME;
    }

    public static final class Keys {
        private Keys(){}
        public static final String LAST_CHECKED_CANNOT_BE_NULL = "lastChecked.null";
        public static final String LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED = "lastChecked.after.currentLastChecked";
        public static final String DUPLICATE_READINGTYPE_ON_METROLOGY_CONTRACT = "duplicateReadingTypeOnMetrologyContract";
        public static final String FIELD_IS_REQUIRED = "thisFieldIsRequired";
    }
}
