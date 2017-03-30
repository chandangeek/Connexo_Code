/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

public interface LongCounter {

    void reset();

    void increment();

    /**
     * @param value
     * @throws IllegalArgumentException if the implementation disallows decrementing, or the count resulting in a negative total.
     */
    void add(long value);

    long getValue();
}
