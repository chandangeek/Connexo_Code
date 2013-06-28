package com.elster.jupiter.orm.query.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.orm.fields.impl.FieldMapping;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.TableImpl;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class JoinDataMapper<T> {
	private final DataMapperImpl<T> dataMapper;
	private final String alias;
	private Map<Object,T> cache;

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

	final TableImpl getTable() {
		return (TableImpl) getMapper().getTable();
	}


	final <R> List<JoinDataMapper<R>> wrap(DataMapperImpl<R> newMapper , AliasFactory aliasFactory) {
		List<JoinDataMapper<R>> result = new ArrayList<>();
		for (ForeignKeyConstraint constraint : getTable().getForeignKeyConstraints()) {
			if (newMapper.getTable().equals(constraint.getReferencedTable())) {
				result.add(new ParentDataMapper<>(newMapper , constraint , aliasFactory.getAlias()));
				return result;
			}
		}
		for (ForeignKeyConstraint constraint : newMapper.getTable().getForeignKeyConstraints()) {
			if (getTable().equals(constraint.getReferencedTable())) {
				if (constraint.getReverseCurrentFieldName() != null) {
					result.add(new CurrentDataMapper<>(newMapper, constraint, aliasFactory.getAlias(true)));
				}
				result.add(new ChildDataMapper<>(newMapper , constraint , aliasFactory.getAlias()));
			}
		}
		return result;
	}

	final ColumnAndAlias getColumnAndAlias(String fieldName) {
		Column column = getTable().getColumnForField(fieldName);
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

	final boolean hasField(String fieldName)  {
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
		for (Column each : getTable().getColumns()) {
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

	final T put(Object key , T value) {
		return cache.put(key, value);
	}

	final T get(Object key) {
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

	void completeFind() {
		for (T each : cache.values()) {
			if (each instanceof PersistenceAware) {
				((PersistenceAware) each).postLoad();
			} else {
				return;
			}
		}
	}

	boolean isChild() {
		return false;
	}

	abstract String getName();
	abstract T set(Object value , ResultSet rs , int index) throws SQLException;
	abstract boolean canRestrict();
	abstract boolean appendFromClause(SqlBuilder builder , String parentAlias , boolean isMarked , boolean forceOuterJoin);

	final public Boolean hasWhereField(String fieldName) {
		return canRestrict() && hasField(fieldName);
	}

	final public List<String> getQueryFields() {
		List<String> result = new ArrayList<>();
		for (Column each : getTable().getColumns()) {
			String fieldName = each.getFieldName();
			String[] parts = fieldName.split("\\.");
			String part = "";
			for (int i = 0 ; i < parts.length - 1 ; i++) {
				part += parts[i];
				if (!result.contains(part)) {
					result.add(part);
				}
				part += ".";
			}
			result.add(fieldName);
		}
		for (ForeignKeyConstraint each : getTable().getForeignKeyConstraints()) {
			String fieldName = each.getFieldName();
			if (fieldName != null) {
				result.add(fieldName);
			}
		}
		return result;
	}
}
