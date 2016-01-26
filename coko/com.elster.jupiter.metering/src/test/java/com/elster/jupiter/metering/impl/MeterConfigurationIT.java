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
import com.elster.jupiter.metering.MeterConfiguration;
import com.elster.jupiter.metering.MeterReadingTypeConfiguration;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingType;
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
import com.google.common.collect.Range;
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
import java.time.Instant;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MeterConfigurationIT {

    public static final String MULTIPLIER_TYPE_NAME = "Pulse";
    public static final BigDecimal VALUE = BigDecimal.valueOf(2, 0);
    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    private Injector injector;

    private static final ZonedDateTime ACTIVE_DATE = ZonedDateTime.of(2014, 4, 9, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime END_DATE = ZonedDateTime.of(2014, 8, 9, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());

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
    private ReadingType secondaryMetered;
    private ReadingType primaryMetered;


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
                    new MeteringModule(
                            "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0",  // no macro period, measuring period =  15 min, secondary metered
                            "0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.0.72.0"  // no macro period, measuring period =  15 min, primary metered
                    ),
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
        secondaryMetered = meteringService.getReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0").get();
        primaryMetered = meteringService.getReadingType("0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.0.72.0").get();
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testCreateConfiguration() {
        createAndActivateMeter();
        createMultiplierType();

        MeterConfiguration meterConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            meterConfiguration = meter
                    .startingConfigurationOn(ACTIVE_DATE.toInstant())
                    .endingAt(END_DATE.toInstant())
                    .configureReadingType(secondaryMetered)
                    .withOverflowValue(BigDecimal.valueOf(15))
                    .withNumberOfFractionDigits(3)
                    .withMultiplierOfType(multiplierType)
                    .calculating(primaryMetered)
                    .create();
            context.commit();
        }

        assertThat(meter.getConfiguration(ACTIVE_DATE.toInstant())).contains(meterConfiguration);

        meter = meteringService.findMeter(meter.getId()).get();
        meterConfiguration = meter.getConfiguration(ACTIVE_DATE.toInstant()).get();

        assertThat(meterConfiguration.getRange()).isEqualTo(Range.closedOpen(ACTIVE_DATE.toInstant(), END_DATE.toInstant()));
        assertThat(meterConfiguration.getReadingTypeConfigs()).hasSize(1);

        MeterReadingTypeConfiguration meterReadingTypeConfiguration = meterConfiguration.getReadingTypeConfigs().get(0);

        assertThat(meterReadingTypeConfiguration.getMeasured()).isEqualTo(secondaryMetered);
        assertThat(meterReadingTypeConfiguration.getCalculated()).contains(primaryMetered);
        assertThat(meterReadingTypeConfiguration.getMultiplierType()).isEqualTo(multiplierType);
        assertThat(meterReadingTypeConfiguration.getOverflowValue()).hasValue(BigDecimal.valueOf(15));
        assertThat(meterReadingTypeConfiguration.getNumberOfFractionDigits()).hasValue(3);
    }

    @Test
    public void testEndConfiguration() {
        createAndActivateMeter();
        createMultiplierType();

        MeterConfiguration meterConfiguration;
        try (TransactionContext context = transactionService.getContext()) {
            meterConfiguration = meter
                    .startingConfigurationOn(ACTIVE_DATE.toInstant())
                    .configureReadingType(secondaryMetered)
                    .withOverflowValue(BigDecimal.valueOf(15))
                    .withNumberOfFractionDigits(3)
                    .withMultiplierOfType(multiplierType)
                    .calculating(primaryMetered)
                    .create();
            context.commit();
        }

        assertThat(meter.getConfiguration(ACTIVE_DATE.toInstant())).contains(meterConfiguration);

        meter = meteringService.findMeter(meter.getId()).get();
        meterConfiguration = meter.getConfiguration(ACTIVE_DATE.toInstant()).get();

        try (TransactionContext context = transactionService.getContext()) {
            meterConfiguration.endAt(END_DATE.toInstant());
            context.commit();
        }

        meter = meteringService.findMeter(meter.getId()).get();
        meterConfiguration = meter.getConfiguration(ACTIVE_DATE.toInstant()).get();

        Range<Instant> range = meterConfiguration.getRange();
        assertThat(range.hasUpperBound());
        assertThat(range.upperEndpoint()).isEqualTo(END_DATE.toInstant());
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