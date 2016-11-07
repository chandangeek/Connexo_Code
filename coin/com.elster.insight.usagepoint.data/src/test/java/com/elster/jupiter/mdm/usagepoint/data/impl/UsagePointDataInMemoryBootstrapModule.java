package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.config.impl.UsagePointConfigModule;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.impl.ValidationModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import static org.mockito.Mockito.mock;

public class UsagePointDataInMemoryBootstrapModule {
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Injector injector;

    public void activate() {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new OrmModule(),
                new DataVaultModule(),
                new DomainUtilModule(),
                new NlsModule(),
                new UserModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(false),
                new InMemoryMessagingModule(),
                new TaskModule(),
                new IdsModule(),
                new EventsModule(),
                new PartyModule(),
                new FiniteStateMachineModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new MeteringModule(),
                new CustomPropertySetsModule(),
                new UsagePointConfigModule(),
                new KpiModule(),
                new ValidationModule(),
                new MeteringGroupsModule(),
                new UsagePointDataModule(),
                new BasicPropertiesModule()

        );
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(UsagePointConfigurationService.class);
            injector.getInstance(UsagePointDataService.class);
            ctx.commit();
        }
    }

    public void deactivate() {
        inMemoryBootstrapModule.deactivate();
    }

    public TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    public UsagePointConfigurationService getUsagePointConfigurationService() {
        return injector.getInstance(UsagePointConfigurationService.class);
    }

    public MeteringService getMeteringService() {
        return injector.getInstance(MeteringService.class);
    }

    public CustomPropertySetService getCustomPropertySetService() {
        return injector.getInstance(CustomPropertySetService.class);
    }

    public UsagePointDataService getUsagePointDataService() {
        return injector.getInstance(UsagePointDataService.class);
    }

    public PropertySpecService getPropertySpecService() {
        return injector.getInstance(PropertySpecService.class);
    }

    public ThreadPrincipalService getThreadPrincipalService() {
        return injector.getInstance(ThreadPrincipalService.class);
    }

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(SearchService.class).toInstance(mock(SearchService.class));
            bind(TimeService.class).toInstance(mock(TimeService.class));
        }
    }
}
