package com.elster.jupiter.util.time.impl;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import org.junit.Test;

import java.time.Clock;
import java.time.ZonedDateTime;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

public class OffsetClockTest {

    private static final ZonedDateTime ACTUAL_TIME = ZonedDateTime.of(2013, 8, 17, 17, 8, 45, 123456789, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime IN_THE_PAST = ZonedDateTime.of(2011, 5, 23, 2, 29, 23, 103456789, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime IN_THE_FUTURE = ZonedDateTime.of(2018, 3, 8, 16, 22, 13, 120456789, TimeZoneNeutral.getMcMurdo());

    private static final Clock ACTUAL_CLOCK = Clock.fixed(ACTUAL_TIME.toInstant(), ACTUAL_TIME.getZone());

    @Test
    public void testCreatingIt() {
        Clock offsetClock = new OffsetClock(ACTUAL_CLOCK, IN_THE_PAST.toLocalDateTime());

        assertThat(ZonedDateTime.now(offsetClock)).isEqualTo(IN_THE_PAST);
    }

    @Test
    public void testResettingIt() {
        OffsetClock offsetClock = new OffsetClock(ACTUAL_CLOCK, IN_THE_PAST.toLocalDateTime());

        assertThat(ZonedDateTime.now(offsetClock)).isEqualTo(IN_THE_PAST);

        offsetClock.set(IN_THE_FUTURE.toLocalDateTime());

        assertThat(ZonedDateTime.now(offsetClock)).isEqualTo(IN_THE_FUTURE);
    }

}