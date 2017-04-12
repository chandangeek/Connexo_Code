/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.impl.aggregation.ServerDataAggregationService;
import com.elster.jupiter.metering.impl.config.MetrologyPurposeDeletionVetoEventHandler;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MeteringInMemoryBootstrapModule {
    private final Clock clock;
    private final String[] readingTypeRequirements;
    private CustomPropertySetService customPropertySetService;
    private DataAggregationService dataAggregationService;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Injector injector;

    public static MeteringInMemoryBootstrapModule withAllDefaults() {
        return new MeteringInMemoryBootstrapModule();
    }

    public static MeteringInMemoryBootstrapModule withCustomPropertySetService(CustomPropertySetService service) {
        MeteringInMemoryBootstrapModule module = withAllDefaults();
        module.customPropertySetService = service;
        return module;
    }

    public static MeteringInMemoryBootstrapModule withClock(Clock clock) {
        return new MeteringInMemoryBootstrapModule(clock);
    }

    public static MeteringInMemoryBootstrapModule withClockAndReadingTypes(Clock clock, String... requiredReadingTypes) {
        return new MeteringInMemoryBootstrapModule(clock, requiredReadingTypes);
    }

    public MeteringInMemoryBootstrapModule(String... requiredReadingTypes) {
        this(Clock.systemUTC(), requiredReadingTypes);
    }

    private MeteringInMemoryBootstrapModule() {
        this(Clock.systemUTC());
    }

    private MeteringInMemoryBootstrapModule(Clock clock) {
        this(clock, null);
    }

    private MeteringInMemoryBootstrapModule(Clock clock, String... requiredReadingTypes) {
        this.clock = clock;
        this.readingTypeRequirements = requiredReadingTypes;
    }

    public MeteringInMemoryBootstrapModule withDataAggregationService(DataAggregationService dataAggregation) {
        this.dataAggregationService = dataAggregation;
        return this;
    }

    public void activate() {
        List<Module> modules = new ArrayList<>();
        modules.add(new UtilModule(clock));
        modules.add(new MockModule());
        modules.add(inMemoryBootstrapModule);
        modules.add(new IdsModule());
        modules.add(this.readingTypeRequirements != null
                ? new MeteringModule(readingTypeRequirements).withDataAggregationService(dataAggregationService)
                : new MeteringModule().withDataAggregationService(dataAggregationService));
        modules.add(new PartyModule());
        modules.add(new FiniteStateMachineModule());
        modules.add(new UserModule());
        modules.add(new EventsModule());
        modules.add(new InMemoryMessagingModule());
        modules.add(new DomainUtilModule());
        modules.add(new OrmModule());
        modules.add(new ThreadSecurityModule());
        modules.add(new DataVaultModule());
        modules.add(new PubSubModule());
        modules.add(new TransactionModule());
        modules.add(new NlsModule());
        modules.add(new BasicPropertiesModule());
        modules.add(new TimeModule());
        modules.add(new SearchModule());
        modules.add(new UsagePointLifeCycleConfigurationModule());
        if (this.customPropertySetService == null) {
            modules.add(new CustomPropertySetsModule());
        }
        injector = Guice.createInjector(modules.toArray(new Module[modules.size()]));
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(ThreadPrincipalService.class);
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(PropertySpecService.class);
            addMessageHandlers();
            createDefaultUsagePointLifeCycle();
            ctx.commit();
        }
    }

    private void addMessageHandlers() {
        ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(this.injector.getInstance(MetrologyPurposeDeletionVetoEventHandler.class));
        ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(this.injector.getInstance(UsagePointLifeCycleDeletionEventHandler.class));
        ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(this.injector.getInstance(UsagePointStateDeletionEventHandler.class));
        ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(this.injector.getInstance(UsagePointStateChangeEventHandler.class));
    }

    private void createDefaultUsagePointLifeCycle() {
        UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService = injector.getInstance(UsagePointLifeCycleConfigurationService.class);
        usagePointLifeCycleConfigurationService.newUsagePointLifeCycle("Default life cycle").markAsDefault();
    }

    public void deactivate() {
        inMemoryBootstrapModule.deactivate();
    }

    public ServerMetrologyConfigurationService getMetrologyConfigurationService() {
        return injector.getInstance(ServerMetrologyConfigurationService.class);
    }

    public ServerDataAggregationService getServerDataAggregationService() {
        return injector.getInstance(ServerDataAggregationService.class);
    }

    public TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    public ServerMeteringService getMeteringService() {
        return injector.getInstance(ServerMeteringService.class);
    }

    public PropertySpecService getPropertySpecService() {
        return injector.getInstance(PropertySpecService.class);
    }

    public CustomPropertySetService getCustomPropertySetService() {
        return injector.getInstance(CustomPropertySetService.class);
    }

    public Clock getClock() {
        return injector.getInstance(Clock.class);
    }

    public ThreadPrincipalService getThreadPrincipalService() {
        return injector.getInstance(ThreadPrincipalService.class);
    }

    public OrmService getOrmService() {
        return injector.getInstance(OrmService.class);
    }

    public Publisher getPublisher() {
        return injector.getInstance(Publisher.class);
    }

    public FiniteStateMachineService getFiniteStateMachineService() {
        return injector.getInstance(FiniteStateMachineService.class);
    }

    public NlsService getNlsService() {
        return injector.getInstance(NlsService.class);
    }

    public MeteringDataModelService getMeteringDataModelService() {
        return injector.getInstance(MeteringDataModelService.class);
    }

    public UsagePointLifeCycleConfigurationService getUsagePointLifeCycleConfService() {
        return injector.getInstance(UsagePointLifeCycleConfigurationService.class);
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(LicenseService.class).toInstance(mockLicenseService());
            if (customPropertySetService != null) {
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
            }
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    private LicenseService mockLicenseService() {
        LicenseService licenseService = mock(LicenseService.class);
        License license = mock(License.class);
        when(licenseService.getLicenseForApplication("INS")).thenReturn(Optional.of(license));
        return licenseService;
    }

}