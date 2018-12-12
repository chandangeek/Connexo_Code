/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.DomainMapper;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.orm.impl.KeyValue;
import com.elster.jupiter.util.sql.SqlBuilder;

abstract public class AbstractChildDataMapper<T> extends JoinDataMapper <T> {
	private final ForeignKeyConstraintImpl constraint;
	private Map<Object, List<T>> targetCache;
	
	public AbstractChildDataMapper(DataMapperImpl<T> dataMapper,ForeignKeyConstraintImpl constraint, String alias) {
		super(dataMapper, alias);
		this.constraint = constraint;
	}

	@Override
	void clearCache() {
		super.clearCache();
		targetCache = new HashMap<>();
	}
	
	ForeignKeyConstraintImpl getConstraint() {
		return constraint;
	}
	
	Map<Object, List<T>> getTargetCache() {
		return targetCache;
	}
	
	@Override
	final T set(Object target, ResultSet rs, int index) throws SQLException {
		if (constraint.getReverseFieldName() != null) {
			addTarget(target);
		}
		T value = null;
		KeyValue key = getMapper().getPrimaryKey(rs, index);
		if (key != null) {
			value = get(key);
			if (value == null) {	
				value = getMapper().construct(rs,index);
				put(key, value);
				if (constraint.getReverseFieldName() !=  null) {
					addTargetEntry(target,value);
				}
			}
		}
		if (constraint.getFieldName() != null) {
			DomainMapper.FIELDSTRICT.set(value,constraint.getFieldName(),target);
		}		
		return value;
	}
		
	private void addTarget(Object target) {
		List<T> values = targetCache.get(target);
		if (values == null) {
			values = new ArrayList<>();
			targetCache.put(target, values);
		}				
	}
	
	private void addTargetEntry(Object target, T value) {
		List<T> values = targetCache.get(target);
		values.add(value);
	}


	@Override
	final boolean appendFromClause(SqlBuilder builder, String parentAlias, boolean isMarked, boolean forceOuterJoin) {
		builder.append(" LEFT JOIN ");
		appendTable(builder);
		builder.append(" ON ");
		builder.openBracket();
		List<ColumnImpl> primaryKeyColumns = constraint.getReferencedTable().getPrimaryKeyColumns();
		List<ColumnImpl> foreignKeyColumns = constraint.getColumns();
		String separator = "";
		for ( int i = 0 ; i < primaryKeyColumns.size() ; i++) {
			builder.append(separator);
			builder.append(primaryKeyColumns.get(i).getName(parentAlias));			
			builder.append(" = ");
			builder.append(foreignKeyColumns.get(i).getName(getAlias()));			
			separator = " AND ";
		}
		builder.closeBracketSpace();
		return true;
	}
	
	@Override
	final String getName() {
		return constraint.getReverseFieldName();
	}

	
}
