package com.energyict.mdc.device.lifecycle.config.rest.impl.i18n;

import com.elster.jupiter.nls.TranslationKey;

public enum ConflictGroupTranslationKey implements TranslationKey{
    TRANSITION_ACTION_SUB_CATEGORY_ESTIMATION("conflict_estimation", "Toggle data estimation"),
    TRANSITION_ACTION_SUB_CATEGORY_VALIDATION("conflict_validation", "Toggle data validation"),
    TRANSITION_ACTION_SUB_CATEGORY_ESTIMATION_DESCRIPTION(Keys.TRANSITION_ACTION_SUB_CATEGORY_DESCRIPTION_KEY + "conflict_estimation", "Activate or deactivate the data estimation on this device. This auto action is effective immediately."),
    TRANSITION_ACTION_SUB_CATEGORY_COMMUNICATION_DESCRIPTION(Keys.TRANSITION_ACTION_SUB_CATEGORY_DESCRIPTION_KEY + "conflict_validation", "Activate or deactivate the data validation on this device. This auto action is effective immediately."),
    ;

    private final String key;
    private final String defaultFormat;

    ConflictGroupTranslationKey(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public static class Keys {
        public static final String TRANSITION_ACTION_SUB_CATEGORY_DESCRIPTION_KEY = "transition.microaction.conflict.description.";
        private Keys() {}
    }
}
