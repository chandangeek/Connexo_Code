package com.energyict.mdc.common.rest;

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
