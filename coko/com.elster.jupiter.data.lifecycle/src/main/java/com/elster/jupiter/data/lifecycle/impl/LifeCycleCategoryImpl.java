package com.elster.jupiter.data.lifecycle.impl;

import java.time.Instant;
import java.time.Period;

import com.elster.jupiter.data.lifecycle.LifeCycleCategoryName;
import com.elster.jupiter.data.lifecycle.LifeCycleCategory;

public class LifeCycleCategoryImpl implements LifeCycleCategory {
	
	private LifeCycleCategoryName category;
	private int partitionSize;
	private int retention;
	@SuppressWarnings("unused")
	private Instant createTime;
	@SuppressWarnings("unused")
	private Instant modTime;
	@SuppressWarnings("unused")
	private String userName;
	@SuppressWarnings("unused")
	private long version;

	LifeCycleCategory init(LifeCycleCategoryName category) {
		this.category = category;
		this.partitionSize = 30;
		this.retention = 999 * 30;
		return this;
	}
	
	@Override
	public LifeCycleCategoryName getName() {
		return category;
	}

	@Override
	public int getRetainedPartitionCount() {
		return retention / partitionSize; 
	}

	@Override
	public Period getPartitionSize() {
		return Period.ofDays(partitionSize);
	}

	@Override
	public Period getRetention() {
		return Period.ofDays(retention);
	}
}
