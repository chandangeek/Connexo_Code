/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;

import javax.ws.rs.core.MultivaluedMap;

import java.util.List;

import static com.elster.jupiter.util.Checks.is;

class RestQueryImpl<T> implements RestQuery<T> {
	private final Query<T> query;
	
	RestQueryImpl(Query<T> query) {
		this.query = query;
	}

	@Override
	public List<T> select(QueryParameters map,String order, String ...orders) {
        return select(map, Order.from(order,orders));
	}

    @Override
    public List<T> select(QueryParameters map, Order... orders) {
        int start = map.getStartInt();
        int limit = map.getLimit();
        Condition condition = convert(map);
        if (limit >= 0) {
            return query.select(condition, start + 1, start + limit + 1, orders);
        } else {
            return query.select(condition, orders);
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
