/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;


import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeComparator;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.util.Ranges;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UsagePointOutputReferenceCopier {
    private final ResourceHelper resourceHelper;
    private final OutputChannelDataInfoFactory outputChannelDataInfoFactory;
    private final MeteringService meteringService;

    private final ReadingTypeComparator readingTypeComparator = ReadingTypeComparator.ignoring(ReadingTypeComparator.Attribute.Multiplier);

    @Inject
    public UsagePointOutputReferenceCopier(MeteringService meteringService,
                                           ResourceHelper resourceHelper,
                                           OutputChannelDataInfoFactory outputChannelDataInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.outputChannelDataInfoFactory = outputChannelDataInfoFactory;
        this.meteringService = meteringService;
    }

    public List<OutputChannelDataInfo> copy(AggregatedChannel sourceChannel, ReferenceChannelDataInfo referenceChannelDataInfo) {
        List<OutputChannelDataInfo> resultReadings = new ArrayList<>();

        UsagePoint usagePoint = resourceHelper.findUsagePointByName(referenceChannelDataInfo.referenceUsagePoint)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, "referenceUsagePoint", referenceChannelDataInfo.referenceUsagePoint));
        MetrologyPurpose purpose = resourceHelper.findMetrologyPurpose(referenceChannelDataInfo.referencePurpose)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, "referencePurpose", referenceChannelDataInfo.referencePurpose));
        ReadingType readingType = meteringService.getReadingType(referenceChannelDataInfo.readingType)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, "readingType", referenceChannelDataInfo.readingType));

        Map<Range<Instant>, Range<Instant>> correctedRanges = getCorrectedTimeStampsForReference(referenceChannelDataInfo.startDate, referenceChannelDataInfo.intervals);
        List<Instant> readingTimeStamps = correctedRanges.values().stream().filter(Range::hasUpperBound).map(Range::upperEndpoint).collect(Collectors.toList());
        if (readingTimeStamps.isEmpty()) {
            return Collections.emptyList();
        }
        Range<Instant> referenceRange = correctedRanges.values().stream().reduce(Range::span).get();

        List<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfigurations = usagePoint.getEffectiveMetrologyConfigurations(referenceRange).stream()
                .filter(emc -> emc.getRange().isConnected(referenceRange) && readingTimeStamps.stream().anyMatch(emc::isEffectiveAt)).collect(Collectors.toList());
        for (EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint : effectiveMetrologyConfigurations) {
            MetrologyContract metrologyContract = resourceHelper.findMetrologyContract(effectiveMetrologyConfigurationOnUsagePoint, purpose)
                    .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.METROLOGYPURPOSE_IS_NOT_FOUND_ON_USAGEPOINT, "referencePurpose", purpose.getName(), usagePoint
                            .getName()));
            if (readingTypeComparator.compare(readingType, sourceChannel.getMainReadingType())!=0) {
                throw new LocalizedFieldValidationException(MessageSeeds.READINGTYPES_DONT_MATCH, "readingType");
            }
            ReadingTypeDeliverable readingTypeDeliverable = metrologyContract.getDeliverables().stream().filter(output -> output.getReadingType().equals(readingType))
                    .findFirst()
                    .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.READINGTYPE_NOT_FOUND_ON_USAGEPOINT, "readingType", referenceChannelDataInfo.readingType));
            AggregatedChannel referenceChannel = effectiveMetrologyConfigurationOnUsagePoint.getAggregatedChannel(metrologyContract, readingTypeDeliverable.getReadingType())
                    .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.READINGTYPE_NOT_FOUND_ON_USAGEPOINT, "readingType", referenceChannelDataInfo.readingType));

            Map<Instant, IntervalReadingRecord> sourceRecords = sourceChannel
                    .getIntervalReadings(Ranges.copy(referenceRange.intersection(effectiveMetrologyConfigurationOnUsagePoint.getRange())).asOpenClosed()).stream()
                    .filter(readingRecord -> correctedRanges.keySet().stream().anyMatch(r -> r.upperEndpoint().equals(readingRecord.getTimeStamp())))
                    .collect(Collectors.toMap(BaseReading::getTimeStamp, Function.identity(), (a, b) -> a));
            Map<Instant, ReadingQualityRecord> referenceReadingQualities = referenceChannel.findReadingQualities()
                    .ofQualityIndex(QualityCodeIndex.SUSPECT)
                    .inTimeInterval(Ranges.copy(referenceRange.intersection(effectiveMetrologyConfigurationOnUsagePoint.getRange())).asOpenClosed()).stream()
                    .filter(ReadingQualityRecord::isSuspect)
                    .filter(readingQualityRecord -> readingTimeStamps.contains(readingQualityRecord.getReadingTimestamp()))
                    .collect(Collectors.toMap(ReadingQualityRecord::getReadingTimestamp, Function.identity(), (a, b) -> a));
            Map<Instant, IntervalReadingRecord> referenceRecords = referenceChannel
                    .getIntervalReadings(Ranges.copy(referenceRange.intersection(effectiveMetrologyConfigurationOnUsagePoint.getRange())).asOpenClosed()).stream()
                    .filter(readingRecord -> readingTimeStamps.contains(readingRecord.getTimeStamp()))
                    .filter(readingRecord -> referenceChannelDataInfo.allowSuspectData
                            || !Optional.ofNullable(referenceReadingQualities.get(readingRecord.getTimeStamp())).isPresent())
                    .collect(Collectors.toMap(BaseReading::getTimeStamp, Function.identity(), (a, b) -> a));

            correctedRanges.entrySet().stream().filter(e -> effectiveMetrologyConfigurationOnUsagePoint.overlaps(e.getValue())).forEach(range -> {
                Optional<IntervalReadingRecord> referenceRecord = Optional.ofNullable(referenceRecords.get(range.getValue().upperEndpoint()));
                Optional<IntervalReadingRecord> sourceRecord = Optional.ofNullable(sourceRecords.get(range.getKey().upperEndpoint()));
                if (sourceRecord.isPresent() && referenceRecord.isPresent()) {
                    resultReadings.add(copyRecord(sourceRecord.get(), referenceRecord.get(), referenceChannelDataInfo, usagePoint.getZoneId()));
                } else if (referenceRecord.isPresent()) {
                    resultReadings.add(copyRecord(referenceRecord.get(), range.getKey(), sourceChannel, referenceChannelDataInfo, usagePoint.getZoneId()));
                }
            });
        }
        if (!referenceChannelDataInfo.completePeriod || resultReadings.size() == referenceChannelDataInfo.intervals.size()) {
            return resultReadings;
        } else {
            return Collections.emptyList();
        }
    }

    private OutputChannelDataInfo copyRecord(IntervalReadingRecord referenceReading, Range<Instant> sourceInterval, Channel sourceChannel, ReferenceChannelDataInfo referenceChannelDataInfo, ZoneId zoneId) {
        OutputChannelDataInfo channelDataInfo = outputChannelDataInfoFactory.createUpdatedChannelDataInfo(sourceInterval, referenceReading.getValue()
                        .scaleByPowerOfTen(referenceReading.getReadingType().getMultiplier().getMultiplier() - sourceChannel.getMainReadingType().getMultiplier().getMultiplier()),
                referenceChannelDataInfo.projectedValue,
                Optional.empty(),
                zoneId);
        return channelDataInfo;
    }

    private OutputChannelDataInfo copyRecord(IntervalReadingRecord sourceRecord, IntervalReadingRecord referenceReading, ReferenceChannelDataInfo referenceChannelDataInfo, ZoneId zoneId) {
        OutputChannelDataInfo channelDataInfo = outputChannelDataInfoFactory.createUpdatedChannelDataInfo(sourceRecord, referenceReading.getValue()
                        .scaleByPowerOfTen(referenceReading.getReadingType().getMultiplier().getMultiplier() - sourceRecord.getReadingType().getMultiplier().getMultiplier()),
                referenceChannelDataInfo.projectedValue,
                Optional.empty(),
                zoneId);
        return channelDataInfo;
    }

    private Map<Range<Instant>, Range<Instant>> getCorrectedTimeStampsForReference(Instant referenceStartDate, List<IntervalInfo> intervals) {
        Instant startDate = intervals.stream().map(date -> Instant.ofEpochMilli(date.end)).min(Instant::compareTo).get();
        TemporalAmount offset = Duration.between(startDate, referenceStartDate);
        return intervals.stream()
                .map(interval -> Range.openClosed(Instant.ofEpochMilli(interval.start), Instant.ofEpochMilli(interval.end)))
                .collect(Collectors.toMap(Function.identity(), interval ->  Range.openClosed(interval.lowerEndpoint().plus(offset), interval.upperEndpoint().plus(offset))));
    }
}