package com.elster.jupiter.orm.associations.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.DomainMapper;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;

public class ManagedPersistentList<T> extends PersistentList<T> {
	
	public ManagedPersistentList(ForeignKeyConstraintImpl constraint, DataMapperImpl<T> dataMapper, Object owner) {
		super(constraint, dataMapper, owner);
	}
	
	public ManagedPersistentList(ForeignKeyConstraintImpl constraint, DataMapperImpl<T> dataMapper, Object owner , List<T> target) {
		super(constraint, dataMapper, owner,target);
	}
	
	@Override
	public T remove(int index) {
		T result = getTarget().remove(index);
		if (result != null) {
			getDataMapper().remove(result);
			updatePositions(index);
		}
		return result;
	}
	
	@Override
	public void add(int index,T element) {
		setPosition(index,element);
		try {
			getDataMapper().getWriter().persist(element);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
		getTarget().add(index,element);
		updatePositions(index);
	}
	
	@Override
	public boolean addAll(Collection<? extends T> collection) {
		if (collection.isEmpty()) {
			return false;
		}
		List<T> toAdd = new ArrayList<>(collection);
		int index = getTarget().size();
		for (T value : toAdd) {
			setPosition(index++,value);
		}
		try {
			getDataMapper().getWriter().persist(toAdd);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
		return getTarget().addAll(toAdd);
	}
	
	@Override
	public boolean removeAll(Collection<?> collection) {
		if (collection.isEmpty() || isEmpty()) {
			return false;
		}
		List<T> removedList = new ArrayList<>();
		for (Object toRemove : collection) {
			if (getTarget().remove(toRemove)) {
				removedList.add(getDataMapper().cast(toRemove));
			}
		}
		getDataMapper().remove(removedList);
		updatePositions(0);
		return !removedList.isEmpty();
	}
	
	public void clear() {
		getDataMapper().remove(getTarget());
		getTarget().clear();
	}
	
	private void updatePositions(int startIndex) {
		if (!getConstraint().isAutoIndex()) {
			return;
		}
		List <T> toUpdate = new ArrayList<>();
		for (int i = startIndex ; i < getTarget().size() ; i++) {
			T value = getTarget().get(i);
			if (setPosition(i + 1 ,value)) {
				toUpdate.add(value);
			}
		}
		getDataMapper().update(toUpdate, "position");
	}
	
	
	private boolean setPosition(int position, T value) {
		if (!getConstraint().isAutoIndex()) {
			return false;
		}
		DomainMapper mapper = getDataMapper().getTable().getDomainMapper();
		int oldPosition = (Integer) mapper.get(value, "position");
		if (oldPosition == position) {
			return false;
		} else {
			mapper.set(value, "position", position);
			return true;
		}
	}
	
	public void reorder(List<T> newOrder) {
		setTarget(new ArrayList<>(newOrder));
		updatePositions(0);
	}
	
}
