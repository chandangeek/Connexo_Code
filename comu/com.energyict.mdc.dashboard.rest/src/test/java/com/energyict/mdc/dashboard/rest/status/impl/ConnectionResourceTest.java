package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.protocols.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.google.common.base.Optional;
import com.jayway.jsonpath.JsonModel;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.dashboard.rest.status.ComServerStatusResource} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (17:27)
 */
public class ConnectionResourceTest extends DashboardApplicationJerseyTest {

    @Test
    public void testDeviceTypesAddedToFilter() throws Exception {
        DeviceType deviceType1 = mock(DeviceType.class);
        DeviceType deviceType2 = mock(DeviceType.class);
        DeviceType deviceType3 = mock(DeviceType.class);

        when(deviceConfigurationService.findDeviceType(101L)).thenReturn(deviceType1);
        when(deviceConfigurationService.findDeviceType(102L)).thenReturn(deviceType2);
        when(deviceConfigurationService.findDeviceType(103L)).thenReturn(deviceType3);

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("deviceTypes", Arrays.asList(101L,102L,103L))).queryParam("start",0).queryParam("limit",10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().deviceTypes).contains(deviceType1).contains(deviceType2).contains(deviceType3).hasSize(3);

    }

    @Test
    public void testBadRequestWhenPagingIsMissing() throws Exception {
        Response response = target("/connections").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    public void testCurrentStateAddedToFilter() throws Exception {

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("currentStates", Arrays.asList("Busy","OnHold"))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().taskStatuses).contains(TaskStatus.Busy).contains(TaskStatus.OnHold).hasSize(2);

    }

    @Test
    public void testComPortPoolAddedToFilter() throws Exception {
        ComPortPool comPortPool1 = mock(ComPortPool.class);
        when(comPortPool1.getId()).thenReturn(1001L);
        ComPortPool comPortPool2 = mock(ComPortPool.class);
        when(comPortPool2.getId()).thenReturn(1002L);
        when(engineModelService.findAllComPortPools()).thenReturn(Arrays.asList(comPortPool1,comPortPool2));

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("comPortPools", Arrays.asList(1001L,1002L))).queryParam("start", 0).queryParam("limit",10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().comPortPools).contains(comPortPool1).contains(comPortPool2).hasSize(2);

    }

    @Test
    public void testConnectionTypesAddedToFilter() throws Exception {
        ConnectionTypePluggableClass connectionType1 = mock(ConnectionTypePluggableClass.class);
        when(protocolPluggableService.findConnectionTypePluggableClass(2001)).thenReturn(connectionType1);
        ConnectionTypePluggableClass connectionType2 = mock(ConnectionTypePluggableClass.class);
        when(protocolPluggableService.findConnectionTypePluggableClass(2002)).thenReturn(connectionType2);

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("connectionTypes", Arrays.asList("2001","2002"))).queryParam("start", 0).queryParam("limit",10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().connectionTypes).contains(connectionType1).contains(connectionType2).hasSize(2);
    }

    @Test
    public void testLatestResultsAddedToFilter() throws Exception {
        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("latestResults", Arrays.asList("Success","SetupError","Broken"))).queryParam("start", 0).queryParam("limit",10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().latestResults).contains(ComSession.SuccessIndicator.Success).contains(ComSession.SuccessIndicator.Broken).contains(ComSession.SuccessIndicator.Broken).hasSize(3);
    }

    @Test
    public void testLatestStatesAddedToFilter() throws Exception {
        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("latestStates", Arrays.asList("Success","NotApplicable","Failure"))).queryParam("start", 0).queryParam("limit",10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().latestStatuses).contains(ConnectionTask.SuccessIndicator.SUCCESS).contains(ConnectionTask.SuccessIndicator.FAILURE).contains(ConnectionTask.SuccessIndicator.NOT_APPLICABLE).hasSize(3);
    }

    @Test
    public void testStartIntervalFromAddedToFilter() throws Exception {
        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("startIntervalFrom", 1407916436000L)).queryParam("start", 0).queryParam("limit",10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionStart.getStart()).isEqualTo(new Date(1407916436000L));
        assertThat(captor.getValue().lastSessionStart.getEnd()).isNull();
        assertThat(captor.getValue().lastSessionEnd).isNull();
    }

    @Test
    public void testStartIntervalToAddedToFilter() throws Exception {

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("startIntervalTo", 1407916436000L)).queryParam("start", 0).queryParam("limit",10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionStart.getEnd()).isEqualTo(new Date(1407916436000L));
        assertThat(captor.getValue().lastSessionStart.getStart()).isNull();
        assertThat(captor.getValue().lastSessionEnd).isNull();
    }

    @Test
    public void testStartAndFinishIntervalFromAndToAddedToFilter() throws Exception {

        Map<String, Object> map = target("/connections").queryParam("filter",
                ExtjsFilter.filter()
                        .property("startIntervalFrom", 1407916436000L).property("startIntervalTo", 1407916784000L)
                        .property("finishIntervalFrom", 1407916436000L).property("finishIntervalTo", 1407916784000L)
                        .create()).queryParam("start", 0).queryParam("limit",10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionStart.getStart()).isEqualTo(new Date(1407916436000L));
        assertThat(captor.getValue().lastSessionStart.getEnd()).isEqualTo(new Date(1407916784000L));
        assertThat(captor.getValue().lastSessionEnd.getStart()).isEqualTo(new Date(1407916436000L));
        assertThat(captor.getValue().lastSessionEnd.getEnd()).isEqualTo(new Date(1407916784000L));
    }

    @Test
    public void testEndIntervalFromAddedToFilter() throws Exception {
        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("finishIntervalFrom", 1407916436000L)).queryParam("start", 0).queryParam("limit",10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionEnd.getStart()).isEqualTo(new Date(1407916436000L));
        assertThat(captor.getValue().lastSessionEnd.getEnd()).isNull();
        assertThat(captor.getValue().lastSessionStart).isNull();
    }

    @Test
    public void testEndIntervalToAddedToFilter() throws Exception {

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("finishIntervalTo", 1407916436000L)).queryParam("start", 0).queryParam("limit",10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionEnd.getEnd()).isEqualTo(new Date(1407916436000L));
        assertThat(captor.getValue().lastSessionEnd.getStart()).isNull();
        assertThat(captor.getValue().lastSessionStart).isNull();
    }

    @Test
    public void testEndIntervalFromAndToAddedToFilter() throws Exception {

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter().property("finishIntervalFrom", 1407916436000L).property("finishIntervalTo", 1407916784000L).create()).queryParam("start", 0).queryParam("limit",10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionEnd.getStart()).isEqualTo(new Date(1407916436000L));
        assertThat(captor.getValue().lastSessionEnd.getEnd()).isEqualTo(new Date(1407916784000L));
        assertThat(captor.getValue().lastSessionStart).isNull();
    }

    @Test
    public void testConnectionTaskJsonBinding() throws Exception {
        DateTime now = DateTime.now();
        Date startDate = now.toDate();
        Date endDate = now.plusHours(1).toDate();
        Date plannedNext = now.plusHours(2).toDate();
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTaskService.findConnectionTasksByFilter(Matchers.<ConnectionTaskFilterSpecification>anyObject(), anyInt(), anyInt())).thenReturn(Arrays.<ConnectionTask>asList(connectionTask));
        ComSession comSession = mock(ComSession.class);
        Optional<ComSession> comSessionOptional = Optional.of(comSession);
        when(connectionTask.getLastComSession()).thenReturn(comSessionOptional);
        when(connectionTask.getId()).thenReturn(1234L);
        when(connectionTask.getName()).thenReturn("fancy name");
        PartialScheduledConnectionTask partialConnectionTask = mock(PartialScheduledConnectionTask.class);
        when(partialConnectionTask.getName()).thenReturn("partial connection task name");
        when(partialConnectionTask.getId()).thenReturn(991L);
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
        when(comTaskExecution1.getStatus()).thenReturn(TaskStatus.NeverCompleted);
        when(device.getComTaskExecutions()).thenReturn(Arrays.<ComTaskExecution>asList(comTaskExecution1));
        when(connectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        when(connectionTask.getSuccessIndicator()).thenReturn(ConnectionTask.SuccessIndicator.SUCCESS);
        when(connectionTask.getTaskStatus()).thenReturn(TaskStatus.OnHold);
        when(connectionTask.getCurrentRetryCount()).thenReturn(7);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(1212L);
        when(comServer.getName()).thenReturn("com server");
        when(connectionTask.getExecutingComServer()).thenReturn(comServer);
        when(comSession.getNumberOfFailedTasks()).thenReturn(401);
        when(comSession.getNumberOfSuccessFulTasks()).thenReturn(12);
        when(comSession.getNumberOfPlannedButNotExecutedTasks()).thenReturn(3);
        when(comSession.getSuccessIndicator()).thenReturn(ComSession.SuccessIndicator.Success);
        when(comSession.getStartDate()).thenReturn(startDate);
        when(comSession.getStopDate()).thenReturn(endDate);
        when(comSession.getTotalDuration()).thenReturn(Duration.standardSeconds(4L));
        when(connectionTask.getConnectionType()).thenReturn(new OutboundTcpIpConnectionType());
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getName()).thenReturn("com port");
        when(comPort.getId()).thenReturn(99L);
        when(comSession.getComPort()).thenReturn(comPort);
        when(connectionTask.getLastComSession()).thenReturn(Optional.of(comSession));
        when(connectionTask.getPlannedNextExecutionTimestamp()).thenReturn(plannedNext);
        ComWindow window = mock(ComWindow.class);
        when(window.getStart()).thenReturn(PartialTime.fromHours(9));
        when(window.getEnd()).thenReturn(PartialTime.fromHours(17));
        when(connectionTask.getCommunicationWindow()).thenReturn(window);
        ComSchedule comSchedule=mock(ComSchedule.class);
        when(comSchedule.getName()).thenReturn("Weekly billing");
        when(comSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration(1, TimeDuration.WEEKS),new TimeDuration(12, TimeDuration.HOURS)));
        when(comTaskExecution1.getComSchedule()).thenReturn(comSchedule);
        when(comTaskExecution1.getExecutionPriority()).thenReturn(100);
        when(comTaskExecution1.getLastExecutionStartTimestamp()).thenReturn(new Date());
        when(comTaskExecution1.getLastSuccessfulCompletionTimestamp()).thenReturn(new Date());
        when(comTaskExecution1.getNextExecutionTimestamp()).thenReturn(new Date());
        when(communicationTaskService.findComTaskExecutionsByConnectionTask(connectionTask)).thenReturn(Arrays.<ComTaskExecution>asList(comTaskExecution1));
        ComTaskExecutionSession comTaskExecutionSession = mock(ComTaskExecutionSession.class);
        when(comTaskExecutionSession.getHighestPriorityCompletionCode()).thenReturn(CompletionCode.Ok);
        when(communicationTaskService.findLastSessionFor(comTaskExecution1)).thenReturn(Optional.of(comTaskExecutionSession));
        String response = target("/connections").queryParam("start",0).queryParam("limit", 10).request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);

        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.connectionTasks")).hasSize(1);
        assertThat(jsonModel.<String>get("$.connectionTasks[0].device.id")).isEqualTo("1234-5678-9012");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].device.name")).isEqualTo("some device");
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].deviceType.id")).isEqualTo(1010);
        assertThat(jsonModel.<String>get("$.connectionTasks[0].deviceType.name")).isEqualTo("device type");
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].deviceConfiguration.id")).isEqualTo(123123);
        assertThat(jsonModel.<String>get("$.connectionTasks[0].deviceConfiguration.name")).isEqualTo("123123");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].currentState.id")).isEqualTo("OnHold");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].currentState.displayValue")).isEqualTo("Inactive");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].latestResult.id")).isEqualTo("Success");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].latestResult.displayValue")).isEqualTo("Success");
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].taskCount.numberOfSuccessfulTasks")).isEqualTo(12);
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].taskCount.numberOfFailedTasks")).isEqualTo(401);
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].taskCount.numberOfIncompleteTasks")).isEqualTo(3);
        assertThat(jsonModel.<Long>get("$.connectionTasks[0].startDateTime")).isEqualTo(startDate.getTime());
        assertThat(jsonModel.<Long>get("$.connectionTasks[0].endDateTime")).isEqualTo(endDate.getTime());
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].duration.count")).isEqualTo(4);
        assertThat(jsonModel.<String>get("$.connectionTasks[0].duration.timeUnit")).isEqualTo("seconds");
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].comServer.id")).isEqualTo(1212);
        assertThat(jsonModel.<String>get("$.connectionTasks[0].comServer.name")).isEqualTo("com server");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].direction")).isEqualTo("Outbound");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].connectionType")).isEqualTo("OutboundTcpIp");
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].connectionMethod.id")).isEqualTo(991);
        assertThat(jsonModel.<String>get("$.connectionTasks[0].connectionMethod.name")).isEqualTo("partial connection task name (default)");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].connectionStrategy.id")).isEqualTo("asSoonAsPossible");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].connectionStrategy.displayValue")).isEqualTo("As soon as possible");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].window")).isEqualTo("09:00 - 17:00");
        assertThat(jsonModel.<Long>get("$.connectionTasks[0].nextExecution")).isEqualTo(plannedNext.getTime());
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].communicationTasks.count")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.connectionTasks[0].communicationTasks.communicationsTasks")).hasSize(1);
    }
}