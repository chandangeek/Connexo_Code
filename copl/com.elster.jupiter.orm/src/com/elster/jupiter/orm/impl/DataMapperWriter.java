package com.elster.jupiter.orm.impl;

import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.OptimisticLockException;
import com.elster.jupiter.orm.PersistenceException;
import com.elster.jupiter.orm.plumbing.Bus;
import com.elster.jupiter.time.UtcInstant;
import static com.elster.jupiter.orm.plumbing.Bus.getConnection;

public class DataMapperWriter<T,S extends T> {
	private final DomainMapper fieldMapper = DomainMapper.FIELD;
	private final TableSqlGenerator sqlGenerator;
	
	DataMapperWriter(DataMapperImpl<T,S> dataMapper) {
		this.sqlGenerator = dataMapper.getSqlGenerator();
	}
	
	private long getNext(Connection connection , String sequence) throws SQLException {
		PreparedStatement statement = connection.prepareStatement("select " + sequence + ".nextval from dual");
		try {
			ResultSet rs = statement.executeQuery();
			try {
				rs.next();
				return rs.getLong(1);
			} finally {
				rs.close();
			}
		} finally {
			statement.close();
		}
	}
	
	void persist(T object) throws SQLException {
		prepare(object,false,new UtcInstant());
		Map<Column, Long> autoIncrements = new HashMap<Column,Long>();
		try (Connection connection = getConnection(true)) {			
			try (PreparedStatement statement = connection.prepareStatement(sqlGenerator.insertSql(false))) {
				int index = 1;	
				for (Column column : getColumns())  {
					if (column.isAutoIncrement()) {						
						autoIncrements.put(column, getNext(connection, column.getSequenceName()));
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
			fieldMapper.set(object,entry.getKey().getFieldName(), value);
		}
		refresh(object,true);
	}
		
	void persist(List<T> objects) throws SQLException {
		UtcInstant now = new UtcInstant();
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
	
	void update(T object,Column[] columns) throws SQLException {
		UtcInstant now = new UtcInstant();
		if (sqlGenerator.getTable().hasJournal()) {
			journal(object,now);
		}
		prepare(object,true,now);
		Column[] versionCountColumns = sqlGenerator.getTable().getVersionColumns();
		Map<Column,Long> versionCounts = (versionCountColumns.length == 0) ? null : new HashMap<Column,Long>();
		try (Connection connection = getConnection(true)) {			
			String sql = sqlGenerator.updateSql(columns);
			try (PreparedStatement statement = connection.prepareStatement(sql)) {				
				int index = 1;	
				for (Column column : columns)  {
					statement.setObject(index++, getValue(object,column));
				}
				for (Column column : sqlGenerator.getTable().getAutoUpdateColumns()) {
					statement.setObject(index++, getValue(object,column));
				}
				for (Column column : getPrimaryKeyColumns()) {
					statement.setObject(index++, getValue(object,column));
				}
				for (Column column : sqlGenerator.getTable().getVersionColumns()) {
					Long value = (Long) getValue(object , column);
					versionCounts.put(column, value);
					statement.setObject(index++, value);
				}
				int result = statement.executeUpdate();
				if (result != 1) {
					if (versionCountColumns.length == 0) {
						throw new PersistenceException("Updated " + result + " rows");
					} else {
						throw new OptimisticLockException();
					}
				}
			}							
		} 	
		for (Map.Entry<Column, Long> entry : versionCounts.entrySet()) {
			// version count must have integer mapping
			fieldMapper.set(object,entry.getKey().getFieldName(), entry.getValue() + 1);
		}
		refresh(object,false);
	}
	
	
	void update(List<T> objects,Column[] columns) throws SQLException {	
		UtcInstant now = new UtcInstant();
		if (sqlGenerator.getTable().hasJournal()) {
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
					for (Column column : sqlGenerator.getTable().getAutoUpdateColumns()) {
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
		if (sqlGenerator.getTable().hasJournal()) {
			journal(object,new UtcInstant());
		}
		try (Connection connection = getConnection(true)) {			
			try (PreparedStatement statement = connection.prepareStatement(sqlGenerator.deleteSql())) {
				int index = 1;	
				for (Column column : getPrimaryKeyColumns()) {
					statement.setObject(index++, getValue(object,column));
				}
				int result = statement.executeUpdate();
				if (result != 1) {
					throw new PersistenceException("Deleted " + result + " rows");
				}
			}							
		} 	
	}
	
	void remove(List<T> objects) throws SQLException {
		UtcInstant now = new UtcInstant();
		if (sqlGenerator.getTable().hasJournal()) {
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
		Column[] columns = afterInsert ? sqlGenerator.getTable().getInsertValueColumns() : sqlGenerator.getTable().getUpdateValueColumns();
		if (columns.length == 0)
			return;		
		refresh(object,columns);
	}
	
	private void refresh(T object , Column[] columns) throws SQLException {
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
				fieldMapper.set(target,each.getFieldName(),now);
			}
			if (each.getConversion() == ColumnConversion.CHAR2PRINCIPAL && !(update && each.skipOnUpdate())) {
				fieldMapper.set(target,each.getFieldName(),getCurrentUserName());
			}
		}
	}
	
	private String getCurrentUserName() {
		Principal principal = Bus.getPrincipal();
		return principal == null ? null : principal.getName();
	}

	private Object getValue(Object target , Column column) {		
		return ((ColumnImpl) column).convertToDb(fieldMapper.get(target , column.getFieldName()));
	}
	
	private void setValue(Object target , Column column , ResultSet rs, int index) throws SQLException {
		fieldMapper.set(target, column.getFieldName() , ((ColumnImpl) column).convertFromDb(rs, index));
	}	

		
	private ColumnImpl[] getColumns() {
		return sqlGenerator.getColumns();
	}
	
	private Column[] getPrimaryKeyColumns() {
		return sqlGenerator.getPrimaryKeyColumns();
	}
	
}
