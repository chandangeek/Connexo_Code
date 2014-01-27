package com.elster.jupiter.rest.util.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

import static com.elster.jupiter.util.Checks.is;

class RestQueryImpl<T> implements RestQuery<T> {
	private final Query<T> query;
	
	RestQueryImpl(Query<T> query) {
		this.query = query;
	}

	@Override
	public List<T> select(QueryParameters map,String ...orderBy) {
        return select(map, Condition.TRUE, orderBy);
	}

    @Override
    public List<T> select(QueryParameters map, Condition condition, String... orderBy) {
        int start = map.getStart();
        int limit = map.getLimit();
        condition = condition.and(convert(map));
        if (limit >= 0) {
            return query.select(condition, start + 1, start + limit,orderBy);
        } else {
            return query.select(condition,orderBy);
        }
    }
    
	private Condition convert(MultivaluedMap<String, String> map) {
		Condition condition = Condition.TRUE;
		for (String key : map.keySet()) {
			if (query.hasField(key)) {		
				String value = map.getFirst(key);
				if (!is(value).emptyOrOnlyWhiteSpace()) {
					condition = condition.and(Operator.EQUAL.compare(key,query.convert(key, value))); 					
				}
			}
		}
		return condition;
	}
	

}
