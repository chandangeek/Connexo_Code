/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.lifecycle.config.MicroCategory;

import java.util.Arrays;
import java.util.Optional;

public enum MicroCategoryTranslationKey implements TranslationKey {

    TRANSITION_ACTION_CHECK_CATEGORY_COMMUNICATION(Keys.NAME_PREFIX + MicroCategory.COMMUNICATION, "Communication"),
    TRANSITION_ACTION_CHECK_CATEGORY_VALIDATION_AND_ESTIMATION(Keys.NAME_PREFIX + MicroCategory.VALIDATION_AND_ESTIMATION, "Validation and estimation"),
    TRANSITION_ACTION_CHECK_CATEGORY_VALIDATION(Keys.NAME_PREFIX + MicroCategory.VALIDATION, "Validation"),
    TRANSITION_ACTION_CHECK_CATEGORY_DATA_COLLECTION(Keys.NAME_PREFIX + MicroCategory.DATA_COLLECTION, "Data collection"),
    TRANSITION_ACTION_CHECK_CATEGORY_TOPOLOGY(Keys.NAME_PREFIX + MicroCategory.TOPOLOGY, "Topology"),
    TRANSITION_ACTION_CHECK_CATEGORY_ISSUES_AND_ALARMS(Keys.NAME_PREFIX + MicroCategory.ISSUES_AND_ALARMS, "Issues and alarms"),
    TRANSITION_ACTION_CHECK_CATEGORY_ISSUES(Keys.NAME_PREFIX + MicroCategory.ISSUES, "Issues and alarms"),
    TRANSITION_ACTION_CHECK_CATEGORY_INSTALLATION(Keys.NAME_PREFIX + MicroCategory.INSTALLATION, "Installation"),
    TRANSITION_ACTION_CHECK_CATEGORY_RETENTION(Keys.NAME_PREFIX + MicroCategory.RETENTION, "Retention"),
    TRANSITION_ACTION_CHECK_CATEGORY_MONITORING(Keys.NAME_PREFIX + MicroCategory.MONITORING, "Monitoring"),
    TRANSITION_ACTION_CHECK_CATEGORY_DECOMMISSION(Keys.NAME_PREFIX + MicroCategory.DECOMMISSION, "Decommission"),
    TRANSITION_ACTION_CHECK_CATEGORY_MULTIELEMENT(Keys.NAME_PREFIX + MicroCategory.MULTIELEMENT, "Multi-element"),
    TRANSITION_ACTION_CHECK_CATEGORY_ZONES(Keys.NAME_PREFIX + MicroCategory.ZONES, "Zones");

    private final String key;
    private final String defaultFormat;

    MicroCategoryTranslationKey(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    public static Optional<TranslationKey> getCategory(MicroCategory microCategory) {
        return Arrays.stream(MicroCategoryTranslationKey.values())
                .filter(candidate -> candidate.getKey().equals(Keys.NAME_PREFIX + microCategory.name()))
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

        public static final String NAME_PREFIX = "transition.category.";

        private Keys() {
        }
    }
}
