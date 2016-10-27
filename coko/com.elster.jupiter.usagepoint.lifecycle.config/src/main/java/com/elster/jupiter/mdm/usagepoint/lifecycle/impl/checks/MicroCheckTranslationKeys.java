package com.elster.jupiter.mdm.usagepoint.lifecycle.impl.checks;

import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroCheck;
import com.elster.jupiter.nls.TranslationKey;

public enum MicroCheckTranslationKeys implements TranslationKey {
    ALL_DATA_VALID_NAME(Keys.NAME_PREFIX + MicroCheck.Key.ALL_DATA_VALID.name(), "All data valid"),
    ALL_DATA_VALID_DESCRIPTION(Keys.DESCRIPTION_PREFIX + MicroCheck.Key.ALL_DATA_VALID.name(), "Check if there are no suspect readings on the outputs of a usage point."),;

    private final String key;
    private final String defaultFormat;

    MicroCheckTranslationKeys(String key, String defaultFormat) {
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
        static String NAME_PREFIX = "usage.point.micro.check.name.";
        static String DESCRIPTION_PREFIX = "usage.point.micro.check.description.";

        private Keys() {
        }
    }
}
