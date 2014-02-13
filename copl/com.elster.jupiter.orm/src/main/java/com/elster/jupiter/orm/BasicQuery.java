package com.elster.jupiter.orm;

import java.util.List;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.base.Optional;

/**
 * 
 * Fetches tuples from the database and converts them to objects
 * Defines the common methods between DataMapper and QueryExecutor
 * 
 */
public interface BasicQuery<T> {
	Optional<T> getOptional(Object... values);
	T getExisting(Object... values);
    List<T> select(Condition condition, String ... orderBy);
    List<T> select(Condition condition, Order ordering , Order ... orderings);
}