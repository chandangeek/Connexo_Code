package com.elster.jupiter.orm.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.Table;
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
	
	DataMapperImpl(Class<T> api, DataMapperType mapperType ,  Table table) {
		this.sqlGenerator = new TableSqlGenerator((TableImpl) table);
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
	public Table getTable() {
		return sqlGenerator.getTable();
	}
	
	public TableSqlGenerator getSqlGenerator() {
		return sqlGenerator;
	}
	

	@Override
    Optional<T> findByPrimaryKey(Object[] values) {
		try {
			return reader.findByPrimaryKey(values);
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
	
	
	@Override
	public void persist(T object)  {
		try {
			writer.persist(object);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
		
	@Override
	// note that this will not fill back auto increment columns.
	public void persist(List<T> objects)  {
		try {
			writer.persist(objects);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
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
			Column column = getTable().getColumnForField(fieldName);
			if (column.isPrimaryKeyColumn() || column.isVersion() || column.hasUpdateValue() || column.isDiscriminator()) {
				throw new IllegalArgumentException("Cannot update special column");
			} else {
				columns[i++] = column;
			}
		}
		return columns;
	}
	
	private void update(T object,Column[] columns) {
		try {
			writer.update(object,columns);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
	
	
	@Override
	public void update(List<T> objects)  {
		update(objects,sqlGenerator.getTable().getStandardColumns());
	}
	
	@Override
	public void update(List<T> objects , String... fieldNames)  {
		update(objects,getUpdateColumns(fieldNames));
	}
	
	private void update(List<T> objects,Column[] columns){
		try {
			writer.update(objects,columns);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		} 	
	}
	
	@Override
	public void remove(T object )  {
		try {
			writer.remove(object);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
	
	@Override
	public void remove(List<T> objects )  {
		try {
			writer.remove(objects);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
	
	@Override
	public QueryExecutor<T> with(DataMapper<?>... dataMappers) {
		QueryExecutorImpl <T> result = new QueryExecutorImpl<>(this);
		for (DataMapper<?> each : dataMappers) {
			result.add(each);
		}
		return result;
	}
	
	public Object convert(Column column , String value) {
		if (column.isEnum()) {
			return getEnum(column,value);
		} else {
			return ((ColumnImpl) column).convert(value);
		}
	}
	
	private Object getEnum(Column column, String value) {
		return mapperType.getEnum(column.getFieldName(), value);			
	}
	
	private ColumnImpl[] getColumns() {
		return getSqlGenerator().getColumns();
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
	
	public Object getPrimaryKey(ResultSet rs , int index) throws SQLException {
		Column[] primaryKeyColumns = getSqlGenerator().getPrimaryKeyColumns();
		switch (primaryKeyColumns.length) {
			case 0:
				return null;
			
			case 1: {
				Object result = getValue(primaryKeyColumns[0],rs,index);
				return rs.wasNull() ? null : result;
			}
		
			default: {
				Object[] values = new Object[primaryKeyColumns.length];
				for (int i = 0 ; i < primaryKeyColumns.length ; i++) {
					values[i] = getValue(primaryKeyColumns[i],rs,index);
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
}
