package com.elster.jupiter.parties.impl;

import java.sql.SQLException;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;

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
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Condition;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import static org.assertj.core.api.Assertions.assertThat;

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
        			new TransactionModule(),
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
        getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                doTestCrud(getPartyService());

            }
        });
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testDuplicateRepresentation() {
    	getTransactionService().execute(new VoidTransaction() {
    		@Override
        	protected void doPerform() {
            	doTestDuplicateRepresentation(getPartyService());
        	}
        });
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testDuplicateRole() {
    	getTransactionService().execute(new VoidTransaction() {
    		@Override
        	protected void doPerform() {
            	doTestDuplicateRole(getPartyService());
        	}
        });
    }

    private void doTestCrud(PartyService partyService) {
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
    }

    private void doTestDuplicateRepresentation(PartyService partyService) {
    	Person person = partyService.newPerson("Frank", "Hyldmar");
    	UserService userService = injector.getInstance(UserService.class);
    	User user = userService.findUser("admin").get();
    	person.appointDelegate(user, new Date(0));
    	person.save();
    	assertThat(person.getCurrentDelegates()).hasSize(1);
    	person.appointDelegate(user,new Date());
    }
    
    private void doTestDuplicateRole(PartyService partyService) {
    	Organization organization = partyService.newOrganization("Elster");
    	PartyRole role = partyService.createRole("111", "222", "333", "444", "555");
    	organization.assumeRole(role, new Date(0));
    	organization.save();
    	assertThat(organization.getPartyInRoles()).hasSize(1);
    	organization.assumeRole(role, new Date());

    }
}
