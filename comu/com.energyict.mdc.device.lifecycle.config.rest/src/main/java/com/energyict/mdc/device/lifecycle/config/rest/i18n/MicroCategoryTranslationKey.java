package com.energyict.mdc.device.lifecycle.config.rest.i18n;

import com.energyict.mdc.device.lifecycle.config.MicroCategory;

import com.elster.jupiter.nls.TranslationKey;

/**
 * Contains translation keys for the {@link com.energyict.mdc.device.lifecycle.config.MicroCategory micro categories}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-22 (11:11)
 */
public enum MicroCategoryTranslationKey implements TranslationKey {

    TRANSITION_ACTION_CHECK_CATEGORY_COMMUNICATION(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.COMMUNICATION, "Communication"),
    TRANSITION_ACTION_CHECK_CATEGORY_VALIDATION_AND_ESTIMATION(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.VALIDATION_AND_ESTIMATION, "Validation and estimation"),
    TRANSITION_ACTION_CHECK_CATEGORY_DATA_COLLECTION(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.DATA_COLLECTION, "Data collection"),
    TRANSITION_ACTION_CHECK_CATEGORY_TOPOLOGY(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.TOPOLOGY, "Topology"),
    TRANSITION_ACTION_CHECK_CATEGORY_ISSUES_AND_ALARMS(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.ISSUES_AND_ALARMS, "Issues and alarms"),
    TRANSITION_ACTION_CHECK_CATEGORY_VALIDATION(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.INSTALLATION, "Installation"),
    TRANSITION_ACTION_CHECK_CATEGORY_RETENTION(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.RETENTION, "Retention"),
    TRANSITION_ACTION_SUB_CATEGORY_ESTIMATION("conflict_estimation", "Toggle data estimation"),
    TRANSITION_ACTION_SUB_CATEGORY_VALIDATION("conflict_validation", "Toggle data validation"),
    TRANSITION_ACTION_SUB_CATEGORY_ESTIMATION_DESCRIPTION(Keys.TRANSITION_ACTION_SUB_CATEGORY_DESCRIPTION_KEY + "conflict_estimation", "Activate or deactivate the data estimation on this device. This auto action is effective immediately."),
    TRANSITION_ACTION_SUB_CATEGORY_COMMUNICATION_DESCRIPTION(Keys.TRANSITION_ACTION_SUB_CATEGORY_DESCRIPTION_KEY + "conflict_validation", "Activate or deactivate the data validation on this device. This auto action is effective immediately.");

    private final String key;
    private final String defaultFormat;

    MicroCategoryTranslationKey(String key, String defaultFormat) {
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
        public static final String TRANSITION_ACTION_CHECK_CATEGORY_KEY = "transition.category.";
        public static final String TRANSITION_ACTION_SUB_CATEGORY_DESCRIPTION_KEY = "transition.microaction.conflict.description.";
        private Keys() {}
    }

}