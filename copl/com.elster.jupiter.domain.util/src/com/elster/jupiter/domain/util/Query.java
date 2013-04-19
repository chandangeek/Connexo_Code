package com.elster.jupiter.domain.util;

import java.util.List;

import com.elster.jupiter.conditions.Club;
import com.elster.jupiter.conditions.Condition;

public interface Query<T> {
	List<T> where(Condition condition, String ... includes);
	List<T> eagerWhere(Condition condition, String ... excludes);
	List<T> where(Condition condition, int from , int to , String ... includes);
	List<T> eagerWhere(Condition condition, int from , int to ,String ... excludes);
	boolean hasField(String fieldName);
	Object convert(String fieldName , String value);
	Club toClub(Condition condition, String ... fieldNames);
}
