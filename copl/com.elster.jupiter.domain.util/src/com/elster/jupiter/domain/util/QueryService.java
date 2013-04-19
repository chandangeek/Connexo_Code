package com.elster.jupiter.domain.util;

import com.elster.jupiter.orm.QueryExecutor;

public interface QueryService {
	<T> Query<T> wrap(QueryExecutor<T> queryExecutor);
}
