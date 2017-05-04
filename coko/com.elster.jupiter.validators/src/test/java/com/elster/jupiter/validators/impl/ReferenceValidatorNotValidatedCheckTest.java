/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.validation.ValidationResult;

import java.math.BigDecimal;

import org.junit.Test;

import static com.elster.jupiter.validators.impl.Utils.BIG_DECIMAL_0;
import static com.elster.jupiter.validators.impl.Utils.BIG_DECIMAL_1;
import static com.elster.jupiter.validators.impl.Utils.BIG_DECIMAL_10;
import static com.elster.jupiter.validators.impl.Utils.BIG_DECIMAL_100;
import static com.elster.jupiter.validators.impl.Utils.BIG_DECIMAL_20;
import static com.elster.jupiter.validators.impl.Utils.BIG_DECIMAL_30;
import static com.elster.jupiter.validators.impl.Utils.INSTANT_2016_FEB_01;
import static com.elster.jupiter.validators.impl.Utils.INSTANT_2016_FEB_02;
import static com.elster.jupiter.validators.impl.Utils.INSTANT_2016_FEB_03;
import static org.assertj.core.api.Assertions.assertThat;

public class ReferenceValidatorNotValidatedCheckTest extends ReferenceValidatorTest {
    @Test
    public void identicalReadingsValidationTest() {
        validateWithIdenticalReadings(new ReferenceValidatorRule()
                .withCheckPurpose(VALIDATING_PURPOSE)
                .withReferencePurpose(REFERENCE_PURPOSE)
                .withValidatingReadingType(VALIDATING_READING_TYPE)
                .withReferenceReadingType(VALIDATING_READING_TYPE)
                .withReferenceUsagePoint(REFERENCE_USAGE_POINT)
                .withValuedDifference(BIG_DECIMAL_100)
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold());

        validateWithIdenticalReadings(new ReferenceValidatorRule()
                .withCheckPurpose(VALIDATING_PURPOSE)
                .withReferencePurpose(REFERENCE_PURPOSE)
                .withValidatingReadingType(VALIDATING_READING_TYPE)
                .withReferenceReadingType(VALIDATING_READING_TYPE)
                .withReferenceUsagePoint(REFERENCE_USAGE_POINT)
                .withValuedDifference(BIG_DECIMAL_1)
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold());

        validateWithIdenticalReadings(new ReferenceValidatorRule()
                .withCheckPurpose(VALIDATING_PURPOSE)
                .withReferencePurpose(REFERENCE_PURPOSE)
                .withValidatingReadingType(VALIDATING_READING_TYPE)
                .withReferenceReadingType(VALIDATING_READING_TYPE)
                .withReferenceUsagePoint(REFERENCE_USAGE_POINT)
                .withValuedDifference(BIG_DECIMAL_0)
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold());

        validateWithIdenticalReadings(new ReferenceValidatorRule()
                .withCheckPurpose(VALIDATING_PURPOSE)
                .withReferencePurpose(REFERENCE_PURPOSE)
                .withValidatingReadingType(VALIDATING_READING_TYPE)
                .withReferenceReadingType(VALIDATING_READING_TYPE)
                .withReferenceUsagePoint(REFERENCE_USAGE_POINT)
                .withValuedDifference(BIG_DECIMAL_100)
                .passIfNoRefData(true)
                .useValidatedData(false)
                .withNoMinThreshold());
    }

    @Test
    public void identicalReadingsValidationTestWithMuptilierInReferenceReadingType() {
        validateWithIdenticalReadings(new ReferenceValidatorRule()
                .withCheckPurpose(VALIDATING_PURPOSE)
                .withReferencePurpose(REFERENCE_PURPOSE)
                .withValidatingReadingType(VALIDATING_READING_TYPE)
                .withReferenceReadingType(REFERENCE_READING_TYPE_COMPARABLE)
                .withReferenceUsagePoint(REFERENCE_USAGE_POINT)
                .withValuedDifference(BIG_DECIMAL_100)
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold(), COMPARABLE_READING_TYPE_MULTIPIER);

        validateWithIdenticalReadings(new ReferenceValidatorRule()
                .withCheckPurpose(VALIDATING_PURPOSE)
                .withReferencePurpose(REFERENCE_PURPOSE)
                .withValidatingReadingType(VALIDATING_READING_TYPE)
                .withReferenceReadingType(REFERENCE_READING_TYPE_COMPARABLE)
                .withReferenceUsagePoint(REFERENCE_USAGE_POINT)
                .withValuedDifference(BIG_DECIMAL_1)
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold(), COMPARABLE_READING_TYPE_MULTIPIER);

        validateWithIdenticalReadings(new ReferenceValidatorRule()
                .withCheckPurpose(VALIDATING_PURPOSE)
                .withReferencePurpose(REFERENCE_PURPOSE)
                .withValidatingReadingType(VALIDATING_READING_TYPE)
                .withReferenceReadingType(REFERENCE_READING_TYPE_COMPARABLE)
                .withReferenceUsagePoint(REFERENCE_USAGE_POINT)
                .withValuedDifference(BIG_DECIMAL_0)
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold(), COMPARABLE_READING_TYPE_MULTIPIER);

        validateWithIdenticalReadings(new ReferenceValidatorRule()
                .withCheckPurpose(VALIDATING_PURPOSE)
                .withReferencePurpose(REFERENCE_PURPOSE)
                .withValidatingReadingType(VALIDATING_READING_TYPE)
                .withReferenceReadingType(REFERENCE_READING_TYPE_COMPARABLE)
                .withReferenceUsagePoint(REFERENCE_USAGE_POINT)
                .withValuedDifference(BIG_DECIMAL_100)
                .passIfNoRefData(true)
                .useValidatedData(false)
                .withNoMinThreshold(), COMPARABLE_READING_TYPE_MULTIPIER);
    }

    private void validateWithIdenticalReadings(ReferenceValidatorRule rule) {
        validateWithIdenticalReadings(rule, BigDecimal.valueOf(1D));
    }

    private void validateWithIdenticalReadings(ReferenceValidatorRule rule, BigDecimal referenceValueMultipier) {
        ChannelReadings validatingChannelReadings = new ChannelReadings(3);
        validatingChannelReadings.setReadingValue(0, BIG_DECIMAL_10, INSTANT_2016_FEB_01);
        validatingChannelReadings.setReadingValue(1, BIG_DECIMAL_20, INSTANT_2016_FEB_02);
        validatingChannelReadings.setReadingValue(2, BIG_DECIMAL_30, INSTANT_2016_FEB_03);

        ValidatedChannelReadings referenceReadings = new ValidatedChannelReadings(3);
        referenceReadings.setReadingValue(0, BIG_DECIMAL_10.multiply(referenceValueMultipier), INSTANT_2016_FEB_01, ValidationResult.SUSPECT);
        referenceReadings.setReadingValue(1, BIG_DECIMAL_20.multiply(referenceValueMultipier), INSTANT_2016_FEB_02);
        referenceReadings.setReadingValue(2, BIG_DECIMAL_30.multiply(referenceValueMultipier), INSTANT_2016_FEB_03);

        ValidationConfiguration validationConfiguration = new ValidationConfiguration(rule, validatingChannelReadings, referenceReadings);
        ReferenceComparisonValidator validator = initValidator(validationConfiguration);
        assertThat(validationConfiguration.validatingChannelReadings.readings.size()).isEqualTo(3);
        assertThat(validationConfiguration.validatingChannelReadings.readings.stream()
                .map(validator::validate)
                .filter((c -> !c.equals(ValidationResult.VALID))).count()).isEqualTo(0);
        assertThat(validator.finish().size()).isEqualTo(0);

    }
}
