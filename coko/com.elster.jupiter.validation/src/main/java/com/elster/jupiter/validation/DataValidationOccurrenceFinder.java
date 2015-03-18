package com.elster.jupiter.validation;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;


public interface DataValidationOccurrenceFinder {
    DataValidationOccurrenceFinder setStart(Integer start);

    DataValidationOccurrenceFinder setLimit(Integer limit);

    DataValidationOccurrenceFinder withStartDateIn(Range<Instant> interval);

    DataValidationOccurrenceFinder withEndDateIn(Range<Instant> interval);

    List<? extends DataValidationOccurrence> find();
}
