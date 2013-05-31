package com.elster.jupiter.rest.util.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.QueryParameters;
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

	@Override
	public List<T> select(QueryParameters map) {
        return select(map, Condition.TRUE);
	}

    @Override
    public List<T> select(QueryParameters map, Condition condition) {
        int start = map.getStart();
        int limit = map.getLimit();
        condition = condition.and(convert(map));
        if (limit >= 0) {
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
