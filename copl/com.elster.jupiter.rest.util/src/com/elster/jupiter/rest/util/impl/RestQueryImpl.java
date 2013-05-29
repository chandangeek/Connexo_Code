package com.elster.jupiter.rest.util.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

class RestQueryImpl<T> implements RestQuery<T> {
	private final Query<T> query;
	
	RestQueryImpl(Query<T> query) {
		this.query = query;
	}

	private String getLast(MultivaluedMap<String, String> map, String key) {
		List<String> values = map.get(key);
		return values == null || values.isEmpty() ? null : values.get(values.size() - 1);
	}
	
	private int getLastValue(MultivaluedMap<String, String> map, String key) {
		String intString = getLast(map,key);
		try {
			return intString == null ? -1 : Integer.parseInt(intString);
		} catch (NumberFormatException ex) {
			return -1;
		}
	}
	
	@Override
	public int getStart(MultivaluedMap<String, String> map) {
		return getLastValue(map,"start");
	}
	
	@Override
	public int getLimit(MultivaluedMap<String, String> map) {
		return getLastValue(map,"limit");
	}
	
	@Override
	public List<T> select(MultivaluedMap<String, String> map) {
        return select(map, Condition.TRUE);
	}

    @Override
    public List<T> select(MultivaluedMap<String, String> map, Condition condition) {
        int start = getStart(map);
        int limit = getLimit(map);
        condition = condition.and(convert(map));
        if (start >= 0 && limit >= 0) {
            return query.select(condition, start + 1, start + limit);
        } else {
            return query.select(condition);
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
