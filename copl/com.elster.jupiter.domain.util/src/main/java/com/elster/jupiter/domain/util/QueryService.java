/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util;

import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;

public interface QueryService {
	/**
	 * create a Query object based on the argument
	 */
	<T> Query<T> wrap(QueryExecutor<T> queryExecutor);
	/**
	 * 
	 * @return a condition specifying that the interval specified by fieldName must include now.
	 */
	Condition isCurrent(String fieldName);
}
