/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface OverlapCalculatorBuilder {
    List<ValuesRangeConflict> whenCreating(Range<Instant> newRange);

    List<ValuesRangeConflict> whenUpdating(Instant existingRangeStart, Range<Instant> newRange);
}