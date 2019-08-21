/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.servicecall.ServiceCallIssueService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class ServiceCallIssueModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(QueryService.class);
        requireBinding(OrmService.class);
        requireBinding(MeteringService.class);
        requireBinding(MessageService.class);
        requireBinding(EventService.class);
        requireBinding(IssueService.class);
        requireBinding(NlsService.class);
        requireBinding(ServiceCallService.class);
        requireBinding(PropertySpecService.class);

        bind(ServiceCallIssueService.class).to(ServiceCallIssueServiceImpl.class).in(Scopes.SINGLETON);
        bind(ServiceCallIssueActionsFactory.class).in(Scopes.SINGLETON);
    }
}
