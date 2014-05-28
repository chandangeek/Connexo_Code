package com.elster.jupiter.issue.impl.module;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.issue.impl.service.*;
import com.elster.jupiter.issue.share.service.*;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
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

        bind(IssueMappingService.class).to(IssueMappingServiceImpl.class).in(Scopes.SINGLETON);
        bind(InstallService.class).to(InstallServiceImpl.class).in(Scopes.SINGLETON);
        bind(IssueAssignmentService.class).to(IssueAssignmentServiceImpl.class).in(Scopes.SINGLETON);
        bind(IssueService.class).to(IssueServiceImpl.class).in(Scopes.SINGLETON);
        bind(IssueCreationService.class).to(IssueCreationServiceImpl.class).in(Scopes.SINGLETON);
        bind(IssueActionService.class).to(IssueActionServiceImpl.class).in(Scopes.SINGLETON);
        bind(IssueHelpService.class).to(IssueHelpServiceImpl.class).in(Scopes.SINGLETON); // TODO delete when events will be sent by MDC
    }
}
