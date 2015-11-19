package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.ZonedDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MeterMultiplierIT {

    public static final String MULTIPLIER_TYPE_NAME = "Pulse";
    public static final BigDecimal VALUE = BigDecimal.valueOf(2, 0);
    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    private Injector injector;

    private static final ZonedDateTime ACTIVE_DATE = ZonedDateTime.of(2014, 4, 9, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;


    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private Meter meter;
    private MeterActivation meterActivation;
    private MultiplierType multiplierType;
    private MeteringService meteringService;
    private TransactionService transactionService;


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
        }
    }

    @Before
    public void setUp() throws SQLException {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new MeteringModule(),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new BpmModule(),
                    new FiniteStateMachineModule(),
                    new NlsModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            injector.getInstance(FiniteStateMachineService.class);
            meteringService = injector.getInstance(MeteringService.class);
            return null;
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testCreateMultiplierType() {
        createAndActivateMeter();

        // business method
        createMultiplierType();

        assertThat(meteringService.getMultiplierType(MULTIPLIER_TYPE_NAME)).contains(multiplierType);

    }

    @Test
    public void testSetMultiplier() {
        createAndActivateMeter();
        createMultiplierType();

        try (TransactionContext context = transactionService.getContext()) {
            meterActivation.setMultiplier(multiplierType, VALUE);
            context.commit();
        }

        assertThat(meterActivation.getMultiplier(multiplierType)).contains(VALUE);

    }

    private void createMultiplierType() {
        try (TransactionContext context = transactionService.getContext()) {
            multiplierType = meteringService.createMultiplierType(MULTIPLIER_TYPE_NAME);
            context.commit();
        }
    }

    private void createAndActivateMeter() {
        try (TransactionContext context = transactionService.getContext()) {
             meter = meteringService.findAmrSystem(1).get()
                    .newMeter("amrID")
                    .setMRID("mRID")
                    .create();
            meterActivation = meter.activate(ACTIVE_DATE.toInstant());
            context.commit();
        }
        meter = meteringService.findMeter(meter.getId()).get();
        meterActivation = meter.getMeterActivations().get(0);
    }


}