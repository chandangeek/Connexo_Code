package com.elster.jupiter.orm.impl;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.orm.OptimisticLockException;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.orm.associations.impl.ManagedPersistentList;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.UtcInstant;

public class DataMapperWriter<T> {
	private final DataMapperImpl<T> dataMapper;
	
	DataMapperWriter(DataMapperImpl<T> dataMapper) {
		this.dataMapper = dataMapper;
	}

	private TableImpl<? super T> getTable() {
		return dataMapper.getTable();
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
		try (Connection connection = getConnection(true)) {			
			try (PreparedStatement statement = connection.prepareStatement(getSqlGenerator().insertSql(false))) {
				int index = 1;	
				for (ColumnImpl column : getColumns())  {
					if (column.isAutoIncrement()) {			
						Long nextVal = getNext(connection, column.getQualifiedSequenceName());
						column.setDomainValue(object, nextVal);
						statement.setObject(index++, column.hasIntValue() ? nextVal.intValue() : nextVal);
					} else if (!column.hasInsertValue()) {
						column.setObject(statement, index++, object);
					}
				}
				statement.executeUpdate();				
			}							
		} 		
		refresh(object,true);
		if (getTable().hasChildren()) {
			for (ForeignKeyConstraintImpl constraint : getTable().getReverseMappedConstraints()) {
				if (constraint.isComposition()) {
					Field field = constraint.compositionField(object.getClass());
					if (field != null) {			
						try {
							List parts =(List) field.get(object);
							DataMapperImpl<?> mapper = (DataMapperImpl<?>) constraint.compositionMapper(field);
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
							column.setObject(statement,index++, tuple);
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
					Field field = constraint.compositionField(object.getClass());
					if (field != null) {
						if (mapper == null) {
							mapper = (DataMapperImpl<?>) constraint.compositionMapper(field);
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
				index = bindPrimaryKey(statement, index, object);
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
					index = bindPrimaryKey(statement, index, tuple);
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
		ColumnImpl[] versionCountColumns = getTable().getVersionColumns();
		List<Pair<ColumnImpl,Long>> versionCounts = new ArrayList<>(versionCountColumns.length);
		try (Connection connection = getConnection(true)) {			
			String sql = getSqlGenerator().updateSql(columns);
			try (PreparedStatement statement = connection.prepareStatement(sql)) {				
				int index = 1;	
				for (ColumnImpl column : columns)  {
					column.setObject(statement, index++, object);
				}
				for (ColumnImpl column : getTable().getAutoUpdateColumns()) {
					column.setObject(statement, index++, object);
				}
				index = bindPrimaryKey(statement, index, object);
				for (ColumnImpl column : versionCountColumns) {
					Long value = (Long) column.getDomainValue(object);
					versionCounts.add(Pair.of(column, value));
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
		for (Pair<ColumnImpl, Long> pair : versionCounts) {
			pair.getFirst().setDomainValue(object, pair.getLast() + 1);
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
						column.setObject(statement, index++, tuple);
					}
					for (ColumnImpl column : getTable().getAutoUpdateColumns()) {
						column.setObject(statement, index++, tuple);
					}
					index = bindPrimaryKey(statement, index, tuple);
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
				index = bindPrimaryKey(statement, index, object);
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
					index = bindPrimaryKey(statement, index, tuple);
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
				index = bindPrimaryKey(statement, index, object);			
				try (ResultSet resultSet = statement.executeQuery()) {				
					resultSet.next();
					int columnIndex = 1;
					for (ColumnImpl column : columns) {					
						column.setDomainValue(object,resultSet,columnIndex++) ;
					}									
				} 			
			} 
		} 
	}
	
	private void prepare(Object target, boolean update, UtcInstant now) {
		for (ColumnImpl each : getColumns()) {
			each.prepare(target,update,now);
		}
	}

	private List<ColumnImpl> getColumns() {
		return getTable().getColumns();
	}
	
	int bindPrimaryKey(PreparedStatement statement, int index, Object target) throws SQLException {
		for (ColumnImpl column : getTable().getPrimaryKeyColumns()) {
			column.setObject(statement,index++, target);
		}
		return index;
	}
		
}
