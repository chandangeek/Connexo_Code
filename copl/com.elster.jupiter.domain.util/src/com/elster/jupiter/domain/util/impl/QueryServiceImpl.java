package com.elster.jupiter.domain.util.impl;

import org.osgi.service.component.annotations.Component;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.orm.QueryExecutor;

@Component(name = "com.elster.jupiter.domain")
public class QueryServiceImpl implements QueryService {

	@Override
	public <T> Query<T> wrap(QueryExecutor<T> queryExecutor) {
		return new QueryImpl<>(queryExecutor);
	}

}
