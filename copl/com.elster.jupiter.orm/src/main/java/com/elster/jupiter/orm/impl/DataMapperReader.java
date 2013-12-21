package com.elster.jupiter.orm.impl;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.orm.NotUniqueException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.impl.ManagedPersistentList;
import com.elster.jupiter.orm.associations.impl.PersistentReference;
import com.elster.jupiter.orm.associations.impl.UnManagedPersistentList;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.orm.fields.impl.ColumnEqualsFragment;
import com.elster.jupiter.orm.fields.impl.FieldMapping;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;

public class DataMapperReader<T> {
	private final TableSqlGenerator sqlGenerator;
	private final String alias;
	private final DataMapperType mapperType;
	
	DataMapperReader(DataMapperImpl<T> dataMapper, DataMapperType mapperType) {
		this.sqlGenerator = dataMapper.getSqlGenerator();	
		this.alias = dataMapper.getAlias();
		this.mapperType = mapperType;
	}
	
	private Connection getConnection(boolean txRequired) throws SQLException {
		return getTable().getDataModel().getConnection(txRequired);
	}
	
	private TableImpl getTable() {
		return sqlGenerator.getTable();
	}
	
	private TableSqlGenerator getSqlGenerator() {
		return sqlGenerator;
	}
	
	private List<SqlFragment> getPrimaryKeyFragments(Object[] values) {
		List<ColumnImpl> pkColumns = getPrimaryKeyColumns();
		if (pkColumns.size() != values.length) {
			throw new IllegalArgumentException("Argument array length does not match Primary Key Field count of " + pkColumns.size());
		}
		List<SqlFragment> fragments = new ArrayList<>(pkColumns.size());
		for (int i = 0 ; i < values.length ; i++) {
			fragments.add(new ColumnEqualsFragment(pkColumns.get(i) , values[i] , alias));
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
		return getPrimaryKeyColumns().size();
	}
	
	public T lock(Object... values)  throws SQLException {
		List<T> candidates = find(getPrimaryKeyFragments(values) , null , true);
		return candidates.isEmpty() ? null : candidates.get(0);
	}

	
	private String getListOrder(String fieldName) {
		ForeignKeyConstraintImpl constraint = getTable().getConstraintForField(fieldName);
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
        if (!fragments.isEmpty()) {
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


    	
	private T newInstance(ResultSet rs , int startIndex) throws SQLException {
		for (int i = 0 ; i < getColumns().size() ; i++) {
			if (getColumns().get(i).isDiscriminator()) {
				return mapperType.newInstance(rs.getString(startIndex + i));
			}
		}
		throw MappingException.noDiscriminatorColumn();
	}
	
	T construct(ResultSet rs, int startIndex) throws SQLException {		
		T result = mapperType.hasMultiple() ? newInstance(rs,startIndex) : mapperType.<T>newInstance();
		List<Pair<ColumnImpl, Object>> columnValues = new ArrayList<>();
		DomainMapper mapper = mapperType.getDomainMapper();
		for (ColumnImpl column : getSqlGenerator().getColumns()) {
			Object value = column.convertFromDb(rs, startIndex++);
			if (column.isForeignKeyPart()) {
				columnValues.add(Pair.of(column,rs.wasNull() ? null : value));
			}
			if (column.getFieldName() != null) {
				mapper.set(result, column.getFieldName(), value, getTable().getDataModel().getInjector());
			}
		}
		for (ForeignKeyConstraintImpl constraint : getTable().getReferenceConstraints()) {
			Field field = mapper.getField(result.getClass(), constraint.getFieldName());
			if (field != null && Reference.class.isAssignableFrom(field.getType())) {
				Object[] key = createKey(constraint,columnValues);
				DataMapperImpl<?> dataMapper = constraint.getReferencedTable().getDataMapper(getTypeArgument(field));
				Reference<?> reference = new PersistentReference<>(key, dataMapper);
				try {
					field.set(result, reference);
				} catch (ReflectiveOperationException ex) {
					throw new MappingException(ex);
				}
			}
		}
		for (ForeignKeyConstraintImpl constraint : getTable().getReverseConstraints()) {
			Field field = mapper.getField(result.getClass(), constraint.getReverseFieldName());
			if (field != null && List.class.isAssignableFrom(field.getType())) {
				DataMapperImpl<?> dataMapper = (DataMapperImpl<?>) constraint.getTable().getDataMapper(getTypeArgument(field));
				List<?> value = (constraint.isComposition()) ?
						new ManagedPersistentList<>(constraint, dataMapper, result) :
						new UnManagedPersistentList<>(constraint, dataMapper, result);
				try {
					field.set(result, value);
				} catch (ReflectiveOperationException ex) {
					throw new MappingException(ex);
				}
			}
		}
		return result;
	}
	
	private Class<?> getTypeArgument(Field field) {
		ParameterizedType type = (ParameterizedType) field.getGenericType();
		return (Class<?>) type.getActualTypeArguments()[0];
	}
	
	private Object getValue(ColumnImpl column , List<Pair<ColumnImpl,Object>> columnValues) {
		for (Pair<ColumnImpl,Object> pair : columnValues) {
			if (column.equals(pair.getFirst())) {
				return pair.getLast();
			}
		}
		throw new IllegalArgumentException();
	}
	
	private Object[] createKey(ForeignKeyConstraintImpl constraint , List<Pair<ColumnImpl,Object>> columnValues) {
		List<ColumnImpl> columns = constraint.getColumns();
		Object[] result = new Object[columns.size()];
		for (int i = 0 ; i < result.length ; i++) {
			result[i] = getValue(columns.get(i),columnValues);
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
	
	private List<ColumnImpl> getColumns() {
		return getSqlGenerator().getColumns();
	}
	
	private List<ColumnImpl> getPrimaryKeyColumns() {
		return getSqlGenerator().getPrimaryKeyColumns();
	}
	
	Column getColumnForField(String fieldName) {
		return getTable().getColumnForField(fieldName);
	}
	
	private int getIndex(Column column) {
		int result = getColumns().indexOf(column);
		if (result < 0) {
			throw new IllegalArgumentException();
		}
		return result;
	}
	
	private Object getValue(ColumnImpl column , ResultSet rs , int startIndex ) throws SQLException {
		int offset = getIndex(column);
		return column.convertFromDb(rs, startIndex + offset);
	}
	
	Object getPrimaryKey(ResultSet rs , int index) throws SQLException {
		List<ColumnImpl> primaryKeyColumns = getPrimaryKeyColumns();		
		if (primaryKeyColumns.size() == 0) {
			return null;
		}
		if (primaryKeyColumns.size() == 1) {		
			Object result = getValue(primaryKeyColumns.get(0),rs,index);
			return rs.wasNull() ? null : result;
		}
		Object[] values = new Object[primaryKeyColumns.size()];
		for (int i = 0 ; i < values.length ; i++) {
			values[i] = getValue(primaryKeyColumns.get(i),rs,index);
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
		FieldMapping mapping = getTable().getFieldMapping(fieldName);
		if (mapping == null) {
			throw new IllegalArgumentException("Invalid field " + fieldName);
		} else {
			fragments.add(mapping.asEqualFragment(value, alias));
		}
	}
	
	
}
