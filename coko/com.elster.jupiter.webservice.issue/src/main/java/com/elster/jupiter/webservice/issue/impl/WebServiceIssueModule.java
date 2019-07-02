/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.webservice.issue.WebServiceIssueService;
import com.elster.jupiter.webservice.issue.impl.event.WebServiceEventHandlerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class WebServiceIssueModule extends AbstractModule {

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

        bind(WebServiceIssueService.class).to(WebServiceIssueServiceImpl.class).in(Scopes.SINGLETON);
        bind(WebServiceIssueServiceImpl.class).in(Scopes.SINGLETON);
        bind(WebServiceEventHandlerFactory.class).in(Scopes.SINGLETON);
    }
}
