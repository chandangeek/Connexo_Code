/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.associations.impl;

import java.util.List;

import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;

public class UnmanagedPersistentList<T> extends PersistentList<T> {
	
	public UnmanagedPersistentList(ForeignKeyConstraintImpl constraint, DataMapperImpl<T> dataMapper, Object owner) {
		super(constraint,dataMapper,owner);
	}
	
	public UnmanagedPersistentList(ForeignKeyConstraintImpl constraint, DataMapperImpl<T> dataMapper, Object owner, List<T> initialValue) {
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
	
	@Override
	public T set(int index,T element) {
		return getTarget().set(index,element);
	}
	 
}
