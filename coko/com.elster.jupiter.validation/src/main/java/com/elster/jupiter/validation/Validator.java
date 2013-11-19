package com.elster.jupiter.validation;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.time.Interval;

import java.util.List;

public interface Validator {

    List<String> getrequiredKeys();

    List<String> getOptionalKeys();

    String getReadingQualityTypeCode();

    ValidationStats validate(Channel channel, ReadingType readingType, Interval interval);
}
