package com.elster.jupiter.util.time.impl;

import com.elster.jupiter.util.time.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.fest.assertions.api.Assertions.assertThat;

public class DefaultClockTest {

    private Clock clock;

    @Before
    public void setUp() {
        clock = new DefaultClock();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetTimeZone() throws Exception {
        assertThat(clock.getTimeZone()).isEqualTo(TimeZone.getDefault());
    }

    @Test
    public void testNow() throws Exception {
        Date earlier = new Date();
        Date now = clock.now();
        Date later = new Date();
        assertThat(now).isAfterOrEqualsTo(earlier)
                .isBeforeOrEqualsTo(later);
    }
}
