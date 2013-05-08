package com.elster.jupiter.domain.util;

import java.util.List;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Subquery;

public interface Query<T> {
	void setLazy(String... includes);
	void setEager(String... excludes);
	List<T> select(Condition condition, String ... orderBy);	
	List<T> select(Condition condition,int from , int to , String ... orderBy);
	T get(Object ... key);
	boolean hasField(String fieldName);
	Object convert(String fieldName , String value);
	Subquery asSubquery(Condition condition, String ... fieldNames);
	List<String> getQueryFieldNames();
	Class<?> getType(String fieldName);
}
