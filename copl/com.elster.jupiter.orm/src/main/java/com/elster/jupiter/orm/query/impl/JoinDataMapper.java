/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.query.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.orm.fields.impl.FieldMapping;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.ForeignKeyConstraintImpl;
import com.elster.jupiter.orm.impl.KeyValue;
import com.elster.jupiter.orm.impl.TableImpl;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract class JoinDataMapper<T> {
	private final DataMapperImpl<T> dataMapper;
	private final String alias;
	private Map<KeyValue,T> cache;

	JoinDataMapper(DataMapperImpl<T> dataMapper, String alias) {
		this.dataMapper = dataMapper;
		this.alias = alias;
	}

	final DataMapperImpl<T> getMapper() {
		return dataMapper;
	}

	final String getAlias() {
		return alias;
	}

	final TableImpl<? super T> getTable() {
		return getMapper().getTable();
	}

	final <R> List<JoinDataMapper<R>> wrap(DataMapperImpl<R> newMapper , AliasFactory aliasFactory) {
		List<JoinDataMapper<R>> result = new ArrayList<>();
		for (ForeignKeyConstraintImpl constraint : getTable().getForeignKeyConstraints()) {
			if (newMapper.getTable().equals(constraint.getReferencedTable())) {
				result.add(new ParentDataMapper<>(newMapper , constraint , aliasFactory.getAlias()));
				return result;
			}
		}
		for (ForeignKeyConstraintImpl constraint : newMapper.getTable().getForeignKeyConstraints()) {
			if (getTable().equals(constraint.getReferencedTable())) {
				if (constraint.isTemporal()) {
					result.add(new EffectiveDataMapper<>(newMapper, constraint, aliasFactory.getAlias()));
				} else {
					result.add(new ChildDataMapper<>(newMapper , constraint , aliasFactory.getAlias()));
				}
			}
		}
		return result;
	}

	final List<ColumnAndAlias> getColumnAndAliases(String fieldName) {
		FieldMapping mapping = getTable().getFieldMapping(fieldName);
		return mapping == null ? null : mapping.getColumns().stream()
				.map(column -> new ColumnAndAlias(column, getAlias()))
				.collect(Collectors.toList());
	}

	final ColumnAndAlias getColumnAndAlias(String fieldName) {
		ColumnImpl column = getTable().getColumnForField(fieldName);
		return column == null ? null : new ColumnAndAlias(column,getAlias());
	}

	final SqlFragment getFragment(Comparison comparison , String fieldName)   {
		FieldMapping mapping = getTable().getFieldMapping(fieldName);
		return mapping == null ? null : mapping.asComparisonFragment(comparison, getAlias());
	}

	final SqlFragment getFragment(Contains contains, String fieldName)   {
		FieldMapping mapping = getTable().getFieldMapping(fieldName);
		return mapping == null ? null : mapping.asContainsFragment(contains, getAlias());
	}

	boolean hasField(String fieldName)  {
		return getTable().getFieldMapping(fieldName) != null;
	}

	final DataMapperImpl<T> getDataMapperForField(String fieldName) {
		Column column = getTable().getColumnForField(fieldName);
		if (column != null) {
			return getMapper();
		}
		return null;
	}

	final String appendColumns(SqlBuilder builder , String separator) {
		for (Column each : this.getRealColumns()) {
			builder.append(separator);
			builder.append(each.getName(alias));
			builder.space();
			String uniqueName = alias + each.getName();
			if (uniqueName.length() > 30) {
				uniqueName = uniqueName.substring(0, 30);
			}
            uniqueName = uniqueName.replace("\"", "");
			builder.append("\"");
			builder.append(uniqueName);
			builder.append("\"");
			separator = ", ";
		}
		return separator;
	}

	private List<ColumnImpl> getRealColumns() {
	    return this.getTable().getRealColumns().collect(Collectors.toList());
    }

	final void appendTable(SqlBuilder builder) {
		getMapper().getSqlGenerator().appendTable(builder.getBuffer(), "", getAlias());
	}

	final Class<?> getType(String fieldName) {
		if (getTable().getFieldType(fieldName) == null) {
			return null;
		} else {
			return dataMapper.getType(fieldName);
		}
	}

	final T put(KeyValue key , T value) {
		return cache.put(key, value);
	}

	final T get(KeyValue key) {
		return cache.get(key);
	}

	// overrides start here

	String reduce(String fieldName) {
		String constraintField = getName();
		if (constraintField == null || !fieldName.startsWith(constraintField + ".")) {
			return null;
		}
		return fieldName.substring(constraintField.length() + 1);
	}
	void clearCache() {
		this.cache = new HashMap<>();
	}

	void completeFind(Instant effectiveDate) {
		this.cache.values()
            .stream()
            .filter(PersistenceAware.class::isInstance)
            .map(PersistenceAware.class::cast)
            .forEach(PersistenceAware::postLoad);
	}

	boolean isChild() {
		return false;
	}

	abstract String getName();
	abstract T set(Object value , ResultSet rs , int index) throws SQLException;
	abstract boolean appendFromClause(SqlBuilder builder , String parentAlias , boolean isMarked , boolean forceOuterJoin);

	final Boolean hasWhereField(String fieldName) {
		return hasField(fieldName);
	}

	final List<String> getQueryFields() {
		return new ArrayList<>(dataMapper.getQueryFields());
	}

	public abstract boolean isReachable();

	abstract boolean skipFetch(boolean marked, boolean anyChildMarked);

	abstract boolean needsDistinct(boolean marked, boolean anyChildMarked);

}
