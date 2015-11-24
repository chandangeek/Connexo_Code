package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
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
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link MeterImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-16 (11:03)
 */
@RunWith(MockitoJUnitRunner.class)
public class ChannelImplIT {

    private static final ZonedDateTime ACTIVATION = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    public static final String BULK = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String DELTA = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    private Injector injector;

    private AtomicLong offsets = new AtomicLong(5);

    private Clock clock = new ProgrammableClock(TimeZoneNeutral.getMcMurdo(), () -> ACTIVATION.plusDays(offsets.getAndIncrement()).toInstant());

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
                    new MeteringModule(BULK, DELTA),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(clock),
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
        injector.getInstance(TransactionService.class).execute(() -> {
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
    public void createEndDeviceWithManagedState() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        ReadingType bulkReadingType = meteringService.getReadingType(BULK).get();
        ReadingType deltaReadingType = meteringService.getReadingType(DELTA).get();
        Meter meter;
        try (TransactionContext context = transactionService.getContext()) {

            meter = meteringService.findAmrSystem(1).get()
                    .newMeter("amrID")
                    .setMRID("mRID")
                    .create();

            MeterActivation meterActivation = meter.activate(ACTIVATION.toInstant());


            Channel channel = meterActivation.createChannel(bulkReadingType);

//            assertThat(channel.getReadingTypes()).contains(deltaReadingType);

            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(BULK);

            IntervalReading reading1 = IntervalReadingImpl.of(ACTIVATION.plusDays(3).toInstant(), BigDecimal.valueOf(5));
            IntervalReading reading2 = IntervalReadingImpl.of(ACTIVATION.plusDays(3).plusMinutes(15).toInstant(), BigDecimal.valueOf(7));

            intervalBlock.addAllIntervalReadings(Arrays.asList(reading1, reading2));
            meterReading.addIntervalBlock(intervalBlock);
            meter.store(meterReading);

            context.commit();
        }

        Instant since = clock.instant();

        try (TransactionContext context = transactionService.getContext()) {
            MeterActivation meterActivation = meter.getMeterActivations().get(0);
            Channel channel = meterActivation.getChannels().get(0);
            channel.getCimChannel(deltaReadingType).get().editReadings(Collections.singletonList(IntervalReadingImpl.of(ACTIVATION.plusDays(3).toInstant(), BigDecimal.valueOf(5))));

            context.commit();
        }

        Channel channel = meter.getMeterActivations().get(0).getChannels().get(0);

        List<BaseReadingRecord> bulkReadingsUpdatedSince = channel.getReadingsUpdatedSince(bulkReadingType, Range.<Instant>all(), since);
        assertThat(bulkReadingsUpdatedSince).isEmpty();
        List<BaseReadingRecord> deltaReadingsUpdatedSince = channel.getReadingsUpdatedSince(deltaReadingType, Range.<Instant>all(), since);
        assertThat(deltaReadingsUpdatedSince).hasSize(1);

    }


}