/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.usagepoint.lifecycle.impl.UsagePointLifeCycleModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public void initializeDataBase() {
        this.bootstrapModule = new InMemoryBootstrapModule();
        List<Module> modules = new ArrayList<>();
        modules.add(new UtilModule(clock));
        modules.add(new MockModule());
        modules.add(new IdsModule());
        modules.add(new MeteringModule());
        modules.add(new PartyModule());
        modules.add(new FiniteStateMachineModule());
        modules.add(new UserModule());
        modules.add(new EventsModule());
        modules.add(new DomainUtilModule());
        modules.add(new OrmModule());
        modules.add(new ThreadSecurityModule());
        modules.add(new TransactionModule());
        modules.add(new NlsModule());
        modules.add(new InMemoryMessagingModule());
        modules.add(new BasicPropertiesModule());
        modules.add(new CustomPropertySetsModule());
        modules.add(new SearchModule());
        modules.add(new PubSubModule());
        modules.add(new DataVaultModule());
        modules.add(new TimeModule());
        modules.add(new UsagePointLifeCycleConfigurationModule());
        modules.add(new UsagePointLifeCycleModule());
        modules.add(new CalendarModule());
        modules.add(new TaskModule());
        modules.add(bootstrapModule);

        injector = Guice.createInjector(modules.toArray(new Module[modules.size()]));
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(ThreadPrincipalService.class);
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(PropertySpecService.class);
            createDefaultUsagePointLifeCycle();
            ctx.commit();
        }
        this.transactionService = this.injector.getInstance(TransactionService.class);
    }

    public void cleanUpDataBase() {
        this.bootstrapModule.deactivate();
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public <T> T getService(Class<T> serviceClass) {
        return this.injector.getInstance(serviceClass);
    }

    public Clock getClock() {
        return clock;
    }

    private class MockModule extends AbstractModule {
        @Override
        public void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(LicenseService.class).toInstance(mockLicenseService());

            Thesaurus thesaurus = mock(Thesaurus.class);
            when(thesaurus.getFormat(any(TranslationKey.class)))
                    .thenAnswer(invocation -> ((TranslationKey) invocation.getArguments()[0]).getDefaultFormat());
            when(thesaurus.getFormat(any(MessageSeed.class)))
                    .thenAnswer(invocation -> ((MessageSeed) invocation.getArguments()[0]).getDefaultFormat());
            bind(Thesaurus.class).toInstance(thesaurus);
            bind(MessageInterpolator.class).toInstance(thesaurus);

        }
    }

    private void createDefaultUsagePointLifeCycle() {
        UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService = injector.getInstance(UsagePointLifeCycleConfigurationService.class);
        UsagePointLifeCycle lifeCycle = usagePointLifeCycleConfigurationService.newUsagePointLifeCycle("Default life cycle");
        lifeCycle.markAsDefault();
    }

    private LicenseService mockLicenseService() {
        LicenseService licenseService = mock(LicenseService.class);
        License license = mock(License.class);
        when(licenseService.getLicenseForApplication("INS")).thenReturn(Optional.of(license));
        return licenseService;
    }
}

