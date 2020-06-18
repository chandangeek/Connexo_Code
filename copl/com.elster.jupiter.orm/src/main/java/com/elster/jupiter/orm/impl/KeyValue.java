/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import java.util.Arrays;
import java.util.Objects;

public final class KeyValue {
    public static final KeyValue NO_KEY = new KeyValue(new Object[0]);

    private final Object[] key;

    private KeyValue(Object[] values) {
        this.key = values;
    }

    public static KeyValue of(Object[] key) {
        return new KeyValue(Objects.requireNonNull(key));
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof KeyValue
                && Arrays.equals(key, ((KeyValue) other).key);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(key);
    }

    public Object[] getKey() {
        return key;
    }

    public Object get(int index) {
        return key[index];
    }

    public boolean isNullAllowZero() {
        for (Object aKey : key) {
            if (aKey == null) {
                return true;
            }
        }
        return false;
    }

    public boolean isNull() {
        return isNullAllowZero()
                || key[0] instanceof Number
                && ((Number) key[0]).longValue() == 0;
    }

    public long getId() {
        if (key[0] instanceof Number) {
            return ((Number) key[0]).longValue();
        } else {
            return 0;
        }
    }

    public int size() {
        return key.length;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public String toString() {
        return "KeyValue: " + Arrays.toString(key);
    }
}
