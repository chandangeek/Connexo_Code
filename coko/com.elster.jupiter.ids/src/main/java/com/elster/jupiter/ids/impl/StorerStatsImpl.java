/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.StorerStats;
import com.elster.jupiter.ids.TimeSeriesEntry;

public class StorerStatsImpl implements StorerStats {

    private static final long NANOS_PER_MILLI = 1_000_000L;
    private int entryCount;
    private int insertCount;
    private int updateCount;
    private long nanos;

	@Override
	public int getEntryCount() {
		return entryCount;
	}

	@Override
	public int getInsertCount() {
		return insertCount;
	}

	@Override
	public int getUpdateCount() {
		return updateCount;
	}

	@Override
	public long getExecuteTime() {
		return nanos / NANOS_PER_MILLI;
	}

	void start() {
		nanos = System.nanoTime();
	}
	
	void stop() {
		nanos = System.nanoTime() - nanos;
	}
	
	void add(TimeSeriesEntry entry) {
		entryCount++;
	}
	
	@Override
	public String toString() {
		return "" + entryCount + " entries ( inserted: "  + insertCount + " updated: " + updateCount + ") in " + getExecuteTime() + " ms ";
	}

	public void addCount(int inserts, int updates) {
		this.insertCount += inserts;
		this.updateCount += updates;
	}
	
}
