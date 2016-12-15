package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.nls.TranslationKey;

public enum MicroActionTranslationKeys implements TranslationKey {
    SET_CONNECTION_STATE_NAME(Keys.NAME_PREFIX + SetConnectionStateAction.class.getSimpleName(), "Set connection state"),
    SET_CONNECTION_STATE_DESCRIPTION(Keys.DESCRIPTION_PREFIX + SetConnectionStateAction.class.getSimpleName(), "Set connection state to one of the available states."),
    SET_CONNECTION_STATE_PROPERTY_NAME("set.connection.state.property.name", "Connection state"),
    SET_CONNECTION_STATE_PROPERTY_MESSAGE("set.connection.state.property.message", "Incorrect value for ''Connection state''"),;

    private final String key;
    private final String defaultFormat;

    MicroActionTranslationKeys(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static class Keys {
        static String NAME_PREFIX = "usage.point.micro.action.name.";
        static String DESCRIPTION_PREFIX = "usage.point.micro.action.description.";

        private Keys() {
        }
    }
}
