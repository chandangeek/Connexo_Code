package com.elster.jupiter.domain.util.impl;

import org.osgi.service.component.annotations.Component;

import com.elster.jupiter.conditions.Condition;
import com.elster.jupiter.conditions.Operator;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.orm.QueryExecutor;

@Component(name = "com.elster.jupiter.domain")
public class QueryServiceImpl implements QueryService {

	@Override
	public <T> Query<T> wrap(QueryExecutor<T> queryExecutor) {
		return new QueryImpl<>(queryExecutor);
	}

	@Override
	public Condition isCurrent(String name) {
		long now = System.currentTimeMillis();
		Condition condition = Operator.LESSTHANOREQUAL.compare( name + ".start" , now);
		condition.and(Operator.GREATERTHAN.compare(name + ".stop", now));
		return condition;
	}

}
