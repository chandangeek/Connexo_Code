package com.elster.jupiter.validators;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.Validator;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConsecutiveZerosValidator implements Validator {

    public ConsecutiveZerosValidator(Map<String, Quantity> props) {
        //TODO automatically generated method body, provide implementation.

    }

    @Override
    public List<String> getRequiredKeys() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getOptionalKeys() {
        return new ArrayList<>();
    }

    @Override
    public Optional<ReadingQualityType> getReadingQualityTypeCode() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void init(Channel channel, ReadingType readingType, Interval interval) {
        //TODO automatically generated method body, provide implementation.

    }

    @Override
    public ValidationResult validate(IntervalReadingRecord intervalReadingRecord) {
        //TODO automatically generated method body, provide implementation.
        return null;
    }

    @Override
    public ValidationResult validate(ReadingRecord readingRecord) {
        //TODO automatically generated method body, provide implementation.
        return null;
    }
}
