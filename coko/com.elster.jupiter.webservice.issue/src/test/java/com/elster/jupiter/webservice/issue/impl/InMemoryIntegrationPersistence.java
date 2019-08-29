/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl;

import com.elster.jupiter.audit.impl.AuditModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.issue.impl.module.IssueModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesDataModelService;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.webservice.issue.WebServiceIssueService;

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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InMemoryIntegrationPersistence {
    private static final Clock clock = mock(Clock.class);

    private TransactionService transactionService;
    private InMemoryBootstrapModule bootstrapModule;
    private Injector injector;

    public InMemoryIntegrationPersistence() {
        super();
    }

    public void initializeDatabase(boolean showSqlLogging) throws SQLException {
        this.bootstrapModule = new InMemoryBootstrapModule();
        this.injector = Guice.createInjector(this.getModules(showSqlLogging));
        DataModel dataModel = injector.getInstance(DataModel.class);
        when(dataModel.getInstance(any())).thenAnswer(invocationOnMock -> injector.getInstance(invocationOnMock.getArgumentAt(0, Class.class)));
        this.transactionService = this.injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            getService(WebServicesDataModelService.class);
            getService(WebServiceIssueService.class);
            ctx.commit();
        }
    }

    private Module[] getModules(boolean showSqlLogging) {
        List<Module> modules = new ArrayList<>();
        Collections.addAll(modules,
                new MockModule(),
                bootstrapModule,
                new OrmModule(),
                new InMemoryMessagingModule(),
                new MeteringModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new UtilModule(clock),
                new TransactionModule(showSqlLogging),
                new NlsModule(),
                new UserModule(),
                new BasicPropertiesModule(),
                new IssueModule(),
                new WebServiceIssueModule(),
                new WebServicesModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new IdsModule(),
                new PartyModule(),
                new FiniteStateMachineModule(),
                new CustomPropertySetsModule(),
                new SearchModule(),
                new TaskModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new TimeModule(),
                new CalendarModule(),
                new DataVaultModule(),
                new BpmModule(),
                new AuditModule()
        );
        return modules.toArray(new Module[modules.size()]);
    }

    public void dropDatabase() throws SQLException {
        this.bootstrapModule.deactivate();
    }

    public Clock getClock() {
        return clock;
    }

    public TransactionService getTransactionService() {
        return transactionService;
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
            bind(LicenseService.class).toInstance(mock(LicenseService.class));

            bind(Thesaurus.class).toInstance(NlsModule.FakeThesaurus.INSTANCE);
            bind(MessageInterpolator.class).toInstance(NlsModule.FakeThesaurus.INSTANCE);

            bind(LogService.class).toInstance(mock(LogService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());

            bind(DataModel.class).toInstance(mock(DataModel.class));

            bind(HttpService.class).toInstance(mock(HttpService.class));

            bind(KieResources.class).to(ResourceFactoryServiceImpl.class);
            bind(KnowledgeBuilderFactoryService.class).to(KnowledgeBuilderFactoryServiceImpl.class);
            bind(KnowledgeBaseFactoryService.class).to(KnowledgeBaseFactoryServiceImpl.class);
        }
    }
}
