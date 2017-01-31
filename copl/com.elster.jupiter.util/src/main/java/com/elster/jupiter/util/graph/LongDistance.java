/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.graph;

class LongDistance implements Distance {
    private final long distance;

    static final LongDistance ZERO = new LongDistance(0L);

    private LongDistance(long distance) {
        this.distance = distance;
    }

    public static Distance of(long distance) {
        return new LongDistance(distance);
    }

    @Override
    public int compareTo(Distance o) {
        if (o instanceof Infinity) {
            return -1;
        }
        LongDistance other = (LongDistance) o;
        return Long.compare(distance, other.distance);
    }

    @Override
    public Distance plus(long distance) {
        return new LongDistance(this.distance + distance);
    }

    @Override
    public String toString() {
        return "LongDistance{" +
                "distance=" + distance +
                '}';
    }
}
