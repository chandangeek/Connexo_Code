package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.impl.event.DataValidationEventHandlerFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class IssueDataValidationModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(QueryService.class);
        requireBinding(OrmService.class);
        requireBinding(MeteringService.class);
        requireBinding(MessageService.class);
        requireBinding(EventService.class);
        requireBinding(IssueService.class);
        requireBinding(NlsService.class);
        requireBinding(PropertySpecService.class);

        bind(IssueDataValidationService.class).to(IssueDataValidationServiceImpl.class).in(Scopes.SINGLETON);
        bind(DataValidationEventHandlerFactory.class).in(Scopes.SINGLETON);
    }
}