/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.impl.MeteringInMemoryBootstrapModule;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;

import com.google.common.collect.ImmutableList;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.MetricMultiplier.quantity;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static com.elster.jupiter.util.streams.Predicates.not;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReadingEditRemoveIT {

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
    public void test() {
        MeteringService meteringService = inMemoryBootstrapModule.getMeteringService();
        Meter meter;
        String readingTypeCode;
        Instant existDate = ZonedDateTime.of(2014, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant newDate = ZonedDateTime.of(2014, 2, 2, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();

        //step 1
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        meter = amrSystem.newMeter("myMeter", "myName").create();
        ReadingTypeCodeBuilder builder = ReadingTypeCodeBuilder.of(Commodity.ELECTRICITY_SECONDARY_METERED)
                .accumulate(Accumulation.BULKQUANTITY)
                .flow(FlowDirection.FORWARD)
                .measure(MeasurementKind.ENERGY)
                .in(MetricMultiplier.KILO, ReadingTypeUnit.WATTHOUR);
        readingTypeCode = builder.code();

        //step 2
        ReadingImpl reading = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(1), existDate);
        reading.addQuality("2.5.258");
        reading.addQuality("2.6.1");
        meter.store(QualityCodeSystem.MDC, MeterReadingImpl.of(reading));

        // step 3
        Channel channel = meter.getCurrentMeterActivation().get().getChannelsContainer().getChannels().get(0);
        ReadingType readingType = meteringService.getReadingType(readingTypeCode).get();
        assertQualities(channel, existDate, new ReadingQualityType[]{
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT),
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ZEROUSAGE)
        }, new ReadingQualityType[0]);

        //step 4
        CimChannel cimChannel = channel.getCimChannel(readingType).get();
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED), existDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.EDITGENERIC), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC), existDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ESTIMATEGENERIC), existDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ESTIMATEGENERIC), existDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ACCEPTED), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ACCEPTED), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ACCEPTED), existDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.REJECTED), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.REJECTED), existDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1000), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 2000), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.KNOWNMISSINGREAD), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT), newDate);
        cimChannel.createReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), newDate);

        // step 5
        // test edit & add
        ReadingImpl reading1 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), existDate);
        ReadingImpl reading2 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(2), newDate);
        channel.editReadings(QualityCodeSystem.MDC, ImmutableList.of(reading1, reading2));
        // existDate qualities
        assertQualities(channel, existDate, new ReadingQualityType[]{
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.EDITGENERIC)
        }, new ReadingQualityType[]{
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ZEROUSAGE),
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED),
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.REJECTED)
        });
        // newDate qualities
        assertQualities(channel, newDate, new ReadingQualityType[]{
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED)
        }, new ReadingQualityType[]{
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.REJECTED),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1000),
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 2000),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD),
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.KNOWNMISSINGREAD)
        });
        Optional<BaseReadingRecord> channelReading = channel.getReading(existDate);
        assertThat(channelReading).isPresent();
        assertThat(channelReading.get().getQuantity(readingType)).isEqualTo(quantity(BigDecimal.valueOf(2), KILO, WATTHOUR));
        channelReading = channel.getReading(newDate);
        assertThat(channelReading).isPresent();
        assertThat(channelReading.get().getQuantity(readingType)).isEqualTo(quantity(BigDecimal.valueOf(2), KILO, WATTHOUR));

        // step 6
        // test re-edit
        reading2 = ReadingImpl.of(readingTypeCode, BigDecimal.valueOf(3), newDate);
        channel.editReadings(QualityCodeSystem.MDM, ImmutableList.of(reading2));
        assertQualities(channel, newDate, new ReadingQualityType[]{
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC)
        }, new ReadingQualityType[]{
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ADDED),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.REJECTED),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.VALIDATION, 1000),
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.VALIDATION, 2000),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD),
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.KNOWNMISSINGREAD)
        });
        channelReading = channel.getReading(newDate);
        assertThat(channelReading).isPresent();
        assertThat(channelReading.get().getQuantity(readingType)).isEqualTo(quantity(BigDecimal.valueOf(3), KILO, WATTHOUR));

        // step 7
        // test remove
        BaseReadingRecord baseReadingRecord1 = channel.getReading(existDate).get();
        BaseReadingRecord baseReadingRecord2 = channel.getReading(newDate).get();
        channel.removeReadings(QualityCodeSystem.MDM, ImmutableList.of(baseReadingRecord1));
        assertQualities(channel, existDate, new ReadingQualityType[]{
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.REJECTED)
        }, new ReadingQualityType[0]);
        assertThat(channel.getReading(existDate)).isEmpty();
        channel.removeReadings(QualityCodeSystem.MDC, ImmutableList.of(baseReadingRecord2));
        assertQualities(channel, newDate, new ReadingQualityType[]{
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.REJECTED)
        }, new ReadingQualityType[0]);
        assertThat(channel.getReading(newDate)).isEmpty();
    }

    private static void assertQualities(Channel channel, Instant date, ReadingQualityType[] actual, ReadingQualityType[] nonActual) {
        List<ReadingQualityRecord> qualities = channel.findReadingQualities()
                .atTimestamp(date)
                .collect();
        assertThat(qualities.stream()
                .filter(ReadingQualityRecord::isActual)
                .map(ReadingQualityRecord::getType)
                .collect(Collectors.toList()))
                .containsOnly(actual);
        assertThat(qualities.stream()
                .filter(not(ReadingQualityRecord::isActual))
                .map(ReadingQualityRecord::getType)
                .collect(Collectors.toList()))
                .containsOnly(nonActual);
    }
}
