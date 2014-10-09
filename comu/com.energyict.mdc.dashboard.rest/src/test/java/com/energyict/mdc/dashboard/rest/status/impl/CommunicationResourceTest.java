package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.protocols.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.google.common.base.Optional;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.dashboard.rest.status.ComServerStatusResource} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (17:27)
 */
public class CommunicationResourceTest extends DashboardApplicationJerseyTest {

    @Test
    public void testDeviceTypesAddedToFilter() throws Exception {
        DeviceType deviceType1 = mock(DeviceType.class);
        when(deviceType1.getId()).thenReturn(101L);
        DeviceType deviceType2 = mock(DeviceType.class);
        when(deviceType2.getId()).thenReturn(201L);
        DeviceType deviceType3 = mock(DeviceType.class);
        when(deviceType3.getId()).thenReturn(301L);

        Finder<DeviceType> deviceTypeFinder = mockFinder(Arrays.asList(deviceType1, deviceType2, deviceType3));
        when(deviceConfigurationService.findAllDeviceTypes()).thenReturn(deviceTypeFinder);
        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter("deviceTypes", Arrays.asList(201, 301, 101))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ComTaskExecutionFilterSpecification> captor = ArgumentCaptor.forClass(ComTaskExecutionFilterSpecification.class);
        verify(communicationTaskService).findComTaskExecutionsByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().deviceTypes).contains(deviceType1).contains(deviceType2).contains(deviceType3).hasSize(3);
    }

    @Test
    public void testCurrentStateAddedToFilter() throws Exception {

        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter("currentStates", Arrays.asList("Busy", "OnHold", "Retrying"))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ComTaskExecutionFilterSpecification> captor = ArgumentCaptor.forClass(ComTaskExecutionFilterSpecification.class);
        verify(communicationTaskService).findComTaskExecutionsByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().taskStatuses).contains(TaskStatus.Busy).contains(TaskStatus.OnHold).contains(TaskStatus.Retrying).hasSize(3);

    }

    @Test
    public void testLatestResultsAddedToFilter() throws Exception {
        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter("latestResults", Arrays.asList("TimeError", "ProtocolError"))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ComTaskExecutionFilterSpecification> captor = ArgumentCaptor.forClass(ComTaskExecutionFilterSpecification.class);
        verify(communicationTaskService).findComTaskExecutionsByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().latestResults).contains(CompletionCode.TimeError).contains(CompletionCode.ProtocolError).hasSize(2);
    }

    @Test
    public void testComSchedulesAddedToFilter() throws Exception {
        ComSchedule comSchedule1 = mock(ComSchedule.class);
        when(comSchedule1.getId()).thenReturn(101L);
        ComSchedule comSchedule2 = mock(ComSchedule.class);
        when(comSchedule2.getId()).thenReturn(102L);
        ComSchedule comSchedule3 = mock(ComSchedule.class);
        when(comSchedule3.getId()).thenReturn(103L);
        when(schedulingService.findAllSchedules()).thenReturn(Arrays.asList(comSchedule1, comSchedule2, comSchedule3));

        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter("comSchedules", Arrays.asList(103, 102))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ComTaskExecutionFilterSpecification> captor = ArgumentCaptor.forClass(ComTaskExecutionFilterSpecification.class);
        verify(communicationTaskService).findComTaskExecutionsByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().comSchedules).contains(comSchedule2).contains(comSchedule3).hasSize(2);
    }

    @Test
    public void testComTasksAddedToFilter() throws Exception {
        ComTask comTask1 = mock(ComTask.class);
        when(comTask1.getId()).thenReturn(11L);
        ComTask comTask2 = mock(ComTask.class);
        when(comTask2.getId()).thenReturn(12L);
        ComTask comTask3 = mock(ComTask.class);
        when(comTask3.getId()).thenReturn(13L);
        when(taskService.findAllComTasks()).thenReturn(Arrays.asList(comTask1, comTask2, comTask3));

        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter("comTasks", Arrays.asList(13, 12))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ComTaskExecutionFilterSpecification> captor = ArgumentCaptor.forClass(ComTaskExecutionFilterSpecification.class);
        verify(communicationTaskService).findComTaskExecutionsByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().comTasks).contains(comTask2).contains(comTask3).hasSize(2);
    }

    @Test
    public void testBadRequestWhenPagingIsMissing() throws Exception {
        Response response = target("/communications").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    public void testStartIntervalFromAddedToFilter() throws Exception {
        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter("startIntervalFrom", 1407916436000L)).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ComTaskExecutionFilterSpecification> captor = ArgumentCaptor.forClass(ComTaskExecutionFilterSpecification.class);
        verify(communicationTaskService).findComTaskExecutionsByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionStart.getStart()).isEqualTo(new Date(1407916436000L));
        assertThat(captor.getValue().lastSessionStart.getEnd()).isNull();
        assertThat(captor.getValue().lastSessionEnd).isNull();
    }

    @Test
    public void testStartIntervalToAddedToFilter() throws Exception {

        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter("startIntervalTo", 1407916436000L)).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ComTaskExecutionFilterSpecification> captor = ArgumentCaptor.forClass(ComTaskExecutionFilterSpecification.class);
        verify(communicationTaskService).findComTaskExecutionsByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionStart.getEnd()).isEqualTo(new Date(1407916436000L));
        assertThat(captor.getValue().lastSessionStart.getStart()).isNull();
        assertThat(captor.getValue().lastSessionEnd).isNull();
    }

    @Test
    public void testStartAndFinishIntervalFromAndToAddedToFilter() throws Exception {

        Map<String, Object> map = target("/communications").queryParam("filter",
                ExtjsFilter.filter()
                        .property("startIntervalFrom", 1407916436000L).property("startIntervalTo", 1407916784000L)
                        .property("finishIntervalFrom", 1407916436000L).property("finishIntervalTo", 1407916784000L)
                        .create()).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ComTaskExecutionFilterSpecification> captor = ArgumentCaptor.forClass(ComTaskExecutionFilterSpecification.class);
        verify(communicationTaskService).findComTaskExecutionsByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionStart.getStart()).isEqualTo(new Date(1407916436000L));
        assertThat(captor.getValue().lastSessionStart.getEnd()).isEqualTo(new Date(1407916784000L));
        assertThat(captor.getValue().lastSessionEnd.getStart()).isEqualTo(new Date(1407916436000L));
        assertThat(captor.getValue().lastSessionEnd.getEnd()).isEqualTo(new Date(1407916784000L));
    }

    @Test
    public void testEndIntervalFromAddedToFilter() throws Exception {
        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter("finishIntervalFrom", 1407916436000L)).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ComTaskExecutionFilterSpecification> captor = ArgumentCaptor.forClass(ComTaskExecutionFilterSpecification.class);
        verify(communicationTaskService).findComTaskExecutionsByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionEnd.getStart()).isEqualTo(new Date(1407916436000L));
        assertThat(captor.getValue().lastSessionEnd.getEnd()).isNull();
        assertThat(captor.getValue().lastSessionStart).isNull();
    }

    @Test
    public void testEndIntervalToAddedToFilter() throws Exception {

        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter("finishIntervalTo", 1407916436000L)).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ComTaskExecutionFilterSpecification> captor = ArgumentCaptor.forClass(ComTaskExecutionFilterSpecification.class);
        verify(communicationTaskService).findComTaskExecutionsByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionEnd.getEnd()).isEqualTo(new Date(1407916436000L));
        assertThat(captor.getValue().lastSessionEnd.getStart()).isNull();
        assertThat(captor.getValue().lastSessionStart).isNull();
    }

    @Test
    public void testEndIntervalFromAndToAddedToFilter() throws Exception {

        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter().property("finishIntervalFrom", 1407916436000L).property("finishIntervalTo", 1407916784000L).create()).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ComTaskExecutionFilterSpecification> captor = ArgumentCaptor.forClass(ComTaskExecutionFilterSpecification.class);
        verify(communicationTaskService).findComTaskExecutionsByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionEnd.getStart()).isEqualTo(new Date(1407916436000L));
        assertThat(captor.getValue().lastSessionEnd.getEnd()).isEqualTo(new Date(1407916784000L));
        assertThat(captor.getValue().lastSessionStart).isNull();
    }

    @Test
    public void testCommunicationTaskJsonBinding() throws Exception {
        ScheduledComTaskExecution comTaskExecution1 = mock(ScheduledComTaskExecution.class);
        when(communicationTaskService.findComTaskExecutionsByFilter(Matchers.<ComTaskExecutionFilterSpecification>anyObject(), anyInt(), anyInt())).thenReturn(Arrays.<ComTaskExecution>asList(comTaskExecution1));
        ComTaskExecutionSession comSession = mock(ComTaskExecutionSession.class);
        when(comSession.getHighestPriorityCompletionCode()).thenReturn(CompletionCode.IOError);
        when(communicationTaskService.findLastSessionFor(comTaskExecution1)).thenReturn(Optional.of(comSession));
        PartialScheduledConnectionTask partialConnectionTask = mock(PartialScheduledConnectionTask.class);
        when(partialConnectionTask.getName()).thenReturn("partial connection task name");
        Device device = mock(Device.class);
        when(device.getmRID()).thenReturn("1234-5678-9012");
        when(device.getName()).thenReturn("some device");
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1010L);
        when(deviceType.getName()).thenReturn("device type");
        when(device.getDeviceType()).thenReturn(deviceType);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(123123L);
        when(deviceConfiguration.getName()).thenReturn("Configuration");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(comTaskExecution1.getCurrentTryCount()).thenReturn(999);
        when(comTaskExecution1.getDevice()).thenReturn(device);
        when(comTaskExecution1.getStatus()).thenReturn(TaskStatus.Busy);
        when(device.getComTaskExecutions()).thenReturn(Arrays.<ComTaskExecution>asList(comTaskExecution1));
        when(comSession.getSuccessIndicator()).thenReturn(ComTaskExecutionSession.SuccessIndicator.Success);
        when(comSession.getStartDate()).thenReturn(new Date());
        when(comSession.getStopDate()).thenReturn(new Date());
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getName()).thenReturn("comPortPool");
        when(comPortPool.getId()).thenReturn(1111L);
        OutboundTcpIpConnectionType connectionType = mock(OutboundTcpIpConnectionType.class);
        when(connectionType.getDirection()).thenReturn(ConnectionType.Direction.OUTBOUND);
        ComWindow window = mock(ComWindow.class);
        when(window.getStart()).thenReturn(PartialTime.fromHours(9));
        when(window.getEnd()).thenReturn(PartialTime.fromHours(17));
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(comSchedule.getName()).thenReturn("Weekly billing");
        when(comSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration(1, TimeDuration.WEEKS), new TimeDuration(12, TimeDuration.HOURS)));
        when(comTaskExecution1.getComSchedule()).thenReturn(comSchedule);
        when(comTaskExecution1.getExecutionPriority()).thenReturn(100);
        when(comTaskExecution1.getLastExecutionStartTimestamp()).thenReturn(new Date());
        when(comTaskExecution1.getLastSuccessfulCompletionTimestamp()).thenReturn(new Date());
        when(comTaskExecution1.getNextExecutionTimestamp()).thenReturn(new Date());
        ComTask comTask1 = mock(ComTask.class);
        when(comTask1.getName()).thenReturn("Read all");
        ComTask comTask2 = mock(ComTask.class);
        when(comTask2.getName()).thenReturn("Basic check");
        when(comTaskExecution1.getComTasks()).thenReturn(Arrays.asList(comTask1, comTask2));
        ScheduledConnectionTask connectionTask = mockConnectionTask();
        when(comTaskExecution1.getConnectionTask()).thenReturn((ConnectionTask) connectionTask);
        Map<String, Object> map = target("/communications").queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        assertThat(map).containsKey("total");
        assertThat(map).containsKey("communicationTasks");
        Map<String, Object> communicationTaskMap = (Map) ((List) map.get("communicationTasks")).get(0);
        assertThat(communicationTaskMap)
                .containsKey("name")
                .containsKey("comTasks")
                .containsKey("device")
                .containsKey("deviceType")
                .containsKey("deviceConfiguration")
                .containsKey("comScheduleName")
                .containsKey("comScheduleFrequency")
                .containsKey("urgency")
                .containsKey("currentState")
                .containsKey("latestResult")
                .containsKey("startTime")
                .containsKey("successfulFinishTime")
                .containsKey("nextCommunication")
                .containsKey("alwaysExecuteOnInbound")
                .containsKey("connectionTask")
                .hasSize(15);


    }

    private ScheduledConnectionTask mockConnectionTask() {
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTaskService.findConnectionTasksByFilter(Matchers.<ConnectionTaskFilterSpecification>anyObject(), anyInt(), anyInt())).thenReturn(Arrays.<ConnectionTask>asList(connectionTask));
        ComSession comSession = mock(ComSession.class);
        Optional<ComSession> comSessionOptional = Optional.of(comSession);
        when(connectionTask.getLastComSession()).thenReturn(comSessionOptional);
        when(connectionTask.getId()).thenReturn(1234L);
        when(connectionTask.getName()).thenReturn("fancy name");
        PartialScheduledConnectionTask partialConnectionTask = mock(PartialScheduledConnectionTask.class);
        when(partialConnectionTask.getName()).thenReturn("partial connection task name");
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.isDefault()).thenReturn(true);
        Device device = mock(Device.class);
        when(device.getmRID()).thenReturn("1234-5678-9012");
        when(device.getName()).thenReturn("some device");
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1010L);
        when(deviceType.getName()).thenReturn("device type");
        when(device.getDeviceType()).thenReturn(deviceType);
        when(connectionTask.getDevice()).thenReturn(device);
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(123123L);
        when(deviceConfiguration.getName()).thenReturn("123123");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ScheduledComTaskExecution comTaskExecution1 = mock(ScheduledComTaskExecution.class);
        when(comTaskExecution1.getConnectionTask()).thenReturn((ConnectionTask) connectionTask);
        when(comTaskExecution1.getCurrentTryCount()).thenReturn(999);
        when(comTaskExecution1.getDevice()).thenReturn(device);
        when(comTaskExecution1.getStatus()).thenReturn(TaskStatus.Busy);
        when(device.getComTaskExecutions()).thenReturn(Arrays.<ComTaskExecution>asList(comTaskExecution1));
        when(connectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        when(connectionTask.getTaskStatus()).thenReturn(TaskStatus.Busy);
        when(connectionTask.getSuccessIndicator()).thenReturn(ConnectionTask.SuccessIndicator.SUCCESS);
        when(comSession.getNumberOfFailedTasks()).thenReturn(401);
        when(comSession.getNumberOfSuccessFulTasks()).thenReturn(12);
        when(comSession.getNumberOfPlannedButNotExecutedTasks()).thenReturn(3);
        when(comSession.getSuccessIndicator()).thenReturn(ComSession.SuccessIndicator.Success);
        when(comSession.getStartDate()).thenReturn(Instant.now());
        when(comSession.getStopDate()).thenReturn(Instant.now());
        ComPort comPort = mock(ComPort.class);
        when(comPort.getName()).thenReturn("com port");
        when(comPort.getId()).thenReturn(99L);
        when(comSession.getComPort()).thenReturn(comPort);
        when(connectionTask.getLastComSession()).thenReturn(Optional.of(comSession));
        OutboundTcpIpConnectionType connectionType = mock(OutboundTcpIpConnectionType.class);
        when(connectionType.getDirection()).thenReturn(ConnectionType.Direction.OUTBOUND);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        ComWindow window = mock(ComWindow.class);
        when(window.getStart()).thenReturn(PartialTime.fromHours(9));
        when(window.getEnd()).thenReturn(PartialTime.fromHours(17));
        when(connectionTask.getCommunicationWindow()).thenReturn(window);
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(comSchedule.getName()).thenReturn("Weekly billing");
        when(comSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration(1, TimeDuration.WEEKS), new TimeDuration(12, TimeDuration.HOURS)));
        when(comTaskExecution1.getComSchedule()).thenReturn(comSchedule);
        when(comTaskExecution1.getExecutionPriority()).thenReturn(100);
        when(comTaskExecution1.getLastExecutionStartTimestamp()).thenReturn(new Date());
        when(comTaskExecution1.getLastSuccessfulCompletionTimestamp()).thenReturn(new Date());
        when(comTaskExecution1.getNextExecutionTimestamp()).thenReturn(new Date());
        when(communicationTaskService.findComTaskExecutionsByConnectionTask(connectionTask)).thenReturn(Arrays.<ComTaskExecution>asList(comTaskExecution1));
        ComTaskExecutionSession comTaskExecutionSession = mock(ComTaskExecutionSession.class);
        when(comTaskExecutionSession.getHighestPriorityCompletionCode()).thenReturn(CompletionCode.Ok);
        when(communicationTaskService.findLastSessionFor(comTaskExecution1)).thenReturn(Optional.of(comTaskExecutionSession));
        return connectionTask;
    }

    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.defaultSortColumn(anyString())).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        return finder;
    }


}