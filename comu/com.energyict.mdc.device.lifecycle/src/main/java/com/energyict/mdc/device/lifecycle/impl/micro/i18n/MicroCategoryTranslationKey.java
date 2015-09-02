package com.energyict.mdc.device.lifecycle.impl.micro.i18n;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;

import java.util.Arrays;
import java.util.Optional;

/**
 * Contains translation keys for the {@link com.energyict.mdc.device.lifecycle.config.MicroCategory micro categories}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-22 (11:11)
 */
public enum MicroCategoryTranslationKey implements TranslationKey {

    TRANSITION_ACTION_CHECK_CATEGORY_COMMUNICATION(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.COMMUNICATION, "Communication"),
    TRANSITION_ACTION_CHECK_CATEGORY_VALIDATION_AND_ESTIMATION(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.VALIDATION_AND_ESTIMATION, "Validation and estimation"),
    TRANSITION_ACTION_CHECK_CATEGORY_VALIDATION(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.VALIDATION, "Validation"),
    TRANSITION_ACTION_CHECK_CATEGORY_DATA_COLLECTION(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.DATA_COLLECTION, "Data collection"),
    TRANSITION_ACTION_CHECK_CATEGORY_TOPOLOGY(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.TOPOLOGY, "Topology"),
    TRANSITION_ACTION_CHECK_CATEGORY_ISSUES_AND_ALARMS(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.ISSUES_AND_ALARMS, "Issues and alarms"),
    TRANSITION_ACTION_CHECK_CATEGORY_ISSUES(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.ISSUES, "Issues"),
    TRANSITION_ACTION_CHECK_CATEGORY_INSTALLATION(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.INSTALLATION, "Installation"),
    TRANSITION_ACTION_CHECK_CATEGORY_RETENTION(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + MicroCategory.RETENTION, "Retention"),
    ;

    private final String key;
    private final String defaultFormat;

    MicroCategoryTranslationKey(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    public static Optional<TranslationKey> getCategory(MicroCategory microCategory) {
        return Arrays.stream(MicroCategoryTranslationKey.values())
                .filter(candidate -> candidate.getKey().equals(Keys.TRANSITION_ACTION_CHECK_CATEGORY_KEY + microCategory.name()))
                .map(TranslationKey.class::cast)
                .findFirst();
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

        private Keys() {
        }
    }

}