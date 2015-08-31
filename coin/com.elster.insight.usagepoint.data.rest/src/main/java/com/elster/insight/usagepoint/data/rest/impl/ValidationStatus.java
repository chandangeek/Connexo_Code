package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.validation.ValidationResult;

public enum ValidationStatus {

    OK("validationStatus.ok", ValidationResult.VALID),
    SUSPECT("validationStatus.suspect", ValidationResult.SUSPECT),
    NOT_VALIDATED("validationStatus.notValidated", ValidationResult.NOT_VALIDATED);

    private final String nameKey;
    private final ValidationResult match;

    private ValidationStatus(String nameKey, ValidationResult match) {
        this.nameKey = nameKey;
        this.match = match;
    }

    public String getNameKey() {
        return nameKey;
    }

    @Override
    public String toString() {
        return getNameKey();
    }

    public static ValidationStatus forResult(ValidationResult result) {
        for (ValidationStatus validationStatus : values()) {
            if (validationStatus.match.equals(result)) {
                return validationStatus;
            }
        }
        return null;
    }
}