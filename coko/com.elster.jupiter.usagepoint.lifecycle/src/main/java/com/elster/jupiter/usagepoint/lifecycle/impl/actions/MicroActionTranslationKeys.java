package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.nls.TranslationKey;

public enum MicroActionTranslationKeys implements TranslationKey {
    CANCEL_ALL_SERVICE_CALLS_NAME(Keys.NAME_PREFIX + CancelAllServiceCallsAction.class.getSimpleName(), "Cancel all service calls"),
    CANCEL_ALL_SERVICE_CALLS_DESCRIPTION(Keys.DESCRIPTION_PREFIX + CancelAllServiceCallsAction.class.getSimpleName(), "Cancel all service calls on a usage point that can be canceled"),;

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
