/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.PasswordDecryptService;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.hsm.HsmEncryptionService;
import com.elster.jupiter.hsm.HsmEnergyService;
import com.elster.jupiter.users.blacklist.BlackListModule;
import com.elster.jupiter.http.whiteboard.TokenModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.impl.module.IssueModule;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.metering.zone.impl.MeteringZoneModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pki.impl.PkiModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.impl.ServiceCallModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.validation.impl.ValidationModule;
import com.energyict.mdc.device.config.impl.DeviceConfigurationModule;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceDataModule;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.lifecycle.config.impl.DeviceLifeCycleConfigurationModule;
import com.energyict.mdc.dynamic.impl.MdcDynamicModule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issues.impl.IssuesModule;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.impl.MasterDataModule;
import com.energyict.mdc.metering.impl.MdcReadingTypeUtilServiceModule;
import com.energyict.mdc.pluggable.impl.PluggableModule;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.impl.ProtocolApiModule;
import com.energyict.mdc.protocol.api.services.CustomPropertySetInstantiatorService;
import com.energyict.mdc.protocol.pluggable.impl.ProtocolPluggableModule;
import com.energyict.mdc.scheduling.SchedulingModule;
import com.energyict.mdc.tasks.impl.TasksModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.drools.compiler.builder.impl.KnowledgeBuilderFactoryServiceImpl;
import org.drools.core.impl.KnowledgeBaseFactoryServiceImpl;
import org.drools.core.io.impl.ResourceFactoryServiceImpl;
import org.kie.api.io.KieResources;
import org.kie.internal.KnowledgeBaseFactoryService;
import org.kie.internal.builder.KnowledgeBuilderFactoryService;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import javax.validation.MessageInterpolator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.mockito.Matchers;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseIT {
    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));

            bind(KieResources.class).to(ResourceFactoryServiceImpl.class);
            bind(KnowledgeBaseFactoryService.class).to(KnowledgeBaseFactoryServiceImpl.class);
            bind(KnowledgeBuilderFactoryService.class).to(KnowledgeBuilderFactoryServiceImpl.class);
            bind(LicenseService.class).toInstance(mock(LicenseService.class));

            Thesaurus thesaurus = mock(Thesaurus.class);
            bind(Thesaurus.class).toInstance(thesaurus);
            bind(MessageInterpolator.class).toInstance(thesaurus);

            bind(LogService.class).toInstance(mock(LogService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());

            bind(CustomPropertySetInstantiatorService.class).toInstance(mock(CustomPropertySetInstantiatorService.class));
            bind(DeviceMessageSpecificationService.class).toInstance(mock(DeviceMessageSpecificationService.class));

            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(HsmEnergyService.class).toInstance(mock(HsmEnergyService.class));
            bind(HsmEncryptionService.class).toInstance(mock(HsmEncryptionService.class));
            bind(AppService.class).toInstance(mock(AppService.class));

            bind(PasswordDecryptService.class).toInstance(mock(PasswordDecryptService.class));
        }
    }

    @BeforeClass
    public static void setEnvironment() {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new CustomPropertySetsModule(),
                new IdsModule(),
                new MeteringGroupsModule(),
                new SearchModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new MeteringModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new H2OrmModule(),
                new PkiModule(),
                new DataVaultModule(),
                new com.elster.jupiter.tasks.impl.TaskModule(),
                new KpiModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(),
                new NlsModule(),
                new UserModule(),
                new BpmModule(),
                new IssueModule(),
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
                new IssueDataValidationModule(),
                new CalendarModule(),
                new ServiceCallModule(),
                new WebServicesModule(),
                new AuditServiceModule(),
                new FileImportModule(),
                new MeteringZoneModule(),
                new TokenModule(),
                new BlackListModule()
        );

        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            // initialize Issue tables
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringGroupsService.class);
            injector.getInstance(MasterDataService.class);
            injector.getInstance(AuditService.class);
            injector.getInstance(IssueDataValidationService.class);
            injector.getInstance(MeteringZoneService.class);
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

    protected Thesaurus getThesaurus() {
        return injector.getInstance(Thesaurus.class);
    }

    protected IssueDataValidationService getIssueDataValidationService() {
        return injector.getInstance(IssueDataValidationService.class);
    }

    protected UserService getUserService() {
        return injector.getInstance(UserService.class);
    }

    protected ThreadPrincipalService getThreadPrincipalService() {
        return injector.getInstance(ThreadPrincipalService.class);
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
        ((IssueServiceImpl) getIssueService()).addCreationRuleTemplate(template);
        return template;
    }

    protected DataModel getDataModel() {
        return ((IssueDataValidationServiceImpl) getIssueDataValidationService()).getDataModel();
    }

    protected DataModel getIssueDataModel() {
        return ((IssueServiceImpl) getIssueService()).getDataModel();
    }

    protected DataValidationActionsFactory getDefaultActionsFactory() {
        return injector.getInstance(DataValidationActionsFactory.class);
    }

    protected static class DispatchCreationEventException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public DispatchCreationEventException(String message) {
            super(message);
        }
    }
}
