/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;


public interface Counter {

    void reset();

    default void increment() {
        add(1);
    }

    default void decrement() {
        add(-1);
    }

    /**
     * @param value
     * @throws java.lang.IllegalArgumentException if the implementation disallows decrementing, or the count resulting in a negative total.
     */
    void add(int value);

    int getValue();
}
