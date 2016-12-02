package com.elster.jupiter.usagepoint.lifecycle.impl.checks;

import com.elster.jupiter.nls.TranslationKey;

public enum MicroCheckTranslationKeys implements TranslationKey {
    METROLOGY_CONF_IS_DEFINED_NAME(Keys.NAME_PREFIX + MetrologyConfigurationIsDefinedCheck.class.getSimpleName(), "Metrology configuration is defined"),
    METROLOGY_CONF_IS_DEFINED_DESCRIPTION(Keys.DESCRIPTION_PREFIX + MetrologyConfigurationIsDefinedCheck.class.getSimpleName(), "Check if a metrology configuration is defined on a usage point."),
    METROLOGY_CONF_IS_DEFINED_MESSAGE("metrology.conf.is.defined.message", "Metrology configuration is not defined"),;

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
