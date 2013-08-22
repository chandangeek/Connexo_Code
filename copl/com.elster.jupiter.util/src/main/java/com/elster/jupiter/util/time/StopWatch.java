package com.elster.jupiter.util.time;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * StopWatch allows for timing the execution of code.
 */
public final class StopWatch {
	
	private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
	private final boolean measureCpu;
    private State state = new Running();
	private long cpu;
	private long elapsed;

    private interface State {
        long getElapsed();

        long getCpu();
    }

    private class Running implements State {

        @Override
        public long getCpu() {
            return threadMXBean.getCurrentThreadCpuTime() - cpu;
        }

        @Override
        public long getElapsed() {
            return System.nanoTime() - elapsed;
        }
    }

    private class Stopped implements State {

        @Override
        public long getCpu() {
            return cpu;
        }

        @Override
        public long getElapsed() {
            return elapsed;
        }
    }

    /**
     * Creates a StopWatch instance, that starts timing immediately.
     */
    public StopWatch() {
		this(false);			
	}

    /**
     * Creates a StopWatch instance, that starts timing immediately, and which will also register cpu timing.
     */
	public StopWatch(boolean measureCpu) {
		this.measureCpu = measureCpu;
		this.elapsed = System.nanoTime();	
		if (measureCpu) {
			this.cpu = threadMXBean.getCurrentThreadCpuTime();
		}
	}

    /**
     * Marks the end of timing.
     */
	public void stop() {
		if (measureCpu) {
			this.cpu = threadMXBean.getCurrentThreadCpuTime() - cpu;
		}
		this.elapsed = System.nanoTime() - elapsed;
        state = new Stopped();
	}
	
	@Override
	public String toString() {
		return "Elapsed: " + elapsed + " ns - Cpu: " + cpu + " ns";
	}

	public long getElapsed() {
		return state.getElapsed();
	}
	
	public long getCpu() {
		return state.getCpu();
	}
	
}
