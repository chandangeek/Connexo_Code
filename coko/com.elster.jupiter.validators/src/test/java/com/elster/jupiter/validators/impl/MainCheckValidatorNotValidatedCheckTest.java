/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.validation.ValidationResult;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MainCheckValidatorNotValidatedCheckTest extends MainCheckValidatorTest {

    @Test
    public void identicalReadingsValidationTest() {

        validateWithIdenticalReadings(new MainCheckValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withValuedDifference(bigDecimal(100D))
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold());

        validateWithIdenticalReadings(new MainCheckValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withValuedDifference(bigDecimal(1D))
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold());

        validateWithIdenticalReadings(new MainCheckValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withValuedDifference(bigDecimal(0D))
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold());

        validateWithIdenticalReadings(new MainCheckValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withValuedDifference(bigDecimal(100D))
                .passIfNoRefData(true)
                .useValidatedData(false)
                .withNoMinThreshold());
    }

    private void validateWithIdenticalReadings(MainCheckValidatorRule rule){
        ChannelReadings mainChannelReadings = new ChannelReadings(3);
        mainChannelReadings.setReadingValue(0, bigDecimal(10D), instant("20160101000000"));
        mainChannelReadings.setReadingValue(1, bigDecimal(20D), instant("20160102000000"));
        mainChannelReadings.setReadingValue(2, bigDecimal(30D), instant("20160103000000"));

        ValidatedChannelReadings checkReadings = new ValidatedChannelReadings(3);
        checkReadings.setReadingValue(0, bigDecimal(10D), instant("20160101000000"),ValidationResult.SUSPECT);
        checkReadings.setReadingValue(1, bigDecimal(20D), instant("20160102000000"));
        checkReadings.setReadingValue(2, bigDecimal(30D), instant("20160103000000"));

        ValidationConfiguration validationConfiguration = new ValidationConfiguration(rule, mainChannelReadings, checkReadings);
        MainCheckValidator validator = initValidator(validationConfiguration);

        assertEquals(3, validationConfiguration.mainChannelReadings.readings.size());

        assertEquals(0L, validationConfiguration.mainChannelReadings.readings.stream()
                .map(validator::validate)
                .filter((c -> !c.equals(ValidationResult.VALID))).count());

        assertEquals(0, validator.finish().size());
    }
}
