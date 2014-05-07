package com.elster.jupiter.util.time;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    
    @Test
    public void testRestart()  {
    	StopWatch stopWatch1 = new StopWatch(true);
    	StopWatch stopWatch2 = new StopWatch(true);
    	stopWatch2.stop();
    	try {
    		Thread.sleep(10L);
    		for (int i = 0 ; i < 1000000 ; i++) {
    			Math.pow(i, 7);
    		}
    	} catch (InterruptedException ex) {
    	}
    	stopWatch2.start();
    	stopWatch2.stop();
    	stopWatch1.stop();
    	assertThat(stopWatch1.getElapsed()).isGreaterThan(stopWatch2.getElapsed());
    	assertThat(stopWatch1.getCpu()).isGreaterThan(stopWatch2.getCpu());
    	
    }

}
