package com.elster.jupiter.rest.util;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import java.util.List;

public interface RestQuery<T> {
    
		
	List<T> select(QueryParameters queryParameters, Order... orders);

	@Deprecated
	List<T> select(QueryParameters queryParameters, String order, String ... orders);
	
	@Deprecated
	/*
	 * use query.setRestriction for initial condition
	 */
    List<T> select(QueryParameters queryParameters, Condition condition, String... orderBy);

}
