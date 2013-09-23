package com.elster.jupiter.util;

import java.math.BigDecimal;

/**
 * Checks holds methods that start fluent API for checks on Objects.
 */
public enum Checks {
    ;

    public static ObjectChecker<Object> is(Object object) {
        return new ObjectChecker<>(object);
    }

    public static StringChecker is(String s) {
        return new StringChecker(s);
    }

    public static BigDecimalChecker is(BigDecimal bigDecimal) {
        return new BigDecimalChecker(bigDecimal);
    }
}
