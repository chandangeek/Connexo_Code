package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.SelectEvent;
import com.elster.jupiter.util.time.StopWatch;

public class SelectEventImpl implements SelectEvent {
	private final StopWatch stopWatch;
	private final String text;
	private int rowCount;

	
	public SelectEventImpl(String text) {
		this.text = text;
		this.stopWatch = new StopWatch();
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
		this.stopWatch.stop();
	}

	@Override
	public long getElapsed() {
		return stopWatch.getElapsed();
	}


	@Override
	public long getCpu() {
		return stopWatch.getCpu();
	}


	@Override
	public String getText() {
		return text;
	}


	@Override
	public int getRowCount() {
		return rowCount;
	}
	
	@Override
	public String toString() {
		return "Fetched " + rowCount + " tuples in " + (getElapsed()/1000L) + " µs for " + getText();
	}
}
