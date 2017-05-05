/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.validation.ValidationResult;

import org.junit.Test;

import static com.elster.jupiter.validators.impl.Utils.BIG_DECIMAL_10;
import static com.elster.jupiter.validators.impl.Utils.BIG_DECIMAL_130;
import static com.elster.jupiter.validators.impl.Utils.BIG_DECIMAL_20;
import static com.elster.jupiter.validators.impl.Utils.BIG_DECIMAL_30;
import static com.elster.jupiter.validators.impl.Utils.BIG_DECIMAL_50;
import static com.elster.jupiter.validators.impl.Utils.INSTANT_2016_FEB_01;
import static com.elster.jupiter.validators.impl.Utils.INSTANT_2016_FEB_02;
import static com.elster.jupiter.validators.impl.Utils.INSTANT_2016_FEB_03;
import static org.junit.Assert.assertEquals;

public class ReferenceValidatorPercentDiffTest extends ReferenceValidatorTest {
    @Test
    public void identicalReadingsValidationTest() {

        validateWithReadings(new ReferenceValidatorRule()
                .withCheckPurpose(VALIDATING_PURPOSE)
                .withReferencePurpose(REFERENCE_PURPOSE)
                .withValidatingReadingType(VALIDATING_READING_TYPE)
                .withReferenceReadingType(VALIDATING_READING_TYPE)
                .withReferenceUsagePoint(REFERENCE_USAGE_POINT)
                .withPercentDifference(50D)
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold());
    }

    private void validateWithReadings(ReferenceValidatorRule rule){
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
        ReferenceComparisonValidator validator = initValidator(validationConfiguration);

        assertEquals(3, validationConfiguration.validatingChannelReadings.readings.size());

        assertEquals(2L, validationConfiguration.validatingChannelReadings.readings.stream()
                .map(validator::validate)
                .filter((c -> !c.equals(ValidationResult.VALID))).count());
        assertEquals(0, validator.finish().size());
    }
}
