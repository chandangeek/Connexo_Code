package com.elster.insight.usagepoint.data.exceptions;


import com.elster.insight.usagepoint.data.UsagePointDataService;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

import static java.util.logging.Level.SEVERE;

/**
 * Defines all the {@link MessageSeed}s of the usage point data module.
 */
public enum MessageSeeds implements MessageSeed {
    LAST_CHECKED_CANNOT_BE_NULL(1, Keys.LAST_CHECKED_CANNOT_BE_NULL, "The new last checked timestamp cannot be null"),
    LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED(2, Keys.LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED, "The new last checked {2,date,yyyy-MM-dd HH:mm:ss} cannot be after current last checked {1,date,yyyy-MM-dd HH:mm:ss}"),
    NO_LINKED_CUSTOM_PROPERTY_SET_ON_METROLOGY_CONFIGURATION(3, Keys.NO_LINKED_CUSTOM_PROPERTY_SET_ON_METROLOGY_CONFIGURATION,
            "The custom attribute set ''{0}'' is not attached to ''{1}'' metrology configuration."),
    CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER(4, Keys.CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER,
            "The custom attribute set ''{0}'' is not editable by current user."),
    NO_LINKED_METROLOGY_CONFIGURATION(5, Keys.NO_LINKED_METROLOGY_CONFIGURATION,
            "There is no linked metrology configuration for ''{1}'' usage point."),
    ;

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
        return UsagePointDataService.COMPONENT_NAME;
    }

    public static class Keys {
        private Keys(){}
        public static final String LAST_CHECKED_CANNOT_BE_NULL = "lastChecked.null";
        public static final String LAST_CHECKED_AFTER_CURRENT_LAST_CHECKED = "lastChecked.after.currentLastChecked";
        public static final String NO_LINKED_CUSTOM_PROPERTY_SET_ON_METROLOGY_CONFIGURATION = "no.linked.custom.property.set.on.metrology.configuration";
        public static final String CUSTOM_PROPERTY_SET_IS_NOT_EDITABLE_BY_USER = "custom.property.set.is.not.editable.by.user";
        public static final String NO_LINKED_METROLOGY_CONFIGURATION = "no.linked.metrology.configuration";
    }
}