package com.elster.jupiter.orm.impl;

import java.lang.reflect.*;

import com.elster.jupiter.orm.PersistenceException;

class FieldMapper {
		
	public Object get(Object target , String  fieldPath) {
		for (String fieldName : fieldPath.split("\\.")) {
			target = target == null ? null : basicGet(target,fieldName);
		}
		return target;
	}
					
	private Object basicGet(Object target, String fieldName) {
		Field field = getField(target.getClass(), fieldName);
		try {
			return field.get(target);
		} catch (IllegalAccessException e) {
			throw new PersistenceException(e);
		}
	}

	private Object getOrCreate(Object target, String fieldName) {
		Field field = getField(target.getClass(), fieldName);
		try {
			Object result = field.get(target);
			if (result == null) {
				Constructor<?> constructor = field.getType().getDeclaredConstructor();
				constructor.setAccessible(true);
				result = constructor.newInstance();
				field.set(target, result);
			}
			return result;			
		} catch (ReflectiveOperationException e) {
			throw new PersistenceException(e);
		}
	}
	
	void set(Object target , String  fieldPath, Object value) {		
		String[] fieldNames = fieldPath.split("\\.");
		if (fieldNames.length > 1) {
			if (value != null) {
				target = getOrCreate(target,fieldNames[0]);
			} else {
				target = basicGet(target,fieldNames[0]);
			}
		}
		for (int i = 1 ; i < fieldNames.length - 1 ; i++) {
			target = target == null ? null : basicGet(target,fieldNames[i]);
		}
		if (target != null) {
			basicSet(target,fieldNames[fieldNames.length-1],value);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void basicSet(Object target , String fieldName , Object value) {
		Field field = getField(target.getClass(), fieldName);
		if (value != null && field.getType().isEnum()) {
			value = getEnum((Class<? extends Enum<?>>) field.getType(),value);
		}
		try {
			field.set(target, value);
		} catch (IllegalAccessException e) {
			throw new PersistenceException(e);
		}		
	}
		
	private Object getEnum(Class<? extends Enum<?>> clazz, Object value) {
		Enum<?>[] enumConstants = clazz.getEnumConstants();
		if (value instanceof Integer) {
			return enumConstants[(Integer) value];
		} else {
			for (Enum<?> each : clazz.getEnumConstants()) {
				if (each.name().equals(value)) {
					return each;
				}
			}
		}
		throw new IllegalArgumentException("" + value);
	}
	
	@SuppressWarnings("unchecked")
	Object getEnum(Class<?> clazz , String fieldName , String value) {
		Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) getField(clazz,fieldName).getType();
		return getEnum(enumClass,value);
	}
	
	private Field getField(Class<?> clazz, String fieldName) {	
		Class<?> current = clazz;
	    do {
	        for ( Field field : current.getDeclaredFields() ) {
	            if (field.getName().equals(fieldName)) {
	            	field.setAccessible(true);
	            	return field;
	            }
	        }
	        current = current.getSuperclass();
	    } while ( current != null );
	    throw new PersistenceException("Field " + fieldName + " not found in " + clazz);
	}
}
