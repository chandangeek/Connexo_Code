package com.elster.jupiter.data.lifecycle.impl;

import java.time.Instant;
import java.time.Period;
import java.util.Optional;

import javax.inject.Inject;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.orm.DataModel;

public class LifeCycleCategoryImpl implements LifeCycleCategory {
	
	private LifeCycleCategoryKind kind;
	private int partitionSize;
	private int retention;
	@SuppressWarnings("unused")
	private Instant createTime;
	private Instant modTime;
	@SuppressWarnings("unused")
	private String userName;
	@SuppressWarnings("unused")
	private long version;
	
	private DataModel dataModel;
	
	@Inject
	LifeCycleCategoryImpl(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	LifeCycleCategoryImpl init(LifeCycleCategoryKind kind) {
		this.kind = kind;
		this.partitionSize = 30;
		this.retention = 999 * 30;
		return this;
	}
	
	@Override
	public LifeCycleCategoryKind getKind() {
		return kind;
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
	
	@Override
	public void setRetentionDays(int days) {
		this.retention = days;
		dataModel.update(this);
	}
	
	@Override
	public String getName() {
		return kind.name().toLowerCase();
	}
	
	@Override
	public String getTranslationKey() {
    	return "data.lifecycle.category." + kind.name();
	}
	
	Optional<LifeCycleCategory> asOf(Instant instant) {
		if (!instant.isBefore(modTime)) {
			return Optional.of(this);
			
		} 
		return dataModel.mapper(LifeCycleCategory.class).getJournal(kind).stream()
			.filter(journalEntry -> !instant.isBefore(journalEntry.getJournalTime()))
			.map(journalEntry -> journalEntry.get())
			.findFirst();
	}
}
