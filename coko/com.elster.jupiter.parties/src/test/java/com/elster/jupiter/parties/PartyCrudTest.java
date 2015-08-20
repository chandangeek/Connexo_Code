package com.elster.jupiter.parties;


import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import javax.validation.ConstraintViolationException;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class PartyCrudTest {

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
        }
    }

    private static final boolean printSql = false;

    @BeforeClass
    public static void setUp() {
        injector = Guice.createInjector(
        			new MockModule(),
        			inMemoryBootstrapModule,
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
    public void testCrud()  {
        try (TransactionContext context = getTransactionService().getContext()) {
        	PartyService partyService = getPartyService();
         	Organization organization = partyService.newOrganization("EICT");
        	organization.save();
        	organization.setName("Elster");
        	organization.setAliasName("EnergyICT");
        	organization.setDescription("Delivering tomorrow's energy solutions today");
        	StreetAddress address = new StreetAddress();
        	address.getStreetDetail().setBuildingName("KKS");
        	address.getStreetDetail().setName("Stasegemsesteenweg");
        	address.getStreetDetail().setNumber("114");
        	organization.setStreetAddress(address);
        	organization.save();
        	Query<Party> query = partyService.getPartyQuery();
        	query.setLazy();
        	assertThat(query.select(Condition.TRUE)).hasSize(1);
        	query.setEager();
        	assertThat(query.select(Condition.TRUE)).hasSize(1);
        	partyService.createRole("XXX", "YYY", "ZZZ", "AAA", "BBB");
        	PartyRole role = partyService.getPartyRoles().get(0);
        	organization.assumeRole(partyService.getPartyRoles().get(0), Instant.now());
        	assertThat(organization.getPartyInRoles(Instant.now()).get(0).getRole()).isEqualTo(role);
        	Party party = query.select(Condition.TRUE).get(0);
        	assertThat(party.getPartyInRoles(Instant.now()).get(0).getRole()).isEqualTo(role);
        	query.setLazy();
        	party = query.select(Condition.TRUE).get(0);
        	assertThat(party.getPartyInRoles(Instant.now()).get(0).getRole()).isEqualTo(role);
        	query = partyService.getPartyQuery();
        	Condition condition = Where.where("partyInRoles.interval").isEffective();
        	assertThat(query.select(condition)).isNotEmpty();
        	query.setEffectiveDate(Instant.EPOCH);
        	assertThat(query.select(condition)).isEmpty();
        	UserService userService = injector.getInstance(UserService.class);
        	User user = userService.findUser("admin").get();
        	party.appointDelegate(user, Instant.EPOCH);
        	party.save();
        	party = query.select(Condition.TRUE).get(0);
        	assertThat(party.getCurrentDelegates().get(0).getDelegate()).isEqualTo(user);
        	assertThat(role.getParties()).isNotEmpty();
        	assertThat(query.select(Where.where("upperName").isEqualTo("ELSTER"))).hasSize(1);
        	context.commit();
        }
        try (TransactionContext context = getTransactionService().getContext()) {
        	Optional<Party> party = getPartyService().getParty(1);
        	party.get().getCurrentDelegates().size();
        	for (PartyInRole each : party.get().getPartyInRoles(Instant.now())) {
        		each.getRole().getMRID();
        	}
        	context.commit();
        	assertThat(context.getStats().getSqlCount()).isEqualTo(2);
        }
        try (TransactionContext context = getTransactionService().getContext()) {
        	for (Party party : getPartyService().getPartyQuery().select(Condition.TRUE)) {
        		party.delete();
        	}
        	context.commit();
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testDuplicateRepresentation() {
    	try (TransactionContext context = getTransactionService().getContext()) {
    		Person person = getPartyService().newPerson("Frank", "Hyldmar");
        	UserService userService = injector.getInstance(UserService.class);
        	User user = userService.findUser("admin").get();
        	person.appointDelegate(user, Instant.EPOCH);
        	person.save();
        	assertThat(person.getCurrentDelegates()).hasSize(1);
        	person.appointDelegate(user, Instant.now());
    		context.commit();
        }
    	try (TransactionContext context = getTransactionService().getContext()) {
    		for (Party party : getPartyService().getPartyQuery().select(Condition.TRUE)) {
        		party.delete();
        	}
        	context.commit();
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testDuplicateRole() {
    	try (TransactionContext context = getTransactionService().getContext()) {
    		Organization organization = getPartyService().newOrganization("Elster");
    		PartyRole role = getPartyService().createRole("111", "222", "333", "444", "555");
    		organization.assumeRole(role, Instant.EPOCH);
    		organization.save();
    		assertThat(organization.getPartyInRoles(Instant.now())).hasSize(1);
    		organization.assumeRole(role, Instant.now());
    		context.commit();
    	}
    }

    @Test
    public void testPartyRoleCache() {
    	try (TransactionContext context = getTransactionService().getContext()) {
    		for (int i = 10 ; i < 20 ; i++) {
    			String name = "M" + i;
    			getPartyService().createRole("XAZ", name , name , name , name);
    			getPartyService().getRole(name); // load in cache
    		}
    		context.commit();
    	}
    	try (TransactionContext context = getTransactionService().getContext()) {
    		assertThat(getPartyService().getPartyRoles().size()).isGreaterThanOrEqualTo(10); // will do sql
    		assertThat(getPartyService().getRole("M15").isPresent()).isTrue(); // should not do sql
    		assertThat(getPartyService().getRole("M1599").isPresent()).isFalse(); // will do sql
    		context.commit();
    		assertThat(context.getStats().getSqlCount()).isEqualTo(2);
    	}

    }

    @Test(expected=ConstraintViolationException.class)
    public void testValidation() {
    	try (TransactionContext context = getTransactionService().getContext()) {
           	PartyService partyService = getPartyService();
           	Organization organization = partyService.newOrganization("Elster");
           	organization.setStreetAddress(new StreetAddress());
           	organization.save();
           	context.commit();
    	}
    }

    @Test(expected=ConstraintViolationException.class)
    public void testDuplicate() {
    	try (TransactionContext context = getTransactionService().getContext()) {
           	PartyService partyService = getPartyService();
           	Organization organization = partyService.newOrganization("Elster");
           	organization.save();
           	organization = partyService.newOrganization("Elster");
           	organization.save();
           	context.commit();
    	}
    }
}
