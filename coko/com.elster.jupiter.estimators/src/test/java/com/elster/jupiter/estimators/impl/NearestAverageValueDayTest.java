package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.tests.assertions.JupiterAssertions;
import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.estimation.DiscardDaySettings;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityWithTypeFetcher;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.impl.AllRelativePeriod;
import com.elster.jupiter.util.logging.LoggingContext;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NearestAverageValueDayTest {
    private static final Set<QualityCodeSystem> SYSTEMS = Estimator.qualityCodeSystemsToTakeIntoAccount(QualityCodeSystem.MDC);
    private static final Logger LOGGER = Logger.getLogger(NearestAvgValueDayEstimator.class.getName());
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
    private Estimatable estimable;
    @Mock
    private ReadingType readingType, bulkReadingType, deltaReadingType;
    @Mock
    private Channel channel, otherChannel;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private TimeService timeService;
    @Mock
    private CimChannel deltaCimChannel;
    @Mock
    private Meter meter;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReadingQualityWithTypeFetcher deltaFetcher;
    @Mock
    private CalendarService calendarService;
    @Mock
    private Calendar calendar;
    @Mock
    private Event event, falseEvent;
    @Mock
    private Calendar.ZonedView zonedView;

    private LogRecorder logRecorder;

    @Before
    public void setUp() {
        List<Event> eventList = Collections.singletonList(event);
        when(calendar.getId()).thenReturn(1L);
        when(event.getId()).thenReturn(1L);
        when(calendar.getEvents()).thenReturn(eventList);
        when(calendar.forZone(any(), any(), any())).thenReturn(zonedView);
        when(zonedView.eventFor(ESTIMABLE_TIME.toInstant())).thenReturn(falseEvent);
        when(falseEvent.getId()).thenReturn(2L);
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        estimable = new EstimableImpl(ESTIMABLE_TIME.toInstant());
        doReturn(AllRelativePeriod.INSTANCE).when(timeService).getAllRelativePeriod();
        doReturn(singletonList(estimable)).when(block).estimatables();
        doReturn(readingType).when(block).getReadingType();
        doReturn(channel).when(block).getChannel();
        doReturn(deltaCimChannel).when(block).getCimChannel();
        doReturn(channelsContainer).when(channel).getChannelsContainer();

        doReturn(Optional.of(meter)).when(channelsContainer).getMeter();
        doReturn(asList(channel, otherChannel)).when(channelsContainer).getChannels();
        doReturn(asList(deltaReadingType, bulkReadingType)).when(channel).getReadingTypes();
        doReturn(singletonList(readingType)).when(otherChannel).getReadingTypes();
        doReturn(Optional.of(deltaCimChannel)).when(channel).getCimChannel(readingType);
        doReturn(Optional.of(deltaCimChannel)).when(channel).getCimChannel(deltaReadingType);
        doReturn(deltaFetcher).when(deltaCimChannel).findReadingQualities();

        doReturn(START.toInstant()).when(channelsContainer).getStart();
        doReturn(Optional.of(LAST_CHECKED.toInstant())).when(validationService).getLastChecked(channel);
        doReturn(buildReadings()).when(channel).getReadings(Range.closedOpen(ESTIMABLE_TIME.toInstant().minus(3 * ChronoUnit.WEEKS.getDuration().toDays(), ChronoUnit.DAYS), ESTIMABLE_TIME.toInstant()));
        doReturn(TimeZoneNeutral.getMcMurdo()).when(channel).getZoneId();
        doReturn(Optional.of(bulkReadingType)).when(readingType).getBulkReadingType();
        doReturn(TimeAttribute.FIXEDBLOCK15MIN).when(readingType).getMeasuringPeriod();
        doReturn(false).when(deltaReadingType).isCumulative();
        doReturn(true).when(deltaReadingType).isRegular();
        doAnswer(invocation -> ((Instant) invocation.getArguments()[0]).minus(15, ChronoUnit.MINUTES)).when(channel).getPreviousDateTime(any());

        doReturn("readingType").when(readingType).getMRID();
        doReturn("deltaReadingType").when(deltaReadingType).getMRID();
        doReturn("bulkReadingType").when(bulkReadingType).getMRID();
        doReturn("meter").when(meter).getName();

        doReturn(true).when(readingType).isRegular();
        doReturn(true).when(deltaReadingType).isRegular();
        doReturn(true).when(bulkReadingType).isCumulative();
        doReturn(true).when(bulkReadingType).isRegular();

        logRecorder = new LogRecorder(Level.ALL);
        LOGGER.addHandler(logRecorder);
        LoggingContext.getCloseableContext().with("rule", "rule");
    }

    private List<BaseReadingRecord> buildReadings() {
        Long[] values = new Long[]{4L, 1000L, 100000L, 100L, 100L, 5L, 200L, null, 6L, 100L, 7L};
        int number = values.length;
        int start = -5;
        Set<Instant> badTimes = Stream.of(
                ESTIMABLE_TIME.minusDays(28),
                ESTIMABLE_TIME.minusDays(21),
                ESTIMABLE_TIME.minusDays(14),
                ESTIMABLE_TIME.plusDays(21)
        ).map(ZonedDateTime::toInstant).collect(Collectors.toSet());


        List<Instant> timestamps = IntStream.rangeClosed(start, start + number - 2)
                .mapToObj(i -> ESTIMABLE_TIME.plusDays(7 * i).toInstant())
                .collect(Collectors.toList());
        timestamps.add(1, ESTIMABLE_TIME.minusDays(25).toInstant());
        int i = 0;
        List<BaseReadingRecord> readingRecords = new ArrayList<>(number);
        for (Instant timestamp : timestamps) {
            readingRecords.add(mockDeltaReadingIsValid(timestamp, values[i++],
                    badTimes.contains(timestamp)));
        }
        return readingRecords;
    }

    private BaseReadingRecord mockDeltaReadingIsValid(Instant instant, Long value, boolean isValid) {
        when(deltaFetcher
                .atTimestamp(instant)
                .actual()
                .ofQualitySystems(SYSTEMS)
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.KNOWNMISSINGREAD))
                .noneMatch())
                .thenReturn(isValid);
        return mockDeltaReading(instant, value);
    }

    private BaseReadingRecord mockDeltaReading(Instant instant, Long value) {
        return mockReading(instant, value, readingType);
    }

    private static BaseReadingRecord mockReading(Instant instant, Long value, ReadingType readingType) {
        BaseReadingRecord reading = mock(BaseReadingRecord.class);
        doReturn(instant).when(reading).getTimeStamp();
        if (value == null) {
            doReturn(BigDecimal.valueOf(0)).when(reading).getValue();
        } else {
            doReturn(BigDecimal.valueOf(value)).when(reading).getValue();
        }
        Quantity amount = Optional.ofNullable(value)
                .map(BigDecimal::valueOf)
                .map(Unit.WATT_HOUR::amount)
                .orElse(null);
        doReturn(amount).when(reading).getQuantity(readingType);
        return reading;
    }

    @After
    public void tearDown() {
        LoggingContext.getCloseableContext().close();
        LOGGER.removeHandler(logRecorder);
    }

    @Test
    public void testEstimatePasses() {
        when(zonedView.eventFor(any())).thenReturn(falseEvent);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);

        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(NearestAvgValueDayEstimator.NUMBER_OF_SAMPLES, 2L)
                .put(NearestAvgValueDayEstimator.MAXIMUM_NUMBER_OF_WEEKS, 3L)
                .put(NearestAvgValueDayEstimator.DISCARD_SPECIFIC_DAY, new DiscardDaySettings(true, calendar, event))
                .build();
        NearestAvgValueDayEstimator estimator = new NearestAvgValueDayEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, calendarService, props);

        estimator.init();

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).contains(block);
        assertThat(estimable.getEstimation()).isEqualTo(BigDecimal.valueOf(50050));
    }

    @Test
    public void testEstimatePassesWithDiscardDayFalse() {
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(NearestAvgValueDayEstimator.NUMBER_OF_SAMPLES, 2L)
                .put(NearestAvgValueDayEstimator.MAXIMUM_NUMBER_OF_WEEKS, 3L)
                .put(NearestAvgValueDayEstimator.DISCARD_SPECIFIC_DAY, new DiscardDaySettings(false, null, null))
                .build();
        NearestAvgValueDayEstimator estimator = new NearestAvgValueDayEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, calendarService, props);

        estimator.init();

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).contains(block);
        assertThat(estimable.getEstimation()).isEqualTo(BigDecimal.valueOf(50050));
    }

    @Test
    public void testEstimateFailsIfNotEnoughSamples() {
        List<BaseReadingRecord> readingRecords = buildReadings();
        readingRecords = readingRecords.subList(0, readingRecords.size() - 10);
        doReturn(readingRecords).when(channel).getReadings(Range.closedOpen(ESTIMABLE_TIME.toInstant().minus(3 * ChronoUnit.WEEKS.getDuration().toDays(), ChronoUnit.DAYS), ESTIMABLE_TIME.toInstant()));
        when(zonedView.eventFor(any())).thenReturn(falseEvent);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);

        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(NearestAvgValueDayEstimator.NUMBER_OF_SAMPLES, 2L)
                .put(NearestAvgValueDayEstimator.MAXIMUM_NUMBER_OF_WEEKS, 3L)
                .put(NearestAvgValueDayEstimator.DISCARD_SPECIFIC_DAY, new DiscardDaySettings(true, calendar, event))
                .build();
        NearestAvgValueDayEstimator estimator = new NearestAvgValueDayEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, calendarService, props);

        estimator.init(LOGGER);

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).isEmpty();
        assertThat(result.remainingToBeEstimated()).containsExactly(block);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("since not enough samples are found")).atLevel(Level.INFO);
    }

    @Test
    public void testEstimateFailsIfEstimableDayIsDiscarded() {
        when(zonedView.eventFor(any())).thenReturn(event);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);

        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(NearestAvgValueDayEstimator.NUMBER_OF_SAMPLES, 2L)
                .put(NearestAvgValueDayEstimator.MAXIMUM_NUMBER_OF_WEEKS, 3L)
                .put(NearestAvgValueDayEstimator.DISCARD_SPECIFIC_DAY, new DiscardDaySettings(true, calendar, event))
                .build();
        NearestAvgValueDayEstimator estimator = new NearestAvgValueDayEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, calendarService, props);

        estimator.init();

        EstimationResult result = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(result.estimated()).isEmpty();
        assertThat(result.remainingToBeEstimated()).containsExactly(block);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("since the values to estimate belong to a day configured to be discarded")).atLevel(Level.INFO);
    }

    @Test
    public void testEstimateFailsWhenReadingTypeIsNotRegular() {
        doReturn(false).when(readingType).isRegular();

        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(NearestAvgValueDayEstimator.NUMBER_OF_SAMPLES, 2L)
                .put(NearestAvgValueDayEstimator.MAXIMUM_NUMBER_OF_WEEKS, 3L)
                .put(NearestAvgValueDayEstimator.DISCARD_SPECIFIC_DAY, new DiscardDaySettings(true, calendar, event))
                .build();
        NearestAvgValueDayEstimator estimator = new NearestAvgValueDayEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, calendarService, props);

        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(block);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("since it has a reading type that is not regular")).atLevel(Level.INFO);
    }


    @Test
    public void testEstimateFailsWhenReadingTypeIsNotCumulative() {
        doReturn(true).when(readingType).isCumulative();

        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(NearestAvgValueDayEstimator.NUMBER_OF_SAMPLES, 2L)
                .put(NearestAvgValueDayEstimator.MAXIMUM_NUMBER_OF_WEEKS, 3L)
                .put(NearestAvgValueDayEstimator.DISCARD_SPECIFIC_DAY, new DiscardDaySettings(true, calendar, event))
                .build();
        NearestAvgValueDayEstimator estimator = new NearestAvgValueDayEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, calendarService, props);

        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(block);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("Only delta readingtypes are allowed")).atLevel(Level.INFO);
    }

    @Test
    public void testEstimateFailsWhenReadingTypeMeasurmentTimeIsNotValid() {
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.MONTHLY);

        Map<String, Object> props = ImmutableMap.<String, Object>builder()
                .put(NearestAvgValueDayEstimator.NUMBER_OF_SAMPLES, 2L)
                .put(NearestAvgValueDayEstimator.MAXIMUM_NUMBER_OF_WEEKS, 3L)
                .put(NearestAvgValueDayEstimator.DISCARD_SPECIFIC_DAY, new DiscardDaySettings(true, calendar, event))
                .build();
        NearestAvgValueDayEstimator estimator = new NearestAvgValueDayEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService, calendarService, props);

        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(block), QualityCodeSystem.MDC);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(block);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
        JupiterAssertions.assertThat(logRecorder).hasRecordWithMessage(message -> message.contains("measuring period  is larger than day")).atLevel(Level.INFO);
    }


    @Test
    public void testGetSupportedApplications() {
        assertThat(new AverageWithSamplesEstimator(thesaurus, propertySpecService, validationService, meteringService, timeService).getSupportedQualityCodeSystems())
                .containsOnly(QualityCodeSystem.MDC, QualityCodeSystem.MDM);
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
}
