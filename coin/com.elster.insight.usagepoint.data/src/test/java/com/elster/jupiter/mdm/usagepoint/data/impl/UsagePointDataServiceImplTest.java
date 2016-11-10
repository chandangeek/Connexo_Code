package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummary;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummaryFlag;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingQualityWithTypeFetcher;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Clock;
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointDataServiceImplTest {
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
    private Clock clock;
    @Mock
    private OrmService ormService;
    @Mock
    private DataModel dataModel;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ValidationService validationService;
    @Mock
    private NlsService nlsService;
    @Mock
    private CustomPropertySetService customPropertySetService;
    @Mock
    private UsagePointConfigurationService usagePointConfigurationService;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private MetrologyPurpose metrologyPurpose;
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
    private ReadingQualityRecord error, suspect, missing, added, edited, removed, estimated;
    @Mock
    private FullInstaller installer;

    @Captor
    ArgumentCaptor<Range<Instant>> captor;

    private UsagePointDataService usagePointDataService;
    private ChannelDataValidationSummary summary;
    private Map<ReadingTypeDeliverable, ChannelDataValidationSummary> summaries;

    @Before
    public void setUp() {
        when(dataModel.getInstance(Installer.class)).thenAnswer(invocation -> installer);
        when(ormService.newDataModel(eq(UsagePointDataService.COMPONENT_NAME), anyString())).thenReturn(dataModel);
        when(clock.instant()).thenReturn(NOW);
        when(usagePoint.getMRID()).thenReturn("Mrmrmrrr");
        when(metrologyContract.getId()).thenReturn(777L);
        when(metrologyContract.getDeliverables()).thenReturn(Arrays.asList(deliverable1, deliverable2));
        when(metrologyContract.getMetrologyPurpose()).thenReturn(metrologyPurpose);
        when(metrologyPurpose.getId()).thenReturn(311L);
        when(deliverable1.getReadingType()).thenReturn(readingType);
        when(deliverable2.getReadingType()).thenReturn(readingType);
        when(effectiveMetrologyConfiguration.getUsagePoint()).thenReturn(usagePoint);
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.of(channelsContainer));
        when(channelsContainer.getChannel(readingType)).thenReturn(Optional.of(channel));
        when(channelsContainer.getInterval()).thenReturn(Interval.of(Range.all()));

        usagePointDataService = new UsagePointDataServiceImpl(clock, ormService, meteringService, validationService,
                nlsService, customPropertySetService, usagePointConfigurationService, UpgradeModule.FakeUpgradeService.getInstance());

        when(validationService.getLastChecked(channel)).thenReturn(Optional.of(LAST_CHECKED));
        when(channel.isRegular()).thenReturn(true);
        when(channel.toList(captor.capture())).thenAnswer(invocation -> getInstantsFromInterval(captor.getValue()));
        when(channel.findReadingQualities()).thenReturn(fetcher);
        when(error.getType()).thenReturn(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.ERRORCODE));
        when(error.getReadingTimestamp()).thenReturn(MISSING_DATE);
        when(suspect.getType()).thenReturn(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT));
        when(suspect.getReadingTimestamp()).thenReturn(SUSPECT_DATE);
        when(missing.getType()).thenReturn(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.KNOWNMISSINGREAD));
        when(missing.getReadingTimestamp()).thenReturn(MISSING_DATE);
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
                .ofAnyQualityIndexInCategories(ImmutableSet.of(QualityCodeCategory.EDITED, QualityCodeCategory.ESTIMATED))
                .stream())
                .thenAnswer(invocation -> Stream.of(error, suspect, missing, added, edited, removed, estimated)
                        .filter(record -> captor.getValue().contains(record.getReadingTimestamp())));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetValidationSummaryForPeriodWithoutData() {
        Range<Instant> interval = Range.openClosed(NOW, NOW.plusNanos(1));
        summary = usagePointDataService.getValidationSummary(channel, interval);
        assertThat(summary.getValues()).isEmpty();
        assertThat(summary.getSum()).isZero();
        assertThat(summary.getTargetInterval()).isEqualTo(interval);
    }

    @Test
    public void testGetValidationSummaryForFuturePeriod() {
        Range<Instant> interval = Range.openClosed(NOW, FUTURE_DATE);
        summaries = usagePointDataService.getValidationSummary(effectiveMetrologyConfiguration, metrologyContract, interval);
        assertThat(summaries.keySet()).containsExactly(deliverable1, deliverable2);
        assertThat(summaries.values().stream()
                .peek(summary -> assertThat(summary.getValues()).containsExactly(MapEntry.entry(ChannelDataValidationSummaryFlag.NOT_VALIDATED, 1)))
                .peek(summary -> assertThat(summary.getSum()).isEqualTo(1))
                .peek(summary -> assertThat(summary.getTargetInterval()).isEqualTo(interval))
                .count()).isEqualTo(2);
    }

    @Test
    public void testGetValidationSummaryForUnvalidatedPeriod() {
        Range<Instant> interval = Range.openClosed(LAST_CHECKED, FUTURE_DATE);
        summaries = usagePointDataService.getValidationSummary(effectiveMetrologyConfiguration, metrologyContract, interval);
        assertThat(summaries.keySet()).containsExactly(deliverable1, deliverable2);
        assertThat(summaries.values().stream()
                .peek(summary -> assertThat(summary.getValues()).containsExactly(MapEntry.entry(ChannelDataValidationSummaryFlag.NOT_VALIDATED, 3)))
                .peek(summary -> assertThat(summary.getSum()).isEqualTo(3))
                .peek(summary -> assertThat(summary.getTargetInterval()).isEqualTo(interval))
                .count()).isEqualTo(2);
    }

    @Test
    public void testGetValidationSummaryForPeriodStartingBeforeChannelsContainerStart() {
        Range<Instant> actualRange = Range.openClosed(FIRST_DATE, NOW);
        when(channelsContainer.getInterval()).thenReturn(Interval.of(Range.atLeast(FIRST_DATE)));
        summaries = usagePointDataService.getValidationSummary(effectiveMetrologyConfiguration, metrologyContract, NOMINAL_RANGE);
        assertThat(summaries.keySet()).containsExactly(deliverable1, deliverable2);
        assertThat(summaries.values().stream()
                .peek(summary -> assertThat(summary.getValues()).containsExactly(
                        MapEntry.entry(ChannelDataValidationSummaryFlag.MISSING, 1),
                        MapEntry.entry(ChannelDataValidationSummaryFlag.SUSPECT, 1),
                        MapEntry.entry(ChannelDataValidationSummaryFlag.ESTIMATED, 1),
                        MapEntry.entry(ChannelDataValidationSummaryFlag.EDITED, 3),
                        MapEntry.entry(ChannelDataValidationSummaryFlag.NOT_VALIDATED, 2)
                ))
                .peek(summary -> assertThat(summary.getSum()).isEqualTo(8))
                .peek(summary -> assertThat(summary.getTargetInterval()).isEqualTo(actualRange))
                .count()).isEqualTo(2);
    }

    @Test
    public void testGetValidationSummaryForPeriodEndingBeforeChannelsContainerStart() {
        when(channelsContainer.getInterval()).thenReturn(Interval.of(Range.atLeast(NOW)));
        summaries = usagePointDataService.getValidationSummary(effectiveMetrologyConfiguration, metrologyContract, NOMINAL_RANGE);
        assertThat(summaries.keySet()).containsExactly(deliverable1, deliverable2);
        assertThat(summaries.values().stream()
                .peek(summary -> assertThat(summary.getValues()).isEmpty())
                .peek(summary -> assertThat(summary.getSum()).isZero())
                .peek(summary -> assertThat(summary.getTargetInterval()).isEqualTo(NOMINAL_RANGE))
                .count()).isEqualTo(2);
    }

    @Test
    public void testGetValidationSummaryForEmptyPeriod() {
        summaries = usagePointDataService.getValidationSummary(effectiveMetrologyConfiguration, metrologyContract, Range.openClosed(NOW, NOW));
        assertThat(summaries.keySet()).containsExactly(deliverable1, deliverable2);
        assertThat(summaries.values().stream()
                .peek(summary -> assertThat(summary.getValues()).isEmpty())
                .peek(summary -> assertThat(summary.getSum()).isZero())
                .peek(summary -> assertThat(summary.getTargetInterval()).isEqualTo(Range.openClosed(NOW, NOW)))
                .count()).isEqualTo(2);
    }

    @Test
    public void testGetValidationSummaryForPeriodStartingBeforeChannelsContainerEnd() {
        when(channelsContainer.getInterval()).thenReturn(Interval.of(Range.closedOpen(FIRST_DATE.minusSeconds(1), FIRST_DATE.minusNanos(1))));
        summaries = usagePointDataService.getValidationSummary(effectiveMetrologyConfiguration, metrologyContract, NOMINAL_RANGE);
        assertThat(summaries.keySet()).containsExactly(deliverable1, deliverable2);
        assertThat(summaries.values().stream()
                .peek(summary -> assertThat(summary.getValues()).isEmpty())
                .peek(summary -> assertThat(summary.getSum()).isZero())
                .peek(summary -> assertThat(summary.getTargetInterval()).isEqualTo(NOMINAL_RANGE))
                .count()).isEqualTo(2);
    }

    @Test
    public void testGetValidationSummaryIfMetrologyPurposeIsNotLinkedToUsagePoint() {
        when(effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract)).thenReturn(Optional.empty());
        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage(equalTo("Metrology purpose with id 311 is not found on usage point with MRID Mrmrmrrr."));
        usagePointDataService.getValidationSummary(effectiveMetrologyConfiguration, metrologyContract, NOMINAL_RANGE);
    }

    @Test
    public void testGetValidationSummaryIfDeliverableIsDuplicatedOnContract() {
        when(metrologyContract.getDeliverables()).thenReturn(Arrays.asList(deliverable1, deliverable1));
        expectedException.expect(LocalizedException.class);
        expectedException.expectMessage(equalTo("Same reading type deliverable appear several times on metrology contract with id 777."));
        usagePointDataService.getValidationSummary(effectiveMetrologyConfiguration, metrologyContract, NOMINAL_RANGE);
    }

    @Test
    public void testGetValidationSummaryNominalCase() {
        summary = usagePointDataService.getValidationSummary(channel, NOMINAL_RANGE);
        assertThat(summary.getValues()).containsExactly(
                MapEntry.entry(ChannelDataValidationSummaryFlag.MISSING, 1),
                MapEntry.entry(ChannelDataValidationSummaryFlag.SUSPECT, 1),
                MapEntry.entry(ChannelDataValidationSummaryFlag.ESTIMATED, 1),
                MapEntry.entry(ChannelDataValidationSummaryFlag.EDITED, 3),
                MapEntry.entry(ChannelDataValidationSummaryFlag.VALID, 1),
                MapEntry.entry(ChannelDataValidationSummaryFlag.NOT_VALIDATED, 2));
        assertThat(summary.getSum()).isEqualTo(9);
        assertThat(summary.getTargetInterval()).isEqualTo(NOMINAL_RANGE);
    }

    @Test
    public void testGetValidationSummaryForInfiniteRange() {
        summary = usagePointDataService.getValidationSummary(channel, Range.all());
        assertThat(summary.getValues()).containsExactly(
                MapEntry.entry(ChannelDataValidationSummaryFlag.MISSING, 1),
                MapEntry.entry(ChannelDataValidationSummaryFlag.SUSPECT, 1),
                MapEntry.entry(ChannelDataValidationSummaryFlag.ESTIMATED, 1),
                MapEntry.entry(ChannelDataValidationSummaryFlag.EDITED, 3),
                MapEntry.entry(ChannelDataValidationSummaryFlag.VALID, 1),
                MapEntry.entry(ChannelDataValidationSummaryFlag.NOT_VALIDATED, 3));
        assertThat(summary.getSum()).isEqualTo(10);
        assertThat(summary.getTargetInterval()).isEqualTo(Range.all());
    }

    @Test
    public void testGetValidationSummaryForDifferentNumberOfDeliverables() {
        when(metrologyContract.getDeliverables()).thenReturn(Collections.emptyList());
        summaries = usagePointDataService.getValidationSummary(effectiveMetrologyConfiguration, metrologyContract, NOMINAL_RANGE);
        assertThat(summaries).isEmpty();
        when(metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(deliverable2));
        summaries = usagePointDataService.getValidationSummary(effectiveMetrologyConfiguration, metrologyContract, NOMINAL_RANGE);
        assertThat(summaries.keySet()).containsExactly(deliverable2);
        assertThat(summaries.values().stream()
                .peek(summary -> assertThat(summary.getValues()).containsExactly(
                        MapEntry.entry(ChannelDataValidationSummaryFlag.MISSING, 1),
                        MapEntry.entry(ChannelDataValidationSummaryFlag.SUSPECT, 1),
                        MapEntry.entry(ChannelDataValidationSummaryFlag.ESTIMATED, 1),
                        MapEntry.entry(ChannelDataValidationSummaryFlag.EDITED, 3),
                        MapEntry.entry(ChannelDataValidationSummaryFlag.VALID, 1),
                        MapEntry.entry(ChannelDataValidationSummaryFlag.NOT_VALIDATED, 2)
                ))
                .peek(summary -> assertThat(summary.getSum()).isEqualTo(9))
                .peek(summary -> assertThat(summary.getTargetInterval()).isEqualTo(NOMINAL_RANGE))
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
        summaries = usagePointDataService.getValidationSummary(effectiveMetrologyConfiguration, metrologyContract, interval);
        assertThat(summaries.keySet()).containsExactly(deliverable2, deliverable1);
        summary = summaries.get(deliverable1);
        assertThat(summary.getValues()).containsExactly(
                MapEntry.entry(ChannelDataValidationSummaryFlag.MISSING, 1),
                MapEntry.entry(ChannelDataValidationSummaryFlag.SUSPECT, 1),
                MapEntry.entry(ChannelDataValidationSummaryFlag.ESTIMATED, 1),
                MapEntry.entry(ChannelDataValidationSummaryFlag.EDITED, 3),
                MapEntry.entry(ChannelDataValidationSummaryFlag.NOT_VALIDATED, 2));
        assertThat(summary.getSum()).isEqualTo(8);
        assertThat(summary.getTargetInterval()).isEqualTo(interval);
        summary = summaries.get(deliverable2);
        assertThat(summary.getValues()).containsExactly(
                MapEntry.entry(ChannelDataValidationSummaryFlag.VALID, 4),
                MapEntry.entry(ChannelDataValidationSummaryFlag.NOT_VALIDATED, 1));
        assertThat(summary.getSum()).isEqualTo(5);
        assertThat(summary.getTargetInterval()).isEqualTo(interval);
    }

    @Test
    public void testGetValidationSummaryFlagPrecedence() {
        Range<Instant> oneTimestampRange = Range.singleton(MISSING_DATE);
        when(suspect.getReadingTimestamp()).thenReturn(MISSING_DATE);
        when(added.getReadingTimestamp()).thenReturn(MISSING_DATE);
        when(edited.getReadingTimestamp()).thenReturn(MISSING_DATE);
        when(removed.getReadingTimestamp()).thenReturn(MISSING_DATE);
        when(estimated.getReadingTimestamp()).thenReturn(MISSING_DATE);
        List<Pair<ChannelDataValidationSummaryFlag, ReadingQualityRecord>> expectedMapping = ImmutableList.of(
                Pair.of(ChannelDataValidationSummaryFlag.MISSING, missing), // check that flag is missing, remove missing
                Pair.of(ChannelDataValidationSummaryFlag.SUSPECT, suspect), // etc.
                Pair.of(ChannelDataValidationSummaryFlag.SUSPECT, error),
                Pair.of(ChannelDataValidationSummaryFlag.ESTIMATED, estimated),
                Pair.of(ChannelDataValidationSummaryFlag.EDITED, edited),
                Pair.of(ChannelDataValidationSummaryFlag.EDITED, added),
                Pair.of(ChannelDataValidationSummaryFlag.EDITED, removed)
        );
        expectedMapping.forEach(pair -> {
            summary = usagePointDataService.getValidationSummary(channel, oneTimestampRange);
            assertThat(summary.getValues()).containsExactly(
                    MapEntry.entry(pair.getFirst(), 1));
            assertThat(summary.getSum()).isEqualTo(1);
            assertThat(summary.getTargetInterval()).isEqualTo(oneTimestampRange);
            ReadingQualityRecord mock = pair.getLast();
            when(mock.getReadingTimestamp()).thenReturn(FIRST_DATE);
        });
        // now no qualities, no readings
        summary = usagePointDataService.getValidationSummary(channel, oneTimestampRange);
        assertThat(summary.getValues()).isEmpty();
        assertThat(summary.getSum()).isEqualTo(0);
        assertThat(summary.getTargetInterval()).isEqualTo(oneTimestampRange);
        // but if we add reading...
        when(channel.toList(oneTimestampRange)).thenReturn(Collections.singletonList(MISSING_DATE));
        summary = usagePointDataService.getValidationSummary(channel, oneTimestampRange);
        assertThat(summary.getValues()).containsExactly(
                MapEntry.entry(ChannelDataValidationSummaryFlag.VALID, 1));
        assertThat(summary.getSum()).isEqualTo(1);
        assertThat(summary.getTargetInterval()).isEqualTo(oneTimestampRange);
    }

    @Test
    public void testGetValidationSummaryNoLastChecked() {
        when(validationService.getLastChecked(channel)).thenReturn(Optional.empty());
        summary = usagePointDataService.getValidationSummary(channel, NOMINAL_RANGE);
        assertThat(summary.getValues()).containsExactly(
                MapEntry.entry(ChannelDataValidationSummaryFlag.NOT_VALIDATED, 7));
        assertThat(summary.getSum()).isEqualTo(7);
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
        summary = usagePointDataService.getValidationSummary(channel, NOMINAL_RANGE);
        assertThat(summary.getValues()).containsExactly(
                MapEntry.entry(ChannelDataValidationSummaryFlag.MISSING, 1),
                MapEntry.entry(ChannelDataValidationSummaryFlag.SUSPECT, 1),
                MapEntry.entry(ChannelDataValidationSummaryFlag.ESTIMATED, 1),
                MapEntry.entry(ChannelDataValidationSummaryFlag.EDITED, 3),
                MapEntry.entry(ChannelDataValidationSummaryFlag.VALID, 1),
                MapEntry.entry(ChannelDataValidationSummaryFlag.NOT_VALIDATED, 2));
        assertThat(summary.getSum()).isEqualTo(9);
        assertThat(summary.getTargetInterval()).isEqualTo(NOMINAL_RANGE);
    }

    private static List<Instant> getInstantsFromInterval(Range<Instant> interval) {
        return Stream.of(FIRST_DATE, EDITED_DATE, ESTIMATED_DATE, SUSPECT_DATE, LAST_CHECKED, UNCHECKED_DATE, NOW, FUTURE_DATE)
                .filter(interval::contains)
                .collect(Collectors.toList());
    }
}
