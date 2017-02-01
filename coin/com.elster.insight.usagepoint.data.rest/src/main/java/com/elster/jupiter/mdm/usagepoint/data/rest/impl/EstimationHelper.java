/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.util.Ranges;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EstimationHelper {
    public static final Logger LOGGER = Logger.getLogger(EstimationHelper.class.getName());
    private final EstimationService estimationService;
    private final ExceptionFactory exceptionFactory;
    private final PropertyValueInfoService propertyValueInfoService;
    private final Clock clock;
    private final OutputChannelDataInfoFactory channelDataInfoFactory;
    private final Thesaurus thesaurus;

    @Inject
    public EstimationHelper(EstimationService estimationService, ExceptionFactory exceptionFactory, PropertyValueInfoService propertyValueInfoService, Thesaurus thesaurus, Clock clock, OutputChannelDataInfoFactory channelDataInfoFactory) {
        this.estimationService = estimationService;
        this.exceptionFactory = exceptionFactory;
        this.propertyValueInfoService = propertyValueInfoService;
        this.clock = clock;
        this.channelDataInfoFactory = channelDataInfoFactory;
        this.thesaurus = thesaurus;
    }

    Estimator getEstimator(EstimateChannelDataInfo estimateChannelDataInfo) {
        Map<String, Object> propertyMap = new HashMap<>();
        if (estimateChannelDataInfo.estimatorImpl == null) {
            throw exceptionFactory.newException(MessageSeeds.ESTIMATOR_REQUIRED);
        }

        Estimator estimator = estimationService.getEstimator(estimateChannelDataInfo.estimatorImpl)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.ESTIMATOR_NOT_FOUND));
        Map<String, String> invalidProperties = new HashMap<>();
        for (PropertySpec propertySpec : estimator.getPropertySpecs()) {
            try {
                Object value = propertyValueInfoService.findPropertyValue(propertySpec, estimateChannelDataInfo.properties);
                propertySpec.validateValue(value);
                propertyMap.put(propertySpec.getName(), value);
            } catch (Exception ex) {
                invalidProperties.put(propertySpec.getName(), thesaurus.getFormat(MessageSeeds.INVALID_ESTIMATOR_PROPERTY_VALUE)
                        .format());
            }
        }
        try {
            estimator.validateProperties(propertyMap);
        } catch (LocalizedFieldValidationException ex) {
            invalidProperties.put(ex.getViolatingProperty(), thesaurus.getFormat(MessageSeeds.INVALID_ESTIMATOR_PROPERTY_VALUE)
                    .format());
        }
        if (!invalidProperties.isEmpty()) {
            throw new EstimatorPropertiesException(invalidProperties);
        }

        Estimator baseEstimator = estimationService.getEstimator(estimateChannelDataInfo.estimatorImpl, propertyMap)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.ESTIMATOR_NOT_FOUND));
        baseEstimator.init(LOGGER);
        return baseEstimator;
    }

    EstimationResult previewEstimate(QualityCodeSystem system, ChannelsContainer channelsContainer, ReadingType readingType, Range<Instant> range, Estimator estimator) {
        return estimationService.previewEstimate(system, channelsContainer, range, readingType, estimator);
    }

    List<OutputChannelDataInfo> getChannelDataInfoFromEstimationReports(Channel channel, List<Range<Instant>> ranges, List<EstimationResult> results) {
        List<Instant> failedTimestamps = new ArrayList<>();
        List<OutputChannelDataInfo> channelDataInfos = new ArrayList<>();

        List<IntervalReadingRecord> channelData = ranges.stream()
                .flatMap(r -> channel.getIntervalReadings(Ranges.openClosed(r.lowerEndpoint(), r.upperEndpoint()))
                        .stream())
                .collect(Collectors.toList());

        for (EstimationResult result : results) {
            for (EstimationBlock block : result.estimated()) {
                for (Estimatable estimatable : block.estimatables()) {
                    getChannelDataInfo(estimatable, channelData).ifPresent(channelDataInfos::add);
                }
            }
            for (EstimationBlock block : result.remainingToBeEstimated()) {
                for (Estimatable estimatable : block.estimatables()) {
                    failedTimestamps.add(estimatable.getTimestamp());
                }
            }
        }
        if (!failedTimestamps.isEmpty()) {
            throw new EstimationErrorException(failedTimestamps);
        }
        return channelDataInfos;
    }


    private Optional<OutputChannelDataInfo> getChannelDataInfo(Estimatable estimatable, List<IntervalReadingRecord> channelData) {
        return channelData.stream()
                .filter(readingRecord -> readingRecord.getTimeStamp().equals(estimatable.getTimestamp()))
                .map(readingRecord -> getChannelDataInfo(readingRecord, estimatable))
                .findFirst();
    }

    private OutputChannelDataInfo getChannelDataInfo(IntervalReadingRecord reading, Estimatable estimatable) {
        return channelDataInfoFactory.createEstimatedChannelDataInfo(reading, estimatable.getEstimation());
    }
}
