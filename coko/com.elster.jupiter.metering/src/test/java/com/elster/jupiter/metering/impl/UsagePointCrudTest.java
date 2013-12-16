package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.cache.impl.OrmCacheModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.joda.time.DateMidnight;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointCrudTest {

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
        			new IdsModule(), 
        			new MeteringModule(), 
        			new PartyModule(), 
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
				injector.getInstance(MeteringService.class);
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
                Date date = new DateMidnight(2001, 1, 1).toDate();
                doTest(getMeteringService(), date);

            }
        });

    }

    private MeteringService getMeteringService() {
        return injector.getInstance(MeteringService.class);
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    private void doTest(MeteringService meteringService, Date date) {
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("mrID");
        usagePoint.save();
        long id = usagePoint.getId();
        assertThat(Bus.getOrmClient().getUsagePointFactory().find()).hasSize(1);
        usagePoint.setAmiBillingReady(AmiBillingReadyKind.AMIDISABLED);
        usagePoint.save();
        assertThat(Bus.getOrmClient().getUsagePointFactory().find("amiBillingReady",AmiBillingReadyKind.AMIDISABLED)).hasSize(1);
        assertThat(usagePoint.getVersion()).isEqualTo(1);
        usagePoint.delete();
        assertThat(Bus.getOrmClient().getUsagePointFactory().find()).hasSize(0);
        assertThat(Bus.getOrmClient().getUsagePointFactory().getJournal(id)).hasSize(2);
     }

}
