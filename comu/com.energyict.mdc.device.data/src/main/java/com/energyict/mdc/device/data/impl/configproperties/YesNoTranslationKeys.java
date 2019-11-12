/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configproperties;

import com.elster.jupiter.nls.TranslationKey;


public enum YesNoTranslationKeys implements TranslationKey {
    VALUE_YES("communication.settings.value.yes", "Yes"),
    VALUE_NO("communication.settings.value.no", "No"),
    ;

    private String key;
    private String defaultFormat;

    YesNoTranslationKeys(String key, String defaultFormat) {
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
