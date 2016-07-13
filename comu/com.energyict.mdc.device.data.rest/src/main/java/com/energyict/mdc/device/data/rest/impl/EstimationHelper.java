package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.rest.PropertyUtils;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.util.Ranges;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfileReading;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EstimationHelper {
    private final EstimationService estimationService;
    private final ExceptionFactory exceptionFactory;
    private final PropertyUtils propertyUtils;
    private final Clock clock;
    private final DeviceDataInfoFactory deviceDataInfoFactory;
    private final Thesaurus thesaurus;
    public static final Logger LOGGER = Logger.getLogger(EstimationHelper.class.getName());

    @Inject
    public EstimationHelper(EstimationService estimationService, ExceptionFactory exceptionFactory, PropertyUtils propertyUtils, Thesaurus thesaurus, Clock clock, DeviceDataInfoFactory deviceDataInfoFactory) {
        this.estimationService = estimationService;
        this.exceptionFactory = exceptionFactory;
        this.propertyUtils = propertyUtils;
        this.clock = clock;
        this.deviceDataInfoFactory = deviceDataInfoFactory;
        this.thesaurus = thesaurus;
    }

    public Estimator getEstimator(EstimateChannelDataInfo estimateChannelDataInfo) {
        Map<String, Object> propertyMap = new HashMap<>();
        if (estimateChannelDataInfo.estimatorImpl == null) {
            throw exceptionFactory.newException(MessageSeeds.ESTIMATOR_REQUIRED);
        }

        Estimator estimator = estimationService.getEstimator(estimateChannelDataInfo.estimatorImpl).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.ESTIMATOR_NOT_FOUND));
        Map<String, String> invalidProperties = new HashMap<>();
        for (PropertySpec propertySpec : estimator.getPropertySpecs()) {
            try {
                Object value = propertyUtils.findPropertyValue(propertySpec, estimateChannelDataInfo.properties);
                propertySpec.validateValue(value);
                propertyMap.put(propertySpec.getName(), value);
            } catch (Exception ex) {
                invalidProperties.put(propertySpec.getName(), thesaurus.getFormat(MessageSeeds.INVALID_ESTIMATOR_PROPERTY_VALUE).format());
            }
        }
        try {
            estimator.validateProperties(propertyMap);
        } catch (LocalizedFieldValidationException ex) {
            invalidProperties.put(ex.getViolatingProperty(), thesaurus.getFormat(MessageSeeds.INVALID_ESTIMATOR_PROPERTY_VALUE).format());
        }
        if(!invalidProperties.isEmpty()) {
            throw new EstimatorPropertiesException(invalidProperties);
        }

        Estimator baseEstimator = estimationService.getEstimator(estimateChannelDataInfo.estimatorImpl, propertyMap).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.ESTIMATOR_NOT_FOUND));
        baseEstimator.init(LOGGER);
        return baseEstimator;
    }

    public EstimationResult previewEstimate(Device device, ReadingType readingType, Range<Instant> range, Estimator estimator) {
        MeterActivation meterActivation = device.getMeterActivationsMostRecentFirst().stream()
                .filter(ma -> ma.getInterval().toOpenClosedRange().contains(range.upperEndpoint()))
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.METER_ACTIVATION_NOT_FOUND));
        return estimationService.previewEstimate(meterActivation, range, readingType, estimator);
    }

    public List<ChannelDataInfo> getChannelDataInfoFromEstimationReports(Channel channel, List<Range<Instant>> ranges, List<EstimationResult> results) {
        List<Instant> failedTimestamps = new ArrayList<>();
        List<ChannelDataInfo> channelDataInfos = new ArrayList<>();
        DeviceValidation deviceValidation = channel.getDevice().forValidation();
        boolean isValidationActive = deviceValidation.isValidationActive(channel, clock.instant());

        List<LoadProfileReading> channelData = new ArrayList<>();
        channelData.addAll(ranges.stream().flatMap(r -> channel.getChannelData(Ranges.openClosed(r.lowerEndpoint(), r.upperEndpoint())).stream()).collect(Collectors.toList()));

        for (EstimationResult result : results) {
            for (EstimationBlock block : result.estimated()) {
                for (Estimatable estimatable : block.estimatables()) {
                    channelDataInfos.addAll(fillChannelDataInfoList(channel, block, estimatable, channelData, isValidationActive, deviceValidation));
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

    private List<ChannelDataInfo> fillChannelDataInfoList(Channel channel, EstimationBlock block, Estimatable estimatable, List<LoadProfileReading> channelData, boolean isValidationActive, DeviceValidation deviceValidation) {
        List<ChannelDataInfo> channelDataInfos = new ArrayList<>();
        for (LoadProfileReading reading : channelData) {
            if (reading.getRange().upperEndpoint().equals(estimatable.getTimestamp())) {
                channelDataInfos.add(getChannelDataInfo(channel, block, reading, isValidationActive, deviceValidation, estimatable));
                break;
            }
        }
        return channelDataInfos;
    }

    private ChannelDataInfo getChannelDataInfo(Channel channel, EstimationBlock block, LoadProfileReading reading, boolean isValidationActive, DeviceValidation deviceValidation, Estimatable estimatable) {
        //todo do we need to add the datalogger here?
        ChannelDataInfo channelDataInfo = deviceDataInfoFactory.createChannelDataInfo(channel, reading, isValidationActive, deviceValidation, null);
        if (!channel.getReadingType().isCumulative()) {
            channelDataInfo.value = estimatable.getEstimation();
            channelDataInfo.mainValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
        } else {
            if (block.getReadingType().equals(channel.getReadingType())) {
                channelDataInfo.collectedValue = estimatable.getEstimation();
                channelDataInfo.bulkValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
            }
            Instant readingTime = estimatable.getTimestamp();
            if (channel.getCalculatedReadingType(readingTime).isPresent() && channel.getCalculatedReadingType(readingTime).get().equals(block.getReadingType())) {
                channelDataInfo.value = estimatable.getEstimation();
                channelDataInfo.mainValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
            }
        }
        return channelDataInfo;
    }
}
