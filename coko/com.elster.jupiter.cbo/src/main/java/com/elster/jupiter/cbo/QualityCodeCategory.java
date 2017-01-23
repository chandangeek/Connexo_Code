package com.elster.jupiter.cbo;

import java.util.Optional;

public enum QualityCodeCategory {
    VALID(TranslationKeys.CATEGORY_VALID),
    DIAGNOSTICS(TranslationKeys.CATEGORY_DIAGNOSTICS),
    POWERQUALITY(TranslationKeys.CATEGORY_POWERQUALITY),
    TAMPER(TranslationKeys.CATEGORY_TAMPER),
    DATACOLLECTION(TranslationKeys.CATEGORY_DATACOLLECTION),
    REASONABILITY(TranslationKeys.CATEGORY_REASONABILITY),
    VALIDATION(TranslationKeys.CATEGORY_VALIDATION) {
                @Override
                public Optional<QualityCodeIndex> qualityCodeIndex(int index) {
                    Optional superQualityCodeIndex = super.qualityCodeIndex(index);
                    return superQualityCodeIndex.isPresent() ? superQualityCodeIndex : Optional.of(QualityCodeIndex.VALIDATIONGENERIC);
                }
            },
    EDITED(TranslationKeys.CATEGORY_EDITED),
    ESTIMATED(TranslationKeys.CATEGORY_ESTIMATED){
        @Override
        public Optional<QualityCodeIndex> qualityCodeIndex(int index) {
            return Optional.of(QualityCodeIndex.ESTIMATEGENERIC);
        }
    },
    OBSOLETE_OSCILLATORY(TranslationKeys.CATEGORY_OBSOLETE_OSCILLATORY),
    QUESTIONABLE(TranslationKeys.CATEGORY_QUESTIONABLE),
    DERIVED(TranslationKeys.CATEGORY_DERIVED) {
        @Override
        public Optional<QualityCodeIndex> qualityCodeIndex(int index) {
            Optional<QualityCodeIndex> qualityCodeIndex = super.qualityCodeIndex(index);
            return qualityCodeIndex.isPresent() ? qualityCodeIndex : Optional.of(QualityCodeIndex.INFERRED);
        }
    },
    PROJECTED(TranslationKeys.CATEGORY_PROJECTED),;

    private final TranslationKeys translationKey;

    QualityCodeCategory(TranslationKeys translationKey) {
        this.translationKey = translationKey;
    }

    public TranslationKeys getTranslationKey() {
        return translationKey;
    }

    public static Optional<QualityCodeCategory> get(int ordinal) {
        return Optional.ofNullable(ordinal < values().length ? values()[ordinal] : null);
    }

    public Optional<QualityCodeIndex> qualityCodeIndex(int index) {
        return QualityCodeIndex.get(this, index);
    }
}
