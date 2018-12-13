/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.graph;

final class Infinity implements Distance {

    static final Infinity INFINITY = new Infinity();

    private Infinity() {
    }

    @Override
    public int compareTo(Distance other) {
        if (this.equals(other)) {
            return 0;
        }
        return 1;
    }

    @Override
    public Distance plus(long distance) {
        return this;
    }

    @Override
    public String toString() {
        return "Infinity";
    }
}
