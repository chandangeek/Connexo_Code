package com.elster.jupiter.orm.impl;

import java.lang.reflect.Field;
import com.elster.jupiter.orm.associations.Reference;
import com.google.inject.Injector;

abstract class DataMapperType<T> {
	private final TableImpl<T> table;
	
	DataMapperType(TableImpl<T> table) {
		this.table = table;
	}
	abstract boolean maps(Class<?> clazz);
	abstract DomainMapper getDomainMapper();
	abstract boolean hasMultiple();
	abstract T newInstance();
	abstract T newInstance(String discriminator);
	abstract Class<?> getType(String fieldName);
	abstract Object getEnum(String fieldName, String value);
	abstract String getDiscriminator(Class<?> clazz);
	abstract Field getField(String fieldName);
	
	final Injector getInjector() {
		return table.getDataModel().getInjector();
	}
	
	final boolean isReference(String fieldName) {
		Class<?> clazz = getType(fieldName);
		return clazz == null ? false : Reference.class.isAssignableFrom(clazz); 
	}
	
}
