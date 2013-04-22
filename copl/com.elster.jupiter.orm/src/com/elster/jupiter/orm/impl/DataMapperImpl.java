package com.elster.jupiter.orm.impl;

import java.util.*;
import java.lang.reflect.Constructor;
import java.security.Principal;
import java.sql.*;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.plumbing.Bus;
import com.elster.jupiter.sql.util.SqlBuilder;
import com.elster.jupiter.sql.util.SqlFragment;
import com.elster.jupiter.time.UtcInstant;

class DataMapperImpl<T , S extends T> extends AbstractFinder<T> implements DataMapper<T> {
	
	private final static String ALIAS = "a";
	
	final private TableSqlGenerator sqlGenerator;
	final private FieldMapper mapper = new FieldMapper();
	final private Constructor<S> constructor;
	final private Class<S> implementation;
	
	DataMapperImpl(Class<T> api, Class<S> implementation ,  Table table) {
		this.sqlGenerator = new TableSqlGenerator((TableImpl) table);
		try {
			this.implementation = implementation;
			this.constructor = implementation.getDeclaredConstructor();
			this.constructor.setAccessible(true);
		} catch (ReflectiveOperationException ex) {
			throw new PersistenceException(ex);
		}
	}	
	
	@Override 
	public Table getTable() {
		return sqlGenerator.getTable();
	}
	
	TableSqlGenerator getSqlGenerator() {
		return sqlGenerator;
	}
	
	
	private Connection getConnection(boolean transactionRequired) throws SQLException {
		return Bus.getConnection(transactionRequired);
	}
	
	private List<SqlFragment> getPrimaryKeyFragments(Object[] values) {
		Column[] pkColumns = getPrimaryKeyColumns();
		if (pkColumns.length != values.length) {
			throw new IllegalArgumentException("Argument array length does not match Primary Key Field count of " + pkColumns.length);
		}
		List<SqlFragment> fragments = new ArrayList<>(pkColumns.length);
		for (int i = 0 ; i < values.length ; i++) {
			fragments.add(new ColumnFragment(pkColumns[i] , values[i] , ALIAS));
		}
		return fragments;		
	}
	
	@Override
	List<T> findByPrimaryKey (Object[] values) {
		return find(getPrimaryKeyFragments(values),null,false);		
	}
	
	@Override
	int getPrimaryKeyLength() {
		return getPrimaryKeyColumns().length;
	}
	
	@Override
	public T lock(Object... values)  {
		List<T> candidates = find(getPrimaryKeyFragments(values) , null , true);
		return candidates.isEmpty() ? null : candidates.get(0);
	}
	
	@Override
	public List<T> find(String[] fieldNames , Object[] values , String... orderColumns) {
		List<SqlFragment> fragments = new ArrayList<>();
		if (fieldNames != null) {
			for (int i = 0 ; i < fieldNames.length ; i++) {
				addFragments(fragments,fieldNames[i], values[i]);
			}
		}
		return find(fragments, orderColumns, false);		
	}
			
	public List<T> find(List<SqlFragment> fragments , String[] orderColumns , boolean lock) {
		try {
			return doFind(fragments, orderColumns,lock);
		} catch(SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	
	private SqlBuilder selectSql(List<SqlFragment> fragments, String[] orderColumns , boolean lock) {
		SqlBuilder builder = new SqlBuilder(sqlGenerator.getSelectFromClause(ALIAS));
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
				builder.append(column == null ? each : column.getName(ALIAS));
				separator = ", ";
			}
		}
		if (lock) {
			builder.append(" for update ");
		}
		return builder;
	}
	
	private List<T> doFind(List<SqlFragment> fragments, String[] orderColumns,boolean lock) throws SQLException {
		List<Setter> setters = new ArrayList<>();
		for (SqlFragment each : fragments) {
			if (each instanceof Setter) {
				setters.add((Setter) each);
			}
		}
		List<T> result = new ArrayList<>();	
		SqlBuilder builder = selectSql(fragments, orderColumns,lock);
		System.out.println(builder);
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
	
	private T newInstance() {
		try {			
			return constructor.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new PersistenceException(e);
		}
	}
	
	T construct(ResultSet rs, int startIndex) throws SQLException {		
		T result = newInstance();		
		int columnIndex = startIndex;
		for (Column column : sqlGenerator.getColumns()) {						
			setValue(result,column,rs,columnIndex++);
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
	
	@Override
	public void persist(T object)  {
		try {
			doPersist(object);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
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
	private void doPersist(T object) throws SQLException {
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
			mapper.set(object,entry.getKey().getFieldName(), value);
		}
		refresh(object,true);
	}
		
	@Override
	// note that this will not fill back auto increment columns.
	public void persist(List<T> objects)  {
		try {
			doPersist(objects);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	
	private void doPersist(List<T> objects) throws SQLException {
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
			Column column = getColumnForField(fieldName);
			if (column.isPrimaryKeyColumn() || column.isVersion() || column.hasUpdateValue()) {
				throw new IllegalArgumentException("Cannot update primary key column or version count column or column with update value");
			} else {
				columns[i++] = column;
			}
		}
		return columns;
	}
	
	private void update(T object,Column[] columns) {
		try {
			doUpdate(object,columns);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
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
	
	private void doUpdate(T object,Column[] columns) throws SQLException {
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
			mapper.set(object,entry.getKey().getFieldName(), entry.getValue() + 1);
		}
		refresh(object,false);
	}
	
	@Override
	public void update(List<T> objects)  {
		try {
			doUpdate(objects,sqlGenerator.getTable().getStandardColumns());
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	
	@Override
	public void update(List<T> objects , String... fieldNames)  {
		try {
			doUpdate(objects,getUpdateColumns(fieldNames));
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	
	private void doUpdate(List<T> objects,Column[] columns) throws SQLException {	
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
	
	@Override
	public void remove(T object )  {
		try {
			doRemove(object);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	
	private void doRemove(T object) throws SQLException {		
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
	
	@Override
	public void remove(List<T> objects )  {
		try {
			doRemove(objects);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	
	private void doRemove(List<T> objects) throws SQLException {
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
				mapper.set(target,each.getFieldName(),now);
			}
			if (each.getConversion() == ColumnConversion.CHAR2PRINCIPAL && !(update && each.skipOnUpdate())) {
				mapper.set(target,each.getFieldName(),getCurrentUserName());
			}
		}
	}
	
	private String getCurrentUserName() {
		Principal principal = Bus.getPrincipal();
		return principal == null ? null : principal.getName();
	}

	private Object getValue(Object target , Column column) {		
		return ((ColumnImpl) column).convertToDb(mapper.get(target , column.getFieldName()));
	}
	
	private void setValue(Object target , Column column , ResultSet rs, int index) throws SQLException {
		mapper.set(target, column.getFieldName() , ((ColumnImpl) column).convertFromDb(rs, index));
	}	
	
	Object convert(Column column , String value) {
		if (column.isEnum()) {
			return getEnum(column,value);
		} else {
			return ((ColumnImpl) column).convert(value);
		}
	}
	
	private Object getEnum(Column column, String value) {		
		return mapper.getEnum(implementation, column.getFieldName(),value);		
	}
	
	private ColumnImpl[] getColumns() {
		return sqlGenerator.getColumns();
	}
	
	private Column[] getPrimaryKeyColumns() {
		return sqlGenerator.getPrimaryKeyColumns();
	}
	
	Column getColumnForField(String fieldName) {
		return getTable().getColumnForField(fieldName);
	}


	@Override
	public QueryExecutor<T> with(DataMapper<?>... dataMappers) {
		QueryExecutorImpl <T> result = new QueryExecutorImpl<>(this);
		for (DataMapper<?> each : dataMappers) {
			result.add(each);
		}
		return result;
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
		Column column = getColumnForField(fieldName);
		if (column != null) {
			fragments.add(new ColumnFragment(column, value,ALIAS));
			return;
		}
		TableConstraint constraint = getForeignKeyConstraintFor(fieldName);
		if (constraint != null) {			
			fragments.add(new ConstraintFragment(constraint, value , ALIAS));
			return;
		}
		throw new IllegalArgumentException("Invalid field " + fieldName);
	}
}
