/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.associations.impl;

import java.util.AbstractList;
import java.util.List;
import java.util.Optional;

import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.orm.impl.KeyValue;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;

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

	private List<T> loadTarget(){
		QueryExecutor<T> query = dataMapper.query(constraint.reverseEagers());
		Condition condition = Where.where(constraint.getFieldName()).isEqualTo(owner);
		if (constraint.getReverseOrderFieldName() == null) {
			return query.select(condition);
		} else {
			return query.select(condition, Order.ascending(constraint.getReverseOrderFieldName()));
		}
	}

	final List<T> getTarget() {
		if (target == null) {
			target = loadTarget();
			if (dataMapper.getTable().isCached()) {
				for (T object : target) {
					dataMapper.getTable().putToCache(object);
				}
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

	Object getOwner() {
		return owner;
	}
}
