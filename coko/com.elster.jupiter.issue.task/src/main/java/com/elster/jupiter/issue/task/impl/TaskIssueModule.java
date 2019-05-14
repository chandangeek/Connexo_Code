/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.issue.task.TaskIssueService;
import com.elster.jupiter.issue.task.impl.event.TaskIssueEventHandlerFactory;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.tasks.TaskService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class TaskIssueModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(QueryService.class);
        requireBinding(OrmService.class);
        requireBinding(MeteringService.class);
        requireBinding(MessageService.class);
        requireBinding(EventService.class);
        requireBinding(IssueService.class);
        requireBinding(NlsService.class);
        requireBinding(TaskService.class);
        requireBinding(PropertySpecService.class);

        bind(TaskIssueService.class).to(TaskIssueServiceImpl.class).in(Scopes.SINGLETON);
        bind(TaskIssueEventHandlerFactory.class).in(Scopes.SINGLETON);
        bind(TaskIssueActionsFactory.class).in(Scopes.SINGLETON);
    }
}
