package com.elster.jupiter.orm.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.sql.SqlBuilder;

abstract public class JoinDataMapper<T> {
	private final DataMapperImpl<T,? extends T> dataMapper;
	private final String alias;
	private Map<Object,T> cache;
	
	JoinDataMapper(DataMapperImpl<T,? extends T> dataMapper, String alias) {
		this.dataMapper = dataMapper;
		this.alias = alias;
	}

	final DataMapperImpl<T, ? extends T> getMapper() {
		return dataMapper;
	}
	
	final String getAlias() {
		return alias;
	}
	
	final Table getTable() {
		return getMapper().getTable();
	}
	
	
	final <R> JoinDataMapper<R> wrap(DataMapperImpl<R,? extends R> newMapper , int index) {
		for (TableConstraint constraint : getTable().getConstraints()) {
			if (newMapper.getTable().equals(constraint.getReferencedTable())) {
				char aliasSuffix = (char) ('a' + index);
				return new ParentDataMapper<R>(newMapper , constraint , getAlias() + aliasSuffix);
			}
		}
		for (TableConstraint constraint : newMapper.getTable().getConstraints()) {		
			if (getTable().equals(constraint.getReferencedTable())) {
				char aliasSuffix = (char) ('z' - index);
				return new ChildDataMapper<R>(newMapper , constraint , getAlias() + aliasSuffix);				
			}
		}
		return null;
	}
	
	final ColumnAndAlias getColumnAndAlias(String fieldName) {
		Column column = getTable().getColumnForField(fieldName);
		return column == null ? null : new ColumnAndAlias(column,getAlias());	
	}
	
	final boolean hasField(String fieldName)  {
		return getTable().getColumnForField(fieldName) != null;
	}
	
	
	final DataMapperImpl<T,? extends T> getDataMapperForField(String fieldName) {
		Column column = getTable().getColumnForField(fieldName);
		if (column != null) {
			return getMapper();
		}
		return null;
	}
	
	
	final String appendColumns(SqlBuilder builder , String separator) {
		for (Column each : getTable().getColumns()) {
			builder.append(separator);
			builder.append(each.getName(alias));
			builder.space();
			String uniqueName = alias + each.getName();
			if (uniqueName.length() > 30) {
				uniqueName = uniqueName.substring(0, 30);
			}
			builder.append(uniqueName);			
			separator = ", ";		
		}
		return separator;
	}
	
	final void appendTable(SqlBuilder builder) {
		getMapper().getSqlGenerator().appendTable(builder.getBuffer(), "", getAlias());
	}
	

	final T put(Object key , T value) {
		return cache.put(key, value);
	}
	
	final T get(Object key) {
		return cache.get(key);
	}

	// overrides start here
	
	String reduce(String fieldName) {
		String constraintField = getName();
		if (constraintField == null || !fieldName.startsWith(constraintField + ".")) {
			return null;
		}
		return fieldName.substring(constraintField.length() + 1);		
	}
	void clearCache() {
		this.cache = new HashMap<>();
	}

	void completeFind() {
		// do nothing by default
	}
	
	boolean isChild() {
		return false;
	}
	
	abstract String getName();
	abstract T set(Object value , ResultSet rs , int index) throws SQLException;
	abstract boolean canRestrict();
	abstract boolean appendFromClause(SqlBuilder builder , String parentAlias , boolean isMarked , boolean forceOuterJoin);
}
