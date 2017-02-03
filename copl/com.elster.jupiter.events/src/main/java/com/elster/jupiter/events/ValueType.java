/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events;

public enum ValueType {

    STRING(String.class), 
    BYTE(Byte.class), 
    SHORT(Short.class), 
    INTEGER(Integer.class), 
    LONG(Long.class), 
    FLOAT(Float.class), 
    DOUBLE(Double.class), 
    BOOLEAN(Boolean.class), 
    CHARACTER(Character.class);

    private final Class<?> type;

    ValueType(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }
}
