/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.validation.ValidationResult;

import org.junit.Test;

import static com.elster.jupiter.validators.impl.Utils.*;
import static org.junit.Assert.assertEquals;

public class MainCheckValidatorPercentDiffTest extends MainCheckValidatorTest {
    @Test
    public void identicalReadingsValidationTest() {

        validateWithReadings(new ValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withPercentDifference(50D)
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold());
    }

    private void validateWithReadings(ValidatorRule rule){
        ChannelReadings mainChannelReadings = new ChannelReadings(3);
        mainChannelReadings.setReadingValue(0, BIG_DECIMAL_10, INSTANT_2016_FEB_01);
        mainChannelReadings.setReadingValue(1, BIG_DECIMAL_20, INSTANT_2016_FEB_02);
        mainChannelReadings.setReadingValue(2, BIG_DECIMAL_30, INSTANT_2016_FEB_03);

        // NOTE: check channel readings are not validated!
        ValidatedChannelReadings checkReadings = new ValidatedChannelReadings(3);
        checkReadings.setReadingValue(0, BIG_DECIMAL_10, INSTANT_2016_FEB_01);
        checkReadings.setReadingValue(1, BIG_DECIMAL_50, INSTANT_2016_FEB_02);
        checkReadings.setReadingValue(2, BIG_DECIMAL_130, INSTANT_2016_FEB_03);

        ValidationConfiguration validationConfiguration = new ValidationConfiguration(rule, mainChannelReadings, checkReadings);
        MainCheckValidator validator = initValidator(validationConfiguration);

        assertEquals(3, validationConfiguration.mainChannelReadings.readings.size());

        assertEquals(2L, validationConfiguration.mainChannelReadings.readings.stream()
                .map(validator::validate)
                .filter((c -> !c.equals(ValidationResult.VALID))).count());
        assertEquals(0, validator.finish().size());
    }
}
