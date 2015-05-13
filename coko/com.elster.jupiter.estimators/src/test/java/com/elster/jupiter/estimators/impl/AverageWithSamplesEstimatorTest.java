package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.estimation.BulkAdvanceReadingsSettings;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.estimation.ReadingTypeAdvanceReadingsSettings;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.AllRelativePeriod;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AverageWithSamplesEstimatorTest {

    private static final ZonedDateTime ESTIMATABLE_TIME = ZonedDateTime.of(2004, 4, 13, 14, 15, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime START = ZonedDateTime.of(2001, 1, 1, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime LAST_CHECKED = ZonedDateTime.of(2004, 4, 20, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo());

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private ValidationService validationService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private EstimationBlock block;
    private Estimatable estimatable, estimatable2;
    @Mock
    private ReadingType readingType, bulkReadingType, advanceReadingType;
    @Mock
    private Channel channel;
    @Mock
    private MeterActivation meterActivation;

    @Before
    public void setUp() {
        estimatable = new EstimatableImpl(ESTIMATABLE_TIME.toInstant());
        estimatable2 = new EstimatableImpl(ESTIMATABLE_TIME.plusMinutes(15).toInstant());
        doReturn(asList(estimatable)).when(block).estimatables();
        doReturn(readingType).when(block).getReadingType();
        doReturn(channel).when(block).getChannel();
        doReturn(meterActivation).when(channel).getMeterActivation();
        doReturn(START.toInstant()).when(meterActivation).getStart();
        doReturn(Optional.of(LAST_CHECKED.toInstant())).when(validationService).getLastChecked(channel);
        doReturn(buildReadings()).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        doReturn(TimeZoneNeutral.getMcMurdo()).when(channel).getZoneId();
        doReturn(readingType).when(block).getReadingType();
        doReturn(Optional.of(bulkReadingType)).when(readingType).getBulkReadingType();
        doAnswer(invocation -> ((Instant) invocation.getArguments()[0]).minus(15, ChronoUnit.MINUTES)).when(channel).getPreviousDateTime(any());
    }

    private List<BaseReadingRecord> buildReadings() {
        Long[] values = new Long[] {4L, 100L, 100L, 5L, 200L, null, 6L, 100L, 7L};
        List<BaseReadingRecord> readingRecords = IntStream.rangeClosed(-4, 4)
                .mapToObj(i -> mockReading(ESTIMATABLE_TIME.plusDays(7 * i).toInstant(), values[i + 4]))
                .collect(Collectors.toCollection(ArrayList::new));
        readingRecords.add(1, mockReading(ESTIMATABLE_TIME.minusDays(25).toInstant(), 1000L));
        doReturn(Collections.emptyList()).when(channel).findReadingQuality(any(Instant.class));
        doReturn(singletonList(confirmedReadingQuality())).when(channel).findReadingQuality(ESTIMATABLE_TIME.minusDays(21).toInstant());
        doReturn(singletonList(estimatedReadingQuality())).when(channel).findReadingQuality(ESTIMATABLE_TIME.minusDays(14).toInstant());
        doReturn(singletonList(editReadingQuality())).when(channel).findReadingQuality(ESTIMATABLE_TIME.plusDays(21).toInstant());
        return readingRecords;
    }

    private List<BaseReadingRecord> buildTwiceTheReadings() {
        Long[] values = new Long[] {4L, 100L, 100L, 5L, 200L, null, 6L, 100L, 7L};
        List<BaseReadingRecord> readingRecords = IntStream.rangeClosed(-4, 4)
                .mapToObj(i -> ImmutableList.of(mockReading(ESTIMATABLE_TIME.plusDays(7 * i).toInstant(), values[i + 4]), mockReading(ESTIMATABLE_TIME.plusDays(7 * i).plusMinutes(15).toInstant(), values[i + 4])))
                .flatMap(List::stream)
                .collect(Collectors.toCollection(ArrayList::new));
        doReturn(Collections.emptyList()).when(channel).findReadingQuality(any(Instant.class));
        doReturn(singletonList(confirmedReadingQuality())).when(channel).findReadingQuality(ESTIMATABLE_TIME.minusDays(21).toInstant());
        doReturn(singletonList(estimatedReadingQuality())).when(channel).findReadingQuality(ESTIMATABLE_TIME.minusDays(14).toInstant());
        doReturn(singletonList(editReadingQuality())).when(channel).findReadingQuality(ESTIMATABLE_TIME.plusDays(21).toInstant());
        doReturn(singletonList(confirmedReadingQuality())).when(channel).findReadingQuality(ESTIMATABLE_TIME.minusDays(21).plusMinutes(15).toInstant());
        doReturn(singletonList(estimatedReadingQuality())).when(channel).findReadingQuality(ESTIMATABLE_TIME.minusDays(14).plusMinutes(15).toInstant());
        doReturn(singletonList(editReadingQuality())).when(channel).findReadingQuality(ESTIMATABLE_TIME.plusDays(21).plusMinutes(15).toInstant());
        return readingRecords;
    }

    private ReadingQualityRecord suspectReadingQuality() {
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(readingQualityRecord.isActual()).thenReturn(true);
        when(readingQualityRecord.getReadingType()).thenReturn(readingType);
        when(readingQualityRecord.isSuspect()).thenReturn(true);
        return readingQualityRecord;
    }

    private ReadingQualityRecord editReadingQuality() {
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(readingQualityRecord.isActual()).thenReturn(true);
        when(readingQualityRecord.getReadingType()).thenReturn(readingType);
        when(readingQualityRecord.hasEditCategory()).thenReturn(true);
        return readingQualityRecord;
    }

    private ReadingQualityRecord confirmedReadingQuality() {
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(readingQualityRecord.isActual()).thenReturn(true);
        when(readingQualityRecord.getReadingType()).thenReturn(readingType);
        when(readingQualityRecord.isConfirmed()).thenReturn(true);
        return readingQualityRecord;
    }

    private ReadingQualityRecord estimatedReadingQuality() {
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(readingQualityRecord.isActual()).thenReturn(true);
        when(readingQualityRecord.getReadingType()).thenReturn(readingType);
        when(readingQualityRecord.hasEstimatedCategory()).thenReturn(true);
        return readingQualityRecord;
    }

    private BaseReadingRecord mockReading(Instant instant, Long value) {
        BaseReadingRecord reading = mock(BaseReadingRecord.class);
        doReturn(instant).when(reading).getTimeStamp();
        Quantity amount = Optional.ofNullable(value)
                .map(BigDecimal::valueOf)
                .map(Unit.WATT_HOUR::amount)
                .orElse(null);
        doReturn(amount).when(reading).getQuantity(readingType);
        return reading;
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testEstimateSuccess() {
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, new AllRelativePeriod())
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, NoneAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, props);

        estimator.init();

        EstimationResult result = estimator.estimate(asList(block));

        assertThat(result.estimated()).contains(block);
        assertThat(estimatable.getEstimation()).isEqualTo(BigDecimal.valueOf(5000000, 6));
    }

    @Test
    public void testEstimateSuccessIfJustEnoughSamples() {
        List<BaseReadingRecord> readingRecords = buildReadings();
        readingRecords = readingRecords.subList(1, readingRecords.size() - 2);
        doReturn(readingRecords).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, new AllRelativePeriod())
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, NoneAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, props);

        estimator.init();

        EstimationResult result = estimator.estimate(asList(block));

        assertThat(result.estimated()).contains(block);
        assertThat(estimatable.getEstimation()).isEqualTo(BigDecimal.valueOf(5500000, 6));
    }

    @Test
    public void testEstimateSuccessSuspectInsteadOfMissing() {
        List<BaseReadingRecord> readingRecords = buildReadings();
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(100))).when(readingRecords.get(6)).getQuantity(readingType);
        doReturn(readingRecords).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        doReturn(singletonList(suspectReadingQuality())).when(channel).findReadingQuality(ESTIMATABLE_TIME.plusDays(7).toInstant());

        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, new AllRelativePeriod())
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, NoneAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, props);

        estimator.init();

        EstimationResult result = estimator.estimate(asList(block));

        assertThat(result.estimated()).contains(block);
        assertThat(estimatable.getEstimation()).isEqualTo(BigDecimal.valueOf(5000000, 6));
    }

    @Test
    public void testEstimateFailIfTooManySuspects() {
        Estimatable estimatableExtra = mock(Estimatable.class);
        doReturn(asList(estimatableExtra, estimatable)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 1L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, new AllRelativePeriod())
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, NoneAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, props);

        estimator.init();

        EstimationResult result = estimator.estimate(asList(block));

        assertThat(result.estimated()).isEmpty();
        assertThat(result.remainingToBeEstimated()).containsExactly(block);
    }

    @Test
    public void testEstimateFailIfNotEnoughSamples() {
        List<BaseReadingRecord> readingRecords = buildReadings();
        readingRecords = readingRecords.subList(1, readingRecords.size() - 3);
        doReturn(readingRecords).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, new AllRelativePeriod())
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, NoneAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, props);

        estimator.init();

        EstimationResult result = estimator.estimate(asList(block));

        assertThat(result.estimated()).isEmpty();
        assertThat(result.remainingToBeEstimated()).containsExactly(block);
    }

    @Test
    public void testEstimateFailUsingBulkToScaleSinceBlockSizeIs1() {
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, new AllRelativePeriod())
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, BulkAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, props);

        estimator.init();

        EstimationResult result = estimator.estimate(asList(block));

        assertThat(result.estimated()).isEmpty();
        assertThat(result.remainingToBeEstimated()).containsExactly(block);
    }

    @Test
    public void testEstimateSuccessUsingBulkToScale() {
        doReturn(buildTwiceTheReadings()).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        BaseReadingRecord preBulkReading = mock(BaseReadingRecord.class);
        BaseReadingRecord postBulkReading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(14))).when(preBulkReading).getQuantity(bulkReadingType);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(22))).when(postBulkReading).getQuantity(bulkReadingType);
        doReturn(Optional.of(preBulkReading)).when(channel).getReading(ESTIMATABLE_TIME.minusMinutes(15).toInstant());
        doReturn(Optional.of(postBulkReading)).when(channel).getReading(ESTIMATABLE_TIME.plusMinutes(15).toInstant());


        doReturn(asList(estimatable, estimatable2)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, new AllRelativePeriod())
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, BulkAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, props);

        estimator.init();

        EstimationResult result = estimator.estimate(asList(block));

        assertThat(result.estimated()).contains(block);
        assertThat(estimatable.getEstimation()).isEqualTo(BigDecimal.valueOf(4000000, 6));
    }

    @Test
    public void testEstimateSuccessUsingAdvanceToScale() {
        doReturn(buildTwiceTheReadings()).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        BaseReadingRecord preAdvanceReading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(14))).when(preAdvanceReading).getQuantity(advanceReadingType);
        BaseReadingRecord postAdvanceReading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(32))).when(postAdvanceReading).getQuantity(advanceReadingType);
        doReturn(singletonList(preAdvanceReading)).when(meterActivation).getReadingsBefore(ESTIMATABLE_TIME.toInstant(), advanceReadingType, 1);
        doReturn(singletonList(postAdvanceReading)).when(meterActivation).getReadings(Range.atLeast(ESTIMATABLE_TIME.plusMinutes(15).toInstant()), advanceReadingType);
        doReturn(ESTIMATABLE_TIME.minusMinutes(35).toInstant()).when(preAdvanceReading).getTimeStamp();
        doReturn(ESTIMATABLE_TIME.plusMinutes(35).toInstant()).when(postAdvanceReading).getTimeStamp();
        BaseReadingRecord pre1Reading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(3))).when(pre1Reading).getQuantity(readingType);
        doReturn(ESTIMATABLE_TIME.minusMinutes(30).toInstant()).when(pre1Reading).getTimeStamp();
        BaseReadingRecord pre2Reading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(3))).when(pre2Reading).getQuantity(readingType);
        doReturn(ESTIMATABLE_TIME.minusMinutes(15).toInstant()).when(pre2Reading).getTimeStamp();
        doReturn(asList(pre1Reading, pre2Reading)).when(channel).getReadings(Range.openClosed(ESTIMATABLE_TIME.minusMinutes(35).toInstant(), ESTIMATABLE_TIME.minusMinutes(15).toInstant()));
        BaseReadingRecord post1Reading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(4))).when(post1Reading).getQuantity(readingType);
        doReturn(ESTIMATABLE_TIME.plusMinutes(30).toInstant()).when(post1Reading).getTimeStamp();
        BaseReadingRecord post2Reading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(6))).when(post2Reading).getQuantity(readingType);
        doReturn(ESTIMATABLE_TIME.plusMinutes(45).toInstant()).when(post2Reading).getTimeStamp();
        doReturn(asList(post1Reading, post2Reading)).when(channel).getReadings(Range.openClosed(ESTIMATABLE_TIME.plusMinutes(15).toInstant(), ESTIMATABLE_TIME.plusMinutes(45).toInstant()));
        doReturn(asList(ESTIMATABLE_TIME.plusMinutes(30).toInstant())).when(channel).toList(Range.openClosed(ESTIMATABLE_TIME.plusMinutes(15).toInstant(), ESTIMATABLE_TIME.plusMinutes(35).toInstant()));
        doReturn(ESTIMATABLE_TIME.plusMinutes(45).toInstant()).when(channel).getNextDateTime(ESTIMATABLE_TIME.plusMinutes(30).toInstant());
        doReturn(asList(ESTIMATABLE_TIME.minusMinutes(30).toInstant(), ESTIMATABLE_TIME.minusMinutes(15).toInstant())).when(channel).toList(Range.openClosed(ESTIMATABLE_TIME.minusMinutes(35).toInstant(), ESTIMATABLE_TIME.minusMinutes(15).toInstant()));
        doReturn(Optional.of(Duration.ofMinutes(15))).when(channel).getIntervalLength();

        doReturn(asList(estimatable, estimatable2)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, new AllRelativePeriod())
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType))
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, props);

        estimator.init();

        EstimationResult result = estimator.estimate(asList(block));

        assertThat(result.estimated()).contains(block);
        assertThat(estimatable.getEstimation()).isEqualTo(BigDecimal.valueOf(4000000, 6));
    }


    private static class EstimatableImpl implements Estimatable {

        private final Instant timestamp;
        private BigDecimal bigDecimal;

        private EstimatableImpl(Instant timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public Instant getTimestamp() {
            return timestamp;
        }

        @Override
        public void setEstimation(BigDecimal value) {
            bigDecimal = value;
        }

        @Override
        public BigDecimal getEstimation() {
            return bigDecimal;
        }
    }
}