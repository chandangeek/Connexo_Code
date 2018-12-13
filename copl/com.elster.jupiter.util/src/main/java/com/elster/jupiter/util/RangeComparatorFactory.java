/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Comparator;
import java.util.function.Function;

public final class RangeComparatorFactory<T extends Comparable<? super T>> {
    public static final Comparator<Range<Instant>> INSTANT_DEFAULT
            = RangeComparatorFactory.withLimits(Instant.EPOCH, Instant.ofEpochMilli(Long.MAX_VALUE)).comparing(Instant::toEpochMilli).defaultComparator();

    private T min, max;
    private Comparator<? super T> referenceComparator = (o1, o2) -> o1.compareTo(o2);

    private RangeComparatorFactory(T min, T max) {
        this.min = min;
        this.max = max;
    }

    public static <T extends Comparable<? super T>> RangeComparatorFactory<T> withLimits(T min, T max) {
        return new RangeComparatorFactory<T>(min, max);
    }

    public RangeComparatorFactory<T> withReferenceComparator(Comparator<? super T> comparator) {
        referenceComparator = comparator;
        return this;
    }

    public <U extends Comparable<? super U>> RangeComparatorFactory<T> comparing(Function<? super T, U> keyExtractor) {
        referenceComparator = Comparator.comparing(keyExtractor);
        return this;
    }

    public Comparator<Range<T>> comparatorByStart() {
        return Comparator.comparing(range -> range.hasLowerBound() ? range.lowerEndpoint() : min, referenceComparator);
    }

    public Comparator<Range<T>> defaultComparator() {
        return comparatorByStart().thenComparing(range -> range.hasUpperBound() ? range.upperEndpoint() : max, referenceComparator);
    }
}
