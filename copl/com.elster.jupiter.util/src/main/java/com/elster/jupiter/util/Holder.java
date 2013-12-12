package com.elster.jupiter.util;

/**
 * Holds a reference to a variable of type T
 * @param <T>
 */
public interface Holder<T> {

    /**
     * @return the instance currently held.
     */
    T get();

}