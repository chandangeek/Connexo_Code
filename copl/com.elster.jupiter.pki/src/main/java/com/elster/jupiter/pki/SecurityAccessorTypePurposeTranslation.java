/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;

public enum SecurityAccessorTypePurposeTranslation implements TranslationKey {
    DEVICE_OPERATIONS(SecurityAccessorType.Purpose.DEVICE_OPERATIONS, "Device operations"),
    FILE_OPERATIONS(SecurityAccessorType.Purpose.FILE_OPERATIONS, "File operations");

    private static final String KEY_PREFIX = "securityAccessorType.purpose.";
    private SecurityAccessorType.Purpose purpose;
    private String defaultFormat;

    SecurityAccessorTypePurposeTranslation(SecurityAccessorType.Purpose purpose, String defaultFormat) {
        this.purpose = purpose;
        this.defaultFormat = defaultFormat;
    }

    public static SecurityAccessorTypePurposeTranslation fromPurpose(SecurityAccessorType.Purpose purpose) {
        return Arrays.stream(values())
                .filter(value -> purpose == value.purpose)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("There is a non-translated element of " + SecurityAccessorType.Purpose.class.getSimpleName() + "!"));
    }

    public static String translate(SecurityAccessorType.Purpose purpose, Thesaurus thesaurus) {
        return thesaurus.getFormat(fromPurpose(purpose)).format();
    }

    public SecurityAccessorType.Purpose getPurpose() {
        return purpose;
    }

    @Override
    public String getKey() {
        return KEY_PREFIX + purpose.name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }
}
