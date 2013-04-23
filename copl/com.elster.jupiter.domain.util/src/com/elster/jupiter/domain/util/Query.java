package com.elster.jupiter.domain.util;

import java.util.List;

import com.elster.jupiter.conditions.Club;
import com.elster.jupiter.conditions.Condition;

public interface Query<T> {
	List<T> select(Condition condition, String ... includes);
	List<T> eagerSelect(Condition condition, String ... excludes);
	List<T> select(Condition condition, int from , int to , String ... includes);
	List<T> eagerSelect(Condition condition, int from , int to ,String ... excludes);
	T get(Object ... key);
	boolean hasField(String fieldName);
	Object convert(String fieldName , String value);
	Club toClub(Condition condition, String ... fieldNames);
}
