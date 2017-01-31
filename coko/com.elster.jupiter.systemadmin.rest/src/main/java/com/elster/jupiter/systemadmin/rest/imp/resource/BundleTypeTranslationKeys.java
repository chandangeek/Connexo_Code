/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.resource;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.system.BundleType;

import java.util.Arrays;

public enum BundleTypeTranslationKeys implements TranslationKey {
    APPLICATION_SPECIFIC(BundleType.APPLICATION_SPECIFIC, "Application-specific"),
    THIRD_PARTY(BundleType.THIRD_PARTY, "Third party");

    private final BundleType bundleType;
    private final String defaultFormat;

    BundleTypeTranslationKeys(BundleType bundleType, String defaultFormat) {
        this.bundleType = bundleType;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return "bundle.type." + this.bundleType.name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public static BundleTypeTranslationKeys getTranslatedName(BundleType bundleType) {
        return Arrays.stream(BundleTypeTranslationKeys.values()).filter(key -> key.bundleType == bundleType).findFirst().orElse(THIRD_PARTY);
    }
}
