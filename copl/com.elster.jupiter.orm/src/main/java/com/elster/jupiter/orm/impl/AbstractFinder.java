package com.elster.jupiter.orm.impl;

import java.util.List;
import java.util.Map;

import com.elster.jupiter.orm.Finder;

public abstract class AbstractFinder<T> implements Finder<T> {

	@Override
	final public List<T> find() {
		return find((String[]) null , (Object[]) null , (String[]) null);
	}
			
	@Override
	final public List<T> find(String fieldName , Object value) {
		return find(new String[] {fieldName} , new Object[] { value });
	}
	
	@Override
	final public List<T> find(String fieldName , Object value , String orderBy) {
		return find(new String[] {fieldName} , new Object[] { value },orderBy);
	}
	
	@Override
	final public List<T> find(String fieldName1 , Object value1, String fieldName2, Object value2) {
		return find(new String[] {fieldName1 , fieldName2 } , new Object[] { value1 , value2 });
	}
	
	@Override
	final public List<T> find(String fieldName1 , Object value1, String fieldName2, Object value2,String orderBy) {
		return find(new String[] {fieldName1 , fieldName2 } , new Object[] { value1 , value2 },orderBy);
	}
	
	@Override
	final public List<T> find(String[] fieldNames , Object[] values ) {
		return find(fieldNames,values, (String[]) null);
	}
	
	@Override
	final public List<T> find(Map<String,Object> valueMap) {
		return find(valueMap, (String[]) null);
	}
	
	@Override 
	final public List<T> find(Map<String,Object> valueMap, String... orderBy) {
		if (valueMap == null) {
			return find((String[]) null , (Object[]) null , orderBy);
		}
		String[] fieldNames = new String[valueMap.size()];
		Object[] values = new Object[valueMap.size()];
		int index = 0;
		for (Map.Entry<String,Object> entry : valueMap.entrySet()) {
			fieldNames[index] = entry.getKey();
			values[index++] = entry.getValue();
		}
		return find(fieldNames,values,orderBy);		
	}
	
	@Override
	final public T getUnique(String fieldName, Object value) {
		return getUnique(new String[] { fieldName } , new Object[] { value });
	}
	
	
	@Override
	final public T getUnique(String fieldName1 , Object value1, String fieldName2, Object value2) {
		return getUnique(new String[] { fieldName1 , fieldName2 } , new Object[] { value1 , value2 });
	}
	
	@Override
	final public T getUnique(String[] fieldNames , Object[] values) {
		List<T> candidates = find(fieldNames, values);
		return candidates.isEmpty() ? null : candidates.get(0);		
	}
		
	@Override
	final public T get(Object... values) {
		if (getPrimaryKeyLength() != values.length) {
			throw new IllegalArgumentException("Argument array length " + values.length + " does not match Primary Key Field count of " + getPrimaryKeyLength());
		}
		List<T> candidates = findByPrimaryKey(values);
		return candidates.isEmpty() ? null : candidates.get(0);
	}
	
	abstract int getPrimaryKeyLength();
	
	abstract List<T> findByPrimaryKey(Object[] values);
}
