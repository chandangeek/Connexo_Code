package com.elster.jupiter.util;

public interface Counter {

    void reset();

    void increment();

    /**
     * @param value
     * @throws java.lang.IllegalArgumentException if the implementation disallows decrementing, or the count resulting in a negative total.
     */
    void add(int value);

    int getValue();
}
