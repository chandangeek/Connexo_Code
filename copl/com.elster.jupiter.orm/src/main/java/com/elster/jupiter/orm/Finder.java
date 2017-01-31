/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.conditions.Order;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fetches tuples from the database and converts them to objects
 * A Finder is associated with one table.
 * Finder contains simple find methods. More advanced criteria can be specified on a Query object.
 * Also a Query object can join in other tables to avoid the N+1 ORM performance issue
 */
@ProviderType
public interface Finder<T> extends BasicQuery<T> {

    /**
     * Fetch all tuples. Only use on small tables
     */
    List<T> find();

    /**
     * Fetch all tuples where fieldName equals value
     */
    List<T> find(String fieldName, Object value);

    /**
     * @param fieldName
     * @param value
     * @param orderBy   if this matches a fieldName, it is converted to the corresponding Column name,
     *                  otherwise the string is passed transparently to the order by clause on the select statement.
     * @return
     */
    @Deprecated
    List<T> find(String fieldName, Object value, String order);
    List<T> find(String fieldName, Object value, Order... orders);
    /**
     * Fetch all tuples where fieldName1 equals value 1 AND fieldName 2 equals value2
     *
     * @param fieldName1
     * @param value1
     * @param fieldName2
     * @param value2
     * @return
     */
    List<T> find(String fieldName1, Object value1, String fieldName2, Object value2);

    @Deprecated
    List<T> find(String fieldName1, Object value1, String fieldName2, Object value2, String order);
    List<T> find(String fieldName1, Object value1, String fieldName2, Object value2, Order... orders);

    List<T> find(String[] fieldNames, Object[] values);

    @Deprecated
    List<T> find(String[] fieldNames, Object[] values, String order, String... orders);
    List<T> find(String[] fieldNames, Object[] values, Order... orders);

    List<T> find(Map<String, Object> valueMap);

    @Deprecated
    List<T> find(Map<String, Object> valueMap, String order, String... orders);
    List<T> find(Map<String, Object> valueMap, Order... orders);

    /**
     * Find journal entries for a primary key
     */
    List<JournalEntry<T>> getJournal(Object... values);
	Optional<JournalEntry<T>> getJournalEntry(Instant instant, Object... values);
	
    /**
     * Find object where fieldName equals value
     *
     * @throws NotUniqueException
     */
    Optional<T> getUnique(String fieldName, Object value);

    Optional<T> getUnique(String fieldName1, Object value1, String fieldName2, Object value2);

    Optional<T> getUnique(String[] fieldNames, Object[] values);

    /**
	 * @since 1.1
	 * 
	 */
	JournalFinder<T> at(Instant instant);
    /**
	 * @since 1.1
	 * 
	 */
    interface JournalFinder<S> {
    	List<JournalEntry<S>> find(Map<String, Object> valueMap);
    }
}