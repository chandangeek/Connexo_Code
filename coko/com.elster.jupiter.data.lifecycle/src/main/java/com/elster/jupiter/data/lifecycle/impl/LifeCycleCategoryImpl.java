/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.data.lifecycle.impl;

import com.elster.jupiter.data.lifecycle.LifeCycleCategory;
import com.elster.jupiter.data.lifecycle.LifeCycleCategoryKind;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.PurgeConfiguration;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.JournalEntry;

import javax.inject.Inject;
import java.time.Instant;
import java.time.Period;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

final class LifeCycleCategoryImpl implements LifeCycleCategory {

	private LifeCycleCategoryKind kind;
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

	private final DataModel dataModel;
	private final Thesaurus thesaurus;
	private final MeteringService meteringService;

	@Inject
	LifeCycleCategoryImpl(DataModel dataModel, Thesaurus thesaurus, MeteringService meteringService) {
		this.dataModel = dataModel;
		this.thesaurus = thesaurus;
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
	public long getVersion() {
		return this.version;
	}

	@Override
	public String getName() {
		return kind.name().toLowerCase();
	}

	@Override
	public String getDisplayName() {
		return LifeCycleCategoryKindTranslationKeys
				.from(this.kind)
                .map(this.thesaurus::getFormat)
                .map(nlsMessageFormat -> nlsMessageFormat.format())
                .orElseGet(() -> this.kind.name());
	}

	Optional<LifeCycleCategoryImpl> asOf(Instant instant) {
		if (!instant.isBefore(modTime)) {
			return Optional.of(this);

		}
		return dataModel.mapper(LifeCycleCategoryImpl.class).getJournalEntry(instant, kind)
			.map(JournalEntry::get);
	}

	@Override
	public String toString() {
		return "" + this.getName() + " (Retention: " + getRetention() + ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		LifeCycleCategoryImpl that = (LifeCycleCategoryImpl) o;

		return this.kind == that.kind;
	}

	@Override
	public int hashCode() {
		return Objects.hash(kind);
	}

}