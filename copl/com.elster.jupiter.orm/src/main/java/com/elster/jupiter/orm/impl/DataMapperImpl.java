package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.PersistenceException;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.query.impl.QueryExecutorImpl;
import com.google.common.base.Optional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DataMapperImpl<T> extends AbstractFinder<T> implements DataMapper<T> {
	
	private final TableSqlGenerator sqlGenerator;
	private final DomainMapper mapper;
	private final Collection<Class<? extends T>> implementations;
	private final String alias;
	private final DataMapperReader<T> reader;
	private final DataMapperWriter<T> writer;
	
	DataMapperImpl(Class<T> api, Class<? extends T> implementation ,  Table table) {
		this.mapper = DomainMapper.FIELDSTRICT;
		this.sqlGenerator = new TableSqlGenerator((TableImpl) table);
		this.alias = createAlias(api.getName());
		this.implementations = new ArrayList<>(1);
		this.implementations.add(implementation);
		this.reader = new DataMapperReader<>(this,implementation);
		this.writer = new DataMapperWriter<>(this);
	}	
	
	DataMapperImpl(Class<T> api , Map<String,Class<? extends T>> implementations , Table table) {
		this.mapper = DomainMapper.FIELDLENIENT;
		this.sqlGenerator = new TableSqlGenerator((TableImpl) table);
		this.alias = createAlias(api.getName());
		this.implementations = implementations.values(); 
		this.reader = new DataMapperReader<>(this, implementations);
		this.writer = new DataMapperWriter<>(this, implementations);
	}
	
	private String createAlias(String apiName) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0 ; i < apiName.length() ; i++) {
			char next = apiName.charAt(i);
			if (Character.isUpperCase(next)) {
				builder.append(next);
			}
		}
        if (builder.length() == 0) {
            builder.append('a');
        }
        builder.insert(0, '\"');
        builder.append('\"');
		return builder.toString().toLowerCase();
	}
	
	public String getAlias() {
		return alias;
	}
	
	@Override 
	public Table getTable() {
		return sqlGenerator.getTable();
	}
	
	public TableSqlGenerator getSqlGenerator() {
		return sqlGenerator;
	}
	

	@Override
    Optional<T> findByPrimaryKey(Object[] values) {
		try {
			return reader.findByPrimaryKey(values);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	
	@Override
	int getPrimaryKeyLength() {
		return getTable().getPrimaryKeyColumns().size();
	}
	
	@Override
	public T lock(Object... values)  {
		try {
			return reader.lock(values);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	
	@Override
	public List<T> find(String[] fieldNames, Object[] values, String... orderColumns) {
		try {
			return reader.find(fieldNames,values,orderColumns);
		} catch(SQLException ex) {
			throw new PersistenceException(ex);
		}
	}

    public T construct(ResultSet rs, int startIndex) throws SQLException {
		return reader.construct(rs,startIndex);
	}
	
	
	@Override
	public void persist(T object)  {
		try {
			writer.persist(object);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
		
	@Override
	// note that this will not fill back auto increment columns.
	public void persist(List<T> objects)  {
		try {
			writer.persist(objects);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	
	@Override
	public void update(T object)  {
		update(object,sqlGenerator.getTable().getStandardColumns());		
	}
	
	@Override
	public void update(T object , String... fieldNames)  {
		update(object,getUpdateColumns(fieldNames));
	}
	
	private Column[] getUpdateColumns(String[] fieldNames) {
		Column[] columns = new Column[fieldNames.length];
		int i = 0;
		for (String fieldName : fieldNames) {
			Column column = getTable().getColumnForField(fieldName);
			if (column.isPrimaryKeyColumn() || column.isVersion() || column.hasUpdateValue() || column.isDiscriminator()) {
				throw new IllegalArgumentException("Cannot update special column");
			} else {
				columns[i++] = column;
			}
		}
		return columns;
	}
	
	private void update(T object,Column[] columns) {
		try {
			writer.update(object,columns);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	
	
	@Override
	public void update(List<T> objects)  {
		update(objects,sqlGenerator.getTable().getStandardColumns());
	}
	
	@Override
	public void update(List<T> objects , String... fieldNames)  {
		update(objects,getUpdateColumns(fieldNames));
	}
	
	private void update(List<T> objects,Column[] columns){
		try {
			writer.update(objects,columns);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		} 	
	}
	
	@Override
	public void remove(T object )  {
		try {
			writer.remove(object);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	
	@Override
	public void remove(List<T> objects )  {
		try {
			writer.remove(objects);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	
	@Override
	public QueryExecutor<T> with(DataMapper<?>... dataMappers) {
		QueryExecutorImpl <T> result = new QueryExecutorImpl<>(this);
		for (DataMapper<?> each : dataMappers) {
			result.add(each);
		}
		return result;
	}
	
	public Object convert(Column column , String value) {
		if (column.isEnum()) {
			return getEnum(column,value);
		} else {
			return ((ColumnImpl) column).convert(value);
		}
	}
	
	private Object getEnum(Column column, String value) {
		for (Class<? extends T> implementation : implementations) {
			Object result = mapper.getEnum(implementation, column.getFieldName(),value);
			if (result != null) {
				return result;
			}
		}
		return null;			
	}
	
	private ColumnImpl[] getColumns() {
		return getSqlGenerator().getColumns();
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
	
	public Object getPrimaryKey(ResultSet rs , int index) throws SQLException {
		Column[] primaryKeyColumns = getSqlGenerator().getPrimaryKeyColumns();
		switch (primaryKeyColumns.length) {
			case 0:
				return null;
			
			case 1: {
				Object result = getValue(primaryKeyColumns[0],rs,index);
				return rs.wasNull() ? null : result;
			}
		
			default: {
				Object[] values = new Object[primaryKeyColumns.length];
				for (int i = 0 ; i < primaryKeyColumns.length ; i++) {
					values[i] = getValue(primaryKeyColumns[i],rs,index);
					if (rs.wasNull()) {
						return null;
					}
				}
				return new CompositePrimaryKey(values);				
			}
		}
	}
	
	ForeignKeyConstraint getForeignKeyConstraintFor(String name) {
		for (ForeignKeyConstraint each : getTable().getForeignKeyConstraints()) {
			if (each.getFieldName().equals(name))
				return each;
		}
		return null;
	}
	
	public Class<?> getType(String fieldName) {
		for (Class<? extends T> implementation : implementations) {
			Class<?> result = mapper.getType(implementation, fieldName);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
}
