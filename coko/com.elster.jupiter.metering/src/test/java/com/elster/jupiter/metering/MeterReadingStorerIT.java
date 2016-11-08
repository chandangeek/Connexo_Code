package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.Status;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.readings.EndDeviceEvent;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.ProtocolReadingQualities;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MeterReadingStorerIT {
    private static final String EVENTTYPECODE = "3.7.12.242";
    public static final Clock clock = Clock.system(ZoneId.of("Europe/Athens"));
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = MeteringInMemoryBootstrapModule.withClockAndReadingTypes(clock, "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");

    @BeforeClass
    public static void beforeClass() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void afterClass() {
        inMemoryBootstrapModule.deactivate();
    }

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @Test
    @Transactional
    public void testBulk() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        amrSystem.newMeter("myMeter", "myName").create();
        Meter meter = amrSystem.lockMeter("myMeter").get();
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
        meter.store(QualityCodeSystem.MDC, meterReading);
        assertThat(meter.getChannelsContainers().get(0).getZoneId()).isEqualTo(clock.getZone());
        List<? extends BaseReadingRecord> readings = meter.getChannelsContainers().get(0).getReadings(
                Range.openClosed(instant.minusSeconds(15 * 60L), instant.plusSeconds(15 * 60L)),
                meteringService.getReadingType(builder.period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code()).get());
        assertThat(readings).hasSize(2);
        assertThat(readings.get(0).getQuantity(0)).isNull();
        assertThat(readings.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.valueOf(100));
        readings = meter.getChannelsContainers().get(0).getReadings(
                Range.openClosed(instant.minusSeconds(15 * 60L), instant.plusSeconds(15 * 60L)),
                meteringService.getReadingType(registerReadingTypeCode).get());
        assertThat(readings).hasSize(1);
        assertThat(readings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(1200));
        assertThat(meter.getReadingsBefore(instant, meteringService.getReadingType(intervalReadingTypeCode).get(), 10)).isEmpty();
        assertThat(meter.getReadingsOnOrBefore(instant, meteringService.getReadingType(intervalReadingTypeCode).get(), 10)).hasSize(1);
        List<Channel> channels = meter.getChannelsContainers().get(0).getChannels();
        Optional<Channel> channel = Optional.empty();
        for (Channel candidate : channels) {
            if (candidate.getMainReadingType().getMRID().equals(registerReadingTypeCode)) {
                channel = Optional.of(candidate);
            }
        }
        assertThat(channel.isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testDelta() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        Meter meter = amrSystem.newMeter("myMeter", "myName").create();
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
        HashSet<ReadingQualityType> readingQualityTypes = new HashSet<>(Arrays.asList(ProtocolReadingQualities.BATTERY_LOW.getReadingQualityType()));
        IntervalReadingImpl reading = IntervalReadingImpl.of(instant.plusSeconds(15 * 60L), BigDecimal.valueOf(1100), readingQualityTypes);
        reading.addQuality("3.6.1");
        block.addIntervalReading(reading);
        reading = IntervalReadingImpl.of(instant.plusSeconds(30 * 60L), BigDecimal.valueOf(1200), readingQualityTypes);
        reading.addQuality("3.6.2");
        block.addIntervalReading(reading);
        meter.store(QualityCodeSystem.MDM, meterReading);
        Channel channel = meter.getChannelsContainers().stream().flatMap(ma -> ma.getChannels().stream()).findFirst().get();
        List<BaseReadingRecord> readings = channel.getReadings(Range.openClosed(instant.minusSeconds(15 * 60L), instant.plusSeconds(15 * 60L)));
        assertThat(readings).hasSize(2);
        assertThat(readings.get(0).getQuantity(0).getValue()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(readings.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.valueOf(1100));
        assertThat(readings.get(1).getReadingQualities()).hasSize(2);
        assertThat(readings.get(1).getReadingQualities().get(0).getTypeCode()).isEqualTo(ProtocolReadingQualities.BATTERY_LOW.getCimCode());
        Range<Instant> range = Range.closed(instant.minusSeconds(15 * 60L), instant.plusSeconds(30 * 60L));
        assertThat(channel.findReadingQualities().inTimeInterval(range).collect()).hasSize(4);
        channel.removeReadings(QualityCodeSystem.MDC, readings);
        assertThat(channel.getReadings(range)).hasSize(1);
        List<ReadingQualityRecord> readingQualities = channel.findReadingQualities().inTimeInterval(range).sorted().collect();
        assertThat(readingQualities).hasSize(4);
        assertThat(readingQualities.get(0).getType()).isEqualTo(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.REJECTED));
        assertThat(readingQualities.get(1).getType()).isEqualTo(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.REJECTED));
    }

    @Test
    @Transactional
    public void testIdempotency() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        Meter meter = amrSystem.newMeter("myMeter", "myName").create();
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

        meter.store(QualityCodeSystem.MDC, meterReading);
        meter.store(QualityCodeSystem.MDC, meterReading);

        List<? extends BaseReadingRecord> readings = meter.getChannelsContainers().get(0).getReadings(
                Range.openClosed(instant.minusSeconds(15 * 60L), instant.plusSeconds(15 * 60L)),
                meteringService.getReadingType(builder.period(TimeAttribute.MINUTE15).accumulate(Accumulation.DELTADELTA).code()).get());
        assertThat(readings).hasSize(2);
        assertThat(readings.get(0).getQuantity(0)).isNull();
        assertThat(readings.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.valueOf(100));
        readings = meter.getChannelsContainers().get(0).getReadings(
                Range.openClosed(instant.minusSeconds(15 * 60L), instant.plusSeconds(15 * 60L)),
                meteringService.getReadingType(registerReadingTypeCode).get());
        assertThat(readings).hasSize(1);
        assertThat(readings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(1200));
        assertThat(readings.get(0).getQuantities().get(0).getValue()).isEqualTo(BigDecimal.valueOf(1200));
        assertThat(((Reading) readings.get(0)).getText()).isNull();
    }

    @Test
    @Transactional
    public void testAddRegularReading() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        Meter meter = amrSystem.newMeter("myMeter", "myName").create();
        Instant instant = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        String intervalReadingTypeCode = "32.12.2.4.1.9.58.0.0.0.0.0.0.0.0.0.0.0";
        int endDeviceEventLogbookId = 95176;
        String endDeviceEventTypeCode = "1.2.3.4";
        String endDeviceEventSeverity = "Severity";
        String endDeviceEventReason = "The Reason";
        String statusValue = "A value";
        String statusReason = "A reason";
        String statusRemark = "A remark";
        Status endDeviceEventStatus = Status.builder()
                .value(statusValue)
                .reason(statusReason)
                .remark(statusRemark)
                .at(instant)
                .build();
        String endDeviceEventIssuerId = "The Issuer";
        String endDeviceEventIssuerTrackingId = "The Issuer tracking id";
        String endDeviceEventName = "The name";
        String endDeviceEventDescription = "The extended description";
        String endDeviceEventAliasName = "The alias name";
        ((ServerMeteringService) meteringService).createEndDeviceEventType(endDeviceEventTypeCode);
        Reading reading = ReadingImpl.of(intervalReadingTypeCode, BigDecimal.valueOf(1200), instant);
        MeterReadingImpl meterReading = MeterReadingImpl.of(reading);
        EndDeviceEvent endDeviceEvent = EndDeviceEventImpl.of(endDeviceEventTypeCode, instant);
        ((EndDeviceEventImpl) endDeviceEvent).setLogBookId(endDeviceEventLogbookId);
        ((EndDeviceEventImpl) endDeviceEvent).setReason(endDeviceEventReason);
        ((EndDeviceEventImpl) endDeviceEvent).setSeverity(endDeviceEventSeverity);
        ((EndDeviceEventImpl) endDeviceEvent).setStatus(endDeviceEventStatus);
        ((EndDeviceEventImpl) endDeviceEvent).setIssuerId(endDeviceEventIssuerId);
        ((EndDeviceEventImpl) endDeviceEvent).setIssuerTrackingId(endDeviceEventIssuerTrackingId);
        ((EndDeviceEventImpl) endDeviceEvent).setName(endDeviceEventName);
        ((EndDeviceEventImpl) endDeviceEvent).setDescription(endDeviceEventDescription);
        ((EndDeviceEventImpl) endDeviceEvent).setAliasName(endDeviceEventAliasName);
        meterReading.addEndDeviceEvent(endDeviceEvent);
        meter.store(QualityCodeSystem.MDC, meterReading);
        List<? extends BaseReadingRecord> readings = meter.getReadings(Range.all(), meteringService.getReadingType(intervalReadingTypeCode).get());
        assertThat(readings).isNotEmpty();
        Channel channel = meter.getChannelsContainers().get(0).getChannels().get(0);
        List<Reading> changes = new ArrayList<>();
        changes.add(ReadingImpl.of(intervalReadingTypeCode, BigDecimal.valueOf(1300), instant));
        changes.add(ReadingImpl.of(intervalReadingTypeCode, BigDecimal.valueOf(1400), instant.plusSeconds(3600)));
        channel.editReadings(QualityCodeSystem.MDM, changes);
        readings = meter.getReadings(Range.atLeast(Instant.EPOCH), meteringService.getReadingType(intervalReadingTypeCode).get());
        assertThat(readings).isNotEmpty();
        assertThat(readings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(1300));
        assertThat(readings.get(0).edited()).isTrue();
        assertThat(readings.get(0).wasAdded()).isFalse();
        assertThat(readings.get(1).edited()).isTrue();
        assertThat(readings.get(1).wasAdded()).isTrue();
        List<EndDeviceEventRecord> endDeviceEvents = meter.getDeviceEvents(Range.all());
        assertThat(endDeviceEvents).isNotEmpty();
        assertThat(endDeviceEvents.get(0).getLogBookId()).isEqualTo(endDeviceEventLogbookId);
        assertThat(endDeviceEvents.get(0).getReason()).isEqualTo(endDeviceEventReason);
        assertThat(endDeviceEvents.get(0).getSeverity()).isEqualTo(endDeviceEventSeverity);
        assertThat(endDeviceEvents.get(0).getStatus().getValue()).isEqualTo(statusValue);
        assertThat(endDeviceEvents.get(0).getStatus().getReason()).isEqualTo(statusReason);
        assertThat(endDeviceEvents.get(0).getStatus().getRemark()).isEqualTo(statusRemark);
        assertThat(endDeviceEvents.get(0).getStatus().getDateTime()).isEqualTo(instant);
        assertThat(endDeviceEvents.get(0).getIssuerID()).isEqualTo(endDeviceEventIssuerId);
        assertThat(endDeviceEvents.get(0).getIssuerTrackingID()).isEqualTo(endDeviceEventIssuerTrackingId);
        assertThat(endDeviceEvents.get(0).getName()).isEqualTo(endDeviceEventName);
        assertThat(endDeviceEvents.get(0).getDescription()).isEqualTo(endDeviceEventDescription);
        assertThat(endDeviceEvents.get(0).getAliasName()).isEqualTo(endDeviceEventAliasName);
    }

    @Test
    @Transactional
    public void testAddTextRegister() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        Meter meter = amrSystem.newMeter("myMeter", "myName").create();
        meter.update();
        String readingTypeCode = "0.12.0.0.1.9.58.0.0.0.0.0.0.0.0.0.0.0";
        Instant instant = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Reading reading = ReadingImpl.of(readingTypeCode, "Sample text", instant);
        meter.store(QualityCodeSystem.MDC, MeterReadingImpl.of(reading));
        List<? extends BaseReadingRecord> readings = meter.getChannelsContainers().get(0).getReadings(
                Range.openClosed(instant.minusSeconds(15 * 60L), instant.plusSeconds(15 * 60L)),
                meteringService.getReadingType(readingTypeCode).get());
        assertThat(((ReadingRecord) readings.get(0)).getText()).isEqualTo("Sample text");
        assertThat(readings.get(0).getValue()).isNull();
    }

    @Test
    @Transactional
    public void testBulkUpdate() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        Meter meter = amrSystem.newMeter("myMeter", "myName").create();
        meter.update();
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
        builder.period(TimeAttribute.NOTAPPLICABLE).code();
        meter.store(QualityCodeSystem.MDC, meterReading);

        Channel channel = meter.getChannelsContainers().get(0).getChannels().get(0);
        IntervalReading reading = IntervalReadingImpl.of(instant.plusSeconds(15 * 60L), BigDecimal.valueOf(50));
        channel.editReadings(QualityCodeSystem.MDM, ImmutableList.of(reading));

        List<? extends BaseReadingRecord> readings = channel.getReadings(
                Range.openClosed(instant.minusSeconds(15 * 60L), instant.plusSeconds(15 * 60L)));
        assertThat(readings).hasSize(2);
        assertThat(readings.get(0).getQuantity(0)).isNull();
        assertThat(readings.get(1).getQuantity(0).getValue()).isEqualTo(BigDecimal.valueOf(50));
        assertThat(readings.get(1).getQuantity(1).getValue()).isEqualTo(BigDecimal.valueOf(1100));
        assertThat(readings.get(1).getProcessStatus().get(ProcessStatus.Flag.EDITED)).isTrue();
    }
}
