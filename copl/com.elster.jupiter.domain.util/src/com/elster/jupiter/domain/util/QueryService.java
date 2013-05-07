package com.elster.jupiter.domain.util;

import com.elster.jupiter.conditions.Condition;
import com.elster.jupiter.orm.QueryExecutor;

public interface QueryService {
	<T> Query<T> wrap(QueryExecutor<T> queryExecutor);
	Condition isCurrent(String name);
}
