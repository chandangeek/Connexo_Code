package com.elster.jupiter.orm.impl;

interface  DataMapperType {
	boolean maps(Class<?> clazz);
	DomainMapper getDomainMapper();
	boolean hasMultiple();
	<T> T newInstance(String discriminator);
	Class<?> getType(String fieldName);
	Object getEnum(String fieldName, String value);
	Object getDiscriminator(Class<?> clazz);
	boolean isReference(String fieldName);
	
}
