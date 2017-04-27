/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeComparator;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfileReading;

import com.google.common.collect.Range;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChannelReferenceDataCopier {
    private final ResourceHelper resourceHelper;
    private final DeviceDataInfoFactory deviceDataInfoFactory;
    private final MeteringService meteringService;
    private final Channel sourceChannel;

    private List<ChannelDataInfo> resultReadings = new ArrayList<>();
    private Map<Range<Instant>, Range<Instant>> correctedRanges;
    private Range<Instant> sourceRange;
    private Range<Instant> referenceRange;
    private List<Instant> readingTimeStamps;

    private final ReadingTypeComparator readingTypeComparator = ReadingTypeComparator.ignoring(ReadingTypeComparator.Attribute.Multiplier);

    public ChannelReferenceDataCopier(MeteringService meteringService,
                                      ResourceHelper resourceHelper,
                                      DeviceDataInfoFactory deviceDataInfoFactory,
                                      Channel sourceChannel) {
        this.resourceHelper = resourceHelper;
        this.deviceDataInfoFactory = deviceDataInfoFactory;
        this.meteringService = meteringService;
        this.sourceChannel = sourceChannel;
    }

    public List<ChannelDataInfo> get(ReferenceChannelDataInfo referenceChannelDataInfo) {

        DeviceValidation deviceValidation = sourceChannel.getDevice().forValidation();
        boolean isValidationActive = deviceValidation.isValidationActive();

        Device referenceDevice = resourceHelper.findDeviceByName(referenceChannelDataInfo.referenceDevice)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_DEVICE, "referenceDevice", referenceChannelDataInfo.referenceDevice));
        ReadingType readingType = meteringService.getReadingType(referenceChannelDataInfo.readingType)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.NO_SUCH_READINGTYPE, "readingType", referenceChannelDataInfo.readingType));
        Channel referenceChannel = referenceDevice.getChannels().stream()
                .filter(ch -> ch.getCalculatedReadingType(referenceChannelDataInfo.startDate)
                        .filter(refernceReadingType -> refernceReadingType.equals(readingType))
                        .isPresent())
                .findFirst()
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.READINGTYPE_NOT_FOUND_ON_DEVICE, "readingType"));
        if (readingTypeComparator.compare(referenceChannel.getReadingType(), sourceChannel.getReadingType()) != 0) {
            throw new LocalizedFieldValidationException(MessageSeeds.READINGTYPES_DONT_MATCH, "readingType");
        }

        resultReadings = new ArrayList<>();
        correctedRanges = getCorrectedTimeStampsForReference(referenceChannelDataInfo.startDate, referenceChannelDataInfo.intervals);
        readingTimeStamps = correctedRanges.values().stream().filter(Range::hasUpperBound).map(Range::upperEndpoint).collect(Collectors.toList());
        if (readingTimeStamps.isEmpty()) {
            return Collections.emptyList();
        }
        sourceRange = correctedRanges.keySet().stream().reduce(Range::span).get();
        referenceRange = correctedRanges.values().stream().reduce(Range::span).get();

        List<LoadProfileReading> sourceReadings = sourceChannel.getChannelData(sourceRange).stream()
                .filter(reading -> correctedRanges.keySet().contains(reading.getRange()))
                .collect(Collectors.toList());
        Map<Instant, LoadProfileReading> referenceReadings = referenceChannel.getChannelData(referenceRange).stream()
                .filter(reading -> readingTimeStamps.contains(reading.getRange().upperEndpoint()))
                .filter(referenceReading -> !referenceReading.getChannelValues().isEmpty())
                .collect(Collectors.toMap(r -> r.getRange().upperEndpoint(), Function.identity(), (a, b) -> a));
        Map<Instant, ReadingQualityRecord> referenceReadingQualities = referenceChannel.getChannelData(referenceRange).stream()
                .filter(reading -> readingTimeStamps.contains(reading.getRange().upperEndpoint()))
                .flatMap(e -> e.getReadingQualities().values().stream().flatMap(Collection::stream))
                .filter(ReadingQualityRecord::isSuspect)
                .filter(readingTimeStamps::contains)
                .collect(Collectors.toMap(ReadingQualityRecord::getTimestamp, Function.identity(), (a, b) -> a));

        sourceReadings.forEach(record -> {
            Optional.ofNullable(correctedRanges.get(record.getRange()))
                    .flatMap(r -> Optional.ofNullable(referenceReadings.get(r.upperEndpoint())))
                    .ifPresent(referenceReading -> {
                        ChannelDataInfo channelDataInfo = deviceDataInfoFactory.createChannelDataInfo(sourceChannel, record, isValidationActive, deviceValidation, null);
                        ChannelDataInfo referenceDataInfo = deviceDataInfoFactory.createChannelDataInfo(sourceChannel, referenceReading, isValidationActive, deviceValidation, null);
                        channelDataInfo.value = referenceDataInfo.value
                                .scaleByPowerOfTen(referenceChannel.getReadingType().getMultiplier().getMultiplier() - sourceChannel.getReadingType().getMultiplier().getMultiplier());
                        channelDataInfo.mainValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
                        channelDataInfo.commentId = referenceDataInfo.commentId;
                        channelDataInfo.commentValue = referenceDataInfo.commentValue;
                        if (referenceChannelDataInfo.allowSuspectData || !Optional.ofNullable(referenceReadingQualities.get(referenceReading.getRange().upperEndpoint()))
                                .filter(ReadingQualityRecord::isSuspect)
                                .isPresent()) {
                            resultReadings.add(channelDataInfo);
                        }
                    });
        });

        if (!referenceChannelDataInfo.completePeriod || resultReadings.size() == referenceChannelDataInfo.intervals.size()) {
            return resultReadings;
        } else {
            return Collections.emptyList();
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