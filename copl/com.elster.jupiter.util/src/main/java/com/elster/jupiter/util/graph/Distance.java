/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.graph;

interface Distance extends Comparable<Distance> {
    Distance plus(long distance);

    default boolean isSmallerThan(Distance other) {
        return this.compareTo(other) < 0;
    }

    default boolean isInfinite() {
        return Infinity.INFINITY.equals(this);
    }
}
