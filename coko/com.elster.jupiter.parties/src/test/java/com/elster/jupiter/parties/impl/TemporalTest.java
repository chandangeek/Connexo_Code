/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
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
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


public class TemporalTest {

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    private static final boolean printSql = false;

    @BeforeClass
    public static void setUp() {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
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
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext() ) {
            injector.getInstance(PartyService.class);
            ctx.commit();
        }
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    private PartyService getPartyService() {
        return injector.getInstance(PartyService.class);
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }
    
    @Test
    public void testTemporal()  {
        try (TransactionContext context = getTransactionService().getContext()) {
        	PartyService partyService = getPartyService();
         	Organization organization = partyService.newOrganization("Melrose")
                    .setAliasName("Melrose Place")
                    .setDescription("Buy and Improve")
                    .create();
        	PartyRole role = partyService.createRole("XXX", "YYY", "ZZZ", "AAA", "BBB");
        	organization.assumeRole(role, Instant.now());
        	context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext())  {
        	Query<Party> query = getPartyService().getPartyQuery();
        	Condition condition = Where.where("partyInRoles.interval").isEffective();
        	List<Party> parties = query.select(condition);
        	assertThat(parties.get(0).getPartyInRoles(query.getEffectiveDate())).isNotEmpty();
        	context.commit();
        	assertThat(context.getStats().getSqlCount()).isEqualTo(1);
        }
        try (TransactionContext context = getTransactionService().getContext()) {
        	Query<Party> query = getPartyService().getPartyQuery();
        	List<Party> parties = query.select(Condition.TRUE);
        	assertThat(parties.get(0).getPartyInRoles(query.getEffectiveDate())).isNotEmpty();
        	// second time should be cached
        	assertThat(parties.get(0).getPartyInRoles(query.getEffectiveDate())).isNotEmpty();
        	context.commit();
        	assertThat(context.getStats().getSqlCount()).isEqualTo(2);
        }
    }
}
