package com.elster.jupiter.orm.impl;

import java.lang.reflect.Field;

import com.google.common.base.Optional;
import com.google.inject.Injector;

interface  DataMapperType {
	boolean maps(Class<?> clazz);
	DomainMapper getDomainMapper();
	boolean hasMultiple();
	<T> T newInstance();
	<T> T newInstance(String discriminator);
	<T> T newInstance(Class<T> clazz);
	Class<?> getType(String fieldName);
	Object getEnum(String fieldName, String value);
	Object getDiscriminator(Class<?> clazz);
	boolean isReference(String fieldName);
	void init(Optional<Injector> injector);
	Field getField(String fieldName);
}
