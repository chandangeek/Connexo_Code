/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.validation.ValidationResult;

import org.junit.Test;

import static com.elster.jupiter.validators.impl.Utils.*;
import static org.junit.Assert.assertEquals;

public class MainCheckValidatorPartuallyValidatedCheckTest extends MainCheckValidatorTest {


    @Test
    public void identicalReadingsValidationTest() {

        validateWithIdenticalReadings(new ValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withValuedDifference(BIG_DECIMAL_100)
                .passIfNoRefData(false)
                .useValidatedData(true)
                .withNoMinThreshold());

        validateWithIdenticalReadings(new ValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withValuedDifference(BIG_DECIMAL_20)
                .passIfNoRefData(true)
                .useValidatedData(true)
                .withNoMinThreshold());
    }

    private void validateWithIdenticalReadings(ValidatorRule rule){
        ChannelReadings mainChannelReadings = new ChannelReadings(3);
        mainChannelReadings.setReadingValue(0, BIG_DECIMAL_10, INSTANT_2016_FEB_01);
        mainChannelReadings.setReadingValue(1, BIG_DECIMAL_20, INSTANT_2016_FEB_02);
        mainChannelReadings.setReadingValue(2, BIG_DECIMAL_30, INSTANT_2016_FEB_03);

        // NOTE: check channel readings are all validated!
        ValidatedChannelReadings checkReadings = new ValidatedChannelReadings(3);
        checkReadings.setReadingValue(0, BIG_DECIMAL_10, INSTANT_2016_FEB_01, ValidationResult.VALID);
        checkReadings.setReadingValue(1, BIG_DECIMAL_20, INSTANT_2016_FEB_02, ValidationResult.NOT_VALIDATED);
        checkReadings.setReadingValue(2, BIG_DECIMAL_30, INSTANT_2016_FEB_03, ValidationResult.SUSPECT);

        ValidationConfiguration validationConfiguration = new ValidationConfiguration(rule, mainChannelReadings, checkReadings);
        MainCheckValidator validator = initValidator(validationConfiguration);

        assertEquals(3, validationConfiguration.mainChannelReadings.readings.size());

        assertEquals(2L, validationConfiguration.mainChannelReadings.readings.stream()
                .map(validator::validate)
                .filter((c -> !c.equals(ValidationResult.VALID))).count());
        assertEquals(0, validator.finish().size());
    }
}
