/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;

@ProviderType
public interface ValuesRangeConflict {
    ValuesRangeConflictType getType();
    String getMessage();
    Range<Instant> getConflictingRange();
    CustomPropertySetValues getValues();
}