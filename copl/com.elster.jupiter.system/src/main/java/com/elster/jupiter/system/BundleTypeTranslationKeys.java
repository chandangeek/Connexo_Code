package com.elster.jupiter.system;

import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;

public enum BundleTypeTranslationKeys implements TranslationKey {
    APPLICATION_SPECIFIC(BundleType.APPLICATION_SPECIFIC, "Application-specific"),
    THIRDPARTY(BundleType.THIRDPARTY, "Third party");

    private final BundleType bundleType;
    private final String defaultFormat;

    BundleTypeTranslationKeys(BundleType bundleType, String defaultFormat) {
        this.bundleType = bundleType;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.bundleType.name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public static BundleTypeTranslationKeys getTranslatedName(BundleType bundleType) {
        return Arrays.stream(BundleTypeTranslationKeys.values()).filter(key -> key.bundleType.equals(bundleType)).findFirst().orElse(THIRDPARTY);
    }
}
