package com.elster.jupiter.orm;

import java.util.List;
import java.util.Map;

public interface Finder<T> {
	// finder
	List<T> find();
	List<T> find(String columnName, Object value);
	List<T> find(String columnName, Object value, String orderBy);
	List<T> find(String columnName1, Object value1,String columnName2, Object value2);
	List<T> find(String columnName1, Object value1,String columnName2, Object value2, String orderBy);
	List<T> find(String[] fieldNames , Object[] values );
	List<T> find(String[] fieldNames , Object[] values , String... orderColumns);
	List<T> find(Map<String,Object> valueMap);
	List<T> find(Map<String,Object> valueMap,String... orderColumns);
	// get by primary key
	T get(Object... values);	
	// get unique
	T getUnique(String columnName, Object value);
	T getUnique(String columnName1, Object value1, String columnName2,Object value2);
	T getUnique(String[] fieldNames , Object[] values);
	// special find 
	List<T> findLenient(Map<String,String> map);	
	
}