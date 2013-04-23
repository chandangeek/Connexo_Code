package com.elster.jupiter.orm;

import java.util.List;

import com.elster.jupiter.conditions.Club;
import com.elster.jupiter.conditions.Condition;

public interface QueryExecutor<T> {
	<R> void add(DataMapper<R> dataMapper);	
	List<T> select(Condition condition, String[] orderBy, boolean eager , String[] exceptions);
	List<T> select(Condition condition, String[] orderBy , boolean eager , String[] exceptions , int from , int to);	
	boolean hasField(String fieldName);
	Object convert(String fieldName , String value);
	Club toClub(Condition condition, String[] fieldNames);
	T get(Object[] key, boolean eager , String[] exceptions);
}
