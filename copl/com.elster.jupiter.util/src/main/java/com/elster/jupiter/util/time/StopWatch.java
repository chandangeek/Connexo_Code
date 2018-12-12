/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * StopWatch allows for timing the execution of code.
 */
public final class StopWatch {
	
	private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
	private final boolean measureCpu;
    private State state = new Stopped();
	private long cpu = 0;
	private long elapsed = 0;
	private long lap = 0;
	private long cpuStart;
	private long elapsedStart;
	private long lapStart;

    private interface State {
        long getElapsed();
        long getCpu();
        State stop();
        State start();
    }

    private class Running implements State {

        @Override
        public long getCpu() {
            return THREAD_MX_BEAN.getCurrentThreadCpuTime() - cpuStart + cpu;
        }

        @Override
        public long getElapsed() {
            return System.nanoTime() - elapsedStart + elapsed;
        }
        
        @Override
        public State start() {
        	return this;
        }
        
        @Override
        public State stop() {
        	if (measureCpu) {
    			cpu += THREAD_MX_BEAN.getCurrentThreadCpuTime() - cpuStart;
    		}
    		long now = System.nanoTime();
    		elapsed += now - elapsedStart;
    		lap += now - lapStart;
        	return new Stopped();
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
        
        @Override
        public State start() {
        	elapsedStart = System.nanoTime();	
    		lapStart = elapsedStart;
    		if (measureCpu) {
    			cpuStart = THREAD_MX_BEAN.getCurrentThreadCpuTime();
    		}
    		return new Running();
        }
        
        @Override
        public State stop() {
        	return this;
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
		start();
	}

    /**
     * Marks the end of timing.
     */
	public void stop() {
		state = state.stop();
	}
	
	/*
	 * Restarts 
	 */
	public final void start() {
		state = state.start();
	}
	
	public String toString() {
		return "Elapsed: " + getElapsed() + " ns - Cpu: " + getCpu() + " ns";
	}

	public long getElapsed() {
		return state.getElapsed();
	}
	
	public long getCpu() {
		return state.getCpu();
	}
	
	public long lap() {
		long previousLap = lapStart;
		this.lapStart = System.nanoTime();
		long result = lapStart - previousLap + lap;
		lap = 0;
		return result;
	}
	
}
