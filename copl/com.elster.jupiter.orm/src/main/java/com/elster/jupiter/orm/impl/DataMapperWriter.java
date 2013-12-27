package com.elster.jupiter.orm.impl;

import java.lang.reflect.Field;
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

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.orm.OptimisticLockException;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.impl.ManagedPersistentList;
import com.elster.jupiter.orm.associations.impl.PersistentReference;
import com.elster.jupiter.util.time.UtcInstant;

public class DataMapperWriter<T> {
	private final DataMapperImpl<T> dataMapper;
	
	DataMapperWriter(DataMapperImpl<T> dataMapper) {
		this.dataMapper = dataMapper;
	}

	private TableImpl<? super T> getTable() {
		return dataMapper.getTable();
	}
	
	private DataMapperType<? super T> getDataMapperType() {
		return dataMapper.getMapperType();
	}
	
	private DomainMapper getDomainMapper() {
		return getDataMapperType().getDomainMapper();
	}
	
	private TableSqlGenerator getSqlGenerator() {
		return dataMapper.getSqlGenerator();
	}
	
	private Connection  getConnection(boolean tranactionRequired) throws SQLException {
		return getTable().getDataModel().getConnection(tranactionRequired);
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
		prepare(object,false,new UtcInstant(getTable().getDataModel().getClock()));
		Map<ColumnImpl, Long> autoIncrements = new HashMap<>();
		try (Connection connection = getConnection(true)) {			
			try (PreparedStatement statement = connection.prepareStatement(getSqlGenerator().insertSql(false))) {
				int index = 1;	
				for (ColumnImpl column : getColumns())  {
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
		for (Map.Entry<ColumnImpl, Long> entry : autoIncrements.entrySet()) {
			Number value = entry.getValue();			
			if (entry.getKey().hasIntValue()) {
				value = value.intValue();
			}
			getDomainMapper().set(object,entry.getKey().getFieldName(), value);
		}
		refresh(object,true);
		if (getTable().hasChildren()) {
			for (ForeignKeyConstraintImpl constraint : getTable().getReverseMappedConstraints()) {
				if (constraint.isComposition()) {
					Field field = getDomainMapper().getField(object.getClass(), constraint.getReverseFieldName());
					if (field != null) {				
						try {
							List parts =(List) field.get(object);
							Class<?> clazz = getDomainMapper().extractClass(field.getGenericType());
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
		return getTable().hasAutoIncrementColumns() && !getTable().hasChildren();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void persist(List<T> objects) throws SQLException {
		if (objects.isEmpty()) {
			return;
		}
		UtcInstant now = new UtcInstant(getTable().getDataModel().getClock());
		if (getTable().hasAutoIncrementColumns() && getTable().hasChildren()) {
			for (T tuple : objects) {
				persist(tuple);
			}
			return;
		}
		try (Connection connection = getConnection(true)) {			
			try (PreparedStatement statement = connection.prepareStatement(getSqlGenerator().insertSql(true))) {
				for (T tuple : objects) {
					prepare(tuple,false,now);
					int index = 1;	
					for (ColumnImpl column : getColumns())  {
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
		for (ForeignKeyConstraintImpl constraint : getTable().getReverseMappedConstraints()) {
			if (constraint.isComposition()) {
				List allParts = new ArrayList<>();
				DataMapperImpl<?> mapper = null;
				for (Object object : objects) {
					Field field = getDomainMapper().getField(object.getClass(), constraint.getReverseFieldName());
					if (field != null) {
						if (mapper == null) {
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
		String sql = getSqlGenerator().journalSql();
		try (Connection connection = getConnection(true)) {						
			try (PreparedStatement statement = connection.prepareStatement(sql)) {				
				int index = 1;
				statement.setLong(index++, now.getTime());				
				for (ColumnImpl column : getPrimaryKeyColumns()) {
					statement.setObject(index++, getValue(object,column));
				}
				statement.executeUpdate();
			}
		}
	}
	
	private void journal(List<T> objects,UtcInstant now) throws SQLException {
		String sql = getSqlGenerator().journalSql();
		try (Connection connection = getConnection(true)) {						
			try (PreparedStatement statement = connection.prepareStatement(sql)) {						
				for (T tuple : objects) {
					int index = 1;
					statement.setLong(index++, now.getTime());				
					for (ColumnImpl column : getPrimaryKeyColumns()) {
						statement.setObject(index++, getValue(tuple,column));
					}
					statement.addBatch();
				}
				statement.executeBatch();
			}
		}
	}
	
	void update(T object,List<ColumnImpl> columns) throws SQLException {
		UtcInstant now = new UtcInstant(getTable().getDataModel().getClock());
		if (getTable().hasJournal()) {
			journal(object,now);
		}
		prepare(object,true,now);
		Column[] versionCountColumns = getTable().getVersionColumns();
		Map<Column,Long> versionCounts = new HashMap<>();
		try (Connection connection = getConnection(true)) {			
			String sql = getSqlGenerator().updateSql(columns);
			try (PreparedStatement statement = connection.prepareStatement(sql)) {				
				int index = 1;	
				for (ColumnImpl column : columns)  {
					statement.setObject(index++, getValue(object,column));
				}
				for (ColumnImpl column : getTable().getAutoUpdateColumns()) {
					statement.setObject(index++, getValue(object,column));
				}
				for (ColumnImpl column : getPrimaryKeyColumns()) {
					statement.setObject(index++, getValue(object,column));
				}
				for (ColumnImpl column : getTable().getVersionColumns()) {
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
			getDomainMapper().set(object,entry.getKey().getFieldName(), entry.getValue() + 1);
		}
		refresh(object,false);
	}
	
	
	void update(List<T> objects,List<ColumnImpl> columns) throws SQLException {	
		UtcInstant now = new UtcInstant(getTable().getDataModel().getClock());
		if (getTable().hasJournal()) {
			journal(objects,now);
		}
		try (Connection connection = getConnection(true)) {							
			try (PreparedStatement statement = connection.prepareStatement(getSqlGenerator().updateSql(columns))) {
				for (T tuple : objects) {
					prepare(tuple,true,now);
					int index = 1;	
					for (ColumnImpl column : columns)  {
						statement.setObject(index++, getValue(tuple,column));
					}
					for (ColumnImpl column : getTable().getAutoUpdateColumns()) {
						statement.setObject(index++, getValue(tuple,column));
					}
					for (ColumnImpl column : getPrimaryKeyColumns()) {
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
			journal(object,new UtcInstant(getTable().getDataModel().getClock()));
		}
		try (Connection connection = getConnection(true)) {			
			try (PreparedStatement statement = connection.prepareStatement(getSqlGenerator().deleteSql())) {
				int index = 1;	
				for (ColumnImpl column : getPrimaryKeyColumns()) {
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
		UtcInstant now = new UtcInstant(getTable().getDataModel().getClock());
		if (getTable().hasJournal()) {
			journal(objects,now);
		}
		try (Connection connection = getConnection(true)) {
			try (PreparedStatement statement = connection.prepareStatement(getSqlGenerator().deleteSql())) {
				for (T tuple : objects) {					
					int index = 1;	
					for (ColumnImpl column : getPrimaryKeyColumns()) {
						statement.setObject(index++, getValue(tuple,column));
					}
					statement.addBatch();
				}
				statement.executeBatch();
			}							
		} 	
	}
	
	private void refresh(T object, boolean afterInsert) throws SQLException {
		List<ColumnImpl> columns = afterInsert ? getTable().getInsertValueColumns() : getTable().getUpdateValueColumns();
		if (columns.size() == 0) {
			return;		
		}
		refresh(object,columns);
	}
	
	private void refresh(T object , List<ColumnImpl> columns) throws SQLException {
		try (Connection connection = getConnection(false)) {
			String sql = getSqlGenerator().refreshSql(columns);
			try (PreparedStatement statement = connection.prepareStatement(sql)) {
				int index = 1;
				for (ColumnImpl column : getPrimaryKeyColumns()) {
					statement.setObject(index++, getValue(object, column));
				}				
				try (ResultSet resultSet = statement.executeQuery()) {				
					resultSet.next();
					int columnIndex = 1;
					for (ColumnImpl column : columns) {					
						setValue(object,column,resultSet,columnIndex++) ;
					}									
				} 			
			} 
		} 
	}
	
	private void prepare(Object target, boolean update, UtcInstant now) {
		for (Column each : getColumns()) {
			if (each.getConversion() == ColumnConversion.NUMBER2NOW && !(update && each.skipOnUpdate())) {				
				getDomainMapper().set(target,each.getFieldName(),now);
			}
			if (each.getConversion() == ColumnConversion.CHAR2PRINCIPAL && !(update && each.skipOnUpdate())) {
				getDomainMapper().set(target,each.getFieldName(),getCurrentUserName());
			}
		}
	}
	
	private String getCurrentUserName() {
		Principal principal = getTable().getDataModel().getPrincipal();
		return principal == null ? null : principal.getName();
	}

	private Object getValue(Object target , ColumnImpl column) {
		if (column.isDiscriminator()) {
			return getDataMapperType().getDiscriminator(target.getClass());
		} 
		if (column.getFieldName() == null) {
			return getValue(target, column , column.getForeignKeyConstraint());
		}
		else {			
			return column.convertToDb(getDomainMapper().get(target , column.getFieldName()));
		}
	}
	
	private Object convertToDb(ColumnImpl column , Object value) {
		return column.convertToDb(value);
	}
	
	private Object getValue(Object target , ColumnImpl column , ForeignKeyConstraintImpl constraint) {
		Field field = getDomainMapper().getField(target.getClass(),constraint.getFieldName());
		if (field == null) {
			return null;
		}
		Reference<?> reference = (Reference<?>) getDomainMapper().get(target, constraint.getFieldName());
		if (reference == null || !reference.isPresent()) {
			return null;
		}
		int index = constraint.getColumns().indexOf(column);
		if (reference instanceof PersistentReference<?>) {
			Object value = ((PersistentReference<?>) reference).getKeyPart(index);
			return value == null ? null : convertToDb(column,value);
		}
		Object value = constraint.getReferencedTable().getPrimaryKey(reference.get()).get(index);
		return convertToDb(column,value);
	}

	private void setValue(Object target , ColumnImpl column , ResultSet rs, int index) throws SQLException {
		getDomainMapper().set(target, column.getFieldName(), column.convertFromDb(rs, index));
	}	

	private List<ColumnImpl> getColumns() {
		return getTable().getColumns();
	}
	
	private List<ColumnImpl> getPrimaryKeyColumns() {
		return getTable().getPrimaryKeyColumns();
	}
	
		
}
