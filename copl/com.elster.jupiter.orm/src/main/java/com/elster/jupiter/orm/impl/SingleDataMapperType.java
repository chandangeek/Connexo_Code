package com.elster.jupiter.orm.impl;

import java.lang.reflect.Field;

public class SingleDataMapperType extends DataMapperType {
	private final Class<?> implementation;
	
	SingleDataMapperType(Class<?> clazz) {
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

	@SuppressWarnings("unchecked")
	@Override
	<T> T newInstance() {
		return (T) getInjector().getInstance(implementation);
	}
	
	@Override
	<T> T newInstance(String discriminator) {
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
	Object getDiscriminator(Class<?> clazz) {
		throw new IllegalStateException("Should not implement");
	}
	
	@Override
	Field getField(String fieldName) {
		return DomainMapper.FIELDLENIENT.getField(implementation,fieldName);
	}
}
