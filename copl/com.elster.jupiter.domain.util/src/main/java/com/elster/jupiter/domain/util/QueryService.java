package com.elster.jupiter.domain.util;

import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;

public interface QueryService {
	<T> Query<T> wrap(QueryExecutor<T> queryExecutor);
	Condition isCurrent(String name);
}
