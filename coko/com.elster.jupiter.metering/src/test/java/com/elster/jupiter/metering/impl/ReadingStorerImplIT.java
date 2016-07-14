package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.impl.CustomPropertySetsModule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProtocolReadingQualities;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.properties.impl.BasicPropertiesModule;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.impl.TimeModule;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ReadingStorerImplIT {

    private static final Set<ReadingQualityType> BATTERY_LOW = new HashSet<>(Arrays.asList(ProtocolReadingQualities.BATTERY_LOW.getReadingQualityType()));

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
    public static final String SECONDARY_DELTA = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String PRIMARY_DELTA = "0.0.2.4.1.2.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String SECONDARY_BULK = "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String PRIMARY_BULK = "0.0.2.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String SECONDARY_PULSE_DELTA = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.111.0";
    public static final String SECONDARY_BULK_REG = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    public static final String PRIMARY_BULK_REG = "0.0.0.1.1.2.12.0.0.0.0.0.0.0.0.3.72.0";

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

    @Before
    public void setUp() throws SQLException {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new IdsModule(),
                    new MeteringModule(SECONDARY_DELTA, SECONDARY_BULK, PRIMARY_DELTA, PRIMARY_BULK, SECONDARY_PULSE_DELTA, SECONDARY_BULK_REG, PRIMARY_BULK_REG),
                    new BasicPropertiesModule(),
                    new TimeModule(),
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
                    new NlsModule(),
                    new CustomPropertySetsModule(),
                    new BasicPropertiesModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            injector.getInstance(CustomPropertySetService.class);
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

        ReadingType deltaReadingType = meteringService.getReadingType(SECONDARY_DELTA).get();
        ReadingType bulkReadingType = meteringService.getReadingType(SECONDARY_BULK).get();

        Channel channel = createMeterAndChannelWithDelta(bulkReadingType);

        transactionService.run(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());

            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.toInstant(), BigDecimal.valueOf(10000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15).toInstant(), BigDecimal.valueOf(11000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 2).toInstant(), BigDecimal.valueOf(12000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 3).toInstant(), BigDecimal.valueOf(13000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 4).toInstant(), BigDecimal.valueOf(14000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 5).toInstant(), BigDecimal.valueOf(15000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 6).toInstant(), BigDecimal.valueOf(16000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 7).toInstant(), BigDecimal.valueOf(17000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 8).toInstant(), BigDecimal.valueOf(18000, 2), BATTERY_LOW));

            meterReading.addIntervalBlock(intervalBlock);

            channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);
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

            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 9).toInstant(), BigDecimal.valueOf(19000, 2), BATTERY_LOW));

            meterReading.addIntervalBlock(intervalBlock);

            channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);
        });

        readings = channel.getReadings(Range.atLeast(BASE.plusMinutes(15 * 9).toInstant()));

        assertThat(readings).hasSize(1);

        assertThat(readings.get(0).getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(1000, 2), 3, "Wh"));
        assertThat(readings.get(0).getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(19000, 2), 3, "Wh"));
        assertThat(readings.get(0).getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * 9).toInstant());
    }

    @Test
    public void testWriteBulkDataWithMultiplier() {

        ReadingType deltaReadingType = meteringService.getReadingType(PRIMARY_DELTA).get();
        ReadingType bulkReadingType = meteringService.getReadingType(SECONDARY_BULK).get();
        ReadingType bulkPrimaryReadingType = meteringService.getReadingType(PRIMARY_BULK).get();

        Channel channel = createMeterAndChannelWithMultiplier(bulkReadingType, bulkPrimaryReadingType, BigDecimal.valueOf(5, 0));

        transactionService.run(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());

            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.toInstant(), BigDecimal.valueOf(10000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15).toInstant(), BigDecimal.valueOf(11000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 2).toInstant(), BigDecimal.valueOf(12000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 3).toInstant(), BigDecimal.valueOf(13000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 4).toInstant(), BigDecimal.valueOf(14000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 5).toInstant(), BigDecimal.valueOf(15000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 6).toInstant(), BigDecimal.valueOf(16000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 7).toInstant(), BigDecimal.valueOf(17000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 8).toInstant(), BigDecimal.valueOf(18000, 2), BATTERY_LOW));

            meterReading.addIntervalBlock(intervalBlock);

            channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);
        });

        List<BaseReadingRecord> readings = channel.getReadings(Range.atLeast(BASE.toInstant()));

        assertThat(readings).hasSize(9);

        assertThat(readings.get(0).getQuantity(deltaReadingType)).isNull();
        assertThat(readings.get(0).getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10000, 2), 3, "Wh"));
        assertThat(readings.get(0).getTimeStamp()).isEqualTo(BASE.toInstant());

        for (int i = 1; i < 8; i++) {
            BaseReadingRecord baseReadingRecord = readings.get(i);
            assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(5000, 2), 3, "Wh"));
            assertThat(baseReadingRecord.getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10000 + i * 1000, 2), 3, "Wh"));
            assertThat(baseReadingRecord.getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * i).toInstant());
        }

        transactionService.run(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());

            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 9).toInstant(), BigDecimal.valueOf(19000, 2), BATTERY_LOW));

            meterReading.addIntervalBlock(intervalBlock);

            channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);
        });

        readings = channel.getReadings(Range.atLeast(BASE.plusMinutes(15 * 9).toInstant()));

        assertThat(readings).hasSize(1);

        assertThat(readings.get(0).getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(5000, 2), 3, "Wh"));
        assertThat(readings.get(0).getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(19000, 2), 3, "Wh"));
        assertThat(readings.get(0).getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * 9).toInstant());
    }

    @Test
    public void testWritePulseDataWithMultiplier() {
        ReadingType deltaReadingType = meteringService.getReadingType(PRIMARY_DELTA).get();
        ReadingType pulseDeltaReadingType = meteringService.getReadingType(SECONDARY_PULSE_DELTA).get();

        Channel channel = createMeterAndChannelWithMultiplier(pulseDeltaReadingType, deltaReadingType, BigDecimal.valueOf(50, 0));

        transactionService.run(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(pulseDeltaReadingType.getMRID());

            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.toInstant(), BigDecimal.valueOf(10, 0), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15).toInstant(), BigDecimal.valueOf(11, 0), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 2).toInstant(), BigDecimal.valueOf(12, 0), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 3).toInstant(), BigDecimal.valueOf(13, 0), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 4).toInstant(), BigDecimal.valueOf(14, 0), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 5).toInstant(), BigDecimal.valueOf(15, 0), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 6).toInstant(), BigDecimal.valueOf(16, 0), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 7).toInstant(), BigDecimal.valueOf(17, 0), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 8).toInstant(), BigDecimal.valueOf(18, 0), BATTERY_LOW));

            meterReading.addIntervalBlock(intervalBlock);

            channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);
        });

        List<BaseReadingRecord> readings = channel.getReadings(Range.atLeast(BASE.toInstant()));

        assertThat(readings).hasSize(9);

        for (int i = 0; i < 8; i++) {
            BaseReadingRecord baseReadingRecord = readings.get(i);
            assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(500 + 50 * i, 0), 3, "Wh"));
            assertThat(baseReadingRecord.getQuantity(pulseDeltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10 + i, 0), 0, "Count"));
            assertThat(baseReadingRecord.getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * i).toInstant());
        }

        Channel readChannel = meteringService.findChannel(channel.getId()).get();
        assertThat(readChannel.getReadingTypes()).hasSize(2);
    }

    @Test
    public void testWriteIrregularBulkWithMultiplied() {
        ReadingType multipliedBulkReadingType = meteringService.getReadingType(PRIMARY_BULK_REG).get();
        ReadingType bulkReadingType = meteringService.getReadingType(SECONDARY_BULK_REG).get();

        Channel channel = createMeterAndChannelWithMultiplier(bulkReadingType, multipliedBulkReadingType, BigDecimal.valueOf(2, 0));

        transactionService.run(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();

            Reading reading = ReadingImpl.of(bulkReadingType.getMRID(), BigDecimal.valueOf(123406, 2), BASE.toInstant());

            meterReading.addReading(reading);

            channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);
        });

        List<BaseReadingRecord> readings = channel.getReadings(Range.atLeast(BASE.toInstant()));

        assertThat(readings).hasSize(1);

        BaseReadingRecord baseReadingRecord = readings.get(0);
        assertThat(baseReadingRecord.getQuantity(multipliedBulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(246812, 2), 3, "Wh"));
        assertThat(baseReadingRecord.getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(123406, 2), 3, "Wh"));
        assertThat(baseReadingRecord.getTimeStamp()).isEqualTo(BASE.toInstant());

        Channel readChannel = meteringService.findChannel(channel.getId()).get();
        assertThat(readChannel.getReadingTypes()).hasSize(2);
    }

    @Test
    public void testWriteMissingBulkData() {
        ReadingType deltaReadingType = meteringService.getReadingType(SECONDARY_DELTA).get();
        ReadingType bulkReadingType = meteringService.getReadingType(SECONDARY_BULK).get();

        Channel channel = createMeterAndChannelWithDelta(bulkReadingType);

        transactionService.run(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());

            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.toInstant(), BigDecimal.valueOf(10000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15).toInstant(), BigDecimal.valueOf(11000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 2).toInstant(), BigDecimal.valueOf(12000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 3).toInstant(), BigDecimal.valueOf(13000, 2), BATTERY_LOW));
            // so not this one ! This is the missing one at first : intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 4).toInstant(), BigDecimal.valueOf(14000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 5).toInstant(), BigDecimal.valueOf(15000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 6).toInstant(), BigDecimal.valueOf(16000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 7).toInstant(), BigDecimal.valueOf(17000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 8).toInstant(), BigDecimal.valueOf(18000, 2), BATTERY_LOW));

            meterReading.addIntervalBlock(intervalBlock);

            channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);
        });

        List<BaseReadingRecord> readings = channel.getReadings(Range.atLeast(BASE.toInstant()));

        assertThat(readings).hasSize(8);

        for (int i = 0; i < 7; i++) {
            int multiplier = i >= 4 ? i + 1 : i;
            BaseReadingRecord baseReadingRecord = readings.get(i);
            if (i != 0 && i != 4) {
                assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(1000, 2), 3, "Wh"));
            } else {
                assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isNull();
            }
            assertThat(baseReadingRecord.getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10000 + multiplier * 1000, 2), 3, "Wh"));
            assertThat(baseReadingRecord.getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * multiplier).toInstant());
        }

        transactionService.run(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());

            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 4).toInstant(), BigDecimal.valueOf(14000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 9).toInstant(), BigDecimal.valueOf(19000, 2), BATTERY_LOW));

            meterReading.addIntervalBlock(intervalBlock);

            channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);
        });

        readings = channel.getReadings(Range.atLeast(BASE.toInstant()));

        assertThat(readings).hasSize(10);

        for (int i = 0; i < 9; i++) {
            BaseReadingRecord baseReadingRecord = readings.get(i);
            if (i != 0) {
                assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(1000, 2), 3, "Wh"));
            } else {
                assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isNull();
            }
            assertThat(baseReadingRecord.getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10000 + i * 1000, 2), 3, "Wh"));
            assertThat(baseReadingRecord.getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * i).toInstant());
        }
    }

    @Test
    public void testWriteMissingBulkDataWithMultiplier() {
        ReadingType deltaReadingType = meteringService.getReadingType(PRIMARY_DELTA).get();
        ReadingType bulkReadingType = meteringService.getReadingType(SECONDARY_BULK).get();
        ReadingType bulkPrimaryReadingType = meteringService.getReadingType(PRIMARY_BULK).get();

        Channel channel = createMeterAndChannelWithMultiplier(bulkReadingType, bulkPrimaryReadingType, BigDecimal.valueOf(2, 0));

        transactionService.run(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());

            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.toInstant(), BigDecimal.valueOf(10000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15).toInstant(), BigDecimal.valueOf(11000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 2).toInstant(), BigDecimal.valueOf(12000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 3).toInstant(), BigDecimal.valueOf(13000, 2), BATTERY_LOW));
            // so not this one ! This is the missing one at first : intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 4).toInstant(), BigDecimal.valueOf(14000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 5).toInstant(), BigDecimal.valueOf(15000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 6).toInstant(), BigDecimal.valueOf(16000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 7).toInstant(), BigDecimal.valueOf(17000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 8).toInstant(), BigDecimal.valueOf(18000, 2), BATTERY_LOW));

            meterReading.addIntervalBlock(intervalBlock);

            channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);
        });

        List<BaseReadingRecord> readings = channel.getReadings(Range.atLeast(BASE.toInstant()));

        assertThat(readings).hasSize(8);

        for (int i = 0; i < 7; i++) {
            int multiplier = i >= 4 ? i + 1 : i;
            BaseReadingRecord baseReadingRecord = readings.get(i);
            if (i != 0 && i != 4) {
                assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(2000, 2), 3, "Wh"));
            } else {
                assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isNull();
            }
            assertThat(baseReadingRecord.getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10000 + multiplier * 1000, 2), 3, "Wh"));
            assertThat(baseReadingRecord.getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * multiplier).toInstant());
        }

        transactionService.run(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());

            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 4).toInstant(), BigDecimal.valueOf(14000, 2), BATTERY_LOW));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15 * 9).toInstant(), BigDecimal.valueOf(19000, 2), BATTERY_LOW));

            meterReading.addIntervalBlock(intervalBlock);

            channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);
        });

        readings = channel.getReadings(Range.atLeast(BASE.toInstant()));

        assertThat(readings).hasSize(10);

        for (int i = 0; i < 9; i++) {
            BaseReadingRecord baseReadingRecord = readings.get(i);
            if (i != 0) {
                assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(2000, 2), 3, "Wh"));
            } else {
                assertThat(baseReadingRecord.getQuantity(deltaReadingType)).isNull();
            }
            assertThat(baseReadingRecord.getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(10000 + i * 1000, 2), 3, "Wh"));
            assertThat(baseReadingRecord.getTimeStamp()).isEqualTo(BASE.plusMinutes(15 * i).toInstant());
        }
    }

    @Test
    public void testWriteBulkDataWithBackflowUnderflowAndOverflowForMdcAndMdm() {
        ReadingType deltaReadingType = meteringService.getReadingType(SECONDARY_DELTA).get();
        ReadingType bulkReadingType = meteringService.getReadingType(SECONDARY_BULK).get();

        Channel channel = createMeterAndChannelWithDelta(bulkReadingType);

        transactionService.run(() -> {
            Meter meter = channel.getChannelsContainer().getMeter().get();
            meter.startingConfigurationOn(Instant.EPOCH)
                    .configureReadingType(bulkReadingType)
                    .withOverflowValue(BigDecimal.valueOf(999999))
                    .create();
        });

        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());

        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.toInstant(), BigDecimal.valueOf(100, 0), BATTERY_LOW));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(15).toInstant(), BigDecimal.valueOf(999998, 0), Collections.emptySet()));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(BASE.plusMinutes(30).toInstant(), BigDecimal.valueOf(100, 0), Collections.emptySet()));

        meterReading.addIntervalBlock(intervalBlock);

        Stream.of(QualityCodeSystem.MDC, QualityCodeSystem.MDM).forEach(system -> {
            transactionService.run(() -> {
                channel.getChannelsContainer().getMeter().get().store(system, meterReading);
            });

            List<BaseReadingRecord> readings = channel.getReadings(Range.atLeast(BASE.toInstant()));

            assertThat(readings).hasSize(3);

            assertThat(readings.get(0).getQuantity(deltaReadingType)).isNull();
            assertThat(readings.get(0).getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(100, 0), 3, "Wh"));
            assertThat(readings.get(0).getTimeStamp()).isEqualTo(BASE.toInstant());

            assertThat(readings.get(1).getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(-102, 0), 3, "Wh"));
            assertThat(readings.get(1).getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(999998, 0), 3, "Wh"));
            assertThat(readings.get(1).getTimeStamp()).isEqualTo(BASE.plusMinutes(15).toInstant());

            assertThat(readings.get(2).getQuantity(deltaReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(102, 0), 3, "Wh"));
            assertThat(readings.get(2).getQuantity(bulkReadingType)).isEqualTo(Quantity.create(BigDecimal.valueOf(100, 0), 3, "Wh"));
            assertThat(readings.get(2).getTimeStamp()).isEqualTo(BASE.plusMinutes(30).toInstant());

            List<ReadingQualityRecord> qualities = channel.findReadingQualities()
                    .atTimestamp(BASE.plusMinutes(15).toInstant())
                    .collect();
            assertThat(qualities).hasSize(1);
            assertThat(qualities.get(0).getTypeCode()).isEqualTo(ReadingQualityType.of(system, QualityCodeIndex.REVERSEROTATION).getCode());

            qualities = channel.findReadingQualities()
                    .atTimestamp(BASE.plusMinutes(30).toInstant())
                    .collect();
            assertThat(qualities).hasSize(1);
            assertThat(qualities.get(0).getTypeCode()).isEqualTo(ReadingQualityType.of(system, QualityCodeIndex.OVERFLOWCONDITIONDETECTED).getCode());
        });
    }

    private Channel createMeterAndChannelWithMultiplier(ReadingType measured, ReadingType caluclated, BigDecimal multiplierValue) {
        return transactionService.execute(() -> {
                AmrSystem mdc = meteringService.findAmrSystem(1L).get();
                Meter meter = mdc.newMeter("AMR_ID")
                        .setMRID("meterMRID")
                        .setName("Meter")
                        .create();
                MeterActivation meterActivation = meter.activate(ACTIVATION.toInstant());

                MultiplierType multiplierType = meteringService.createMultiplierType("multiplierType");

                meterActivation.setMultiplier(multiplierType, multiplierValue);

                meter.startingConfigurationOn(ACTIVATION.toInstant())
                        .configureReadingType(measured)
                        .withMultiplierOfType(multiplierType)
                        .calculating(caluclated)
                        .create();

            return meterActivation.getChannelsContainer().createChannel(measured);
            });
    }

    private Channel createMeterAndChannelWithDelta(ReadingType bulkReadingType) {
        return transactionService.execute(() -> {
            AmrSystem mdc = meteringService.findAmrSystem(1L).get();
            Meter meter = mdc.newMeter("AMR_ID")
                    .setMRID("meterMRID")
                    .setName("Meter")
                    .create();
            MeterActivation meterActivation = meter.activate(ACTIVATION.toInstant());
            return meterActivation.getChannelsContainer().createChannel(bulkReadingType);
        });
    }
}
