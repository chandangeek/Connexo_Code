package com.elster.jupiter.util;

/**
 * Copyrights EnergyICT
 * Date: 29/05/13
 * Time: 10:50
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
