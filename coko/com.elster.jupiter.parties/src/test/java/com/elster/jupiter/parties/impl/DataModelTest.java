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
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


public class DataModelTest {

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
    public void testInheritance()  {
    	PartyServiceImpl partyService = (PartyServiceImpl) getPartyService();
    	DataModel dataModel = partyService.getDataModel();
        try (TransactionContext context = getTransactionService().getContext()) {
         	Organization organization = partyService.newOrganization("Melrose")
					.setAliasName("Melrose Place")
                    .setDescription("Buy and Improve")
                    .create();
        	assertThat(dataModel.mapper(Party.class).find()).hasSize(1);
        	assertThat(dataModel.mapper(Organization.class).find()).hasSize(1);
        	assertThat(dataModel.mapper(Person.class).find()).hasSize(0);
        	long id = organization.getId();
        	assertThat(dataModel.mapper(Party.class).getOptional(id).isPresent()).isTrue();
        	assertThat(dataModel.mapper(Organization.class).getOptional(id).isPresent()).isTrue();
        	assertThat(dataModel.mapper(Person.class).getOptional(id).isPresent()).isFalse();
        	assertThat(dataModel.mapper(Party.class).getEager(id).isPresent()).isTrue();
        	assertThat(dataModel.mapper(Organization.class).getEager(id).isPresent()).isTrue();
        	assertThat(dataModel.mapper(Person.class).getEager(id).isPresent()).isFalse();
        	partyService.createRole("XXX", "YYY", "ZZZ", "AAA", "BBB");
        	PartyRole role = partyService.getPartyRoles().get(0);
        	organization.assumeRole(role, Instant.now());
        	Condition condition = Where.where("party.description").isEqualTo(organization.getDescription());
        	assertThat(dataModel.stream(PartyInRole.class).join(Party.class).filter(condition).sorted(Order.ascending("party")).select()).hasSize(1);
        	assertThat(dataModel.query(PartyInRole.class, Party.class).select(condition,Order.ascending("party"))).hasSize(1);
        	assertThat(dataModel.mapper(Party.class).find("description", organization.getDescription(),Order.descending("description"))).hasSize(1);
        	User user = injector.getInstance(UserService.class).findUser("admin").get();
        	Instant start = Instant.now();
        	PartyRepresentation representation = organization.appointDelegate(user, start);
        	representation = dataModel.mapper(PartyRepresentation.class).getOptional(user.getName(),organization.getId(),start.toEpochMilli()).get();
        	dataModel.touch(representation);
        	context.commit();
        }
        Party party = dataModel.mapper(Party.class).find().get(0);
        PartyRole role = partyService.getRole("YYY").get();
        try (TransactionContext context = getTransactionService().getContext()) {
        	Condition condition = Where.where("party").isEqualTo(party).and(Where.where("role").isEqualTo(role));
        	List<PartyInRole> representations = dataModel.query(PartyInRole.class).select(condition,Order.ascending("version"));
        	assertThat(representations).isNotEmpty();
        	for (PartyInRole each : representations) {
        		each.getParty().getAliasName();
        	}
        	context.commit();
        	assertThat(context.getStats().getSqlCount()).isEqualTo(1);
        }
        DataMapper<Party> mapper = dataModel.mapper(Party.class);
        SqlBuilder builder = mapper.builder("P", "FIRST_ROWS(1)");
        int count = 0;
        try (Fetcher<Party> fetcher = mapper.fetcher(builder)) {
        	for (Party each : fetcher) {
        		assertThat(each).isNotNull();
        		count++;
        	}
        	assertThat(count).isNotZero();
        }
        assertThat(dataModel.stream(Party.class).count()).isNotZero();
    }
    
    @Test
    public void testWriteBack()  {
        try (TransactionContext context = getTransactionService().getContext()) {
        	PartyServiceImpl partyService = (PartyServiceImpl) getPartyService();
         	PersonImpl person = (PersonImpl) partyService.newPerson("Frank","Hyldmar").create();
            Instant now = Instant.now();
            assertThat(person.getCreateTime().isAfter(now)).isFalse();
         	assertThat(person.getModTime().isAfter(now)).isFalse();
         	assertThat(person.getUserName()).isNotNull();
        	assertThat(person.getVersion()).isEqualTo(1);
         	person.setAliasName("xxxx");
         	person.update();
         	assertThat(person.getCreateTime().isAfter(now)).isFalse();
         	assertThat(person.getModTime().isBefore(now)).isFalse();
         	assertThat(person.getVersion()).isEqualTo(2);
        }
    }
    
    @Test(expected=IllegalStateException.class)
    public void testQueryRestriction() {
		PartyServiceImpl partyService = (PartyServiceImpl) getPartyService();
		DataModel dataModel = partyService.getDataModel();
		dataModel.query(PartyInRole.class, Person.class);
    }
    
    @Test
    public void testQueryNoRestriction() {
		PartyServiceImpl partyService = (PartyServiceImpl) getPartyService();
		DataModel dataModel = partyService.getDataModel();
		dataModel.query(PartyInRole.class, Party.class);
		Optional<PartyInRole> optional = dataModel.stream(PartyInRole.class).join(PartyImpl.class).sorted(Order.ascending("name").nullsFirst()).findFirst();
		assertThat(optional).isNotNull();
    }
}
