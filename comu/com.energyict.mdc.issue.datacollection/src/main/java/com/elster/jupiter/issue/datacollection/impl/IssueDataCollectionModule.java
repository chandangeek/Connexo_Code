package com.elster.jupiter.issue.datacollection.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.datacollection.impl.install.InstallServiceImpl;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.callback.InstallService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class IssueDataCollectionModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(QueryService.class);
        requireBinding(MeteringService.class);
        requireBinding(MessageService.class);
        requireBinding(EventService.class);
        requireBinding(IssueService.class);
        requireBinding(IssueCreationService.class);

        bind(InstallService.class).to(InstallServiceImpl.class).in(Scopes.SINGLETON);
    }
}
