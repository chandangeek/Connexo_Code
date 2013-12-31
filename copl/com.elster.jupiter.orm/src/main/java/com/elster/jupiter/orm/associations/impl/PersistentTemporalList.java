package com.elster.jupiter.orm.associations.impl;

import java.util.Date;
import java.util.List;

import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.orm.associations.TemporalList;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.collect.ImmutableList;

public class PersistentTemporalList<T extends Effectivity> extends AbstractPersistentTemporalAspect<T> implements TemporalList<T> {
	private UtcInstant effectiveDate;
	private List<T> effectives;

	public PersistentTemporalList(ForeignKeyConstraintImpl constraint,DataMapperImpl<T> dataMapper, Object owner) {
		super(constraint, dataMapper, owner);
	}

	@Override
	public boolean add(T element) {
		if (effectiveDate != null && element.getInterval().isEffective(effectiveDate.toDate())) {
			effectives.add(element);
		}
		return super.add(element);
	}

	@Override
	public boolean remove(T element) {
		if (element.getInterval().isEffective(effectiveDate.toDate())) {
			effectives.remove(element);
		}
		return super.remove(element);
	}

	@Override
	public List<T> effective(Date when) {
		if (effectiveDate == null || !effectiveDate.toDate().equals(when)) {
			setCache(when,allEffective(when));
		}
		return ImmutableList.copyOf(effectives);
	}
	
	public void setCache(Date effectiveDate, List<T> values) {
		this.effectiveDate = new UtcInstant(effectiveDate);
		this.effectives = values;
	}
	

}
