/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util;

import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * A Query is the object version of  a database join.
 * Unlike a traditional join, a Query has a main type,
 * and zero or more associated types. Actually the list of associated types constitute a
 * number of possible joins. Depending on the select conditions and the eagerness or lazyness,
 * the associated types may or may not be joined to the main type
 * The provider of the Query object decides which types can be joined to the main type.
 * If an associated type is the n part in a 0 to n association, criteria on the associated type
 * must be interpreted as main type HAS ANY associated type where
 * Also in this case, the collection containing the associated type will not be realized,
 * because the query will not return all the associated types.
 */
public interface Query<T> {
    /**
     * do not retrieve sub types, except for the associated types described by the argument
     */
    void setLazy(String... includes);

    /**
     * do retrieve all associated types, except for the associated types described by the argument
     */
    void setEager(String... excludes);

    /**
     * @return list of objects matching the condition
     */
    List<T> select(Condition condition, Order... orders);

    /**
     * paginated query
     */
    List<T> select(Condition condition, int from, int to, Order... orders);

    /**
     * get by primary key. This differs from DataMapper.get that it
     * will retrieve associated objects in a single query
     */
    Optional<T> get(Object... key);

    /**
     * @return true if the Query knows fieldName
     */
    boolean hasField(String fieldName);

    /**
     * Converts a String value to the type specified by the fieldName,
     * so that it can  be used in a condition.
     */
    Object convert(String fieldName, String value);

    /**
     * Convert the Query to a SubQuery
     */
    Subquery asSubquery(Condition condition, String... fieldNames);

    /**
     * @return a list of all possible field name that can be used in conditions for this Query
     */
    List<String> getQueryFieldNames();

    /**
     * @return the Java type corresponding to this field name
     */
    Class<?> getType(String fieldName);

    Instant getEffectiveDate();

    void setEffectiveDate(Instant date);

    void setRestriction(Condition condition);
}
