/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.data.push.enddeviceevents;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.calendar.impl.CalendarModule;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.http.whiteboard.TokenService;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.groups.impl.MeteringGroupsModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.impl.SearchModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
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
import com.energyict.mdc.cim.webservices.outbound.soap.EndDeviceEventsServiceProvider;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;

import java.sql.SQLException;

import static org.mockito.Mockito.mock;

public class InMemoryIntegrationPersistence {
    private InMemoryBootstrapModule bootstrapModule;
    private Injector injector;
    private OrmService ormService;
    private TransactionService transactionService;
    private EndDeviceEventMessageHandlerFactory endDeviceEventMessageHandlerFactory;

    public InMemoryIntegrationPersistence() {
        super();
    }

    public void initializeDatabase() throws SQLException {
        bootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new EventsModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new MeteringModule(
                        "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0",
                        "0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
                new MeteringGroupsModule(),
                new BasicPropertiesModule(),
                new WebServicesModule(),
                new TransactionModule(),
                new CalendarModule(),
                new FiniteStateMachineModule(),
                new TimeModule(),
                new BpmModule(),
                new CustomPropertySetsModule(),
                new IdsModule(),
                new InMemoryMessagingModule(),
                new OrmModule(),
                new PubSubModule(),
                new SearchModule(),
                new UserModule(),
                new DataVaultModule(),
                new H2OrmModule(),
                new PartyModule(),
                new ThreadSecurityModule(),
                new TaskModule(),
                new UtilModule(),
                new UsagePointLifeCycleConfigurationModule()
        );
        transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            ormService = injector.getInstance(OrmService.class);
            transactionService = injector.getInstance(TransactionService.class);
            endDeviceEventMessageHandlerFactory = injector.getInstance(EndDeviceEventMessageHandlerFactory.class);
            ctx.commit();
        }
    }

    public void cleanUpDataBase() throws SQLException {
        bootstrapModule.deactivate();
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public OrmService getOrmService() {
        return ormService;
    }

    public EndDeviceEventMessageHandlerFactory getEndDeviceEventMessageHandlerFactory() {
        return endDeviceEventMessageHandlerFactory;
    }

    public <T> T getInstance(Class<T> tClass) {
        return injector.getInstance(tClass);
    }

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(TokenService.class).toInstance(mock(TokenService.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(HttpService.class).toInstance(mock(HttpService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            bind(EndDeviceEventsServiceProvider.class).toInstance(mock(EndDeviceEventsServiceProvider.class));
        }
    }
}
