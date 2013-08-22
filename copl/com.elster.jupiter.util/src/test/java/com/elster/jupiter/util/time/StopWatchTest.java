package com.elster.jupiter.util.time;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class StopWatchTest {

    @Test
    public void testElapsed() {
        StopWatch stopWatch = new StopWatch();

        long elapsed = stopWatch.getElapsed();
        assertThat(stopWatch.getElapsed()).isGreaterThan(elapsed);
    }

    @Test
    public void testStop() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.stop();
        long elapsed = stopWatch.getElapsed();
        assertThat(stopWatch.getElapsed()).isEqualTo(elapsed);
    }

    @Test
    public void testElapsedCpu() {
        StopWatch stopWatch = new StopWatch(true);
        long elapsed = stopWatch.getCpu();

        assertThat(stopWatch.getCpu()).isGreaterThanOrEqualTo(elapsed);
    }

    @Test
    public void testStopCpu() {
        StopWatch stopWatch = new StopWatch(true);
        stopWatch.stop();
        long elapsed = stopWatch.getCpu();

        assertThat(stopWatch.getCpu()).isEqualTo(elapsed);
    }

}
