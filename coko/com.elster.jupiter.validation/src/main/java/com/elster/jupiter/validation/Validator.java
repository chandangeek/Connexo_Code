package com.elster.jupiter.validation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.util.List;

public interface Validator {

    List<String> getRequiredKeys();

    List<String> getOptionalKeys();

    Optional<ReadingQualityType> getReadingQualityTypeCode();

    void init(Channel channel, ReadingType readingType, Interval interval);

    ValidationResult validate(IntervalReadingRecord intervalReadingRecord);

    ValidationResult validate(ReadingRecord readingRecord);

    String getDisplayName();

}
