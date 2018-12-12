/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

/**
 * Thrown by JSon adapters when they run into a string value that can't be converted to an enum
 */
public class UnmappableEnumValueException extends RuntimeException {
    private String fieldName;

    public UnmappableEnumValueException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
