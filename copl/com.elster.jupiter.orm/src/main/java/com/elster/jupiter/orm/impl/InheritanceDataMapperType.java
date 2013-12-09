package com.elster.jupiter.orm.impl;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import com.elster.jupiter.orm.MappingException;

public class InheritanceDataMapperType<T> implements DataMapperType {
	private final Map<String,Class<? extends T>> implementations;
	private final Map<String,Constructor<? extends T>> constructors = new HashMap<>();
	
	public InheritanceDataMapperType(Map<String,Class<? extends T>> implementations) {
		this.implementations = implementations;
		for (Map.Entry<String, Class<? extends T>> entry : implementations.entrySet()) {
			try {
				Constructor<? extends T> constructor = entry.getValue().getDeclaredConstructor();
				constructor.setAccessible(true);
				constructors.put(entry.getKey(),constructor);
			} catch (ReflectiveOperationException e) {
				throw new MappingException(e);
			}
		}
	}

	@Override
	public boolean maps(Class<?> clazz) {
		return implementations.containsValue(clazz);
	}

	@Override
	public DomainMapper getDomainMapper() {
		return DomainMapper.FIELDLENIENT;
	}

	@Override
	public boolean hasMultiple() {
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T newInstance(String discriminator) {
		Constructor<? extends T> constructor = constructors.get(discriminator);
		try {
			return constructor == null ? null : constructor.newInstance();
		} catch (ReflectiveOperationException ex) {
			throw new MappingException(ex);
		}
	}
	
	@Override
	public Class<?> getType(String fieldName) {
		for (Class<?> implementation : implementations.values()) {
			Class<?> result = getDomainMapper().getType(implementation, fieldName);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
	@Override
	public Object getEnum(String fieldName, String value) {
		for (Class<? extends T> implementation : implementations.values()) {
			Object result = getDomainMapper().getEnum(implementation, fieldName,value);
			if (result != null) {
				return result;
			}
		}
		return null;			
	}
	
	@Override
	public Object getDiscriminator(Class<?> clazz) {
		for (Map.Entry<String,Class<? extends T>> entry : implementations.entrySet()) {
			if (entry.getValue() == clazz) {
				return entry.getKey();
			}
		}
		throw new MappingException(clazz);
	}
}
