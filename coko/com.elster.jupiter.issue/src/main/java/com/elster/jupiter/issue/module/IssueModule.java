package com.elster.jupiter.issue.module;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.IssueService;
import com.elster.jupiter.issue.impl.IssueServiceImpl;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class IssueModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(QueryService.class);
        requireBinding(UserService.class);
        requireBinding(EventService.class);
        requireBinding(MeteringService.class);
        requireBinding(AppService.class);
        requireBinding(MessageService.class);
        requireBinding(CronExpressionParser.class);

        bind(IssueService.class).to(IssueServiceImpl.class).in(Scopes.SINGLETON);
    }
}
