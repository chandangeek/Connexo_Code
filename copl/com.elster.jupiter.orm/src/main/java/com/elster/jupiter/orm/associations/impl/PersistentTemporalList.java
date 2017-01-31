/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.associations.impl;

import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.orm.associations.TemporalList;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;

import com.google.common.collect.ImmutableList;

import java.time.Instant;
import java.util.List;

public class PersistentTemporalList<T extends Effectivity> extends AbstractPersistentTemporalAspect<T> implements TemporalList<T> {
	private Instant effectiveDate;
	private List<T> effectives;

	PersistentTemporalList(ForeignKeyConstraintImpl constraint, DataMapperImpl<T> dataMapper, Object owner) {
		super(constraint, dataMapper, owner);
	}

	@Override
	public boolean add(T element) {
		if (effectiveDate != null && element.isEffectiveAt(effectiveDate)) {
			effectives.add(element);
		}
		return super.add(element);
	}

	@Override
	public boolean remove(T element) {
		if (element.isEffectiveAt(effectiveDate)) {
			effectives.remove(element);
		}
		return super.remove(element);
	}

	@Override
	public void clear() {
		all().forEach(super::remove);
	}

	@Override
	public List<T> effective(Instant when) {
		if (effectiveDate == null || !effectiveDate.equals(when)) {
			setCache(when,allEffective(when));
		}
		return ImmutableList.copyOf(effectives);
	}

	public void setCache(Instant effectiveDate, List<T> values) {
		this.effectiveDate = effectiveDate;
		this.effectives = values;
	}

}