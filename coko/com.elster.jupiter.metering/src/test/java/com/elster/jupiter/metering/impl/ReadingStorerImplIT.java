package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.units.Quantity;
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
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingStorerImplIT {

    private static final ZonedDateTime ACTIVATION = ZonedDateTime.of(1975, 9, 19, 21, 46, 55, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime BASE = ZonedDateTime.of(2025, 12, 20, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private Subscriber topicHandler;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
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
                    new MeteringModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
                    new PartyModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(Clock.fixed(BASE.plusMonths(1).toInstant(), TimeZoneNeutral.getMcMurdo())),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new BpmModule(),
                    new FiniteStateMachineModule(),
                    new NlsModule());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            meteringService = injector.getInstance(MeteringService.class);
            return null;
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testWriteBulkData() {
        AmrSystem mdc = meteringService.findAmrSystem(1L).get();

        ReadingType deltaReadingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        ReadingType bulkReadingType = meteringService.getReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();


        Channel channel = transactionService.execute(() -> {
            Meter meter = mdc.newMeter("AMR_ID")
                    .setMRID("meterMRID")
                    .setName("Meter")
                    .create();
            MeterActivation meterActivation = meter.activate(ACTIVATION.toInstant());
            return meterActivation.createChannel(bulkReadingType);
        });

        transactionService.run(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());

            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.toInstant(), BigDecimal.valueOf(10000, 2), ProfileStatus.of(ProfileStatus.Flag.BATTERY_LOW)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 1).toInstant(), BigDecimal.valueOf(11000, 2), ProfileStatus.of(ProfileStatus.Flag.BATTERY_LOW)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 2).toInstant(), BigDecimal.valueOf(12000, 2), ProfileStatus.of(ProfileStatus.Flag.BATTERY_LOW)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 3).toInstant(), BigDecimal.valueOf(13000, 2), ProfileStatus.of(ProfileStatus.Flag.BATTERY_LOW)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 4).toInstant(), BigDecimal.valueOf(14000, 2), ProfileStatus.of(ProfileStatus.Flag.BATTERY_LOW)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 5).toInstant(), BigDecimal.valueOf(15000, 2), ProfileStatus.of(ProfileStatus.Flag.BATTERY_LOW)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 6).toInstant(), BigDecimal.valueOf(16000, 2), ProfileStatus.of(ProfileStatus.Flag.BATTERY_LOW)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 7).toInstant(), BigDecimal.valueOf(17000, 2), ProfileStatus.of(ProfileStatus.Flag.BATTERY_LOW)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 8).toInstant(), BigDecimal.valueOf(18000, 2), ProfileStatus.of(ProfileStatus.Flag.BATTERY_LOW)));

            meterReading.addIntervalBlock(intervalBlock);

            channel.getMeterActivation().getMeter().get().store(meterReading);

        });

        List<BaseReadingRecord> readings = channel.getReadings(Range.atLeast(BASE.toInstant()));

        assertThat(readings).hasSize(9);

        assertThat(readings.get(0).getQuantity(deltaReadingType)).isNull();
        assertThat(readings.get(0).getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10000, 2), 3, "Wh"));
        assertThat(readings.get(0).getTimeStamp()).isEqualTo(BASE.toInstant());

        for (int i = 1; i < 8; i++) {
            BaseReadingRecord baseReadingRecord = readings.get(i);
            assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(1000, 2), 3, "Wh"));
            assertThat(baseReadingRecord.getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10000 + i * 1000, 2), 3, "Wh"));
            assertThat(baseReadingRecord.getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * i).toInstant());
        }

        transactionService.run(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());

            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 9).toInstant(), BigDecimal.valueOf(19000, 2), ProfileStatus.of(ProfileStatus.Flag.BATTERY_LOW)));

            meterReading.addIntervalBlock(intervalBlock);

            channel.getMeterActivation().getMeter().get().store(meterReading);

        });

        readings = channel.getReadings(Range.atLeast(BASE.plusMinutes(15 * 9).toInstant()));

        assertThat(readings).hasSize(1);

        assertThat(readings.get(0).getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(1000, 2), 3, "Wh"));
        assertThat(readings.get(0).getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(19000, 2), 3, "Wh"));
        assertThat(readings.get(0).getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * 9).toInstant());
    }

}
