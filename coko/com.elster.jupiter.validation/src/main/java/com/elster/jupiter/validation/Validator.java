package com.elster.jupiter.validation;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.HasDynamicProperties;

import aQute.bnd.annotation.ConsumerType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@ConsumerType
public interface Validator extends HasDynamicProperties {

    Optional<QualityCodeIndex> getReadingQualityCodeIndex();

    void init(Channel channel, ReadingType readingType, Range<Instant> interval);

    /**
     * Marks the end of validating the interval specified through init(). Implementing validators return a Map that is either empty, or that contains mappings of Date
     * to ValidationResult, in case these could only be established at the end of the interval.
     *
     * @return
     */
    Map<Instant, ValidationResult> finish();

    ValidationResult validate(IntervalReadingRecord intervalReadingRecord);

    ValidationResult validate(ReadingRecord readingRecord);

    String getDisplayName();

    String getDisplayName(String property);

    String getDefaultFormat();

}
