package com.energyict.mdc.scheduling.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.SchedulingStatus;
import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.tasks.ComTask;
import com.jayway.jsonpath.JsonModel;
import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.joda.time.DateTimeConstants;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SchedulingResourceTest extends SchedulingApplicationJerseyTest {

    @Test
    public void testGetEmptyScheduleList() throws Exception {
        List<ComSchedule> comSchedules = new ArrayList<>();
        when(schedulingService.findAllSchedules()).thenReturn(comSchedules);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        Map<String, Object> map = target("/schedules/").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List<?>) map.get("schedules")).isEmpty();
    }

    @Test
    public void testGetSingleScheduleList() throws Exception {
        ComSchedule mockedSchedule = mock(ComSchedule.class);
        when(mockedSchedule.getId()).thenReturn(1L);
        when(mockedSchedule.getStartDate()).thenReturn(Instant.now());
        when(mockedSchedule.getName()).thenReturn("name");
        when(mockedSchedule.getmRID()).thenReturn("mRID");
        when(mockedSchedule.getPlannedDate()).thenReturn(Optional.of(Instant.now()));
        when(mockedSchedule.getSchedulingStatus()).thenReturn(SchedulingStatus.ACTIVE);
        when(mockedSchedule.getNextTimestamp(any(Calendar.class))).thenReturn(new Date());
        when(mockedSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration("10 minutes")));
        ComTask comTask1 = mockComTask(11L, "Com task 1");
        ComTask comTask2 = mockComTask(12L, "Com task 2");
        when(mockedSchedule.getComTasks()).thenReturn(Arrays.asList(comTask1, comTask2));
        when(schedulingService.findAllSchedules()).thenReturn(Arrays.asList(mockedSchedule));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        Map<String, Object> map = target("/schedules/").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(1);
        List<Map<String, Object>> schedules = (List<Map<String, Object>>) map.get("schedules");
        assertThat(schedules).hasSize(1);
        Map<String, Object> actual = schedules.get(0);
        assertThat(actual).hasSize(8)
                .containsKey("id")
                .containsKey("name")
                .containsKey("temporalExpression")
                .containsKey("plannedDate")
                .containsKey("isInUse")
                .containsKey("comTaskUsages")
                .containsKey("mRID")
                .containsKey("startDate");
    }

    @Test
    public void testGetScheduleListSecondPage() throws Exception {
        List<ComSchedule> schedules = new ArrayList<>();
        IntStream.range(1, 100).forEach(i -> schedules.add(mockComSchedule(i, MessageFormat.format("cs {0}",i))));
        when(schedulingService.findAllSchedules()).thenReturn(schedules);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        Response response = target("/schedules/").queryParam("start",10).queryParam("limit",10).request().get();
        JsonModel jsonModel = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(21);
        List<String> list = jsonModel.<List<String>>get("$.schedules[*].name");
        assertThat(list).hasSize(10).containsOnly("cs 11", "cs 12", "cs 13", "cs 14", "cs 15", "cs 16", "cs 17", "cs 18", "cs 19", "cs 20");

    }

    private ComSchedule mockComSchedule(long id, String name) {
        ComSchedule mockedSchedule = mock(ComSchedule.class);
        when(mockedSchedule.getId()).thenReturn(id);
        when(mockedSchedule.getName()).thenReturn(name);
        when(mockedSchedule.getPlannedDate()).thenReturn(Optional.of(Instant.now()));
        when(mockedSchedule.getSchedulingStatus()).thenReturn(SchedulingStatus.ACTIVE);
        when(mockedSchedule.getNextTimestamp(any(Calendar.class))).thenReturn(new Date());
        when(mockedSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration("10 minutes")));

        return mockedSchedule;
    }

    @Test
    public void testRemoveComTaskFromSchedule() throws Exception {
        final long COM_TASK_1 = 11L;
        final long COM_TASK_2 = 12L;

        ComSchedule mockedSchedule = mock(ComSchedule.class);
        when(mockedSchedule.getId()).thenReturn(1L);
        when(mockedSchedule.getName()).thenReturn("name");
        when(mockedSchedule.getPlannedDate()).thenReturn(Optional.<Instant>empty());
        when(mockedSchedule.getSchedulingStatus()).thenReturn(SchedulingStatus.ACTIVE);
        when(mockedSchedule.getNextTimestamp(any(Calendar.class))).thenReturn(new Date());
        when(mockedSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration("10 minutes")));
        ComTask comTask1 = mockComTask(COM_TASK_1, "Com task 1");
        ComTask comTask2 = mockComTask(COM_TASK_2, "Com task 2");
        when(mockedSchedule.getComTasks()).thenReturn(Arrays.asList(comTask1, comTask2));
        when(schedulingService.findSchedule(1L)).thenReturn(Optional.of(mockedSchedule));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        ComScheduleInfo comScheduleInfo = new ComScheduleInfo();
        comScheduleInfo.plannedDate = Instant.now();
        comScheduleInfo.name = "new name";
        ComTaskInfo comTaskInfo = new ComTaskInfo();
        comTaskInfo.id = COM_TASK_1;
        comScheduleInfo.comTaskUsages = Arrays.asList(comTaskInfo);
        Entity<ComScheduleInfo> json = Entity.json(comScheduleInfo);
        Response response = target("/schedules/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(mockedSchedule, times(1)).removeComTask(comTask2);
        verify(mockedSchedule, never()).removeComTask(comTask1);
        verify(mockedSchedule, never()).addComTask(any(ComTask.class));
    }

    @Test
    public void testUpdateNothing() throws Exception {
        ComSchedule mockedSchedule = mock(ComSchedule.class);
        when(mockedSchedule.getId()).thenReturn(1L);
        when(mockedSchedule.getName()).thenReturn("name");
        when(mockedSchedule.getPlannedDate()).thenReturn(Optional.<Instant>empty());
        when(mockedSchedule.getSchedulingStatus()).thenReturn(SchedulingStatus.ACTIVE);
        when(mockedSchedule.getNextTimestamp(any(Calendar.class))).thenReturn(new Date());
        when(mockedSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration("10 minutes")));
        when(schedulingService.findSchedule(1L)).thenReturn(Optional.of(mockedSchedule));
        ComScheduleInfo comScheduleInfo = new ComScheduleInfo();
        Entity<ComScheduleInfo> json = Entity.json(comScheduleInfo);
        Response response = target("/schedules/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testAddComTaskToSchedule() throws Exception {
        final long COM_TASK_1 = 11L;
        final long COM_TASK_2 = 12L;
        final long COM_TASK_3 = 13L;

        ComSchedule mockedSchedule = mock(ComSchedule.class);
        when(mockedSchedule.getId()).thenReturn(1L);
        when(mockedSchedule.getName()).thenReturn("name");
        when(mockedSchedule.getPlannedDate()).thenReturn(Optional.<Instant>empty());
        when(mockedSchedule.getSchedulingStatus()).thenReturn(SchedulingStatus.ACTIVE);
        when(mockedSchedule.getNextTimestamp(any(Calendar.class))).thenReturn(new Date());
        when(mockedSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration("10 minutes")));
        ComTask comTask1 = mockComTask(COM_TASK_1, "Com task 1");
        ComTask comTask2 = mockComTask(COM_TASK_2, "Com task 2");
        ComTask comTask3 = mockComTask(COM_TASK_3, "Com task 3");
        when(mockedSchedule.getComTasks()).thenReturn(Arrays.asList(comTask1, comTask2));
        when(schedulingService.findSchedule(1L)).thenReturn(Optional.of(mockedSchedule));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        ComScheduleInfo comScheduleInfo = new ComScheduleInfo();
        comScheduleInfo.plannedDate = Instant.now();
        comScheduleInfo.name = "new name";
        ComTaskInfo comTaskInfo1 = new ComTaskInfo();
        comTaskInfo1.id = COM_TASK_1;
        ComTaskInfo comTaskInfo2 = new ComTaskInfo();
        comTaskInfo2.id = COM_TASK_2;
        ComTaskInfo comTaskInfo3 = new ComTaskInfo();
        comTaskInfo3.id = COM_TASK_3;
        comScheduleInfo.comTaskUsages = Arrays.asList(comTaskInfo1, comTaskInfo2, comTaskInfo3);
        Entity<ComScheduleInfo> json = Entity.json(comScheduleInfo);
        Response response = target("/schedules/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(mockedSchedule, times(1)).addComTask(comTask3);
        verify(mockedSchedule, never()).removeComTask(any(ComTask.class));
    }

    @Test
    public void testAddRemoveComTask() throws Exception {
        final long COM_TASK_1 = 1011L;
        final long COM_TASK_2 = 1012L;
        final long COM_TASK_3 = 1013L;
        final long COM_TASK_4 = 1014L;

        ComSchedule mockedSchedule = mock(ComSchedule.class);
        when(mockedSchedule.getId()).thenReturn(1L);
        when(mockedSchedule.getName()).thenReturn("name");
        when(mockedSchedule.getPlannedDate()).thenReturn(Optional.<Instant>empty());
        when(mockedSchedule.getSchedulingStatus()).thenReturn(SchedulingStatus.ACTIVE);
        when(mockedSchedule.getNextTimestamp(any(Calendar.class))).thenReturn(new Date());
        when(mockedSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration("10 minutes")));
        ComTask comTask1 = mockComTask(COM_TASK_1, "Com task 1");
        ComTask comTask2 = mockComTask(COM_TASK_2, "Com task 2");
        ComTask comTask3 = mockComTask(COM_TASK_3, "Com task 3");
        ComTask comTask4 = mockComTask(COM_TASK_4, "Com task 4");
        when(mockedSchedule.getComTasks()).thenReturn(Arrays.asList(comTask1, comTask2, comTask3));
        when(schedulingService.findSchedule(1L)).thenReturn(Optional.of(mockedSchedule));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        ComScheduleInfo comScheduleInfo = new ComScheduleInfo();
        comScheduleInfo.plannedDate = Instant.now();
        comScheduleInfo.name = "new name";
        ComTaskInfo comTaskInfo1 = new ComTaskInfo();
        comTaskInfo1.id = COM_TASK_1;
        ComTaskInfo comTaskInfo2 = new ComTaskInfo();
        comTaskInfo2.id = COM_TASK_2;
        ComTaskInfo comTaskInfo4 = new ComTaskInfo();
        comTaskInfo4.id = COM_TASK_4;
        comScheduleInfo.comTaskUsages = Arrays.asList(comTaskInfo1, comTaskInfo2, comTaskInfo4); // so delete 3, add 4
        Entity<ComScheduleInfo> json = Entity.json(comScheduleInfo);
        Response response = target("/schedules/1").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(mockedSchedule, times(1)).addComTask(comTask4);
        verify(mockedSchedule, times(1)).removeComTask(comTask3);
    }

    @Test
    public void testGetComTasksOfScheduleWithoutQueryParam() throws Exception {
        ComSchedule mockedSchedule = mock(ComSchedule.class);
        when(mockedSchedule.getId()).thenReturn(1L);
        when(mockedSchedule.getName()).thenReturn("name");
        when(mockedSchedule.getSchedulingStatus()).thenReturn(SchedulingStatus.ACTIVE);
        when(mockedSchedule.getNextTimestamp(any(Calendar.class))).thenReturn(new Date());
        when(mockedSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration("10 minutes")));
        ComTask comTask1 = mockComTask(11L, "Com task 1");
        ComTask comTask2 = mockComTask(12L, "Com task 2");
        when(mockedSchedule.getComTasks()).thenReturn(Arrays.asList(comTask1, comTask2));
        when(schedulingService.findSchedule(1L)).thenReturn(Optional.of(mockedSchedule));

        List list = target("/schedules/1/comTasks").request().get(List.class);
        assertThat(list).hasSize(2);
    }

    @Test
    public void testGetAllComTasksOfScheduleWithFalseQueryParameter() throws Exception {
        long COMTASK_3 = 13L;
        ComSchedule mockedSchedule = mock(ComSchedule.class);
        when(mockedSchedule.getId()).thenReturn(1L);
        when(mockedSchedule.getName()).thenReturn("name");
        when(mockedSchedule.getSchedulingStatus()).thenReturn(SchedulingStatus.ACTIVE);
        when(mockedSchedule.getNextTimestamp(any(Calendar.class))).thenReturn(new Date());
        when(mockedSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration("10 minutes")));
        ComTask comTask1 = mockComTask(11L, "Com task 1");
        ComTask comTask2 = mockComTask(12L, "Com task 2");
        ComTask comTask3 = mockComTask(COMTASK_3, "Com task 3");
        when(mockedSchedule.getComTasks()).thenReturn(Arrays.asList(comTask1, comTask2));
        when(schedulingService.findSchedule(1L)).thenReturn(Optional.of(mockedSchedule));
        when(deviceConfigurationService.findAvailableComTasks(mockedSchedule)).thenReturn(Arrays.asList(comTask3));

        List<Map<String, Object>> list = target("/schedules/1/comTasks").queryParam("filter", ExtjsFilter.filter().property("available", "false").create()).request().get(List.class);
        assertThat(list).hasSize(2);
    }

    @Test
    public void testGetAvailableComTasksOfSchedule() throws Exception {
        long COMTASK_3 = 13L;
        ComSchedule mockedSchedule = mock(ComSchedule.class);
        when(mockedSchedule.getId()).thenReturn(1L);
        when(mockedSchedule.getName()).thenReturn("name");
        when(mockedSchedule.getSchedulingStatus()).thenReturn(SchedulingStatus.ACTIVE);
        when(mockedSchedule.getNextTimestamp(any(Calendar.class))).thenReturn(new Date());
        when(mockedSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration("10 minutes")));
        ComTask comTask1 = mockComTask(11L, "Com task 1");
        ComTask comTask2 = mockComTask(12L, "Com task 2");
        ComTask comTask3 = mockComTask(COMTASK_3, "Com task 3");
        when(mockedSchedule.getComTasks()).thenReturn(Arrays.asList(comTask1, comTask2));
        when(schedulingService.findSchedule(1L)).thenReturn(Optional.of(mockedSchedule));
        when(deviceConfigurationService.findAvailableComTasks(mockedSchedule)).thenReturn(Arrays.asList(comTask3));

        List<Map<String, Object>> list = target("/schedules/1/comTasks").queryParam("filter", ExtjsFilter.filter().property("available", "tRue").create()).request().get(List.class);
        assertThat(list).hasSize(2); // param values are case sensitive !!! tRue != true
        list = target("/schedules/1/comTasks").queryParam("filter", ExtjsFilter.filter().property("available", "true").create()).request().get(List.class);
        assertThat(list).hasSize(1);

        assertThat(list.get(0).get("id")).isEqualTo((int) COMTASK_3);
    }

    @Test
    public void testGetAvailableComTasksOfScheduleIfComTask12AlreadyAssigned() throws Exception {
        long COMTASK_3 = 13L;
        ComSchedule mockedSchedule = mock(ComSchedule.class);
        when(mockedSchedule.getId()).thenReturn(1L);
        when(mockedSchedule.getName()).thenReturn("name");
        when(mockedSchedule.getSchedulingStatus()).thenReturn(SchedulingStatus.ACTIVE);
        when(mockedSchedule.getNextTimestamp(any(Calendar.class))).thenReturn(new Date());
        when(mockedSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration("10 minutes")));
        ComTask comTask1 = mockComTask(11L, "Com task 1");
        ComTask comTask2 = mockComTask(12L, "Com task 2");
        ComTask comTask3 = mockComTask(COMTASK_3, "Com task 3");
        when(mockedSchedule.getComTasks()).thenReturn(Arrays.asList(comTask1, comTask2));
        when(schedulingService.findSchedule(1L)).thenReturn(Optional.of(mockedSchedule));
        when(deviceConfigurationService.findAvailableComTasks(mockedSchedule)).thenReturn(Arrays.asList(comTask3, comTask2));

        List<Map<String, Object>> list = target("/schedules/1/comTasks").queryParam("filter", ExtjsFilter.filter().property("available", "true").create()).request().get(List.class);
        assertThat(list).hasSize(1);
        assertThat(list.get(0).get("id")).isEqualTo((int) COMTASK_3);
    }

    @Test
    public void testPreviewMinutelyWithoutOffset() throws Exception {
        PreviewInfo previewInfo = new PreviewInfo();
        previewInfo.temporalExpression = new TemporalExpressionInfo();
        previewInfo.temporalExpression.every = new TimeDurationInfo(new TimeDuration(10, TimeDuration.TimeUnit.MINUTES));
        previewInfo.startDate = new Date(1400146123000L); //  Thu, 15 May 2014 09:28:43 GMT

        Entity<PreviewInfo> entity = Entity.json(previewInfo);
        Response response = target("/schedules/preview").request().put(entity);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        PreviewInfo responseEntity = response.readEntity(PreviewInfo.class);
        assertThat(responseEntity.nextOccurrences).hasSize(5);
        assertThat(responseEntity.nextOccurrences.get(0)).isEqualTo(new Date(1400146200000L));
        assertThat(responseEntity.nextOccurrences.get(1)).isEqualTo(new Date(1400146200000L + 10 * DateTimeConstants.MILLIS_PER_MINUTE));
        assertThat(responseEntity.nextOccurrences.get(2)).isEqualTo(new Date(1400146200000L + 20 * DateTimeConstants.MILLIS_PER_MINUTE));
        assertThat(responseEntity.nextOccurrences.get(3)).isEqualTo(new Date(1400146200000L + 30 * DateTimeConstants.MILLIS_PER_MINUTE));
        assertThat(responseEntity.nextOccurrences.get(4)).isEqualTo(new Date(1400146200000L + 40 * DateTimeConstants.MILLIS_PER_MINUTE));
    }

    @Test
    public void testPreviewMinutelyWithOffset() throws Exception {
        PreviewInfo previewInfo = new PreviewInfo();
        previewInfo.temporalExpression = new TemporalExpressionInfo();
        previewInfo.temporalExpression.every = new TimeDurationInfo(new TimeDuration(10, TimeDuration.TimeUnit.MINUTES));
        previewInfo.temporalExpression.offset = new TimeDurationInfo(new TimeDuration(5, TimeDuration.TimeUnit.SECONDS));
        previewInfo.startDate = new Date(1400146123000L); //  Thu, 15 May 2014 09:28:43 GMT

        Entity<PreviewInfo> entity = Entity.json(previewInfo);
        Response response = target("/schedules/preview").request().put(entity);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        PreviewInfo responseEntity = response.readEntity(PreviewInfo.class);
        assertThat(responseEntity.nextOccurrences).hasSize(5);
        assertThat(responseEntity.nextOccurrences.get(0)).isEqualTo(new Date(1400146205000L));
        assertThat(responseEntity.nextOccurrences.get(1)).isEqualTo(new Date(1400146205000L + 10 * DateTimeConstants.MILLIS_PER_MINUTE));
        assertThat(responseEntity.nextOccurrences.get(2)).isEqualTo(new Date(1400146205000L + 20 * DateTimeConstants.MILLIS_PER_MINUTE));
        assertThat(responseEntity.nextOccurrences.get(3)).isEqualTo(new Date(1400146205000L + 30 * DateTimeConstants.MILLIS_PER_MINUTE));
        assertThat(responseEntity.nextOccurrences.get(4)).isEqualTo(new Date(1400146205000L + 40 * DateTimeConstants.MILLIS_PER_MINUTE));
    }


    private ComTask mockComTask(long id, String name) {
        ComTask comTask1 = mock(ComTask.class);
        when(comTask1.getId()).thenReturn(id);
        when(comTask1.getName()).thenReturn(name);
        when(taskService.findComTask(id)).thenReturn(Optional.of(comTask1));
        return comTask1;
    }

}
