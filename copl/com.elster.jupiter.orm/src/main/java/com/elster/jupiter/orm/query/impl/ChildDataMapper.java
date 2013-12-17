package com.elster.jupiter.orm.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.orm.associations.impl.ManagedPersistentList;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.DomainMapper;
import com.elster.jupiter.util.sql.SqlBuilder;

public class ChildDataMapper<T> extends JoinDataMapper <T> {
	private final ForeignKeyConstraint constraint;
	private Map<Object, List<T>> targetCache;
	
	public ChildDataMapper(DataMapperImpl<T> dataMapper,ForeignKeyConstraint constraint, String alias) {
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
		Object key = getMapper().getPrimaryKey(rs, index);
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
		List<Column> primaryKeyColumns = constraint.getReferencedTable().getPrimaryKeyColumns();
		List<Column> foreignKeyColumns = constraint.getColumns();
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
		String fieldName = constraint.getReverseFieldName();
		if (fieldName != null) {		
			for (Map.Entry<Object,List<T>> entry : targetCache.entrySet()) {
				if (constraint.isOneToOne()) {
					if (entry.getValue().size() > 1) {
						throw new NotUniqueException(fieldName);
					}
					if (entry.getValue().size() == 1) {
						DomainMapper.FIELDSTRICT.set(entry.getKey(), fieldName , entry.getValue().get(0));
					}
				} else {
					sort(entry.getValue());
					List<T> values = entry.getValue();
					if (constraint.isComposition()) {
						values = new ManagedPersistentList<>(constraint, getMapper(), entry.getKey(),values);
					}
					DomainMapper.FIELDSTRICT.set(entry.getKey(), fieldName , values);
				}
			}
		}
	}

	private void sort(List<?> list) {
		String fieldName = constraint.getReverseOrderFieldName();
		if (fieldName != null) {
			Collections.sort(list, new FieldComparator(fieldName));
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

	private static class FieldComparator implements Comparator<Object> {
		private final String sortField;
		
		FieldComparator(String sortField) {
			this.sortField = sortField;
		}
		
		@Override
		public int compare(Object o1, Object o2) {
			return getValue(o1).compareTo(getValue(o2));			
		}
		
		@SuppressWarnings("unchecked")
		private Comparable<Object> getValue(Object o) {
			return (Comparable<Object>) DomainMapper.FIELDSTRICT.get(o, sortField);
		}
		
	}
}
