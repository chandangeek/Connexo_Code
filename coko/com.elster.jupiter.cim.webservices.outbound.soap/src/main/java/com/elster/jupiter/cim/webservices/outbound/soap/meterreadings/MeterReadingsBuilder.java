/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.TranslationKeys;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingInfo;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.Ranges;

import ch.iec.tc57._2011.meterreadings.DateTimeInterval;
import ch.iec.tc57._2011.meterreadings.IntervalBlock;
import ch.iec.tc57._2011.meterreadings.IntervalReading;
import ch.iec.tc57._2011.meterreadings.MeterReading;
import ch.iec.tc57._2011.meterreadings.MeterReadings;
import ch.iec.tc57._2011.meterreadings.Name;
import ch.iec.tc57._2011.meterreadings.RationalNumber;
import ch.iec.tc57._2011.meterreadings.Reading;
import ch.iec.tc57._2011.meterreadings.ReadingInterharmonic;
import ch.iec.tc57._2011.meterreadings.ReadingQuality;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class MeterReadingsBuilder {

    private Set<ReadingType> referencedReadingTypes;
    private Set<ReadingQualityType> referencedReadingQualityTypes;

    private static ch.iec.tc57._2011.meterreadings.ReadingType createReadingType(ReadingType readingType) {
        ch.iec.tc57._2011.meterreadings.ReadingType info = new ch.iec.tc57._2011.meterreadings.ReadingType();
        info.setMRID(readingType.getMRID());
        info.getNames().add(createName(readingType.getFullAliasName()));
        info.setAccumulation(readingType.getAccumulation().getDescription());
        info.setAggregate(readingType.getAggregate().getDescription());
        info.setArgument(createRationalNumber(readingType.getArgument()));
        info.setCommodity(readingType.getCommodity().getDescription());
        info.setConsumptionTier(BigInteger.valueOf(readingType.getConsumptionTier()));
        info.setCpp(BigInteger.valueOf(readingType.getCpp()));
        info.setCurrency(readingType.getCurrency().getCurrencyCode());
        info.setFlowDirection(readingType.getFlowDirection().getDescription());
        info.setInterharmonic(createReadingInterharmonic(readingType.getInterharmonic()));
        info.setMacroPeriod(readingType.getMacroPeriod().getDescription());
        info.setMeasurementKind(readingType.getMeasurementKind().getDescription());
        info.setMeasuringPeriod(readingType.getMeasuringPeriod().getDescription());
        info.setMultiplier(readingType.getMultiplier().toString());
        info.setPhases(readingType.getPhases().getDescription());
        info.setTou(BigInteger.valueOf(readingType.getTou()));
        info.setUnit(readingType.getUnit().getName());
        return info;
    }

    private static Name createName(String value) {
        Name name = new Name();
        name.setName(value);
        return name;
    }

    private static RationalNumber createRationalNumber(com.elster.jupiter.cbo.RationalNumber rational) {
        return Optional.ofNullable(rational)
                .filter(number -> !com.elster.jupiter.cbo.RationalNumber.NOTAPPLICABLE.equals(number))
                .map(number -> {
                    RationalNumber info = new RationalNumber();
                    info.setNumerator(BigInteger.valueOf(rational.getNumerator()));
                    info.setDenominator(BigInteger.valueOf(rational.getDenominator()));
                    return info;
                })
                .orElse(null);
    }

    private static ReadingInterharmonic createReadingInterharmonic(com.elster.jupiter.cbo.RationalNumber rational) {
        return Optional.ofNullable(rational)
                .filter(number -> !com.elster.jupiter.cbo.RationalNumber.NOTAPPLICABLE.equals(number))
                .map(number -> {
                    ReadingInterharmonic info = new ReadingInterharmonic();
                    info.setNumerator(BigInteger.valueOf(rational.getNumerator()));
                    info.setDenominator(BigInteger.valueOf(rational.getDenominator()));
                    return info;
                })
                .orElse(null);
    }

    private static DateTimeInterval createDateTimeInterval(Range<Instant> interval) {
        DateTimeInterval dateTimeInterval = new DateTimeInterval();
        Ranges.lowerBound(interval).ifPresent(dateTimeInterval::setStart);
        Ranges.upperBound(interval).ifPresent(dateTimeInterval::setEnd);
        return dateTimeInterval;
    }

    private static ch.iec.tc57._2011.meterreadings.ReadingQualityType createReadingQualityType(ReadingQualityType readingQualityType) {
        ch.iec.tc57._2011.meterreadings.ReadingQualityType info = new ch.iec.tc57._2011.meterreadings.ReadingQualityType();
        info.setMRID(readingQualityType.getCode());
        readingQualityType.system()
                .map(QualityCodeSystem::getTranslationKey)
                .map(TranslationKeys::getDefaultFormat)
                .ifPresent(info::setSystemId);
        readingQualityType.category()
                .map(QualityCodeCategory::getTranslationKey)
                .map(TranslationKeys::getDefaultFormat)
                .ifPresent(info::setCategory);
        readingQualityType.qualityIndex()
                .map(QualityCodeIndex::getTranslationKey)
                .map(TranslationKey::getDefaultFormat)
                .ifPresent(info::setSubCategory);
        return info;
    }

    MeterReadings build(List<ReadingInfo> readingInfos) {
        MeterReadings meterReadings = new MeterReadings();
        List<MeterReading> meterReadingsList = meterReadings.getMeterReading();
        List<ch.iec.tc57._2011.meterreadings.ReadingType> readingTypeList = meterReadings.getReadingType();
        List<ch.iec.tc57._2011.meterreadings.ReadingQualityType> readingQualityTypeList = meterReadings.getReadingQualityType();
        referencedReadingTypes = new HashSet<>();
        referencedReadingQualityTypes = new HashSet<>();
        Map<ReadingType, List<BaseReading>> readingsByReadingTypes = new HashMap<>();

        if (! readingInfos.isEmpty()) {
            Map<Pair<Optional<Meter>, Optional<UsagePoint>>, List<ReadingInfo>> readingsMap =
                    readingInfos.stream().collect(Collectors.groupingBy(rInfo -> Pair.of(rInfo.getMeter(), rInfo.getUsagePoint())));

            for (Map.Entry<Pair<Optional<Meter>, Optional<UsagePoint>>, List<ReadingInfo>> entry : readingsMap.entrySet()) {
                Optional<Meter> meter = entry.getKey().getFirst();
                Optional<UsagePoint> usagePoint = entry.getKey().getLast();
                for (ReadingInfo readingInfo : entry.getValue()) {
                    BaseReading reading = readingInfo.getReading();
                    List<BaseReading> readings = Optional.ofNullable(readingsByReadingTypes.get(readingInfo.getReadingType())).orElse(new ArrayList<>());
                    readings.add(reading);
                    // sort readings by timestamp
                    readings.sort(Comparator.comparing(BaseReading::getTimeStamp));
                    readingsByReadingTypes.put(readingInfo.getReadingType(), readings);
                }
                Optional<MeterReading> meterReading = wrapInMeterReading(readingsByReadingTypes, usagePoint, meter);
                if (meterReading.isPresent()) {
                    meterReadingsList.add(meterReading.get());
                }
            }
        }

        // filled in scope of wrapInMeterReading
        referencedReadingTypes.stream()
                .map(MeterReadingsBuilder::createReadingType)
                .forEach(readingTypeList::add);
        // filled in scope of wrapInMeterReading
        referencedReadingQualityTypes.stream()
                .map(MeterReadingsBuilder::createReadingQualityType)
                .forEach(readingQualityTypeList::add);

        return meterReadings;
    }

    private Optional<MeterReading> wrapInMeterReading(Map<ReadingType, List<BaseReading>> readingsByReadingTypes, Optional<UsagePoint> usagePoint, Optional<Meter> meter) {
        MeterReading meterReading = new MeterReading();
        List<IntervalBlock> intervalBlocks = meterReading.getIntervalBlocks();
        List<Reading> registerReadings = meterReading.getReadings();
        readingsByReadingTypes.forEach((readingType, readings) -> {
            if (readingType.isRegular()) {
                if (!readings.isEmpty()) {
                    intervalBlocks.add(createIntervalBlock(readingType, readings));
                }
            } else {
                registerReadings.addAll(readings.stream()
                        .map(reading -> createReading(readingType, reading))
                        .collect(Collectors.toList()));
            }
        });
        if (intervalBlocks.isEmpty() && registerReadings.isEmpty()) {
            return Optional.empty();
        }
        if (meter.isPresent()) {
            meterReading.setMeter(createMeter(meter.get()));
        }
        if (usagePoint.isPresent()) {
            meterReading.setUsagePoint(createUsagePoint(usagePoint.get()));
        }
        return Optional.of(meterReading);
    }

    private IntervalBlock createIntervalBlock(ReadingType readingType, List<BaseReading> readings) {
        ch.iec.tc57._2011.meterreadings.IntervalBlock.ReadingType intervalBlockReadingType = new IntervalBlock.ReadingType();
        reference(readingType, intervalBlockReadingType::setRef);

        IntervalBlock intervalBlock = new IntervalBlock();
        intervalBlock.setReadingType(intervalBlockReadingType);
        intervalBlock.getIntervalReadings().addAll(readings.stream().map(this::createIntervalReading).collect(Collectors.toList()));

        return intervalBlock;
    }

    private IntervalReading createIntervalReading(BaseReading reading) {
        IntervalReading info = new IntervalReading();
        info.setTimeStamp(reading.getTimeStamp());
        Optional.ofNullable(reading.getValue())
                .map(BigDecimal::toPlainString)
                .ifPresent(info::setValue);
        info.setReportedDateTime(reading.getReportedDateTime());

        List<ReadingQuality> readingQualities = info.getReadingQualities();
        reading.getReadingQualities().stream()
                .map(this::createReadingQuality)
                .forEach(readingQualities::add);

        return info;
    }

    private Reading createReading(ReadingType readingType, BaseReading reading) {
        Reading.ReadingType readingReadingType = new Reading.ReadingType();
        reference(readingType, readingReadingType::setRef);

        Reading info = new Reading();
        info.setReadingType(readingReadingType);
        info.setTimeStamp(reading.getTimeStamp());

        Optional.ofNullable(reading.getValue())
                .map(BigDecimal::toPlainString)
                .ifPresent(info::setValue);
        info.setReportedDateTime(reading.getReportedDateTime());
        reading.getTimePeriod()
                .map(MeterReadingsBuilder::createDateTimeInterval)
                .ifPresent(info::setTimePeriod);


        List<ReadingQuality> readingQualities = info.getReadingQualities();
        reading.getReadingQualities().stream()
                .map(this::createReadingQuality)
                .forEach(readingQualities::add);

        return info;
    }

    private void reference(ReadingType readingType, Consumer<String> referenceSetter) {
        referenceSetter.accept(readingType.getMRID());
        referencedReadingTypes.add(readingType);
    }

    private ch.iec.tc57._2011.meterreadings.ReadingQuality createReadingQuality(com.elster.jupiter.metering.readings.ReadingQuality qualityRecord) {
        ch.iec.tc57._2011.meterreadings.ReadingQuality info = new ch.iec.tc57._2011.meterreadings.ReadingQuality();
        if (qualityRecord instanceof ReadingQualityRecord) {
            info.setTimeStamp(((ReadingQualityRecord) qualityRecord).getTimestamp());
        }
        info.setReadingQualityType(reference(qualityRecord.getType()));
        info.setComment(qualityRecord.getComment());
        return info;
    }

    private ch.iec.tc57._2011.meterreadings.ReadingQuality.ReadingQualityType reference(ReadingQualityType readingQualityType) {
        ch.iec.tc57._2011.meterreadings.ReadingQuality.ReadingQualityType reference
                = new ch.iec.tc57._2011.meterreadings.ReadingQuality.ReadingQualityType();
        reference.setRef(readingQualityType.getCode());
        referencedReadingQualityTypes.add(readingQualityType);
        return reference;
    }

    private ch.iec.tc57._2011.meterreadings.Meter createMeter(Meter meter) {
        ch.iec.tc57._2011.meterreadings.Meter meterReadingMeter = new ch.iec.tc57._2011.meterreadings.Meter();
        meterReadingMeter.setMRID(meter.getMRID());
        meterReadingMeter.getNames().add(createName(meter.getName()));
        return meterReadingMeter;
    }

    private ch.iec.tc57._2011.meterreadings.UsagePoint createUsagePoint(UsagePoint usagePoint) {
        ch.iec.tc57._2011.meterreadings.UsagePoint meterReadingUsagePoint = new ch.iec.tc57._2011.meterreadings.UsagePoint();
        meterReadingUsagePoint.setMRID(usagePoint.getMRID());
        meterReadingUsagePoint.getNames().add(createName(usagePoint.getName()));
        return meterReadingUsagePoint;
    }
}