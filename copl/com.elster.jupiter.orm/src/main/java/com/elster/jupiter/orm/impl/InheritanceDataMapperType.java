package com.elster.jupiter.orm.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.orm.associations.Reference;
import com.google.common.base.Optional;
import com.google.inject.Injector;

public class InheritanceDataMapperType<T> implements DataMapperType {
	private final Map<String,Class<? extends T>> implementations;
	private Map<String,Constructor<? extends T>> constructors = new HashMap<>();
	private Optional<Injector> injector;
	
	public InheritanceDataMapperType(Map<String,Class<? extends T>> implementations) {
		this.implementations = implementations;
	}
	
	public void init(Optional<Injector> injector) {
		this.injector = injector;
		if (injector.isPresent()) {
			for (Class<? extends T> value : implementations.values()) {
				injector.get().getInstance(value);
			}
		} else {
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

	@Override
	public <S> S newInstance() {
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T newInstance(String discriminator) {
		if (injector.isPresent()) {
			return injector.get().getInstance(implementations.get(discriminator));
		} else {
			Constructor<? extends T> constructor = constructors.get(discriminator);
			try {
				return constructor == null ? null : constructor.newInstance();
			} catch (ReflectiveOperationException ex) {
				throw new MappingException(ex);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <S> S newInstance(Class<S> clazz) {
		if (injector.isPresent()) {
			return injector.get().getInstance(clazz);
		} else {
			for (Map.Entry<String, Class<? extends T>> entry : implementations.entrySet()) {
				if (entry.getValue().equals(clazz)) {
					return (S) newInstance(entry.getKey());
				}
			}
			throw new IllegalArgumentException("" + clazz);
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

	@Override
	public boolean isReference(String fieldName) {
		Class<?> clazz = getType(fieldName);
		return clazz == null ? false : Reference.class.isAssignableFrom(clazz); 
	}
	
	@Override
	public Field getField(String fieldName) {
		for (Class<?> implementation : implementations.values()) {
			Field result = getDomainMapper().getField(implementation, fieldName);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
}
