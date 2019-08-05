/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl;

import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fileimport.impl.FileImportModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionPropertiesProvider;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.fsm.impl.StateTransitionTriggerEventTopicHandler;
import com.elster.jupiter.http.whiteboard.HttpAuthenticationService;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.impl.module.IssueModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.impl.config.MetrologyConfigurationModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pki.impl.PkiModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.impl.ServiceCallModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.UsagePointLifeCycleService;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.usagepoint.lifecycle.impl.UsagePointInitialStateChangeRequestHandler;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.impl.ValidationModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
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
import java.sql.SQLException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InMemoryIntegrationPersistence {
    private static final Clock clock = mock(Clock.class);

    private TransactionService transactionService;
    private InMemoryBootstrapModule bootstrapModule;
    private Injector injector;
    private LicenseService licenseService;
    private MetrologyConfigurationService metrologyConfigurationService;
    private FiniteStateMachineService finiteStateMachineService;
    private ThreadPrincipalService threadPrincipalService;
    private UsagePointLifeCycleService usagePointLifeCycleService;

    public InMemoryIntegrationPersistence() {
        super();
    }

    public InMemoryIntegrationPersistence(MetrologyConfigurationService metrologyConfigurationService) {
        this();
        this.metrologyConfigurationService = metrologyConfigurationService;
    }

    public void initializeDatabase(String testName, boolean showSqlLogging) throws SQLException {
        this.bootstrapModule = new InMemoryBootstrapModule();
        License license = mock(License.class);
        this.licenseService = mock(LicenseService.class);
        when(this.licenseService.getLicenseForApplication(anyString())).thenReturn(Optional.of(license));
        Properties properties = new Properties();
        properties.put("protocols", "all");
        when(license.getLicensedValues()).thenReturn(properties);
        this.injector = Guice.createInjector(this.getModules(showSqlLogging));
        this.transactionService = this.injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = this.transactionService.getContext()) {
            injector.getInstance(CustomPropertySetService.class);
            this.transactionService = this.injector.getInstance(TransactionService.class);
            injector.getInstance(ServiceCallService.class);
            injector.getInstance(CustomPropertySetService.class);
            if (metrologyConfigurationService == null) {
                StateTransitionTriggerEventTopicHandler stateTransitionTriggerEventTopicHandler = new StateTransitionTriggerEventTopicHandler(
                        this.injector.getInstance(EventService.class),
                        this.injector.getInstance(BpmService.class),
                        this.injector.getInstance(StateTransitionPropertiesProvider.class));
                ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(stateTransitionTriggerEventTopicHandler);
                com.elster.jupiter.metering.impl.StateTransitionChangeEventTopicHandler meteringTopicHandler =
                        new com.elster.jupiter.metering.impl.StateTransitionChangeEventTopicHandler(Clock.systemDefaultZone(),
                                this.injector.getInstance(FiniteStateMachineService.class),
                                this.injector.getInstance(MeteringService.class));
                ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(meteringTopicHandler);
                UsagePointInitialStateChangeRequestHandler stateTransitionChangeEventTopicHandler =
                        new UsagePointInitialStateChangeRequestHandler();
                ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(stateTransitionChangeEventTopicHandler);
                this.finiteStateMachineService = this.injector.getInstance(FiniteStateMachineService.class);
                this.threadPrincipalService = this.injector.getInstance(ThreadPrincipalService.class);
            }

            ctx.commit();
        }
    }


    private Module[] getModules(boolean showSqlLogging) {
        List<Module> modules = new ArrayList<>();
        Collections.addAll(modules,
                new MockModule(),
                bootstrapModule,
                new OrmModule(),
                new ServiceCallModule(),
                new CustomPropertySetsModule(),
                new DataVaultModule(),
                new InMemoryMessagingModule(),
                new IdsModule(),
                new PkiModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new MeteringModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new DataVaultModule(),
                new TaskModule(),
                new UtilModule(clock),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(showSqlLogging),
                new NlsModule(),
                new UserModule(),
                new BpmModule(),
                new MeteringGroupsModule(),
                new SearchModule(),
                new FiniteStateMachineModule(),
                new BasicPropertiesModule(),
                new KpiModule(),
                new ValidationModule(),
                new EstimationModule(),
                new TimeModule(),
                new IssueModule(),
                new UsagePointIssueDataValidationModule(),
                new CalendarModule(),
                new WebServicesModule(),
                new AuditServiceModule(),
                new FileImportModule()
        );
        if (this.metrologyConfigurationService == null) {
            modules.add(new MetrologyConfigurationModule());
        }
        return modules.toArray(new Module[modules.size()]);
    }

    public void cleanUpDataBase() throws SQLException {
        this.bootstrapModule.deactivate();
    }

    public Clock getClock() {
        return clock;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public FiniteStateMachineService getFiniteStateMachineService() {
        return finiteStateMachineService;
    }

    public ThreadPrincipalService getThreadPrincipalService() {
        return threadPrincipalService;
    }

    public UsagePointLifeCycleService getUsagePointLifecycleService() {
        return usagePointLifeCycleService;
    }

    public <T> T getService(Class<T> serviceClass) {
        return this.injector.getInstance(serviceClass);
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Clock.class).toInstance(clock);
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));

            bind(KieResources.class).to(ResourceFactoryServiceImpl.class);
            bind(KnowledgeBaseFactoryService.class).to(KnowledgeBaseFactoryServiceImpl.class);
            bind(KnowledgeBuilderFactoryService.class).to(KnowledgeBuilderFactoryServiceImpl.class);

            Thesaurus thesaurus = mock(Thesaurus.class);
            when(thesaurus.getString(anyString(), anyString())).then(invocation -> invocation.getArgumentAt(1, String.class));
            bind(Thesaurus.class).toInstance(thesaurus);
            bind(MessageInterpolator.class).toInstance(thesaurus);

            bind(StateTransitionPropertiesProvider.class).toInstance(mock(StateTransitionPropertiesProvider.class));
            bind(HttpAuthenticationService.class).toInstance(mock(HttpAuthenticationService.class));
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(LicenseService.class).toInstance(licenseService);
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());

            bind(PropertyValueInfoService.class).toInstance(mock(PropertyValueInfoService.class));

            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(AppService.class).toInstance(mock(AppService.class));
        }
    }

}
