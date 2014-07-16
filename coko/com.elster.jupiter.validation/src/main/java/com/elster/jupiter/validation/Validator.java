package com.elster.jupiter.validation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Validator {

    List<PropertySpec> getPropertySpecs();

    Optional<ReadingQualityType> getReadingQualityTypeCode();

    void init(Channel channel, ReadingType readingType, Interval interval);

    /**
     * Marks the end of validating the interval specified through init(). Implementing validators return a Map that is either empty, or that contains mappings of Date
     * to ValidationResult, in case these could only be established at the end of the interval.
     *
     * @return
     */
    Map<Date, ValidationResult> finish();

    ValidationResult validate(IntervalReadingRecord intervalReadingRecord);

    ValidationResult validate(ReadingRecord readingRecord);

    String getDisplayName();

    String getDisplayName(String property);

    String getDefaultFormat();

}
