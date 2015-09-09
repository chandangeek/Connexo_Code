package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.PurgeConfiguration;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.time.Instant;
import java.time.Period;
import java.util.Optional;
import java.util.logging.Logger;

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
	private MeteringService meteringService;

	@Inject
	LifeCycleCategoryImpl(DataModel dataModel, MeteringService meteringService) {
		this.dataModel = dataModel;
		this.meteringService = meteringService;
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
		PurgeConfiguration.Builder builder = PurgeConfiguration.builder();
		if (kind.configure(builder, this)) {
			builder.logger(Logger.getLogger(getClass().getPackage().getName()));
			meteringService.configurePurge(builder.build());
		}
	}

	@Override
	public String getName() {
		return kind.name().toLowerCase();
	}

	@Override
	public String getTranslationKey() {
    	return TranslationKeys.Constants.DATA_LIFECYCLE_CATEGORY_NAME_PREFIX + kind.name();
	}

	Optional<LifeCycleCategoryImpl> asOf(Instant instant) {
		if (!instant.isBefore(modTime)) {
			return Optional.of(this);

		}
		return dataModel.mapper(LifeCycleCategoryImpl.class).getJournalEntry(instant, kind)
			.map(journalEntry -> journalEntry.get());
	}

	@Override
	public String toString() {
		return "" + this.getName() + " (Retention: " + getRetention() + ")";
	}
}
