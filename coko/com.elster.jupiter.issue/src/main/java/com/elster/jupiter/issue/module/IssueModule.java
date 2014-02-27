package com.elster.jupiter.issue.module;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.issue.impl.IssueServiceImpl;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.users.UserService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class IssueModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(QueryService.class);
        requireBinding(UserService.class);
        requireBinding(MeteringService.class);

        bind(IssueService.class).to(IssueServiceImpl.class).in(Scopes.SINGLETON);
    }
}
