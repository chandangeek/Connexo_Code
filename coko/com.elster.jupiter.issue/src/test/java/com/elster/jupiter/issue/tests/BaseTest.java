package com.elster.jupiter.issue.tests;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.impl.module.IssueModule;
import com.elster.jupiter.issue.impl.records.IssueImpl;
import com.elster.jupiter.issue.impl.service.InstallServiceImpl;
import com.elster.jupiter.issue.impl.service.IssueMappingServiceImpl;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.IssueAction;
import com.elster.jupiter.issue.share.cep.IssueActionFactory;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.*;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.io.KieResources;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactoryService;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.mockito.Matchers;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore("Base functionality for all tests")
public class BaseTest {
    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));

            TaskService taskService = mock(TaskService.class);
            bind(TaskService.class).toInstance(taskService);

            RecurrentTaskBuilder builder = mock(RecurrentTaskBuilder.class);
            when(taskService.newBuilder()).thenReturn(builder);
            when(builder.build()).thenReturn(mock(RecurrentTask.class));

            bind(KieResources.class).toInstance(mock(KieResources.class));
            bind(KnowledgeBaseFactoryService.class).toInstance(mockKnowledgeBaseFactoryService());
            bind(KnowledgeBuilderFactoryService.class).toInstance(mockKnowledgeBuilderFactoryService());
        }
    }

    @BeforeClass
    public static void setEnvironment(){
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new MeteringModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule(),
                new UserModule(),
                new IssueModule()
        );

        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(InstallServiceImpl.class);
            // In OSGI container issue types will be set by separate bundle
            IssueService issueService = injector.getInstance(IssueService.class);
            IssueType type = issueService.createIssueType("datacollection", "Data Collection");
            issueService.createReason("reason", type);
            ctx.commit();
        }
    }

    @AfterClass
    public static void deactivateEnvironment(){
        inMemoryBootstrapModule.deactivate();
    }

    protected TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }
    protected TransactionContext getContext(){
        return getTransactionService().getContext();
    }
    protected UserService getUserService() {
        return injector.getInstance(UserService.class);
    }
    protected IssueService getIssueService() {
        return injector.getInstance(IssueService.class);
    }
    protected IssueMappingService getIssueMappingService(){
        return injector.getInstance(IssueMappingService.class);
    }
    protected IssueCreationService getIssueCreationService(){
        return injector.getInstance(IssueCreationService.class);
    }
    protected IssueActionService getIssueActionService(){
        return injector.getInstance(IssueActionService.class);
    }
    protected IssueAssignmentService getIssueAssignmentService() {
        return injector.getInstance(IssueAssignmentService.class);
    }
    protected ThreadPrincipalService getThreadPrincipalService() {
        return injector.getInstance(ThreadPrincipalService.class);
    }
    protected DataModel getDataModel(){
        IssueMappingServiceImpl impl = IssueMappingServiceImpl.class.cast(getIssueMappingService());
        return  impl.getDataModel();
    }

    protected CreationRule getSimpleCreationRule() {
        CreationRule rule = getIssueCreationService().createRule();
        rule.setName("Simple Rule");
        rule.setComment("Comment for rule");
        rule.setContent("Empty content");
        rule.setReason(getIssueService().findReason(1L).orNull());
        rule.setDueInValue(15L);
        rule.setDueInType(DueInType.DAY);
        rule.setTemplateUuid("Parent template uuid");
        rule.save();
        return rule;
    }

    protected Issue createIssueMinInfo() {
        try (TransactionContext context = getContext()) {
            Issue issue = getDataModel().getInstance(IssueImpl.class);
            issue.setReason(getIssueService().findReason(1).orNull());
            issue.setStatus(getIssueService().findStatus(1).orNull());
            issue.setRule(getSimpleCreationRule());
            issue.save();
            context.commit();
            return issue;
        }
    }

    protected static KnowledgeBuilderFactoryService mockKnowledgeBuilderFactoryService() {
        KnowledgeBuilderConfiguration config = mock(KnowledgeBuilderConfiguration.class);
        KnowledgeBuilder builder = mock(KnowledgeBuilder.class);
        KnowledgeBuilderFactoryService service = mock(KnowledgeBuilderFactoryService.class);
        when(service.newKnowledgeBuilderConfiguration(Matchers.<java.util.Properties>any(), Matchers.<java.lang.ClassLoader[]>any())).thenReturn(config);
        when(service.newKnowledgeBuilder(Matchers.<KnowledgeBuilderConfiguration>any())).thenReturn(builder);
        return service;
    }

    protected static KnowledgeBaseFactoryService mockKnowledgeBaseFactoryService() {
        KieBaseConfiguration config = mock(KieBaseConfiguration.class);
        KnowledgeBase base = mockKnowledgeBase();
        KnowledgeBaseFactoryService service = mock(KnowledgeBaseFactoryService.class);
        when(service.newKnowledgeBaseConfiguration(Matchers.<java.util.Properties>any(), Matchers.<java.lang.ClassLoader[]>any())).thenReturn(config);
        when(service.newKnowledgeBase(Matchers.<KieBaseConfiguration>any())).thenReturn(base);
        return service;
    }

    protected static KnowledgeBase mockKnowledgeBase() {
        StatefulKnowledgeSession ksession = mockStatefulKnowledgeSession();//mock(StatefulKnowledgeSession.class);
        KnowledgeBase base = mock(KnowledgeBase.class);
        when(base.newStatefulKnowledgeSession()).thenReturn(ksession);
        return base;
    }

    protected static StatefulKnowledgeSession mockStatefulKnowledgeSession() {
        return mock(StatefulKnowledgeSession.class);
    }

    protected CreationRuleTemplate getMockCreationRuleTemplate() {
        return mock(CreationRuleTemplate.class);
    }

    protected IssueEvent getMockIssueEvent() {
        IssueEvent event = mock(IssueEvent.class);
        when(event.getStatus()).thenReturn(getIssueService().findStatus(1).get());
        return event;
    }

    protected IssueActionFactory getMockIssueActionFactory() {
        IssueActionFactory factory = mock(IssueActionFactory.class);
        when(factory.createIssueAction(Matchers.<String>any())).thenReturn(getMockIssueAction());
        return factory;
    }

    protected IssueAction getMockIssueAction() {
        return mock(IssueAction.class);
    }
}
