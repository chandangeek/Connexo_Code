package com.elster.jupiter.orm.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.query.impl.QueryExecutorImpl;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;

public class DataMapperImpl<T> extends AbstractFinder<T> implements DataMapper<T> {
	
	// contains relevant content from select keyword from v$reserved_words where length(keyword) < 4 order by keyword 
	private final static String[] RESERVED_WORDS = {
		"A",                                                                    
		"ABS",
		"ACL",
		"ACL",                                                                             
		"ADD",                                                                             
		"ALL",                                                                             
		"AND",                                                                             
		"ANY",                                                                             
		"AS",                                                                              
		"ASC",                                                                             
		"AT",                                                                              
		"AVG",                                                                             
		"BY",                                                                              
		"CHR",                                                                             
		"COS",                                                                             
		"CV",                                                                              
		"D",                                                                               
		"DAY",                                                                             
		"DBA",                                                                             
		"DDL",                                                                             
		"DEC",                                                                             
		"DML",                                                                             
		"DV",                                                                              
		"E",                                                                               
		"EM",                                                                              
		"END",                                                                             
		"EXP",                                                                             
		"FAR",                                                                             
		"FOR",                                                                             
		"G",                                                                               
		"GET",                                                                             
		"H",                                                                               
		"HOT",                                                                             
		"ID",                                                                              
		"IF",                                                                              
		"ILM",                                                                             
		"IN",                                                                              
		"INT",                                                                             
		"IS",                                                                              
		"JOB",                                                                             
		"K",                                                                               
		"KEY",                                                                             
		"LAG",                                                                             
		"LN",                                                                              
		"LOB",                                                                             
		"LOG",                                                                             
		"LOW",                                                                             
		"M",                                                                               
		"MAX",                                                                             
		"MIN",                                                                             
		"MOD",                                                                             
		"NAN",                                                                             
		"NAV",                                                                             
		"NEG",                                                                             
		"NEW",                                                                             
		"NO",                                                                              
		"NOT",                                                                             
		"NVL",                                                                             
		"OF",                                                                              
		"OFF",                                                                             
		"OID",                                                                             
		"OLD",                                                                             
		"OLS",                                                                             
		"ON",                                                                              
		"ONE",                                                                             
		"OR",                                                                              
		"OWN",                                                                             
		"P",                                                                               
		"PER",                                                                             
		"RAW",                                                                             
		"RBA",                                                                             
		"REF",                                                                             
		"ROW",                                                                             
		"SB4",                                                                             
		"SCN",                                                                             
		"SET",                                                                             
		"SID",                                                                             
		"SIN",                                                                             
		"SQL",                                                                             
		"SUM",                                                                             
		"T",                                                                               
		"TAG",                                                                             
		"TAN",                                                                             
		"THE",                                                                            
		"TO",                                                                              
		"TX",                                                                              
		"U",                                                                               
		"UB2",                                                                             
		"UBA",                                                                             
		"UID",                                                                             
		"USE",                                                                             
		"V1",                                                                              
		"V2",                                                                              
		"XID",                                                                             
		"XML",                                                                             
		"XS",                                                                              
		"YES" }; 
	private final TableSqlGenerator sqlGenerator;
	private final DataMapperType mapperType;
	private final String alias;
	private final DataMapperReader<T> reader;
	private final DataMapperWriter<T> writer;
	
	DataMapperImpl(Class<T> api, DataMapperType mapperType ,  TableImpl table) {
		this.sqlGenerator = new TableSqlGenerator(table);
		this.alias = createAlias(api.getName());
		this.mapperType = mapperType;
		this.reader = new DataMapperReader<>(this,mapperType);
		this.writer = new DataMapperWriter<>(this);
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
            builder.append('X');
        }
        String result = builder.toString().toUpperCase();
        if (Arrays.binarySearch(RESERVED_WORDS,result) >= 0) {
        	return result + "1";
        } else {
        	return result;      
        }
	}
	
	public String getAlias() {
		return alias;
	}
	
	@Override 
	public TableImpl getTable() {
		return sqlGenerator.getTable();
	}
	
	public TableSqlGenerator getSqlGenerator() {
		return sqlGenerator;
	}
	
	private TableCache<T> getCache() {
		return getTable().getCache();
	}

	@Override
    Optional<T> findByPrimaryKey(Object[] values) {
		TableCache<T> cache = getCache();
		Optional<T> result = cache.getOptional(reader,values);
		if (result.isPresent()) {
			return result;
		}
		try {
			result = reader.findByPrimaryKey(values);
			if (result.isPresent()) {
				cache.put(reader,values, result.get());
			}
			return result;
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
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
			throw new UnderlyingSQLFailedException(ex);
		}
	}
	
	@Override
	public List<T> find(String[] fieldNames, Object[] values, String... orderColumns) {
		if (fieldNames == null) {
			Optional<List<T>> candidate = getCache().find(reader);
			if (candidate.isPresent()) {
				return candidate.get();
			}
		}
		try {
			return reader.find(fieldNames,values,orderColumns);
		} catch(SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}

    @Override
    public List<JournalEntry<T>> getJournal(Object... values) {
        try {
            return reader.findJournals(values);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    public T construct(ResultSet rs, int startIndex) throws SQLException {
		return reader.construct(rs,startIndex);
	}
	
	private void preventIfChild() {
		if (getTable().isChild()) {
			throw new UnsupportedOperationException();
		}
	}
	
	private void cacheChange() {
		//TODO send event
	}
	@Override
	public void persist(T object)  {
		preventIfChild();
		// initialize cache if needed
		TableCache<T> cache = getCache();
		cache.getOptional(reader, new Object[0]);
		try {
			writer.persist(object);
			cache.cache(reader, object);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
		cacheChange();
	}
		
	@Override
	// note that this will not fill back auto increment columns.
	public void persist(List<T> objects)  {
		preventIfChild();
		// initialize cache if needed
		TableCache<T> cache = getCache();
		cache.getOptional(reader, new Object[0]);		
		try {
			writer.persist(objects);
			for (T each : objects) {
				cache.cache(reader,each);
			}
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
		cacheChange();
	}
	
	@Override
	public void update(T object)  {
		update(object,sqlGenerator.getTable().getStandardColumns());
	}
	
	@Override
	public void update(T object , String... fieldNames)  {
		update(object,getUpdateColumns(fieldNames));
	}
	
	private List<ColumnImpl> getUpdateColumns(String[] fieldNames) {
		List<ColumnImpl> columns = new ArrayList<>(fieldNames.length);
		for (String fieldName : fieldNames) {
			ColumnImpl column = getTable().getColumnForField(fieldName);
			if (column.isPrimaryKeyColumn() || column.isVersion() || column.hasUpdateValue() || column.isDiscriminator()) {
				throw new IllegalArgumentException("Cannot update special column");
			} else {
				columns.add(column);
			}
		}
		return columns;
	}
	
	private void update(T object,List<ColumnImpl> columns) {
		try {
			writer.update(object,columns);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
		cacheChange();
	}
	
	
	@Override
	public void update(List<T> objects)  {
		update(objects,sqlGenerator.getTable().getStandardColumns());
	}
	
	@Override
	public void update(List<T> objects , String... fieldNames)  {
		update(objects,getUpdateColumns(fieldNames));
	}
	
	private void update(List<T> objects,List<ColumnImpl> columns){
		try {
			writer.update(objects,columns);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		} 	
		cacheChange();
	}
	
	@Override
	public void remove(T object )  {
		preventIfChild();
		try {
			writer.remove(object);
			getCache().remove(object);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
		cacheChange();
	}
	
	@Override
	public void remove(List<T> objects )  {
		preventIfChild();
		try {
			writer.remove(objects);
			for (T each : objects) {
				getCache().remove(each);
			}
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
		cacheChange();
	}
	
	@Override
	public QueryExecutor<T> with(DataMapper<?>... dataMappers) {
		QueryExecutorImpl <T> result = new QueryExecutorImpl<>(this);
		for (DataMapper<?> each : dataMappers) {
			result.add(each);
		}
		return result;
	}
	
	public Object convert(ColumnImpl column , String value) {
		if (column.isEnum()) {
			return getEnum(column,value);
		} else {
			return column.convert(value);
		}
	}
	
	private Object getEnum(Column column, String value) {
		return mapperType.getEnum(column.getFieldName(), value);			
	}
	
	private List<ColumnImpl> getColumns() {
		return getSqlGenerator().getColumns();
	}
		
	private int getIndex(ColumnImpl column) {
		int i = getColumns().indexOf(column);
		if (i < 0) {
			throw new IllegalArgumentException(column.toString());
		}
		return i;
	}
	
	private Object getValue(ColumnImpl column , ResultSet rs , int startIndex ) throws SQLException {
		int offset = getIndex(column);
		return column.convertFromDb(rs, startIndex + offset);
	}
	
	public Object getPrimaryKey(ResultSet rs , int index) throws SQLException {
		List<ColumnImpl> primaryKeyColumns = getSqlGenerator().getPrimaryKeyColumns();
		switch (primaryKeyColumns.size()) {
			case 0:
				return null;
			
			case 1: {
				Object result = getValue(primaryKeyColumns.get(0),rs,index);
				return rs.wasNull() ? null : result;
			}
		
			default: {
				Object[] values = new Object[primaryKeyColumns.size()];
				for (int i = 0 ; i < values.length ; i++) {
					values[i] = getValue(primaryKeyColumns.get(i),rs,index);
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
			if (each.getFieldName().equals(name)) {
                return each;
            }
		}
		return null;
	}
	
	public Class<?> getType(String fieldName) {
		return mapperType.getType(fieldName);
	}
	
	public List<T> select(Condition condition, String ... orderBy) {
		return with().select(condition, orderBy, false,null);
	}
	
	DataMapperType getMapperType() {
		return mapperType;
	}
	
	public DataMapperWriter<T> getWriter() {
		return writer;
	}
	
	public T newInstance() {
		return mapperType.newInstance((String) null);
	}

	@Override
	public Object[] getPrimaryKey(T object) {
		return getTable().getPrimaryKey(object);
	}
	
	@Override
	public Optional<T> getEager(Object ... key) {
		return getTable().<T>getQuery().getOptional(key);
	}

	@Override
	public Object getAttribute(Object target, String fieldName) {
		return DomainMapper.FIELDSTRICT.get(target, fieldName);
	}

	public boolean isAutoId() {
		return getTable().isAutoId();
	}
}
