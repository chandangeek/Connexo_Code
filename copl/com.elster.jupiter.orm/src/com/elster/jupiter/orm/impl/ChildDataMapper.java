package com.elster.jupiter.orm.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.TableConstraint;
import com.elster.jupiter.sql.SqlBuilder;

public class ChildDataMapper<T> extends JoinDataMapper <T> {
	final private TableConstraint constraint;
	private Map<Object , List<Object>> targetCache;
	
	public ChildDataMapper(DataMapperImpl<T,? extends T> dataMapper,TableConstraint constraint, String alias) {
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
		T value = null;
		Object key = getMapper().getPrimaryKey(rs, index);
		if (key != null) {
			value = get(key);
			if (value == null) {	
				value = getMapper().construct(rs,index);
				put(key, value);
				if (constraint.getReverseFieldName() !=  null) {
					add(target,value);
				}
			}
		}
		if (constraint.getFieldName() != null) {
			new FieldMapper().set(value,constraint.getFieldName(),target);
		}		
		return value;
	}
		
	private void add(Object target, Object value) {
		List<Object> values = targetCache.get(target);
		if (values == null) {
			values = new ArrayList<>();
			targetCache.put(target, values);
		}
		values.add(value);
	}


	@Override
	boolean appendFromClause(SqlBuilder builder, String parentAlias, boolean isMarked, boolean forceOuterJoin) {
		builder.append(" LEFT JOIN ");
		appendTable(builder);
		builder.append(" ON ");
		builder.openBracket();
		Column[] primaryKeyColumns = constraint.getTable().getPrimaryKeyColumns();
		List<Column> foreignKeyColumns = constraint.getColumns();
		String separator = "";
		for ( int i = 0 ; i < primaryKeyColumns.length ; i++) {
			builder.append(separator);
			builder.append(primaryKeyColumns[i].getName(parentAlias));			
			builder.append(" = ");
			builder.append(foreignKeyColumns.get(i).getName(getAlias()));			
			separator = " AND ";
		}
		builder.closeBracketSpace();
		return true;
	}

	@Override
	void completeFind() {
		String fieldName = constraint.getReverseFieldName();
		if (fieldName != null) {
			FieldMapper fieldMapper = new FieldMapper();
			for (Map.Entry<Object,List<Object>> entry : targetCache.entrySet()) {
				fieldMapper.set(entry.getKey(), fieldName , entry.getValue());
			}
		}
	}

	@Override
	String getName() {
		return constraint.getReverseFieldName();
	}

	@Override
	boolean canRestrict() {
		return false;
	}
	
	@Override
	boolean isChild() {
		return true;
	}


}
