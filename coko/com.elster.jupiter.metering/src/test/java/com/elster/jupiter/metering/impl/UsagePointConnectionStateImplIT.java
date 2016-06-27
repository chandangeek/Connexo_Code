package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectionState;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointConnectionStateImplIT {

    private static final Quantity VOLTAGE = Unit.VOLT.amount(BigDecimal.valueOf(220));
    private static final Quantity RATED_CURRENT = Unit.AMPERE.amount(BigDecimal.valueOf(14));
    private static final Quantity RATED_POWER = Unit.WATT.amount(BigDecimal.valueOf(156156));
    private static final Quantity RATED_POWER2 = Unit.WATT.amount(BigDecimal.valueOf(156157));
    private static final Quantity LOAD = Unit.VOLT_AMPERE.amount(BigDecimal.valueOf(12345));

    private static final Instant JANUARY_2014 = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant FEBRUARY_2014 = ZonedDateTime.of(2014, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant JANUARY_2013 = ZonedDateTime.of(2013, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant MARCH_2014 = ZonedDateTime.of(2014, 3, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();

    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;


    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(SearchService.class).toInstance(mock(SearchService.class));
            bind(LicenseService.class).toInstance(mock(LicenseService.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @Before
    public void setUp() throws SQLException {
        try {
            try {
                injector = Guice.createInjector(
                        new MockModule(),
                        inMemoryBootstrapModule,
                        new InMemoryMessagingModule(),
                        new IdsModule(),
                        new MeteringModule(),
                        new BasicPropertiesModule(),
                        new TimeModule(),
                        new PartyModule(),
                        new EventsModule(),
                        new DomainUtilModule(),
                        new OrmModule(),
                        new UtilModule(),
                        new ThreadSecurityModule(),
                        new PubSubModule(),
                        new TransactionModule(false),
                        new BpmModule(),
                        new FiniteStateMachineModule(),
                        new NlsModule(),
                        new CustomPropertySetsModule()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(CustomPropertySetService.class);
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
    public void testConnectionState() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);

        try (TransactionContext context = transactionService.getContext()) {
            ServerMeteringService meteringService = injector.getInstance(ServerMeteringService.class);
            DataModel dataModel = meteringService.getDataModel();
            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            UsagePoint usagePoint = serviceCategory.newUsagePoint("mrID", Instant.EPOCH).create();
            assertThat(dataModel.mapper(UsagePoint.class).find()).hasSize(1);

            //get connection state
            ConnectionState connectionState =  usagePoint.getConnectionState();
            assertThat(connectionState.equals(ConnectionState.UNDER_CONSTRUCTION)).isTrue();

            //add connection state valid from 1 january 2014
            usagePoint.setConnectionState(ConnectionState.CONNECTED, JANUARY_2014);

            //get connection state
            connectionState =  usagePoint.getConnectionState();
            assertThat(connectionState.equals(ConnectionState.CONNECTED)).isTrue();

            //add connection state valid from 1 february 2014 (this closes the previous detail on this date)
            usagePoint.setConnectionState(ConnectionState.LOGICALLY_DISCONNECTED, FEBRUARY_2014);

            context.commit();

            //get connection state
            connectionState =  usagePoint.getConnectionState();
            assertThat(connectionState.equals(ConnectionState.LOGICALLY_DISCONNECTED)).isTrue();
        }
    }
}

