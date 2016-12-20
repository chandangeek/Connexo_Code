package com.elster.jupiter.orm.impl;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

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
    Stream<Class<? extends T>> streamImplementations(List<Class<?>> fragments) {
        return Stream.<Class<? extends T>>of(implementation)
                .filter(impl -> fragments.stream().allMatch(fragment -> fragment.isAssignableFrom(impl)));
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
	String getDiscriminator(Class<?> clazz) {
		throw new IllegalStateException("Should not implement");
	}
	
	@Override
	Field getField(String fieldName) {
		return DomainMapper.FIELDLENIENT.getPathField(implementation,fieldName);
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
