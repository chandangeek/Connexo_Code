package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.interval.PartialTime;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecificationMessage;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.ItemizeCommunicationsFilterQueueMessage;
import com.energyict.mdc.device.data.tasks.ItemizeConnectionFilterRescheduleQueueMessage;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
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
        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter("deviceTypes", Arrays.asList(201l, 301l, 101l))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

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
    public void testDeviceGroupsAddedToFilter() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup1 = mock(QueryEndDeviceGroup.class);
        when(meteringGroupsService.findEndDeviceGroup(111)).thenReturn(Optional.of(queryEndDeviceGroup1));
        QueryEndDeviceGroup queryEndDeviceGroup2 = mock(QueryEndDeviceGroup.class);
        when(meteringGroupsService.findEndDeviceGroup(112)).thenReturn(Optional.of(queryEndDeviceGroup2));

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("deviceGroups", Arrays.asList(111L, 112L))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().deviceGroups).contains(queryEndDeviceGroup1).contains(queryEndDeviceGroup2).hasSize(2);

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

        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter("comSchedules", Arrays.asList(103l, 102l))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

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
        Finder<ComTask> comTaskFinder = mockFinder(Arrays.asList(comTask1, comTask2, comTask3));
        when(taskService.findAllComTasks()).thenReturn(comTaskFinder);

        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter("comTasks", Arrays.asList(13l, 12l))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

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
        assertThat(captor.getValue().lastSessionStart.getStart()).isEqualTo(Instant.ofEpochMilli(1407916436000L));
        assertThat(captor.getValue().lastSessionStart.getEnd()).isNull();
        assertThat(captor.getValue().lastSessionEnd).isNull();
    }

    @Test
    public void testStartIntervalToAddedToFilter() throws Exception {

        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter("startIntervalTo", 1407916436000L)).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ComTaskExecutionFilterSpecification> captor = ArgumentCaptor.forClass(ComTaskExecutionFilterSpecification.class);
        verify(communicationTaskService).findComTaskExecutionsByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionStart.getEnd()).isEqualTo(Instant.ofEpochMilli(1407916436000L));
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
        assertThat(captor.getValue().lastSessionStart.getStart()).isEqualTo(Instant.ofEpochMilli(1407916436000L));
        assertThat(captor.getValue().lastSessionStart.getEnd()).isEqualTo(Instant.ofEpochMilli(1407916784000L));
        assertThat(captor.getValue().lastSessionEnd.getStart()).isEqualTo(Instant.ofEpochMilli(1407916436000L));
        assertThat(captor.getValue().lastSessionEnd.getEnd()).isEqualTo(Instant.ofEpochMilli(1407916784000L));
    }

    @Test
    public void testEndIntervalFromAddedToFilter() throws Exception {
        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter("finishIntervalFrom", 1407916436000L)).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ComTaskExecutionFilterSpecification> captor = ArgumentCaptor.forClass(ComTaskExecutionFilterSpecification.class);
        verify(communicationTaskService).findComTaskExecutionsByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionEnd.getStart()).isEqualTo(Instant.ofEpochMilli(1407916436000L));
        assertThat(captor.getValue().lastSessionEnd.getEnd()).isNull();
        assertThat(captor.getValue().lastSessionStart).isNull();
    }

    @Test
    public void testEndIntervalToAddedToFilter() throws Exception {

        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter("finishIntervalTo", 1407916436000L)).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ComTaskExecutionFilterSpecification> captor = ArgumentCaptor.forClass(ComTaskExecutionFilterSpecification.class);
        verify(communicationTaskService).findComTaskExecutionsByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionEnd.getEnd()).isEqualTo(Instant.ofEpochMilli(1407916436000L));
        assertThat(captor.getValue().lastSessionEnd.getStart()).isNull();
        assertThat(captor.getValue().lastSessionStart).isNull();
    }

    @Test
    public void testEndIntervalFromAndToAddedToFilter() throws Exception {

        Map<String, Object> map = target("/communications").queryParam("filter", ExtjsFilter.filter().property("finishIntervalFrom", 1407916436000L).property("finishIntervalTo", 1407916784000L).create()).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ComTaskExecutionFilterSpecification> captor = ArgumentCaptor.forClass(ComTaskExecutionFilterSpecification.class);
        verify(communicationTaskService).findComTaskExecutionsByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionEnd.getStart()).isEqualTo(Instant.ofEpochMilli(1407916436000L));
        assertThat(captor.getValue().lastSessionEnd.getEnd()).isEqualTo(Instant.ofEpochMilli(1407916784000L));
        assertThat(captor.getValue().lastSessionStart).isNull();
    }

    @Test
    public void testCommunicationTaskJsonBinding() throws Exception {
        ScheduledComTaskExecution comTaskExecution1 = mock(ScheduledComTaskExecution.class);
        when(communicationTaskService.findComTaskExecutionsByFilter(Matchers.<ComTaskExecutionFilterSpecification>anyObject(), anyInt(), anyInt())).thenReturn(Arrays.<ComTaskExecution>asList(comTaskExecution1));
        ComSession comSession = mock(ComSession.class);
        when(comSession.getNumberOfFailedTasks()).thenReturn(401);
        when(comSession.getNumberOfSuccessFulTasks()).thenReturn(12);
        when(comSession.getNumberOfPlannedButNotExecutedTasks()).thenReturn(3);
        when(comSession.getSuccessIndicator()).thenReturn(ComSession.SuccessIndicator.Success);
        when(comSession.getStartDate()).thenReturn(Instant.now());
        when(comSession.getStopDate()).thenReturn(Instant.now());
        ComPort comPort = mock(ComPort.class);
        when(comPort.getName()).thenReturn("com port");
        when(comPort.getId()).thenReturn(99L);
        ComServer comServer = mock(ComServer.class);
        (when(comServer.getId())).thenReturn(991L);
        (when(comServer.getName())).thenReturn("rudi.local");
        when(comPort.getComServer()).thenReturn(comServer);
        when(comSession.getComPort()).thenReturn(comPort);

        ComTaskExecutionSession comTaskExecutionSession = mock(ComTaskExecutionSession.class);
        when(comTaskExecutionSession.getHighestPriorityCompletionCode()).thenReturn(CompletionCode.IOError);
        when(comTaskExecutionSession.getComSession()).thenReturn(comSession);
        when(communicationTaskService.findLastSessionFor(comTaskExecution1)).thenReturn(java.util.Optional.of(comTaskExecutionSession));
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
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(deviceConfiguration.getId()).thenReturn(123123L);
        when(deviceConfiguration.getName()).thenReturn("Configuration");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(comTaskExecution1.getCurrentTryCount()).thenReturn(999);
        when(comTaskExecution1.getDevice()).thenReturn(device);
        when(comTaskExecution1.getStatus()).thenReturn(TaskStatus.Busy);
        when(comTaskExecution1.getId()).thenReturn(123123L);
        when(device.getComTaskExecutions()).thenReturn(Arrays.<ComTaskExecution>asList(comTaskExecution1));
        when(comTaskExecutionSession.getSuccessIndicator()).thenReturn(ComTaskExecutionSession.SuccessIndicator.Success);
        when(comTaskExecutionSession.getStartDate()).thenReturn(Instant.now());
        when(comTaskExecutionSession.getStopDate()).thenReturn(Instant.now());
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getName()).thenReturn("comPortPool");
        when(comPortPool.getId()).thenReturn(1111L);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionType.getDirection()).thenReturn(ConnectionType.Direction.OUTBOUND);
        ComWindow window = mock(ComWindow.class);
        when(window.getStart()).thenReturn(PartialTime.fromHours(9));
        when(window.getEnd()).thenReturn(PartialTime.fromHours(17));
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(comSchedule.getName()).thenReturn("Weekly billing");
        when(comSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.WEEKS),new TimeDuration(12, TimeDuration.TimeUnit.HOURS)));
        when(comTaskExecution1.getComSchedule()).thenReturn(comSchedule);
        when(comTaskExecution1.getExecutionPriority()).thenReturn(100);
        when(comTaskExecution1.getLastExecutionStartTimestamp()).thenReturn(Instant.now());
        when(comTaskExecution1.getLastSuccessfulCompletionTimestamp()).thenReturn(Instant.now());
        when(comTaskExecution1.getNextExecutionTimestamp()).thenReturn(Instant.now());
        ComTask comTask1 = mock(ComTask.class);
        when(comTask1.getName()).thenReturn("Read all");
        ComTask comTask2 = mock(ComTask.class);
        when(comTask2.getName()).thenReturn("Basic check");
        when(comTaskExecution1.getComTasks()).thenReturn(Arrays.asList(comTask1, comTask2));
        ScheduledConnectionTask connectionTask = mockConnectionTask();
        doReturn(Optional.of(connectionTask)).when(comTaskExecution1).getConnectionTask();
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
                .containsKey("id")
                .hasSize(17);


    }

    private ScheduledConnectionTask mockConnectionTask() {
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTaskService.findConnectionTasksByFilter(Matchers.<ConnectionTaskFilterSpecification>anyObject(), anyInt(), anyInt())).thenReturn(Arrays.<ConnectionTask>asList(connectionTask));
        when(connectionTask.getId()).thenReturn(1234L);
        when(connectionTask.getName()).thenReturn("fancy name");
        PartialScheduledConnectionTask partialConnectionTask = mock(PartialScheduledConnectionTask.class);
        when(partialConnectionTask.getName()).thenReturn("partial connection task name");
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.isDefault()).thenReturn(true);
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getName()).thenReturn("com port pool");
        when(comPortPool.getId()).thenReturn(91L);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
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
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        ScheduledComTaskExecution comTaskExecution1 = mock(ScheduledComTaskExecution.class);
        when(comTaskExecution1.getConnectionTask()).thenReturn(Optional.of(connectionTask));
        when(comTaskExecution1.getCurrentTryCount()).thenReturn(999);
        when(comTaskExecution1.getDevice()).thenReturn(device);
        when(comTaskExecution1.getStatus()).thenReturn(TaskStatus.Busy);
        when(device.getComTaskExecutions()).thenReturn(Arrays.<ComTaskExecution>asList(comTaskExecution1));
        when(connectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        when(connectionTask.getTaskStatus()).thenReturn(TaskStatus.Busy);
        when(connectionTask.getSuccessIndicator()).thenReturn(ConnectionTask.SuccessIndicator.SUCCESS);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionType.getDirection()).thenReturn(ConnectionType.Direction.OUTBOUND);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        ComWindow window = mock(ComWindow.class);
        when(window.getStart()).thenReturn(PartialTime.fromHours(9));
        when(window.getEnd()).thenReturn(PartialTime.fromHours(17));
        when(connectionTask.getCommunicationWindow()).thenReturn(window);
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(comSchedule.getName()).thenReturn("Weekly billing");
        when(comSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.WEEKS),new TimeDuration(12, TimeDuration.TimeUnit.HOURS)));
        when(comTaskExecution1.getComSchedule()).thenReturn(comSchedule);
        when(comTaskExecution1.getExecutionPriority()).thenReturn(100);
        when(comTaskExecution1.getLastExecutionStartTimestamp()).thenReturn(Instant.now());
        when(comTaskExecution1.getLastSuccessfulCompletionTimestamp()).thenReturn(Instant.now());
        when(comTaskExecution1.getNextExecutionTimestamp()).thenReturn(Instant.now());
        Finder<ComTaskExecution> comTaskExecutionFinder = mockFinder(Arrays.<ComTaskExecution>asList(comTaskExecution1));
        when(communicationTaskService.findComTaskExecutionsByConnectionTask(connectionTask)).thenReturn(comTaskExecutionFinder);
        ComTaskExecutionSession comTaskExecutionSession = mock(ComTaskExecutionSession.class);
        when(comTaskExecutionSession.getHighestPriorityCompletionCode()).thenReturn(CompletionCode.Ok);
        when(communicationTaskService.findLastSessionFor(comTaskExecution1)).thenReturn(java.util.Optional.of(comTaskExecutionSession));
        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
        when(connectionTypePluggableClass.getName()).thenReturn(CommunicationResourceTest.class.getSimpleName());
        when(connectionTask.getPluggableClass()).thenReturn(connectionTypePluggableClass);
        return connectionTask;
    }

    @Test
    public void testRunConnectionsWithFilter() throws Exception {
        mockAppServers(CommunicationTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION, CommunicationTaskService.COMMUNICATION_RESCHEDULER_QUEUE_DESTINATION);
        when(jsonService.serialize(any())).thenReturn("json");
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(messageService.getDestinationSpec("ItemizeCommFilterQD")).thenReturn(Optional.of(destinationSpec));
        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        when(destinationSpec.message("json")).thenReturn(messageBuilder);

        ComTaskExecutionFilterSpecificationMessage message = new ComTaskExecutionFilterSpecificationMessage();
        message.currentStates.add("OnHold");
        message.deviceGroups.add(1003L);
        message.latestResults.add("IoError");
        message.deviceTypes.add(1004L);
        message.deviceTypes.add(1005L);
        Instant now = Instant.now();
        message.startIntervalFrom = now;
        message.startIntervalTo = now.plusSeconds(1);
        message.finishIntervalFrom = now.plusSeconds(2);
        message.finishIntervalTo = now.plusSeconds(3);
        message.comTasks.add(1002L);
        message.comSchedules.add(1001L);

        CommunicationsBulkRequestInfo info = new CommunicationsBulkRequestInfo();
        info.filter = message;
        Response response = target("/communications/run").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        verify(jsonService).serialize(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue() instanceof ItemizeConnectionFilterRescheduleQueueMessage);
        ItemizeCommunicationsFilterQueueMessage itemizeConnectionFilterQueueMessage = (ItemizeCommunicationsFilterQueueMessage) argumentCaptor.getValue();
        assertThat(itemizeConnectionFilterQueueMessage.comTaskExecutionFilterSpecificationMessage.currentStates).containsOnly(TaskStatus.OnHold.name());
        assertThat(itemizeConnectionFilterQueueMessage.comTaskExecutionFilterSpecificationMessage.deviceGroups).containsOnly(1003L);
        assertThat(itemizeConnectionFilterQueueMessage.comTaskExecutionFilterSpecificationMessage.latestResults).containsOnly(CompletionCode.IOError.name());
        assertThat(itemizeConnectionFilterQueueMessage.comTaskExecutionFilterSpecificationMessage.deviceTypes).containsOnly(1004L, 1005L);
        assertThat(itemizeConnectionFilterQueueMessage.comTaskExecutionFilterSpecificationMessage.startIntervalFrom).isEqualTo(now);
        assertThat(itemizeConnectionFilterQueueMessage.comTaskExecutionFilterSpecificationMessage.startIntervalTo).isEqualTo(now.plusSeconds(1));
        assertThat(itemizeConnectionFilterQueueMessage.comTaskExecutionFilterSpecificationMessage.finishIntervalFrom).isEqualTo(now.plusSeconds(2));
        assertThat(itemizeConnectionFilterQueueMessage.comTaskExecutionFilterSpecificationMessage.finishIntervalTo).isEqualTo(now.plusSeconds(3));
        assertThat(itemizeConnectionFilterQueueMessage.comTaskExecutionFilterSpecificationMessage.comTasks).containsOnly(1002L);
        assertThat(itemizeConnectionFilterQueueMessage.comTaskExecutionFilterSpecificationMessage.comSchedules).containsOnly(1001L);
        assertThat(itemizeConnectionFilterQueueMessage.action).isEqualTo("scheduleNow");
    }


    private <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(JsonQueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        return finder;
    }

}