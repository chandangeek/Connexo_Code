package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;

public enum MetrologyConfigurationStatusTranslationKeys implements TranslationKey {

    INACTIVE(MetrologyConfigurationStatus.INACTIVE, "Inactive"),
    ACTIVE(MetrologyConfigurationStatus.ACTIVE, "Active"),
    DEPRECATED(MetrologyConfigurationStatus.DEPRECATED, "Deprecated");

    private final MetrologyConfigurationStatus status;
    private final String defaultFormat;

    MetrologyConfigurationStatusTranslationKeys(MetrologyConfigurationStatus status, String defaultFormat) {
        this.status = status;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return "metrologyConfiguration.status." + this.status.getId();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public static MetrologyConfigurationStatusTranslationKeys getTranslatedName(MetrologyConfigurationStatus status) {
        return Arrays.stream(MetrologyConfigurationStatusTranslationKeys.values()).filter(key -> key.status == status).findFirst().orElse(INACTIVE);
    }
}