package com.elster.jupiter.orm.associations.impl;

import java.util.List;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.ForeignKeyConstraint;

public class UnmanagedPersistentList<T> extends PersistentList<T> {
	
	public UnmanagedPersistentList(ForeignKeyConstraint constraint, DataMapper<T> dataMapper, Object owner) {
		super(constraint,dataMapper,owner);
	}
	
	public UnmanagedPersistentList(ForeignKeyConstraint constraint, DataMapper<T> dataMapper, Object owner, List<T> initialValue) {
		super(constraint,dataMapper,owner,initialValue);
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
