package com.energyict.mdc.scheduling.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConstraintViolationExceptionMapper;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;
import com.elster.jupiter.rest.util.LocalizedFieldValidationExceptionMapper;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.SchedulingStatus;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.joda.time.DateTimeConstants;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SchedulingResourceTest extends JerseyTest {

    private static NlsService nlsService;
    private static Thesaurus thesaurus;
    private static Clock clock;
    private static SchedulingService schedulingService;
    private static DeviceDataService deviceDataService;
    private static DeviceConfigurationService deviceConfigurationService;
    private static TaskService taskService;

    @BeforeClass
    public static void setUpClass() throws Exception {
        schedulingService = mock(SchedulingService.class);
        deviceDataService = mock(DeviceDataService.class);
        deviceConfigurationService = mock(DeviceConfigurationService.class);
        taskService = mock(TaskService.class);
        clock = mock(Clock.class);
        nlsService = mock(NlsService.class);
        thesaurus = mock(Thesaurus.class);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        reset();
    }

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig resourceConfig = new ResourceConfig(
                SchedulingResource.class,
                ConstraintViolationExceptionMapper.class,
                LocalizedFieldValidationExceptionMapper.class,
                LocalizedExceptionMapper.class);
        resourceConfig.register(JacksonFeature.class); // Server side JSON processing
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(schedulingService).to(SchedulingService.class);
                bind(deviceDataService).to(DeviceDataService.class);
                bind(deviceConfigurationService).to(DeviceConfigurationService.class);
                bind(taskService).to(TaskService.class);
                bind(nlsService).to(NlsService.class);
                bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
                bind(thesaurus).to(Thesaurus.class);
                bind(clock).to(Clock.class);
            }
        });
        return resourceConfig;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(JacksonFeature.class); // client side JSON processing

        super.configureClient(config);
    }

    @Test
    public void testGetEmptyScheduleList() throws Exception {
        List<ComSchedule> comSchedules = new ArrayList<>();
        ListPager<ComSchedule> comSchedulePage = ListPager.of(comSchedules);
        when(schedulingService.findAllSchedules(any(Calendar.class))).thenReturn(comSchedulePage);
        when(clock.getTimeZone()).thenReturn(Calendar.getInstance().getTimeZone());

        Map<String, Object> map = target("/schedules/").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List<?>)map.get("schedules")).isEmpty();
    }

    @Test
    public void testGetSingleScheduleList() throws Exception {
        ComSchedule mockedSchedule = mock(ComSchedule.class);
        when(mockedSchedule.getId()).thenReturn(1L);
        when(mockedSchedule.getName()).thenReturn("name");
        when(mockedSchedule.getSchedulingStatus()).thenReturn(SchedulingStatus.ACTIVE);
        when(mockedSchedule.getNextTimestamp(any(Calendar.class))).thenReturn(new Date());
        when(mockedSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration("10 minutes")));
        ComTask comTask1 = mockComTask(11L, "Com task 1");
        ComTask comTask2 = mockComTask(12L, "Com task 2");
        when(mockedSchedule.getComTasks()).thenReturn(Arrays.asList(comTask1, comTask2));
        ListPager<ComSchedule> comSchedulePage = ListPager.of(Arrays.asList(mockedSchedule));
        when(schedulingService.findAllSchedules(any(Calendar.class))).thenReturn(comSchedulePage);
        when(clock.getTimeZone()).thenReturn(Calendar.getInstance().getTimeZone());

        Map<String, Object> map = target("/schedules/").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(1);
        List<Map<String, Object>> schedules = (List<Map<String, Object>>) map.get("schedules");
        assertThat(schedules).hasSize(1);
        Map<String, Object> actual = schedules.get(0);
        assertThat(actual).hasSize(9)
                .containsKey("id")
                .containsKey("name")
                .containsKey("temporalExpression")
                .containsKey("plannedDate")
                .containsKey("isInUse")
                .containsKey("schedulingStatus")
                .containsKey("comTaskUsages")
                .containsKey("mRID")
                .containsKey("startDate");
    }

    @Test
    public void testRemoveComTaskFromSchedule() throws Exception {
        final long COM_TASK_1 = 11L;
        final long COM_TASK_2 = 12L;

        ComSchedule mockedSchedule = mock(ComSchedule.class);
        when(mockedSchedule.getId()).thenReturn(1L);
        when(mockedSchedule.getName()).thenReturn("name");
        when(mockedSchedule.getSchedulingStatus()).thenReturn(SchedulingStatus.ACTIVE);
        when(mockedSchedule.getNextTimestamp(any(Calendar.class))).thenReturn(new Date());
        when(mockedSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration("10 minutes")));
        ComTask comTask1 = mockComTask(COM_TASK_1, "Com task 1");
        ComTask comTask2 = mockComTask(COM_TASK_2, "Com task 2");
        when(mockedSchedule.getComTasks()).thenReturn(Arrays.asList(comTask1, comTask2));
        when(schedulingService.findSchedule(1L)).thenReturn(mockedSchedule);
        when(clock.getTimeZone()).thenReturn(Calendar.getInstance().getTimeZone());

        ComScheduleInfo comScheduleInfo = new ComScheduleInfo();
        comScheduleInfo.plannedDate=new Date();
        comScheduleInfo.name="new name";
        ComTaskInfo comTaskInfo = new ComTaskInfo();
        comTaskInfo.id= COM_TASK_1;
        comScheduleInfo.comTaskUsages=Arrays.asList(comTaskInfo);
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
        when(mockedSchedule.getSchedulingStatus()).thenReturn(SchedulingStatus.ACTIVE);
        when(mockedSchedule.getNextTimestamp(any(Calendar.class))).thenReturn(new Date());
        when(mockedSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration("10 minutes")));
        when(schedulingService.findSchedule(1L)).thenReturn(mockedSchedule);
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
        when(mockedSchedule.getSchedulingStatus()).thenReturn(SchedulingStatus.ACTIVE);
        when(mockedSchedule.getNextTimestamp(any(Calendar.class))).thenReturn(new Date());
        when(mockedSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration("10 minutes")));
        ComTask comTask1 = mockComTask(COM_TASK_1, "Com task 1");
        ComTask comTask2 = mockComTask(COM_TASK_2, "Com task 2");
        ComTask comTask3 = mockComTask(COM_TASK_3,"Com task 3");
        when(mockedSchedule.getComTasks()).thenReturn(Arrays.asList(comTask1, comTask2));
        when(schedulingService.findSchedule(1L)).thenReturn(mockedSchedule);
        when(clock.getTimeZone()).thenReturn(Calendar.getInstance().getTimeZone());

        ComScheduleInfo comScheduleInfo = new ComScheduleInfo();
        comScheduleInfo.plannedDate=new Date();
        comScheduleInfo.name="new name";
        ComTaskInfo comTaskInfo1 = new ComTaskInfo();
        comTaskInfo1.id= COM_TASK_1;
        ComTaskInfo comTaskInfo2 = new ComTaskInfo();
        comTaskInfo2.id= COM_TASK_2;
        ComTaskInfo comTaskInfo3= new ComTaskInfo();
        comTaskInfo3.id= COM_TASK_3;
        comScheduleInfo.comTaskUsages=Arrays.asList(comTaskInfo1, comTaskInfo2, comTaskInfo3);
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
        when(mockedSchedule.getSchedulingStatus()).thenReturn(SchedulingStatus.ACTIVE);
        when(mockedSchedule.getNextTimestamp(any(Calendar.class))).thenReturn(new Date());
        when(mockedSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration("10 minutes")));
        ComTask comTask1 = mockComTask(COM_TASK_1, "Com task 1");
        ComTask comTask2 = mockComTask(COM_TASK_2,"Com task 2");
        ComTask comTask3 = mockComTask(COM_TASK_3,"Com task 3");
        ComTask comTask4 = mockComTask(COM_TASK_4,"Com task 4");
        when(mockedSchedule.getComTasks()).thenReturn(Arrays.asList(comTask1, comTask2, comTask3));
        when(schedulingService.findSchedule(1L)).thenReturn(mockedSchedule);
        when(clock.getTimeZone()).thenReturn(Calendar.getInstance().getTimeZone());

        ComScheduleInfo comScheduleInfo = new ComScheduleInfo();
        comScheduleInfo.plannedDate=new Date();
        comScheduleInfo.name="new name";
        ComTaskInfo comTaskInfo1 = new ComTaskInfo();
        comTaskInfo1.id= COM_TASK_1;
        ComTaskInfo comTaskInfo2 = new ComTaskInfo();
        comTaskInfo2.id= COM_TASK_2;
        ComTaskInfo comTaskInfo4= new ComTaskInfo();
        comTaskInfo4.id= COM_TASK_4;
        comScheduleInfo.comTaskUsages=Arrays.asList(comTaskInfo1, comTaskInfo2, comTaskInfo4); // so delete 3, add 4
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
        when(schedulingService.findSchedule(1L)).thenReturn(mockedSchedule);

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
        when(schedulingService.findSchedule(1L)).thenReturn(mockedSchedule);
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
        when(schedulingService.findSchedule(1L)).thenReturn(mockedSchedule);
        when(deviceConfigurationService.findAvailableComTasks(mockedSchedule)).thenReturn(Arrays.asList(comTask3));

        List<Map<String, Object>> list = target("/schedules/1/comTasks").queryParam("filter", ExtjsFilter.filter().property("available", "tRue").create()).request().get(List.class);
        assertThat(list).hasSize(1);
        assertThat(list.get(0).get("id")).isEqualTo((int)COMTASK_3);
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
        when(schedulingService.findSchedule(1L)).thenReturn(mockedSchedule);
        when(deviceConfigurationService.findAvailableComTasks(mockedSchedule)).thenReturn(Arrays.asList(comTask3, comTask2));

        List<Map<String, Object>> list = target("/schedules/1/comTasks").queryParam("filter", ExtjsFilter.filter().property("available", "true").create()).request().get(List.class);
        assertThat(list).hasSize(1);
        assertThat(list.get(0).get("id")).isEqualTo((int)COMTASK_3);
    }

    @Test
    public void testPreviewMinutelyWithoutOffset() throws Exception {
        PreviewInfo previewInfo = new PreviewInfo();
        previewInfo.temporalExpression = new TemporalExpressionInfo();
        previewInfo.temporalExpression.every=new TimeDurationInfo(new TimeDuration(10, TimeDuration.MINUTES));
        previewInfo.startDate=new Date(1400146123000L); //  Thu, 15 May 2014 09:28:43 GMT

        Entity<PreviewInfo> entity = Entity.json(previewInfo);
        Response response = target("/schedules/preview").request().put(entity);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        PreviewInfo responseEntity = response.readEntity(PreviewInfo.class);
        assertThat(responseEntity.nextOccurrences).hasSize(5);
        assertThat(responseEntity.nextOccurrences.get(0)).isEqualTo(new Date(1400146200000L));
        assertThat(responseEntity.nextOccurrences.get(1)).isEqualTo(new Date(1400146200000L + 10*DateTimeConstants.MILLIS_PER_MINUTE));
        assertThat(responseEntity.nextOccurrences.get(2)).isEqualTo(new Date(1400146200000L + 20*DateTimeConstants.MILLIS_PER_MINUTE));
        assertThat(responseEntity.nextOccurrences.get(3)).isEqualTo(new Date(1400146200000L + 30*DateTimeConstants.MILLIS_PER_MINUTE));
        assertThat(responseEntity.nextOccurrences.get(4)).isEqualTo(new Date(1400146200000L + 40*DateTimeConstants.MILLIS_PER_MINUTE));
    }

    @Test
    public void testPreviewMinutelyWithOffset() throws Exception {
        PreviewInfo previewInfo = new PreviewInfo();
        previewInfo.temporalExpression = new TemporalExpressionInfo();
        previewInfo.temporalExpression.every=new TimeDurationInfo(new TimeDuration(10, TimeDuration.MINUTES));
        previewInfo.temporalExpression.offset=new TimeDurationInfo(new TimeDuration(5, TimeDuration.SECONDS));
        previewInfo.startDate=new Date(1400146123000L); //  Thu, 15 May 2014 09:28:43 GMT

        Entity<PreviewInfo> entity = Entity.json(previewInfo);
        Response response = target("/schedules/preview").request().put(entity);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        PreviewInfo responseEntity = response.readEntity(PreviewInfo.class);
        assertThat(responseEntity.nextOccurrences).hasSize(5);
        assertThat(responseEntity.nextOccurrences.get(0)).isEqualTo(new Date(1400146205000L));
        assertThat(responseEntity.nextOccurrences.get(1)).isEqualTo(new Date(1400146205000L + 10*DateTimeConstants.MILLIS_PER_MINUTE));
        assertThat(responseEntity.nextOccurrences.get(2)).isEqualTo(new Date(1400146205000L + 20*DateTimeConstants.MILLIS_PER_MINUTE));
        assertThat(responseEntity.nextOccurrences.get(3)).isEqualTo(new Date(1400146205000L + 30*DateTimeConstants.MILLIS_PER_MINUTE));
        assertThat(responseEntity.nextOccurrences.get(4)).isEqualTo(new Date(1400146205000L + 40*DateTimeConstants.MILLIS_PER_MINUTE));
    }

    @Test
    public void testGetAvailableComTasksOfScheduleWithFaultyFilter() throws Exception {
        when(thesaurus.getString(anyString(), anyString())).thenReturn("");
        ComSchedule mockedSchedule = mock(ComSchedule.class);
        when(schedulingService.findSchedule(1L)).thenReturn(mockedSchedule);

        Response response = target("/schedules/1/comTasks").queryParam("filter", ExtjsFilter.filter().property("available", "BOGUS").create()).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }


    private ComTask mockComTask(long id, String name) {
        ComTask comTask1 = mock(ComTask.class);
        when(comTask1.getId()).thenReturn(id);
        when(comTask1.getName()).thenReturn(name);
        when(taskService.findComTask(id)).thenReturn(comTask1);
        return comTask1;
    }

}
