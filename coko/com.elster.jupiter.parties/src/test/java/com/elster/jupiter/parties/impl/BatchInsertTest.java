/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.Person;
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
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


public class BatchInsertTest {

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
    public void testBatchInsert()  {
    	PartyServiceImpl partyService = (PartyServiceImpl) getPartyService();
    	DataModel dataModel = partyService.getDataModel();
        try (TransactionContext context = getTransactionService().getContext()) {
        	List<Party> parties = new ArrayList<>();
         	Organization organization = partyService.newOrganization("Melrose")
					.setAliasName("Melrose Place")
					.setDescription("Buy and Improve")
					.create();
        	parties.add(organization);
        	Person person = partyService.newPerson("Simon","Peckham").create();
        	parties.add(person);
        	assertThat(organization.getId()).isEqualTo(1);
        	assertThat(person.getId()).isEqualTo(2);
        	assertThat(organization.getVersion()).isEqualTo(1);
        	assertThat(person.getVersion()).isEqualTo(1);
        	dataModel.mapper(Party.class).update(parties);
        	assertThat(organization.getId()).isEqualTo(1);
        	assertThat(person.getId()).isEqualTo(2);
        	assertThat(organization.getVersion()).isEqualTo(2);
        	assertThat(person.getVersion()).isEqualTo(2);
        	organization.setDescription("xyz");
        	person.setDescription("abc");
        	dataModel.mapper(Party.class).update(parties);
        	assertThat(organization.getVersion()).isEqualTo(3);
        	assertThat(person.getVersion()).isEqualTo(3);
        	context.commit();
        }
    }
    
}
