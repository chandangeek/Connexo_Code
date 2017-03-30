/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

/**
 * Enum to represent validation status for reading
 */
public enum ValidationStatus {
    OK("validationStatus.ok"),
    SUSPECT("validationStatus.suspect"),
    NOT_VALIDATED("validationStatus.notValidated");

    private final String nameKey;

    ValidationStatus(String nameKey) {
        this.nameKey = nameKey;
    }

    public String getNameKey() {
        return nameKey;
    }

    @Override
    public String toString() {
        return getNameKey();
    }
}
