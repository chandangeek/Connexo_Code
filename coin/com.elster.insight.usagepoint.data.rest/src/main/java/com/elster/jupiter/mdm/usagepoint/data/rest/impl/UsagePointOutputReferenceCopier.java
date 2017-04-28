/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;


import com.elster.jupiter.metering.AggregatedChannel;
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

import java.time.Duration;
import java.time.Instant;
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
    private final AggregatedChannel sourceChannel;

    private ReferenceChannelDataInfo referenceChannelDataInfo;
    private List<OutputChannelDataInfo> resultReadings = new ArrayList<>();
    private Map<Range<Instant>, Range<Instant>> correctedRanges;
    private Range<Instant> referenceRange;
    private List<Instant> readingTimeStamps;

    private final ReadingTypeComparator readingTypeComparator = ReadingTypeComparator.ignoring(ReadingTypeComparator.Attribute.Multiplier);

    public UsagePointOutputReferenceCopier(MeteringService meteringService,
                                           ResourceHelper resourceHelper,
                                           OutputChannelDataInfoFactory outputChannelDataInfoFactory,
                                           AggregatedChannel sourceChannel) {
        this.resourceHelper = resourceHelper;
        this.outputChannelDataInfoFactory = outputChannelDataInfoFactory;
        this.meteringService = meteringService;
        this.sourceChannel = sourceChannel;
    }

    public List<OutputChannelDataInfo> get(ReferenceChannelDataInfo referenceChannelDataInfo) {
        this.referenceChannelDataInfo = referenceChannelDataInfo;
        ReadingType readingType = meteringService.getReadingType(referenceChannelDataInfo.readingType)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_READING_TYPE_FOR_MRID, "readingType", referenceChannelDataInfo.readingType));
        UsagePoint usagePoint = resourceHelper.findUsagePointByName(referenceChannelDataInfo.referenceUsagePoint)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_USAGE_POINT_WITH_NAME, "usagePoint", referenceChannelDataInfo.referenceUsagePoint));
        MetrologyPurpose purpose = resourceHelper.findMetrologyPurpose(referenceChannelDataInfo.referencePurpose)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_METROLOGY_PURPOSE, "referencePurpose", referenceChannelDataInfo.referencePurpose));
        resultReadings = new ArrayList<>();

        correctedRanges = getCorrectedTimeStampsForReference(referenceChannelDataInfo.startDate, referenceChannelDataInfo.intervals);
        readingTimeStamps = correctedRanges.values().stream().filter(Range::hasUpperBound).map(Range::upperEndpoint).collect(Collectors.toList());
        if (readingTimeStamps.isEmpty()) {
            return Collections.emptyList();
        }
        referenceRange = correctedRanges.values().stream().reduce(Range::span).get();

        List<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfigurations = usagePoint.getEffectiveMetrologyConfigurations(referenceRange).stream()
                .filter(emc -> emc.getRange().isConnected(referenceRange) && readingTimeStamps.stream().anyMatch(emc::isEffectiveAt)).collect(Collectors.toList());
        for (EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint : effectiveMetrologyConfigurations) {
            MetrologyContract metrologyContract = resourceHelper.findMetrologyContract(effectiveMetrologyConfigurationOnUsagePoint, purpose)
                    .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.METROLOGYPURPOSE_IS_NOT_FOUND_ON_USAGEPOINT, "referencePurpose", purpose.getName(), usagePoint
                            .getName()));
            ReadingTypeDeliverable readingTypeDeliverable = metrologyContract.getDeliverables().stream().filter(output -> output.getReadingType().equals(readingType))
                    .findFirst()
                    .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.READINGTYPE_NOT_FOUND_ON_USAGEPOINT, "readingType", referenceChannelDataInfo.readingType));
            AggregatedChannel referenceChannel = effectiveMetrologyConfigurationOnUsagePoint.getAggregatedChannel(metrologyContract, readingTypeDeliverable.getReadingType())
                    .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.READINGTYPE_NOT_FOUND_ON_USAGEPOINT, "readingType", referenceChannelDataInfo.readingType));
            if (readingTypeComparator.compare(referenceChannel.getMainReadingType(), sourceChannel.getMainReadingType())!=0) {
                throw new LocalizedFieldValidationException(MessageSeeds.READINGTYPES_DONT_MATCH, "readingType");
            }
            Map<Instant, IntervalReadingRecord> sourceRecords = sourceChannel
                    .getIntervalReadings(Ranges.copy(referenceRange.intersection(effectiveMetrologyConfigurationOnUsagePoint.getRange())).asOpenClosed()).stream()
                    .filter(readingRecord -> correctedRanges.keySet().stream().anyMatch(r -> r.upperEndpoint().equals(readingRecord.getTimeStamp())))
                    .collect(Collectors.toMap(BaseReading::getTimeStamp, Function.identity(), (a, b) -> a));
            Map<Instant, IntervalReadingRecord> referenceRecords = referenceChannel
                    .getIntervalReadings(Ranges.copy(referenceRange.intersection(effectiveMetrologyConfigurationOnUsagePoint.getRange())).asOpenClosed()).stream()
                    .filter(readingRecord -> readingTimeStamps.contains(readingRecord.getTimeStamp()))
                    .collect(Collectors.toMap(BaseReading::getTimeStamp, Function.identity(), (a, b) -> a));
            Map<Instant, ReadingQualityRecord> referenceReadingQualities = referenceChannel.findReadingQualities()
                    .inTimeInterval(Ranges.copy(referenceRange.intersection(effectiveMetrologyConfigurationOnUsagePoint.getRange())).asOpenClosed()).stream()
                    .filter(ReadingQualityRecord::isSuspect)
                    .filter(readingTimeStamps::contains)
                    .collect(Collectors.toMap(ReadingQualityRecord::getTimestamp, Function.identity(), (a, b) -> a));

            correctedRanges.entrySet().stream().filter(e -> effectiveMetrologyConfigurationOnUsagePoint.overlaps(e.getValue())).forEach(range -> {
                Optional<IntervalReadingRecord> referenceRecord = Optional.ofNullable(referenceRecords.get(range.getValue().upperEndpoint()));
                Optional<IntervalReadingRecord> sourceRecord = Optional.ofNullable(sourceRecords.get(range.getKey().upperEndpoint()));
                if (sourceRecord.isPresent() && referenceRecord.isPresent()) {
                    copyRecord(sourceRecord.get(), referenceRecord.get(), referenceReadingQualities);
                } else if (referenceRecord.isPresent()) {
                    copyRecord(referenceRecord.get(), range.getKey(), referenceReadingQualities);
                }
            });
        }
        if (!referenceChannelDataInfo.completePeriod || resultReadings.size() == referenceChannelDataInfo.intervals.size()) {
            return resultReadings;
        } else {
            return Collections.emptyList();
        }
    }

    private void copyRecord(IntervalReadingRecord referenceReading, Range<Instant> sourceInterval, Map<Instant, ReadingQualityRecord> referenceReadingQualities) {
        OutputChannelDataInfo channelDataInfo = new OutputChannelDataInfo();
        channelDataInfo.value = referenceReading.getValue()
                .scaleByPowerOfTen(referenceReading.getReadingType().getMultiplier().getMultiplier() - sourceChannel.getMainReadingType().getMultiplier().getMultiplier());
        if (referenceChannelDataInfo.commentId != null) {
            resourceHelper.getReadingQualityComment(referenceChannelDataInfo.commentId)
                    .ifPresent(comment -> {
                        channelDataInfo.commentId = comment.getId();
                        channelDataInfo.commentValue = comment.getComment();
                    });
        }
        channelDataInfo.isProjected = referenceChannelDataInfo.projectedValue;
        channelDataInfo.interval = IntervalInfo.from(sourceInterval);
        if (referenceChannelDataInfo.allowSuspectData || !Optional.ofNullable(referenceReadingQualities.get(sourceInterval.upperEndpoint()))
                .filter(ReadingQualityRecord::isSuspect)
                .isPresent()) {
            resultReadings.add(channelDataInfo);
        }
    }

    private void copyRecord(IntervalReadingRecord sourceRecord, IntervalReadingRecord referenceReading, Map<Instant, ReadingQualityRecord> referenceReadingQualities) {
        OutputChannelDataInfo channelDataInfo = outputChannelDataInfoFactory.createUpdatedChannelDataInfo(sourceRecord, referenceReading.getValue()
                        .scaleByPowerOfTen(referenceReading.getReadingType().getMultiplier().getMultiplier() - sourceRecord
                                .getReadingType()
                                .getMultiplier()
                                .getMultiplier()),
                referenceChannelDataInfo.projectedValue, referenceChannelDataInfo.commentId != null
                        ? resourceHelper.getReadingQualityComment(referenceChannelDataInfo.commentId)
                        : Optional.empty());
        channelDataInfo.isProjected = referenceChannelDataInfo.projectedValue;
        if (referenceChannelDataInfo.allowSuspectData || !Optional.ofNullable(referenceReadingQualities.get(sourceRecord.getTimeStamp()))
                .filter(ReadingQualityRecord::isSuspect)
                .isPresent()) {
            resultReadings.add(channelDataInfo);
        }
    }

    private Map<Range<Instant>, Range<Instant>> getCorrectedTimeStampsForReference(Instant referenceStartDate, List<IntervalInfo> intervals) {
        Instant startDate = intervals.stream().map(date -> Instant.ofEpochMilli(date.end)).min(Instant::compareTo).get();
        TemporalAmount offset = Duration.between(startDate, referenceStartDate);
        return intervals.stream()
                .map(interval -> Range.openClosed(Instant.ofEpochMilli(interval.start), Instant.ofEpochMilli(interval.end)))
                .collect(Collectors.toMap(Function.identity(), interval ->  Range.openClosed(interval.lowerEndpoint().plus(offset), interval.upperEndpoint().plus(offset))));
    }
}