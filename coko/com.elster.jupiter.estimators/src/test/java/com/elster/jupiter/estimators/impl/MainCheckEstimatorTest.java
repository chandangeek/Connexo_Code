/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
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
import com.elster.jupiter.nls.NlsMessageFormat;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class MainCheckEstimatorTest {

    private static final Logger LOGGER = Logger.getLogger(MainCheckEstimatorTest.class.getName());
    @Mock
    private MetrologyPurpose PURPOSE;
    private static final String PURPOSE_NAME = "Purpose";
    @Mock
    private MetrologyPurpose NOT_EXISTING_PURPOSE;
    private static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    MainCheckEstimator mockEstimator(EstimationConfiguration estimationConfiguration) {
        MainCheckEstimator estimator = new MainCheckEstimator(estimationConfiguration.thesaurus, estimationConfiguration.metrologyConfigurationService, estimationConfiguration.validationService, estimationConfiguration.propertySpecService, estimationConfiguration.properties);
        estimator.init(estimationConfiguration.logger==null?LOGGER:estimationConfiguration.logger);
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

    BigDecimal findEstimatedValue(EstimationConfiguration estimationConfiguration, Instant timeStamp) {
        return estimationConfiguration.blocks.stream().flatMap(blockConfiguration -> blockConfiguration.estimatables.stream()).filter(estimatableConf -> estimatableConf.timeStamp.compareTo(timeStamp)==0).findFirst().map(estimatableConf -> estimatableConf.estimatedValue).orElse(null);
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
        boolean notAvailablePurpose;
        private Logger logger;


        EstimationConfiguration withLogger(Logger logger) {
            this.logger = logger;
            return this;
        }

        EstimationConfiguration withBlock(BlockConfiguration blockConfiguration) {
            blocks.add(blockConfiguration);
            return this;
        }

        EstimationConfiguration withNotAvailablePurpose(){
            this.notAvailablePurpose = true;
            return this;
        }

        List<EstimationBlock> getAllBlocks() {
            return estimationBlocks;
        }

        NlsMessageFormat createNlsMessageFormat(MessageSeeds messageSeeds){
            NlsMessageFormat nlsMessageFormat = mock(NlsMessageFormat.class);
            when(nlsMessageFormat.format(anyVararg())).thenAnswer(invocationOnMock -> {
                return String.format(messageSeeds.getDefaultFormat().replaceAll("\\{.}","%s"),invocationOnMock.getArguments());
            });
            return nlsMessageFormat;
        }


        void mockAll() {

            thesaurus = mock(Thesaurus.class);

            NlsMessageFormat nameFormat = mock(NlsMessageFormat.class);
            when(nameFormat.format()).thenReturn(MainCheckEstimator.TranslationKeys.ESTIMATOR_NAME.getDefaultFormat());
            when(thesaurus.getFormat(MainCheckEstimator.TranslationKeys.ESTIMATOR_NAME)).thenReturn(nameFormat);

            Arrays.stream(MessageSeeds.values()).forEach(messageSeeds -> {
                NlsMessageFormat nlsMessageFormat = createNlsMessageFormat(messageSeeds);
                when(thesaurus.getFormat(messageSeeds)).thenReturn(nlsMessageFormat);
            });

            properties = new HashMap<String, Object>() {{
                put(MainCheckEstimator.CHECK_PURPOSE, notAvailablePurpose?NOT_EXISTING_PURPOSE:PURPOSE);
            }};

            UsagePoint usagePoint = mock(UsagePoint.class);
            when(usagePoint.getName()).thenReturn("usage point name");

            estimationBlocks = blocks.stream().map(b-> b.mockBlock(usagePoint)).collect(Collectors.toList());

            when(usagePoint.getEffectiveMetrologyConfigurations(any(Range.class))).thenAnswer(invocationOnMock -> {
                Range<Instant> range = (Range<Instant>) invocationOnMock.getArguments()[0];
                BlockConfiguration blockConf = blocks.stream().filter(b -> b.belongs(range)).findFirst().orElse(null);
                return Collections.singletonList(blockConf.effectiveMetrologyConfigurationOnUsagePoint);
            });

            validationService = mock(ValidationService.class);
            ValidationEvaluator validationEvaluator = mock(ValidationEvaluator.class);
            when(validationService.getEvaluator()).thenReturn(validationEvaluator);
            when(validationEvaluator.getValidationStatus(any(),any(),any())).thenAnswer(invocationOnMock -> {
                List<BaseReading> checkChannelBaseReading = (List<BaseReading>)invocationOnMock.getArguments()[2];
                return checkChannelBaseReading.stream().map(BaseReading::getTimeStamp).map(timeStamp -> blocks.stream().flatMap(block ->
                        block.estimatables.stream()).filter(estimatableConf -> estimatableConf.timeStamp.compareTo(timeStamp)==0).findFirst()).map(estimatableConf -> {
                   if (estimatableConf.isPresent()) {
                       DataValidationStatus dataValidationStatus = mock(DataValidationStatus.class);
                       when(dataValidationStatus.getValidationResult()).thenReturn(estimatableConf.get().referenceValue.validationResult);
                       when(dataValidationStatus.getReadingTimestamp()).thenReturn(estimatableConf.get().timeStamp);
                       return dataValidationStatus;
                   } else {
                       return null;
                   }
                }).collect(Collectors.toList());
            });
        }
    }

    class BlockConfiguration {
        List<EstimatableConf> estimatables = new ArrayList<>();
        boolean noCheckChannel;

        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint;

        boolean belongs(Range<Instant> range){
            Instant startInterval = estimatables.get(0).timeStamp;
            Instant endInterval = estimatables
                    .get(estimatables.size() - 1)
                    .timeStamp;
            return range.lowerEndpoint().compareTo(startInterval)==0 && range.upperEndpoint().compareTo(endInterval)==0;
        }

        BlockConfiguration withEstimatable(EstimatableConf estimatableConf) {
            estimatables.add(estimatableConf);
            return this;
        }

        BlockConfiguration withNoCheckChannel() {
            this.noCheckChannel = true;
            return this;
        }


        EstimationBlock mockBlock(UsagePoint usagePoint) {
            EstimationBlock estimationBlock = mock(EstimationBlock.class);
            Channel channel = mock(Channel.class);
            when(estimationBlock.getChannel()).thenReturn(channel);
            ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
            when(channel.getChannelsContainer()).thenReturn(channelsContainer);
            when(channelsContainer.getUsagePoint()).thenReturn(Optional.of(usagePoint));

            List<Estimatable> estimatablesList = estimatables.stream()
                    .map(EstimatableConf::mockEstimatable)
                    .collect(Collectors.toList());
            doReturn(estimatablesList).when(estimationBlock).estimatables();

            ReadingType readingType = mock(ReadingType.class);
            when(readingType.getFullAliasName()).thenReturn("[Daily] Secondary Delta A+ (kWh)");

            when(estimationBlock.getReadingType()).thenReturn(readingType);

            effectiveMetrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);

            //estimatables.stream().map(estimatableConf -> estimatableConf.timeStamp).forEach(i->when(usagePoint.getEffectiveMetrologyConfiguration(i)).thenReturn(Optional.of(effectiveMetrologyConfigurationOnUsagePoint)));


            UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
            when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
            MetrologyContract metrologyContract = mock(MetrologyContract.class);
            when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
            when(metrologyContract.getMetrologyPurpose()).thenReturn(PURPOSE);
            when(PURPOSE.getName()).thenReturn(PURPOSE_NAME);

            when(effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(metrologyContract)).thenReturn(Optional
                    .of(channelsContainer));

            Channel checkChannel = mock(Channel.class);

            when(channelsContainer.getChannel(readingType)).thenReturn(noCheckChannel?Optional.empty():Optional.of(checkChannel));

            when(checkChannel.getIntervalReadings(any())).thenAnswer(invocationOnMock -> {
                Range<Instant> interval = (Range<Instant>)invocationOnMock.getArguments()[0];
                List<EstimatableConf> estimatableConf =
                        estimatables.stream()
                                .filter(e ->
                                        e.timeStamp.compareTo(interval.lowerEndpoint()) >= 0 && e.timeStamp.compareTo(interval
                                                .upperEndpoint()) <= 0)
                                .collect(Collectors.toList());

                return estimatableConf.stream().filter(e -> e.referenceValue!=null).map(e->{
                    IntervalReadingRecord intervalReading = mock(IntervalReadingRecord.class);
                    when(intervalReading.getValue()).thenReturn(e.referenceValue.value);
                    when(intervalReading.getTimeStamp()).thenReturn(e.timeStamp);
                    return intervalReading;
                }).collect(Collectors.toList());
            });
            return estimationBlock;
        }
    }

    class EstimatableConf {
        ReferenceValue referenceValue;
        Instant timeStamp;

        BigDecimal estimatedValue;

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
            doAnswer(invocationOnMock -> {
                estimatedValue = (BigDecimal)invocationOnMock.getArguments()[0];
                return null;
            }).when(estimatable).setEstimation(any());
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
