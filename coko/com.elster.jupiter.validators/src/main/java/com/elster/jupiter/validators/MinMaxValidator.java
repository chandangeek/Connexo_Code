package com.elster.jupiter.validators;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationStats;
import com.elster.jupiter.validation.Validator;
import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MinMaxValidator implements Validator {

    private static final String MIN = "minimum";
    private static final String MAX = "maximum";

    public MinMaxValidator(Map<String, Quantity> props) {
    }

    @Override
    public List<String> getRequiredKeys() {
        return ImmutableList.of(MIN, MAX);
    }

    @Override
    public List<String> getOptionalKeys() {
        return Collections.emptyList();
    }

    @Override
    public String getReadingQualityTypeCode() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ValidationStats validate(Channel channel, ReadingType readingType, Interval interval) {
        //TODO automatically generated method body, provide implementation.
        return null;
    }
}
