package com.elster.jupiter.orm.associations.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.ForeignKeyConstraint;

public class UnManagedPersistentList<T> extends PersistentList<T> {
	
	public UnManagedPersistentList(ForeignKeyConstraint constraint, DataMapper<T> dataMapper, Object owner) {
		super(constraint,dataMapper,owner);
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
