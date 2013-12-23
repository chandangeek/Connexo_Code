package com.elster.jupiter.orm.associations.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.ForeignKeyConstraint;

import java.util.AbstractList;
import java.util.List;

public abstract class PersistentList<T> extends AbstractList<T> {
	
	private List<T> target;
	private final DataMapper<T> dataMapper;
	private final Object owner;
	private final ForeignKeyConstraint constraint;
	
	PersistentList(ForeignKeyConstraint constraint, DataMapper<T> dataMapper, Object owner) {
		this.constraint = constraint;
		this.dataMapper = dataMapper;
		this.owner = owner;
	}

	PersistentList(ForeignKeyConstraint constraint, DataMapper<T> dataMapper, Object owner, List<T> target) {
		this(constraint,dataMapper,owner);
		this.target = target;
	}
	
	final List<T> getTarget() {
		if (target == null) {
			if (constraint.getReverseOrderFieldName() == null) {
				target = dataMapper.find(constraint.getFieldName(),owner);
			} else {
				target = dataMapper.find(constraint.getFieldName(),owner,constraint.getReverseOrderFieldName());
            }
		}
		return target;
	}
	
	@Override
    public final T get(int index) {
		return getTarget().get(index);
	}

	@Override
    public final int size() {
		return getTarget().size();
	}

	@Override
    public final T set(int index,T element) {
		return getTarget().set(index, element);
	}
	
}
