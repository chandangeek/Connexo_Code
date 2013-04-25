package com.elster.jupiter.orm.impl;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.elster.jupiter.conditions.*;
import com.elster.jupiter.orm.*;
import com.elster.jupiter.sql.util.SqlFragment;


public class QueryExecutorImpl<T> implements QueryExecutor<T> {	

	private final JoinTreeNode<T> root;
	private final Set<String> aliases = new HashSet<>();
	
	public QueryExecutorImpl(DataMapperImpl<T, ? extends T> mapper) {
		RootDataMapper<T> rootDataMapper = new RootDataMapper<>(mapper);
		this.root = new JoinTreeNode<>(rootDataMapper);
		this.aliases.add(rootDataMapper.getAlias());		
	}
    
	@SuppressWarnings("unchecked")
	@Override 
	public <R> void add(DataMapper<R> dataMapper) {
		DataMapperImpl<R, ? extends R> newMapper = (DataMapperImpl<R,? extends R>) dataMapper;
		String alias = newMapper.getAlias();
		String base = alias;
		for (int i = 2 ;  aliases.contains(alias) ; i++) {
			alias = base + i;
		}
		aliases.add(alias);
		boolean result = root.addMapper((DataMapperImpl<R,? extends R>) dataMapper , alias);
		if (!result) {
			throw new IllegalArgumentException("No referential key match for " + dataMapper.getTable().getName());
		}
	}	
	
	@Override
	public List<T> select(Condition condition, String[] orderBy , boolean eager , String[] exceptions) {
		return select(condition, orderBy, eager, exceptions, 0,0);
	}
	
	@Override
	public List<T> select(Condition condition, String[] orderBy , boolean eager , String[] exceptions , int from , int to) {
		try {
			return new JoinExecutor<>(root.copy(),from,to).select(condition,orderBy , eager, exceptions);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	

	@Override
	public boolean hasField(String fieldName) {
		return root.hasWhereField(fieldName);
	}
	
	@Override
	public Class<?> getType(String fieldName) {
		return root.getType(fieldName);
	}

	@Override
	public SqlFragment asFragment(Condition condition, String[] fieldNames) {
		return new JoinExecutor<>(root.copy()).getSqlBuilder(condition, fieldNames);		
	}
	
	public Object convert(String fieldName, String value) {
		DataMapperImpl<?,?>  mapper = root.getDataMapperForField(fieldName);
		if (mapper != null) {
			Column column = root.getColumnForField(fieldName);
				if (column != null) {
					return mapper.convert(column,value);
				}
		}
		throw new IllegalArgumentException("No mapper or column for " + fieldName);
	}

	@Override
	public T get(Object[] key , boolean eager , String[] exceptions) {
		List<Column> primaryKeyColumns = this.root.getTable().getPrimaryKeyColumns();
		if (primaryKeyColumns.size() != key.length) {
			throw new IllegalArgumentException("Key mismatch");
		}
		Condition condition = Condition.TRUE;
		int i = 0;
		for (Column column : primaryKeyColumns) {
			condition = condition.and(Operator.EQUAL.compare(column.getFieldName(),key[i++]));
		}
		List<T> result = this.select(condition, null , eager , exceptions);
		return result.isEmpty() ? null : result.get(0);
	}

	@Override
	public List<String> getQueryFieldNames() {		
		return root.getQueryFields();	
	}
}


