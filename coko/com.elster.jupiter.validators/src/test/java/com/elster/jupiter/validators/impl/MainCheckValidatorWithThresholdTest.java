/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.validation.ValidationResult;

import static com.elster.jupiter.validators.impl.Utils.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MainCheckValidatorWithThresholdTest extends MainCheckValidatorTest {

    @Test
    public void identicalReadingsValidationTest() {

        validateWithReadings(new ValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withValuedDifference(BIG_DECIMAL_10)
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withMinThreshold(BIG_DECIMAL_15));

        validateWithReadings(new ValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withValuedDifference(BIG_DECIMAL_30)
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withMinThreshold(BIG_DECIMAL_15));

    }

    private void validateWithReadings(ValidatorRule rule){
        ChannelReadings mainChannelReadings = new ChannelReadings(5);
        mainChannelReadings.setReadingValue(0, BIG_DECIMAL_10, INSTANT_2016_FEB_01);
        mainChannelReadings.setReadingValue(1, BIG_DECIMAL_20, INSTANT_2016_FEB_02);
        mainChannelReadings.setReadingValue(2, BIG_DECIMAL_30, INSTANT_2016_FEB_03);
        mainChannelReadings.setReadingValue(3, BIG_DECIMAL_1, INSTANT_2016_FEB_04);
        mainChannelReadings.setReadingValue(4, BIG_DECIMAL_2, INSTANT_2016_FEB_05);

        // NOTE: check channel readings are not validated!
        ValidatedChannelReadings checkReadings = new ValidatedChannelReadings(5);
        checkReadings.setReadingValue(0, BIG_DECIMAL_10, INSTANT_2016_FEB_01);
        checkReadings.setReadingValue(1, BIG_DECIMAL_20, INSTANT_2016_FEB_02);
        checkReadings.setReadingValue(2, BIG_DECIMAL_30, INSTANT_2016_FEB_03);
        checkReadings.setReadingValue(3, BIG_DECIMAL_3, INSTANT_2016_FEB_04);
        checkReadings.setReadingValue(4, BIG_DECIMAL_4, INSTANT_2016_FEB_05);

        ValidationConfiguration validationConfiguration = new ValidationConfiguration(rule, mainChannelReadings, checkReadings);
        MainCheckValidator validator = initValidator(validationConfiguration);

        assertThat(validationConfiguration.mainChannelReadings.readings.size()).isEqualTo(5);

        assertThat(validationConfiguration.mainChannelReadings.readings.stream()
                .map(validator::validate)
                .filter((c -> c.equals(ValidationResult.VALID))).count()).isEqualTo(5L);

        assertThat(validator.finish().size()).isEqualTo(0);

    }
}
