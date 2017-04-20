/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

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
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.NonOrBigDecimalValueProperty;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.TwoValuesAbsoluteDifference;
import com.elster.jupiter.properties.TwoValuesDifference;
import com.elster.jupiter.properties.TwoValuesPercentDifference;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test {@link MainCheckValidator}
 */
@RunWith(MockitoJUnitRunner.class)
abstract public class MainCheckValidatorTest {

    @Mock
    protected Thesaurus thesaurus;

    private static DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    static final String CHECK_PURPOSE = "Purpose";

    protected PropertySpecService propertySpecService = new PropertySpecServiceImpl();

    protected Range<Instant> range = Range.closed(instant("20160101000000"),instant("20160207000000"));

    MainCheckValidator initValidator(ValidationConfiguration validationConfiguration) {
        MainCheckValidator validator = new MainCheckValidator(thesaurus, propertySpecService, validationConfiguration.rule
                .createProperties(), validationConfiguration.metrologyConfigurationService, validationConfiguration.validationService);
        validator.init(validationConfiguration.checkChannel, validationConfiguration.readingType, range);
        return validator;
    }

    BigDecimal bigDecimal(Double value) {
        return BigDecimal.valueOf(value);
    }

    static Instant instant(String value) {
        return LocalDate.from(dateTimeFormat.parse(value)).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * Describes validation rule
     */
    class MainCheckValidatorRule {
        String checkPurpose;
        String notExistingCheckPurpose;
        TwoValuesDifference twoValuesDifference;
        NonOrBigDecimalValueProperty minThreshold;
        boolean passIfNoData;
        boolean useValidatedData;
        boolean noCheckChannel;

        MainCheckValidatorRule withCheckPurpose(String checkPurpose) {
            this.checkPurpose = checkPurpose;
            return this;
        }

        MainCheckValidatorRule withNotExistingCheckPurpose(String notExistingCheckPurpose) {
            this.notExistingCheckPurpose = notExistingCheckPurpose;
            return this;
        }

        MainCheckValidatorRule withNotExistingCheckChannel(){
            noCheckChannel = true;
            return this;
        }



        MainCheckValidatorRule withValuedDifference(BigDecimal value) {
            TwoValuesAbsoluteDifference twoValuesAbsoluteDifference = new TwoValuesAbsoluteDifference();
            twoValuesAbsoluteDifference.value = value;
            this.twoValuesDifference = twoValuesAbsoluteDifference;
            return this;
        }

        MainCheckValidatorRule withPerentDifference(Double percent) {
            TwoValuesPercentDifference twoValuesPercentDifference = new TwoValuesPercentDifference();
            twoValuesPercentDifference.percent = percent;
            this.twoValuesDifference = twoValuesPercentDifference;
            return this;
        }

        MainCheckValidatorRule passIfNoRefData(boolean passIfNoData) {
            this.passIfNoData = passIfNoData;
            return this;
        }

        MainCheckValidatorRule useValidatedData(boolean useValidatedData) {
            this.useValidatedData = useValidatedData;
            return this;
        }

        MainCheckValidatorRule withNoMinThreshold() {
            NonOrBigDecimalValueProperty noThreshold = new NonOrBigDecimalValueProperty();
            noThreshold.isNone = true;
            this.minThreshold = noThreshold;
            return this;
        }

        MainCheckValidatorRule withMinThreshold(BigDecimal minThreshold) {
            NonOrBigDecimalValueProperty threshold = new NonOrBigDecimalValueProperty();
            threshold.isNone = false;
            threshold.value = minThreshold;
            this.minThreshold = threshold;
            return this;
        }

        Map<String, Object> createProperties() {
            return ImmutableMap.of(MainCheckValidator.CHECK_PURPOSE, notExistingCheckPurpose==null?checkPurpose:notExistingCheckPurpose,
                    MainCheckValidator.MAX_ABSOLUTE_DIFF, twoValuesDifference,
                    MainCheckValidator.MIN_THRESHOLD, minThreshold,
                    MainCheckValidator.PASS_IF_NO_REF_DATA, passIfNoData,
                    MainCheckValidator.USE_VALIDATED_DATA, useValidatedData);
        }
    }

    /**
     * Describes the sequence of channel readings
     */
    class ChannelReadings {

        List<IntervalReadingRecord> readings = new ArrayList<>();

        ChannelReadings(int readingsCount) {
            IntStream.rangeClosed(1,readingsCount).forEach(c -> readings.add(null));
        }

        void setReadingValue(int index, BigDecimal value, Instant readingTime) {
            IntervalReadingRecord reading = mock(IntervalReadingRecord.class);
            when(reading.getTimeStamp()).thenReturn(readingTime);
            when(reading.getValue()).thenReturn(value);
            readings.remove(index);
            readings.add(index, reading);
        }

        Channel mockChannel() {
            Channel channel = mock(Channel.class);
            when(channel.getIntervalReadings(range)).thenReturn(readings.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
            return channel;
        }

    }

    class ValidatedChannelReadings extends ChannelReadings {

        List<DataValidationStatus> validationStatuses = new ArrayList<>();

        ValidatedChannelReadings(int readingsCount) {
            super(readingsCount);
            IntStream.rangeClosed(1,readingsCount).forEach(c -> validationStatuses.add(null));
        }

        void setReadingValue(int index, BigDecimal value, Instant readingTime) {
            setReadingValue(index, value, readingTime, ValidationResult.NOT_VALIDATED);
        }

        void setReadingValue(int index, BigDecimal value, Instant readingTime, ValidationResult validationResult) {
            super.setReadingValue(index, value, readingTime);
            DataValidationStatus dataValidationStatus = mock(DataValidationStatus.class);
            when(dataValidationStatus.getReadingTimestamp()).thenReturn(readingTime);
            when(dataValidationStatus.getValidationResult()).thenReturn(validationResult);
            validationStatuses.remove(index);
            validationStatuses.add(index, dataValidationStatus);
        }

        ValidationEvaluator mockEvaluator() {
            ValidationEvaluator evaluator = mock(ValidationEvaluator.class);
            when(evaluator.getValidationStatus(anyObject(),
                    anyObject(),
                    anyObject()))
                    .thenReturn(validationStatuses.stream().filter(Objects::nonNull).collect(Collectors.toList()));
            return evaluator;
        }
    }

    /**
     * Describes configuration to be validated
     */
    class ValidationConfiguration {

        // internal properties - input
        MainCheckValidatorRule rule;
        ChannelReadings mainChannelReadings;
        ValidatedChannelReadings checkChannelReadings;

        // external mocks - output
        ReadingType readingType;
        Channel mainChannel;
        Channel checkChannel;
        ValidationService validationService;
        MetrologyConfigurationService metrologyConfigurationService;

        public ValidationConfiguration(MainCheckValidatorRule rule, ChannelReadings mainChannelReadings, ValidatedChannelReadings checkChannelReadings) {
            this.rule = rule;
            this.mainChannelReadings = mainChannelReadings;
            this.checkChannelReadings = checkChannelReadings;
            mockAll();
        }

        void mockAll() {
            readingType = mock(ReadingType.class);
            when(readingType.getMRID()).thenReturn("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
            when(readingType.getFullAliasName()).thenReturn("[Daily] Secondary Delta A+ (kWh)");
            mainChannel = mock(Channel.class);
            UsagePoint usagePoint = mock(UsagePoint.class);
            when(usagePoint.getName()).thenReturn("Usage point name");
            ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
            EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint = mock(EffectiveMetrologyConfigurationOnUsagePoint.class);
            UsagePointMetrologyConfiguration metrologyConfiguration = mock(UsagePointMetrologyConfiguration.class);
            MetrologyContract metrologyContract = mock(MetrologyContract.class);
            MetrologyPurpose metrologyPurpose = mock(MetrologyPurpose.class);

            when(metrologyPurpose.getName()).thenReturn(rule.checkPurpose);
            when(metrologyContract.getMetrologyPurpose()).thenReturn(metrologyPurpose);
            when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
            when(effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
            when(usagePoint.getEffectiveMetrologyConfigurations(range)).thenReturn(Collections.singletonList(effectiveMetrologyConfigurationOnUsagePoint));
            when(channelsContainer.getUsagePoint()).thenReturn(Optional.of(usagePoint));
            when(mainChannel.getChannelsContainer()).thenReturn(channelsContainer);
            when(effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(metrologyContract)).thenReturn(Optional
                    .of(channelsContainer));

            checkChannel = checkChannelReadings.mockChannel();

            when(checkChannel.getChannelsContainer()).thenReturn(channelsContainer);

            when(channelsContainer.getChannel(readingType)).thenReturn(rule.noCheckChannel?Optional.empty():Optional.of(checkChannel));

            validationService = mock(ValidationService.class);
            ValidationEvaluator validationEvaluator = checkChannelReadings.mockEvaluator();
            when(validationService.getEvaluator()).thenReturn(validationEvaluator);

            metrologyConfigurationService = mock(MetrologyConfigurationService.class);
            when(metrologyConfigurationService.getMetrologyPurposes()).thenReturn(Collections.singletonList(metrologyPurpose));

        }
    }
}
