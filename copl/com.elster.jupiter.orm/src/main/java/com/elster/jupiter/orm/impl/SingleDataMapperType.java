package com.elster.jupiter.orm.impl;

import java.lang.reflect.Constructor;

import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.orm.associations.Reference;
import com.google.common.base.Optional;
import com.google.inject.Injector;

public class SingleDataMapperType implements DataMapperType {
	private final Class<?> implementation;
	private Constructor<?> constructor;
	private Optional<Injector> injector;
	
	public SingleDataMapperType(Class<?> clazz) {
		this.implementation = clazz;
		
	}
	
	@Override
	public void init(Optional<Injector> injector) {
		this.injector = injector;
		if (injector.isPresent()) {
			constructor = null;
		} else {
			try {
				constructor = implementation.getDeclaredConstructor();
			} catch (ReflectiveOperationException e) {
				throw new MappingException(e);
			}
			constructor.setAccessible(true);
		}
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
	public <T> T newInstance() {
		if (injector.isPresent()) {
			return (T) injector.get().getInstance(implementation);
		} else {
			try {
				return (T) constructor.newInstance();
			} catch (ReflectiveOperationException ex) {
				throw new MappingException(ex);
			}
		}
	}
	
	@Override
	public <T> T newInstance(String discriminator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T newInstance(Class<T> clazz) {
		if (clazz.equals(implementation)) {
			return newInstance();
		} else {
			throw new IllegalArgumentException("" + clazz);
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
