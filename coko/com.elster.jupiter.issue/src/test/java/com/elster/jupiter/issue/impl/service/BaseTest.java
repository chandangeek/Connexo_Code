package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.impl.module.IssueModule;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.IssueAction;
import com.elster.jupiter.issue.share.cep.IssueActionFactory;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.entity.*;
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
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.TestRule;
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

import java.util.logging.Level;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore("Base functionality for all tests")
public class BaseTest {
    public static final String ISSUE_DEFAULT_TYPE_UUID = "datacollection";
    public static final String ISSUE_DEFAULT_REASON = "reason.default";
    public static final MessageSeed MESSAGE_SEED_DEFAULT_TRANSLATION = new MessageSeed() {
        @Override
        public String getModule() {
            return IssueService.COMPONENT_NAME;
        }
        @Override
        public int getNumber() {
            return 0;
        }
        @Override
        public String getKey() {
            return "issue.entity.default.translation";
        }
        @Override
        public String getDefaultFormat() {
            return "Default entity";
        }
        @Override
        public Level getLevel() {
            return Level.INFO;
        }
    };

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static IssueService issueService;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

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
                new FiniteStateMachineModule(),
                new IssueModule()
        );

        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(FiniteStateMachineService.class);
            // In OSGI container issue types will be set by separate bundle
            issueService = injector.getInstance(IssueService.class);
            IssueType type = issueService.createIssueType(ISSUE_DEFAULT_TYPE_UUID, MESSAGE_SEED_DEFAULT_TRANSLATION);
            issueService.createReason(ISSUE_DEFAULT_REASON, type, MESSAGE_SEED_DEFAULT_TRANSLATION);
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

    protected TransactionContext getContext() {
        return getTransactionService().getContext();
    }

    protected UserService getUserService() {
        return injector.getInstance(UserService.class);
    }

    protected IssueService getIssueService() {
        return injector.getInstance(IssueService.class);
    }

    protected IssueCreationService getIssueCreationService() {
        return issueService.getIssueCreationService();
    }

    protected IssueActionService getIssueActionService() {
        return issueService.getIssueActionService();
    }

    protected IssueAssignmentService getIssueAssignmentService() {
        return issueService.getIssueAssignmentService();
    }

    protected ThreadPrincipalService getThreadPrincipalService() {
        return injector.getInstance(ThreadPrincipalService.class);
    }

    protected CreationRule getSimpleCreationRule() {
        CreationRule rule = getIssueCreationService().createRule();
        rule.setName("Simple Rule " + (1 + Math.random() * 10000));
        rule.setComment("Comment for rule");
        rule.setContent("Empty content");
        rule.setReason(getIssueService().findReason(ISSUE_DEFAULT_REASON).orElse(null));
        rule.setDueInValue(15L);
        rule.setDueInType(DueInType.DAY);
        rule.setTemplateUuid("Parent template uuid");
        rule.save();
        return rule;
    }

    protected OpenIssue createIssueMinInfo() {
        try (TransactionContext context = getContext()) {
            OpenIssue issue = getDataModel().getInstance(OpenIssueImpl.class);
            issue.setReason(getIssueService().findReason(ISSUE_DEFAULT_REASON).orElse(null));
            issue.setStatus(getIssueService().findStatus(IssueStatus.OPEN).orElse(null));
            CreationRule rule = getSimpleCreationRule();
            rule.setName("create-issue-min-info");
            issue.setRule(rule);
            issue.save();
            context.commit();
            return issue;
        }
    }

    protected DataModel getDataModel() {
        return ((IssueServiceImpl)issueService).getDataModel();
    }

    protected static KnowledgeBuilderFactoryService mockKnowledgeBuilderFactoryService() {
        KnowledgeBuilderConfiguration config = mock(KnowledgeBuilderConfiguration.class);
        KnowledgeBuilder builder = mock(KnowledgeBuilder.class);
        KnowledgeBuilderFactoryService service = mock(KnowledgeBuilderFactoryService.class);
        when(service.newKnowledgeBuilderConfiguration(Matchers.<java.util.Properties>any(), Matchers.<java.lang.ClassLoader[]>any())).thenReturn(config);
        when(service.newKnowledgeBuilder(Matchers.<KnowledgeBuilderConfiguration>any())).thenReturn(builder);
        return service;
    }

    @SuppressWarnings("deprecation")
    protected static KnowledgeBaseFactoryService mockKnowledgeBaseFactoryService() {
        KieBaseConfiguration config = mock(KieBaseConfiguration.class);
        KnowledgeBase base = mockKnowledgeBase();
        KnowledgeBaseFactoryService service = mock(KnowledgeBaseFactoryService.class);
        when(service.newKnowledgeBaseConfiguration(Matchers.<java.util.Properties>any(), Matchers.<java.lang.ClassLoader[]>any())).thenReturn(config);
        when(service.newKnowledgeBase(Matchers.<KieBaseConfiguration>any())).thenReturn(base);
        return service;
    }

    @SuppressWarnings("deprecation")
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
        CreationRuleTemplate mock = mock(CreationRuleTemplate.class);
        when(mock.getUUID()).thenReturn("uuid");
        return mock;
    }

    protected IssueEvent getMockIssueEvent() {
        return mock(IssueEvent.class);
    }

    protected IssueActionFactory getMockIssueActionFactory() {
        IssueActionFactory factory = mock(IssueActionFactory.class);
        when(factory.createIssueAction(Matchers.<String>any())).thenReturn(getMockIssueAction());
        when(factory.getId()).thenReturn("id");
        return factory;
    }

    protected IssueAction getMockIssueAction() {
        return mock(IssueAction.class);
    }
}
