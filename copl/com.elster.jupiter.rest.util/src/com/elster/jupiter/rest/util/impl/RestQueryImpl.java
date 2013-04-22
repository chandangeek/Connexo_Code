package com.elster.jupiter.rest.util.impl;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import com.elster.jupiter.conditions.Condition;
import com.elster.jupiter.conditions.Operator;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.RestQuery;

class RestQueryImpl<T> implements RestQuery<T> {
	private final Query<T> query;
	
	RestQueryImpl(Query<T> query) {
		this.query = query;
	}

	@Override
	public List<T> select(MultivaluedMap<String, String> map) {
		return query.select(convert(map));
	}
	
	private Condition convert(MultivaluedMap<String, String> map) {
		Condition condition = Condition.TRUE;
		for (String key : map.keySet()) {
			if (query.hasField(key)) {		
				String value = map.getFirst(key);
				if (value.trim().length() > 0) {				
					condition = condition.and(Operator.EQUAL.compare(key,query.convert(key, value))); 					
				}
			}
		}
		return condition;
	}
	

}
