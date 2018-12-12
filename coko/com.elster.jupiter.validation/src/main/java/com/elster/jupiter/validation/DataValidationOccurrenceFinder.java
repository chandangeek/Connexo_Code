/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface DataValidationOccurrenceFinder {
    DataValidationOccurrenceFinder setStart(Integer start);

    DataValidationOccurrenceFinder setLimit(Integer limit);

    DataValidationOccurrenceFinder withStartDateIn(Range<Instant> interval);

    DataValidationOccurrenceFinder withEndDateIn(Range<Instant> interval);

    List<? extends DataValidationOccurrence> find();
}
