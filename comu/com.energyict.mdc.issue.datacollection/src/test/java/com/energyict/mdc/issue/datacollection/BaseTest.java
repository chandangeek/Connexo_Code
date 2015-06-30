package com.energyict.mdc.issue.datacollection;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.validation.MessageInterpolator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;

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
import org.osgi.service.log.LogService;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.impl.module.IssueModule;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.device.topology.impl.TopologyModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.io.impl.MdcIOModule;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.impl.IssueDataCollectionModule;
import com.energyict.mdc.issue.datacollection.impl.IssueDataCollectionServiceImpl;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.impl.TasksModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public abstract class BaseTest {

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());

    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BpmService.class).toInstance(mock(BpmService.class));
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));

            bind(KieResources.class).toInstance(mock(KieResources.class));
            bind(KnowledgeBaseFactoryService.class).toInstance(mockKnowledgeBaseFactoryService());
            bind(KnowledgeBuilderFactoryService.class).toInstance(mockKnowledgeBuilderFactoryService());
            bind(LicenseService.class).toInstance(mock(LicenseService.class));

            Thesaurus thesaurus = mock(Thesaurus.class);
            bind(Thesaurus.class).toInstance(thesaurus);
            bind(MessageInterpolator.class).toInstance(thesaurus);

            bind(LogService.class).toInstance(mock(LogService.class));
        }
    }

    @BeforeClass
    public static void setEnvironment() {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new MeteringGroupsModule(),
                new MeteringModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new DataVaultModule(),
                new com.elster.jupiter.tasks.impl.TaskModule(),
                new KpiModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule(),
                new UserModule(),
                new IssueModule(),
                new MdcIOModule(),
                new MdcReadingTypeUtilServiceModule(),
                new BasicPropertiesModule(),
                new MdcDynamicModule(),
                new EngineModelModule(),
                new PluggableModule(),
                new ProtocolPluggableModule(),
                new ValidationModule(),
                new EstimationModule(),
                new TimeModule(),
                new FiniteStateMachineModule(),
                new DeviceLifeCycleConfigurationModule(),
                new DeviceConfigurationModule(),
                new DeviceDataModule(),
                new MasterDataModule(),
                new TasksModule(),
                new IssuesModule(),
                new SchedulingModule(),
                new ProtocolApiModule(),
                new TopologyModule(),
                new IssueDataCollectionModule()
        );

        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            // initialize Issue tables
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringGroupsService.class);
            injector.getInstance(MasterDataService.class);
            injector.getInstance(IssueDataCollectionService.class);
            ctx.commit();
        }
    }

    @AfterClass
    public static void deactivateEnvironment() {
        inMemoryBootstrapModule.deactivate();
    }

    protected TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    protected TransactionContext getContext() {
        return getTransactionService().getContext();
    }

    protected IssueService getIssueService() {
        return injector.getInstance(IssueService.class);
    }

    protected JsonService getJsonService() {
        return injector.getInstance(JsonService.class);
    }

    protected MeteringService getMeteringService() {
        return injector.getInstance(MeteringService.class);
    }

    protected DeviceService getDeviceService() {
        return injector.getInstance(DeviceService.class);
    }

    protected CommunicationTaskService getCommunicationTaskService() {
        return injector.getInstance(CommunicationTaskService.class);
    }

    protected ConnectionTaskService getConnectionTaskService() {
        return injector.getInstance(ConnectionTaskService.class);
    }

    protected OrmService getOrmService() {
        return injector.getInstance(OrmService.class);
    }

    protected Thesaurus getThesaurus() {
        return injector.getInstance(Thesaurus.class);
    }

    protected IssueDataCollectionService getIssueDataCollectionService() {
        return injector.getInstance(IssueDataCollectionService.class);
    }

    protected Injector getInjector() {
        return injector;
    }

    protected Message getMockMessage(String payload) {
        Message message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload.getBytes());
        return message;
    }

    protected IssueCreationService getMockIssueCreationService() {
        IssueCreationService issueCreationService = mock(IssueCreationService.class);
        doThrow(new DispatchCreationEventException("processed!")).when(issueCreationService).dispatchCreationEvent(Matchers.anyListOf(IssueEvent.class));
        return issueCreationService;
    }

    protected CreationRuleTemplate getMockCreationRuleTemplate() {
        CreationRuleTemplate template = mock(CreationRuleTemplate.class);
        when(template.getName()).thenReturn("template");
        when(template.getContent()).thenReturn("Content");
        ((IssueServiceImpl)getIssueService()).addCreationRuleTemplate(template);
        return template;
    }


    protected CreationRule getCreationRule(String name, String reasonKey) {
        CreationRuleBuilder builder = getIssueService().getIssueCreationService().newCreationRule();
        builder.setName(name);
        builder.setComment("Comment for rule");
        builder.setIssueType(getIssueService().findIssueType(IssueDataCollectionService.DATA_COLLECTION_ISSUE).get());
        builder.setReason(getIssueService().findReason(reasonKey).orElse(null));
        builder.setDueInTime(DueInType.DAY, 15L);
        CreationRuleTemplate template = getMockCreationRuleTemplate();
        builder.setTemplate(template.getName());
        CreationRule rule = builder.complete();
        rule.save();
        return rule;
    }

    protected DataModel getDataModel() {
        return ((IssueDataCollectionServiceImpl) getIssueDataCollectionService()).getDataModel();
    }

    protected DataModel getIssueDataModel() {
        return ((IssueServiceImpl)getIssueService()).getDataModel();
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
        @SuppressWarnings("deprecation")
        KnowledgeBase base = mockKnowledgeBase();
        KnowledgeBaseFactoryService service = mock(KnowledgeBaseFactoryService.class);
        when(service.newKnowledgeBaseConfiguration(Matchers.<java.util.Properties>any(), Matchers.<java.lang.ClassLoader[]>any())).thenReturn(config);
        when(service.newKnowledgeBase(Matchers.<KieBaseConfiguration>any())).thenReturn(base);
        return service;
    }

    @SuppressWarnings("deprecation")
    protected static KnowledgeBase mockKnowledgeBase() {
        StatefulKnowledgeSession ksession = mock(StatefulKnowledgeSession.class);
        KnowledgeBase base = mock(KnowledgeBase.class);
        when(base.newStatefulKnowledgeSession()).thenReturn(ksession);
        return base;
    }

    protected static class DispatchCreationEventException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public DispatchCreationEventException(String message) {
            super(message);
        }
    }
}
