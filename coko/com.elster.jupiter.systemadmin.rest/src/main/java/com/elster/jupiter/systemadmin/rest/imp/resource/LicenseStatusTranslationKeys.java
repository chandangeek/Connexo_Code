/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.resource;

import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.TranslationKey;

import java.util.Arrays;

public enum LicenseStatusTranslationKeys implements TranslationKey {
    ACTIVE(License.Status.ACTIVE, "Active"),
    EXPIRED(License.Status.EXPIRED, "Expired");

    private final License.Status status;
    private final String defaultFormat;

    LicenseStatusTranslationKeys(License.Status status, String defaultFormat) {
        this.status = status;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return "license.status." + this.status.name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public static LicenseStatusTranslationKeys getTranslatedName(License.Status status) {
        return Arrays.stream(LicenseStatusTranslationKeys.values()).filter(key -> key.status == status).findFirst().orElse(ACTIVE);
    }
}
