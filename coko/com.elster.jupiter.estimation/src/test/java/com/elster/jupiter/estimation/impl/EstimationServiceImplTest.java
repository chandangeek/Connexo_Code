/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationReport;
import com.elster.jupiter.estimation.EstimationResolver;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.EstimatorFactory;
import com.elster.jupiter.estimation.Priority;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingQualityWithTypeFetcher;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EstimationServiceImplTest {
    private static final Logger LOGGER = Logger.getLogger(EstimationServiceImplTest.class.getName());

    private final ReadingQualityType readingQualityType1 = ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 1);
    private final ReadingQualityType readingQualityType2 = ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 2);
    private EstimationServiceImpl estimationService;

    @Mock
    private TimeService timeService;
    @Mock
    private MeteringService meteringService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OrmService ormService;
    @Mock
    private QueryService queryService;
    @Mock
    private NlsService nlsService;
    @Mock
    private EventService eventService;
    @Mock
    private EventTypeBuilder eventTypeBuilder;
    @Mock
    private TaskService taskService;
    @Mock
    private MeteringGroupsService meteringGroupService;
    @Mock
    private QueueTableSpec queueTableSpec;
    @Mock
    private DestinationSpec destinationSpec;
    @Mock
    private SubscriberSpec subscriberSpec;
    @Mock
    private MessageService messageService;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private ReadingType readingType1, readingType2;
    @Mock
    private EstimationResolver resolver;
    @Mock
    private IEstimationRuleSet ruleSet;
    @Mock
    private IEstimationRule rule1, rule2;
    @Mock
    private Channel channel;
    @Mock(name = "Estimator1")
    private Estimator estimator1;
    @Mock(name = "Estimator2")
    private Estimator estimator2;
    @Mock
    private EstimatorFactory factory;
    @Mock
    private UserService userService;
    @Mock
    private User user;
    @Mock
    private Group group;
    @Mock
    private CimChannel cimChannel1, cimChannel2;
    @Mock
    private Meter meter;
    @Mock
    private RelativePeriodCategory relativePeriodCategory;
    @Mock
    private RelativePeriod relativePeriod;
    @Mock
    private UpgradeService upgradeService;
    @Mock
    private ReadingQualityWithTypeFetcher fetcher;
    @Mock
    private ReadingQualityWithTypeFetcher emptyFetcher;

    private LogRecorder logRecorder;

    @Before
    public void setUp() {
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(messageService.getQueueTableSpec(any(String.class))).thenReturn(Optional.of(queueTableSpec));
        when(queueTableSpec.createDestinationSpec(any(String.class), any(Integer.class))).thenReturn(destinationSpec);
        doNothing().when(destinationSpec).save();
        doNothing().when(destinationSpec).activate();
        when(destinationSpec.subscribe(any(TranslationKey.class), anyString(), any(Layer.class))).thenReturn(subscriberSpec);

        when(timeService.findRelativePeriodCategoryByName(any(String.class))).thenReturn(Optional.of(relativePeriodCategory));
        when(timeService.findRelativePeriodByName(any(String.class))).thenReturn(Optional.of(relativePeriod));

        when(eventService.buildEventTypeWithTopic(any(String.class))).thenReturn(eventTypeBuilder);
        when(eventTypeBuilder.name(any(String.class))).thenReturn(eventTypeBuilder);
        when(eventTypeBuilder.component(any(String.class))).thenReturn(eventTypeBuilder);
        when(eventTypeBuilder.category(any(String.class))).thenReturn(eventTypeBuilder);
        when(eventTypeBuilder.scope(any(String.class))).thenReturn(eventTypeBuilder);
        when(eventTypeBuilder.withProperty(any(String.class), any(ValueType.class), any(String.class))).thenReturn(eventTypeBuilder);

        when(userService.findGroup(any(String.class))).thenReturn(Optional.of(group));
        when(userService.createUser(any(String.class), any(String.class))).thenReturn(user);

        this.estimationService = new EstimationServiceImpl(meteringService, ormService, queryService, nlsService, eventService, taskService, meteringGroupService, messageService, timeService, userService, upgradeService, Clock
                .systemDefaultZone());

        estimationService.addEstimationResolver(resolver);
        estimationService.addEstimatorFactory(factory);

        String estimator1Name = estimator1.toString();
        String estimator2Name = estimator2.toString();
        when(factory.available()).thenReturn(Arrays.asList(estimator1Name, estimator2Name));
        when(factory.createTemplate(estimator1Name)).thenReturn(estimator1);
        when(factory.createTemplate(estimator2Name)).thenReturn(estimator2);

        doReturn(Arrays.asList(readingType1, readingType2)).when(meterActivation).getReadingTypes();
        doReturn(Collections.singletonList(ruleSet)).when(resolver).resolve(channelsContainer);
        doReturn(QualityCodeSystem.MDC).when(ruleSet).getQualityCodeSystem();
        doReturn(Priority.NORMAL).when(resolver).getPriority();
        doReturn(Arrays.asList(rule1, rule2)).when(ruleSet).getRules();
        doReturn(ImmutableSet.of(readingType1, readingType2)).when(rule1).getReadingTypes();
        doReturn(ImmutableSet.of(readingType1, readingType2)).when(rule2).getReadingTypes();
        doReturn(Collections.singletonList(channel)).when(channelsContainer).getChannels();
        doReturn(Arrays.asList(readingType1, readingType2)).when(channel).getReadingTypes();
        doReturn(true).when(channel).isRegular();
        List<ReadingQualityRecord> readingQualityRecords = readingQualities();
        doReturn(fetcher).when(channel).findReadingQualities();
        doReturn(fetcher).when(fetcher).ofQualitySystems(Collections.singleton(QualityCodeSystem.MDC));
        doReturn(emptyFetcher).when(fetcher).ofQualitySystems(Collections.singleton(QualityCodeSystem.MDM));
        doReturn(fetcher).when(fetcher).ofQualityIndex(any(QualityCodeIndex.class));
        doReturn(emptyFetcher).when(emptyFetcher).ofQualityIndex(any(QualityCodeIndex.class));
        doReturn(fetcher).when(fetcher).inTimeInterval(Range.all());
        doReturn(emptyFetcher).when(emptyFetcher).inTimeInterval(Range.all());
        doReturn(readingQualityRecords).when(fetcher).collect();
        doReturn(Collections.emptyList()).when(emptyFetcher).collect();
        doReturn(Optional.of(cimChannel1)).when(channel).getCimChannel(readingType1);
        doReturn(Optional.of(cimChannel2)).when(channel).getCimChannel(readingType2);
        doReturn(fetcher).when(cimChannel1).findReadingQualities();
        doReturn(fetcher).when(cimChannel2).findReadingQualities();
        doAnswer(invocation -> ((Instant) invocation.getArguments()[0]).plus(Duration.ofMinutes(15))).when(channel).getNextDateTime(any());
        doReturn(estimator1).when(rule1).createNewEstimator();
        doReturn(estimator2).when(rule2).createNewEstimator();
        doReturn(true).when(rule1).isActive();
        doReturn(true).when(rule2).isActive();
        doReturn(channelsContainer).when(channel).getChannelsContainer();
        doReturn(Optional.of(meter)).when(channelsContainer).getMeter();
        when(channelsContainer.getReadingTypes(any())).thenReturn(new HashSet<>(Arrays.asList(readingType1, readingType2)));
        doAnswer(invocation -> {
            List<EstimationBlock> estimationBlocks = (List<EstimationBlock>) invocation.getArguments()[0];
            SimpleEstimationResult.EstimationResultBuilder builder = SimpleEstimationResult.builder();
            estimationBlocks.stream().findFirst().ifPresent((block) -> {
                builder.addEstimated(block);
                block.setReadingQualityType(readingQualityType1);
            });
            estimationBlocks.stream().skip(1).forEach(builder::addRemaining);
            return builder.build();
        }).when(estimator1).estimate(anyListOf(EstimationBlock.class), eq(QualityCodeSystem.MDC));
        doAnswer(invocation -> {
            List<EstimationBlock> estimationBlocks = (List<EstimationBlock>) invocation.getArguments()[0];
            SimpleEstimationResult.EstimationResultBuilder builder = SimpleEstimationResult.builder();
            estimationBlocks.stream().reduce((a, b) -> b).ifPresent((block) -> {
                builder.addEstimated(block);
                block.setReadingQualityType(readingQualityType2);
            });
            estimationBlocks.subList(0, Math.max(0, estimationBlocks.size() - 1)).stream().forEach(builder::addRemaining);
            return builder.build();
        }).when(estimator2).estimate(anyListOf(EstimationBlock.class), eq(QualityCodeSystem.MDC));

        logRecorder = new LogRecorder(Level.ALL);
        LOGGER.addHandler(logRecorder);
    }

    // these will result in 3 estimation blocks
    private List<ReadingQualityRecord> readingQualities() {
        ImmutableList.Builder<ReadingQualityRecord> builder = ImmutableList.builder();
        builder.addAll(blockOfReadingQualities(5, ZonedDateTime.of(2013, 10, 1, 14, 15, 0, 0, TimeZoneNeutral.getMcMurdo())));
        builder.addAll(blockOfReadingQualities(1, ZonedDateTime.of(2013, 10, 1, 19, 45, 0, 0, TimeZoneNeutral.getMcMurdo())));
        builder.addAll(blockOfReadingQualities(3, ZonedDateTime.of(2013, 10, 2, 20, 0, 0, 0, TimeZoneNeutral.getMcMurdo())));

        return builder.build();
    }

    private List<ReadingQualityRecord> blockOfReadingQualities(int count, ZonedDateTime time) {
        ImmutableList.Builder<ReadingQualityRecord> builder = ImmutableList.builder();
        for (int i = 0; i < count; i++) {
            ReadingQualityRecord readingQuality = mock(ReadingQualityRecord.class);
            BaseReadingRecord baseReadingRecord = mock(BaseReadingRecord.class);
            doReturn(time.toInstant()).when(readingQuality).getReadingTimestamp();
            doReturn(Optional.of(baseReadingRecord)).when(readingQuality).getBaseReadingRecord();
            doReturn(time.toInstant()).when(baseReadingRecord).getTimeStamp();
            builder.add(readingQuality);
            time = time.plus(Duration.ofMinutes(15));
        }
        return builder.build();
    }

    @After
    public void tearDown() {
        LOGGER.removeHandler(logRecorder);
    }

    @Test
    public void testPreviewEstimate() {
        EstimationReport report = estimationService.previewEstimate(QualityCodeSystem.MDC, channelsContainer, Range.all(), LOGGER);
        assertThat(report.getResults()).hasSize(2).containsKey(readingType1).containsKey(readingType2);

        EstimationResult estimationResult = report.getResults().get(readingType1);

        assertThat(estimationResult.estimated()).hasSize(2);
        assertThat(estimationResult.estimated().get(0).getReadingQualityType()).isEqualTo(readingQualityType1);
        assertThat(estimationResult.estimated().get(1).getReadingQualityType()).isEqualTo(readingQualityType2);
        assertThat(estimationResult.remainingToBeEstimated()).hasSize(1);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Successful estimation "));
    }

    @Test
    public void testPreviewEstimateFromOtherSystem() {
        EstimationReport report = estimationService.previewEstimate(QualityCodeSystem.MDM, channelsContainer, Range.all(), LOGGER);
        assertThat(report.getResults()).isEmpty(); // no suspects, no rules
    }

    @Test
    public void testPreviewEstimateWhenRuleIsNotActive() {
        doReturn(false).when(rule1).isActive();
        doReturn(false).when(rule2).isActive();

        EstimationReport report = estimationService.previewEstimate(QualityCodeSystem.MDC, channelsContainer, Range.all(), LOGGER);
        assertThat(report.getResults()).hasSize(2).containsKey(readingType1).containsKey(readingType2);

        EstimationResult estimationResult = report.getResults().get(readingType1);

        assertThat(estimationResult.estimated()).hasSize(0);
        assertThat(estimationResult.remainingToBeEstimated()).hasSize(3);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.endsWith(" could not be estimated."));
    }

    @Test
    public void testGetAvailableEstimators() {
        when(estimator1.getSupportedQualityCodeSystems()).thenReturn(ImmutableSet.of(QualityCodeSystem.ENDDEVICE, QualityCodeSystem.EXTERNAL));
        when(estimator2.getSupportedQualityCodeSystems()).thenReturn(ImmutableSet.of(QualityCodeSystem.ENDDEVICE));
        assertThat(estimationService.getAvailableEstimators(QualityCodeSystem.ENDDEVICE))
                .as("Both estimators must be supported for ENDDEVICE")
                .containsExactly(estimator1, estimator2);
        assertThat(estimationService.getAvailableEstimators(QualityCodeSystem.EXTERNAL))
                .as("Only estimator1 must be supported for EXTERNAL")
                .containsExactly(estimator1);
        assertThat(estimationService.getAvailableEstimators(QualityCodeSystem.OTHER))
                .as("No estimator must be supported for OTHER")
                .isEmpty();
    }

    @Test
    public void testGetAvailableEstimatorImplementations() {
        when(estimator1.getSupportedQualityCodeSystems()).thenReturn(ImmutableSet.of(QualityCodeSystem.ENDDEVICE));
        when(estimator2.getSupportedQualityCodeSystems()).thenReturn(ImmutableSet.of(QualityCodeSystem.ENDDEVICE, QualityCodeSystem.EXTERNAL));
        assertThat(estimationService.getAvailableEstimatorImplementations(QualityCodeSystem.ENDDEVICE))
                .as("Both estimators must be supported for ENDDEVICE")
                .containsExactly("Estimator1", "Estimator2");
        assertThat(estimationService.getAvailableEstimatorImplementations(QualityCodeSystem.EXTERNAL))
                .as("Only estimator2 must be supported for EXTERNAL")
                .containsExactly("Estimator2");
        assertThat(estimationService.getAvailableEstimatorImplementations(QualityCodeSystem.OTHER))
                .as("No estimator must be supported for OTHER")
                .isEmpty();
    }
}
