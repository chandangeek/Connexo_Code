package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.estimation.*;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.rest.PropertyUtils;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.rest.ValueModificationFlag;
import com.google.common.collect.Range;


import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EstimationHelper {
    private final EstimationService estimationService;
    private final ExceptionFactory exceptionFactory;
    private final PropertyUtils propertyUtils;
    private final Clock clock;
    private final DeviceDataInfoFactory deviceDataInfoFactory;
    public static final Logger LOGGER = Logger.getLogger(EstimationHelper.class.getName());

    @Inject
    public EstimationHelper(EstimationService estimationService, ExceptionFactory exceptionFactory, PropertyUtils propertyUtils, Clock clock, DeviceDataInfoFactory deviceDataInfoFactory) {
        this.estimationService = estimationService;
        this.exceptionFactory = exceptionFactory;
        this.propertyUtils = propertyUtils;
        this.clock = clock;
        this.deviceDataInfoFactory = deviceDataInfoFactory;
    }

    public Estimator getEstimator(EstimateChannelDataInfo estimateChannelDataInfo) {
        Map<String, Object> propertyMap = new HashMap<>();
        if (estimateChannelDataInfo.estimatorImpl == null) {
            throw exceptionFactory.newException(MessageSeeds.ESTIMATOR_REQUIRED);
        }

        Estimator estimator = estimationService.getEstimator(estimateChannelDataInfo.estimatorImpl).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.ESTIMATOR_NOT_FOUND));
        for (PropertySpec propertySpec : estimator.getPropertySpecs()) {
            Object value = propertyUtils.findPropertyValue(propertySpec, estimateChannelDataInfo.properties);
            if (value != null) {
                propertyMap.put(propertySpec.getName(), value);
            }
        }

        Estimator baseEstimator = estimationService.getEstimator(estimateChannelDataInfo.estimatorImpl, propertyMap).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.ESTIMATOR_NOT_FOUND));
        baseEstimator.init(LOGGER);
        return baseEstimator;
    }

    public EstimationResult previewEstimate(Device device, ReadingType readingType, Range<Instant> range, Estimator estimator) {
        MeterActivation meterActivation = device.getCurrentMeterActivation().orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.METER_ACTIVATION_NOT_FOUND));
        return estimationService.previewEstimate(meterActivation, range, readingType, estimator);
    }

    public List<ChannelDataInfo> getChannelDataInfoFromEstimationReports(Channel channel, List<Range<Instant>> ranges, List<EstimationResult> results) {
        List<ChannelDataInfo> channelDataInfos = new ArrayList<>();
        DeviceValidation deviceValidation = channel.getDevice().forValidation();
        boolean isValidationActive = deviceValidation.isValidationActive(channel, clock.instant());

        List<LoadProfileReading> channelData = new ArrayList<>();
        channelData.addAll(ranges.stream().flatMap(r -> channel.getChannelData(Ranges.openClosed(r.lowerEndpoint(), r.upperEndpoint())).stream()).collect(Collectors.toList()));

        for(EstimationResult result : results) {
            for (EstimationBlock block : result.estimated()) {
                for (Estimatable estimatable : block.estimatables()) {
                    channelDataInfos.addAll(fillChannelDataInfoList(channel, block, estimatable, channelData, isValidationActive, deviceValidation));
                }
            }
        }
        return channelDataInfos;
    }

    private List<ChannelDataInfo> fillChannelDataInfoList(Channel channel, EstimationBlock block, Estimatable estimatable, List<LoadProfileReading> channelData,  boolean isValidationActive, DeviceValidation deviceValidation) {
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
        ChannelDataInfo channelDataInfo = deviceDataInfoFactory.createChannelDataInfo(reading, isValidationActive, deviceValidation);
        if (!channel.getReadingType().isCumulative()) {
        channelDataInfo.value = estimatable.getEstimation();
        channelDataInfo.validationInfo.mainValidationInfo.valueModificationFlag = ValueModificationFlag.ESTIMATED;
        channelDataInfo.validationInfo.mainValidationInfo.validationRules = Collections.EMPTY_SET;
        channelDataInfo.validationInfo.mainValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
        } else {
            if (block.getReadingType().equals(channel.getReadingType())) {
                channelDataInfo.collectedValue = estimatable.getEstimation();
                channelDataInfo.validationInfo.bulkValidationInfo.valueModificationFlag = ValueModificationFlag.ESTIMATED;
                channelDataInfo.validationInfo.bulkValidationInfo.validationRules = Collections.EMPTY_SET;
                channelDataInfo.validationInfo.bulkValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
            }
            if (channel.getReadingType().getCalculatedReadingType().isPresent() && channel.getReadingType().getCalculatedReadingType().get().equals(block.getReadingType())) {
                channelDataInfo.value = estimatable.getEstimation();
                channelDataInfo.validationInfo.mainValidationInfo.valueModificationFlag = ValueModificationFlag.ESTIMATED;
                channelDataInfo.validationInfo.mainValidationInfo.validationRules = Collections.EMPTY_SET;
                channelDataInfo.validationInfo.mainValidationInfo.validationResult = ValidationStatus.NOT_VALIDATED;
            }
        }
        return channelDataInfo;
    }
}
