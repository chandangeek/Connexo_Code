package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.Finder;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.fields.impl.FieldMapping;
import com.elster.jupiter.orm.query.impl.QueryExecutorImpl;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		"YES",
		"FILE"};
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
				return Optional.empty();
			}
		}
		try {
			Optional<T> result;
			if (getTable().isCached()) {
				result = getEager(keyValue.getKey());
			} else {
				result = reader.findByPrimaryKey(keyValue);
			}
			result.ifPresent( t -> cache.put(keyValue, t));
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

	@Override
	public Optional<T> lockObjectIfVersion(long version, Object... values) {
		try {
			return Optional.ofNullable(reader.lock(KeyValue.of(values), version));
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}

	@Override
	public Optional<T> lockNoWait(Object... values)  {
		try {
			return Optional.ofNullable(reader.lockNoWait(KeyValue.of(values)));
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
	
	@Override
	public List<T> find(String[] fieldNames, Object[] values, Order... orders) {
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

    @Override
    public JournalFinderImpl at(Instant instant) {
    	if (!table.hasJournal() || !table.getColumn(TableImpl.MODTIMECOLUMNAME).isPresent()) {
    		throw new IllegalStateException();
    	}
    	return new JournalFinderImpl(Objects.requireNonNull(instant));        
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
		// do not cache object at this time, as tx may rollback
		try {
			writer.persist(object);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
		
	@Override
	// note that this will not fill back auto increment columns.
	public void persist(List<T> objects)  {
		if (objects.isEmpty()) {
			return;
		}
		if (objects.size() == 1) {
			persist(objects.get(0));
			return;
		}
		preventIfChild();
		// do not cache object at this time, as tx may rollback
		try {
			writer.persist(objects);
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
			FieldMapping mapping = getTable().getFieldMapping(fieldName);
			if (mapping == null) {
				throw new IllegalArgumentException("No mapping for field " + fieldName);
			}
			List<ColumnImpl> cols = mapping.getColumns();
			if (cols.isEmpty()) {
				throw new IllegalArgumentException("No columns found in mapping for field " + fieldName);
			}
			for (ColumnImpl column : cols) {
				if (column.isPrimaryKeyColumn() || column.isVersion() || column.hasUpdateValue() || column.isDiscriminator()) {
					throw new IllegalArgumentException("Cannot update special column");
				} else {
					columns.add(column);
				}
			}
		}
		return columns;
	}
	
	private void update(T object,List<ColumnImpl> columns) {
		//  remove object from cache, as we do not know if tx will commit or rollback
		getCache().remove(object);
		try {
			writer.update(object,columns);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
	
	@Override
	public void update(List<T> objects , String... fieldNames)  {
		if (objects.isEmpty()) {
			return;
		}
		if (objects.size() == 1) {
			update(objects.get(0),fieldNames);
			return;
		}
		update(objects,getUpdateColumns(fieldNames));
	}
	

	private void update(List<T> objects,List<ColumnImpl> columns){
		//remove objects from cache, as we do not know if tx will commit or rollback
		for (T each : objects) {
			getCache().remove(each);
		}
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
	public void remove(List<? extends T> objects )  {
		preventIfChild();
		if (objects.isEmpty()) {
			return;
		}
		try {
			writer.remove(objects);
			for (T each : objects) {
				getCache().remove(each);
			}
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
	
	QueryExecutorImpl<T> with(DataMapper<?>... dataMappers) {
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
	
	public QueryExecutorImpl<T> query(Class<?>... eagers) {		
		return getDataModel().query(this, eagers);
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

	@Override
	public Fetcher<T> fetcher(SqlBuilder builder) {
		try {
			return reader.fetcher(builder);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}

	@Override
	public SqlBuilder builder(String alias, String... hints) {
		return new SqlBuilder(getSqlGenerator().getSelectFromClause(alias, hints));
	}
	
	public DataModelImpl getDataModel() {
		return getTable().getDataModel();
	}
	
	@Override
	public Optional<JournalEntry<T>> getJournalEntry (Instant instant, Object... values)  {		
		return getJournal(values).stream()
			.filter(journalEntry -> instant.isBefore(journalEntry.getJournalTime()))
			.reduce( (previous, current) -> current);					
	}
	
	class JournalFinderImpl implements Finder.JournalFinder<T> {
		
		private final Instant instant;
		
		JournalFinderImpl(Instant instant) {
			this.instant = instant;
		}

		@Override
		public List<JournalEntry<T>> find(Map<String, Object> valueMap) {
	        try {
	        	Stream<JournalEntry<T>> current = reader.find(instant, valueMap).stream().map(tuple -> new JournalEntry<>(tuple));
	        	Stream<JournalEntry<T>> old = reader.findJournals(instant, valueMap).stream();
	        	return Stream.concat(current, old).collect(Collectors.toList());
	        } catch (SQLException e) {
	            throw new UnderlyingSQLFailedException(e);
	        }
		}
		
	}
}
