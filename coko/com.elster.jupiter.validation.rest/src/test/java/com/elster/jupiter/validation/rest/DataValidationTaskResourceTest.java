/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationOccurrenceFinder;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskBuilder;
import com.elster.jupiter.validation.DataValidationTaskStatus;
import com.elster.jupiter.validation.rest.impl.TranslationKeys;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataValidationTaskResourceTest extends BaseValidationRestTest {

    private static final int TASK_ID = 750;
    private static final String INSIGHT_KEY = "INS";
    private static final String MULTISENSE_KEY = "MDC";
    private static final String HEADER_NAME = "X-CONNEXO-APPLICATION-NAME";
    public static final long OK_VERSION = 23L;
    public static final long BAD_VERSION = 21L;

    DataValidationTaskBuilder taskBuilder;

    @Mock
    EndDeviceGroup endDeviceGroup;
    @Mock
    UsagePointGroup usagePointGroup;
    @Mock
    DataValidationTask dataValidationTask;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        dataValidationTask = mockDataValidationTask(TASK_ID, QualityCodeSystem.MDC);
        taskBuilder = FakeBuilder.initBuilderStub(dataValidationTask, DataValidationTaskBuilder.class);
        when(validationService.newTaskBuilder()).thenReturn(taskBuilder);
        when(validationService.findValidationTask(anyLong())).thenReturn(Optional.of(dataValidationTask));
    }

    @Test
    public void getApplicationSpecificTasks() {
        mockDataValidationTasks(mockDataValidationTask(13, QualityCodeSystem.MDC), mockDataValidationTask(15, QualityCodeSystem.MDM));

        String jsonFromMultisense = target("/validationtasks").request().header(HEADER_NAME, MULTISENSE_KEY).get(String.class);

        JsonModel jsonModelFromMultisense = JsonModel.create(jsonFromMultisense);
        assertThat(jsonModelFromMultisense.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModelFromMultisense.<Number>get("$.dataValidationTasks[0].id")).isEqualTo(13);
        assertThat(jsonModelFromMultisense.<String>get("$.dataValidationTasks[0].name")).isEqualTo("Name");

        String jsonFromInsight = target("/validationtasks").request().header(HEADER_NAME, INSIGHT_KEY).get(String.class);

        JsonModel jsonModelFromInsight = JsonModel.create(jsonFromInsight);
        assertThat(jsonModelFromInsight.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModelFromInsight.<Number>get("$.dataValidationTasks[0].id")).isEqualTo(15);
        assertThat(jsonModelFromInsight.<String>get("$.dataValidationTasks[0].name")).isEqualTo("Name");
        assertThat(jsonModelFromInsight.<String>get("$.dataValidationTasks[0].recurrence")).isEqualTo(TranslationKeys.NONE.getDefaultFormat());
    }

    @Test
    public void createTaskForDeviceGroup() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();
        info.deviceGroup = new IdWithDisplayValueInfo<>(1L, "Device group");
        Entity<DataValidationTaskInfo> json = Entity.json(info);

        Response response = target("/validationtasks").request().header(HEADER_NAME, MULTISENSE_KEY).post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void createTaskForMetrologyContract() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();
        info.deviceGroup = null;
        info.metrologyContract = new IdWithDisplayValueInfo<>(1L, "Billing");
        info.metrologyConfiguration = new IdWithDisplayValueInfo<>(1L, "Metrology configuration");
        Entity<DataValidationTaskInfo> json = Entity.json(info);

        Response response = target("/validationtasks").request().header(HEADER_NAME, INSIGHT_KEY).post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void createTaskWithMetrologyPurpose() {
        DataValidationTaskInfo dataValidationTaskInfo = new DataValidationTaskInfo();
        dataValidationTaskInfo.deviceGroup = null;
        dataValidationTaskInfo.metrologyPurpose = new IdWithDisplayValueInfo<>(1L, "Information");
        Entity<DataValidationTaskInfo> json = Entity.json(dataValidationTaskInfo);

        Response response= target("/validationtasks").request().header(HEADER_NAME, INSIGHT_KEY).post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void updateTaskWithMetrologyPurpose() {
        mockDataValidationTask(TASK_ID, QualityCodeSystem.MDM);
        DataValidationTaskInfo info = new DataValidationTaskInfo();
        info.id = TASK_ID;
        info.version = OK_VERSION;
        info.deviceGroup = null;
        info.metrologyPurpose = new IdWithDisplayValueInfo<>(1L, "Billing");
        Entity<DataValidationTaskInfo> json = Entity.json(info);

        Response response = target("/validationtasks/" + TASK_ID).request().header(HEADER_NAME, INSIGHT_KEY).put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void updateTaskForDeviceGroup() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();
        info.id = TASK_ID;
        info.version = OK_VERSION;
        info.deviceGroup = new IdWithDisplayValueInfo<>(1L, "Device group");

        Entity<DataValidationTaskInfo> json = Entity.json(info);
        Response response = target("/validationtasks/" + TASK_ID).request().header(HEADER_NAME, MULTISENSE_KEY).put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void updateTaskForUsagePointGroup() {
        mockDataValidationTask(TASK_ID, QualityCodeSystem.MDM);
        DataValidationTaskInfo info = new DataValidationTaskInfo();
        info.id = TASK_ID;
        info.version = OK_VERSION;
        info.deviceGroup = null;
        info.metrologyContract = new IdWithDisplayValueInfo<>(1L, "Billing");
        info.metrologyConfiguration = new IdWithDisplayValueInfo<>(1L, "Metrology configuration");

        Entity<DataValidationTaskInfo> json = Entity.json(info);
        Response response = target("/validationtasks/" + TASK_ID).request().header(HEADER_NAME, INSIGHT_KEY).put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void updateTaskBadVersion() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();
        info.id = TASK_ID;
        info.deviceGroup = new IdWithDisplayValueInfo<>(1L, "Device group");
        info.version = BAD_VERSION;

        Entity<DataValidationTaskInfo> json = Entity.json(info);
        Response response = target("/validationtasks/" + TASK_ID).request().header(HEADER_NAME, MULTISENSE_KEY).put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void updateTaskBadMetrologyContractVersion() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();
        info.id = TASK_ID;
        info.deviceGroup = null;
        info.metrologyContract = new IdWithDisplayValueInfo<>(1L, "Billing");
        info.metrologyConfiguration = new IdWithDisplayValueInfo<>(1L, "Metrology configuration");
        info.version = BAD_VERSION;

        Entity<DataValidationTaskInfo> json = Entity.json(info);
        Response response = target("/validationtasks/" + TASK_ID).request().header(HEADER_NAME, MULTISENSE_KEY).put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void deleteTaskTestBadVersion() {
        DataValidationTaskInfo info = new DataValidationTaskInfo();
        info.id = TASK_ID;
        info.deviceGroup = new IdWithDisplayValueInfo<>(1L, "Device group");
        info.version = BAD_VERSION;

        Entity<DataValidationTaskInfo> json = Entity.json(info);
        Response response = target("/validationtasks/" + TASK_ID).request().build(HttpMethod.DELETE, json).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testHistorySearchWithInvertedStaredRange() throws Exception {
        Response response = target("/validationtasks/1/history")
                .queryParam("filter", ExtjsFilter.filter().property("startedOnFrom", 1441490400000L).property("startedOnTo", 1441058400000L).create())
                .queryParam("start", "0")
                .queryParam("limit", "10")
                .request()
                .header(HEADER_NAME, MULTISENSE_KEY)
                .get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("startedOnFrom");
        assertThat(jsonModel.<String>get("$.errors[0].msg")).isEqualTo("Invalid range: from-date should be before to-date");
    }

    @Test
    public void testHistorySearchWithInvertedEndedRange() throws Exception {
        Response response = target("/validationtasks/1/history")
                .queryParam("filter", ExtjsFilter.filter().property("finishedOnFrom", 1441490400000L).property("finishedOnTo", 1441058400000L).create())
                .queryParam("start", "0")
                .queryParam("limit", "10")
                .request()
                .header(HEADER_NAME, MULTISENSE_KEY)
                .get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("finishedOnFrom");
        assertThat(jsonModel.<String>get("$.errors[0].msg")).isEqualTo("Invalid range: from-date should be before to-date");
    }

    @Test
    public void testHistorySearchWithoutFrom() throws Exception {
        Response response = target("/validationtasks/1/history")
                .queryParam("filter", ExtjsFilter.filter().property("startedOnTo", 1441058400000L).create())
                .queryParam("start", "0")
                .queryParam("limit", "10")
                .request()
                .header(HEADER_NAME, MULTISENSE_KEY)
                .get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void getDataValidationTaskWithLastOccurrence() {
        Instant startedOn = Instant.now();
        Instant finishedOn = startedOn.plusMillis(1000);
        Instant triggerTime = startedOn.minusMillis(1000);

        DataValidationTask dataValidationTask = mockDataValidationTask(TASK_ID, QualityCodeSystem.MDM);
        DataValidationOccurrence lastValidationOccurrence = mock(DataValidationOccurrence.class);
        when(lastValidationOccurrence.wasScheduled()).thenReturn(true);
        when(lastValidationOccurrence.getStartDate()).thenReturn(Optional.of(startedOn));
        when(lastValidationOccurrence.getEndDate()).thenReturn(Optional.of(finishedOn));
        when(lastValidationOccurrence.getTriggerTime()).thenReturn(triggerTime);
        when(lastValidationOccurrence.getStatus()).thenReturn(DataValidationTaskStatus.SUCCESS);
        when(lastValidationOccurrence.getTask()).thenReturn(dataValidationTask);

        when(dataValidationTask.getLastOccurrence()).thenReturn(Optional.of(lastValidationOccurrence));
        TemporalExpression scheduleExpression = new TemporalExpression(TimeDuration.days(14));
        when(dataValidationTask.getScheduleExpression()).thenReturn(scheduleExpression);
        History<DataValidationTask> history = mock(History.class);
        when(history.getVersionAt(any())).thenReturn(Optional.empty());
        doReturn(history).when(dataValidationTask).getHistory();
        when(dataValidationTask.getScheduleExpression(any())).thenReturn(Optional.empty());
        when(this.timeService.toLocalizedString(any(TemporalExpression.class))).thenReturn("Every 14 days");

        // Business method
        String response = target("/validationtasks/" + TASK_ID).request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        //Asserts
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(TASK_ID);
        assertThat(jsonModel.<String>get("$.recurrence")).isEqualTo("Every 14 days");
        assertThat(jsonModel.<Boolean>get("$.lastValidationOccurence.wasScheduled")).isTrue();
        assertThat(jsonModel.<Number>get("$.lastValidationOccurence.startedOn")).isEqualTo(startedOn.toEpochMilli());
        assertThat(jsonModel.<Number>get("$.lastValidationOccurence.finishedOn")).isEqualTo(finishedOn.toEpochMilli());
        assertThat(jsonModel.<Number>get("$.lastValidationOccurence.lastRun")).isEqualTo(triggerTime.toEpochMilli());
        assertThat(jsonModel.<Number>get("$.lastValidationOccurence.duration")).isEqualTo(1000);
        assertThat(jsonModel.<Number>get("$.schedule.count")).isEqualTo(14);
        assertThat(jsonModel.<String>get("$.schedule.timeUnit")).isEqualTo("days");
    }

    private void mockDataValidationTasks(DataValidationTask... validationTasks) {
        when(validationService.findValidationTasks()).thenReturn(Arrays.asList(validationTasks));
    }

    private DataValidationTask mockDataValidationTask(int id, QualityCodeSystem qualityCodeSystem) {
        Long lid = Integer.valueOf(id).longValue();
        DataValidationTask validationTask = mock(DataValidationTask.class);
        when(validationTask.getId()).thenReturn(lid);
        when(validationTask.getScheduleExpression()).thenReturn(Never.NEVER);
        when(validationTask.getName()).thenReturn("Name");
        when(validationTask.getLastRun()).thenReturn(Optional.<Instant>empty());
        when(validationTask.getMetrologyPurpose()).thenReturn(Optional.of(metrologyPurpose));
        if(qualityCodeSystem.equals(QualityCodeSystem.MDC)) {
            when(validationTask.getEndDeviceGroup()).thenReturn(Optional.of(endDeviceGroup));
            when(validationTask.getUsagePointGroup()).thenReturn(Optional.empty());
        } else {
            when(validationTask.getEndDeviceGroup()).thenReturn(Optional.empty());
            when(validationTask.getUsagePointGroup()).thenReturn(Optional.of(usagePointGroup));
        }
        when(validationTask.getQualityCodeSystem()).thenReturn(qualityCodeSystem);

        DataValidationOccurrenceFinder finder = mock(DataValidationOccurrenceFinder.class);
        when(finder.setLimit(anyInt())).thenReturn(finder);
        when(finder.setStart(anyInt())).thenReturn(finder);
        when(validationTask.getOccurrencesFinder()).thenReturn(finder);
        when(validationTask.getLastOccurrence()).thenReturn(Optional.<DataValidationOccurrence>empty());
        when(validationTask.getVersion()).thenReturn(OK_VERSION);

        doReturn(Optional.of(validationTask)).when(validationService).findValidationTask(lid);
        doReturn(Optional.of(validationTask)).when(validationService).findAndLockValidationTaskByIdAndVersion(lid, OK_VERSION);
        doReturn(Optional.empty()).when(validationService).findAndLockValidationTaskByIdAndVersion(lid, BAD_VERSION);

        return validationTask;
    }
}
