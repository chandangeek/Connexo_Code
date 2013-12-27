package com.elster.jupiter.orm.impl;

import java.lang.reflect.Field;

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
}
