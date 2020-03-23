/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.firmware.FirmwareStatus;

import java.util.stream.Stream;

public enum FirmwareStatusTranslationKeys implements TranslationKey {
    TEST(FirmwareStatus.TEST, "Test"),
    FINAL(FirmwareStatus.FINAL, "Final"),
    GHOST(FirmwareStatus.GHOST, "Ghost"),
    DEPRECATED(FirmwareStatus.DEPRECATED, "Deprecated");

    private FirmwareStatus status;
    private String defaultFormat;

    FirmwareStatusTranslationKeys(FirmwareStatus status, String defaultFormat) {
        this.status = status;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return this.status.getStatus();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static FirmwareStatusTranslationKeys from(FirmwareStatus status) {
        return Stream.of(values())
                .filter(each -> each.status == status)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Translation missing for firmware status: " + status));
    }

    public static String translationFor(FirmwareStatus status, Thesaurus thesaurus) {
        return thesaurus.getFormat(from(status)).format();
    }
}