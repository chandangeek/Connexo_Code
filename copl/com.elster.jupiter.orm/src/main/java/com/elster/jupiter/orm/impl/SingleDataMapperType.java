package com.elster.jupiter.orm.impl;

import java.lang.reflect.Constructor;

import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.orm.associations.Reference;

public class SingleDataMapperType implements DataMapperType {
	private final Class<?> implementation;
	private final Constructor<?> constructor;
	
	public SingleDataMapperType(Class<?> clazz) {
		this.implementation = clazz;
		try {
			constructor = clazz.getDeclaredConstructor();
		} catch (ReflectiveOperationException e) {
			throw new MappingException(e);
		}
		constructor.setAccessible(true);
	}

	@Override
	public boolean maps(Class<?> clazz) {
		return implementation == clazz;
	}

	@Override
	public DomainMapper getDomainMapper() {
		return DomainMapper.FIELDSTRICT;
	}

	@Override
	public boolean hasMultiple() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T newInstance(String discriminator) {
		if (discriminator != null) {
			throw new IllegalArgumentException();
		}
		try {
			return (T) constructor.newInstance();
		} catch (ReflectiveOperationException ex) {
			throw new MappingException(ex);
		}
	}

	@Override
	public Class<?> getType(String fieldName) {
		return getDomainMapper().getType(implementation, fieldName);
	}
	
	@Override
	public Object getEnum(String fieldName, String value) {
		return getDomainMapper().getEnum(implementation, fieldName,value);			
	}

	@Override
	public Object getDiscriminator(Class<?> clazz) {
		throw new IllegalStateException("Should not implement");
	}
	
	@Override
	public boolean isReference(String fieldName) {
		Class<?> clazz = DomainMapper.FIELDLENIENT.getType(implementation,fieldName);
		return clazz == null ? false : Reference.class.isAssignableFrom(clazz); 
	}
}
