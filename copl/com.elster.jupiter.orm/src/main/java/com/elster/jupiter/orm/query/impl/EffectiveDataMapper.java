package com.elster.jupiter.orm.query.impl;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.elster.jupiter.orm.NotUniqueException;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.DomainMapper;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.orm.impl.KeyValue;
import com.elster.jupiter.util.sql.SqlBuilder;

public class EffectiveDataMapper<T> extends JoinDataMapper<T> {
	private final ForeignKeyConstraintImpl constraint;
	private Map<Object, List<T>> targetCache;
	
	public EffectiveDataMapper(DataMapperImpl<T> dataMapper,ForeignKeyConstraintImpl constraint, String alias) {
		super(dataMapper, alias);
		this.constraint = constraint;
	}

	@Override
	void clearCache() {
		super.clearCache();
		targetCache = new HashMap<>();
	}
	
	@Override
	T set(Object target, ResultSet rs, int index) throws SQLException {
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
	boolean appendFromClause(SqlBuilder builder, String parentAlias, boolean isMarked, boolean forceOuterJoin) {
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
	void completeFind() {
		super.completeFind();
		for (Map.Entry<Object,List<T>> entry : targetCache.entrySet()) {
			if (constraint.isOneToOne()) {
				if (entry.getValue().size() > 1) {
					throw new NotUniqueException(constraint.getReverseFieldName());
				}
				if (entry.getValue().size() == 1) {
					constraint.setReverseField(entry.getKey(), entry.getValue().get(0));
				}
			} else {
				List<T> values = entry.getValue();
				constraint.setReverseField(entry.getKey(), values);
			}
		}
	}

	
	@Override
	String getName() {
		return constraint.getReverseFieldName();
	}

	@Override
	boolean canRestrict() {
		return true;
	}
	
	@Override
	boolean isChild() {
		return true;
	}

	@Override
	public boolean isReachable() {
		return constraint.getReverseFieldName() != null;
	}
}
