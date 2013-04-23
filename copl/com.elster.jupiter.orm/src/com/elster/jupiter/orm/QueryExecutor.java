package com.elster.jupiter.orm;

import java.util.List;

import com.elster.jupiter.conditions.Club;
import com.elster.jupiter.conditions.Condition;

public interface QueryExecutor<T> {
	<R> void add(DataMapper<R> dataMapper);
	List<T> select(Condition condition, String[] includes);
	List<T> eagerSelect(Condition condition, String[] excludes);
	List<T> select(Condition condition, int from , int to , String[] includes);
	List<T> eagerSelect(Condition condition, int from , int to , String[] excludes);
	boolean hasField(String fieldName);
	Object convert(String fieldName , String value);
	Club toClub(Condition condition, String[] fieldNames);
	T get(Object[] key);
}
