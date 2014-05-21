package com.energyict.mdc.pluggable.rest.impl;

public class FieldValidationException extends RuntimeException {

    private final String field;

    public FieldValidationException(String message, String field) {
        super(message);
        this.field = field;
    }

    public String getFieldName() {
        return field;
    }
}
