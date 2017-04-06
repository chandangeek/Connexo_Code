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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test {@link MainCheckValidator}
 */
@RunWith(MockitoJUnitRunner.class)
public class MainCheckValidatorTest {

    @Mock
    private Thesaurus thesaurus;

    private static DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final String CHECK_PURPOSE = "Purpose";

    private PropertySpecService propertySpecService = new PropertySpecServiceImpl();
    private MainCheckValidator validator;

    ValidationConfiguration validationConfiguration;
    Range<Instant> range = Range.all();

    @Before
    public void setUp() {

        MainCheckValidatorRule rule = new MainCheckValidatorRule()
                .withCheckPurpose(CHECK_PURPOSE)
                .withValuedDifference(bigDecimal(100D))
                .passIfNoRefData(false)
                .useValidatedData(false)
                .withNoMinThreshold();

        ChannelReadings channelReadings = new ChannelReadings(3);
        channelReadings.setReadingValue(0, bigDecimal(10D), instant("20160101000000"));
        channelReadings.setReadingValue(1, bigDecimal(20D), instant("20160102000000"));
        channelReadings.setReadingValue(2, bigDecimal(30D), instant("20160103000000"));

        ValidatedChannelReadings checkReadings = new ValidatedChannelReadings(3);
        checkReadings.setReadingValue(0, bigDecimal(10D), instant("20160101000000"));
        checkReadings.setReadingValue(1, bigDecimal(20D), instant("20160102000000"));
        checkReadings.setReadingValue(2, bigDecimal(30D), instant("20160103000000"));

        validationConfiguration = new ValidationConfiguration(rule, channelReadings, checkReadings);
        validationConfiguration.mockAll();

        validator = new MainCheckValidator(thesaurus, propertySpecService, rule.createProperties(), validationConfiguration.metrologyConfigurationService, validationConfiguration.validationService);
        validator.init(validationConfiguration.checkChannel, validationConfiguration.readingType, range);
    }

    @Test
    public void testValidationOk() {

        List<ValidationResult> validationResults = validationConfiguration.mainChannelReadings.readings.stream()
                .map(validator::validate)
                .collect(Collectors.toList());
        long notValid = validationResults.stream().filter((c -> !c.equals(ValidationResult.VALID))).count();
        assertEquals(0L, notValid);
    }

    private BigDecimal bigDecimal(Double value) {
        return BigDecimal.valueOf(value);
    }

    private Instant instant(String value) {
        return LocalDate.from(dateTimeFormat.parse(value)).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * Describes validation rule
     */
    @RunWith(MockitoJUnitRunner.class)
    private class MainCheckValidatorRule {
        String checkPurpose;
        TwoValuesDifference twoValuesDifference;
        NonOrBigDecimalValueProperty minThreshold;
        boolean passIfNoData;
        boolean useValidatedData;

        MainCheckValidatorRule withCheckPurpose(String checkPurpose) {
            this.checkPurpose = checkPurpose;
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
            return ImmutableMap.of(MainCheckValidator.CHECK_PURPOSE, this.checkPurpose,
                    MainCheckValidator.MAX_ABSOLUTE_DIFF, this.twoValuesDifference,
                    MainCheckValidator.MIN_THRESHOLD, this.minThreshold,
                    MainCheckValidator.PASS_IF_NO_REF_DATA, this.passIfNoData,
                    MainCheckValidator.USE_VALIDATED_DATA, this.useValidatedData);
        }
    }

    /**
     * Describes the sequence of channel readings
     */
    @RunWith(MockitoJUnitRunner.class)
    private class ChannelReadings {

        List<IntervalReadingRecord> readings;

        ChannelReadings(int readingsCount) {
            this.readings = new ArrayList<>(readingsCount);
        }

        void setReadingValue(int index, BigDecimal value, Instant readingTime) {
            IntervalReadingRecord reading = mock(IntervalReadingRecord.class);
            when(reading.getTimeStamp()).thenReturn(readingTime);
            when(reading.getValue()).thenReturn(value);
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

    @RunWith(MockitoJUnitRunner.class)
    private class ValidatedChannelReadings extends ChannelReadings {

        List<DataValidationStatus> validationStatuses;

        ValidatedChannelReadings(int readingsCount) {
            super(readingsCount);
            this.validationStatuses = new ArrayList<>(readingsCount);
        }

        void setReadingValue(int index, BigDecimal value, Instant readingTime) {
            setReadingValue(index, value, readingTime, ValidationResult.NOT_VALIDATED);
        }

        void setReadingValue(int index, BigDecimal value, Instant readingTime, ValidationResult validationResult) {
            super.setReadingValue(index, value, readingTime);
            DataValidationStatus dataValidationStatus = mock(DataValidationStatus.class);
            when(dataValidationStatus.getReadingTimestamp()).thenReturn(readingTime);
            when(dataValidationStatus.getValidationResult()).thenReturn(validationResult);
            validationStatuses.add(dataValidationStatus);
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
    @RunWith(MockitoJUnitRunner.class)
    private class ValidationConfiguration {

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
        }

        void mockAll() {
            readingType = mock(ReadingType.class);
            mainChannel = mock(Channel.class);
            UsagePoint usagePoint = mock(UsagePoint.class);
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

            when(channelsContainer.getChannel(readingType)).thenReturn(Optional.of(checkChannel));

            validationService = mock(ValidationService.class);
            ValidationEvaluator validationEvaluator = checkChannelReadings.mockEvaluator();
            when(validationService.getEvaluator()).thenReturn(validationEvaluator);

            metrologyConfigurationService = mock(MetrologyConfigurationService.class);
            when(metrologyConfigurationService.getMetrologyPurposes()).thenReturn(Collections.singletonList(metrologyPurpose));

        }
    }
}
