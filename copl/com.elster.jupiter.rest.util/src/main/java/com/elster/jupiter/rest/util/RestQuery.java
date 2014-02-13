package com.elster.jupiter.rest.util;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import java.util.List;

public interface RestQuery<T> {
    
	List<T> select(QueryParameters queryParameters, String... orderBy);
	List<T> select(QueryParameters queryParameters, Order order, Order... orders);

    List<T> select(QueryParameters queryParameters, Condition condition, String... orderBy);
}
