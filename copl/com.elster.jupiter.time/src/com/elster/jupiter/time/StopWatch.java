package com.elster.jupiter.time;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public final class StopWatch {
	
	private final static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
	private final boolean measureCpu;
	private long cpu;
	private long elapsed;
	
	public StopWatch() {		
		this(false);			
	}
	
	public StopWatch(boolean measureCpu) {
		this.measureCpu = measureCpu;
		this.elapsed = System.nanoTime();	
		if (measureCpu) {
			this.cpu = threadMXBean.getCurrentThreadCpuTime();
		}
	}
	
	public void stop() {
		if (measureCpu) {
			this.cpu = threadMXBean.getCurrentThreadCpuTime() - cpu;
		}
		this.elapsed = System.nanoTime() - elapsed;
	}
	
	@Override
	public String toString() {
		return "Elapsed: " + elapsed + " ns - Cpu: " + cpu + " ns";
	}
	
	public long getElapsed() {
		return elapsed;
	}
	
	public long getCpu() {
		return cpu;
	}
	
}
