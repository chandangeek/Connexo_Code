/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.validation.ValidationResult;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MainCheckValidatorWithThresholdTest extends MainCheckValidatorTest {

    @Test
    public void identicalReadingsValidationTest() {

        validateWithReadings(new MainCheckValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withValuedDifference(bigDecimal(10D))
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withMinThreshold(bigDecimal(15D)));

        validateWithReadings(new MainCheckValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withValuedDifference(bigDecimal(30D))
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withMinThreshold(bigDecimal(15D)));

    }

    private void validateWithReadings(MainCheckValidatorRule rule){
        ChannelReadings mainChannelReadings = new ChannelReadings(5);
        mainChannelReadings.setReadingValue(0, bigDecimal(10D), instant("20160101000000"));
        mainChannelReadings.setReadingValue(1, bigDecimal(20D), instant("20160102000000"));
        mainChannelReadings.setReadingValue(2, bigDecimal(30D), instant("20160103000000"));
        mainChannelReadings.setReadingValue(3, bigDecimal(1D), instant("20160104000000"));
        mainChannelReadings.setReadingValue(4, bigDecimal(2D), instant("20160105000000"));

        // NOTE: check channel readings are not validated!
        ValidatedChannelReadings checkReadings = new ValidatedChannelReadings(5);
        checkReadings.setReadingValue(0, bigDecimal(10D), instant("20160101000000"));
        checkReadings.setReadingValue(1, bigDecimal(20D), instant("20160102000000"));
        checkReadings.setReadingValue(2, bigDecimal(30D), instant("20160103000000"));
        checkReadings.setReadingValue(3, bigDecimal(3D), instant("20160104000000"));
        checkReadings.setReadingValue(4, bigDecimal(4D), instant("20160105000000"));

        ValidationConfiguration validationConfiguration = new ValidationConfiguration(rule, mainChannelReadings, checkReadings);
        MainCheckValidator validator = initValidator(validationConfiguration);

        assertEquals(5, validationConfiguration.mainChannelReadings.readings.size());

        assertEquals(5L, validationConfiguration.mainChannelReadings.readings.stream()
                .map(validator::validate)
                .filter((c -> c.equals(ValidationResult.VALID))).count());

        assertEquals(0, validator.finish().size());

    }
}
