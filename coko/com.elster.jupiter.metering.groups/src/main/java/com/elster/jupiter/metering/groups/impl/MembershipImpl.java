/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.groups.Membership;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.time.Instant;

class MembershipImpl<T> implements Membership<T> {
    private final T member;
    private RangeSet<Instant> ranges;

    MembershipImpl(T member, RangeSet<Instant> ranges) {
        this.member = member;
        this.ranges = TreeRangeSet.create(ranges);
    }

    @Override
    public RangeSet<Instant> getRanges() {
        return ImmutableRangeSet.copyOf(ranges);
    }

    @Override
    public T getMember() {
        return member;
    }

    void addRange(Range<Instant> range) {
        ranges.add(range);
    }

    void removeRange(Range<Instant> range) {
        ranges.remove(range);
    }

    Range<Instant> resultingRange(Range<Instant> range) {
        return getRanges().rangeContaining(range.hasLowerBound() ? range.lowerEndpoint() : Instant.MIN);
    }

    MembershipImpl<T> withRanges(RangeSet<Instant> newRanges) {
        return new MembershipImpl<>(member, newRanges);
    }
}
