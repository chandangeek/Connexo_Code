package com.elster.jupiter.issue.impl.module;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.impl.drools.IssueAssignmentServiceImpl;
import com.elster.jupiter.issue.impl.service.InstallServiceImpl;
import com.elster.jupiter.issue.impl.service.IssueHelpServiceImpl;
import com.elster.jupiter.issue.impl.service.IssueMappingServiceImpl;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueHelpService;
import com.elster.jupiter.issue.share.service.IssueMappingService;
import com.elster.jupiter.issue.share.service.IssueService;
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
        requireBinding(EventService.class); // TODO delete when events will be defined by MDC

        bind(IssueMappingService.class).to(IssueMappingServiceImpl.class).in(Scopes.SINGLETON);
        bind(InstallService.class).to(InstallServiceImpl.class).in(Scopes.SINGLETON);
        bind(IssueAssignmentService.class).to(IssueAssignmentServiceImpl.class).in(Scopes.SINGLETON);
        bind(IssueService.class).to(IssueServiceImpl.class).in(Scopes.SINGLETON);
        bind(IssueHelpService.class).to(IssueHelpServiceImpl.class).in(Scopes.SINGLETON); // TODO delete when events will be defined by MDC
    }
}
