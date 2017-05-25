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
import com.energyict.mdc.device.data.rest.ChannelPeriodType;

import com.google.common.collect.Range;

import javax.inject.Inject;
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

    private final ReadingTypeComparator readingTypeComparator = ReadingTypeComparator.ignoring(ReadingTypeComparator.Attribute.Multiplier);

    @Inject
    public ChannelReferenceDataCopier(MeteringService meteringService,
                                      ResourceHelper resourceHelper,
                                      DeviceDataInfoFactory deviceDataInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.deviceDataInfoFactory = deviceDataInfoFactory;
        this.meteringService = meteringService;
    }

    public List<ChannelDataInfo> copy(Channel sourceChannel, ReferenceChannelDataInfo referenceChannelDataInfo) {

        DeviceValidation deviceValidation = sourceChannel.getDevice().forValidation();
        boolean isValidationActive = deviceValidation.isValidationActive();

        Device referenceDevice = resourceHelper.findDeviceByName(referenceChannelDataInfo.referenceDevice)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, "referenceDevice", referenceChannelDataInfo.referenceDevice));
        ReadingType readingType = meteringService.getReadingType(referenceChannelDataInfo.readingType)
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, "readingType", referenceChannelDataInfo.readingType));
        if ((!sourceChannel.getCalculatedReadingType(referenceChannelDataInfo.startDate).isPresent()
                && readingTypeComparator.compare(readingType, sourceChannel.getReadingType()) != 0)
                || (sourceChannel.getCalculatedReadingType(referenceChannelDataInfo.startDate).isPresent()
                && readingTypeComparator.compare(readingType, sourceChannel.getCalculatedReadingType(referenceChannelDataInfo.startDate).get()) != 0)) {
            throw new LocalizedFieldValidationException(MessageSeeds.READINGTYPES_DONT_MATCH, "readingType");
        }
        Channel referenceChannel = Optional.ofNullable(referenceDevice.getChannels().stream()
                .filter(ch -> ch.getReadingType().equals(readingType))
                .findFirst()
                .orElseGet(() -> referenceDevice.getChannels().stream()
                        .filter(ch -> ch.getCalculatedReadingType(referenceChannelDataInfo.startDate)
                                .filter(refernceReadingType -> refernceReadingType.equals(readingType))
                                .isPresent())
                        .findFirst()
                        .orElse(null)))
                .orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.READINGTYPE_NOT_FOUND_ON_DEVICE, "readingType"));

        List<ChannelDataInfo> resultReadings = new ArrayList<>();
        Map<Range<Instant>, Range<Instant>>  correctedRanges = getCorrectedTimeStampsForReference(referenceChannelDataInfo.startDate, referenceChannelDataInfo.intervals);
        List<Instant> readingTimeStamps = correctedRanges.values().stream().filter(Range::hasUpperBound).map(Range::upperEndpoint).collect(Collectors.toList());
        if (readingTimeStamps.isEmpty()) {
            return Collections.emptyList();
        }
        Range<Instant> sourceRange = correctedRanges.keySet().stream().reduce(Range::span).get();
        Range<Instant> referenceRange = correctedRanges.values().stream().reduce(Range::span).get();

        List<LoadProfileReading> sourceReadings = sourceChannel.getChannelData(sourceRange).stream()
                .filter(reading -> correctedRanges.keySet().contains(reading.getRange()))
                .collect(Collectors.toList());
        Map<Instant, LoadProfileReading> referenceReadings = referenceChannel.getChannelData(referenceRange).stream()
                .filter(reading -> readingTimeStamps.contains(reading.getRange().upperEndpoint()))
                .filter(referenceReading -> !referenceReading.getChannelValues().isEmpty())
                .filter(referenceReading -> referenceChannelDataInfo.allowSuspectData
                        || referenceReading.getReadingQualities().values().stream()
                        .flatMap(Collection::stream)
                        .noneMatch(ReadingQualityRecord::isSuspect))
                .collect(Collectors.toMap(r -> r.getRange().upperEndpoint(), Function.identity(), (a, b) -> a));

        sourceReadings.forEach(record -> {
            Optional.ofNullable(correctedRanges.get(record.getRange()))
                    .flatMap(r -> Optional.ofNullable(referenceReadings.get(r.upperEndpoint())))
                    .ifPresent(referenceReading -> {
                        ChannelDataInfo channelDataInfo = deviceDataInfoFactory.createChannelDataInfo(sourceChannel, record, isValidationActive, deviceValidation, null, ChannelPeriodType.of(sourceChannel));
                        ChannelDataInfo referenceDataInfo = deviceDataInfoFactory.createChannelDataInfo(referenceChannel, referenceReading, isValidationActive, deviceValidation, null, ChannelPeriodType.of(referenceChannel));
                        channelDataInfo.value = referenceDataInfo.value
                                .scaleByPowerOfTen(referenceChannel.getReadingType().getMultiplier().getMultiplier() - sourceChannel.getReadingType().getMultiplier().getMultiplier());
                        channelDataInfo.mainValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
                        channelDataInfo.commentId = referenceDataInfo.commentId;
                        channelDataInfo.commentValue = referenceDataInfo.commentValue;
                        resultReadings.add(channelDataInfo);
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
