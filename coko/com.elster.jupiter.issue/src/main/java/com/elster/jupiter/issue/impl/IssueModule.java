package com.elster.jupiter.issue.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.orm.OrmService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class IssueModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(QueryService.class);

        bind(IssueService.class).to(IssueServiceImpl.class).in(Scopes.SINGLETON);
    }
}
