package com.elster.jupiter.orm.impl;

import java.lang.reflect.Field;
import java.util.List;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;

public class SingleDataMapperType<T> extends DataMapperType<T> {
	private final Class<? extends T> implementation;
	
	SingleDataMapperType(TableImpl<T> table, Class<? extends T> clazz) {
		super(table);
		this.implementation = clazz;
	}

	@Override
	boolean maps(Class<?> clazz) {
		return implementation == clazz;
	}

	@Override
	DomainMapper getDomainMapper() {
		return DomainMapper.FIELDSTRICT;
	}

	@Override
	boolean hasMultiple() {
		return false;
	}

	@Override
	T newInstance() {
		return getInjector().getInstance(implementation);
	}
	
	@Override
	T newInstance(String discriminator) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	Class<?> getType(String fieldName) {
		return getDomainMapper().getType(implementation, fieldName);
	}
	
	@Override
	Object getEnum(String fieldName, String value) {
		return getDomainMapper().getEnum(implementation, fieldName,value);			
	}

	@Override
	String getDiscriminator(Class<?> clazz) {
		throw new IllegalStateException("Should not implement");
	}
	
	@Override
	Field getField(String fieldName) {
		return DomainMapper.FIELDLENIENT.getField(implementation,fieldName);
	}

	@Override
	void addSqlFragment(List<SqlFragment> fragments, Class<? extends T> type,String alias) {
	}

	@Override
	Condition condition(Class<? extends T> api) {
		return Condition.TRUE;
	}

	@Override
	boolean needsRestriction(Class<? extends T> api) {
		return false;
	}
	
	
}
