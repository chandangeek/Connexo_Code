package com.elster.jupiter.orm.associations.impl;

import java.util.AbstractList;
import java.util.List;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.ForeignKeyConstraint;

public class PersistentList<T> extends AbstractList<T> {
	
	private List<T> target;
	final private DataMapper<T> dataMapper;
	final private Object owner;
	final private ForeignKeyConstraint constraint;
	
	public PersistentList(ForeignKeyConstraint constraint, DataMapper<T> dataMapper, Object owner) {
		this.constraint = constraint;
		this.dataMapper = dataMapper;
		this.owner = owner;
	}

	private  List<T> getTarget() {
		if (target == null) {
			if (constraint.getReverseOrderFieldName() == null) {
				target = dataMapper.find(constraint.getFieldName(),owner);
			} else 
				target = dataMapper.find(constraint.getFieldName(),owner,constraint.getReverseOrderFieldName());			
		}
		return target;
	}
	
	@Override
	public T get(int index) {
		return getTarget().get(index);
	}

	@Override
	public int size() {
		return getTarget().size();
	}

	@Override
	public T set(int index,T element) {
		return getTarget().set(index, element);
	}
	
	@Override
	public T remove(int index) {
		return getTarget().remove(index);
	}
	
	@Override
	public void add(int index,T element) {
		getTarget().add(index,element);
	}
	
}
