package com.energyict.mdc.device.data.rest.impl;

public enum ValidationStatus {

    OK("validationStatus.ok"),
    SUSPECT("validationStatus.suspect"),
    NOT_VALIDATED("validationStatus.notValidated");

    private String nameKey;

    private ValidationStatus(String nameKey) {
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