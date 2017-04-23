/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validators.MissingRequiredProperty;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsecutiveValidatorTest {

    public static final BigDecimal MINIMUM_THRESHOLD = BigDecimal.valueOf(0.8);
    public static final BigDecimal BELOW_MINIMUM = BigDecimal.valueOf(0.5);
    public static final BigDecimal ABOVE_MINIMUM = BigDecimal.ONE;
    public static final TimeDuration MINIMUM_PERIOD = TimeDuration.hours(2);
    public static final TimeDuration MAXIMUM_PERIOD = TimeDuration.days(1);
    public static final long START_VALIDATION = 100000L;
    public static final long END_VALIDATION = START_VALIDATION + MAXIMUM_PERIOD.getSeconds() * 2;
    public static final long RECORD_INTERVAL = 900L;
    public static final String CONSECUTIVE_ZERO = "Consecutive zero's";

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    private PropertySpecService propertySpecService = new PropertySpecServiceImpl();
    @Mock
    private Channel channel;
    @Mock
    private ReadingType readingType;

    private ConsecutiveValidator consecutiveValidator;
    private IntervalReadingRecord recordFromShortZeroInterval;
    private IntervalReadingRecord recordOutsideZeroInterval;
    private IntervalReadingRecord recordFromZeroInterval;
    private IntervalReadingRecord recordFromLongZeroInterval;
    private IntervalReadingRecord recordAfterShortZeroIntervalInRetroactivelyPeriod;
    private IntervalReadingRecord recordAfterMaxZeroIntervalInRetroactivelyPeriod;
    private List<Instant> instantsFromShortZeroInterval;

    @Before
    public void setUp() {
        when(readingType.getAccumulation()).thenReturn(Accumulation.DELTADELTA);
        ImmutableMap<String, Object> properties = ImmutableMap.of(ConsecutiveValidator.MINIMUM_PERIOD, MINIMUM_PERIOD,
                ConsecutiveValidator.MAXIMUM_PERIOD, MAXIMUM_PERIOD,
                ConsecutiveValidator.MINIMUM_THRESHOLD, MINIMUM_THRESHOLD,
                ConsecutiveValidator.CHECK_RETROACTIVELY, false);
        consecutiveValidator = new ConsecutiveValidator(thesaurus, propertySpecService, properties);
        Range<Instant> validationInterval = Range.openClosed(Instant.ofEpochSecond(START_VALIDATION), Instant.ofEpochSecond(END_VALIDATION));
        List<Instant> channelToList = mockChannelToList(validationInterval);
        when(channel.toList(any())).thenReturn(channelToList);
        List<IntervalReadingRecord> records = mockIntervalReadingsRecords(validationInterval);
        when(channel.getIntervalReadings(any())).thenReturn(records);
        consecutiveValidator.init(channel, readingType, validationInterval);
    }

    @Test
    public void testValidationWithMaxZeroIntervalInRetroactivelyPeriod() {
        ImmutableMap<String, Object> properties = ImmutableMap.of(ConsecutiveValidator.MINIMUM_PERIOD, MINIMUM_PERIOD,
                ConsecutiveValidator.MAXIMUM_PERIOD, MAXIMUM_PERIOD,
                ConsecutiveValidator.MINIMUM_THRESHOLD, MINIMUM_THRESHOLD,
                ConsecutiveValidator.CHECK_RETROACTIVELY, true);
        consecutiveValidator = new ConsecutiveValidator(thesaurus, propertySpecService, properties);
        Range<Instant> validationInterval = Range.openClosed(Instant.ofEpochSecond(START_VALIDATION), Instant.ofEpochSecond(END_VALIDATION));
        Range<Instant> validationIntervalWithRetroactivelyPeriod = Range.openClosed(Instant.ofEpochSecond(START_VALIDATION - MAXIMUM_PERIOD.getSeconds()), Instant.ofEpochSecond(END_VALIDATION));
        List<Instant> channelToList = mockChannelToList(validationIntervalWithRetroactivelyPeriod);
        when(channel.toList(any())).thenReturn(channelToList);
        List<IntervalReadingRecord> records = mockIntervalReadingsRecordsWithMaxZeroIntervalInRetroactivelyPeriod(validationIntervalWithRetroactivelyPeriod);
        when(channel.getIntervalReadings(any())).thenReturn(records);
        consecutiveValidator.init(channel, readingType, validationInterval);

        ValidationResult validationResult = consecutiveValidator.validate(recordAfterMaxZeroIntervalInRetroactivelyPeriod);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationWithShortZeroIntervalInRetroactivelyPeriod() {
        ImmutableMap<String, Object> properties = ImmutableMap.of(ConsecutiveValidator.MINIMUM_PERIOD, MINIMUM_PERIOD,
                ConsecutiveValidator.MAXIMUM_PERIOD, MAXIMUM_PERIOD,
                ConsecutiveValidator.MINIMUM_THRESHOLD, MINIMUM_THRESHOLD,
                ConsecutiveValidator.CHECK_RETROACTIVELY, true);
        consecutiveValidator = new ConsecutiveValidator(thesaurus, propertySpecService, properties);
        Range<Instant> validationInterval = Range.openClosed(Instant.ofEpochSecond(START_VALIDATION), Instant.ofEpochSecond(END_VALIDATION));
        Range<Instant> validationIntervalWithRetroactivelyPeriod = Range.openClosed(Instant.ofEpochSecond(START_VALIDATION - MAXIMUM_PERIOD.getSeconds()), Instant.ofEpochSecond(END_VALIDATION));
        List<Instant> channelToList = mockChannelToList(validationIntervalWithRetroactivelyPeriod);
        when(channel.toList(any())).thenReturn(channelToList);
        List<IntervalReadingRecord> records = mockIntervalReadingsRecordsWithShortZeroIntervalInRetroactivelyPeriod(validationIntervalWithRetroactivelyPeriod);
        when(channel.getIntervalReadings(any())).thenReturn(records);
        consecutiveValidator.init(channel, readingType, validationInterval);

        ValidationResult validationResult = consecutiveValidator.validate(recordAfterShortZeroIntervalInRetroactivelyPeriod);

        assertThat(validationResult).isEqualTo(ValidationResult.SUSPECT);
    }

    @Test
    public void testChangeValidationResultOfRecordsFromShortZeroIntervalInRetroactivelyPeriod() {
        ImmutableMap<String, Object> properties = ImmutableMap.of(ConsecutiveValidator.MINIMUM_PERIOD, MINIMUM_PERIOD,
                ConsecutiveValidator.MAXIMUM_PERIOD, MAXIMUM_PERIOD,
                ConsecutiveValidator.MINIMUM_THRESHOLD, MINIMUM_THRESHOLD,
                ConsecutiveValidator.CHECK_RETROACTIVELY, true);
        consecutiveValidator = new ConsecutiveValidator(thesaurus, propertySpecService, properties);
        Range<Instant> validationInterval = Range.openClosed(Instant.ofEpochSecond(START_VALIDATION), Instant.ofEpochSecond(END_VALIDATION));
        Range<Instant> validationIntervalWithRetroactivelyPeriod = Range.openClosed(Instant.ofEpochSecond(START_VALIDATION - MAXIMUM_PERIOD.getSeconds()), Instant.ofEpochSecond(END_VALIDATION));
        List<Instant> channelToList = mockChannelToList(validationIntervalWithRetroactivelyPeriod);
        when(channel.toList(any())).thenReturn(channelToList);
        List<IntervalReadingRecord> records = mockIntervalReadingsRecordsWithShortZeroIntervalInRetroactivelyPeriod(validationIntervalWithRetroactivelyPeriod);
        when(channel.getIntervalReadings(any())).thenReturn(records);
        consecutiveValidator.init(channel, readingType, validationInterval);
        Map<Instant, ValidationResult> expectedResultMap = createExpectedResultMap();

        Map<Instant, ValidationResult> resultMap = consecutiveValidator.finish();

        assertThat(resultMap).isEqualTo(expectedResultMap);
    }

    @Test
    public void testValidationRecordFromShortZeroInterval() {
        ValidationResult validationResult = consecutiveValidator.validate(recordFromShortZeroInterval);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationRecordNotFromZeroInterval() {
        ValidationResult validationResult = consecutiveValidator.validate(recordOutsideZeroInterval);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test
    public void testValidationRecordFromZeroInterval() {
        ValidationResult validationResult = consecutiveValidator.validate(recordFromZeroInterval);

        assertThat(validationResult).isEqualTo(ValidationResult.SUSPECT);
    }

    @Test
    public void testValidationRecordFromLongZeroInterval() {
        ValidationResult validationResult = consecutiveValidator.validate(recordFromLongZeroInterval);

        assertThat(validationResult).isEqualTo(ValidationResult.VALID);
    }

    @Test(expected = MissingRequiredProperty.class)
    public void testConstructionWithoutRequiredProperty() {
        ImmutableMap<String, Object> properties = ImmutableMap.of(
                ConsecutiveValidator.MAXIMUM_PERIOD, MAXIMUM_PERIOD,
                ConsecutiveValidator.MINIMUM_THRESHOLD, MINIMUM_THRESHOLD
        );
        consecutiveValidator = new ConsecutiveValidator(thesaurus, propertySpecService, properties);
    }

    @Test
    public void testGetDefaultFormat() {
        assertThat(consecutiveValidator.getDefaultFormat()).isEqualTo(CONSECUTIVE_ZERO);
    }

    @Test
    public void testNlsKey() {
        assertThat(consecutiveValidator.getNlsKey()).isEqualTo(SimpleNlsKey.key(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN, ConsecutiveValidator.class.getName()));
    }

    @Test
    public void testPropertyDisplayName() {
        // Business method
        String minPeriodDisplayName = consecutiveValidator.getDisplayName(ConsecutiveValidator.MINIMUM_PERIOD);
        String maxPeriodDisplayName = consecutiveValidator.getDisplayName(ConsecutiveValidator.MAXIMUM_PERIOD);
        String minThresholdDisplayName = consecutiveValidator.getDisplayName(ConsecutiveValidator.MINIMUM_THRESHOLD);
        String checkRetroactivelyDisplayName = consecutiveValidator.getDisplayName(ConsecutiveValidator.CHECK_RETROACTIVELY);

        // Asserts
        assertThat(minPeriodDisplayName).isEqualTo("Minimum period");
        assertThat(maxPeriodDisplayName).isEqualTo("Maximum period");
        assertThat(minThresholdDisplayName).isEqualTo("Minimum threshold");
        assertThat(checkRetroactivelyDisplayName).isEqualTo("Check retroactively");
    }

    @Test
    public void testFinish() {
        assertThat(consecutiveValidator.finish()).isEmpty();
    }

    @Test
    public void testGetReadingQualityTypeCode() {
        assertThat(consecutiveValidator.getReadingQualityCodeIndex().isPresent()).isFalse();
    }

    @Test
    public void testGetSupportedApplications() {
        assertThat(consecutiveValidator.getSupportedQualityCodeSystems()).containsOnly(QualityCodeSystem.MDC, QualityCodeSystem.MDM);
    }

    private List<Instant> mockChannelToList(Range<Instant> interval){
        List<Instant> channelToList = new ArrayList<>();
        long timeSecond = interval.lowerEndpoint().plusSeconds(RECORD_INTERVAL).getEpochSecond();
        while (timeSecond < interval.upperEndpoint().getEpochSecond()){
            channelToList.add(Instant.ofEpochSecond(timeSecond));
            timeSecond += RECORD_INTERVAL;
        }
        return channelToList;
    }

    private List<IntervalReadingRecord> mockIntervalReadingsRecords(Range<Instant> interval){
        List<IntervalReadingRecord> records = new ArrayList<>();
        //add missing record with a timestamp equal to the beginning of the interval, because this entry is not in the interval
        Instant timeSecond = interval.lowerEndpoint().plusSeconds(RECORD_INTERVAL);
        //add record that is part of the interval that is shorter than the minimum period
        recordFromShortZeroInterval = mock(IntervalReadingRecord.class);
        when(recordFromShortZeroInterval.getTimeStamp()).thenReturn(timeSecond);
        when(recordFromShortZeroInterval.getValue()).thenReturn(BELOW_MINIMUM);
        records.add(recordFromShortZeroInterval);

        Instant endZeroInterval = timeSecond.plus(MINIMUM_PERIOD.asTemporalAmount());
        timeSecond = timeSecond.plusSeconds(RECORD_INTERVAL);
        timeSecond = addZeroInterval(records, timeSecond, endZeroInterval);
        //add record having a value greater than the minimum threshold
        recordOutsideZeroInterval = mock(IntervalReadingRecord.class);
        when(recordOutsideZeroInterval.getTimeStamp()).thenReturn(timeSecond);
        when(recordOutsideZeroInterval.getValue()).thenReturn(ABOVE_MINIMUM);
        records.add(recordOutsideZeroInterval);
        //add record that is part of the zero interval with a suitable length
        timeSecond = timeSecond.plusSeconds(RECORD_INTERVAL);
        recordFromZeroInterval = mock(IntervalReadingRecord.class);
        when(recordFromZeroInterval.getTimeStamp()).thenReturn(timeSecond);
        when(recordFromZeroInterval.getValue()).thenReturn(BELOW_MINIMUM);
        records.add(recordFromZeroInterval);

        timeSecond = timeSecond.plusSeconds(RECORD_INTERVAL);
        endZeroInterval = timeSecond.plus(MINIMUM_PERIOD.asTemporalAmount()).plusSeconds(RECORD_INTERVAL);
        timeSecond = addZeroInterval(records, timeSecond, endZeroInterval);
        //add record that will interrupt the zero interval
        IntervalReadingRecord recordWithZeroValue = mock(IntervalReadingRecord.class);
        when(recordWithZeroValue.getTimeStamp()).thenReturn(timeSecond);
        when(recordWithZeroValue.getValue()).thenReturn(ABOVE_MINIMUM);
        records.add(recordWithZeroValue);

        //add record that is part of the interval that is longer than the maximum period
        timeSecond = timeSecond.plusSeconds(RECORD_INTERVAL);
        recordFromLongZeroInterval = mock(IntervalReadingRecord.class);
        when(recordFromLongZeroInterval.getTimeStamp()).thenReturn(timeSecond);
        when(recordFromLongZeroInterval.getValue()).thenReturn(BELOW_MINIMUM);
        records.add(recordFromLongZeroInterval);

        timeSecond = timeSecond.plusSeconds(RECORD_INTERVAL);
        endZeroInterval = timeSecond.plus(MAXIMUM_PERIOD.asTemporalAmount()).plusSeconds(RECORD_INTERVAL);
        addZeroInterval(records, timeSecond, endZeroInterval);
        return records;
    }

    private List<IntervalReadingRecord> mockIntervalReadingsRecordsWithMaxZeroIntervalInRetroactivelyPeriod(Range<Instant> interval){
        List<IntervalReadingRecord> records = new ArrayList<>();
        //add missing record with a timestamp equal to the beginning of the interval, because this entry is not in the interval
        Instant timeSecond = interval.lowerEndpoint().plusSeconds(RECORD_INTERVAL);
        Instant endZeroInterval = timeSecond.plus(MAXIMUM_PERIOD.asTemporalAmount()).plusSeconds(RECORD_INTERVAL);
        //add a zero interval with the maximum length
        timeSecond = addZeroInterval(records, timeSecond, endZeroInterval);
        //add first record after zero interval in retroactively period
        recordAfterMaxZeroIntervalInRetroactivelyPeriod = mock(IntervalReadingRecord.class);
        when(recordAfterMaxZeroIntervalInRetroactivelyPeriod.getTimeStamp()).thenReturn(timeSecond);
        when(recordAfterMaxZeroIntervalInRetroactivelyPeriod.getValue()).thenReturn(BELOW_MINIMUM);
        records.add(recordAfterMaxZeroIntervalInRetroactivelyPeriod);
        return records;
    }

    private List<IntervalReadingRecord> mockIntervalReadingsRecordsWithShortZeroIntervalInRetroactivelyPeriod(Range<Instant> interval){
        List<IntervalReadingRecord> records = new ArrayList<>();
        Instant timeSecond = interval.lowerEndpoint().plus(MAXIMUM_PERIOD.asTemporalAmount()).minus(MINIMUM_PERIOD.asTemporalAmount());
        Instant endZeroInterval = timeSecond.plus(MINIMUM_PERIOD.asTemporalAmount()).plusSeconds(RECORD_INTERVAL);
        //add a short zero interval at the end of the retroactively period
        timeSecond = addZeroInterval(records, timeSecond, endZeroInterval);
        instantsFromShortZeroInterval = records.stream().map(BaseReading::getTimeStamp).collect(Collectors.toList());
        //add first record after zero interval in retroactively period
        recordAfterShortZeroIntervalInRetroactivelyPeriod = mock(IntervalReadingRecord.class);
        when(recordAfterShortZeroIntervalInRetroactivelyPeriod.getTimeStamp()).thenReturn(timeSecond);
        when(recordAfterShortZeroIntervalInRetroactivelyPeriod.getValue()).thenReturn(BELOW_MINIMUM);
        records.add(recordAfterShortZeroIntervalInRetroactivelyPeriod);
        return records;
    }

    private Instant addZeroInterval(List<IntervalReadingRecord> records, Instant timeSecond, Instant endZeroInterval){
        while (timeSecond.compareTo(endZeroInterval) < 0){
            IntervalReadingRecord recordWithZeroValue = mock(IntervalReadingRecord.class);
            when(recordWithZeroValue.getTimeStamp()).thenReturn(timeSecond);
            when(recordWithZeroValue.getValue()).thenReturn(BELOW_MINIMUM);
            records.add(recordWithZeroValue);
            timeSecond = timeSecond.plusSeconds(RECORD_INTERVAL);
        }
        return timeSecond;
    }

    private  Map<Instant, ValidationResult> createExpectedResultMap(){
        return instantsFromShortZeroInterval.stream().collect(Collectors.toMap(Function.identity(), instant -> ValidationResult.SUSPECT));
    }
}
