/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.impl.module.IssueModule;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionFactory;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueActionService;
import com.elster.jupiter.issue.share.service.IssueAssignmentService;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.SimpleTranslationKey;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.nls.impl.NlsServiceImpl;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Order;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.io.KieResources;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactoryService;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.mockito.Matchers;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
public abstract class BaseTest {
    public static final String ISSUE_DEFAULT_TYPE_PREFIX = "DCI";
    public static final String ISSUE_DEFAULT_TYPE_UUID = "datacollection";
    public static final String ISSUE_DEFAULT_REASON = "reason.default";
    public static final TranslationKey MESSAGE_SEED_DEFAULT_TRANSLATION = new SimpleTranslationKey("issue.entity.default.translation", "Default entity");

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    protected static IssueService issueService;
    protected static EndPointConfigurationService endPointConfigurationService;
    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(TimeService.class).toInstance(mock(TimeService.class));
            bind(SearchService.class).toInstance(mock(SearchService.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));

            TaskService taskService = mock(TaskService.class);
            bind(TaskService.class).toInstance(taskService);

            RecurrentTask recurrentTask = mock(RecurrentTask.class);
            RecurrentTaskBuilder builder = FakeBuilder.initBuilderStub(recurrentTask, RecurrentTaskBuilder.class,
                    RecurrentTaskBuilder.RecurrentTaskBuilderNameSetter.class,
                    RecurrentTaskBuilder.RecurrentTaskBuilderDestinationSetter.class,
                    RecurrentTaskBuilder.RecurrentTaskBuilderPayloadSetter.class,
                    RecurrentTaskBuilder.RecurrentTaskBuilderScheduleSetter.class,
                    RecurrentTaskBuilder.RecurrentTaskBuilderFinisher.class
            );
            when(taskService.newBuilder()).thenReturn(builder);

            bind(KieResources.class).toInstance(mock(KieResources.class));
            bind(KnowledgeBaseFactoryService.class).toInstance(mockKnowledgeBaseFactoryService());
            bind(KnowledgeBuilderFactoryService.class).toInstance(mockKnowledgeBuilderFactoryService());
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HttpService.class).toInstance(mock(HttpService.class));
            endPointConfigurationService = mock(EndPointConfigurationService.class);
        }
    }

    @BeforeClass
    public static void setEnvironment() {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new DataVaultModule(),
                new IdsModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new CalendarModule(),
                new MeteringModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new ThreadSecurityModule(),
                new OrmModule(),
                new UtilModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule(),
                new UserModule(),
                new BpmModule(),
                new FiniteStateMachineModule(),
                new IssueModule(),
                new BasicPropertiesModule(),
                new CustomPropertySetsModule(),
                new AuditServiceModule(),
                new WebServicesModule(),
                new MeteringGroupsModule()
        );

        TransactionService transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(EndPointConfigurationService.class);
            injector.getInstance(WebServicesService.class);
            injector.getInstance(FiniteStateMachineService.class);
            issueService = injector.getInstance(IssueService.class);
            injector.getInstance(DummyIssueProvider.class);
            injector.getInstance(ThreadPrincipalService.class).set(() -> "Test");
            // In OSGI container issue types will be set by separate bundle
            IssueType type = issueService.createIssueType(ISSUE_DEFAULT_TYPE_UUID, MESSAGE_SEED_DEFAULT_TRANSLATION, ISSUE_DEFAULT_TYPE_PREFIX);
            issueService.createReason(ISSUE_DEFAULT_REASON, type, MESSAGE_SEED_DEFAULT_TRANSLATION, MESSAGE_SEED_DEFAULT_TRANSLATION);
            ctx.commit();
        }
        injector.getInstance(ThreadPrincipalService.class).set(() -> "Test");
        ((NlsServiceImpl) injector.getInstance(NlsService.class)).addTranslationKeyProvider((IssueServiceImpl) issueService);
    }

    @AfterClass
    public static void deactivateEnvironment() {
        inMemoryBootstrapModule.deactivate();
    }

    @Before
    public void setThreadPrinciple() {
        injector.getInstance(ThreadPrincipalService.class).set(() -> "Test");
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

    protected PropertySpecService getPropertySpecService() {
        return injector.getInstance(PropertySpecService.class);
    }

    protected IssueDefaultActionsFactory getDefaultActionsFactory() {
        return injector.getInstance(IssueDefaultActionsFactory.class);
    }

    protected OpenIssue createAssigneeInfo() {
        OpenIssueImpl issue = getDataModel().getInstance(OpenIssueImpl.class);
        issue.setReason(getIssueService().findReason(ISSUE_DEFAULT_REASON).orElse(null));
        issue.setStatus(getIssueService().findStatus(IssueStatus.OPEN).orElse(null));
        issue.setPriority(Priority.DEFAULT);
        CreationRule rule = createCreationRule("creation rule" + Instant.now());
        WorkGroup workGroup = getUserService().createWorkGroup("WorkGroupName", "Description");
        User user = getUserService().createUser("UserName", "Description");
        issue.setWorkGroup(workGroup);
        issue.setUser(user);
        issue.setRule(rule);
        issue.save();
        return issue;
    }

    protected OpenIssue createIssueMinInfo() {
        OpenIssueImpl issue = getDataModel().getInstance(OpenIssueImpl.class);
        issue.setReason(getIssueService().findReason(ISSUE_DEFAULT_REASON).orElse(null));
        issue.setStatus(getIssueService().findStatus(IssueStatus.OPEN).orElse(null));
        issue.setPriority(Priority.DEFAULT);
        CreationRule rule = createCreationRule("creation rule" + Instant.now());
        issue.setRule(rule);
        issue.save();
        return issue;
    }

    private CreationRule createCreationRule(String name) {
        CreationRuleBuilder builder = getIssueCreationService().newCreationRule();
        builder.setName(name);
        builder.setTemplate(mockCreationRuleTemplate().getName());
        builder.setIssueType(getIssueService().findIssueType(ISSUE_DEFAULT_TYPE_UUID).orElse(null));
        builder.setReason(getIssueService().findReason(ISSUE_DEFAULT_REASON).orElse(null));
        builder.setPriority(Priority.DEFAULT);
        builder.activate();
        return builder.complete();
    }

    private CreationRuleTemplate mockCreationRuleTemplate() {
        CreationRuleTemplate creationRuleTemplate = mock(CreationRuleTemplate.class);
        when(creationRuleTemplate.getPropertySpecs()).thenReturn(Collections.emptyList());
        when(creationRuleTemplate.getName()).thenReturn("Template");
        when(creationRuleTemplate.getContent()).thenReturn("Content");
        ((IssueServiceImpl) getIssueService()).addCreationRuleTemplate(creationRuleTemplate);
        return creationRuleTemplate;
    }

    protected DataModel getDataModel() {
        return ((IssueServiceImpl) issueService).getDataModel();
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

    protected List<IssueComment> getIssueComments(Issue issue) {
        Query<IssueComment> query = getIssueService().query(IssueComment.class, User.class);
        return query.select(where("issueId").isEqualTo(issue.getId()), Order.ascending("createTime"));
    }

    private static class DummyIssueProvider implements IssueProvider {

        @Inject
        DummyIssueProvider(IssueService issueService) {
            ((IssueServiceImpl) issueService).addIssueProvider(this);
        }

        @Override
        public Optional<? extends OpenIssue> getOpenIssue(OpenIssue issue) {
            OpenIssue spyOpenIssue = mock(OpenIssue.class);
            doAnswer(invocationOnMock -> {
                IssueStatus status = (IssueStatus) invocationOnMock.getArguments()[0];
                return issue.closeInternal(status);
            }).when(spyOpenIssue).close(any());
            return Optional.of(spyOpenIssue);
        }

        @Override
        public Optional<? extends HistoricalIssue> getHistoricalIssue(HistoricalIssue issue) {
            return Optional.of(issue);
        }

        @Override
        public Optional<? extends Issue> findIssue(long id) {
            Issue spyIssue = mock(Issue.class);
            return Optional.of(spyIssue);
        }
    }
}