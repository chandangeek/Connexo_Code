package com.elster.jupiter.validation;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.readings.ReadingQuality;

import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Predicate;

public enum ValidationResult {

    VALID, SUSPECT, NOT_VALIDATED;

    public static ValidationResult getValidationResult(Collection<? extends ReadingQuality> qualities) {
        if (qualities.isEmpty()) {
            return ValidationResult.NOT_VALIDATED;
        }
        return qualities.stream()
                .filter(isSuspect())
                .findAny()
                .map(q -> SUSPECT)
                .orElse(VALID);
    }

    private static Predicate<ReadingQuality> isSuspect() {
        return q -> EnumSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.KNOWNMISSINGREAD)
                .contains(q.getType().qualityIndex().orElse(null));
    }
}
 
