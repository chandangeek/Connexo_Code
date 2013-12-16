package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.orm.OptimisticLockException;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.impl.ManagedPersistentList;
import com.elster.jupiter.orm.associations.impl.PersistentReference;
import com.elster.jupiter.orm.internal.Bus;
import com.elster.jupiter.util.time.UtcInstant;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.orm.internal.Bus.getConnection;

public class DataMapperWriter<T> {
	private final DataMapperType mapperType;
	private final TableSqlGenerator sqlGenerator;
	
	DataMapperWriter(DataMapperImpl<T> dataMapper) {
		this.mapperType = dataMapper.getMapperType();
		this.sqlGenerator = dataMapper.getSqlGenerator();
	}

	private TableImpl getTable() {
		return sqlGenerator.getTable();
	}
	
	private long getNext(Connection connection , String sequence) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement("select " + sequence + ".nextval from dual")) {
			try (ResultSet rs = statement.executeQuery()) {
				rs.next();
				return rs.getLong(1);
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void persist(T object) throws SQLException {
		prepare(object,false,new UtcInstant(Bus.getClock()));
		Map<Column, Long> autoIncrements = new HashMap<>();
		try (Connection connection = getConnection(true)) {			
			try (PreparedStatement statement = connection.prepareStatement(sqlGenerator.insertSql(false))) {
				int index = 1;	
				for (Column column : getColumns())  {
					if (column.isAutoIncrement()) {						
						autoIncrements.put(column, getNext(connection, column.getQualifiedSequenceName()));
						statement.setObject(index++, autoIncrements.get(column));
					} else if (!column.hasInsertValue()) {						
						statement.setObject(index++, getValue(object,column));
					}
				}
				statement.executeUpdate();				
			}							
		} 		
		// update autoIncrements fields
		for (Map.Entry<Column, Long> entry : autoIncrements.entrySet()) {
			Number value = entry.getValue();			
			if (((ColumnImpl) entry.getKey()).hasIntValue()) {
				value = value.intValue();
			}
			mapperType.getDomainMapper().set(object,entry.getKey().getFieldName(), value);
		}
		refresh(object,true);
		if (getTable().hasChildren()) {
			for (ForeignKeyConstraint constraint : getTable().getReverseConstraints()) {
				if (constraint.isComposition()) {
					Field field = mapperType.getDomainMapper().getField(object.getClass(), constraint.getReverseFieldName());
					if (field != null) {
						if (!Modifier.isFinal(field.getModifiers())) {
							throw new IllegalStateException("Owner collections must be final");
						}
						try {
							List parts =(List) field.get(object);
							Class<?> clazz = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
							DataMapperImpl<?> mapper = (DataMapperImpl<?>) constraint.getTable().getDataMapper(clazz);
							mapper.getWriter().persist(parts);
							if (mapper.getWriter().needsRefreshAfterBatchInsert()) {						
								field.set(object, new ManagedPersistentList<>(constraint, mapper, object));
							} else {
								field.set(object, new ManagedPersistentList<>(constraint, mapper, object, parts));
							}
						} catch (ReflectiveOperationException ex) {
							throw new MappingException(ex);
						}
					}
				}
			}
		}
	}
	
	boolean needsRefreshAfterBatchInsert() {
		return 
			getTable().hasAutoIncrementColumns() && !getTable().hasChildren();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void persist(List<T> objects) throws SQLException {
		if (objects.isEmpty()) {
			return;
		}
		UtcInstant now = new UtcInstant(Bus.getClock());
		if (getTable().hasAutoIncrementColumns() && getTable().hasChildren()) {
			for (T tuple : objects) {
				persist(tuple);
			}
			return;
		}
		try (Connection connection = getConnection(true)) {			
			try (PreparedStatement statement = connection.prepareStatement(sqlGenerator.insertSql(true))) {
				for (T tuple : objects) {
					prepare(tuple,false,now);
					int index = 1;	
					for (Column column : getColumns())  {
						if (!column.isAutoIncrement() && !column.hasInsertValue()) {						
							statement.setObject(index++, getValue(tuple,column));
						}
					}
					statement.addBatch();
				}
				statement.executeBatch();
			}							
		} 	
		if (!getTable().hasChildren()) {
			return;
		}
		for (ForeignKeyConstraint constraint : getTable().getReverseConstraints()) {
			if (constraint.isComposition()) {
				List allParts = new ArrayList<>();
				DataMapperImpl<?> mapper = null;
				for (Object object : objects) {
					Field field = mapperType.getDomainMapper().getField(object.getClass(), constraint.getReverseFieldName());
					if (field != null) {
						if (mapper == null) {
							if (!Modifier.isFinal(field.getModifiers())) {
								throw new IllegalStateException("Owner collections must be final");
							}
							Class<?> clazz = (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
							mapper = (DataMapperImpl<?>) constraint.getTable().getDataMapper(clazz);
						}
						try {
							List parts =(List) field.get(object);
							allParts.addAll(parts);
							if (mapper.getWriter().needsRefreshAfterBatchInsert()) {
								field.set(object, new ManagedPersistentList<>(constraint, mapper, object));
							} else {
								field.set(object, new ManagedPersistentList<>(constraint, mapper, object, parts));
							}
						} catch (ReflectiveOperationException ex) {
							throw new MappingException(ex);
						}
					}
				}
				mapper.getWriter().persist(allParts);
			}
		} 
	}
		
	private void journal(Object object,UtcInstant now) throws SQLException {
		String sql = sqlGenerator.journalSql();
		try (Connection connection = getConnection(true)) {						
			try (PreparedStatement statement = connection.prepareStatement(sql)) {				
				int index = 1;
				statement.setLong(index++, now.getTime());				
				for (Column column : getPrimaryKeyColumns()) {
					statement.setObject(index++, getValue(object,column));
				}
				statement.executeUpdate();
			}
		}
	}
	
	private void journal(List<T> objects,UtcInstant now) throws SQLException {
		String sql = sqlGenerator.journalSql();
		try (Connection connection = getConnection(true)) {						
			try (PreparedStatement statement = connection.prepareStatement(sql)) {						
				for (T tuple : objects) {
					int index = 1;
					statement.setLong(index++, now.getTime());				
					for (Column column : getPrimaryKeyColumns()) {
						statement.setObject(index++, getValue(tuple,column));
					}
					statement.addBatch();
				}
				statement.executeBatch();
			}
		}
	}
	
	void update(T object,List<Column> columns) throws SQLException {
		UtcInstant now = new UtcInstant(Bus.getClock());
		if (getTable().hasJournal()) {
			journal(object,now);
		}
		prepare(object,true,now);
		Column[] versionCountColumns = getTable().getVersionColumns();
		Map<Column,Long> versionCounts = new HashMap<>();
		try (Connection connection = getConnection(true)) {			
			String sql = sqlGenerator.updateSql(columns);
			try (PreparedStatement statement = connection.prepareStatement(sql)) {				
				int index = 1;	
				for (Column column : columns)  {
					statement.setObject(index++, getValue(object,column));
				}
				for (Column column : getTable().getAutoUpdateColumns()) {
					statement.setObject(index++, getValue(object,column));
				}
				for (Column column : getPrimaryKeyColumns()) {
					statement.setObject(index++, getValue(object,column));
				}
				for (Column column : getTable().getVersionColumns()) {
					Long value = (Long) getValue(object , column);
					versionCounts.put(column, value);
					statement.setObject(index++, value);
				}
				int result = statement.executeUpdate();
				if (result != 1) {
					if (versionCountColumns.length == 0) {
						throw new UnexpectedNumberOfUpdatesException(1, result, UnexpectedNumberOfUpdatesException.Operation.UPDATE);
					} else {
						throw new OptimisticLockException();
					}
				}
			}							
		} 	
		for (Map.Entry<Column, Long> entry : versionCounts.entrySet()) {
			// version count must have integer mapping
			mapperType.getDomainMapper().set(object,entry.getKey().getFieldName(), entry.getValue() + 1);
		}
		refresh(object,false);
	}
	
	
	void update(List<T> objects,List<Column> columns) throws SQLException {	
		UtcInstant now = new UtcInstant(Bus.getClock());
		if (getTable().hasJournal()) {
			journal(objects,now);
		}
		try (Connection connection = getConnection(true)) {							
			try (PreparedStatement statement = connection.prepareStatement(sqlGenerator.updateSql(columns))) {
				for (T tuple : objects) {
					prepare(tuple,true,now);
					int index = 1;	
					for (Column column : columns)  {
						statement.setObject(index++, getValue(tuple,column));
					}
					for (Column column : getTable().getAutoUpdateColumns()) {
						statement.setObject(index++, getValue(tuple,column));
					}
					for (Column column : getPrimaryKeyColumns()) {
						statement.setObject(index++, getValue(tuple,column));
					}
					statement.addBatch();
				}
				statement.executeBatch();				
			}							
		} 	
	}
	
	void remove(T object) throws SQLException {		
		if (getTable().hasJournal()) {
			journal(object,new UtcInstant(Bus.getClock()));
		}
		try (Connection connection = getConnection(true)) {			
			try (PreparedStatement statement = connection.prepareStatement(sqlGenerator.deleteSql())) {
				int index = 1;	
				for (Column column : getPrimaryKeyColumns()) {
					statement.setObject(index++, getValue(object,column));
				}
				int result = statement.executeUpdate();
				if (result != 1) {
					throw new UnexpectedNumberOfUpdatesException(1, result, UnexpectedNumberOfUpdatesException.Operation.DELETE);
				}
			}							
		} 	
	}
	
	void remove(List<T> objects) throws SQLException {
		UtcInstant now = new UtcInstant(Bus.getClock());
		if (getTable().hasJournal()) {
			journal(objects,now);
		}
		try (Connection connection = getConnection(true)) {
			try (PreparedStatement statement = connection.prepareStatement(sqlGenerator.deleteSql())) {
				for (T tuple : objects) {					
					int index = 1;	
					for (Column column : getPrimaryKeyColumns()) {
						statement.setObject(index++, getValue(tuple,column));
					}
					statement.addBatch();
				}
				statement.executeBatch();
			}							
		} 	
	}
	
	private void refresh(T object, boolean afterInsert) throws SQLException {
		List<Column> columns = afterInsert ? getTable().getInsertValueColumns() : getTable().getUpdateValueColumns();
		if (columns.size() == 0)
			return;		
		refresh(object,columns);
	}
	
	private void refresh(T object , List<Column> columns) throws SQLException {
		try (Connection connection = getConnection(false)) {
			String sql = sqlGenerator.refreshSql(columns);
			try (PreparedStatement statement = connection.prepareStatement(sql)) {
				int index = 1;
				for (Column column : getPrimaryKeyColumns()) {
					statement.setObject(index++, getValue(object, column));
				}				
				try (ResultSet resultSet = statement.executeQuery()) {				
					resultSet.next();
					int columnIndex = 1;
					for (Column column : columns) {					
						setValue(object,column,resultSet,columnIndex++) ;
					}									
				} 			
			} 
		} 
	}
	
	private void prepare(Object target, boolean update, UtcInstant now) {
		for (Column each : getColumns()) {
			if (each.getConversion() == ColumnConversion.NUMBER2NOW && !(update && each.skipOnUpdate())) {				
				mapperType.getDomainMapper().set(target,each.getFieldName(),now);
			}
			if (each.getConversion() == ColumnConversion.CHAR2PRINCIPAL && !(update && each.skipOnUpdate())) {
				mapperType.getDomainMapper().set(target,each.getFieldName(),getCurrentUserName());
			}
		}
	}
	
	private String getCurrentUserName() {
		Principal principal = Bus.getPrincipal();
		return principal == null ? null : principal.getName();
	}

	private Object getValue(Object target , Column column) {
		if (column.isDiscriminator()) {
			return mapperType.getDiscriminator(target.getClass());
		} 
		if (column.getFieldName() == null) {
			return  getValue(target, column , ((ColumnImpl) column).getForeignKeyConstraint());
		}
		else {			
			return ((ColumnImpl) column).convertToDb(mapperType.getDomainMapper().get(target , column.getFieldName()));
		}
	}
	
	private Object convertToDb(Column column , Object value) {
		return ((ColumnImpl) column).convertToDb(value);
	}
	
	private Object getValue(Object target , Column column , ForeignKeyConstraint constraint) {
		Reference<?> reference = (Reference<?>) mapperType.getDomainMapper().get(target, constraint.getFieldName());
		if (reference == null || !reference.isPresent()) {
			return null;
		}
		int index = constraint.getColumns().indexOf(column);
		if (reference instanceof PersistentReference<?>) {
			Object value = ((PersistentReference<?>) reference).getKeyPart(index);
			return value == null ? null : convertToDb(column,value);
		}
		Object value = constraint.getReferencedTable().getPrimaryKey(reference.get())[index];
		return convertToDb(column,value);
	}

	private void setValue(Object target , Column column , ResultSet rs, int index) throws SQLException {
		mapperType.getDomainMapper().set(target, column.getFieldName() , ((ColumnImpl) column).convertFromDb(rs, index));
	}	

		
	private List<Column> getColumns() {
		return sqlGenerator.getColumns();
	}
	
	private List<Column> getPrimaryKeyColumns() {
		return sqlGenerator.getPrimaryKeyColumns();
	}
	
		
}
