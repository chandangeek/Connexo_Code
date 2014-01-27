package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.MappingException;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.impl.RefAnyImpl;
import com.google.inject.Injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


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

	private Object create(Class<?> clazz,Injector injector) throws ReflectiveOperationException {
		if (clazz == RefAny.class) {
			if (injector == null) {
				throw new IllegalArgumentException("Needs injector");
			} else {
				return injector.getInstance(RefAnyImpl.class);
			}
		}
		Constructor<?> constructor = clazz.getDeclaredConstructor();
		constructor.setAccessible(true);
		return constructor.newInstance();		
	}

	private Object getOrCreate(Object target, String fieldName,Injector injector) {
		Field field = getField(target.getClass(), fieldName);
		if (field == null) {
			return null;
		} else {
			try {
				Object result = field.get(target);
				if (result == null) {
					result = create(field.getType(),injector);
					field.set(target, result);
				}
				return result;
			} catch (ReflectiveOperationException e) {
				throw new MappingException(e);
			}
		}
	}
	
	public void set(Object target , String  fieldPath, Object value) {		
		set(target,fieldPath,value,null);
	}
	
	public void set(Object target , String  fieldPath, Object value, Injector injector) {		
		String[] fieldNames = fieldPath.split("\\.");
		if (fieldNames.length > 1) {
			if (value != null) {
				target = getOrCreate(target,fieldNames[0],injector);
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
			try {
				Object currentValue = field.get(target);
				if (currentValue instanceof Reference) {
					((Reference<Object>) currentValue).set(value);
				} else {
				field.set(target, value);
				}
			} catch (IllegalAccessException e) {
				throw new MappingException(e);
			}		
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
	
	public static Class<?> extractDomainClass (Field field) {
		Type type = field.getGenericType();
		if (type instanceof Class<?>) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			Type subType = ((ParameterizedType) type).getActualTypeArguments()[0];
			if (subType instanceof Class<?>) {
				return (Class<?>) subType;
			} else if (type instanceof ParameterizedType) {
				return (Class<?>) ((ParameterizedType) subType).getRawType();
			}
		} 
		throw new IllegalArgumentException("" + type);
	}
}
