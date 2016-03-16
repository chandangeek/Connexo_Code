package com.elster.jupiter.metering.bpm.impl;

import com.elster.jupiter.nls.TranslationKey;

public enum TranslationKeys implements TranslationKey {
    USAGE_POINT_ASSOCIATION_PROVIDER(UsagePointProcessAssociationProvider.ASSOCIATION_TYPE, "Usage point"),
    METROLOGY_CONFIGURATION_PROPERTY("metrologyConfigurations", "Metrology configurations");

    private final String key;
    private final String defaultFormat;

    TranslationKeys(String key, String defaultFormat) {
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
}
