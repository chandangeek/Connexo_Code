/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by dantonov on 10.04.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class MainCheckEstimatorTest {

    private static final Logger LOGGER = Logger.getLogger(PowerGapFillTest.class.getName());
    private static final String PURPOSE = "Purpose";
    private static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Test
    public void basicTest() {

        EstimationConfiguration estimationConfiguration = new EstimationConfiguration()
                .withCompletePeriod(false)
                .withBlock(new BlockConfiguration()
                        .withEstimatable(new EstimatableConf()
                                .of(instant("20160101000000"))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(100D))
                                        .withValidationResult(ValidationResult.VALID)))
                        .withEstimatable(new EstimatableConf()
                                .of(instant("20160102000000"))
                                .withReferenceValue(new ReferenceValue()
                                        .withValue(bigDecimal(200D))
                                        .withValidationResult(ValidationResult.NOT_VALIDATED))));

        estimationConfiguration.mockAll();

        Estimator estimator = mockEstimator(estimationConfiguration);
        EstimationResult estimationResult = estimator.estimate(estimationConfiguration.getAllBlocks(), QualityCodeSystem.MDM);
        assertEquals(1,estimationResult.estimated().size());
        assertEquals(0,estimationResult.remainingToBeEstimated().size());
        assertEquals(2,estimationResult.estimated().get(0).estimatables().size());
    }

    MainCheckEstimator mockEstimator(EstimationConfiguration estimationConfiguration) {
        MainCheckEstimator estimator = new MainCheckEstimator(estimationConfiguration.thesaurus, estimationConfiguration.metrologyConfigurationService, estimationConfiguration.validationService, estimationConfiguration.propertySpecService, estimationConfiguration.properties);
        estimator.init(LOGGER);
        return estimator;
    }

    Instant instant(String value) {
        return LocalDate.from(DATE_TIME_FORMATTER.parse(value))
                .atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant();
    }

    BigDecimal bigDecimal(Double value) {
        return BigDecimal.valueOf(value);
    }

    class EstimationConfiguration {
        // Objects to mock
        Thesaurus thesaurus;
        MetrologyConfigurationService metrologyConfigurationService;
        ValidationService validationService;
        PropertySpecService propertySpecService;
        Map<String, Object> properties;
        List<EstimationBlock> estimationBlocks;

        // internal data
        List<BlockConfiguration> blocks = new ArrayList<>();
        boolean completePeriod;

        EstimationConfiguration withCompletePeriod(boolean completePeriod) {
            this.completePeriod = completePeriod;
            return this;
        }

        EstimationConfiguration withBlock(BlockConfiguration blockConfiguration) {
            blocks.add(blockConfiguration);
            return this;
        }

        List<EstimationBlock> getAllBlocks() {
            return estimationBlocks;
        }

        void mockAll() {
            properties = new HashMap<String, Object>() {{
                put(MainCheckEstimator.CHECK_PURPOSE, PURPOSE);
                put(MainCheckEstimator.COMPLETE_PERIOD, completePeriod);
            }};

            estimationBlocks = blocks.stream().map(BlockConfiguration::mockBlock).collect(Collectors.toList());

            validationService = mock(ValidationService.class);
            ValidationEvaluator validationEvaluator = mock(ValidationEvaluator.class);
            when(validationService.getEvaluator()).thenReturn(validationEvaluator);
            when(validationEvaluator.getValidationStatus(any(),any(),any())).thenAnswer(invocationOnMock -> {
                List<BaseReading> checkChannelBaseReading = (List<BaseReading>)invocationOnMock.getArguments()[2];
                BaseReading reading = checkChannelBaseReading.get(0);
                Instant timeStamp = reading.getTimeStamp();
                // find reference reading for this timestamp
                Optional<EstimatableConf> estimatableConfig =
                blocks.stream().flatMap(block ->
                    block.estimatables.stream()).filter(estimatableConf -> estimatableConf.timeStamp.compareTo(timeStamp)==0).findFirst();
                if (estimatableConfig.isPresent()){
                    DataValidationStatus dataValidationStatus = mock(DataValidationStatus.class);
                    when(dataValidationStatus.getValidationResult()).thenReturn(estimatableConfig.get().referenceValue.validationResult);
                    return Collections.singletonList(dataValidationStatus);
                }else {
                    return Collections.EMPTY_LIST;
                }
            });
        }
    }

    class BlockConfiguration {
        List<EstimatableConf> estimatables = new ArrayList<>();

        BlockConfiguration withEstimatable(EstimatableConf estimatableConf) {
            estimatables.add(estimatableConf);
            return this;
        }

        EstimationBlock mockBlock() {
            EstimationBlock estimationBlock = mock(EstimationBlock.class);
            Channel channel = mock(Channel.class);
            when(estimationBlock.getChannel()).thenReturn(channel);
            ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
            when(channel.getChannelsContainer()).thenReturn(channelsContainer);
            UsagePoint usagePoint = mock(UsagePoint.class);
            when(channelsContainer.getUsagePoint()).thenReturn(Optional.of(usagePoint));
            when(usagePoint.getName()).thenReturn("usage point name");

            List<Estimatable> estimatablesList = estimatables.stream()
                    .map(EstimatableConf::mockEstimatable)
                    .collect(Collectors.toList());
            doReturn(estimatablesList).when(estimationBlock).estimatables();

            ReadingType readingType = mock(ReadingType.class);

            when(estimationBlock.getReadingType()).thenReturn(readingType);

            EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
            when(usagePoint.getEffectiveMetrologyConfiguration(any(Instant.class))).thenReturn(Optional.of(effectiveMetrologyConfigurationOnUsagePoint));

            UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
            when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
            MetrologyContract metrologyContract = mock(MetrologyContract.class);
            when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
            MetrologyPurpose metrologyPurpose = mock(MetrologyPurpose.class);
            when(metrologyContract.getMetrologyPurpose()).thenReturn(metrologyPurpose);
            when(metrologyPurpose.getName()).thenReturn(PURPOSE);

            when(effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(metrologyContract)).thenReturn(Optional
                    .of(channelsContainer));

            Channel checkChannel = mock(Channel.class);

            when(channelsContainer.getChannel(readingType)).thenReturn(Optional.of(checkChannel));

            IntervalReadingRecord checkReading = mock(IntervalReadingRecord.class);
            when(checkChannel.getIntervalReadings(any())).thenAnswer(invocationOnMock -> {
                Range<Instant> interval = (Range<Instant>)invocationOnMock.getArguments()[0];
                Optional<EstimatableConf> estimatableConf =
                        estimatables.stream()
                                .filter(e ->
                                        e.timeStamp.compareTo(interval.lowerEndpoint()) > 0 && e.timeStamp.compareTo(interval
                                                .upperEndpoint()) < 0)
                                .findFirst();

                if (estimatableConf.isPresent()){
                    if (estimatableConf.get().referenceValue != null) {
                        IntervalReadingRecord intervalReading = mock(IntervalReadingRecord.class);
                        when(intervalReading.getValue()).thenReturn(estimatableConf.get().referenceValue.value);
                        when(intervalReading.getTimeStamp()).thenReturn(estimatableConf.get().timeStamp);
                        return Collections.singletonList(intervalReading);
                    }
                }
                return Collections.EMPTY_LIST;
            });
            return estimationBlock;
        }
    }

    class EstimatableConf {
        ReferenceValue referenceValue;
        BigDecimal value;
        Instant timeStamp;

        EstimatableConf of(Instant timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }

        EstimatableConf withNoReferenceValue() {
            this.referenceValue = null;
            return this;
        }

        EstimatableConf withReferenceValue(ReferenceValue referenceValue) {
            this.referenceValue = referenceValue;
            return this;
        }

        Estimatable mockEstimatable() {
            Estimatable estimatable = mock(Estimatable.class);
            when(estimatable.getTimestamp()).thenReturn(timeStamp);
            return estimatable;
        }
    }

    class ReferenceValue {
        BigDecimal value;
        ValidationResult validationResult;

        ReferenceValue withValue(BigDecimal value) {
            this.value = value;
            return this;
        }

        ReferenceValue withValidationResult(ValidationResult validationResult) {
            this.validationResult = validationResult;
            return this;
        }
    }
}
