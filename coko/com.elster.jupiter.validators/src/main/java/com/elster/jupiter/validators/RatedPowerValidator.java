package com.elster.jupiter.validators;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.ValidationStats;
import com.elster.jupiter.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RatedPowerValidator implements Validator {

    public RatedPowerValidator(Map<String, Quantity> props) {
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
    public String getReadingQualityTypeCode() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ValidationStats validate(Channel channel, ReadingType readingType, Interval interval) {
        //TODO automatically generated method body, provide implementation.
        return null;
    }
}
