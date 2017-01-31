/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import java.util.Arrays;
import java.util.Optional;

public enum QualityCodeSystem {
    NOTAPPLICABLE(TranslationKeys.SYSTEM_NOTAPPLICABLE),
    ENDDEVICE(TranslationKeys.SYSTEM_ENDDEVICE),
    MDC(TranslationKeys.SYSTEM_MDC),
    MDM(TranslationKeys.SYSTEM_MDM),
    OTHER(TranslationKeys.SYSTEM_OTHER),
    EXTERNAL(TranslationKeys.SYSTEM_EXTERNAL);

    private final TranslationKeys translationKey;

    QualityCodeSystem(TranslationKeys translationKey) {
        this.translationKey = translationKey;
    }

    public static Optional<QualityCodeSystem> get(int ordinal) {
        return Optional.ofNullable(ordinal < values().length ? values()[ordinal] : null);
    }

    public static QualityCodeSystem of(String systemName) {
        return systemName == null || systemName.isEmpty() ?
                NOTAPPLICABLE :
                Arrays.stream(values())
                        .filter(system -> system.name().equalsIgnoreCase(systemName))
                        .findAny()
                        .orElse(OTHER);
    }

    public TranslationKeys getTranslationKey() {
        return translationKey;
    }
}
