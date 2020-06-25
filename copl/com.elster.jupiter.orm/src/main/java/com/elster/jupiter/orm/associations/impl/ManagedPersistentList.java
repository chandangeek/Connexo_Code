/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.associations.impl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.DataMapperWriter;
import com.elster.jupiter.orm.impl.DomainMapper;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.orm.fields.impl.ConstraintEqualFragment;
import com.elster.jupiter.orm.impl.TableImpl;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.base.Strings;

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


	private Object getFieldValie(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
		Field listField = object.getClass().getDeclaredField(fieldName);
		listField.setAccessible(true);
		return listField.get(object);
	}

	@Override
	public void add(int index,T element) {
		System.out.println("MANAGED PERSISTENT LIST ADD" + element);
		Object own = getOwner();
		System.out.println("OWNER = "+own);
		setPosition(index + 1,element);
		try {
			getWriter().persist(element);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
		getTarget().add(index,element);

		ForeignKeyConstraintImpl cnstr = getConstraint();
		TableImpl tb = cnstr.getTable();//Table Of element
		TableImpl rtb = cnstr.getReferencedTable();// Table of owner

		if (rtb.isCached()){
			System.out.println("GET NAME ="+rtb.getName());
			Optional<?> fromCache = rtb.findInCache(rtb.getPrimaryKey(own));
			System.out.println("FROM CACHE = "+fromCache);
			if (fromCache.isPresent()){
				try {
					Object tmp = fromCache.get();

					System.out.println("ReverseFieldName = "+cnstr.getReverseFieldName());
					Field field = tmp.getClass().getDeclaredField(cnstr.getReverseFieldName());
					field.setAccessible(true);
					System.out.println("Obtained field = "+field);
					Object value = field.get(tmp);
					System.out.println("VALIE = "+value);
					field.set(tmp, getTarget());


					/*Optional<?> fromCacheAfter = rtb.findInCache(rtb.getPrimaryKey(own));
					System.out.println("Object after set target = "+fromCacheAfter.get());
					System.out.println("ReverseFieldName = "+cnstr.getReverseFieldName());
					Field fieldAfter = fromCacheAfter.get().getClass().getDeclaredField(cnstr.getReverseFieldName());
					fieldAfter.setAccessible(true);
					System.out.println("Obtained field = "+fieldAfter);
					Object valueAfter = fieldAfter.get(fromCacheAfter.get());
					System.out.println("VALIE = "+valueAfter);*/

				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		//Now go up by tree.
		//String reverseFieldName = cnstr.getReverseFieldName();
		//Check if current object is in list in another object
			//Now get parent object


			TableImpl parentObjjectTable = cnstr.getReferencedTable();
			List<ForeignKeyConstraintImpl> parentTableCnstrntList = parentObjjectTable.getReferenceConstraints();
			for (ForeignKeyConstraintImpl frkcstrnt : parentTableCnstrntList){
				String reverseFieldName = frkcstrnt.getReverseFieldName();
				if (Strings.isNullOrEmpty(reverseFieldName)) {//Check that parent list is in list in another object
					String parentObjFiledName = cnstr.getFieldName();

					try {
						Object parentObject = getFieldValie(own, parentObjFiledName);
						//Now get list in which our object is present
						List lst = (List)getFieldValie(parentObject, reverseFieldName);
						ForeignKeyConstraintImpl constr = (ForeignKeyConstraintImpl)getFieldValie(lst, "constraint");
						TableImpl ownerTable = constr.getTable();
						if (ownerTable.isCached()){
							Optional parentObjectFromCache = ownerTable.findInCache(ownerTable.getPrimaryKey(parentObject));
							if (parentObjectFromCache.isPresent()){
								Object parentFromCache = parentObjectFromCache.get();
								List list = (List)getFieldValie(parentFromCache, reverseFieldName); //Obtain the list in which our object is.
								long neededId = (Long)getFieldValie(parentObject, "id");
								int neededIndex = 0;
								for (Object ob : list){
									long id = (Long)getFieldValie(ob, "id");
									if (id == neededId){
										neededIndex = list.indexOf(ob);
										break;
									}
								}
								if (neededIndex != 0) {
									list.set(neededIndex, parentObject);
								}
							}
						}

					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}


				}


			}




		updatePositions(index + 1);
	}


	private void updateOwner(TableImpl childTable, Object chindObject){
		List<ForeignKeyConstraintImpl> childTableCnstrntList = childTable.getReferenceConstraints();
		for (ForeignKeyConstraintImpl frkcstrnt : childTableCnstrntList){
			String reverseFieldName = frkcstrnt.getReverseFieldName();
			if (Strings.isNullOrEmpty(reverseFieldName)) {//Check that parent list is in list in another object
				String parentObjFiledName = frkcstrnt.getFieldName();

				try {
					Object parentObject = getFieldValie(chindObject, parentObjFiledName);
					//Now get list in which our object is present
					List lst = (List)getFieldValie(parentObject, reverseFieldName);
					ForeignKeyConstraintImpl constr = (ForeignKeyConstraintImpl)getFieldValie(lst, "constraint");
					TableImpl ownerTable = constr.getTable();
					if (ownerTable.isCached()){
						Optional parentObjectFromCache = ownerTable.findInCache(ownerTable.getPrimaryKey(parentObject));
						if (parentObjectFromCache.isPresent()){
							Object parentFromCache = parentObjectFromCache.get();
							List list = (List)getFieldValie(parentFromCache, reverseFieldName); //Obtain the list in which our object is.
							long neededId = (Long)getFieldValie(parentObject, "id");
							int neededIndex = 0;
							for (Object ob : list){
								long id = (Long)getFieldValie(ob, "id");
								if (id == neededId){
									neededIndex = list.indexOf(ob);
									break;
								}
							}
							if (neededIndex != 0) {
								list.set(neededIndex, parentObject);
							}
						}

						updateOwner(ownerTable,parentObjectFromCache.get());
					}

				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}


			}


		}
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
