/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.cim.impl;

import com.elster.jupiter.metering.ReadingType;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class EnumeratedReadingTypeFilter implements ReadingTypeFilter {

    private final Set<ReadingType> toRetain;

    private EnumeratedReadingTypeFilter(ReadingType... toRetain) {
        this.toRetain = ImmutableSet.copyOf(toRetain);
    }

    private EnumeratedReadingTypeFilter(Iterable<ReadingType> toRetain) {
        this.toRetain = ImmutableSet.copyOf(toRetain);
    }

    public static ReadingTypeFilter only(ReadingType... toRetain) {
        return new EnumeratedReadingTypeFilter(toRetain);
    }

    public static ReadingTypeFilter only(Iterable<ReadingType> toRetain) {
        return new EnumeratedReadingTypeFilter(toRetain);
    }

    @Override
    public boolean apply(ReadingType readingType) {
        return toRetain.contains(readingType);
    }
}
