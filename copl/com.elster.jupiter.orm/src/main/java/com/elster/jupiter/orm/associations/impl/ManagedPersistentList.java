package com.elster.jupiter.orm.associations.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.impl.DataMapperImpl;

public class ManagedPersistentList<T> extends PersistentList<T> {
	
	final private DataMapperImpl<T> dataMapper;
	
	public ManagedPersistentList(ForeignKeyConstraint constraint, DataMapperImpl<T> dataMapper, Object owner) {
		super(constraint, dataMapper, owner);
		this.dataMapper = dataMapper;
	}
	
	public ManagedPersistentList(ForeignKeyConstraint constraint, DataMapperImpl<T> dataMapper, Object owner , List<T> target) {
		super(constraint, dataMapper, owner,target);
		this.dataMapper = dataMapper;
	}
	
	@Override
	public T remove(int index) {
		T result = getTarget().remove(index);
		if (result != null) {
			dataMapper.remove(result);
		}
		return result;
	}
	
	@Override
	public void add(int index,T element) {
		try {
			dataMapper.getWriter().persist(element);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
		getTarget().add(index,element);
	}
	
	@Override
	public boolean addAll(Collection<? extends T> collection) {
		if (collection.isEmpty()) {
			return false;
		}
		try {
			dataMapper.getWriter().persist(new ArrayList<>(collection));
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
		return getTarget().addAll(collection);
	}
	
	@Override
	public boolean removeAll(Collection<?> collection) {
		if (collection.isEmpty() || isEmpty()) {
			return false;
		}
		List<T> removedList = new ArrayList<>();
		for (Object toRemove : collection) {
			if (getTarget().remove(toRemove)) {
				removedList.add(dataMapper.cast(toRemove));
			}
		}
		dataMapper.remove(removedList);
		return !removedList.isEmpty();
	}
	
	public void clear() {
		dataMapper.remove(getTarget());
		getTarget().clear();
	}
	
}
