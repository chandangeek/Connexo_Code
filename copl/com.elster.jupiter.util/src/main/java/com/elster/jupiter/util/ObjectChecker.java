package com.elster.jupiter.util;

/**
 * Part of fluent API, see Checks.
 */
public class ObjectChecker<T> {

    private final T toCheck;

    public ObjectChecker(T toCheck) {
        this.toCheck = toCheck;
    }

    public boolean equalTo(Object other) {
        return getToCheck() == other || (getToCheck() != null && getToCheck().equals(other));
    }

    protected T getToCheck() {
        return toCheck;
    }
}
