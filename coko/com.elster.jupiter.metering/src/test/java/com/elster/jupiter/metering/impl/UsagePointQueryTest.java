package com.elster.jupiter.metering.impl;

import static com.elster.jupiter.util.conditions.Where.where;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.security.Principal;
import java.sql.SQLException;
import java.time.Instant;

import com.elster.jupiter.bpm.impl.BpmModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.cbo.StreetAddress;
import com.elster.jupiter.cbo.StreetDetail;
import com.elster.jupiter.cbo.TownDetail;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
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
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.units.Unit;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointQueryTest {

    private Injector injector;

    @Mock
    private LogService logService;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private Principal principal;
    @Mock
    private EventAdmin eventAdmin;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
        }
    }

    @Before
    public void setUp() throws SQLException {
        injector = Guice.createInjector(
        			new MockModule(),
        			inMemoryBootstrapModule,
        			new IdsModule(),
        			new MeteringModule(false),
        			new PartyModule(),
        			new EventsModule(),
        			new InMemoryMessagingModule(),
        			new DomainUtilModule(),
        			new OrmModule(),
        			new UtilModule(),
        			new ThreadSecurityModule(),
        			new PubSubModule(),
        			new UserModule(),
        			new TransactionModule(false),
                    new BpmModule(),
                    new FiniteStateMachineModule(),
                    new NlsModule()
                );
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(EventService.class);
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringService.class);
            return null;
        });
    }

    @After
    public void tearDown() throws SQLException {
       inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void test() throws SQLException {
    	UserService userService = injector.getInstance(UserService.class);
    	final User user = userService.findUser("admin").get();
    	ThreadPrincipalService threadPrincipalService = injector.getInstance(ThreadPrincipalService.class);
    	threadPrincipalService.set(user);
    	getTransactionService().execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                doTest(injector.getInstance(MeteringService.class), user);

            }
        });

    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    private void doTest(MeteringService meteringService, User user) {
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        ServiceLocation location = meteringService.newServiceLocation();
        StreetAddress address = new StreetAddress(new StreetDetail("Stasegemsesteenweg","112"), new TownDetail("8500",  "Kortrijk", "BE"));
        location.setMainAddress(address);
        location.setName("EnergyICT");
        location.save();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("mrID");
        usagePoint.setServiceLocation(location);
        ElectricityDetail detail = (ElectricityDetail) serviceCategory.newUsagePointDetail(usagePoint, Instant.now());
        detail.setAmiBillingReady(AmiBillingReadyKind.AMICAPABLE);
        detail.setRatedPower(Unit.WATT_HOUR.amount(BigDecimal.valueOf(1000),3));
        usagePoint.addDetail(detail);
        usagePoint.save();
        Query<UsagePoint> query = meteringService.getUsagePointQuery();
        Condition condition = where("detail.amiBillingReady").isEqualTo(AmiBillingReadyKind.AMICAPABLE);
        condition = condition.and(where("serviceLocation.mainAddress.townDetail.country").isEqualTo("BE"));
        condition = condition.and(where("detail.ratedPower.value").between(BigDecimal.valueOf(999)).and(BigDecimal.valueOf(1001)));
        assertThat(query.select(condition)).hasSize(1);
        assertThat(query.select(condition).get(0).getServiceCategory().getKind()).isEqualTo(ServiceKind.ELECTRICITY);
        query.setEager();
        assertThat(query.select(condition)).hasSize(1);
        for (int i = 0 ; i < 10 ; i++) {
        	usagePoint = serviceCategory.newUsagePoint("mrID" + i);
        	usagePoint.save();
        }
        assertThat(query.select(Condition.TRUE)).hasSize(11);
        assertThat(query.select(Condition.TRUE,1,5)).hasSize(5);
        assertThat(query.select(meteringService.hasAccountability())).isEmpty();
        PartyService partyService = injector.getInstance(PartyService.class);
        Party party = partyService.newOrganization("Electrabel");
        party.save();
        PartyRole role = partyService.getRole(MarketRoleKind.ENERGYSERVICECONSUMER.name()).get();
        party.assumeRole(role, Instant.now());
        query.setLazy();
        assertThat(query.select(meteringService.hasAccountability())).isEmpty();
        party.appointDelegate(user, Instant.now());
        party = partyService.getParty("Electrabel").get();
        usagePoint.addAccountability(role, party, Instant.now());
        assertThat(query.select(meteringService.hasAccountability())).isNotEmpty();
        assertThat(query.select(Condition.TRUE, Order.descending("mRID").toUpperCase(),Order.ascending("id")).get(0).getMRID()).isEqualTo("mrID9");
        assertThat(usagePoint.getCustomer(Instant.now()).get().getMRID()).isEqualTo("Electrabel");
     }

}
