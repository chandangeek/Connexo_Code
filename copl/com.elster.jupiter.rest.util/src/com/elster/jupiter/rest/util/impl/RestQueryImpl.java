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

	private String getLast(MultivaluedMap<String, String> map, String key) {
		List<String> values = map.get(key);
		return values == null || values.isEmpty() ? null : values.get(values.size() - 1);
	}
	
	@Override
	public List<T> select(MultivaluedMap<String, String> map) {
		String startString = getLast(map,"start");
		String limitString = getLast(map,"limit");
		if (startString != null && limitString != null) {
			int start = Integer.valueOf(startString);
			int limit = Integer.valueOf(limitString);
			return query.select(convert(map),start+1,limit+1);
		} else {
			return query.select(convert(map));
		}
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
