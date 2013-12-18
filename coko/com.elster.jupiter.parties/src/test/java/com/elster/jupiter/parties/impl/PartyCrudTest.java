package com.elster.jupiter.parties.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.sql.SQLException;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.orm.cache.impl.OrmCacheModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Condition;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

@RunWith(MockitoJUnitRunner.class)
public class PartyCrudTest {

    private static Injector injector;
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {       
           bind(BundleContext.class).toInstance(mock(BundleContext.class));          
        }
    }
    
    private static final boolean printSql = false;

    @BeforeClass
    public static void setUp() throws SQLException {
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
        			new OrmCacheModule());
        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
			@Override
			public Void perform() {
				injector.getInstance(PartyService.class);
				return null;
			}
		});
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
        	Query<Party> query = partyService.getPartyQuery();
        	query.setLazy();
        	assertThat(query.select(Condition.TRUE)).hasSize(1);
        	query.setEager();
        	assertThat(query.select(Condition.TRUE)).hasSize(1);
        	partyService.createRole("XXX", "YYY", "ZZZ", "AAA", "BBB");
        	PartyRole role = partyService.getPartyRoles().get(0);
        	organization.assumeRole(partyService.getPartyRoles().get(0),new Date());
        	assertThat(organization.getPartyInRoles().get(0).getRole()).isEqualTo(role);
        	Party party = query.select(Condition.TRUE).get(0);
        	assertThat(party.getPartyInRoles().get(0).getRole()).isEqualTo(role);
        	query.setLazy();
        	party = query.select(Condition.TRUE).get(0);
        	assertThat(party.getPartyInRoles().get(0).getRole()).isEqualTo(role);
        	UserService userService = injector.getInstance(UserService.class);
        	User user = userService.findUser("admin").get();
        	party.appointDelegate(user, new Date(0));
        	party.save();
        	party = query.select(Condition.TRUE).get(0);
        	assertThat(party.getCurrentDelegates().get(0).getDelegate()).isEqualTo(user);
        	context.commit();
        	assertThat(context.getStats().getSqlCount()).isLessThan(25);
        }
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testDuplicateRepresentation() {
    	try (TransactionContext context = getTransactionService().getContext()) {
    		Person person = getPartyService().newPerson("Frank", "Hyldmar");
        	UserService userService = injector.getInstance(UserService.class);
        	User user = userService.findUser("admin").get();
        	person.appointDelegate(user, new Date(0));
        	person.save();
        	assertThat(person.getCurrentDelegates()).hasSize(1);
        	person.appointDelegate(user,new Date());
    		context.commit();
        }
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testDuplicateRole() {
    	try (TransactionContext context = getTransactionService().getContext()) {
    		Organization organization = getPartyService().newOrganization("Elster");
    		PartyRole role = getPartyService().createRole("111", "222", "333", "444", "555");
    		organization.assumeRole(role, new Date(0));
    		organization.save();
    		assertThat(organization.getPartyInRoles()).hasSize(1);
    		organization.assumeRole(role, new Date());
    		context.commit();
    	}
    }
    
    @Test
    public void testPartyRoleCache() {
    	try (TransactionContext context = getTransactionService().getContext()) {
    		for (int i = 10 ; i < 20 ; i++) {
    			String name = "M" + i;
    			getPartyService().createRole("XAZ", name , name , name , name);
    		}
    		context.commit();
    	}
    	try (TransactionContext context = getTransactionService().getContext()) {
    		assertThat(getPartyService().getPartyRoles().size()).isGreaterThanOrEqualTo(10);
    		assertThat(getPartyService().getRole("M15")).isPresent();
    		assertThat(getPartyService().getRole("M1599")).isAbsent();
    		context.commit();
    		assertThat(context.getStats().getSqlCount()).isEqualTo(1);
    	}
    	((PartyServiceImpl) getPartyService()).clearRoleCache();
    	assertThat(getPartyService().getPartyRoles().size()).isGreaterThanOrEqualTo(10);
    }
}
