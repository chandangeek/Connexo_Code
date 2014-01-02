package com.elster.jupiter.orm.associations.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.orm.associations.TemporalReference;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.orm.impl.KeyValue;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;

public class PersistentTemporalReference<T extends Effectivity> extends AbstractPersistentTemporalAspect<T> implements TemporalReference<T> {
	
	private T cachedValue;
	private UtcInstant cachedDate;

	public PersistentTemporalReference(ForeignKeyConstraintImpl constraint,DataMapperImpl<T> dataMapper, Object owner) {
		super(constraint, dataMapper, owner);
	}

	@Override
	public boolean add(T element) {
		if (this.effective(element.getInterval()).isEmpty()) {
			setPresent(element);
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
		if (cachedValue != null && getPrimaryKey(element).equals(getPrimaryKey(cachedValue))) {
			setAbsent(element.getInterval().getStart());
		}
		return super.remove(element);
	}

	@Override
	public Optional<T> effective(Date when) {
		if (cachedValue != null && cachedValue.getInterval().isEffective(when)) {
			return Optional.of(cachedValue);
		}
		if (cachedDate != null && cachedDate.toDate().equals(when)) {
			return Optional.absent();
		}
		List<T> candidates = allEffective(when);
		if (candidates.size() > 1) {
			throw new IllegalStateException("More than one effective");
		}
		if (candidates.isEmpty()) {
			setAbsent(when);
			return Optional.absent();
		} else {
			setPresent(candidates.get(0));
			return Optional.of(candidates.get(0));
		}
	}
	
	public void setPresent(T value) {
		this.cachedDate = null;
		this.cachedValue = Objects.requireNonNull(value);
	}
	
	private void setAbsent(Date effectiveDate) {
		this.cachedDate = new UtcInstant(Objects.requireNonNull(effectiveDate));
		this.cachedValue = null;
	}

}
