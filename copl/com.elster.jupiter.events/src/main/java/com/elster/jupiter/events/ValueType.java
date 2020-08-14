/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.events;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public enum ValueType {
    STRING(String.class),
    BYTE(Byte.class, Byte.TYPE),
    SHORT(Short.class, Short.TYPE),
    INTEGER(Integer.class, Integer.TYPE),
    LONG(Long.class, Long.TYPE),
    FLOAT(Float.class, Float.TYPE),
    DOUBLE(Double.class, Double.TYPE),
    BOOLEAN(Boolean.class, Boolean.TYPE),
    CHARACTER(Character.class, Character.TYPE),
    LIST(List.class),
    MAP(Map.class);

    private final Class<?> type;
    private final Class<?>[] aux;

    ValueType(Class<?> type, Class<?>... aux) {
        this.type = type;
        this.aux = aux;
    }

    public Class<?> getType() {
        return type;
    }

    public static Optional<ValueType> valueOf(Class<?> type) {
        return Arrays.stream(values())
                .filter(item -> item.type == type || Arrays.stream(item.aux).anyMatch(any -> any == type))
                .findAny();
    }
}
