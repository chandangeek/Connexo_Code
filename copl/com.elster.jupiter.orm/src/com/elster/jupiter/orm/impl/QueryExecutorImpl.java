package com.elster.jupiter.orm.impl;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.elster.jupiter.conditions.*;
import com.elster.jupiter.orm.*;
import com.elster.jupiter.sql.util.SqlBuilder;


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
	public List<T> select(Condition condition,String[] includes) {
		return select(condition,0,0,includes);
	}

	@Override
	public List<T> select(Condition condition, int from , int to , String[] includes) {
		try {
			return new JoinExecutor<>(root.copy(),from,to).select(condition,false,includes);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	@Override
	public List<T> eagerSelect(Condition condition, String[] excludes) {
		return eagerSelect(condition, 0 , 0 , excludes);
	}

	public List<T> eagerSelect(Condition condition, int from , int to,  String[] excludes) {
		try {
			return new JoinExecutor<>(root.copy(),from,to).select(condition,true, excludes);
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		}
	}
	@Override
	public boolean hasField(String fieldName) {
		return root.hasWhereField(fieldName);
	}

	@Override
	public Club toClub(Condition condition, String[] fieldNames) {
		return new SubQueryExecutor(this,condition,fieldNames);
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

	public SqlBuilder getSqlBuilder(Condition condition, String[] fieldNames) {
		return new JoinExecutor<>(root.copy()).getSqlBuilder(condition, fieldNames);		
	}

	@Override
	public T get(Object[] key) {
		List<Column> primaryKeyColumns = this.root.getTable().getPrimaryKeyColumns();
		if (primaryKeyColumns.size() != key.length) {
			throw new IllegalArgumentException("Key mismatch");
		}
		Condition condition = Condition.TRUE;
		int i = 0;
		for (Column column : primaryKeyColumns) {
			condition = condition.and(Operator.EQUAL.compare(column.getFieldName(),key[i++]));
		}
		List<T> result = this.eagerSelect(condition, new String[0]);
		return result.isEmpty() ? null : result.get(0);
	}
}


