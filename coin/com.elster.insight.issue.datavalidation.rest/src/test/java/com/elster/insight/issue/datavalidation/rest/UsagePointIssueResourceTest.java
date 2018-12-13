/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.rest;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidation;
import com.elster.insight.issue.datavalidation.UsagePointNotEstimatedBlock;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class UsagePointIssueResourceTest extends UsagePointIssueDataValidationApplicationJerseyTest {

    @Test
    public void testGetIssueById() {
        UsagePointIssueDataValidation issue = getDefaultIssue();
        doReturn(Optional.of(issue)).when(usagePointIssueDataValidationService).findIssue(1);
        ReadingType readingType = mockReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        ReadingType calculatedReadingType = mockReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.of(calculatedReadingType));

        Channel channel = mockChannel(Range.all());
        //when(device.getChannels()).thenReturn(Collections.singletonList(channel));
        // when(channel.getId()).thenReturn(5L);
        // when(channel.getReadingType()).thenReturn(readingType);
        // when(channel.getInterval()).thenReturn(TimeDuration.minutes(15));*/
        Instant now = Instant.now();
        UsagePointNotEstimatedBlock block1 = mockNotEstimatedBlock(now, now.plus(30, ChronoUnit.MINUTES), readingType);
        UsagePointNotEstimatedBlock block2 = mockNotEstimatedBlock(now.plus(45, ChronoUnit.MINUTES), now.plus(60, ChronoUnit.MINUTES), calculatedReadingType);
        when(issue.getNotEstimatedBlocks()).thenReturn(Arrays.asList(block1, block2));
        String response = target("/issues/1").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.version")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.creationDate")).isEqualTo(0);
        assertThat(jsonModel.<Number>get("$.dueDate")).isEqualTo(0);
        assertThat(jsonModel.<String>get("$.reason.id")).isEqualTo("1");
        assertThat(jsonModel.<String>get("$.reason.name")).isEqualTo("Reason");
        assertThat(jsonModel.<String>get("$.status.id")).isEqualTo("1");
        assertThat(jsonModel.<String>get("$.status.name")).isEqualTo("open");
        assertThat(jsonModel.<Boolean>get("$.status.allowForClosing")).isEqualTo(false);
        assertThat(jsonModel.<Number>get("$.assignee.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.assignee.name")).isEqualTo("Admin");
        assertThat(jsonModel.<Number>get("$.device.id")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.device.serialNumber")).isEqualTo("0.0.0.0.0.0.0.0");
        assertThat(jsonModel.<String>get("$.device.name")).isEqualTo("DefaultDevice");
        assertThat(jsonModel.<String>get("$.device.usagePoint")).isNull();
        assertThat(jsonModel.<String>get("$.device.serviceLocation")).isNull();
        assertThat(jsonModel.<String>get("$.device.serviceCategory")).isNull();

        assertThat(jsonModel.<List<?>>get("$.notEstimatedData")).hasSize(2);
        assertThat(jsonModel.<List<Number>>get("$.notEstimatedData[*].channelId")).containsExactly(5, 5);
        assertThat(jsonModel.<List<String>>get("$.notEstimatedData[*].readingType.mRID")).containsExactly("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0", "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        assertThat(jsonModel.<List<?>>get("$.notEstimatedData[0].notEstimatedBlocks")).hasSize(1);
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[0].startTime")).isEqualTo(now.toEpochMilli());
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[0].endTime")).isEqualTo(now.plus(30, ChronoUnit.MINUTES).toEpochMilli());
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[0].amountOfSuspects")).isEqualTo(2);

        assertThat(jsonModel.<List<?>>get("$.notEstimatedData[1].notEstimatedBlocks")).hasSize(1);
        assertThat(jsonModel.<Number>get("$.notEstimatedData[1].notEstimatedBlocks[0].startTime")).isEqualTo(now.plus(45, ChronoUnit.MINUTES).toEpochMilli());
        assertThat(jsonModel.<Number>get("$.notEstimatedData[1].notEstimatedBlocks[0].endTime")).isEqualTo(now.plus(60, ChronoUnit.MINUTES).toEpochMilli());
        assertThat(jsonModel.<Number>get("$.notEstimatedData[1].notEstimatedBlocks[0].amountOfSuspects")).isEqualTo(1);
    }

    @Test
    public void testGetIssueByIdWithRegisterNotEstimatedBlocks() {
        Instant now = Instant.now();

        UsagePointIssueDataValidation issue = getDefaultIssue();
        doReturn(Optional.of(issue)).when(usagePointIssueDataValidationService).findIssue(1);

        ReadingType readingType = mockReadingType("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        UsagePoint usagePoint = getDefaultUsagePoint();
        doReturn(Optional.of(usagePoint)).when(issue).getUsagePoint();
       /* Register register = mock(Register.class);
        when(usagePoint()).thenReturn(Collections.emptyList());
        when(device.getRegisters()).thenReturn(Collections.singletonList(register));
        when(register.getRegisterSpecId()).thenReturn(5L);
        when(register.getReadingType()).thenReturn(readingType);*/
        Channel channel = mockChannel(Range.all());
        BaseReadingRecord reading1 = mock(BaseReadingRecord.class);
        when(reading1.getTimeStamp()).thenReturn(now.plus(30, ChronoUnit.MINUTES));
        BaseReadingRecord reading2 = mock(BaseReadingRecord.class);
        when(reading2.getTimeStamp()).thenReturn(now.plus(90, ChronoUnit.MINUTES));
        when(channel.getReadings(Range.openClosed(Instant.EPOCH, now.plus(30, ChronoUnit.MINUTES)))).thenReturn(Collections.singletonList(reading1));
        when(channel.getReadings(Range.openClosed(now.plus(45, ChronoUnit.MINUTES), now.plus(90, ChronoUnit.MINUTES)))).thenReturn(Collections.singletonList(reading2));

        UsagePointNotEstimatedBlock block1 = mockNotEstimatedBlock(Instant.EPOCH, now.plus(30, ChronoUnit.MINUTES), readingType);
        when(block1.getChannel()).thenReturn(channel);
        UsagePointNotEstimatedBlock block2 = mockNotEstimatedBlock(now.plus(45, ChronoUnit.MINUTES), now.plus(90, ChronoUnit.MINUTES), readingType);
        when(block2.getChannel()).thenReturn(channel);
        when(issue.getNotEstimatedBlocks()).thenReturn(Arrays.asList(block1, block2));
        String response = target("/issues/1").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(1);

        assertThat(jsonModel.<List<?>>get("$.notEstimatedData")).hasSize(1);
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].registerId")).isEqualTo(5);
        assertThat(jsonModel.<String>get("$.notEstimatedData[0].readingType.mRID")).isEqualTo("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        assertThat(jsonModel.<List<?>>get("$.notEstimatedData[0].notEstimatedBlocks")).hasSize(2);
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[0].startTime")).isEqualTo(now.plus(30, ChronoUnit.MINUTES).toEpochMilli());
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[0].endTime")).isEqualTo(now.plus(30, ChronoUnit.MINUTES).toEpochMilli());
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[0].amountOfSuspects")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[1].startTime")).isEqualTo(now.plus(90, ChronoUnit.MINUTES).toEpochMilli());
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[1].endTime")).isEqualTo(now.plus(90, ChronoUnit.MINUTES).toEpochMilli());
        assertThat(jsonModel.<Number>get("$.notEstimatedData[0].notEstimatedBlocks[1].amountOfSuspects")).isEqualTo(1);
    }

    @Test
    public void testGetNonexistentIssueById() {
        when(usagePointIssueDataValidationService.findIssue(1)).thenReturn(Optional.empty());

        Response response = target("/issues/1").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }


    private UsagePointNotEstimatedBlock mockNotEstimatedBlock(Instant from, Instant to, ReadingType readingType) {
        UsagePointNotEstimatedBlock block = mock(UsagePointNotEstimatedBlock.class);
        when(block.getStartTime()).thenReturn(from);
        when(block.getEndTime()).thenReturn(to);
        when(block.getReadingType()).thenReturn(readingType);
        return block;
    }
}
