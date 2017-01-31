/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.energyict.mdc.ExpectedErrorRule;
import com.energyict.mdc.engine.config.impl.EngineModelModule;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;

import java.util.Optional;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersistenceTest {
    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    protected static final long DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_ID = 1;

    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedErrorRule = new ExpectedErrorRule();
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    protected InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass;

    @BeforeClass
    public static void staticSetUp() {
        BundleContext bundleContext = mock(BundleContext.class);
        injector = Guice.createInjector(
                new MockModule(bundleContext),
                inMemoryBootstrapModule,
                new DataVaultModule(),
                new OrmModule(),
                new UtilModule(),
                new EventsModule(),
                new NlsModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new InMemoryMessagingModule(),
                new DomainUtilModule(),
                new EventsModule(),
                new TransactionModule(false),
                new UserModule(),
                new EngineModelModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
            injector.getInstance(NlsService.class); // fake call to make sure component is initialized
            injector.getInstance(ProtocolPluggableService.class); // fake call to make sure component is initialized
            injector.getInstance(UserService.class); // fake call to make sure component is initialized
            injector.getInstance(EngineConfigurationService.class); // fake call to make sure component is initialized
            ctx.commit();
        }
    }

    @AfterClass
    public static void staticTearDown() {
    	inMemoryBootstrapModule.deactivate();
    }

    @Before
    public void setUp() {
        inboundDeviceProtocolPluggableClass = mock(InboundDeviceProtocolPluggableClass.class);
        when(inboundDeviceProtocolPluggableClass.getId()).thenReturn(DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(getProtocolPluggableService()
                .findInboundDeviceProtocolPluggableClass(DISCOVERY_PROTOCOL_PLUGGABLE_CLASS_ID))
                .thenReturn(Optional.of(inboundDeviceProtocolPluggableClass));
    }

    public static TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    public final EngineConfigurationService getEngineModelService() {
        return injector.getInstance(EngineConfigurationService.class);
    }

    public final ProtocolPluggableService getProtocolPluggableService() {
        return injector.getInstance(ProtocolPluggableService.class);
    }

}