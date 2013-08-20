package com.elster.jupiter.util;

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

}
