package com.elster.jupiter.util.time;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

public class ProgrammableClockTest {

    private static final long NOW_MILLIS = 654641565164L;
    private ProgrammableClock programmableClock;

    private enum Mock implements ProgrammableClock.SystemAbstraction {
        SYSTEM;

        @Override
        public long currentTimeMillis() {
            return NOW_MILLIS;
        }
    }

    @Before
    public void setUp() throws Exception {
        programmableClock = new ProgrammableClock(Mock.SYSTEM);
    }

    @Test
    public void testDefaultNow() {
        assertThat(programmableClock.now().getTime()).isEqualTo(NOW_MILLIS);
    }

    @Test
    public void testDefaultTimeZone() {
        assertThat(programmableClock.getTimeZone()).isEqualTo(TimeZone.getDefault());
    }

    @Test
    public void testFrozen() {
        Date date = new Date(1515L);
        assertThat(programmableClock.frozenAt(date).now()).isEqualTo(date);
    }

    @Test
    public void testOffset() {
        assertThat(programmableClock.withOffset(-60000L).now()).isEqualTo(new Date(NOW_MILLIS - 60000L));
    }

}
