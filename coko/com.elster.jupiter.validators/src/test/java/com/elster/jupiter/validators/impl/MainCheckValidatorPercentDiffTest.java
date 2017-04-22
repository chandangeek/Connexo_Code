/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.validation.ValidationResult;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MainCheckValidatorPercentDiffTest extends MainCheckValidatorTest {
    @Test
    public void identicalReadingsValidationTest() {

        validateWithReadings(new MainCheckValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withPercentDifference(50D)
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold());
    }

    private void validateWithReadings(MainCheckValidatorRule rule){
        ChannelReadings mainChannelReadings = new ChannelReadings(3);
        mainChannelReadings.setReadingValue(0, bigDecimal(10D), instant("20160101000000"));
        mainChannelReadings.setReadingValue(1, bigDecimal(20D), instant("20160102000000"));
        mainChannelReadings.setReadingValue(2, bigDecimal(30D), instant("20160103000000"));

        // NOTE: check channel readings are not validated!
        ValidatedChannelReadings checkReadings = new ValidatedChannelReadings(3);
        checkReadings.setReadingValue(0, bigDecimal(10D), instant("20160101000000"));
        checkReadings.setReadingValue(1, bigDecimal(50D), instant("20160102000000"));
        checkReadings.setReadingValue(2, bigDecimal(130D), instant("20160103000000"));

        ValidationConfiguration validationConfiguration = new ValidationConfiguration(rule, mainChannelReadings, checkReadings);
        MainCheckValidator validator = initValidator(validationConfiguration);

        assertEquals(3, validationConfiguration.mainChannelReadings.readings.size());

        assertEquals(2L, validationConfiguration.mainChannelReadings.readings.stream()
                .map(validator::validate)
                .filter((c -> !c.equals(ValidationResult.VALID))).count());
        assertEquals(0, validator.finish().size());
    }
}
