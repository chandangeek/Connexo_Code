/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.issue.impl.module;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.event.BulkCloseIssueHandlerFactory;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.impl.service.PropertyFactoriesProviderImpl;
import com.elster.jupiter.issue.share.IssueResourceUtility;
import com.elster.jupiter.issue.share.IssueResourceUtilityImpl;
import com.elster.jupiter.issue.share.PropertyFactoriesProvider;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.users.UserService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.kie.api.io.KieResources;
import org.kie.internal.KnowledgeBaseFactoryService;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;

public class IssueModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(KieResources.class);
        requireBinding(KnowledgeBaseFactoryService.class);
        requireBinding(KnowledgeBuilderFactoryService.class);

        requireBinding(OrmService.class);
        requireBinding(QueryService.class);
        requireBinding(UserService.class);
        requireBinding(MeteringService.class);
        requireBinding(MessageService.class);
        requireBinding(TaskService.class);
        requireBinding(PropertySpecService.class);
        requireBinding(EndPointConfigurationService.class);
        requireBinding(MeteringGroupsService.class);

        bind(PropertyFactoriesProvider.class).to(PropertyFactoriesProviderImpl.class).in(Scopes.SINGLETON);
        bind(IssueService.class).to(IssueServiceImpl.class).in(Scopes.SINGLETON);
        bind(IssueResourceUtility.class).to(IssueResourceUtilityImpl.class);
        bind(BulkCloseIssueHandlerFactory.class).in(Scopes.SINGLETON);
    }
}
