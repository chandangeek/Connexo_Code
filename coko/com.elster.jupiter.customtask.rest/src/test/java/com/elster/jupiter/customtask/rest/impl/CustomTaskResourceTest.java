/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.rest.impl;

import com.elster.jupiter.customtask.CustomTask;
import com.elster.jupiter.customtask.CustomTaskAction;
import com.elster.jupiter.customtask.CustomTaskBuilder;
import com.elster.jupiter.customtask.CustomTaskFactory;
import com.elster.jupiter.customtask.CustomTaskOccurrence;
import com.elster.jupiter.customtask.CustomTaskOccurrenceFinder;
import com.elster.jupiter.customtask.CustomTaskStatus;
import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Never;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CustomTaskResourceTest extends BaseCustomTaskRestTest {

    private static final Long TASK_ID = 750L;
    private static final String INSIGHT_KEY = "INS";
    private static final String INSIGHT_NAME = "Insight";
    private static final String MULTISENSE_KEY = "MDC";
    private static final String MULTISENSE_NAME = "MultiSense";
    private static final String HEADER_NAME = "X-CONNEXO-APPLICATION-NAME";
    private static final String CTF_NAME_FOR_MDC = "Custom task for MDC";
    private static final String CTF_DISPLAY_NAME_FOR_MDC = "MDCCustomTask";
    private static final String CUSTOM_TASK_FACTORY_NAME_FOR_MDM = "Custom task for MDM";
    private static final String CUSTOM_TASK_FACTORY_NAME_1 = "Custom task 1 for MDC";
    private static final String CUSTOM_TASK_FACTORY_NAME_2 = "Custom task 2 for MDC";
    public static final long OK_VERSION = 23L;
    public static final long BAD_VERSION = 21L;

    CustomTaskBuilder taskBuilder;
/*
    @Mock
    EndDeviceGroup endDeviceGroup;
    @Mock
    UsagePointGroup usagePointGroup;
*/

    @Mock
    User user;
    @Mock
    CustomTask customTask;
    @Mock
    private History<CustomTask> customTaskHistory;
    @Mock
    CustomTaskFactory customTaskFactory;
    @Mock
    private QueryStream queryStream;
    @Mock
    private RecurrentTask recurrentTask;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        when(securityContext.getUserPrincipal()).thenReturn(user);
        customTask = mockCustomTask(TASK_ID, MULTISENSE_NAME);
        taskBuilder = FakeBuilder.initBuilderStub(customTask, CustomTaskBuilder.class);
        when(customTaskService.newBuilder()).thenReturn(taskBuilder);
        doReturn(Optional.of(customTask)).when(customTaskService).findAndLockCustomTask(anyLong(), eq(OK_VERSION));
    }

    @Test
    public void getCustomTaskFactories() {
        getCustomTaskFactoriesByApp(MULTISENSE_KEY);
        getCustomTaskFactoriesByApp(INSIGHT_KEY);
    }

    @Test
    public void getApplicationSpecificTasks() {
        getApplicationSpecificTaskByApp(1L, MULTISENSE_NAME, MULTISENSE_KEY);
        getApplicationSpecificTaskByApp(3L, INSIGHT_NAME, INSIGHT_KEY);
    }

    @Test
    public void createTask() {
        CustomTaskInfo info = new CustomTaskInfo();
        info.properties = Collections.emptyList();
        Entity<CustomTaskInfo> json = Entity.json(info);
        Response response = target("/customtask").request().header(HEADER_NAME, MULTISENSE_KEY).post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void updateTask() {
        CustomTaskInfo info = new CustomTaskInfo();
        info.id = TASK_ID;
        info.version = OK_VERSION;
        info.properties = Collections.emptyList();
        Entity<CustomTaskInfo> json = Entity.json(info);
        Response response = target("/customtask/" + TASK_ID).request().header(HEADER_NAME, MULTISENSE_KEY).put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void deleteTasksTest() {
        CustomTaskInfo info = new CustomTaskInfo();
        info.id = TASK_ID;
        info.version = OK_VERSION;
        info.properties = Collections.emptyList();

        Entity<CustomTaskInfo> json = Entity.json(info);
        Response response = target("/customtask/" + TASK_ID).request().build(HttpMethod.DELETE, json).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testHistorySearchWithInvertedStaredRange() throws Exception {
        Response response = target("/customtask/" + TASK_ID + "/history")
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
        Response response = target("/customtask/" + TASK_ID + "/history")
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
        Response response = target("/customtask/" + TASK_ID + "/history")
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

        CustomTask customTask = mockCustomTask(TASK_ID, MULTISENSE_NAME);
        CustomTaskOccurrence customTaskOccurrence = mock(CustomTaskOccurrence.class);
        when(customTaskOccurrence.wasScheduled()).thenReturn(true);
        when(customTaskOccurrence.getStartDate()).thenReturn(Optional.of(startedOn));
        when(customTaskOccurrence.getEndDate()).thenReturn(Optional.of(finishedOn));
        when(customTaskOccurrence.getTriggerTime()).thenReturn(triggerTime);
        when(customTaskOccurrence.getStatus()).thenReturn(CustomTaskStatus.SUCCESS);
        when(customTaskOccurrence.getTask()).thenReturn(customTask);

        when(customTaskOccurrence.getRecurrentTask()).thenReturn(recurrentTask);

        doReturn(Optional.of(customTaskOccurrence)).when(customTask).getLastOccurrence();
        TemporalExpression scheduleExpression = new TemporalExpression(TimeDuration.days(14));
        when(customTask.getScheduleExpression()).thenReturn(scheduleExpression);
        History<CustomTask> history = mock(History.class);
        when(history.getVersionAt(any())).thenReturn(Optional.empty());
        doReturn(history).when(customTask).getHistory();
        when(customTask.getScheduleExpression(any())).thenReturn(Optional.empty());
        when(this.timeService.toLocalizedString(any(TemporalExpression.class))).thenReturn("Every 14 days");

        // Business method
        String response = target("/customtask/" + TASK_ID).request().header(HEADER_NAME, MULTISENSE_KEY).get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        //Asserts
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(TASK_ID.intValue());
        assertThat(jsonModel.<String>get("$.recurrence")).isEqualTo("Every 14 days");
        assertThat(jsonModel.<Boolean>get("$.lastOccurrence.wasScheduled")).isTrue();
        assertThat(jsonModel.<Number>get("$.lastOccurrence.startedOn")).isEqualTo(startedOn.toEpochMilli());
        assertThat(jsonModel.<Number>get("$.lastOccurrence.finishedOn")).isEqualTo(finishedOn.toEpochMilli());
        assertThat(jsonModel.<Number>get("$.lastOccurrence.lastRun")).isEqualTo(triggerTime.toEpochMilli());
        assertThat(jsonModel.<Number>get("$.lastOccurrence.duration")).isEqualTo(1000);
        assertThat(jsonModel.<Number>get("$.schedule.count")).isEqualTo(14);
        assertThat(jsonModel.<String>get("$.schedule.timeUnit")).isEqualTo("days");
    }

    private void getCustomTaskFactoriesByApp(String application) {
        mockCustomTaskFactories(application, mockCustomTaskFactory(CTF_NAME_FOR_MDC, CTF_DISPLAY_NAME_FOR_MDC, application, user));
        String jsonFromMultisense = target("/customtask/types").request().header(HEADER_NAME, application).get(String.class);

        JsonModel jsonModelFromMultisense = JsonModel.create(jsonFromMultisense);
        assertThat(jsonModelFromMultisense.<String>get("[0].name")).isEqualTo(CTF_NAME_FOR_MDC);
        assertThat(jsonModelFromMultisense.<String>get("[0].displayName")).isEqualTo(CTF_DISPLAY_NAME_FOR_MDC);
        assertThat(jsonModelFromMultisense.<List>get("[0].actions")).hasSize(3)
                .contains(CustomTaskAction.ADMINISTRATE.toString())
                .contains(CustomTaskAction.RUN.toString())
                .contains(CustomTaskAction.EDIT.toString());
    }

    private void getApplicationSpecificTaskByApp(Long taskId, String appName, String appKey) {
        mockCustomTask(taskId, appName);

        Response response = target("/customtask/" + taskId.toString()).request().header(HEADER_NAME, appKey).get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        CustomTaskInfo info = response.readEntity(CustomTaskInfo.class);
        assertThat(info.id).isEqualTo(taskId);
        assertThat(info.name).isEqualTo("Name");
    }

    private void mockCustomTaskFactories(String codeSystem, CustomTaskFactory... customTaskFactories) {
        when(customTaskService.getAvailableCustomTasks(codeSystem)).thenReturn(Arrays.asList(customTaskFactories));
    }

    private CustomTaskFactory mockCustomTaskFactory(String name, String displayName, String codeSystem, User user) {
        CustomTaskFactory customTaskFactory = mock(CustomTaskFactory.class);
        when(customTaskFactory.targetApplications()).thenReturn(Arrays.asList(codeSystem));
        when(customTaskFactory.getName()).thenReturn(name);
        when(customTaskFactory.getDisplayName()).thenReturn(displayName);
        when(customTaskFactory.getActionsForUser(user, codeSystem)).thenReturn(Arrays.asList(CustomTaskAction.ADMINISTRATE, CustomTaskAction.RUN, CustomTaskAction.EDIT));

        return customTaskFactory;
    }

    private CustomTask mockCustomTask(Long id, String codeSystem) {
        CustomTask customTask = mock(CustomTask.class);
        when(customTask.getId()).thenReturn(id);
        when(customTask.getScheduleExpression()).thenReturn(Never.NEVER);
        when(customTask.getName()).thenReturn("Name");
        when(customTask.getLastRun()).thenReturn(Optional.<Instant>empty());
        when(customTask.getApplication()).thenReturn(codeSystem);

        CustomTaskOccurrenceFinder finder = mock(CustomTaskOccurrenceFinder.class);
        when(finder.setLimit(anyInt())).thenReturn(finder);
        when(finder.setStart(anyInt())).thenReturn(finder);
        when(customTask.getOccurrencesFinder()).thenReturn(finder);
        when(customTask.getLastOccurrence()).thenReturn(Optional.empty());
        when(customTask.getVersion()).thenReturn(OK_VERSION);
        when(customTask.canBeDeleted()).thenReturn(true);
        when(customTask.getHistory()).thenReturn(customTaskHistory);
        when(customTaskHistory.getVersionAt(any(Instant.class))).thenReturn(Optional.of(customTask));
        doReturn(Optional.of(customTask)).when(customTaskService).findCustomTask(id);
        when(finder.stream()).thenReturn(queryStream);
        when(queryStream.map(any())).thenReturn(queryStream);
        when(queryStream.collect(any())).thenReturn(Collections.singletonList(1));
        return customTask;
    }
}
