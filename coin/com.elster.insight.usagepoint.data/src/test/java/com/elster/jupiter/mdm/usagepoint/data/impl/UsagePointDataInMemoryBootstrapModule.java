/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.audit.impl.AuditServiceModule;
import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.impl.EstimationModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.kpi.impl.KpiModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.config.impl.UsagePointConfigModule;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataCompletionService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointService;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoritesService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.soap.whiteboard.cxf.impl.WebServicesModule;
import com.elster.jupiter.tasks.impl.TaskModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.impl.UsagePointLifeCycleConfigurationModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.impl.ValidationModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import java.time.Clock;

import static org.mockito.Mockito.mock;

public class UsagePointDataInMemoryBootstrapModule {

    public static final String BULK_A_PLUS_WH = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
    public static final String BULK_A_PLUS_KWH = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Injector injector;

    public void activate() {
        activate(null);
    }

    public void activate(Clock clock) {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new H2OrmModule(),
                new DataVaultModule(),
                new DomainUtilModule(),
                new NlsModule(),
                new UserModule(),
                new BpmModule(),
                clock == null ? new UtilModule() : new UtilModule(clock),
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
                new CalendarModule(),
                new MeteringModule(BULK_A_PLUS_WH, BULK_A_PLUS_KWH),
                new CustomPropertySetsModule(),
                new UsagePointConfigModule(),
                new KpiModule(),
                new ValidationModule(),
                new EstimationModule(),
                new MeteringGroupsModule(),
                new UsagePointDataModule(),
                new BasicPropertiesModule(),
                new TimeModule(),
                new SearchModule(),
                new AuditServiceModule(),
                new WebServicesModule()
        );
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(ThreadPrincipalService.class);
            injector.getInstance(UsagePointLifeCycleConfigurationService.class)
                    .newUsagePointLifeCycle("Default life cycle")
                    .markAsDefault();
            injector.getInstance(UsagePointConfigurationService.class);
            injector.getInstance(UsagePointDataModelService.class);
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

    public MeteringGroupsService getMeteringGroupsService() {
        return injector.getInstance(MeteringGroupsService.class);
    }

    public CustomPropertySetService getCustomPropertySetService() {
        return injector.getInstance(CustomPropertySetService.class);
    }

    public UsagePointService getUsagePointService() {
        return injector.getInstance(UsagePointDataModelServiceImpl.class).getUsagePointService();
    }

    public UsagePointDataCompletionService getUsagePointDataCompletionService() {
        return injector.getInstance(UsagePointDataModelServiceImpl.class).getUsagePointDataCompletionService();
    }

    public FavoritesService getFavoritesService() {
        return injector.getInstance(UsagePointDataModelServiceImpl.class).getFavoritesService();
    }

    public PropertySpecService getPropertySpecService() {
        return injector.getInstance(PropertySpecService.class);
    }

    public ThreadPrincipalService getThreadPrincipalService() {
        return injector.getInstance(ThreadPrincipalService.class);
    }

    public UserService getUserService() {
        return injector.getInstance(UserService.class);
    }

    public ValidationService getValidationService() {
        return injector.getInstance(ValidationService.class);
    }

    public EstimationService getEstimationService() {
        return injector.getInstance(EstimationService.class);
    }

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(LogService.class).toInstance(mock(LogService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(HttpService.class).toInstance(mock(HttpService.class));
        }
    }
}
