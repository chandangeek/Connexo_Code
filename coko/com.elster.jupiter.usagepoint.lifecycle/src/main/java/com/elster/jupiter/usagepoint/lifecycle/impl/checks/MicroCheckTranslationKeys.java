package com.elster.jupiter.usagepoint.lifecycle.impl.checks;

import com.elster.jupiter.nls.TranslationKey;

public enum MicroCheckTranslationKeys implements TranslationKey {
    METROLOGY_CONF_IS_DEFINED_NAME(Keys.NAME_PREFIX + MetrologyConfigurationIsDefinedCheck.class.getSimpleName(), "All data valid"),
    METROLOGY_CONF_IS_DEFINED_DESCRIPTION(Keys.DESCRIPTION_PREFIX + MetrologyConfigurationIsDefinedCheck.class.getSimpleName(), "Check if there are no suspect readings on the outputs of a usage point."),;

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
