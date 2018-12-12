/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.resource;

import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;

public enum LicenseTypeTranslationKeys implements TranslationKey {
    EVALUATION(License.Type.EVALUATION, "Evaluation"),
    FULL(License.Type.FULL, "Full");

    private final License.Type type;
    private final String defaultFormat;

    LicenseTypeTranslationKeys(License.Type type, String defaultFormat) {
        this.type = type;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return "license.type." + this.type.name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public static LicenseTypeTranslationKeys getTranslatedName(License.Type type) {
        return Arrays.stream(LicenseTypeTranslationKeys.values()).filter(key -> key.type == type).findFirst().orElse(EVALUATION);
    }
}
