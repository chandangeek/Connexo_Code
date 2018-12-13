/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.associations.impl;

import java.util.List;
import java.util.Optional;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.associations.Reference;

public class ReversePersistentReference<T> implements Reference<T> {
	private Optional<T> target;
	private final DataMapper<T> dataMapper;
	private final Object owner;
	private final ForeignKeyConstraint constraint;
	
	ReversePersistentReference(ForeignKeyConstraint constraint, DataMapper<T> dataMapper, Object owner) {
		this.constraint = constraint;
		this.dataMapper = dataMapper;
		this.owner = owner;
	}

	ReversePersistentReference(ForeignKeyConstraint constraint, DataMapper<T> dataMapper, Object owner, T target) {
		this(constraint,dataMapper,owner);
		this.target = Optional.ofNullable(target);
	}
	
	final Optional<T> getTarget() {
		if (target == null) {
			List<T> candidates = dataMapper.find(constraint.getFieldName(),owner);
			if (candidates.size() > 1) {
				throw new IllegalStateException("More than one reference");
			}
			target = candidates.isEmpty() ? Optional.empty() : Optional.of(candidates.get(0));
		}
		return target;
	}

	@Override
	public Optional<T> getOptional() {
		return getTarget();
	}

	@Override
	public void set(T value) {
		if (value == null) {
			setNull();
			return;
		}
		if (getTarget().isPresent()) {
			throw new UnsupportedOperationException("Reverse references can not be changed");
		}
		target = Optional.of(value);
		if (constraint.isComposition()) {
			dataMapper.persist(value);
		}
	}

	@Override
	public void setNull() {
		if (constraint.isComposition() && getTarget().isPresent()) {
			dataMapper.remove(getTarget().get());
		}
		target = Optional.empty();
	}
}
