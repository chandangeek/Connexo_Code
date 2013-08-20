package com.elster.jupiter.util;

/**
 * Part of fluent API, see Checks.
 */
public class ObjectChecker<T> {

    protected final T toCheck;

    public ObjectChecker(T toCheck) {
        this.toCheck = toCheck;
    }

    public boolean equalTo(Object other) {
        return toCheck == other || (toCheck != null && toCheck.equals(other));
    }
}
