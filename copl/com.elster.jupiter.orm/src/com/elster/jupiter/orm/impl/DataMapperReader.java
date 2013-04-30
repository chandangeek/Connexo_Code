package com.elster.jupiter.orm.impl;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.PersistenceAware;
import com.elster.jupiter.orm.PersistenceException;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.sql.util.SqlBuilder;
import com.elster.jupiter.sql.util.SqlFragment;
import static com.elster.jupiter.orm.plumbing.Bus.getConnection;

public class DataMapperReader<T,S extends T> {
	private final TableSqlGenerator sqlGenerator;
	private final String alias;
	private final Constructor<S> constructor;
	
	DataMapperReader(DataMapperImpl<T,S> dataMapper, Constructor<S> constructor) {
		this.sqlGenerator = dataMapper.getSqlGenerator();	
		this.alias = dataMapper.getAlias();
		this.constructor = constructor;
	}
	
	private Table getTable() {
		return sqlGenerator.getTable();
	}
	
	private TableSqlGenerator getSqlGenerator() {
		return sqlGenerator;
	}
	
	private List<SqlFragment> getPrimaryKeyFragments(Object[] values) {
		Column[] pkColumns = getPrimaryKeyColumns();
		if (pkColumns.length != values.length) {
			throw new IllegalArgumentException("Argument array length does not match Primary Key Field count of " + pkColumns.length);
		}
		List<SqlFragment> fragments = new ArrayList<>(pkColumns.length);
		for (int i = 0 ; i < values.length ; i++) {
			fragments.add(new ColumnEqualsFragment(pkColumns[i] , values[i] , alias));
		}
		return fragments;		
	}
	
	List<T> findByPrimaryKey (Object[] values) throws SQLException {
		return find(getPrimaryKeyFragments(values),null,false);		
	}
	
	int getPrimaryKeyLength() {
		return getPrimaryKeyColumns().length;
	}
	
	public T lock(Object... values)  throws SQLException {
		List<T> candidates = find(getPrimaryKeyFragments(values) , null , true);
		return candidates.isEmpty() ? null : candidates.get(0);
	}

	public List<T> find(String[] fieldNames , Object[] values , String... orderColumns) throws SQLException {
		List<SqlFragment> fragments = new ArrayList<>();
		if (fieldNames != null) {
			for (int i = 0 ; i < fieldNames.length ; i++) {
				addFragments(fragments,fieldNames[i], values[i]);
			}
		}
		return find(fragments, orderColumns, false);		
	}
			
	private List<T> find(List<SqlFragment> fragments, String[] orderColumns,boolean lock) throws SQLException {
		List<Setter> setters = new ArrayList<>();
		for (SqlFragment each : fragments) {
			if (each instanceof Setter) {
				setters.add((Setter) each);
			}
		}
		List<T> result = new ArrayList<>();	
		SqlBuilder builder = selectSql(fragments, orderColumns,lock);
		try (Connection connection = getConnection(false)) {
			try(PreparedStatement statement = builder.prepare(connection)) {
				try (ResultSet resultSet = statement.executeQuery()) {
					while(resultSet.next()) {
						result.add(construct(resultSet,setters));
					}
				}				
			} 
		} 
		return result;			
	}
	
	private SqlBuilder selectSql(List<SqlFragment> fragments, String[] orderColumns , boolean lock) {
		SqlBuilder builder = new SqlBuilder(getSqlGenerator().getSelectFromClause(alias));
		if (fragments.size() > 0) {
			builder.append(" where ");
			String separator = "";
			for (SqlFragment each : fragments) {
				builder.append(separator);
				builder.add(each);
				separator = " AND ";
			}
		}
		if (orderColumns != null && orderColumns.length > 0) {
			builder.append(" order by ");
			String separator = "";
			for (String each : orderColumns) {
				builder.append(separator);
				Column column = getColumnForField(each);
				builder.append(column == null ? each : column.getName(alias));
				separator = ", ";
			}
		}
		if (lock) {
			builder.append(" for update ");
		}
		return builder;
	}
	
	
	private T newInstance() {
		try {			
			return constructor.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new PersistenceException(e);
		}
	}
	
	T construct(ResultSet rs, int startIndex) throws SQLException {		
		T result = newInstance();				
		for (Column column : getSqlGenerator().getColumns()) {						
			DomainMapper.FIELD.set(result, column.getFieldName() , ((ColumnImpl) column).convertFromDb(rs, startIndex++));	
		}					
		return result;
	}
	
	T construct(ResultSet rs, List<Setter> setters) throws SQLException {
		T result = construct(rs,1);
		for (Setter setter : setters) {
			setter.set(result);
		}
		if (result instanceof PersistenceAware) {
			((PersistenceAware) result).postLoad();
		}
		return result;
	}
	
	private ColumnImpl[] getColumns() {
		return getSqlGenerator().getColumns();
	}
	
	private Column[] getPrimaryKeyColumns() {
		return getSqlGenerator().getPrimaryKeyColumns();
	}
	
	Column getColumnForField(String fieldName) {
		return getTable().getColumnForField(fieldName);
	}
	
	private int getIndex(Column column) {
		for (int i = 0 ; i < getColumns().length ; i++) {
			if (column.equals(getColumns()[i])) { 
				return i;
			}
		}
		throw new IllegalArgumentException();
	}
	
	private Object getValue(Column column , ResultSet rs , int startIndex ) throws SQLException {
		int offset = getIndex(column);
		return ((ColumnImpl) column).convertFromDb(rs, startIndex + offset);
	}
	
	Object getPrimaryKey(ResultSet rs , int index) throws SQLException {
		Column[] primaryKeyColumns = getPrimaryKeyColumns();		
		if (primaryKeyColumns.length == 0) {
			return null;
		}
		if (primaryKeyColumns.length == 1) {		
			Object result = getValue(primaryKeyColumns[0],rs,index);
			return rs.wasNull() ? null : result;
		}
		Object[] values = new Object[primaryKeyColumns.length];
		for (int i = 0 ; i < primaryKeyColumns.length ; i++) {
			values[i] = getValue(primaryKeyColumns[i],rs,index);
			if (rs.wasNull()) {
				return null;
			}
		}
		return new CompositePrimaryKey(values);
	}
	
	TableConstraint getForeignKeyConstraintFor(String name) {
		for (TableConstraint each : getTable().getForeignKeyConstraints()) {
			if (each.getFieldName().equals(name))
				return each;
		}
		return null;
	}
	
	void addFragments(List<SqlFragment> fragments, String fieldName , Object value) {
		FieldMapping mapping = ((TableImpl) getTable()).getFieldMapping(fieldName);
		if (mapping == null) {
			throw new IllegalArgumentException("Invalid field " + fieldName);
		} else {
			fragments.add(mapping.asEqualFragment(value, alias));
		}
	}
	
}

	

