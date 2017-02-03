/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.time.Clock;

import static org.mockito.Mockito.mock;

public class CalendarInMemoryBootstrapModule {
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Clock clock;
    private Injector injector;

    public CalendarInMemoryBootstrapModule() {
        super();
    }

    public CalendarInMemoryBootstrapModule(Clock clock) {
        this();
        this.clock = clock;
    }

    public void activate() {
        injector = Guice.createInjector(
                mockModule(),
                inMemoryBootstrapModule,
                new OrmModule(),
                new DataVaultModule(),
                new DomainUtilModule(),
                new NlsModule(),
                new UserModule(),
                utilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(false),
                new InMemoryMessagingModule(),
                new IdsModule(),
                new EventsModule(),
                new CalendarModule()
        );
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            injector.getInstance(ThreadPrincipalService.class);
            injector.getInstance(CalendarService.class);
            ctx.commit();
        }
    }

    private MockModule mockModule() {
        if (this.clock == null) {
            return new MockModule();
        } else {
            return new MockModule(this.clock);
        }
    }

    private UtilModule utilModule() {
        if (this.clock == null) {
            return new UtilModule();
        } else {
            return new UtilModule(this.clock);
        }
    }

    public void deactivate() {
        inMemoryBootstrapModule.deactivate();
    }

    public ServerCalendarService getCalendarService() {
        return injector.getInstance(ServerCalendarService.class);
    }

    public TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    public ThreadPrincipalService getThreadPrincipalService() {
        return injector.getInstance(ThreadPrincipalService.class);
    }

    private static class MockModule extends AbstractModule {
        private Clock clock;

        MockModule() {
            super();
        }

        MockModule(Clock clock) {
            this();
            this.clock = clock;
        }

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(TimeService.class).toInstance(mock(TimeService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
            if (clock == null) {
                bind(Clock.class).toInstance(Clock.systemDefaultZone());
            }
        }
    }
}