/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
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
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetrologyInMemoryBootstrapModule {
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Injector injector;
    private boolean printSql;

    public void setPrintSql(boolean printSql) {
        this.printSql = printSql;
    }

    public void activate() {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new UsagePointConfigModule(),
                new IdsModule(),
                new UsagePointLifeCycleConfigurationModule(),
                new CalendarModule(),
                new MeteringModule(),
                new PartyModule(),
                new FiniteStateMachineModule(),
                new UserModule(),
                new EventsModule(),
                new InMemoryMessagingModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new DataVaultModule(),
                new PubSubModule(),
                new TransactionModule(printSql),
                new NlsModule(),
                new KpiModule(),
                new ValidationModule(),
                new EstimationModule(),
                new MeteringGroupsModule(),
                new TaskModule(),
                new BasicPropertiesModule(),
                new TimeModule(),
                new CustomPropertySetsModule(),
                new SearchModule()
        );
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(ThreadPrincipalService.class);
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(ValidationService.class);
            injector.getInstance(EstimationService.class);
            injector.getInstance(PropertySpecService.class);
            injector.getInstance(CustomPropertySetService.class);
            injector.getInstance(UsagePointConfigurationService.class);
            addMessageHandlers();
            ctx.commit();
        }
    }

    public void deactivate() {
        inMemoryBootstrapModule.deactivate();
    }

    public UsagePointConfigurationService getUsagePointConfigurationService() {
        return injector.getInstance(UsagePointConfigurationService.class);
    }

    public TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    public ServerMeteringService getMeteringService() {
        return injector.getInstance(ServerMeteringService.class);
    }

    public ValidationService getValidationService() {
        return injector.getInstance(ValidationService.class);
    }

    public EstimationService getEstimationService() {
        return injector.getInstance(EstimationService.class);
    }

    public PropertySpecService getPropertySpecService() {
        return injector.getInstance(PropertySpecService.class);
    }

    public CustomPropertySetService getCustomPropertySetService() {
        return injector.getInstance(CustomPropertySetService.class);
    }

    public ServerMetrologyConfigurationService getMetrologyConfigurationService() {
        return (ServerMetrologyConfigurationService) injector.getInstance(MetrologyConfigurationService.class);
    }

    private void addMessageHandlers() {
        MetrologyContractDeletionEventHandler metrologyContractDeletionEventHandler = injector.getInstance(MetrologyContractDeletionEventHandler.class);
        ((EventServiceImpl) this.injector.getInstance(EventService.class)).addTopicHandler(metrologyContractDeletionEventHandler);
    }

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(LicenseService.class).toInstance(mockLicenseService());
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    private static LicenseService mockLicenseService() {
        LicenseService licenseService = mock(LicenseService.class);
        License license = mock(License.class);
        when(licenseService.getLicenseForApplication(any())).thenReturn(Optional.of(license));
        return licenseService;
    }
}
