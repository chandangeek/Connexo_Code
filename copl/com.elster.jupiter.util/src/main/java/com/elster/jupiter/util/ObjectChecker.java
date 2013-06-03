package com.elster.jupiter.util;

/**
 */
public class ObjectChecker<T> {

    protected final T toCheck;

    public ObjectChecker(T toCheck) {
        this.toCheck = toCheck;
    }

    public boolean equalTo(Object other) {
        return this == other || (toCheck != null && toCheck.equals(other));
    }
}
