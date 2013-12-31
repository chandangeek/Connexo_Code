package com.elster.jupiter.orm.query.impl;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.sql.SqlFragment;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;

import java.sql.SQLException;
import java.util.*;


public class QueryExecutorImpl<T> implements QueryExecutor<T> {	

	private final JoinTreeNode<T> root;
	private final AliasFactory aliasFactory = new AliasFactory();
	private Condition restriction = Condition.TRUE;
	private UtcInstant effectiveInstant;
	
	public QueryExecutorImpl(DataMapperImpl<T> mapper) {
		RootDataMapper<T> rootDataMapper = new RootDataMapper<>(mapper);
		aliasFactory.setBase(rootDataMapper.getAlias());
		aliasFactory.getAlias();
		this.root = new JoinTreeNode<>(rootDataMapper);				
	}
    
	public <R> void add(DataMapperImpl<R> newMapper) {
		aliasFactory.setBase(newMapper.getAlias());
		boolean result = root.addMapper(newMapper , aliasFactory);
		if (!result) {
			throw new IllegalArgumentException("No referential key match for " + newMapper.getTable().getName());
		}
	}	
	
	@Override
	public List<T> select(Condition condition, String[] orderBy , boolean eager , String[] exceptions) {
		return select(condition, orderBy, eager, exceptions, 0,0);
	}
	
	@Override
	public List<T> select(Condition condition, String[] orderBy , boolean eager , String[] exceptions , int from , int to) {
		try {
			return new JoinExecutor<>(root.copy(), getEffectiveDate() , from,to).select(restriction.and(condition),orderBy , eager, exceptions);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
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
		return new JoinExecutor<>(root.copy(),getEffectiveDate()).getSqlBuilder(condition, fieldNames);		
	}
	
	public Object convert(String fieldName, String value) {
		ColumnImpl column = root.getColumnForField(fieldName);
		if (column != null) {
			return column.convert(value);
		}
		throw new IllegalArgumentException("No mapper or column for " + fieldName);
	}

	@Override
	public Optional<T> get(Object[] key , boolean eager , String[] exceptions) {
		List<ColumnImpl> primaryKeyColumns = this.root.getTable().getPrimaryKeyColumns();
		if (primaryKeyColumns.size() != key.length) {
			throw new IllegalArgumentException("Key mismatch");
		}
		Condition condition = Condition.TRUE;
		int i = 0;
		for (ColumnImpl column : primaryKeyColumns) {
			condition = condition.and(Operator.EQUAL.compare(column.getFieldName(),key[i++]));
		}
		List<T> result = this.select(condition, null , eager , exceptions);
		if (result.size() > 1) {
			throw new NotUniqueException(Arrays.toString(key));
		}
		return result.isEmpty() ? Optional.<T>absent() : Optional.of(result.get(0));
	}

	@Override
	public List<String> getQueryFieldNames() {
		return root.getQueryFields();	
	}

	@Override
	public void setRestriction(Condition condition) {
		restriction = restriction.and(condition);
	}

	@Override
	public Optional<T> getOptional(Object... values) {
		return get(values,true,new String[0]);
	}

	@Override
	public T getExisting(Object... values) {
		return getOptional(values).get();
	}

	@Override
	public List<T> select(Condition condition, String... orderBy) {
		return select(condition, orderBy, true,new String[0]);
	}

	@Override
	public Date getEffectiveDate() {
		if (effectiveInstant == null) {
			effectiveInstant = new UtcInstant(this.root.getTable().getDataModel().getClock().now());
		}
		return effectiveInstant.toDate();
	}

	@Override
	public void setEffectiveDate(Date date) {
		this.effectiveInstant = new UtcInstant(date);
	}
}


