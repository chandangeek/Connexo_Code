package com.elster.jupiter.orm.associations.impl;

import java.util.Date;
import java.util.List;

import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.orm.associations.TemporalReference;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.orm.impl.KeyValue;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

public class PersistentTemporalReference<T extends Effectivity> extends AbstractPersistentTemporalAspect<T> implements TemporalReference<T> {
	
	private Date effectiveDate;
	private T value;

	public PersistentTemporalReference(ForeignKeyConstraintImpl constraint,DataMapperImpl<T> dataMapper, Object owner) {
		super(constraint, dataMapper, owner);
	}

	@Override
	public boolean add(T element) {
		if (this.effective(element.getInterval()).isEmpty()) {
			this.effectiveDate = element.getInterval().getStart();
			this.value = element;
			return super.add(element);
		} else {
			throw new IllegalArgumentException("" + element);
		}
	}

	private KeyValue getPrimaryKey(T object) {
		return getDataMapper().getTable().getPrimaryKey(object);
	}
	
	@Override
	public boolean remove(T element) {
		if (effectiveDate != null && value != null && getPrimaryKey(element).equals(getPrimaryKey(value))) {
			effectiveDate = null;
			value = null;
		}
		return super.remove(element);
	}

	@Override
	public Optional<T> effective(Date when) {
		if (effectiveDate != null) {
			if (value == null) {
				if (effectiveDate.equals(when)) {
					return Optional.absent();
				}
			} else if (value.getInterval().contains(when, Interval.EndpointBehavior.CLOSED_OPEN)) {
				return Optional.of(value);
			}
		}
		List<T> candidates = allEffective(when);
		if (candidates.size() > 1) {
			throw new IllegalStateException("More than one effective");
		}
		effectiveDate = new Date(when.getTime());
		value = candidates.isEmpty() ? null : candidates.get(0); 
		return Optional.fromNullable(value);
	}

}
