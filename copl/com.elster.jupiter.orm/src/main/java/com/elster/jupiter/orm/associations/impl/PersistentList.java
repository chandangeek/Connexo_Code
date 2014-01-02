package com.elster.jupiter.orm.associations.impl;

import java.util.AbstractList;
import java.util.List;

import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;

public abstract class PersistentList<T> extends AbstractList<T> {
	
	private List<T> target;
	private final DataMapperImpl<T> dataMapper;
	private final Object owner;
	private final ForeignKeyConstraintImpl constraint;
	
	PersistentList(ForeignKeyConstraintImpl constraint, DataMapperImpl<T> dataMapper, Object owner) {
		this.constraint = constraint;
		this.dataMapper = dataMapper;
		this.owner = owner;
	}

	PersistentList(ForeignKeyConstraintImpl constraint, DataMapperImpl<T> dataMapper, Object owner, List<T> target) {
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
	
	ForeignKeyConstraintImpl getConstraint() {
		return constraint;
	}
	
	DataMapperImpl<T> getDataMapper() {
		return dataMapper;
	}
	
	@Override
    public final T get(int index) {
		return getTarget().get(index);
	}

	@Override
    public final int size() {
		return getTarget().size();
	}
	
	void setTarget(List<T> target) {
		this.target = target;
	}

}
