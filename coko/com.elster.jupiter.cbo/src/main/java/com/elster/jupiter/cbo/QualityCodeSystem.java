package com.elster.jupiter.cbo;

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

    public TranslationKeys getTranslationKey() {
        return translationKey;
    }
}
