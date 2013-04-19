package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.StorerStats;
import com.elster.jupiter.ids.TimeSeriesEntry;

public class StorerStatsImpl implements StorerStats {
	int entryCount;
	int insertCount;
	int updateCount;
	long nanos;

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
		return nanos / 1_000_000L;
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
