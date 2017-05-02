/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.validation.ValidationResult;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import org.mockito.Mock;

import static com.elster.jupiter.validators.impl.Utils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

public class ReferenceValidatorMiscTest extends ReferenceValidatorTest {

    @Mock
    private Logger logger;

    private StringBuffer logs;

    @Test
    public void testNotFullyConfigured() {
        validateWithReadings(new ReferenceValidatorRule()
                .withCheckPurpose(VALIDATING_PURPOSE)
                .withValidatingReadingType(VALIDATING_READING_TYPE)
                .withValuedDifference(BIG_DECIMAL_100)
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold()
                .notFullyConfigured(),"WARNING: Failed to validate period \"Mon, 1 Feb 2016 12:00 AM until Fri, 5 Feb 2016 12:00 AM\" using method \"Reference comparison\" on Validating usage point/Purpose 1/[Daily] Secondary Delta A+ (kWh) since the check usage point, purpose and reading type are not specified");
    }

    @Test
    public void testReferenceReadingTypeNotComparable() {
        validateWithReadings(new ReferenceValidatorRule()
                .withCheckPurpose(VALIDATING_PURPOSE)
                .withReferencePurpose(REFERENCE_PURPOSE)
                .withValidatingReadingType(VALIDATING_READING_TYPE)
                .withReferenceReadingType(REFERENCE_READING_TYPE_NOT_COMPARABLE)
                .withReferenceUsagePoint(REFERENCE_USAGE_POINT)
                .withValuedDifference(BIG_DECIMAL_100)
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold(),"WARNING: Failed to validate period \"Mon, 1 Feb 2016 12:00 AM until Fri, 5 Feb 2016 12:00 AM\" using method \"Reference comparison\" on Validating usage point/Purpose 1/[Daily] Secondary Delta A+ (kWh) since specified check output doesnt match the main reading type");
    }

    @Test
    public void testReferenceConfigurationChanged() {
        validateWithReadings(new ReferenceValidatorRule()
                .withCheckPurpose(VALIDATING_PURPOSE)
                .withReferencePurpose(REFERENCE_PURPOSE)
                .withValidatingReadingType(VALIDATING_READING_TYPE)
                .withChangedReferenceConfiguration()
                .withReferenceReadingType(REFERENCE_READING_TYPE_COMPARABLE)
                .withReferenceUsagePoint(REFERENCE_USAGE_POINT)
                .withValuedDifference(BIG_DECIMAL_100)
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold(),"WARNING: Failed to validate period \"Mon, 1 Feb 2016 12:00 AM until Fri, 5 Feb 2016 12:00 AM\" using method \"Reference comparison\" on Validating usage point/Purpose 1/[Daily] Secondary Delta A+ (kWh) since the specified purpose/reading type doesnt exist on the Reference usage point");
    }

    @Test
    public void testChannelWithMissingDataPass() {
        validateWithReadings(new ReferenceValidatorRule()
                .withCheckPurpose(VALIDATING_PURPOSE)
                .withReferencePurpose(REFERENCE_PURPOSE)
                .withValidatingReadingType(VALIDATING_READING_TYPE)
                .withReferenceReadingType(VALIDATING_READING_TYPE)
                .withReferenceUsagePoint(REFERENCE_USAGE_POINT)
                .withValuedDifference(BIG_DECIMAL_100)
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold(), "WARNING: Failed to validate period \"Mon, 1 Feb 2016 12:00 AM until Fri, 5 Feb 2016 12:00 AM\" using method \"Reference comparison\" on Validating usage point/Purpose 1/[Daily] Secondary Delta A+ (kWh) since data from check output is missing or not validated", true);
    }

    @Test
    public void testChannelWithMissingDataNotPass() {
        validateWithReadings(new ReferenceValidatorRule()
                .withCheckPurpose(VALIDATING_PURPOSE)
                .withReferencePurpose(REFERENCE_PURPOSE)
                .withValidatingReadingType(VALIDATING_READING_TYPE)
                .withReferenceReadingType(VALIDATING_READING_TYPE)
                .withReferenceUsagePoint(REFERENCE_USAGE_POINT)
                .withValuedDifference(BIG_DECIMAL_100)
                .passIfNoRefData(true)
                .useValidatedData(false)
                .withNoMinThreshold(), "WARNING: Failed to validate period \"Mon, 1 Feb 2016 12:00 AM until Fri, 5 Feb 2016 12:00 AM\" using method \"Reference comparison\" on Validating usage point/Purpose 1/[Daily] Secondary Delta A+ (kWh) since data from check output is missing or not validated", true);
    }

    private void mockLogger(ReferenceComparisonValidator validator) {
        logs = new StringBuffer();
        doAnswer(invocationOnMock -> {
            Level level = (Level) (invocationOnMock.getArguments()[0]);
            logs.append(level).append(":").append(" ");
            logs.append((String) (invocationOnMock.getArguments()[1]));
            return null;
        }).when(logger).log(any(Level.class), anyString(), any(Throwable.class));

        field("logger").ofType(Logger.class).in(validator).set(logger);
    }

    @Override
    ReferenceComparisonValidator initValidator(ValidationConfiguration validationConfiguration) {
        ReferenceComparisonValidator validator = new ReferenceComparisonValidator(thesaurus, propertySpecService, validationConfiguration.metrologyConfigurationService, validationConfiguration.validationService, validationConfiguration.meteringService, validationConfiguration.rule
                .createProperties());
        mockLogger(validator);
        validator.init(validationConfiguration.validatingChannel, validationConfiguration.rule.validatingReadingType, validationConfiguration.range);
        return validator;
    }

    private void validateWithReadings(ReferenceValidatorRule rule, String warning){
        validateWithReadings(rule,warning,false);
    }

    private void validateWithReadings(ReferenceValidatorRule rule, String warning, boolean missingData) {
        ChannelReadings validatingChannelReadings = new ChannelReadings(3);
        validatingChannelReadings.setReadingValue(0, BIG_DECIMAL_10, INSTANT_2016_FEB_01);
        validatingChannelReadings.setReadingValue(1, BIG_DECIMAL_20, INSTANT_2016_FEB_02);
        validatingChannelReadings.setReadingValue(2, BIG_DECIMAL_30, INSTANT_2016_FEB_03);

        ValidatedChannelReadings referenceReadings = new ValidatedChannelReadings(3);
        referenceReadings.setReadingValue(0, BIG_DECIMAL_10, INSTANT_2016_FEB_01);
        if (!missingData) {
            referenceReadings.setReadingValue(1, BIG_DECIMAL_20, INSTANT_2016_FEB_02);
        }
        referenceReadings.setReadingValue(2, BIG_DECIMAL_30, INSTANT_2016_FEB_03);

        ValidationConfiguration validationConfiguration = new ValidationConfiguration(rule, validatingChannelReadings, referenceReadings);
        ReferenceComparisonValidator validator = initValidator(validationConfiguration);
        assertThat(validationConfiguration.validatingChannelReadings.readings.size()).isEqualTo(3);
        long validReadingsCount = missingData?(rule.passIfNoData?0L:2L):3l;
        assertThat(validationConfiguration.validatingChannelReadings.readings.stream()
                .map(validator::validate)
                .filter((c -> c.equals(ValidationResult.NOT_VALIDATED))).count()).isEqualTo(validReadingsCount);
        assertThat(validator.finish().size()).isEqualTo(0);
        assertThat(logs.toString()).contains(warning);
    }
}
