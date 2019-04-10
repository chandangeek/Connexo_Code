package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;

import java.time.Instant;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ScheduleValidatorTest {


    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Test
    public void testNull() {
        expectedException.expect(LocalizedFieldValidationException.class);
        expectedException.expectMessage(MessageSeeds.COULD_NOT_VALIDATE_NEXTRUN.getDefaultFormat());
        ScheduleValidator.validate(null, null);
    }

    @Test
    public void testNextRunNull() {
        expectedException.expect(LocalizedFieldValidationException.class);
        expectedException.expectMessage(MessageSeeds.COULD_NOT_VALIDATE_NEXTRUN.getDefaultFormat());
        ScheduleValidator.validate(null, Instant.now());
    }

    @Test
    public void testNowNull() {
        expectedException.expect(LocalizedFieldValidationException.class);
        expectedException.expectMessage(MessageSeeds.COULD_NOT_VALIDATE_NEXTRUN.getDefaultFormat());
        ScheduleValidator.validate(Instant.now(), null);
    }

    @Test
    public void testScheduleBeforeNow() {
        expectedException.expect(LocalizedFieldValidationException.class);
        expectedException.expectMessage(MessageSeeds.SCHEDULED_BEFORE_NOW.getDefaultFormat());
        Instant now = Instant.now();
        ScheduleValidator.validate(now, now.plusNanos(1L));
    }

    @Test
    public void testScheduleEquaqlsNow() {
        Instant now = Instant.now();
        ScheduleValidator.validate(now, now);
    }

    @Test
    public void testScheduleAfterNow() {
        Instant now = Instant.now();
        ScheduleValidator.validate(now.plusNanos(1L), now);
    }

}
