package com.elster.jupiter.domain.util.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.orm.QueryExecutor;

public class QueryServiceImpl implements QueryService {

	@Override
	public <T> Query<T> wrap(QueryExecutor<T> queryExecutor) {
		return new QueryImpl<>(queryExecutor);
	}

}
