package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.query.impl.QueryExecutorImpl;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.base.Optional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
	private final Class<T> api;
	private final TableImpl<? super T> table;
	private final TableSqlGenerator sqlGenerator;
	private final String alias;
	private final DataMapperReader<T> reader;
	private final DataMapperWriter<T> writer;
	
	DataMapperImpl(Class<T> api, TableImpl<? super T> table) {
		this.api = api;
		this.table = table;
		this.sqlGenerator = new TableSqlGenerator(table);
		this.alias = createAlias(api.getName());
		this.reader = new DataMapperReader<>(this);
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
	
	public Class<T> getApi() {
		return api;
	}
	
	public TableImpl<? super T> getTable() {
		return table;
	}
	
	public TableSqlGenerator getSqlGenerator() {
		return sqlGenerator;
	}
	
	private TableCache<? super T> getCache() {
		return getTable().getCache();
	}


	@Override
    Optional<T> findByPrimaryKey(KeyValue keyValue) {
		TableCache<? super T> cache = getCache();
		Object cacheVersion = cache.get(keyValue);
		if (cacheVersion != null) {
			if (api.isInstance(cacheVersion)) {
				return Optional.of(api.cast(cacheVersion)); 
			} else {
				return Optional.absent();
			}
		}
		try {
			Optional<T> result = reader.findByPrimaryKey(keyValue);
			if (result.isPresent()) {
				cache.put(keyValue, result.get());
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
			return reader.lock(KeyValue.of(values));
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<T> find(String[] fieldNames, Object[] values, Order... orders) {
		if (fieldNames == null) {
			List<? super T> candidates = getCache().find();
			if (candidates != null) {
				if (needsRestriction()) {
					List<T> result = new ArrayList<>();
					for (Object candidate : candidates) {
						if (api.isInstance(candidate)) {
							result.add(api.cast(candidate));
						}
					}
					return result;
				} else {
					return (List<T>) candidates;
				}
			}
		}
		try {
			return reader.find(fieldNames,values,orders);
		} catch(SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}

    @Override
    public List<JournalEntry<T>> getJournal(Object... values) {
        try {
            return reader.findJournals(KeyValue.of(values));
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
	
	@Override
	public void persist(T object)  {
		preventIfChild();
		// initialize cache if needed
		TableCache<? super T> cache = getCache();
		cache.start();
		try {
			writer.persist(object);
			cache.cache(object);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
		
	@Override
	// note that this will not fill back auto increment columns.
	public void persist(List<T> objects)  {
		preventIfChild();
		// initialize cache if needed
		TableCache<? super T> cache = getCache();
		cache.start();		
		try {
			writer.persist(objects);
			for (T each : objects) {
				cache.cache(each);
			}
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
	
	@Override
	public void update(T object , String... fieldNames)  {
		update(object,getUpdateColumns(fieldNames));
	}
	
	public void touch(T object) {
		if (table.getAutoUpdateColumns().isEmpty()) { 
			throw new IllegalStateException("Nothing to touch");
		} else {
			update(object, Collections.<ColumnImpl>emptyList());
		}
	}
	
	private List<ColumnImpl> getUpdateColumns(String[] fieldNames) {
		if (fieldNames.length == 0) {
			return table.getStandardColumns();
		} 
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
	}
	
	@Override
	public QueryExecutorImpl<T> with(DataMapper<?>... dataMappers) {
		QueryExecutorImpl <T> result = new QueryExecutorImpl<>(this);
		for (DataMapper<?> each : dataMappers) {
			DataMapperImpl<?> dataMapper = (DataMapperImpl<?>) each;
			if (dataMapper.needsRestriction()) {
				throw new IllegalStateException("No Restriction allowed on additional mappers: " + dataMapper);
			} else {
				result.add(dataMapper);
			}
		}
		result.setRestriction(getMapperType().condition(getApi()));
		return result;
	}
	
	public Object convert(ColumnImpl column , String value) {
		return column.convert(value);
	}
	
	private List<ColumnImpl> getColumns() {
		return getTable().getColumns();
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
	
	public KeyValue getPrimaryKey(ResultSet rs , int index) throws SQLException {
		List<ColumnImpl> primaryKeyColumns = getTable().getPrimaryKeyColumns();
		if  (primaryKeyColumns.isEmpty()) {
			return null;
		} else {
			Object[] values = new Object[primaryKeyColumns.size()];
			for (int i = 0 ; i < values.length ; i++) {
				values[i] = getValue(primaryKeyColumns.get(i),rs,index);
				if (rs.wasNull()) {
					return null;
				}
			}
			return KeyValue.of(values);				
		}
	}
	
	public Class<?> getType(String fieldName) {
		return getMapperType().getType(fieldName);
	}
	
	@Override
	@Deprecated
	public List<T> select(Condition condition, String order, String ... orders) {
		return with().select(condition, Order.from(order,orders));
	}

	@Override
	public List<T> select(Condition condition, Order ... orders) {
		return with().select(condition, orders);
	}
	
	DataMapperType <? super T> getMapperType() {
		return table.getMapperType();
	}
	
	public DataMapperWriter<T> getWriter() {
		return writer;
	}
	
	@Override
	public Optional<T> getEager(Object ... key) {
		return (Optional<T>) getTable().getQuery(getApi()).getOptional(key);
	}

	@Override
	public Object getAttribute(Object target, String fieldName) {
		return DomainMapper.FIELDSTRICT.get(target, fieldName);
	}
	
	private boolean needsRestriction() {
		return getMapperType().needsRestriction(api);
	}
	
	public T cast (Object object) {
		return api.cast(object);
	}

	@Override
	@Deprecated
	public List<T> find(String fieldName, Object value, String order) {
		return find(fieldName,value,Order.ascending(order));
	}

	@Override
	@Deprecated
	public List<T> find(String fieldName1, Object value1, String fieldName2, Object value2, String order) {
		return find(fieldName1, value1, fieldName2, value2, Order.ascending(order));
	}

	@Override
	@Deprecated
	public List<T> find(String[] fieldNames, Object[] values, String order,String... orders) {
		return find(fieldNames,values,Order.from(order,orders));
	}

	@Override
	@Deprecated
	public List<T> find(Map<String, Object> valueMap, String order,String... orders) {
		return find(valueMap,Order.from(order,orders));
	}
}
