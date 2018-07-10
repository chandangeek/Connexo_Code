/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl.actions;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.estimation.EstimationReport;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.insight.issue.datavalidation.IssueDataValidation;
import com.elster.insight.issue.datavalidation.NotEstimatedBlock;
import com.elster.insight.issue.datavalidation.impl.BaseTest;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RetryEstimationActionTest extends BaseTest {
    public static final String ISSUE_DEFAULT_TYPE_UUID = "usagepointdatavalidation";
    public static final String ISSUE_DEFAULT_REASON = "reason.default";

    private static long intervalStart = 1410774630000L;
    private static long intervalEnd = 1410828630000L;

    private IssueAction action;

    @Before
    public void setUp() throws Exception {
        action = getDefaultActionsFactory().createIssueAction(RetryEstimationAction.class.getName());
    }

    @Test
    @Transactional
    public void testCreateAndExecuteAction() {
        Map<String, Object> properties = new HashMap<>();

        List<NotEstimatedBlock> nonEstimatedBlocks = new ArrayList<>();
        NotEstimatedBlock nonEstimatedBlock = mock(NotEstimatedBlock.class);
        Channel channel = mock(Channel.class);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        MeterActivation meterActivation = mock(MeterActivation.class);
        Optional<? extends MeterActivation> meterActivationOptional = Optional.of(meterActivation);
        Meter meter = mock(Meter.class);

        when(nonEstimatedBlock.getChannel()).thenReturn(channel);
        nonEstimatedBlocks.add(nonEstimatedBlock);

        when(channel.getChannelsContainer()).thenReturn(channelsContainer);
        when(channelsContainer.getMeter()).thenReturn(Optional.of(meter));
        doReturn(meterActivationOptional).when(meter).getCurrentMeterActivation();

        when(nonEstimatedBlock.getStartTime()).thenReturn(Instant.ofEpochMilli(intervalStart));
        when(nonEstimatedBlock.getEndTime()).thenReturn(Instant.ofEpochMilli(intervalEnd));

        ReadingType readingType = mock(ReadingType.class);
        when(meterActivation.getReadingTypes()).thenReturn(Collections.singletonList(readingType));

        EstimationReport estimationReport = mock(EstimationReport.class, RETURNS_DEEP_STUBS);
        EstimationService estimationService = mock(EstimationService.class, RETURNS_DEEP_STUBS);
        EstimationResult estimationResult = mock(EstimationResult.class, RETURNS_DEEP_STUBS);
        doReturn(channelsContainer).when(meterActivation).getChannelsContainer();
        doReturn(estimationReport).when(estimationService).previewEstimate(any(QualityCodeSystem.class), any(ChannelsContainer.class), any(Range.class));
        doReturn(estimationResult).when(estimationService).previewEstimate(any(QualityCodeSystem.class), any(ChannelsContainer.class), any(Range.class), any(ReadingType.class), any());

        IssueDataValidation issue = mock(IssueDataValidation.class);
        IssueStatus issueStatus = mock(IssueStatus.class);
        when(issue.getNotEstimatedBlocks()).thenReturn(nonEstimatedBlocks);
        when(issue.getStatus()).thenReturn(issueStatus);
        when(issueStatus.isHistorical()).thenReturn(false);

        IssueActionResult actionResult = action.initAndValidate(properties).execute(issue);
        assertThat(actionResult.isSuccess()).isTrue();
    }
}
