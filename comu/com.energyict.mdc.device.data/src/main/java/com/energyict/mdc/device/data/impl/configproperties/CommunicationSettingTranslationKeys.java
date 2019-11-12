/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configproperties;

import com.elster.jupiter.nls.TranslationKey;


public enum CommunicationSettingTranslationKeys implements TranslationKey {
    COMMUNICATION_SELECTOR("communication.settings.selector", "Communication settings"),
    TRUE_MINIMIZED("communication.settings.true.minimized", "True minimized"),
    RANDOMIZATION("communication.settings.randomization", "Randomization"),
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
