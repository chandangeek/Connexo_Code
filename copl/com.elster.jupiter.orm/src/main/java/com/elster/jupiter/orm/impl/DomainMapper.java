package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.orm.RefAny;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;


public enum DomainMapper {
	FIELDSTRICT(true),
	FIELDLENIENT(false);
	
	private final boolean strict;
	
	private DomainMapper(boolean strict) {
		this.strict = strict;
	}
		
	public Object get(Object target , String  fieldPath) {
		for (String fieldName : fieldPath.split("\\.")) {
			target = target == null ? null : basicGet(target,fieldName);
		}
		return target;
	}
					
	private Object basicGet(Object target, String fieldName) {
		Field field = getField(target.getClass(), fieldName);
		if (field == null) {
			return null;
		} else {
			try {
				return field.get(target);
			} catch (IllegalAccessException e) {
				throw new MappingException(e);
			}
		}
	}
	
	private Object create(Class<?> clazz) throws ReflectiveOperationException {
		if (clazz == RefAny.class) {
			return new RefAnyImpl();
		}
		Constructor<?> constructor = clazz.getDeclaredConstructor();
		constructor.setAccessible(true);
		return constructor.newInstance();		
	}

	private Object getOrCreate(Object target, String fieldName) {
		Field field = getField(target.getClass(), fieldName);
		if (field == null) {
			return null;
		} else {
			try {
				Object result = field.get(target);
				if (result == null) {
					result = create(field.getType());
					field.set(target, result);
				}
				return result;
			} catch (ReflectiveOperationException e) {
				throw new MappingException(e);
			}
		}
	}
	
	public void set(Object target , String  fieldPath, Object value) {		
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
		if (field != null) {			
			if (value != null && field.getType().isEnum() && !value.getClass().isEnum()) {
				value = getEnum((Class<? extends Enum<?>>) field.getType(),value);
			}
			try {
				if (Reference.class.isAssignableFrom(field.getType()) && !(value instanceof Reference)) {
					value = ValueReference.of(value);
				}
				field.set(target, value);
			} catch (IllegalAccessException e) {
				throw new MappingException(e);
			}		
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
		System.out.println(value.getClass() + ":" + value);
		throw new IllegalArgumentException("" + value);
	}
	
	@SuppressWarnings("unchecked")
	Object getEnum(Class<?> clazz , String fieldName , String value) {
		Field field = getField(clazz, fieldName);
		if (field == null) {
			return null;
		} else {
			Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) getField(clazz,fieldName).getType();
			return getEnum(enumClass,value);
		}
	}
	
	Field getField(Class<?> clazz, String fieldName) {	
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
	    if (strict) {
	    	throw new MappingException(clazz, fieldName);
	    } else {
	    	return null;
	    }
	}
	
	Class<?> getType(Class<?> implementation , String fieldPath) {
		Class<?> result = implementation;
		for (String fieldName : fieldPath.split("\\.")) {
			if (fieldName.equals(Column.TYPEFIELDNAME)) {
				result = String.class;
			} else {
				Field field = getField(result, fieldName);
				if (field == null) {
					return null;
				} else {
					result = field.getType();
				}
			}
		}
		return result;
	}
}
