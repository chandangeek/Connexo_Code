/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.associations.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.DataMapperWriter;
import com.elster.jupiter.orm.impl.DomainMapper;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.orm.fields.impl.ConstraintEqualFragment;
import com.elster.jupiter.util.sql.SqlBuilder;

public class ManagedPersistentList<T> extends PersistentList<T> {
	
	public ManagedPersistentList(ForeignKeyConstraintImpl constraint, DataMapperImpl<T> dataMapper, Object owner) {
		super(constraint, dataMapper, owner);
	}
	
	public ManagedPersistentList(ForeignKeyConstraintImpl constraint, DataMapperImpl<T> dataMapper, Object owner , List<T> target) {
		super(constraint, dataMapper, owner,target);
	}
	
	private DataMapperWriter<T> getWriter() {
		return getDataMapper().getWriter();
	}
	
	@Override
	public T remove(int index) {
		try {
			T result = getTarget().remove(index);
			if (result != null) {
				getWriter().remove(result);
				updatePositions(index);
			}
			return result;
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
	
	@Override
	public void add(int index,T element) {
		setPosition(index + 1,element);
		try {
			getWriter().persist(element);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
		getTarget().add(index,element);
		updatePositions(index + 1);
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
			getWriter().persist(toAdd);
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
		List<T> toRemove = new ArrayList<>(getTarget());
		toRemove.retainAll(collection);
        getTarget().removeAll(toRemove);
        try {
            getWriter().remove(toRemove);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
		updatePositions(0);
		return !toRemove.isEmpty();
	}
	
	public void clear() {
		try {
			getWriter().remove(getTarget());
		} catch (SQLException e) {
			throw new UnderlyingSQLFailedException(e);
		}
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
		if (!toUpdate.isEmpty()) {
			getDataMapper().update(toUpdate, "position");
		}
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
		// set index to negative value to avoid database constrait violations
		for (int i = 0 ; i < newOrder.size(); i++) {
			setPosition(-i-1, newOrder.get(i));
		}
		getDataMapper().update(newOrder,"position");
		try (Connection connection = getDataMapper().getTable().getDataModel().getConnection(true)) {
			try (PreparedStatement preparedStatement = swapSignSql().prepare(connection)) {
				preparedStatement.executeUpdate();
			}
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
		setTarget(new ArrayList<>(newOrder));
	}
	
	SqlBuilder swapSignSql() {
		ColumnImpl column = getConstraint().getTable().getColumn("position").get();
		SqlBuilder builder = new SqlBuilder("update ");
		builder.append(getConstraint().getTable().getQualifiedName());
		builder.append (" set ");
		builder.append(column.getName());
		builder.append(" = -");
		builder.append(column.getName());
		builder.append(" where ");
		builder.add(new ConstraintEqualFragment(getConstraint(),getOwner(),""));
		return builder;
	}
}
