package com.elster.jupiter.parties.impl;

import static org.mockito.Mockito.when;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

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
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Condition;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PartyCrudTest {

    private Injector injector;

    @Mock
    private LogService logService;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private Principal principal;
    
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {       
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);           
        }
    }

    @Before
    public void setUp() throws SQLException {
        injector = Guice.createInjector(
        			new MockModule(), 
        			inMemoryBootstrapModule,  
        			new PartyModule(), 
        			new EventsModule(),
        			new InMemoryMessagingModule(),
        			new DomainUtilModule(), 
        			new OrmModule(),
        			new UtilModule(), 
        			new ThreadSecurityModule(principal), 
        			new PubSubModule(logService), 
        			new TransactionModule(),
        			new OrmCacheModule());
        when(principal.getName()).thenReturn("Test");
        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
			@Override
			public Void perform() {
				injector.getInstance(PartyService.class);
				return null;
			}
		});
    }

    @After
    public void tearDown() throws SQLException {
       inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void test() throws SQLException {

        getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                doTest(getPartyService());

            }
        });

    }

    private PartyService getPartyService() {
        return injector.getInstance(PartyService.class);
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    private void doTest(PartyService partyService) {
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
    }

}
