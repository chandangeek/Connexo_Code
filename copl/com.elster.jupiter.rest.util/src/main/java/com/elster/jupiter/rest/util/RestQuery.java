package com.elster.jupiter.rest.util;

import com.elster.jupiter.util.conditions.Condition;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

public interface RestQuery<T> {
	List<T> select(MultivaluedMap<String,String> map);
	int getStart(MultivaluedMap<String, String> map);
	int getLimit(MultivaluedMap<String, String> map);

    List<T> select(MultivaluedMap<String, String> map, Condition condition);
}
