/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl.matchers;

/**
 * Models a closed interval of integers.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-18 (08:45)
 */
public class Range {

    private int from;
    private int to;

    public Range(int from, int to) {
        super();
        this.from = from;
        this.to = to;
    }

    public boolean includes (int number) {
        return this.from <= number && number <= this.to;
    }

    public int getFrom () {
        return from;
    }

    public int getTo () {
        return to;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Range range = (Range) o;

        if (from != range.from) {
            return false;
        }
        if (to != range.to) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode () {
        int result = from;
        result = 31 * result + to;
        return result;
    }

}