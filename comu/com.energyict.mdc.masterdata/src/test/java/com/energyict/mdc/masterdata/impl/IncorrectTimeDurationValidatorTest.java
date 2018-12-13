/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.masterdata.exceptions.MessageSeeds;

import javax.validation.ConstraintValidatorContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class IncorrectTimeDurationValidatorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConstraintValidatorContext context;

    @Test
    public void testIsValid() throws Exception {
        TimeDuration interval = new TimeDuration(1, TimeDuration.TimeUnit.YEARS);
        assertThat(new IncorrectTimeDurationValidator().isValid(interval, context)).isTrue();

    }

    @Test
    public void testInvalidMultipleYears() throws Exception {
        TimeDuration interval = new TimeDuration(2, TimeDuration.TimeUnit.YEARS);
        assertThat(new IncorrectTimeDurationValidator().isValid(interval, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate("{" + MessageSeeds.INTERVAL_IN_YEARS_MUST_BE_ONE.getKey() + "}");
    }

    @Test
    public void testInvalidMultipleMonths() throws Exception {
        TimeDuration interval = new TimeDuration(2, TimeDuration.TimeUnit.MONTHS);
        assertThat(new IncorrectTimeDurationValidator().isValid(interval, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate("{" + MessageSeeds.INTERVAL_IN_MONTHS_MUST_BE_ONE.getKey() + "}");
    }

    @Test
    public void testInvalidMultipleDays() throws Exception {
        TimeDuration interval = new TimeDuration(2, TimeDuration.TimeUnit.DAYS);
        assertThat(new IncorrectTimeDurationValidator().isValid(interval, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate("{" + MessageSeeds.INTERVAL_IN_DAYS_MUST_BE_ONE.getKey() + "}");
    }

    @Test
    public void testInvalidIntervalInWeeks() throws Exception {
        TimeDuration interval = new TimeDuration(1, TimeDuration.TimeUnit.WEEKS);
        assertThat(new IncorrectTimeDurationValidator().isValid(interval, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate("{" + MessageSeeds.LOAD_PROFILE_TYPE_INTERVAL_IN_WEEKS_IS_NOT_SUPPORTED.getKey() + "}");
    }

    @Test
    public void testInvalidNegativeIntervalSeconds() throws Exception {
        TimeDuration interval = new TimeDuration(-1, TimeDuration.TimeUnit.SECONDS);
        assertThat(new IncorrectTimeDurationValidator().isValid(interval, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate("{" + MessageSeeds.INTERVAL_MUST_BE_STRICTLY_POSITIVE.getKey() + "}");
    }

    @Test
    public void testInvalidEmptyInterval() throws Exception {
        TimeDuration interval = new TimeDuration(0, TimeDuration.TimeUnit.MINUTES);
        assertThat(new IncorrectTimeDurationValidator().isValid(interval, context)).isFalse();
        verify(context).buildConstraintViolationWithTemplate("{" + MessageSeeds.FIELD_IS_REQUIRED.getKey() + "}");
    }
}
