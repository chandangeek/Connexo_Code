package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataCompletionSummaryFlag;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataCompletionSummaryType;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataModificationSummaryFlags;
import com.elster.jupiter.mdm.usagepoint.data.IChannelDataCompletionSummary;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataCompletionService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataModelService;
import com.elster.jupiter.mdm.usagepoint.data.ValidChannelDataSummaryFlags;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingQualityWithTypeFetcher;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.data.MapEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointDataCompletionServiceImplTest {
    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private static final Instant FIRST_DATE = ZonedDateTime.of(2016, 7, 5, 12, 0, 0, 0, PARIS).toInstant();
    private static final Instant EDITED_DATE = ZonedDateTime.of(2016, 7, 6, 12, 0, 0, 0, PARIS).toInstant();
    private static final Instant ESTIMATED_DATE = ZonedDateTime.of(2016, 7, 7, 12, 0, 0, 0, PARIS).toInstant();
    private static final Instant MISSING_DATE = ZonedDateTime.of(2016, 7, 8, 12, 0, 0, 0, PARIS).toInstant();
    private static final Instant SUSPECT_DATE = ZonedDateTime.of(2016, 7, 9, 12, 0, 0, 0, PARIS).toInstant();
    private static final Instant REJECTED_DATE = ZonedDateTime.of(2016, 7, 10, 12, 0, 0, 0, PARIS).toInstant();
    private static final Instant LAST_CHECKED = ZonedDateTime.of(2016, 7, 11, 12, 0, 0, 0, PARIS).toInstant();
    private static final Instant UNCHECKED_DATE = ZonedDateTime.of(2016, 7, 12, 12, 0, 0, 0, PARIS).toInstant();
    private static final Instant NOW = ZonedDateTime.of(2016, 7, 13, 12, 0, 0, 0, PARIS).toInstant();
    private static final Instant FUTURE_DATE = ZonedDateTime.of(2016, 7, 13, 12, 1, 0, 0, PARIS).toInstant();
    private static final Range<Instant> NOMINAL_RANGE = Range.openClosed(FIRST_DATE.minusNanos(1), NOW);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ValidationService validationService;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private ReadingTypeDeliverable deliverable1, deliverable2;
    @Mock
    private ReadingType readingType;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private Channel channel;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReadingQualityWithTypeFetcher fetcher;
    @Mock
    private ReadingQualityRecord error, suspect, added, edited, removed, estimated;

    @Captor
    ArgumentCaptor<Range<Instant>> captor;

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;
    private UsagePointDataCompletionService usagePointDataCompletionService;
    private IChannelDataCompletionSummary summary, editedSummary, validSummary;
    private Map<ReadingTypeDeliverable, List<IChannelDataCompletionSummary>> summaries;

    @Before
    public void setUp() {
        when(usagePoint.getName()).thenReturn("Mrmrmrrr");
        when(metrologyContract.getId()).thenReturn(777L);
        when(metrologyContract.getDeliverables()).thenReturn(Arrays.asList(deliverable1, deliverable2));
        when(deliverable1.getReadingType()).thenReturn(readingType);
        when(deliverable2.getReadingType()).thenReturn(readingType);
        when(effectiveMetrologyConfiguration.getUsagePoint()).thenReturn(usagePoint);
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        when(channelsContainer.getChannel(readingType)).thenReturn(Optional.of(channel));
        when(channelsContainer.getInterval()).thenReturn(Interval.of(Range.all()));

        UsagePointDataModelService usagePointDataModelService = mock(UsagePointDataModelService.class);
        when(usagePointDataModelService.thesaurus()).thenReturn(thesaurus);
        usagePointDataCompletionService = new UsagePointDataCompletionServiceImpl(usagePointDataModelService, validationService);

        when(validationService.getLastChecked(channel)).thenReturn(Optional.of(LAST_CHECKED));
        when(channel.isRegular()).thenReturn(true);
        when(channel.toList(captor.capture())).thenAnswer(invocation -> getInstantsFromInterval(captor.getValue()));
        when(channel.findReadingQualities()).thenReturn(fetcher);
        when(error.getType()).thenReturn(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ERRORCODE));
        when(error.getReadingTimestamp()).thenReturn(MISSING_DATE);
        when(suspect.getType()).thenReturn(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT));
        when(suspect.getReadingTimestamp()).thenReturn(SUSPECT_DATE);
        when(added.getType()).thenReturn(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ADDED));
        when(added.getReadingTimestamp()).thenReturn(LAST_CHECKED);
        when(edited.getType()).thenReturn(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.EDITGENERIC));
        when(edited.getReadingTimestamp()).thenReturn(EDITED_DATE);
        when(removed.getType()).thenReturn(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.REJECTED));
        when(removed.getReadingTimestamp()).thenReturn(REJECTED_DATE);
        when(estimated.getType()).thenReturn(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, 42));
        when(estimated.getReadingTimestamp()).thenReturn(ESTIMATED_DATE);

        when(fetcher.inTimeInterval(captor.capture())
                .actual()
                .ofQualitySystem(QualityCodeSystem.MDM)
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.KNOWNMISSINGREAD, QualityCodeIndex.ERRORCODE))
                .orOfAnotherTypeInSameSystems()
                .ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.EDITED, QualityCodeCategory.ESTIMATED, QualityCodeCategory.VALIDATION))
                .stream())
                .thenAnswer(invocation -> Stream.of(error, suspect, added, edited, removed, estimated)
                        .filter(record -> captor.getValue().contains(record.getReadingTimestamp())));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetValidationSummaryForPeriodWithoutData() {
        Range<Instant> interval = Range.openClosed(NOW, NOW.plusNanos(1));
        summary = usagePointDataCompletionService.getDataCompletionStatistics(channel, interval).get(0);
        assertThat(summary.getValues()).isEmpty();
        assertThat(summary.getSum()).isZero();
        assertThat(summary.getTargetInterval()).isEqualTo(interval);
    }

    @Test
    public void testGetValidationSummaryForFuturePeriod() {
        Range<Instant> interval = Range.openClosed(NOW, FUTURE_DATE);
        summaries = usagePointDataCompletionService.getDataCompletionStatistics(effectiveMetrologyConfiguration, metrologyContract, interval);
        assertThat(summaries.keySet()).containsExactly(deliverable1, deliverable2);
        assertThat(summaries.values().stream()
                .peek(summary -> assertThat(summary.get(0).getValues()).containsExactly(MapEntry.entry(ChannelDataCompletionSummaryFlag.NOT_VALIDATED, 1)))
                .peek(summary -> assertThat(summary.get(0).getSum()).isEqualTo(1))
                .peek(summary -> assertThat(summary.get(0).getTargetInterval()).isEqualTo(interval))
                .count()).isEqualTo(2);
    }

    @Test
    public void testGetValidationSummaryForUnvalidatedPeriod() {
        Range<Instant> interval = Range.openClosed(LAST_CHECKED, FUTURE_DATE);
        summaries = usagePointDataCompletionService.getDataCompletionStatistics(effectiveMetrologyConfiguration, metrologyContract, interval);
        assertThat(summaries.keySet()).containsExactly(deliverable1, deliverable2);
        assertThat(summaries.values().stream()
                .peek(summary -> assertThat(summary.get(0).getValues()).containsExactly(MapEntry.entry(ChannelDataCompletionSummaryFlag.NOT_VALIDATED, 3)))
                .peek(summary -> assertThat(summary.get(0).getSum()).isEqualTo(3))
                .peek(summary -> assertThat(summary.get(0).getTargetInterval()).isEqualTo(interval))
                .count()).isEqualTo(2);
    }

    @Test
    public void testGetValidationSummaryForPeriodStartingBeforeChannelsContainerStart() {
        Range<Instant> actualRange = Range.openClosed(FIRST_DATE, NOW);
        when(channelsContainer.getInterval()).thenReturn(Interval.of(Range.atLeast(FIRST_DATE)));
        summaries = usagePointDataCompletionService.getDataCompletionStatistics(effectiveMetrologyConfiguration, metrologyContract, NOMINAL_RANGE);
        assertThat(summaries.keySet()).containsExactly(deliverable1, deliverable2);
        assertThat(summaries.values().stream()
                .peek(summary -> assertThat(summary.get(0).getValues()).contains(
                        MapEntry.entry(ChannelDataCompletionSummaryFlag.SUSPECT, 2),
                        MapEntry.entry(ChannelDataCompletionSummaryFlag.VALID, 4),
                        MapEntry.entry(ChannelDataCompletionSummaryFlag.NOT_VALIDATED, 2)
                ))
                .peek(summary -> assertThat(summary.get(0).getSum()).isEqualTo(8))
                .peek(summary -> assertThat(summary.get(0).getTargetInterval()).isEqualTo(actualRange))
                .count()).isEqualTo(2);
    }

    @Test
    public void testGetValidationSummaryForPeriodEndingBeforeChannelsContainerStart() {
        when(channelsContainer.getInterval()).thenReturn(Interval.of(Range.atLeast(NOW)));
        summaries = usagePointDataCompletionService.getDataCompletionStatistics(effectiveMetrologyConfiguration, metrologyContract, NOMINAL_RANGE);
        assertThat(summaries.keySet()).containsExactly(deliverable1, deliverable2);
        assertThat(summaries.values().stream()
                .peek(summary -> assertThat(summary.get(0).getValues()).isEmpty())
                .peek(summary -> assertThat(summary.get(0).getSum()).isZero())
                .peek(summary -> assertThat(summary.get(0).getTargetInterval()).isEqualTo(NOMINAL_RANGE))
                .count()).isEqualTo(2);
    }

    @Test
    public void testGetValidationSummaryForEmptyPeriod() {
        summaries = usagePointDataCompletionService.getDataCompletionStatistics(effectiveMetrologyConfiguration, metrologyContract, Range.openClosed(NOW, NOW));
        assertThat(summaries.keySet()).containsExactly(deliverable1, deliverable2);
        assertThat(summaries.values().stream()
                .peek(summary -> assertThat(summary.get(0).getValues()).isEmpty())
                .peek(summary -> assertThat(summary.get(0).getSum()).isZero())
                .peek(summary -> assertThat(summary.get(0).getTargetInterval()).isEqualTo(Range.openClosed(NOW, NOW)))
                .count()).isEqualTo(2);
    }

    @Test
    public void testGetValidationSummaryForPeriodStartingBeforeChannelsContainerEnd() {
        when(channelsContainer.getInterval()).thenReturn(Interval.of(Range.closedOpen(FIRST_DATE.minusSeconds(1), FIRST_DATE.minusNanos(1))));
        summaries = usagePointDataCompletionService.getDataCompletionStatistics(effectiveMetrologyConfiguration, metrologyContract, NOMINAL_RANGE);
        assertThat(summaries.keySet()).containsExactly(deliverable1, deliverable2);
        assertThat(summaries.values().stream()
                .peek(summary -> assertThat(summary.get(0).getValues()).isEmpty())
                .peek(summary -> assertThat(summary.get(0).getSum()).isZero())
                .peek(summary -> assertThat(summary.get(0).getTargetInterval()).isEqualTo(NOMINAL_RANGE))
                .count()).isEqualTo(2);
    }

    @Test
    public void testGetValidationSummaryIfMetrologyPurposeIsNotLinkedToUsagePoint() {
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.empty());
        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage(equalTo("Metrology contract with id 777 is not found on usage point Mrmrmrrr."));
        usagePointDataCompletionService.getDataCompletionStatistics(effectiveMetrologyConfiguration, metrologyContract, NOMINAL_RANGE);
    }

    @Test
    public void testGetValidationSummaryIfDeliverableIsDuplicatedOnContract() {
        when(metrologyContract.getDeliverables()).thenReturn(Arrays.asList(deliverable1, deliverable1));
        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage(equalTo("Same reading type deliverable appear several times on metrology contract with id 777."));
        usagePointDataCompletionService.getDataCompletionStatistics(effectiveMetrologyConfiguration, metrologyContract, NOMINAL_RANGE);
    }

    @Test
    public void testGetValidationSummaryNominalCase() {
        summary = usagePointDataCompletionService.getDataCompletionStatistics(channel, NOMINAL_RANGE).stream().filter(sum -> sum.getType() == ChannelDataCompletionSummaryType.GENERAL).findFirst().get();
        editedSummary = usagePointDataCompletionService.getDataCompletionStatistics(channel, NOMINAL_RANGE).stream().filter(sum -> sum.getType() == ChannelDataCompletionSummaryType.EDITED).findFirst().get();
        validSummary = usagePointDataCompletionService.getDataCompletionStatistics(channel, NOMINAL_RANGE).stream().filter(sum -> sum.getType() == ChannelDataCompletionSummaryType.VALID).findFirst().get();
        assertThat(summary.getValues()).contains(
                MapEntry.entry(ChannelDataCompletionSummaryFlag.SUSPECT, 2),
                MapEntry.entry(ChannelDataCompletionSummaryFlag.NOT_VALIDATED, 2),
                MapEntry.entry(ChannelDataCompletionSummaryFlag.VALID, 5));
        assertThat(editedSummary.getValues()).contains(
                MapEntry.entry(ChannelDataModificationSummaryFlags.EDITED, 1),
                MapEntry.entry(ChannelDataModificationSummaryFlags.ADDED, 1),
                MapEntry.entry(ChannelDataModificationSummaryFlags.REMOVED, 1));
        assertThat(validSummary.getValues()).contains(
                MapEntry.entry(ValidChannelDataSummaryFlags.VALID, 4),
                MapEntry.entry(ValidChannelDataSummaryFlags.ESTIMATED, 1));
        assertThat(summary.getTargetInterval()).isEqualTo(NOMINAL_RANGE);
        assertThat(summary.getSum()).isEqualTo(9);
        assertThat(editedSummary.getSum()).isEqualTo(3);
        assertThat(validSummary.getSum()).isEqualTo(5);
        assertThat(summary.getType()).isEqualTo(ChannelDataCompletionSummaryType.GENERAL);
        assertThat(editedSummary.getType()).isEqualTo(ChannelDataCompletionSummaryType.EDITED);
        assertThat(validSummary.getType()).isEqualTo(ChannelDataCompletionSummaryType.VALID);
    }

    @Test
    public void testGetValidationSummaryForInfiniteRange() {
        summary = usagePointDataCompletionService.getDataCompletionStatistics(channel, Range.all()).get(0);
        assertThat(summary.getValues()).contains(
                MapEntry.entry(ChannelDataCompletionSummaryFlag.SUSPECT, 2),
                MapEntry.entry(ChannelDataCompletionSummaryFlag.NOT_VALIDATED, 3),
                MapEntry.entry(ChannelDataCompletionSummaryFlag.VALID, 5));
        assertThat(summary.getSum()).isEqualTo(10);
        assertThat(summary.getTargetInterval()).isEqualTo(Range.all());
    }

    @Test
    public void testGetValidationSummaryForDifferentNumberOfDeliverables() {
        when(metrologyContract.getDeliverables()).thenReturn(Collections.emptyList());
        summaries = usagePointDataCompletionService.getDataCompletionStatistics(effectiveMetrologyConfiguration, metrologyContract, NOMINAL_RANGE);
        assertThat(summaries).isEmpty();
        when(metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(deliverable2));
        summaries = usagePointDataCompletionService.getDataCompletionStatistics(effectiveMetrologyConfiguration, metrologyContract, NOMINAL_RANGE);
        assertThat(summaries.keySet()).containsExactly(deliverable2);
        assertThat(summaries.values().stream()
                .peek(summary -> assertThat(summary.get(0).getValues()).contains(
                        MapEntry.entry(ChannelDataCompletionSummaryFlag.SUSPECT, 2),
                        MapEntry.entry(ChannelDataCompletionSummaryFlag.NOT_VALIDATED, 2),
                        MapEntry.entry(ChannelDataCompletionSummaryFlag.VALID, 5)
                ))
                .peek(summary -> assertThat(summary.get(0).getSum()).isEqualTo(9))
                .peek(summary -> assertThat(summary.get(0).getTargetInterval()).isEqualTo(NOMINAL_RANGE))
                .count()).isEqualTo(1);
        when(metrologyContract.getDeliverables()).thenReturn(Arrays.asList(deliverable2, deliverable1));
        ReadingType readingType2 = mock(ReadingType.class);
        when(deliverable2.getReadingType()).thenReturn(readingType2);
        Channel channel2 = mock(Channel.class);
        when(channelsContainer.getChannel(readingType2)).thenReturn(Optional.of(channel2));
        ReadingQualityWithTypeFetcher fetcher2 = mock(ReadingQualityWithTypeFetcher.class, Answers.RETURNS_DEEP_STUBS.get());
        when(channel2.findReadingQualities()).thenReturn(fetcher2);
        when(fetcher2.inTimeInterval(any())
                .actual()
                .ofQualitySystem(QualityCodeSystem.MDM)
                .ofQualityIndices(ImmutableSet.of(QualityCodeIndex.SUSPECT, QualityCodeIndex.KNOWNMISSINGREAD))
                .orOfAnotherTypeInSameSystems()
                .ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.EDITED, QualityCodeCategory.ESTIMATED))
                .stream())
                .thenReturn(Stream.of());
        when(channel2.isRegular()).thenReturn(true);
        when(channel2.toList(any())).thenReturn(Arrays.asList(MISSING_DATE, REJECTED_DATE, LAST_CHECKED, UNCHECKED_DATE, NOW));
        when(validationService.getLastChecked(channel2)).thenReturn(Optional.of(UNCHECKED_DATE));
        Range<Instant> interval = Range.openClosed(FIRST_DATE, NOW);
        summaries = usagePointDataCompletionService.getDataCompletionStatistics(effectiveMetrologyConfiguration, metrologyContract, interval);
        assertThat(summaries.keySet()).containsExactly(deliverable2, deliverable1);
        summary = summaries.get(deliverable1).get(0);
        assertThat(summary.getValues()).contains(
                MapEntry.entry(ChannelDataCompletionSummaryFlag.SUSPECT, 2),
                MapEntry.entry(ChannelDataCompletionSummaryFlag.NOT_VALIDATED, 2),
                MapEntry.entry(ChannelDataCompletionSummaryFlag.VALID, 4));
        assertThat(summary.getSum()).isEqualTo(8);
        assertThat(summary.getTargetInterval()).isEqualTo(interval);
        summary = summaries.get(deliverable2).get(0);
        assertThat(summary.getValues()).contains(
                MapEntry.entry(ChannelDataCompletionSummaryFlag.NOT_VALIDATED, 1),
                MapEntry.entry(ChannelDataCompletionSummaryFlag.VALID, 4));
        assertThat(summary.getSum()).isEqualTo(5);
        assertThat(summary.getTargetInterval()).isEqualTo(interval);
    }

    @Test
    public void testGetValidationSummaryNoLastChecked() {
        when(validationService.getLastChecked(channel)).thenReturn(Optional.empty());
        summary = usagePointDataCompletionService.getDataCompletionStatistics(channel, NOMINAL_RANGE).get(0);
        assertThat(summary.getValues()).contains(
                MapEntry.entry(ChannelDataCompletionSummaryFlag.NOT_VALIDATED, 9));
        assertThat(summary.getSum()).isEqualTo(9);
        assertThat(summary.getTargetInterval()).isEqualTo(NOMINAL_RANGE);
    }

    @Test
    public void testGetValidationSummaryForRegister() {
        when(channel.isRegular()).thenReturn(false);
        when(channel.toList(NOMINAL_RANGE)).thenReturn(Collections.emptyList());
        List<BaseReadingRecord> readings = getInstantsFromInterval(NOMINAL_RANGE).stream()
                .map(instant -> {
                    BaseReadingRecord reading = mock(BaseReadingRecord.class);
                    when(reading.getTimeStamp()).thenReturn(instant);
                    return reading;
                })
                .collect(Collectors.toList());
        when(channel.getReadings(NOMINAL_RANGE)).thenReturn(readings);
        summary = usagePointDataCompletionService.getDataCompletionStatistics(channel, NOMINAL_RANGE).get(0);
        assertThat(summary.getValues()).contains(
                MapEntry.entry(ChannelDataCompletionSummaryFlag.SUSPECT, 2),
                MapEntry.entry(ChannelDataCompletionSummaryFlag.VALID, 5),
                MapEntry.entry(ChannelDataCompletionSummaryFlag.NOT_VALIDATED, 2));
        assertThat(summary.getSum()).isEqualTo(9);
        assertThat(summary.getTargetInterval()).isEqualTo(NOMINAL_RANGE);
    }

    private static List<Instant> getInstantsFromInterval(Range<Instant> interval) {
        return Stream.of(FIRST_DATE, EDITED_DATE, ESTIMATED_DATE, SUSPECT_DATE, LAST_CHECKED, UNCHECKED_DATE, NOW, FUTURE_DATE)
                .filter(interval::contains)
                .collect(Collectors.toList());
    }
}
