/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
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

import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class OrmCacheTest {

    private static Injector bootInjector;
    private Injector injector;
    private static InMemoryBootstrapModule bootMemoryBootstrapModule = new InMemoryBootstrapModule();
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    
    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {       
           bind(BundleContext.class).toInstance(mock(BundleContext.class));  
           bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }
    
    private static final boolean printSql = false;
    
    private static Injector getInjector(InMemoryBootstrapModule boot) {
    	return Guice.createInjector(
                new MockModule(),
                boot,
                new DataVaultModule(),
                new PartyModule(),
                new UserModule(),
                new EventsModule(),
                new InMemoryMessagingModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(printSql),
                new NlsModule()
        );
    }
    
    @BeforeClass
    public static void setUp() throws SQLException {
        // we use a different injector to do the setup, so the injector for the test does not have cached values
        bootInjector = getInjector(bootMemoryBootstrapModule);
        try (TransactionContext ctx = bootInjector.getInstance(TransactionService.class).getContext() ) {
        	bootInjector.getInstance(PartyService.class);
        	ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() throws SQLException {
    	bootMemoryBootstrapModule.deactivate();
    }

    @Before
    public void instanceSetup() throws SQLException {
    	injector = getInjector(inMemoryBootstrapModule);
    }
    
    @After
    public void instanceTearDown() throws SQLException {
    	inMemoryBootstrapModule.deactivate();
    }
    

    @Test
    public void testEventTypeCache() {
		EventService eventService = injector.getInstance(EventService.class);
    	try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
    		com.elster.jupiter.events.EventType eventType = eventService.getEventType(EventType.PARTY_CREATED.topic()).get();
    		assertThat(eventType.getPropertyTypes()).isNotEmpty();
    		ctx.commit();
    		assertThat(ctx.getStats().getSqlCount()).isEqualTo(1);
    	}
    	try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
    		com.elster.jupiter.events.EventType eventType = eventService.getEventType(EventType.PARTY_CREATED.topic()).get();
    		assertThat(eventType.getPropertyTypes()).isNotEmpty();
    		ctx.commit();
    		assertThat(ctx.getStats().getSqlCount()).isEqualTo(0);
    	}
	}
}
