/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;

import static org.mockito.Mockito.mock;

public class InMemoryPersistence {

    private InMemoryBootstrapModule bootstrapModule;
    private Injector injector;

    public void initializeDatabase() {
        this.bootstrapModule = new InMemoryBootstrapModule();
        injector = Guice.createInjector(
                new MockModule(),
                bootstrapModule,
                new UtilModule(),
                new NlsModule(),
                new DomainUtilModule(),
                new ThreadSecurityModule(),
                new UserModule(),
                new OrmModule(),
                new PubSubModule(),
                new InMemoryMessagingModule(),
                new FiniteStateMachineModule(),
                new DataVaultModule(),
                new EventsModule(),
                new TransactionModule(),
                new UsagePointLifeCycleConfigurationModule());
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext ctx = transactionService.getContext()) {
            injector.getInstance(UsagePointLifeCycleConfigurationService.class).addMicroActionFactory(new TestMicroAction.Factory());
            injector.getInstance(UsagePointLifeCycleConfigurationService.class).addMicroCheckFactory(new TestMicroCheck.Factory());
            ctx.commit();
        }
    }

    public void cleanUpDataBase() throws SQLException {
        this.bootstrapModule.deactivate();
    }

    public <T> T get(Class<T> clazz) {
        return this.injector.getInstance(clazz);
    }

    private class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }
}
