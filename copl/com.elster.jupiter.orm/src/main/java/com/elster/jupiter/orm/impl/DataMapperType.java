package com.elster.jupiter.orm.impl;

import java.lang.reflect.Field;
import java.util.Objects;

import com.elster.jupiter.orm.associations.Reference;
import com.google.inject.Injector;

abstract class DataMapperType {
	private Injector injector;
	
	abstract boolean maps(Class<?> clazz);
	abstract DomainMapper getDomainMapper();
	abstract boolean hasMultiple();
	abstract <T> T newInstance();
	abstract <T> T newInstance(String discriminator);
	abstract Class<?> getType(String fieldName);
	abstract Object getEnum(String fieldName, String value);
	abstract Object getDiscriminator(Class<?> clazz);
	abstract Field getField(String fieldName);
	
	final void init(Injector injector) {
		this.injector = Objects.requireNonNull(injector);
	}
	
	final Injector getInjector() {
		return injector;
	}
	
	final boolean isReference(String fieldName) {
		Class<?> clazz = getType(fieldName);
		return clazz == null ? false : Reference.class.isAssignableFrom(clazz); 
	}
	
}
