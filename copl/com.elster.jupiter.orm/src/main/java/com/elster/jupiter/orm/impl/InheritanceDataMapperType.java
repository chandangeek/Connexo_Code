package com.elster.jupiter.orm.impl;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

import com.elster.jupiter.orm.MappingException;

public class InheritanceDataMapperType<T> extends DataMapperType {
	
	private final Map<String,Class<? extends T>> implementations;
	
	InheritanceDataMapperType(Map<String,Class<? extends T>> implementations) {
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
	<S> S newInstance() {
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("unchecked")
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
	Object getDiscriminator(Class<?> clazz) {
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
}
