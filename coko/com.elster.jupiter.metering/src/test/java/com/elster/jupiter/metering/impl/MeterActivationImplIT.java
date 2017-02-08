/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.ami.EndDeviceCapabilities;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeDeliverableBuilder;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.transaction.TransactionContext;

import com.google.common.collect.Range;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterActivationImplIT {

    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = new MeteringInMemoryBootstrapModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    @Rule
    public ExpectedConstraintViolationRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();
    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testPersistence() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem system = meteringService.findAmrSystem(1).get();
        Meter meter = system.newMeter("testPersistence", "myName").create();
        MeterActivation meterActivation = meter.activate(ZonedDateTime.of(2012, 12, 19, 14, 15, 54, 0, ZoneId.systemDefault())
                .toInstant());
        ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        Channel channel = meterActivation.getChannelsContainer().createChannel(readingType);
        MeterActivation loaded = meteringService.findMeterActivation(meterActivation.getId()).get();
        assertThat(loaded.getChannelsContainer().getChannels()).hasSize(1).contains(channel);
    }

    @Test
    @Transactional
    public void testPersistenceWithChannelCreationThroughMeterActivation() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        AmrSystem system = meteringService.findAmrSystem(1).get();
        HeadEndInterface heMock = mock(HeadEndInterface.class);
        when(heMock.getAmrSystem()).thenReturn(system.getName());
        when(heMock.getCapabilities(any(Meter.class))).then(invocationOnMock -> new EndDeviceCapabilities(Collections.singletonList(readingType), Collections.emptyList()));
        try {
            meteringService.addHeadEndInterface(heMock);

            Meter meter = system.newMeter("testPersistence", "myName").create();
            MeterActivation meterActivation = meter.activate(ZonedDateTime.of(2012, 12, 19, 14, 15, 54, 0, ZoneId.systemDefault()).toInstant());

            MeterActivation loaded = meteringService.findMeterActivation(meterActivation.getId()).get();
            assertThat(loaded.getChannelsContainer().getChannels()).hasSize(1);
        } finally {
            meteringService.removeHeadEndInterface(heMock);
        }
    }

    @Test
    @Transactional
    public void testPersistenceWithChannelCreationThroughUpActivation() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        AmrSystem system = meteringService.findAmrSystem(1).get();
        HeadEndInterface heMock = mock(HeadEndInterface.class);
        when(heMock.getAmrSystem()).thenReturn(system.getName());
        when(heMock.getCapabilities(any(Meter.class))).then(invocationOnMock -> new EndDeviceCapabilities(Collections.singletonList(readingType), Collections.emptyList()));
        try {
            meteringService.addHeadEndInterface(heMock);

            Meter meter = system.newMeter("testPersistence", "myName").create();

            UsagePoint usagePoint = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                    .orElseThrow(IllegalArgumentException::new)
                    .newUsagePoint("random name", ZonedDateTime.of(2012, 12, 18, 14, 15, 54, 0, ZoneId.systemDefault()).toInstant()).create();

            MeterActivation meterActivation = usagePoint.activate(meter, inMemoryBootstrapModule.getMetrologyConfigurationService()
                    .findDefaultMeterRole(DefaultMeterRole.DEFAULT), ZonedDateTime.of(2012, 12, 19, 14, 15, 54, 0, ZoneId.systemDefault()).toInstant());

            MeterActivation loaded = meteringService.findMeterActivation(meterActivation.getId()).get();
            assertThat(loaded.getChannelsContainer().getChannels()).hasSize(1);
        } finally {
            meteringService.removeHeadEndInterface(heMock);
        }
    }


    @Test
    public void testCOPL854() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        long meterId, usagePointId;
        Instant start3 = Instant.now();
        Instant start1 = start3.minusSeconds(86400);
        Instant start2 = start3.minusSeconds(43200);
        try (TransactionContext ctx = inMemoryBootstrapModule.getTransactionService().getContext()) {
            AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
            Meter meter = amrSystem.newMeter("testCOPL854", "myName").create();
            meterId = meter.getId();
            UsagePoint up = meteringService.getServiceCategory(ServiceKind.ELECTRICITY)
                    .orElseThrow(IllegalArgumentException::new).newUsagePoint("abcd", Instant.EPOCH)
                    .create();
            usagePointId = up.getId();

            meter.activate(up, start1);

            ctx.commit();
        }
        assertMeterActivations(meterId, usagePointId, start1);

        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            Meter meter = meteringService.findMeterById(meterId).get();
            meter.getUsagePoint(Instant.now()).get().activate(meter, start2);
            context.commit();
        }
        assertMeterActivations(meterId, usagePointId, start1, start2);

        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            UsagePoint up = meteringService.findUsagePointById(usagePointId).get();
            Meter meter = meteringService.findMeterById(meterId).get();

            up.activate(meter, start3);
            context.commit();
        }
        assertMeterActivations(meterId, usagePointId, start1, start2, start3);
    }

    private void assertMeterActivations(long meterId, long usagePointId, Instant... startTimes) {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        List<? extends MeterActivation> meterActivations = meteringService.findMeterById(meterId).get().getMeterActivations();
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
    @Transactional
    public void testAdvanceWithReadings() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem system = meteringService.findAmrSystem(1).get();
        Meter meter = system.newMeter("testAdvanceWithReadings", "myName").create();
        ZonedDateTime startTime = ZonedDateTime.of(2012, 12, 19, 14, 15, 54, 0, ZoneId.systemDefault());
        ZonedDateTime originalCutOff = ZonedDateTime.of(2012, 12, 25, 0, 0, 0, 0, ZoneId.systemDefault());
        ZonedDateTime newCutOff = ZonedDateTime.of(2012, 12, 20, 0, 0, 0, 0, ZoneId.systemDefault());
        MeterActivation meterActivation = meter.activate(startTime.toInstant());
        meterActivation.endAt(originalCutOff.toInstant());
        ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        Channel channel = meterActivation.getChannelsContainer().createChannel(readingType);
        MeterActivation currentActivation = meter.activate(originalCutOff.toInstant());
        Channel currentChannel = currentActivation.getChannelsContainer().createChannel(readingType);
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.minusMinutes(15).toInstant(), BigDecimal.valueOf(4025, 2)));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.toInstant(), BigDecimal.valueOf(4175, 2)));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.plusMinutes(15).toInstant(), BigDecimal.valueOf(4225, 2)));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(originalCutOff.toInstant(), BigDecimal.valueOf(4725, 2)));
        intervalBlock.addIntervalReading(IntervalReadingImpl.of(originalCutOff.plusMinutes(15).toInstant(), BigDecimal.valueOf(4825, 2)));
        meterReading.addIntervalBlock(intervalBlock);
        meter.store(QualityCodeSystem.MDC, meterReading);

        currentActivation.advanceStartDate(newCutOff.toInstant());

        assertThat(meter.getMeterActivations()).hasSize(2);
        MeterActivation first = meter.getMeterActivations().get(0);
        MeterActivation second = meter.getMeterActivations().get(1);
        assertThat(first.getRange()).isEqualTo(Range.closedOpen(startTime.toInstant(), newCutOff.toInstant()));
        assertThat(second.getRange()).isEqualTo(Range.atLeast(newCutOff.toInstant()));

        List<? extends BaseReadingRecord> firstReadings = first.getChannelsContainer().getReadings(Range.all(), readingType);
        assertThat(firstReadings).hasSize(2);
        assertThat(firstReadings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(4025, 2));
        assertThat(firstReadings.get(0).getTimeStamp()).isEqualTo(newCutOff.minusMinutes(15).toInstant());
        assertThat(firstReadings.get(1).getValue()).isEqualTo(BigDecimal.valueOf(4175, 2));
        assertThat(firstReadings.get(1).getTimeStamp()).isEqualTo(newCutOff.toInstant());
        List<? extends BaseReadingRecord> secondReadings = second.getChannelsContainer().getReadings(Range.all(), readingType);
        assertThat(secondReadings).hasSize(3);
        assertThat(secondReadings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(4225, 2));
        assertThat(secondReadings.get(0).getTimeStamp()).isEqualTo(newCutOff.plusMinutes(15).toInstant());
        assertThat(secondReadings.get(1).getValue()).isEqualTo(BigDecimal.valueOf(4725, 2));
        assertThat(secondReadings.get(1).getTimeStamp()).isEqualTo(originalCutOff.toInstant());
        assertThat(secondReadings.get(2).getValue()).isEqualTo(BigDecimal.valueOf(4825, 2));
        assertThat(secondReadings.get(2).getTimeStamp()).isEqualTo(originalCutOff.plusMinutes(15).toInstant());

        List<? extends BaseReadingRecord> firstChannelReadings = first.getChannelsContainer().getChannels().get(0).getReadings(readingType, Range.all());
        assertThat(firstChannelReadings).hasSize(2);
        assertThat(firstChannelReadings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(4025, 2));
        assertThat(firstChannelReadings.get(0).getTimeStamp()).isEqualTo(newCutOff.minusMinutes(15).toInstant());
        assertThat(firstChannelReadings.get(1).getValue()).isEqualTo(BigDecimal.valueOf(4175, 2));
        assertThat(firstChannelReadings.get(1).getTimeStamp()).isEqualTo(newCutOff.toInstant());
        List<? extends BaseReadingRecord> secondChannelReadings = second.getChannelsContainer().getChannels().get(0).getReadings(readingType, Range.all());
        assertThat(secondChannelReadings).hasSize(3);
        assertThat(secondChannelReadings.get(0).getValue()).isEqualTo(BigDecimal.valueOf(4225, 2));
        assertThat(secondChannelReadings.get(0).getTimeStamp()).isEqualTo(newCutOff.plusMinutes(15).toInstant());
        assertThat(secondChannelReadings.get(1).getValue()).isEqualTo(BigDecimal.valueOf(4725, 2));
        assertThat(secondChannelReadings.get(1).getTimeStamp()).isEqualTo(originalCutOff.toInstant());
        assertThat(secondChannelReadings.get(2).getValue()).isEqualTo(BigDecimal.valueOf(4825, 2));
        assertThat(secondChannelReadings.get(2).getTimeStamp()).isEqualTo(originalCutOff.plusMinutes(15).toInstant());
    }

    @Test
    @Transactional
    public void testAdvanceWithoutData() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem system = meteringService.findAmrSystem(1).get();
        Meter meter = system.newMeter("testAdvanceWithoutData", "myName").create();
        ZonedDateTime startTime = ZonedDateTime.of(2012, 12, 19, 14, 15, 54, 0, ZoneId.systemDefault());
        ZonedDateTime originalCutOff = ZonedDateTime.of(2012, 12, 25, 0, 0, 0, 0, ZoneId.systemDefault());
        ZonedDateTime newCutOff = ZonedDateTime.of(2012, 12, 20, 0, 0, 0, 0, ZoneId.systemDefault());
        MeterActivation meterActivation = meter.activate(startTime.toInstant());
        meterActivation.endAt(originalCutOff.toInstant());
        ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        Channel channel = meterActivation.getChannelsContainer().createChannel(readingType);
        MeterActivation currentActivation = meter.activate(originalCutOff.toInstant());
        Channel currentChannel = currentActivation.getChannelsContainer().createChannel(readingType);

        currentActivation.advanceStartDate(newCutOff.toInstant());

        assertThat(meter.getMeterActivations()).hasSize(2);

        MeterActivation first = meter.getMeterActivations().get(0);
        MeterActivation second = meter.getMeterActivations().get(1);
        assertThat(first.getRange()).isEqualTo(Range.closedOpen(startTime.toInstant(), newCutOff.toInstant()));
        assertThat(second.getRange()).isEqualTo(Range.atLeast(newCutOff.toInstant()));
    }

    @Test
    public void testAdvanceWithReadingsAndQualities() {
        ZonedDateTime startTime = ZonedDateTime.of(2012, 12, 19, 14, 15, 54, 0, ZoneId.systemDefault());
        ZonedDateTime originalCutOff = ZonedDateTime.of(2012, 12, 25, 0, 0, 0, 0, ZoneId.systemDefault());
        ZonedDateTime newCutOff = ZonedDateTime.of(2012, 12, 20, 0, 0, 0, 0, ZoneId.systemDefault());
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        Meter meter;
        MeterActivation currentActivation;
        ReadingType readingType;
        MeterActivation meterActivation;
        try (TransactionContext ctx = inMemoryBootstrapModule.getTransactionService().getContext()) {
            AmrSystem system = meteringService.findAmrSystem(1).get();
            meter = system.newMeter("testAdvanceWithReadingsAndQualities", "myName").create();
            meterActivation = meter.activate(startTime.toInstant());
            meterActivation.endAt(originalCutOff.toInstant());
            readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
            meterActivation.getChannelsContainer().createChannel(readingType);
            currentActivation = meter.activate(originalCutOff.toInstant());
            currentActivation.getChannelsContainer().createChannel(readingType);
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            IntervalBlockImpl intervalBlock = IntervalBlockImpl.of("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.minusMinutes(15).toInstant(), BigDecimal.valueOf(4025, 2)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.toInstant(), BigDecimal.valueOf(4175, 2)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(newCutOff.plusMinutes(15).toInstant(), BigDecimal.valueOf(4225, 2)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(originalCutOff.toInstant(), BigDecimal.valueOf(4725, 2)));
            intervalBlock.addIntervalReading(IntervalReadingImpl.of(originalCutOff.plusMinutes(15).toInstant(), BigDecimal.valueOf(4825, 2)));
            meterReading.addIntervalBlock(intervalBlock);
            meter.store(QualityCodeSystem.MDC, meterReading);
            meterActivation.getChannelsContainer()
                    .getChannels()
                    .get(0)
                    .createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), readingType, newCutOff.minusMinutes(15).toInstant());
            meterActivation.getChannelsContainer()
                    .getChannels()
                    .get(0)
                    .createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), readingType, newCutOff.toInstant());
            ReadingQualityRecord readingQuality = meterActivation.getChannelsContainer().getChannels()
                    .get(0)
                    .createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), readingType, newCutOff
                            .plusMinutes(15)
                            .toInstant());
            readingQuality.makePast();
            meterActivation.getChannelsContainer()
                    .getChannels()
                    .get(0)
                    .createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), readingType, originalCutOff.toInstant());
            currentActivation.getChannelsContainer().getChannels()
                    .get(0)
                    .createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), readingType, originalCutOff
                            .plusMinutes(15)
                            .toInstant());
            ctx.commit();
        }

        try (TransactionContext ctx = inMemoryBootstrapModule.getTransactionService().getContext()) {
            currentActivation.advanceStartDate(newCutOff.toInstant());
            ctx.commit();
        }

        assertThat(meter.getMeterActivations()).hasSize(2);
        ChannelsContainer first = meter.getChannelsContainers().get(0);
        ChannelsContainer second = meter.getChannelsContainers().get(1);
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

        List<ReadingQualityRecord> firstQualities = first.getChannels().get(0).findReadingQualities().sorted().collect();
        assertThat(firstQualities).hasSize(2);
        assertThat(firstQualities.get(0).getReadingTimestamp()).isEqualTo(newCutOff.minusMinutes(15).toInstant());
        assertThat(firstQualities.get(1).getReadingTimestamp()).isEqualTo(newCutOff.toInstant());
        List<ReadingQualityRecord> secondQualities = second.getChannels().get(0).findReadingQualities().sorted().collect();
        assertThat(secondQualities).hasSize(3);
        assertThat(secondQualities.get(0).getReadingTimestamp()).isEqualTo(newCutOff.plusMinutes(15).toInstant());
        assertThat(secondQualities.get(0).isActual()).isFalse();
        assertThat(secondQualities.get(1).getReadingTimestamp()).isEqualTo(originalCutOff.toInstant());
        assertThat(secondQualities.get(2).getReadingTimestamp()).isEqualTo(originalCutOff.plusMinutes(15).toInstant());
    }

    @Test
    @Transactional
    public void testCanCreateMeterActivationWithMeterRole() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem system = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter meter = system.newMeter("testCanCreateMeterActivationWithMeterRole_Meter", "myName").create();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory
                .newUsagePoint("testCanCreateMeterActivationWithMeterRole_UP", inMemoryBootstrapModule.getClock().instant())
                .create();
        MeterRole meterRole = inMemoryBootstrapModule.getMetrologyConfigurationService()
                .findDefaultMeterRole(DefaultMeterRole.DEFAULT);
        MeterActivation meterActivation = meter.activate(usagePoint, meterRole, inMemoryBootstrapModule.getClock().instant());

        Optional<MeterRole> meterRoleRef = meterActivation.getMeterRole();
        assertThat(meterRoleRef.isPresent()).isTrue();
        assertThat(meterRoleRef.get()).isEqualTo(meterRole);
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "meter.role.main", messageId = "{the.same.meter.activated.twice.on.usage.point}", strict = true)
    public void testMeterCanNotBeAssignedTwiceForTheSameUsagePoint() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem system = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter meter = system.newMeter("meterForActivation", "myName").create();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        Instant now = inMemoryBootstrapModule.getClock().instant();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("usagePointForActivation", now).create();

        usagePoint.linkMeters()
                .activate(meter, inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT))
                .activate(meter, inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.MAIN))
                .complete();
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "meter.role.main", messageId = "{the.same.meter.activated.twice.on.usage.point}", strict = true)
    public void testMeterCanNotBeAssignedTwiceForTheSameUsagePointCase2() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem system = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter meter = system.newMeter("meterForActivation", "myName").create();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        Instant now = inMemoryBootstrapModule.getClock().instant();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("usagePointForActivation", now).create();

        usagePoint.linkMeters()
                .activate(meter, inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT))
                .complete();

        List<MeterActivation> meterActivations = meteringService.findUsagePointById(usagePoint.getId()).get().getMeterActivations(now);
        assertThat(meterActivations).hasSize(1);
        assertThat(meterActivations.get(0).getMeterRole().get())
                .isEqualTo(inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT));

        usagePoint.linkMeters()
                .activate(meter, inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.MAIN))
                .complete();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(property = "meter.role.default", messageId = "MTR7003S This meter does not provide reading types matching a [15-minute] Secondary Delta 0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0 (kWh).", strict = true)
    public void testMeterDoesNotSatisfyMetrologyRequirements() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        ServerMetrologyConfigurationService metrologyConfigurationService = inMemoryBootstrapModule.getMetrologyConfigurationService();
        AmrSystem system = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter meter = system.newMeter("meterForActivation", "myName").create();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        Instant now = inMemoryBootstrapModule.getClock().instant().truncatedTo(ChronoUnit.MINUTES);
        UsagePoint usagePoint = serviceCategory.newUsagePoint("usagePointForActivation", now).create();
        MeterRole meterRole = metrologyConfigurationService.findDefaultMeterRole(DefaultMeterRole.DEFAULT);
        serviceCategory.addMeterRole(meterRole);
        UsagePointMetrologyConfiguration metrologyConfiguration = metrologyConfigurationService
                .newUsagePointMetrologyConfiguration("mConfigForActivation", serviceCategory)
                .create();
        metrologyConfiguration.addMeterRole(meterRole);
        ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        FullySpecifiedReadingTypeRequirement readingTypeRequirement =
                metrologyConfiguration
                        .newReadingTypeRequirement("Requirement", meterRole)
                        .withReadingType(readingType);
        ReadingTypeDeliverableBuilder builder = metrologyConfiguration.newReadingTypeDeliverable("Deliverable", readingType, Formula.Mode.AUTO);
        ReadingTypeDeliverable deliverable = builder.build(builder.requirement(readingTypeRequirement));
        metrologyConfiguration.addMandatoryMetrologyContract(metrologyConfigurationService.findMetrologyPurpose(DefaultMetrologyPurpose.BILLING).get()).addDeliverable(deliverable);
        usagePoint.apply(metrologyConfiguration, now);

        usagePoint.linkMeters()
                .activate(meter, meterRole)
                .complete();
    }

    @Test
    @Transactional
    public void testActivateAlreadyActiveMeter() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem system = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter meter = system.newMeter("meterForActivation", "myName").create();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        Instant now = inMemoryBootstrapModule.getClock().instant();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("usagePointForActivation", now).create();
        meter.activate(now);

        try {
            usagePoint.linkMeters()
                    .activate(meter, inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.MAIN))
                    .complete();
        } catch (ConstraintViolationException ex) {
            assertThat(ex.getConstraintViolations().size()).isEqualTo(1);
            assertThat(ex.getConstraintViolations().iterator().next().getPropertyPath().toString()).isEqualTo("main");
            assertThat(ex.getConstraintViolations().iterator().next().getMessage()).contains("is already active");
        }
    }

    @Test
    @Transactional
    public void testMeterCanBeLinkedToUsagePoint() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem system = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter meter = system.newMeter("meterForActivation", "myName").create();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        Instant now = inMemoryBootstrapModule.getClock().instant();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("usagePointForActivation", now).create();

        usagePoint.linkMeters()
                .activate(meter, inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT))
                .complete();

        Optional<? extends MeterActivation> meterActivation = meteringService.findMeterById(meter.getId()).get().getMeterActivation(now);
        assertThat(meterActivation).isPresent();
        assertThat(meteringService.findUsagePointById(usagePoint.getId()).get().getMeterActivations(now))
                .contains(meterActivation.get());
    }

    @Test
    @Transactional
    public void testTwoMetersCanBeLinkedToUsagePoint() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem system = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter meter1 = system.newMeter("meterForActivation1", "myName").create();
        Meter meter2 = system.newMeter("meterForActivation2", "myName").create();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        Instant now = inMemoryBootstrapModule.getClock().instant();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("usagePointForActivation", now).create();

        usagePoint.linkMeters()
                .activate(meter1, inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT))
                .activate(meter2, inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.MAIN))
                .complete();

        Optional<? extends MeterActivation> meterActivation = meteringService.findMeterById(meter1.getId()).get().getMeterActivation(now);
        assertThat(meterActivation).isPresent();
        assertThat(meteringService.findUsagePointById(usagePoint.getId()).get().getMeterActivations(now))
                .contains(meterActivation.get());

        meterActivation = meteringService.findMeterById(meter2.getId()).get().getMeterActivation(now);
        assertThat(meterActivation).isPresent();
        assertThat(meteringService.findUsagePointById(usagePoint.getId()).get().getMeterActivations(now))
                .contains(meterActivation.get());
    }

    @Test
    @Transactional
    public void testMeterCanBeRemovedFromMeterRoleOnUsagePoint() {
        ServerMeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        AmrSystem system = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter meter = system.newMeter("meterForActivation", "myName").create();
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        Instant now = inMemoryBootstrapModule.getClock().instant();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("usagePointForActivation", now).create();

        usagePoint.linkMeters()
                .activate(meter, inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT))
                .complete();

        assertThat(meteringService.findMeterById(meter.getId()).get().getMeterActivation(now)).isPresent();

        usagePoint.linkMeters()
                .activate(meter, inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.MAIN))
                .clear(inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT))
                .complete();

        List<MeterActivation> meterActivations = meteringService.findUsagePointById(usagePoint.getId()).get().getMeterActivations(now);
        assertThat(meterActivations).hasSize(1);
        assertThat(meterActivations.get(0).getMeterRole().get())
                .isEqualTo(inMemoryBootstrapModule.getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.MAIN));
    }
}
