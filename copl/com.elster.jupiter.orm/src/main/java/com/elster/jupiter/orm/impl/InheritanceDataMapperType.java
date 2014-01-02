package com.elster.jupiter.orm.impl;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.sql.SqlFragment;
import com.google.common.base.Joiner;

public class InheritanceDataMapperType<T> extends DataMapperType<T> {
	
	private final Map<String,Class<? extends T>> implementations;
	
	InheritanceDataMapperType(TableImpl<T> table, Map<String,Class<? extends T>> implementations) {
		super(table);
		this.implementations = implementations;
	}
	
	@Override
	boolean maps(Class<?> clazz) {
		return implementations.containsValue(clazz);
	}

	@Override
	DomainMapper getDomainMapper() {
		return DomainMapper.FIELDLENIENT;
	}

	@Override
	boolean hasMultiple() {
		return true;
	}

	@Override
	T newInstance() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	T newInstance(String discriminator) {
		return getInjector().getInstance(Objects.requireNonNull(implementations.get(discriminator)));
	}
	
	@Override 
	Class<?> getType(String fieldName) {
		for (Class<?> implementation : implementations.values()) {
			Class<?> result = getDomainMapper().getType(implementation, fieldName);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
	@Override
	Object getEnum(String fieldName, String value) {
		for (Class<? extends T> implementation : implementations.values()) {
			Object result = getDomainMapper().getEnum(implementation, fieldName,value);
			if (result != null) {
				return result;
			}
		}
		return null;			
	}
	
	@Override
	String getDiscriminator(Class<?> clazz) {
		for (Map.Entry<String,Class<? extends T>> entry : implementations.entrySet()) {
			if (entry.getValue() == clazz) {
				return entry.getKey();
			}
		}
		throw new MappingException(clazz);
	}
	
	@Override
	Field getField(String fieldName) {
		for (Class<?> implementation : implementations.values()) {
			Field result = getDomainMapper().getField(implementation, fieldName);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
	@Override
	boolean needsRestriction(Class<? extends T> api) {
		if (api == getTable().getApi()) {
			return false;
		} else {
			for (Map.Entry<String, Class<? extends T>> entry : implementations.entrySet()) {
				if (!api.isAssignableFrom(entry.getValue())) {
					return true;
				}
			}
			return false;
		}
	}
	

	@Override
	Condition condition(Class<? extends T> api) {
		Condition result = Condition.TRUE;
		if (needsRestriction(api)) {
			for (Map.Entry<String, Class<? extends T>> entry : implementations.entrySet()) {
				if (api.isAssignableFrom(entry.getValue())) {
					result = result.and(Where.where("class").isEqualTo(entry.getKey()));
				}
			}
		}
		return result;
	}
	
	@Override
	void addSqlFragment(List<SqlFragment> fragments, Class<? extends T> api, String alias) {
		if (needsRestriction(api)) {
			fragments.add(new DiscriminatorFragment(api,alias));
		}
	}
	
	private class DiscriminatorFragment implements SqlFragment {
		
		private final String alias;
		private final List<String> discriminators = new ArrayList<>();
		
		DiscriminatorFragment(Class<? extends T> api, String alias) {
			this.alias = alias;
			for (Map.Entry<String, Class<? extends T>> entry : implementations.entrySet()) {
				if (api.isAssignableFrom(entry.getValue())) {
					discriminators.add(entry.getKey());
				}
			}
			assert(!discriminators.isEmpty());
		}
		
		@Override
		public int bind(PreparedStatement statement, int position) throws SQLException {
			return position;
		}

		@Override
		public String getText() {
			ColumnImpl column = getTable().getDiscriminator().get();
			StringBuffer buffer = new StringBuffer(column.getName(alias));
			if (discriminators.size() == 1) {
				buffer.append(" = '");
				buffer.append(discriminators.get(0));
				buffer.append("'");
			} else {
				buffer.append(" IN ('");
				buffer.append(Joiner.on("','").join(discriminators));
				buffer.append("')");
			}
			return buffer.toString();
		}
	}
	
}
