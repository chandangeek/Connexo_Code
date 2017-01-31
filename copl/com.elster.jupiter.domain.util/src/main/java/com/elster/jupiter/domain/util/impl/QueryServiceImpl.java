/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.domain.util.impl;

import java.time.Clock;
import org.osgi.service.component.annotations.Component;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.elster.jupiter.domain")
public class QueryServiceImpl implements QueryService {

    private volatile Clock clock;

    public QueryServiceImpl() {

    }

    @Inject
    public QueryServiceImpl(Clock clock) {
        this.clock = clock;
    }

    @Override
	public <T> Query<T> wrap(QueryExecutor<T> queryExecutor) {
		return new QueryImpl<>(queryExecutor);
	}

	@Override
	public Condition isCurrent(String name) {
		long now = clock.instant().toEpochMilli();
		Condition condition = Operator.LESSTHANOREQUAL.compare( name + ".start" , now);
		condition.and(Operator.GREATERTHAN.compare(name + ".stop", now));
		return condition;
	}

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

}
