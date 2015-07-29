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
import com.elster.jupiter.estimation.Priority;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EstimationServiceImplTest {
    private static final Logger LOGGER = Logger.getLogger(EstimationServiceImplTest.class.getName());

    private final ReadingQualityType readingQualityType1 = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, 1);
    private final ReadingQualityType readingQualityType2 = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeCategory.ESTIMATED, 2);
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
    private TaskService taskService;
    @Mock
    private MeteringGroupsService meteringGroupService;
    @Mock
    private MessageService messageService;
    @Mock
    private com.elster.jupiter.metering.MeterActivation meterActivation;
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
    @Mock
    private Estimator estimator1, estimator2;
    @Mock
    private UserService userService;
    @Mock
    private CimChannel cimChannel1, cimChannel2;
    @Mock
    private Meter meter;
    private LogRecorder logRecorder;

    @Before
    public void setUp() {
        this.estimationService = new EstimationServiceImpl(meteringService, ormService, queryService, nlsService, eventService, taskService, meteringGroupService, messageService, timeService, userService);

        estimationService.addEstimationResolver(resolver);

        doReturn(Arrays.asList(readingType1, readingType2)).when(meterActivation).getReadingTypes();
        doReturn(Arrays.asList(ruleSet)).when(resolver).resolve(meterActivation);
        doReturn(Priority.NORMAL).when(resolver).getPriority();
        doReturn(Arrays.asList(rule1, rule2)).when(ruleSet).getRules();
        doReturn(ImmutableSet.of(readingType1, readingType2)).when(rule1).getReadingTypes();
        doReturn(ImmutableSet.of(readingType1, readingType2)).when(rule2).getReadingTypes();
        doReturn(Arrays.asList(channel)).when(meterActivation).getChannels();
        doReturn(Arrays.asList(readingType1, readingType2)).when(channel).getReadingTypes();
        doReturn(true).when(channel).isRegular();
        List<ReadingQualityRecord> readingQualityRecords = readingQualities();
        doReturn(readingQualityRecords).when(channel).findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), Range.<Instant>all());
        doReturn(Optional.of(cimChannel1)).when(channel).getCimChannel(readingType1);
        doReturn(Optional.of(cimChannel2)).when(channel).getCimChannel(readingType2);
        doReturn(readingQualityRecords).when(cimChannel1).findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), Range.<Instant>all());
        doReturn(readingQualityRecords).when(cimChannel2).findReadingQuality(ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT), Range.<Instant>all());
        doAnswer(invocation -> ((Instant) invocation.getArguments()[0]).plus(Duration.ofMinutes(15))).when(channel).getNextDateTime(any());
        doReturn(estimator1).when(rule1).createNewEstimator();
        doReturn(estimator2).when(rule2).createNewEstimator();
        doReturn(true).when(rule1).isActive();
        doReturn(true).when(rule2).isActive();
        doReturn(meterActivation).when(channel).getMeterActivation();
        doReturn(Optional.of(meter)).when(meterActivation).getMeter();
        doAnswer(invocation -> {
            List<EstimationBlock> estimationBlocks = (List<EstimationBlock>) invocation.getArguments()[0];
            SimpleEstimationResult.EstimationResultBuilder builder = SimpleEstimationResult.builder();
            estimationBlocks.stream().findFirst().ifPresent((block) -> {
                builder.addEstimated(block);
                block.setReadingQualityType(readingQualityType1);
            });
            estimationBlocks.stream().skip(1).forEach(builder::addRemaining);
            return builder.build();
        }).when(estimator1).estimate(any());
        doAnswer(invocation -> {
            List<EstimationBlock> estimationBlocks = (List<EstimationBlock>) invocation.getArguments()[0];
            SimpleEstimationResult.EstimationResultBuilder builder = SimpleEstimationResult.builder();
            estimationBlocks.stream().reduce((a, b) -> b).ifPresent((block) -> {
                builder.addEstimated(block);
                block.setReadingQualityType(readingQualityType2);
            });
            estimationBlocks.subList(0, Math.max(0, estimationBlocks.size() - 1)).stream().forEach(builder::addRemaining);
            return builder.build();
        }).when(estimator2).estimate(any());

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
        EstimationReport report = estimationService.previewEstimate(meterActivation, Range.<Instant>all(), LOGGER);
        assertThat(report.getResults()).hasSize(2).containsKey(readingType1).containsKey(readingType2);

        EstimationResult estimationResult = report.getResults().get(readingType1);

        assertThat(estimationResult.estimated()).hasSize(2);
        assertThat(estimationResult.estimated().get(0).getReadingQualityType()).isEqualTo(readingQualityType1);
        assertThat(estimationResult.estimated().get(1).getReadingQualityType()).isEqualTo(readingQualityType2);
        assertThat(estimationResult.remainingToBeEstimated()).hasSize(1);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Successful estimation "));
    }

    @Test
    public void testPreviewEstimateWhenRuleIsNotActive() {
        doReturn(false).when(rule1).isActive();
        doReturn(false).when(rule2).isActive();

        EstimationReport report = estimationService.previewEstimate(meterActivation, Range.<Instant>all(), LOGGER);
        assertThat(report.getResults()).hasSize(2).containsKey(readingType1).containsKey(readingType2);

        EstimationResult estimationResult = report.getResults().get(readingType1);

        assertThat(estimationResult.estimated()).hasSize(0);
        assertThat(estimationResult.remainingToBeEstimated()).hasSize(3);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.endsWith(" could not be estimated."));
    }
}