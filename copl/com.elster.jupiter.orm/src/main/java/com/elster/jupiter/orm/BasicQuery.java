/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import java.util.List;
import java.util.Optional;

/**
 * 
 * Fetches tuples from the database and converts them to objects
 * Defines the common methods between DataMapper and QueryExecutor
 * 
 */
@ProviderType
public interface BasicQuery<T> {
	Optional<T> getOptional(Object... values);
	T getExisting(Object... values);
    List<T> select(Condition condition, Order ... orders);
}