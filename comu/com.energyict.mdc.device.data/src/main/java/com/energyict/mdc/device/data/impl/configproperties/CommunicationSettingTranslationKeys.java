/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configproperties;

import com.elster.jupiter.nls.TranslationKey;


public enum CommunicationSettingTranslationKeys implements TranslationKey {
    COMMUNICATION_SELECTOR("communication.settings.scheduling.selector", "Scheduling priority"),
    TRUE_MINIMIZED(ConfigProperties.TRUE_MINIMIZED.value(), "True minimized"),
    RANDOMIZATION(ConfigProperties.RANDOMIZATION.value(), "Randomization"),
    ;

    private String key;
    private String defaultFormat;

    CommunicationSettingTranslationKeys(String key, String defaultFormat) {
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
