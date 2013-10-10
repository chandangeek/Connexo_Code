package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.orm.fields.impl.ColumnEqualsFragment;
import com.elster.jupiter.orm.fields.impl.FieldMapping;
import com.elster.jupiter.orm.plumbing.Bus;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.orm.plumbing.Bus.getConnection;

public class DataMapperReader<T> {
	private final TableSqlGenerator sqlGenerator;
	private final String alias;
	private final Map<String,Constructor<? extends T>> constructors;
	private final Constructor<? extends T> constructor;
	
	DataMapperReader(DataMapperImpl<T> dataMapper, Class<? extends T> implementation) {
		this.sqlGenerator = dataMapper.getSqlGenerator();	
		this.alias = dataMapper.getAlias();
		this.constructors = null;
		try {
			constructor = implementation.getDeclaredConstructor();
			constructor.setAccessible(true);			
		} catch (ReflectiveOperationException ex) {
			throw new MappingException(ex);
		}
	}
	
	DataMapperReader(DataMapperImpl<T> dataMapper, Map<String, Class<? extends T>> implementations) {
		this.sqlGenerator = dataMapper.getSqlGenerator();	
		this.alias = dataMapper.getAlias();
		this.constructor = null;
		this.constructors = new HashMap<>();
		try {
			for (Map.Entry<String, Class<? extends T>> entry : implementations.entrySet()) {
				Constructor<? extends T> constructor = entry.getValue().getDeclaredConstructor();
				constructor.setAccessible(true);
				constructors.put(entry.getKey(),constructor);
			}
		} catch (ReflectiveOperationException ex) {
			throw new MappingException(ex);
		}
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
	
	Optional<T> findByPrimaryKey (Object[] values) throws SQLException {
        List<T> result = find(getPrimaryKeyFragments(values), null, false);
        if (result.size() > 1) {
            throw new NotUniqueException(Arrays.toString(values));
        }
        return result.isEmpty() ? Optional.<T>absent() : Optional.of(result.get(0));
	}

    List<JournalEntry<T>> findJournals(Object[] values) throws SQLException {
        return findJournal(getPrimaryKeyFragments(values), new String[] { TableImpl.JOURNALTIMECOLUMNNAME + " desc" }, false);
    }
	
	int getPrimaryKeyLength() {
		return getPrimaryKeyColumns().length;
	}
	
	public T lock(Object... values)  throws SQLException {
		List<T> candidates = find(getPrimaryKeyFragments(values) , null , true);
		return candidates.isEmpty() ? null : candidates.get(0);
	}

	
	private String getListOrder(String fieldName) {
		ForeignKeyConstraint constraint = getTable().getConstraintForField(fieldName);
		if (constraint == null) {
			return null;
		} else {
			return constraint.getReverseOrderFieldName();
		}
	}
	
	public List<T> find(String[] fieldNames , Object[] values , String... orderColumns) throws SQLException {
		if (fieldNames != null && fieldNames.length == 1 && (orderColumns == null || orderColumns.length == 0)) {
			String listOrder = getListOrder(fieldNames[0]);
			if (listOrder != null) {
				orderColumns = new String[] { listOrder };
			}
		}
		List<SqlFragment> fragments = new ArrayList<>();
		if (fieldNames != null) {
			for (int i = 0 ; i < fieldNames.length ; i++) {
				addFragments(fragments,fieldNames[i], values[i]);
			}
		}
		return find(fragments, orderColumns, false);		
	}
			
	private List<T> find(List<SqlFragment> fragments, String[] orderColumns,boolean lock) throws SQLException {
        SqlBuilder builder = selectSql(fragments, orderColumns, lock);
        return doFind(fragments, builder);
	}

    private List<JournalEntry<T>> findJournal(List<SqlFragment> fragments, String[] orderColumns,boolean lock) throws SQLException {    	
        SqlBuilder builder = selectJournalSql(fragments, orderColumns, lock);     
        List<JournalEntry<T>> result = new ArrayList<>();
        try (Connection connection = getConnection(false)) {
            try(PreparedStatement statement = builder.prepare(connection)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while(resultSet.next()) {
                    	UtcInstant journalTime = new UtcInstant(resultSet.getLong(1));
                    	T entry = construct(resultSet,2);                    	
                        result.add(new JournalEntry<>(journalTime,entry));
                    }
                }
            }
        }
        return result;
        
    }

    private List<T> doFind(List<SqlFragment> fragments, SqlBuilder builder) throws SQLException {
        List<Setter> setters = getSetters(fragments);
        List<T> result = new ArrayList<>();        
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

    private List<Setter> getSetters(List<SqlFragment> fragments) {
        List<Setter> setters = new ArrayList<>();
        for (SqlFragment each : fragments) {
            if (each instanceof Setter) {
                setters.add((Setter) each);
            }
        }
        return setters;
    }

    private SqlBuilder selectSql(List<SqlFragment> fragments, String[] orderColumns , boolean lock) {
		SqlBuilder builder = new SqlBuilder(getSqlGenerator().getSelectFromClause(alias));
        return doSelectSql(fragments, orderColumns, lock, builder);
	}

    private SqlBuilder selectJournalSql(List<SqlFragment> fragments, String[] orderColumns , boolean lock) {
        SqlBuilder builder = new SqlBuilder(getSqlGenerator().getSelectFromJournalClause(alias));
        return doSelectSql(fragments, orderColumns, lock, builder);
    }

    private SqlBuilder doSelectSql(List<SqlFragment> fragments, String[] orderColumns, boolean lock, SqlBuilder builder) {
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


    private T newInstance(Constructor <? extends T> factory) {
		try {			
			return factory.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new MappingException(e);
		}
	}
	
	private T newInstance(ResultSet rs , int startIndex) throws SQLException {
		for (int i = 0 ; i < getColumns().length ; i++) {
			if (getColumns()[i].isDiscriminator()) {
				String typeString = rs.getString(startIndex + i);
				Constructor<? extends T> factory = constructors.get(typeString);
				if (factory == null) {
					throw MappingException.noMappingForSqlType(typeString);
				} else {
					return newInstance(factory);				
				}
			}
		}
		throw MappingException.noDiscriminatorColumn();
	}
	
	T construct(ResultSet rs, int startIndex) throws SQLException {		
		T result = constructors == null ? newInstance(constructor) : newInstance(rs,startIndex);
		DomainMapper mapper = constructors == null ? DomainMapper.FIELDSTRICT : DomainMapper.FIELDLENIENT;
		for (Column column : getSqlGenerator().getColumns()) {
			mapper.set(result, column.getFieldName(), ((ColumnImpl) column).convertFromDb(rs, startIndex++));
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
	
	ForeignKeyConstraint getForeignKeyConstraintFor(String name) {
		for (ForeignKeyConstraint each : getTable().getForeignKeyConstraints()) {
			if (each.getFieldName().equals(name)) {
				return each;
            }
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

	

