package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.MockitoExtension;
import com.elster.jupiter.devtools.tests.assertions.JupiterAssertions;
import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.estimation.BulkAdvanceReadingsSettings;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleProperties;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.NoneAdvanceReadingsSettings;
import com.elster.jupiter.estimation.ReadingTypeAdvanceReadingsSettings;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.impl.AllRelativePeriod;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.estimators.impl.EqualDistribution.ADVANCE_READINGS_SETTINGS;
import static com.elster.jupiter.estimators.impl.EqualDistribution.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AverageWithSamplesEstimatorTest {
    private static final Set<QualityCodeSystem> SYSTEMS = Estimator.qualityCodeSystemsToTakeIntoAccount(QualityCodeSystem.MDC);
    private static final Logger LOGGER = Logger.getLogger(AverageWithSamplesEstimatorTest.class.getName());
    private static final ZonedDateTime ESTIMABLE_TIME = ZonedDateTime.of(2004, 4, 13, 14, 15, 0, 0, TimeZoneNeutral.getMcMurdo());
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
    private Estimatable estimable, estimable2;
    @Mock
    private ReadingType readingType, bulkReadingType, advanceReadingType, deltaReadingType;
    @Mock
    private Channel channel, otherChannel;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private TimeService timeService;
    @Mock
    private CimChannel deltaCimChannel, bulkCimChannel, advanceCimChannel;
    @Mock
    private Meter meter;
    @Mock
    private ReadingQualityRecord suspect, overflow, estimated;

    private LogRecorder logRecorder;

    @Before
    public void setUp() {
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        estimable = new EstimableImpl(ESTIMABLE_TIME.toInstant());
        estimable2 = new EstimableImpl(ESTIMABLE_TIME.plusMinutes(15).toInstant());
        doReturn(AllRelativePeriod.INSTANCE).when(timeService).getAllRelativePeriod();
        doReturn(singletonList(estimable)).when(block).estimatables();
        doReturn(readingType).when(block).getReadingType();
        doReturn(channel).when(block).getChannel();
        doReturn(deltaCimChannel).when(block).getCimChannel();
        doReturn(channelsContainer).when(channel).getChannelsContainer();

        doReturn(Optional.of(meter)).when(channelsContainer).getMeter();
        doReturn(asList(channel, otherChannel)).when(channelsContainer).getChannels();
        doReturn(asList(deltaReadingType, bulkReadingType)).when(channel).getReadingTypes();
        doReturn(singletonList(advanceReadingType)).when(otherChannel).getReadingTypes();
        doReturn(Optional.of(deltaCimChannel)).when(channel).getCimChannel(readingType);
        doReturn(Optional.of(bulkCimChannel)).when(channel).getCimChannel(bulkReadingType);
        doReturn(Optional.of(deltaCimChannel)).when(channel).getCimChannel(deltaReadingType);
        doReturn(Optional.of(advanceCimChannel)).when(otherChannel).getCimChannel(advanceReadingType);

        doReturn(START.toInstant()).when(channelsContainer).getStart();
        doReturn(Optional.of(LAST_CHECKED.toInstant())).when(validationService).getLastChecked(channel);
        doReturn(buildReadings()).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        doReturn(TimeZoneNeutral.getMcMurdo()).when(channel).getZoneId();
        doReturn(Optional.of(bulkReadingType)).when(readingType).getBulkReadingType();
        doReturn(false).when(deltaReadingType).isCumulative();
        doReturn(true).when(deltaReadingType).isRegular();
        doAnswer(invocation -> ((Instant) invocation.getArguments()[0]).minus(15, ChronoUnit.MINUTES)).when(channel).getPreviousDateTime(any());

        doReturn("readingType").when(readingType).getMRID();
        doReturn("deltaReadingType").when(deltaReadingType).getMRID();
        doReturn("bulkReadingType").when(bulkReadingType).getMRID();
        doReturn("advanceReadingType").when(advanceReadingType).getMRID();
        doReturn("meter").when(meter).getMRID();

        doReturn(true).when(readingType).isRegular();
        doReturn(true).when(deltaReadingType).isRegular();
        doReturn(true).when(bulkReadingType).isCumulative();
        doReturn(true).when(bulkReadingType).isRegular();

        logRecorder = new LogRecorder(Level.ALL);
        LOGGER.addHandler(logRecorder);
        LoggingContext.get().with("rule", "rule");
    }

    private List<BaseReadingRecord> buildReadings() {
        Long[] values = new Long[] {4L, 100000L, 100L, 100L, 5L, 200L, null, 6L, 100L, 7L};
        List<BaseReadingRecord> readingRecords = IntStream.rangeClosed(-5, 4)
                .mapToObj(i -> mockReading(ESTIMABLE_TIME.plusDays(7 * i).toInstant(), values[i + 5]))
                .collect(Collectors.toCollection(ArrayList::new));
        readingRecords.add(1, mockReading(ESTIMABLE_TIME.minusDays(25).toInstant(), 1000L));
        doReturn(Collections.emptyList()).when(deltaCimChannel).findReadingQualities(any(Instant.class));
        doReturn(asList( // doesn't affect the result, just to test the filtration
                suspectReadingQuality(QualityCodeSystem.MDC, false),
                confirmedReadingQuality(QualityCodeSystem.MDC, false),
                estimatedReadingQuality(QualityCodeSystem.MDC, false),
                editReadingQuality(QualityCodeSystem.MDC, false),
                suspectReadingQuality(QualityCodeSystem.MDM, true),
                confirmedReadingQuality(QualityCodeSystem.OTHER, true),
                estimatedReadingQuality(QualityCodeSystem.NOTAPPLICABLE, true),
                editReadingQuality(QualityCodeSystem.EXTERNAL, true)
        )).when(deltaCimChannel).findReadingQualities(ESTIMABLE_TIME.minusDays(7).toInstant());
        doReturn(singletonList(suspectReadingQuality(QualityCodeSystem.MDC, true)))
                .when(deltaCimChannel).findReadingQualities(ESTIMABLE_TIME.minusDays(28).toInstant());
        doReturn(singletonList(confirmedReadingQuality(QualityCodeSystem.MDC, true)))
                .when(deltaCimChannel).findReadingQualities(ESTIMABLE_TIME.minusDays(21).toInstant());
        doReturn(singletonList(estimatedReadingQuality(QualityCodeSystem.MDC, true)))
                .when(deltaCimChannel).findReadingQualities(ESTIMABLE_TIME.minusDays(14).toInstant());
        doReturn(singletonList(editReadingQuality(QualityCodeSystem.MDC, true)))
                .when(deltaCimChannel).findReadingQualities(ESTIMABLE_TIME.plusDays(21).toInstant());
        return readingRecords;
    }

    private List<BaseReadingRecord> buildTwiceTheReadings() {
        Long[] values = new Long[] {4L, 100L, 100L, 5L, 200L, null, 6L, 100L, 7L};
        List<BaseReadingRecord> readingRecords = IntStream.rangeClosed(-4, 4)
                .mapToObj(i -> ImmutableList.of(
                        mockReading(ESTIMABLE_TIME.plusDays(7 * i).toInstant(), values[i + 4]),
                        mockReading(ESTIMABLE_TIME.plusDays(7 * i).plusMinutes(15).toInstant(), values[i + 4])))
                .flatMap(List::stream)
                .collect(Collectors.toCollection(ArrayList::new));
        doReturn(Collections.emptyList()).when(deltaCimChannel).findReadingQualities(any(Instant.class));
        doReturn(singletonList(confirmedReadingQuality(QualityCodeSystem.ENDDEVICE, true)))
                .when(deltaCimChannel).findReadingQualities(ESTIMABLE_TIME.minusDays(21).toInstant());
        doReturn(singletonList(estimatedReadingQuality(QualityCodeSystem.ENDDEVICE, true)))
                .when(deltaCimChannel).findReadingQualities(ESTIMABLE_TIME.minusDays(14).toInstant());
        doReturn(singletonList(editReadingQuality(QualityCodeSystem.ENDDEVICE, true)))
                .when(deltaCimChannel).findReadingQualities(ESTIMABLE_TIME.plusDays(21).toInstant());
        doReturn(singletonList(confirmedReadingQuality(QualityCodeSystem.ENDDEVICE, true)))
                .when(deltaCimChannel).findReadingQualities(ESTIMABLE_TIME.minusDays(21).plusMinutes(15).toInstant());
        doReturn(singletonList(estimatedReadingQuality(QualityCodeSystem.ENDDEVICE, true)))
                .when(deltaCimChannel).findReadingQualities(ESTIMABLE_TIME.minusDays(14).plusMinutes(15).toInstant());
        doReturn(singletonList(editReadingQuality(QualityCodeSystem.ENDDEVICE, true)))
                .when(deltaCimChannel).findReadingQualities(ESTIMABLE_TIME.plusDays(21).plusMinutes(15).toInstant());
        return readingRecords;
    }

    private ReadingQualityRecord suspectReadingQuality(QualityCodeSystem system, boolean actual) {
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(readingQualityRecord.isActual()).thenReturn(actual);
        when(readingQualityRecord.getType()).thenReturn(ReadingQualityType.of(system, QualityCodeIndex.SUSPECT));
        return readingQualityRecord;
    }

    private ReadingQualityRecord editReadingQuality(QualityCodeSystem system, boolean actual) {
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(readingQualityRecord.isActual()).thenReturn(actual);
        when(readingQualityRecord.getType()).thenReturn(ReadingQualityType.of(system, QualityCodeIndex.EDITGENERIC));
        return readingQualityRecord;
    }

    private ReadingQualityRecord confirmedReadingQuality(QualityCodeSystem system, boolean actual) {
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(readingQualityRecord.isActual()).thenReturn(actual);
        when(readingQualityRecord.getType()).thenReturn(ReadingQualityType.of(system, QualityCodeIndex.ACCEPTED));
        return readingQualityRecord;
    }

    private ReadingQualityRecord estimatedReadingQuality(QualityCodeSystem system, boolean actual) {
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(readingQualityRecord.isActual()).thenReturn(actual);
        when(readingQualityRecord.getType()).thenReturn(ReadingQualityType.of(system, QualityCodeIndex.ESTIMATEGENERIC));
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
        LoggingContext.get().close();
        LOGGER.removeHandler(logRecorder);
    }

    @Test
    public void testEstimatePasses() {
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, NoneAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init();

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).contains(block);
        assertThat(estimable.getEstimation()).isEqualTo(BigDecimal.valueOf(6000000, 6));
    }

    @Test
    public void testEstimatePassesIfJustEnoughSamples() {
        List<BaseReadingRecord> readingRecords = buildReadings();
        readingRecords = readingRecords.subList(1, readingRecords.size() - 2);
        doReturn(readingRecords).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, NoneAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init();

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).contains(block);
        assertThat(estimable.getEstimation()).isEqualTo(BigDecimal.valueOf(5500000, 6));
    }

    @Test
    public void testEstimatePassesSuspectInsteadOfMissing() {
        List<BaseReadingRecord> readingRecords = buildReadings();
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(100))).when(readingRecords.get(7)).getQuantity(readingType);
        doReturn(readingRecords).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        doReturn(singletonList(suspectReadingQuality(QualityCodeSystem.MDC, true)))
                .when(deltaCimChannel).findReadingQualities(ESTIMABLE_TIME.plusDays(7).toInstant());

        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, NoneAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init();

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).contains(block);
        assertThat(estimable.getEstimation()).isEqualTo(BigDecimal.valueOf(6000000, 6));
    }

    @Test
    public void testEstimateFailsIfTooManySuspects() {
        Estimatable estimableExtra = mock(Estimatable.class);

        doReturn(ESTIMABLE_TIME.minusMinutes(15).toInstant()).when(estimableExtra).getTimestamp();

        doReturn(asList(estimableExtra, estimable)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 1L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, NoneAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init(LOGGER);

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).isEmpty();
        assertThat(result.remainingToBeEstimated()).containsExactly(block);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("suspects, which exceeds the maximum of")).atLevel(Level.INFO);
    }

    @Test
    public void testEstimateFailsIfNotEnoughSamples() {
        List<BaseReadingRecord> readingRecords = buildReadings();
        readingRecords = readingRecords.subList(1, readingRecords.size() - 3);
        doReturn(readingRecords).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, NoneAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init(LOGGER);

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).isEmpty();
        assertThat(result.remainingToBeEstimated()).containsExactly(block);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("since not enough samples are found")).atLevel(Level.INFO);
    }

    @Test
    public void testEstimatePassesUsingBulkToScale() {
        doReturn(buildTwiceTheReadings()).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        BaseReadingRecord preBulkReading = mock(BaseReadingRecord.class);
        BaseReadingRecord postBulkReading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(14))).when(preBulkReading).getQuantity(bulkReadingType);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(22))).when(postBulkReading).getQuantity(bulkReadingType);
        doReturn(Optional.of(preBulkReading)).when(channel).getReading(ESTIMABLE_TIME.minusMinutes(15).toInstant());
        doReturn(Optional.of(postBulkReading)).when(channel).getReading(ESTIMABLE_TIME.plusMinutes(15).toInstant());


        doReturn(asList(estimable, estimable2)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, BulkAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init();

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).contains(block);
        assertThat(estimable.getEstimation()).isEqualTo(BigDecimal.valueOf(4000000, 6));
    }

    @Test
    public void testEstimatePassesUsingAdvanceToScale() {
        buildReadingsForSuccessfulEstimationWithDelta();

        doReturn(deltaCimChannel).when(block).getCimChannel();
        doReturn(asList(estimable, estimable2)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType))
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init();

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).contains(block);
        assertThat(estimable.getEstimation()).isEqualTo(BigDecimal.valueOf(4000000, 6));
    }

    @Test
    public void testEstimateFailsWhenPriorAdvanceReadingIsSuspectEstimatedOrHasOverflowQuality() {
        doReturn(singletonList(suspect)).when(advanceCimChannel).findReadingQualities(ESTIMABLE_TIME.minusMinutes(35).toInstant());
        doReturn(ESTIMABLE_TIME.minusMinutes(35).toInstant()).when(suspect).getReadingTimestamp();
        doReturn(true).when(suspect).isActual();

        doReturn(buildTwiceTheReadings()).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        BaseReadingRecord preAdvanceReading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(14))).when(preAdvanceReading).getQuantity(advanceReadingType);
        BaseReadingRecord postAdvanceReading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(32))).when(postAdvanceReading).getQuantity(advanceReadingType);
        doReturn(singletonList(preAdvanceReading)).when(channelsContainer).getReadingsBefore(ESTIMABLE_TIME.toInstant(), advanceReadingType, 1);
        doReturn(singletonList(postAdvanceReading)).when(channelsContainer).getReadings(Range.atLeast(ESTIMABLE_TIME.plusMinutes(15).toInstant()), advanceReadingType);
        doReturn(ESTIMABLE_TIME.minusMinutes(35).toInstant()).when(preAdvanceReading).getTimeStamp();
        doReturn(ESTIMABLE_TIME.plusMinutes(35).toInstant()).when(postAdvanceReading).getTimeStamp();

        doReturn(asList(estimable, estimable2)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType))
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init(LOGGER);

        Stream.of(
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT),
                ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ESTIMATEGENERIC),
                ReadingQualityType.of(QualityCodeSystem.ENDDEVICE, QualityCodeIndex.ESTIMATEGENERIC),
                ReadingQualityType.of(QualityCodeSystem.ENDDEVICE, QualityCodeIndex.OVERFLOWCONDITIONDETECTED)
        ).forEach(readingQualityType -> {
            doReturn(readingQualityType).when(suspect).getType();
            EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDM);

            assertThat(result.estimated()).isEmpty();
            assertThat(result.remainingToBeEstimated()).containsExactly(block);
            JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
            JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.endsWith("since the prior advance reading is suspect, estimated or overflow")).atLevel(Level.INFO);
        });
    }

    @Test
    public void testEstimateFailsWhenLaterAdvanceReadingIsSuspectEstimatedOrHasOverflowQuality() {
        doReturn(singletonList(suspect)).when(advanceCimChannel).findReadingQualities(ESTIMABLE_TIME.plusMinutes(35).toInstant());
        doReturn(ESTIMABLE_TIME.plusMinutes(35).toInstant()).when(suspect).getReadingTimestamp();
        doReturn(true).when(suspect).isActual();

        doReturn(buildTwiceTheReadings()).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        BaseReadingRecord preAdvanceReading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(14))).when(preAdvanceReading).getQuantity(advanceReadingType);
        BaseReadingRecord postAdvanceReading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(32))).when(postAdvanceReading).getQuantity(advanceReadingType);
        doReturn(singletonList(preAdvanceReading)).when(channelsContainer).getReadingsBefore(ESTIMABLE_TIME.toInstant(), advanceReadingType, 1);
        doReturn(singletonList(postAdvanceReading)).when(channelsContainer).getReadings(Range.atLeast(ESTIMABLE_TIME.plusMinutes(15).toInstant()), advanceReadingType);
        doReturn(ESTIMABLE_TIME.minusMinutes(35).toInstant()).when(preAdvanceReading).getTimeStamp();
        doReturn(ESTIMABLE_TIME.plusMinutes(35).toInstant()).when(postAdvanceReading).getTimeStamp();

        doReturn(asList(estimable, estimable2)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType))
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init(LOGGER);

        Stream.of(
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT),
                ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.ESTIMATEGENERIC),
                ReadingQualityType.of(QualityCodeSystem.ENDDEVICE, QualityCodeIndex.ESTIMATEGENERIC),
                ReadingQualityType.of(QualityCodeSystem.ENDDEVICE, QualityCodeIndex.OVERFLOWCONDITIONDETECTED)
        ).forEach(readingQualityType -> {
            doReturn(readingQualityType).when(suspect).getType();
            EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

            assertThat(result.estimated()).isEmpty();
            assertThat(result.remainingToBeEstimated()).containsExactly(block);
            JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
            JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.endsWith("since the later advance reading is suspect, estimated or overflow")).atLevel(Level.INFO);
        });
    }

    @Test
    public void testEstimatePassesWhenPriorAndLaterAdvanceReadingsHaveNonActualBadQualities() {
        doReturn(asList(suspect, overflow, estimated)).when(advanceCimChannel).findReadingQualities(ESTIMABLE_TIME.minusMinutes(35).toInstant());
        doReturn(asList(suspect, overflow, estimated)).when(advanceCimChannel).findReadingQualities(ESTIMABLE_TIME.plusMinutes(35).toInstant());
        doReturn(singletonList(suspect)).when(advanceCimChannel).findReadingQualities(ESTIMABLE_TIME.minusMinutes(35).toInstant());
        doReturn(false).when(suspect).isActual();
        doReturn(false).when(overflow).isActual();
        doReturn(false).when(estimated).isActual();
        doReturn(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT)).when(suspect).getType();
        doReturn(ReadingQualityType.of(QualityCodeSystem.ENDDEVICE, QualityCodeIndex.OVERFLOWCONDITIONDETECTED)).when(overflow).getType();
        doReturn(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ESTIMATEGENERIC)).when(estimated).getType();

        buildReadingsForSuccessfulEstimationWithDelta();
        doReturn(deltaCimChannel).when(block).getCimChannel();
        doReturn(asList(estimable, estimable2)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType))
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init(LOGGER);

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDM);

        assertThat(result.estimated()).containsExactly(block);
        assertThat(result.remainingToBeEstimated()).isEmpty();
        assertThat(estimable.getEstimation()).isEqualTo(BigDecimal.valueOf(4000000, 6));
    }

    @Test
    public void testEstimatePassesWhenReadingsHaveBadQualitiesFromOtherSystems() {
        doReturn(asList(suspect, overflow, estimated)).when(advanceCimChannel).findReadingQualities(ESTIMABLE_TIME.minusMinutes(35).toInstant());
        doReturn(asList(suspect, overflow, estimated)).when(advanceCimChannel).findReadingQualities(ESTIMABLE_TIME.plusMinutes(35).toInstant());
        doReturn(singletonList(suspect)).when(advanceCimChannel).findReadingQualities(ESTIMABLE_TIME.minusMinutes(35).toInstant());
        doReturn(true).when(suspect).isActual();
        doReturn(true).when(overflow).isActual();
        doReturn(true).when(estimated).isActual();
        doReturn(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT)).when(suspect).getType();
        doReturn(ReadingQualityType.of(QualityCodeSystem.EXTERNAL, QualityCodeIndex.OVERFLOWCONDITIONDETECTED)).when(overflow).getType();
        doReturn(ReadingQualityType.of(QualityCodeSystem.OTHER, QualityCodeIndex.ESTIMATEGENERIC)).when(estimated).getType();

        buildReadingsForSuccessfulEstimationWithDelta();
        doReturn(deltaCimChannel).when(block).getCimChannel();
        doReturn(asList(estimable, estimable2)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType))
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init(LOGGER);

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        verify(deltaCimChannel, MockitoExtension.and(atLeast(2), MockitoExtension.neverWithOtherArguments()))
                .findReadingQualities(eq(SYSTEMS), any(QualityCodeIndex.class), any(Range.class), eq(true));

        assertThat(result.estimated()).containsExactly(block);
        assertThat(result.remainingToBeEstimated()).isEmpty();
        assertThat(estimable.getEstimation()).isEqualTo(BigDecimal.valueOf(4000000, 6));
    }

    @Test
    public void testEstimateFailsWhenSuspectsAreInPreIntervalOrPostInterval() {
        buildReadingsForSuccessfulEstimationWithDelta();

        doReturn(asList(estimable, estimable2)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType))
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init(LOGGER);

        Stream.of(
                Range.openClosed(ESTIMABLE_TIME.minusMinutes(35).toInstant(), ESTIMABLE_TIME.minusMinutes(15).toInstant()),
                Range.openClosed(ESTIMABLE_TIME.plusMinutes(15).toInstant(), ESTIMABLE_TIME.plusMinutes(45).toInstant())
        ).forEach(instantRange -> {
            doReturn(singletonList(suspect)).when(deltaCimChannel).findReadingQualities(SYSTEMS, QualityCodeIndex.SUSPECT, instantRange, true);

            EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

            assertThat(result.estimated()).isEmpty();
            assertThat(result.remainingToBeEstimated()).containsExactly(block);
            JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
            JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.endsWith("since there are other suspects between the advance readings")).atLevel(Level.INFO);

            doReturn(Collections.emptyList()).when(deltaCimChannel).findReadingQualities(SYSTEMS, QualityCodeIndex.SUSPECT, instantRange, true);
        });
    }

    @Test
    public void testEstimateFailsWhenPriorAdvanceReadingIsMissing() {
        doReturn(buildTwiceTheReadings()).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        BaseReadingRecord postAdvanceReading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(32))).when(postAdvanceReading).getQuantity(advanceReadingType);
        doReturn(Collections.emptyList()).when(channelsContainer).getReadingsBefore(ESTIMABLE_TIME.toInstant(), advanceReadingType, 1);
        doReturn(singletonList(postAdvanceReading)).when(channelsContainer).getReadings(Range.atLeast(ESTIMABLE_TIME.plusMinutes(15).toInstant()), advanceReadingType);
        doReturn(ESTIMABLE_TIME.plusMinutes(35).toInstant()).when(postAdvanceReading).getTimeStamp();

        doReturn(asList(estimable, estimable2)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType))
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init(LOGGER);

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).isEmpty();
        assertThat(result.remainingToBeEstimated()).containsExactly(block);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.endsWith("since the prior advance reading has no value")).atLevel(Level.INFO);
    }

    @Test
    public void testEstimateFailsWhenLaterAdvanceReadingIsMissing() {
        doReturn(buildTwiceTheReadings()).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        BaseReadingRecord preAdvanceReading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(14))).when(preAdvanceReading).getQuantity(advanceReadingType);
        doReturn(singletonList(preAdvanceReading)).when(channelsContainer).getReadingsBefore(ESTIMABLE_TIME.toInstant(), advanceReadingType, 1);
        doReturn(ESTIMABLE_TIME.minusMinutes(35).toInstant()).when(preAdvanceReading).getTimeStamp();

        doReturn(asList(estimable, estimable2)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType))
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init(LOGGER);

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).isEmpty();
        assertThat(result.remainingToBeEstimated()).containsExactly(block);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.endsWith("since the later advance reading has no value")).atLevel(Level.INFO);
    }

    @Test
    public void testEstimateFailsWhenSurroundingBulkReadingsNotAvailable() {
        doReturn(buildTwiceTheReadings()).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        BaseReadingRecord preBulkReading = mock(BaseReadingRecord.class);
        BaseReadingRecord postBulkReading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(14))).when(preBulkReading).getQuantity(bulkReadingType);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(22))).when(postBulkReading).getQuantity(bulkReadingType);
        doReturn(Optional.empty()).when(channel).getReading(ESTIMABLE_TIME.minusMinutes(15).toInstant());
        doReturn(Optional.empty()).when(channel).getReading(ESTIMABLE_TIME.plusMinutes(15).toInstant());


        doReturn(asList(estimable, estimable2)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, BulkAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init(LOGGER);

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).isEmpty();
        assertThat(result.remainingToBeEstimated()).containsExactly(block);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.endsWith("since the surrounding bulk readings are not available")).atLevel(Level.INFO);
    }

    @Test
    public void testEstimateFailsWhenReadingTypeIsNotRegular() {
        doReturn(false).when(readingType).isRegular();
        doReturn(false).when(bulkReadingType).isRegular();

        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, BulkAdvanceReadingsSettings.INSTANCE)
                .build();

        Estimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(block);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("since it has a reading type that is not regular")).atLevel(Level.INFO);
    }

    @Test
    public void testEstimateWithBulkFailsSinceReadingTypeHasNoBulk() {
        doReturn(buildTwiceTheReadings()).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        BaseReadingRecord preBulkReading = mock(BaseReadingRecord.class);
        BaseReadingRecord postBulkReading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(14))).when(preBulkReading).getQuantity(bulkReadingType);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(22))).when(postBulkReading).getQuantity(bulkReadingType);
        doReturn(Optional.of(preBulkReading)).when(channel).getReading(ESTIMABLE_TIME.minusMinutes(15).toInstant());
        doReturn(Optional.of(postBulkReading)).when(channel).getReading(ESTIMABLE_TIME.plusMinutes(15).toInstant());
        doReturn(Optional.empty()).when(readingType).getBulkReadingType();

        doReturn(asList(estimable, estimable2)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, BulkAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init(LOGGER);

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).isEmpty();
        assertThat(result.remainingToBeEstimated()).containsExactly(block);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("since the reading type readingType has no bulk reading type")).atLevel(Level.INFO);
    }

    @Test(expected = LocalizedFieldValidationException.class)
    public void testInvalidPropertiesWhenConsecutiveIsZero() {
        EstimationRuleProperties property = estimationRuleProperty(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 0L);

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService);

        estimator.validateProperties(Collections.singletonMap(property.getName(), property.getValue()));
    }

    @Test(expected = LocalizedFieldValidationException.class)
    public void testInvalidPropertiesWhenConsecutiveIsNegative() {
        EstimationRuleProperties property = estimationRuleProperty(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, -1L);

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService);

        estimator.validateProperties(Collections.singletonMap(property.getName(), property.getValue()));
    }

    @Test(expected = LocalizedFieldValidationException.class)
    public void testInvalidPropertiesWhenNonCumulativeReadingTypeSpecified() {
        EstimationRuleProperties property = estimationRuleProperty(ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(deltaReadingType));

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService);

        estimator.validateProperties(Collections.singletonMap(property.getName(), property.getValue()));
    }

    @Test
    public void testEstimateWithBulkFailsSinceNoPriorBulkReading() {
        doReturn(buildTwiceTheReadings()).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        BaseReadingRecord preBulkReading = mock(BaseReadingRecord.class);
        BaseReadingRecord postBulkReading = mock(BaseReadingRecord.class);
        doReturn(null).when(preBulkReading).getQuantity(bulkReadingType);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(22))).when(postBulkReading).getQuantity(bulkReadingType);
        doReturn(Optional.of(preBulkReading)).when(channel).getReading(ESTIMABLE_TIME.minusMinutes(15).toInstant());
        doReturn(Optional.of(postBulkReading)).when(channel).getReading(ESTIMABLE_TIME.plusMinutes(15).toInstant());


        doReturn(asList(estimable, estimable2)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, BulkAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init(LOGGER);

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).isEmpty();
        assertThat(result.remainingToBeEstimated()).containsExactly(block);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.endsWith("since the prior bulk reading has no value")).atLevel(Level.INFO);
    }

    @Test
    public void testEstimateWithBulkFailsSinceNoLaterBulkReading() {
        doReturn(buildTwiceTheReadings()).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        BaseReadingRecord preBulkReading = mock(BaseReadingRecord.class);
        BaseReadingRecord postBulkReading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(22))).when(preBulkReading).getQuantity(bulkReadingType);
        doReturn(null).when(postBulkReading).getQuantity(bulkReadingType);
        doReturn(Optional.of(preBulkReading)).when(channel).getReading(ESTIMABLE_TIME.minusMinutes(15).toInstant());
        doReturn(Optional.of(postBulkReading)).when(channel).getReading(ESTIMABLE_TIME.plusMinutes(15).toInstant());


        doReturn(asList(estimable, estimable2)).when(block).estimatables();
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L)
                .put(AverageWithSamplesEstimator.MIN_NUMBER_OF_SAMPLES, 2L)
                .put(AverageWithSamplesEstimator.MAX_NUMBER_OF_SAMPLES, 3L)
                .put(AverageWithSamplesEstimator.ALLOW_NEGATIVE_VALUES, false)
                .put(AverageWithSamplesEstimator.RELATIVE_PERIOD, AllRelativePeriod.INSTANCE)
                .put(AverageWithSamplesEstimator.ADVANCE_READINGS_SETTINGS, BulkAdvanceReadingsSettings.INSTANCE)
                .build();
        AverageWithSamplesEstimator estimator = new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, props);

        estimator.init(LOGGER);

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).isEmpty();
        assertThat(result.remainingToBeEstimated()).containsExactly(block);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.endsWith("since the later bulk reading has no value")).atLevel(Level.INFO);

    }

    @Test
    public void testGetSupportedApplications() {
        assertThat(new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService).getSupportedQualityCodeSystems())
                .containsOnly(QualityCodeSystem.MDC, QualityCodeSystem.MDM);
    }

    private void buildReadingsForSuccessfulEstimationWithDelta() {
        doReturn(buildTwiceTheReadings()).when(channel).getReadings(Range.openClosed(START.toInstant(), LAST_CHECKED.toInstant()));
        BaseReadingRecord preAdvanceReading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(14))).when(preAdvanceReading).getQuantity(advanceReadingType);
        BaseReadingRecord postAdvanceReading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(32))).when(postAdvanceReading).getQuantity(advanceReadingType);
        doReturn(singletonList(preAdvanceReading)).when(channelsContainer).getReadingsBefore(ESTIMABLE_TIME.toInstant(), advanceReadingType, 1);
        doReturn(singletonList(postAdvanceReading)).when(channelsContainer).getReadings(Range.atLeast(ESTIMABLE_TIME.plusMinutes(15).toInstant()), advanceReadingType);
        doReturn(ESTIMABLE_TIME.minusMinutes(35).toInstant()).when(preAdvanceReading).getTimeStamp();
        doReturn(ESTIMABLE_TIME.plusMinutes(35).toInstant()).when(postAdvanceReading).getTimeStamp();
        BaseReadingRecord pre1Reading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(3))).when(pre1Reading).getQuantity(readingType);
        doReturn(ESTIMABLE_TIME.minusMinutes(30).toInstant()).when(pre1Reading).getTimeStamp();
        BaseReadingRecord pre2Reading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(3))).when(pre2Reading).getQuantity(readingType);
        doReturn(ESTIMABLE_TIME.minusMinutes(15).toInstant()).when(pre2Reading).getTimeStamp();
        doReturn(asList(pre1Reading, pre2Reading)).when(channel).getReadings(Range.openClosed(ESTIMABLE_TIME.minusMinutes(35).toInstant(), ESTIMABLE_TIME.minusMinutes(15).toInstant()));
        BaseReadingRecord post1Reading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(4))).when(post1Reading).getQuantity(readingType);
        doReturn(ESTIMABLE_TIME.plusMinutes(30).toInstant()).when(post1Reading).getTimeStamp();
        BaseReadingRecord post2Reading = mock(BaseReadingRecord.class);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(6))).when(post2Reading).getQuantity(readingType);
        doReturn(ESTIMABLE_TIME.plusMinutes(45).toInstant()).when(post2Reading).getTimeStamp();
        doReturn(asList(post1Reading, post2Reading)).when(channel).getReadings(Range.openClosed(ESTIMABLE_TIME.plusMinutes(15).toInstant(), ESTIMABLE_TIME.plusMinutes(45).toInstant()));
        doReturn(singletonList(ESTIMABLE_TIME.plusMinutes(30).toInstant())).when(channel).toList(Range.openClosed(ESTIMABLE_TIME.plusMinutes(15).toInstant(), ESTIMABLE_TIME.plusMinutes(35).toInstant()));
        doReturn(ESTIMABLE_TIME.plusMinutes(45).toInstant()).when(channel).getNextDateTime(ESTIMABLE_TIME.plusMinutes(30).toInstant());
        doReturn(asList(ESTIMABLE_TIME.minusMinutes(30).toInstant(), ESTIMABLE_TIME.minusMinutes(15).toInstant())).when(channel).toList(Range.openClosed(ESTIMABLE_TIME.minusMinutes(35).toInstant(), ESTIMABLE_TIME
                .minusMinutes(15).toInstant()));
        doReturn(Optional.of(Duration.ofMinutes(15))).when(channel).getIntervalLength();
    }

    private static class EstimableImpl implements Estimatable {

        private final Instant timestamp;
        private BigDecimal bigDecimal;

        private EstimableImpl(Instant timestamp) {
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

    private EstimationRuleProperties estimationRuleProperty(final String name, final Object value) {
        return new EstimationRuleProperties() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDisplayName() {
                return name;
            }

            @Override
            public String getDescription() {
                return "Description for " + name;
            }

            @Override
            public Object getValue() {
                return value;
            }

            @Override
            public void setValue(Object value) {
            }

            @Override
            public EstimationRule getRule() {
                return mock(EstimationRule.class);
            }
        };
    }
}
