package com.elster.jupiter.metering;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.impl.MeteringModule;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MeterReadingStorerTest {
    private static final String EVENTTYPECODE = "3.7.12.242";

    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    private Clock clock = Clock.system(ZoneId.of("Europe/Athens"));

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
                new UtilModule(clock),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(false),
                new BpmModule(),
                new FiniteStateMachineModule(),
                new NlsModule()
        );
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
    public void testBulk() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            Meter meter = amrSystem.newMeter("myMeter");
            meter.save();
            meter = amrSystem.lockMeter("myMeter").get();
            ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                    .period(TimeAttribute.MINUTE15)
                    .accumulate(Accumulation.BULKQUANTITY)
                    .flow(FlowDirection.FORWARD)
                    .measure(MeasurementKind.ENERGY)
                    .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR);
            String intervalReadingTypeCode = builder.code();
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl block = IntervalBlockImpl.of(intervalReadingTypeCode);
            meterReading.addIntervalBlock(block);
            Instant instant = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
            block.addIntervalReading(IntervalReadingImpl.of(instant, BigDecimal.valueOf(1000)));
            block.addIntervalReading(IntervalReadingImpl.of(instant.plusSeconds(15 * 60L), BigDecimal.valueOf(1100)));
            String registerReadingTypeCode = builder.period(TimeAttribute.NOTAPPLICABLE).code();
            ReadingImpl reading = ReadingImpl.of(registerReadingTypeCode, BigDecimal.valueOf(1200), instant);
            reading.addQuality("1.1.1", "Whatever");
            meterReading.addReading(reading);
            meter.store(meterReading);
            assertThat(meter.getMeterActivations().get(0).getZoneId()).isEqualTo(clock.getZone());
            List<? extends BaseReadingRecord> readings = meter.getMeterActivations().get(0).getReadings(
                    Range.openClosed(instant.minusSeconds(15 * 60L), instant.plusSeconds(15 * 60L)),
                    meteringService.getReadingType(builder.period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code()).get());
            assertThat(readings).hasSize(2);
            assertThat(readings.get(0).getQuantity(0)).isNull();
            assertThat(readings.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.valueOf(100));
            readings = meter.getMeterActivations().get(0).getReadings(
                    Range.openClosed(instant.minusSeconds(15 * 60L), instant.plusSeconds(15 * 60L)),
                    meteringService.getReadingType(registerReadingTypeCode).get());
            assertThat(readings).hasSize(1);
            assertThat(readings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(1200));
            assertThat(meter.getReadingsBefore(instant, meteringService.getReadingType(intervalReadingTypeCode).get(), 10)).isEmpty();
            assertThat(meter.getReadingsOnOrBefore(instant, meteringService.getReadingType(intervalReadingTypeCode).get(), 10)).hasSize(1);
            List<Channel> channels = meter.getMeterActivations().get(0).getChannels();
            Optional<Channel> channel = Optional.empty();
            for (Channel candidate : channels) {
                if (candidate.getMainReadingType().getMRID().equals(registerReadingTypeCode)) {
                    channel = Optional.of(candidate);
                }
            }
            assertThat(channel.isPresent()).isTrue();
//            assertThat(channel.get().findReadingQuality(dateTime.toDate())).hasSize(1);
//            assertThat(meter.getReadingQualities(Range.atLeast(Instant.EPOCH))).hasSize(1);
            //update reading quality
//            meterReading = new MeterReadingImpl();
//            reading = new ReadingImpl(registerReadingTypeCode, BigDecimal.valueOf(1200), dateTime.toDate());
//            String newComment = "Whatever it was";
//        	reading.addQuality("1.1.1",newComment);
//        	reading.addQuality("1.1.2",newComment);
//        	meterReading.addReading(reading);
//        	meter.store(meterReading);
//            assertThat(channel.get().findReadingQuality(dateTime.toDate())).hasSize(2);
//            assertThat(channel.get().findReadingQuality(dateTime.toDate()).stream().map(quality -> quality.getComment()).allMatch(comment -> comment.equals(newComment))).isTrue();
            ctx.commit();
        }

    }

    @Test
    public void testDelta() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            Meter meter = amrSystem.newMeter("myMeter");
            meter.save();
            String readingTypeCode = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                    .period(TimeAttribute.MINUTE15)
                    .accumulate(Accumulation.DELTADELTA)
                    .flow(FlowDirection.FORWARD)
                    .measure(MeasurementKind.ENERGY)
                    .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR)
                    .code();
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl block = IntervalBlockImpl.of(readingTypeCode);
            meterReading.addIntervalBlock(block);
            final Instant instant = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
            block.addIntervalReading(IntervalReadingImpl.of(instant, BigDecimal.valueOf(1000)));
            ProfileStatus status = ProfileStatus.of(ProfileStatus.Flag.BATTERY_LOW);
            IntervalReadingImpl reading = IntervalReadingImpl.of(instant.plusSeconds(15 * 60L), BigDecimal.valueOf(1100), status);
            reading.addQuality("3.6.1");
            block.addIntervalReading(reading);
            reading = IntervalReadingImpl.of(instant.plusSeconds(30 * 60L), BigDecimal.valueOf(1200), status);
            reading.addQuality("3.6.2");
            block.addIntervalReading(reading);
            meter.store(meterReading);
            Channel channel = meter.getMeterActivations().stream().flatMap(ma -> ma.getChannels().stream()).findFirst().get();
            List<BaseReadingRecord> readings = channel.getReadings(Range.openClosed(instant.minusSeconds(15 * 60L), instant.plusSeconds(15 * 60L)));
            assertThat(readings).hasSize(2);
            assertThat(readings.get(0).getQuantity(0).getValue()).isEqualTo(BigDecimal.valueOf(1000));
            assertThat(readings.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.valueOf(1100));
            assertThat(((IntervalReadingRecord) readings.get(1)).getProfileStatus()).isEqualTo(status);
            Range<Instant> range = Range.closed(instant.minusSeconds(15 * 60L), instant.plusSeconds(30 * 60L));
            assertThat(channel.findReadingQuality(range)).hasSize(2);
            channel.removeReadings(readings);
            assertThat(channel.getReadings(range)).hasSize(1);
            assertThat(channel.findReadingQuality(range)).hasSize(3);
            assertThat(channel.findReadingQuality(range).get(1).getType().qualityIndex().get()).isEqualTo(QualityCodeIndex.REJECTED);
            ctx.commit();
        }
    }

    @Test
    public void testIdempotency() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            Meter meter = amrSystem.newMeter("myMeter");
            meter.save();
            ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                    .period(TimeAttribute.MINUTE15)
                    .accumulate(Accumulation.BULKQUANTITY)
                    .flow(FlowDirection.FORWARD)
                    .measure(MeasurementKind.ENERGY)
                    .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR);
            String intervalReadingTypeCode = builder.code();
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl block = IntervalBlockImpl.of(intervalReadingTypeCode);
            meterReading.addIntervalBlock(block);
            Instant instant = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
            block.addIntervalReading(IntervalReadingImpl.of(instant, BigDecimal.valueOf(1000)));
            block.addIntervalReading(IntervalReadingImpl.of(instant.plusSeconds(15 * 60L), BigDecimal.valueOf(1100)));
            String registerReadingTypeCode = builder.period(TimeAttribute.NOTAPPLICABLE).code();
            Reading reading = ReadingImpl.of(registerReadingTypeCode, BigDecimal.valueOf(1200), instant);
            meterReading.addReading(reading);

            EndDeviceEventImpl endDeviceEvent = EndDeviceEventImpl.of(EVENTTYPECODE, instant);
            HashMap<String, String> eventData = new HashMap<>();
            eventData.put("A", "B");
            endDeviceEvent.setEventData(eventData);
            meterReading.addEndDeviceEvent(endDeviceEvent);

            meter.store(meterReading);
            meter.store(meterReading);

            List<? extends BaseReadingRecord> readings = meter.getMeterActivations().get(0).getReadings(
                    Range.openClosed(instant.minusSeconds(15 * 60L), instant.plusSeconds(15 * 60L)),
                    meteringService.getReadingType(builder.period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code()).get());
            assertThat(readings).hasSize(2);
            assertThat(readings.get(0).getQuantity(0)).isNull();
            assertThat(readings.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.valueOf(100));
            readings = meter.getMeterActivations().get(0).getReadings(
                    Range.openClosed(instant.minusSeconds(15 * 60L), instant.plusSeconds(15 * 60L)),
                    meteringService.getReadingType(registerReadingTypeCode).get());
            assertThat(readings).hasSize(1);
            assertThat(readings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(1200));
            assertThat(readings.get(0).getQuantities().get(0).getValue()).isEqualTo(BigDecimal.valueOf(1200));
            assertThat(((Reading) readings.get(0)).getText()).isNull();
            ctx.commit();
        }
    }

    @Test
    public void testAddRegularReading() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            Meter meter = amrSystem.newMeter("myMeter");
            meter.save();
            String intervalReadingTypeCode = "32.12.2.4.1.9.58.0.0.0.0.0.0.0.0.0.0.0";
            Instant instant = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
            Reading reading = ReadingImpl.of(intervalReadingTypeCode, BigDecimal.valueOf(1200), instant);
            meter.store(MeterReadingImpl.of(reading));
            List<? extends BaseReadingRecord> readings = meter.getReadings(Range.all(), meteringService.getReadingType(intervalReadingTypeCode).get());
            assertThat(readings).isNotEmpty();
            Channel channel = meter.getMeterActivations().get(0).getChannels().get(0);
            List<Reading> changes = new ArrayList<>();
            changes.add(ReadingImpl.of(intervalReadingTypeCode, BigDecimal.valueOf(1300), instant));
            changes.add(ReadingImpl.of(intervalReadingTypeCode, BigDecimal.valueOf(1400), instant.plusSeconds(3600)));
            channel.editReadings(changes);
            readings = meter.getReadings(Range.atLeast(Instant.EPOCH), meteringService.getReadingType(intervalReadingTypeCode).get());
            assertThat(readings).isNotEmpty();
            assertThat(readings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(1300));
            assertThat(readings.get(0).edited()).isTrue();
            assertThat(readings.get(0).wasAdded()).isFalse();
            assertThat(readings.get(1).edited()).isTrue();
            assertThat(readings.get(1).wasAdded()).isTrue();
            ctx.commit();

        }
    }

    @Test
    public void testAddTextRegister() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            Meter meter = amrSystem.newMeter("myMeter");
            meter.save();
            String readingTypeCode = "0.12.0.0.1.9.58.0.0.0.0.0.0.0.0.0.0.0";
            Instant instant = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
            Reading reading = ReadingImpl.of(readingTypeCode, "Sample text", instant);
            meter.store(MeterReadingImpl.of(reading));
            List<? extends BaseReadingRecord> readings = meter.getMeterActivations().get(0).getReadings(
                    Range.openClosed(instant.minusSeconds(15 * 60L), instant.plusSeconds(15 * 60L)),
                    meteringService.getReadingType(readingTypeCode).get());
            assertThat(((ReadingRecord) readings.get(0)).getText()).isEqualTo("Sample text");
            assertThat(readings.get(0).getValue()).isNull();
            ctx.commit();
        }
    }

    @Test
    public void testBulkUpdate() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            Meter meter = amrSystem.newMeter("myMeter");
            meter.save();
            ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                    .period(TimeAttribute.MINUTE15)
                    .accumulate(Accumulation.BULKQUANTITY)
                    .flow(FlowDirection.FORWARD)
                    .measure(MeasurementKind.ENERGY)
                    .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR);
            String intervalReadingTypeCode = builder.code();
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl block = IntervalBlockImpl.of(intervalReadingTypeCode);
            meterReading.addIntervalBlock(block);
            Instant instant = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();

            block.addIntervalReading(IntervalReadingImpl.of(instant, BigDecimal.valueOf(1000)));
            block.addIntervalReading(IntervalReadingImpl.of(instant.plusSeconds(15 * 60L), BigDecimal.valueOf(1100)));
            String registerReadingTypeCode = builder.period(TimeAttribute.NOTAPPLICABLE).code();
            meter.store(meterReading);

            Channel channel = meter.getMeterActivations().get(0).getChannels().get(0);
            IntervalReading reading = IntervalReadingImpl.of(instant.plusSeconds(15 * 60L), BigDecimal.valueOf(50));
            channel.editReadings(ImmutableList.of(reading));

            List<? extends BaseReadingRecord> readings = channel.getReadings(
                    Range.openClosed(instant.minusSeconds(15 * 60L), instant.plusSeconds(15 * 60L)));
            assertThat(readings).hasSize(2);
            assertThat(readings.get(0).getQuantity(0)).isNull();
            assertThat(readings.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.valueOf(50));
            assertThat(readings.get(1).getQuantity(1).getValue()).isEqualTo(BigDecimal.valueOf(1100));
            assertThat(readings.get(1).getProcesStatus().get(ProcessStatus.Flag.EDITED)).isTrue();
            ctx.commit();
        }

    }


}
