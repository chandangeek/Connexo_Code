/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Checks holds methods that start fluent API for checks on Objects.
 */
public enum Checks {
    Checks;

    public static ObjectChecker<Object> is(Object object) {
        return new ObjectChecker<>(object);
    }

    public static StringChecker is(String s) {
        return new StringChecker(s);
    }

    public static BigDecimalChecker is(BigDecimal bigDecimal) {
        return new BigDecimalChecker(bigDecimal);
    }

    public static <T> OptionalChecker<T> is(Optional<T> optional) {
        return new OptionalChecker<>(optional);
    }

}