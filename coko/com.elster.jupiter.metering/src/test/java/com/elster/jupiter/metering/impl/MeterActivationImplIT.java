package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MeterActivationImplIT {

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
                    new MeteringModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0"),
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
    public void testPersistence() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem system = meteringService.findAmrSystem(1).get();
            Meter meter = system.newMeter("1").create();
            MeterActivation meterActivation = meter.activate(ZonedDateTime.of(2012, 12, 19, 14, 15, 54, 0, ZoneId.systemDefault()).toInstant());
            ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
            Channel channel = meterActivation.createChannel(readingType);
            MeterActivation loaded = meteringService.findMeterActivation(meterActivation.getId()).get();
            assertThat(loaded.getChannels()).hasSize(1).contains(channel);
        }
    }

    @Test
    public void testCOPL854() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        long meterId, usagePointId;
        Instant start3 = Instant.now();
        Instant start1 = start3.minusSeconds(86400);
        Instant start2 = start3.minusSeconds(43200);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
            Meter meter = amrSystem.newMeter("1").create();
            meterId = meter.getId();
            UsagePoint up = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                    .orElseThrow(IllegalArgumentException::new).newUsagePoint("abcd")
                    .create();
            usagePointId = up.getId();

            meter.activate(up, start1);

            ctx.commit();
        }
        assertMeterActivations(meterId, usagePointId, start1);

        try (TransactionContext context = injector.getInstance(TransactionService.class).getContext()) {
            Meter meter = meteringService.findMeter(meterId).get();
            meter.getUsagePoint(Instant.now()).get().activate(meter, start2);
            context.commit();
        }
        assertMeterActivations(meterId, usagePointId, start1, start2);

        try (TransactionContext context = injector.getInstance(TransactionService.class).getContext()) {
            UsagePoint up = meteringService.findUsagePoint(usagePointId).get();
            Meter meter = meteringService.findMeter(meterId).get();

            up.activate(meter, start3);
            context.commit();
        }
        assertMeterActivations(meterId, usagePointId, start1, start2, start3);
    }

    private void assertMeterActivations(long meterId, long usagePointId, Instant... startTimes) {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        List<? extends MeterActivation> meterActivations = meteringService.findMeter(meterId).get().getMeterActivations();
        assertThat(meterActivations).hasSize(startTimes.length);
        for (int i = 0; i < startTimes.length; i++) {
            Instant startTime = startTimes[i];
            Instant endTime = (i < startTimes.length - 1 ? startTimes[i + 1] : null);
            assertThat(meterActivations.get(i).getMeter()).isPresent();
            assertThat(meterActivations.get(i).getMeter().get().getId()).isEqualTo(meterId);
            assertThat(meterActivations.get(i).getUsagePoint()).isPresent();
            assertThat(meterActivations.get(i).getUsagePoint().get().getId()).isEqualTo(usagePointId);
            assertThat(meterActivations.get(i).getStart()).as("Start date of meter activation " + i + " not as expected").isEqualTo(startTime);
            assertThat(meterActivations.get(i).getEnd()).as("End date of meter activation " + i + " not as expected").isEqualTo(endTime);
        }
    }

    @Test
    public void testAdvanceWithReadings() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem system = meteringService.findAmrSystem(1).get();
            Meter meter = system.newMeter("1").create();
            ZonedDateTime startTime = ZonedDateTime.of(2012, 12, 19, 14, 15, 54, 0, ZoneId.systemDefault());
            ZonedDateTime originalCutOff = ZonedDateTime.of(2012, 12, 25, 0, 0, 0, 0, ZoneId.systemDefault());
            ZonedDateTime newCutOff = ZonedDateTime.of(2012, 12, 20, 0, 0, 0, 0, ZoneId.systemDefault());
            MeterActivation meterActivation = meter.activate(startTime.toInstant());
            meterActivation.endAt(originalCutOff.toInstant());
            ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
            Channel channel = meterActivation.createChannel(readingType);
            MeterActivation currentActivation = meter.activate(originalCutOff.toInstant());
            Channel currentChannel = currentActivation.createChannel(readingType);
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.minusMinutes(15).toInstant(), BigDecimal.valueOf(4025, 2)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.toInstant(), BigDecimal.valueOf(4175, 2)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.plusMinutes(15).toInstant(), BigDecimal.valueOf(4225, 2)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(originalCutOff.toInstant(), BigDecimal.valueOf(4725, 2)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(originalCutOff.plusMinutes(15).toInstant(), BigDecimal.valueOf(4825, 2)));
            meterReading.addIntervalBlock(intervalBlock);
            meter.store(meterReading);

            currentActivation.advanceStartDate(newCutOff.toInstant());

            assertThat(meter.getMeterActivations()).hasSize(2);
            MeterActivation first = meter.getMeterActivations().get(0);
            MeterActivation second = meter.getMeterActivations().get(1);
            assertThat(first.getRange()).isEqualTo(Range.closedOpen(startTime.toInstant(), newCutOff.toInstant()));
            assertThat(second.getRange()).isEqualTo(Range.atLeast(newCutOff.toInstant()));

            List<? extends BaseReadingRecord> firstReadings = first.getReadings(Range.all(), readingType);
            assertThat(firstReadings).hasSize(2);
            assertThat(firstReadings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(4025, 2));
            assertThat(firstReadings.get(0).getTimeStamp()).isEqualTo(newCutOff.minusMinutes(15).toInstant());
            assertThat(firstReadings.get(1).getValue()).isEqualTo(BigDecimal.valueOf(4175, 2));
            assertThat(firstReadings.get(1).getTimeStamp()).isEqualTo(newCutOff.toInstant());
            List<? extends BaseReadingRecord> secondReadings = second.getReadings(Range.all(), readingType);
            assertThat(secondReadings).hasSize(3);
            assertThat(secondReadings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(4225, 2));
            assertThat(secondReadings.get(0).getTimeStamp()).isEqualTo(newCutOff.plusMinutes(15).toInstant());
            assertThat(secondReadings.get(1).getValue()).isEqualTo(BigDecimal.valueOf(4725, 2));
            assertThat(secondReadings.get(1).getTimeStamp()).isEqualTo(originalCutOff.toInstant());
            assertThat(secondReadings.get(2).getValue()).isEqualTo(BigDecimal.valueOf(4825, 2));
            assertThat(secondReadings.get(2).getTimeStamp()).isEqualTo(originalCutOff.plusMinutes(15).toInstant());

            List<? extends BaseReadingRecord> firstChannelReadings = first.getChannels().get(0).getReadings(readingType, Range.all());
            assertThat(firstChannelReadings).hasSize(2);
            assertThat(firstChannelReadings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(4025, 2));
            assertThat(firstChannelReadings.get(0).getTimeStamp()).isEqualTo(newCutOff.minusMinutes(15).toInstant());
            assertThat(firstChannelReadings.get(1).getValue()).isEqualTo(BigDecimal.valueOf(4175, 2));
            assertThat(firstChannelReadings.get(1).getTimeStamp()).isEqualTo(newCutOff.toInstant());
            List<? extends BaseReadingRecord> secondChannelReadings = second.getChannels().get(0).getReadings(readingType, Range.all());
            assertThat(secondChannelReadings).hasSize(3);
            assertThat(secondChannelReadings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(4225, 2));
            assertThat(secondChannelReadings.get(0).getTimeStamp()).isEqualTo(newCutOff.plusMinutes(15).toInstant());
            assertThat(secondChannelReadings.get(1).getValue()).isEqualTo(BigDecimal.valueOf(4725, 2));
            assertThat(secondChannelReadings.get(1).getTimeStamp()).isEqualTo(originalCutOff.toInstant());
            assertThat(secondChannelReadings.get(2).getValue()).isEqualTo(BigDecimal.valueOf(4825, 2));
            assertThat(secondChannelReadings.get(2).getTimeStamp()).isEqualTo(originalCutOff.plusMinutes(15).toInstant());
        }
    }

    @Test
    public void testAdvanceWithoutData() {
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem system = meteringService.findAmrSystem(1).get();
            Meter meter = system.newMeter("1").create();
            ZonedDateTime startTime = ZonedDateTime.of(2012, 12, 19, 14, 15, 54, 0, ZoneId.systemDefault());
            ZonedDateTime originalCutOff = ZonedDateTime.of(2012, 12, 25, 0, 0, 0, 0, ZoneId.systemDefault());
            ZonedDateTime newCutOff = ZonedDateTime.of(2012, 12, 20, 0, 0, 0, 0, ZoneId.systemDefault());
            MeterActivation meterActivation = meter.activate(startTime.toInstant());
            meterActivation.endAt(originalCutOff.toInstant());
            ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
            Channel channel = meterActivation.createChannel(readingType);
            MeterActivation currentActivation = meter.activate(originalCutOff.toInstant());
            Channel currentChannel = currentActivation.createChannel(readingType);

            currentActivation.advanceStartDate(newCutOff.toInstant());

            assertThat(meter.getMeterActivations()).hasSize(2);

            MeterActivation first = meter.getMeterActivations().get(0);
            MeterActivation second = meter.getMeterActivations().get(1);
            assertThat(first.getRange()).isEqualTo(Range.closedOpen(startTime.toInstant(), newCutOff.toInstant()));
            assertThat(second.getRange()).isEqualTo(Range.atLeast(newCutOff.toInstant()));
        }
    }

    @Test
    public void testAdvanceWithReadingsAndQualities() {
        ZonedDateTime startTime = ZonedDateTime.of(2012, 12, 19, 14, 15, 54, 0, ZoneId.systemDefault());
        ZonedDateTime originalCutOff = ZonedDateTime.of(2012, 12, 25, 0, 0, 0, 0, ZoneId.systemDefault());
        ZonedDateTime newCutOff = ZonedDateTime.of(2012, 12, 20, 0, 0, 0, 0, ZoneId.systemDefault());
        MeteringService meteringService = injector.getInstance(MeteringService.class);
        Meter meter = null;
        MeterActivation currentActivation = null;
        ReadingType readingType = null;
        MeterActivation meterActivation = null;
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            AmrSystem system = meteringService.findAmrSystem(1).get();
            meter = system.newMeter("1").create();
            meterActivation = meter.activate(startTime.toInstant());
            meterActivation.endAt(originalCutOff.toInstant());
            readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
            Channel channel = meterActivation.createChannel(readingType);
            currentActivation = meter.activate(originalCutOff.toInstant());
            Channel currentChannel = currentActivation.createChannel(readingType);
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.minusMinutes(15).toInstant(), BigDecimal.valueOf(4025, 2)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.toInstant(), BigDecimal.valueOf(4175, 2)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.plusMinutes(15).toInstant(), BigDecimal.valueOf(4225, 2)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(originalCutOff.toInstant(), BigDecimal.valueOf(4725, 2)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(originalCutOff.plusMinutes(15).toInstant(), BigDecimal.valueOf(4825, 2)));
            meterReading.addIntervalBlock(intervalBlock);
            meter.store(meterReading);
            meterActivation.getChannels().get(0).createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), readingType, newCutOff.minusMinutes(15).toInstant()).save();
            meterActivation.getChannels().get(0).createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), readingType, newCutOff.toInstant()).save();
            ReadingQualityRecord readingQuality = meterActivation.getChannels().get(0).createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), readingType, newCutOff.plusMinutes(15).toInstant());
            readingQuality.makePast();
            meterActivation.getChannels().get(0).createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), readingType, originalCutOff.toInstant()).save();
            currentActivation.getChannels().get(0).createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), readingType, originalCutOff.plusMinutes(15).toInstant()).save();
            ctx.commit();
        }

        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            currentActivation.advanceStartDate(newCutOff.toInstant());
            ctx.commit();
        }

        assertThat(meter.getMeterActivations()).hasSize(2);
        MeterActivation first = meter.getMeterActivations().get(0);
        MeterActivation second = meter.getMeterActivations().get(1);
        assertThat(first.getRange()).isEqualTo(Range.closedOpen(startTime.toInstant(), newCutOff.toInstant()));
        assertThat(second.getRange()).isEqualTo(Range.atLeast(newCutOff.toInstant()));

        List<? extends BaseReadingRecord> firstReadings = first.getReadings(Range.all(), readingType);
        assertThat(firstReadings).hasSize(2);
        assertThat(firstReadings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(4025, 2));
        assertThat(firstReadings.get(0).getTimeStamp()).isEqualTo(newCutOff.minusMinutes(15).toInstant());
        assertThat(firstReadings.get(1).getValue()).isEqualTo(BigDecimal.valueOf(4175, 2));
        assertThat(firstReadings.get(1).getTimeStamp()).isEqualTo(newCutOff.toInstant());
        List<? extends BaseReadingRecord> secondReadings = second.getReadings(Range.all(), readingType);
        assertThat(secondReadings).hasSize(3);
        assertThat(secondReadings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(4225, 2));
        assertThat(secondReadings.get(0).getTimeStamp()).isEqualTo(newCutOff.plusMinutes(15).toInstant());
        assertThat(secondReadings.get(1).getValue()).isEqualTo(BigDecimal.valueOf(4725, 2));
        assertThat(secondReadings.get(1).getTimeStamp()).isEqualTo(originalCutOff.toInstant());
        assertThat(secondReadings.get(2).getValue()).isEqualTo(BigDecimal.valueOf(4825, 2));
        assertThat(secondReadings.get(2).getTimeStamp()).isEqualTo(originalCutOff.plusMinutes(15).toInstant());

        List<? extends BaseReadingRecord> firstChannelReadings = first.getChannels().get(0).getReadings(readingType, Range.all());
        assertThat(firstChannelReadings).hasSize(2);
        assertThat(firstChannelReadings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(4025, 2));
        assertThat(firstChannelReadings.get(0).getTimeStamp()).isEqualTo(newCutOff.minusMinutes(15).toInstant());
        assertThat(firstChannelReadings.get(1).getValue()).isEqualTo(BigDecimal.valueOf(4175, 2));
        assertThat(firstChannelReadings.get(1).getTimeStamp()).isEqualTo(newCutOff.toInstant());
        List<? extends BaseReadingRecord> secondChannelReadings = second.getChannels().get(0).getReadings(readingType, Range.all());
        assertThat(secondChannelReadings).hasSize(3);
        assertThat(secondChannelReadings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(4225, 2));
        assertThat(secondChannelReadings.get(0).getTimeStamp()).isEqualTo(newCutOff.plusMinutes(15).toInstant());
        assertThat(secondChannelReadings.get(1).getValue()).isEqualTo(BigDecimal.valueOf(4725, 2));
        assertThat(secondChannelReadings.get(1).getTimeStamp()).isEqualTo(originalCutOff.toInstant());
        assertThat(secondChannelReadings.get(2).getValue()).isEqualTo(BigDecimal.valueOf(4825, 2));
        assertThat(secondChannelReadings.get(2).getTimeStamp()).isEqualTo(originalCutOff.plusMinutes(15).toInstant());

        List<ReadingQualityRecord> firstQualities = first.getChannels().get(0).findReadingQuality(Range.<Instant>all());
        assertThat(firstQualities).hasSize(2);
        assertThat(firstQualities.get(0).getReadingTimestamp()).isEqualTo(newCutOff.minusMinutes(15).toInstant());
        assertThat(firstQualities.get(1).getReadingTimestamp()).isEqualTo(newCutOff.toInstant());
        List<ReadingQualityRecord> secondQualities = second.getChannels().get(0).findReadingQuality(Range.<Instant>all());
        assertThat(secondQualities).hasSize(3);
        assertThat(secondQualities.get(0).getReadingTimestamp()).isEqualTo(newCutOff.plusMinutes(15).toInstant());
        assertThat(secondQualities.get(0).isActual()).isFalse();
        assertThat(secondQualities.get(1).getReadingTimestamp()).isEqualTo(originalCutOff.toInstant());
        assertThat(secondQualities.get(2).getReadingTimestamp()).isEqualTo(originalCutOff.plusMinutes(15).toInstant());
    }

}
