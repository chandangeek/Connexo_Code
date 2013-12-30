package com.elster.jupiter.orm.associations.impl;

import java.util.Date;
import java.util.List;

import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.orm.associations.TemporalList;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.ImmutableList;

public class PersistentTemporalList<T extends Effectivity> extends AbstractPersistentTemporalAspect<T> implements TemporalList<T> {
	private Date effectiveDate;
	private List<T> effectives;

	public PersistentTemporalList(ForeignKeyConstraintImpl constraint,DataMapperImpl<T> dataMapper, Object owner) {
		super(constraint, dataMapper, owner);
	}

	@Override
	public boolean add(T element) {
		if (effectiveDate != null && element.getInterval().contains(effectiveDate,Interval.EndpointBehavior.CLOSED_OPEN)) {
			effectives.add(element);
		}
		return super.add(element);
	}

	@Override
	public boolean remove(T element) {
		if (element.getInterval().contains(effectiveDate,Interval.EndpointBehavior.CLOSED_OPEN)) {
			effectives.remove(element);
		}
		return super.remove(element);
	}

	@Override
	public List<T> effective(Date when) {
		if (effectiveDate == null || !effectiveDate.equals(when)) {
			effectiveDate = new Date(when.getTime());
			effectives = allEffective(when);
		}
		return ImmutableList.copyOf(effectives);
	}
	

}
