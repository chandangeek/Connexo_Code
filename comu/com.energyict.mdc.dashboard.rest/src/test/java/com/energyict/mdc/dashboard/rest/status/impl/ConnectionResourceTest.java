package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecificationMessage;
import com.energyict.mdc.device.data.tasks.ItemizeConnectionFilterRescheduleQueueMessage;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.jayway.jsonpath.JsonModel;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private static final String CONNECTION_TYPE_PLUGGABLE_CLASS_NAME = ConnectionResourceTest.class.getSimpleName();

    @Test
    public void testDeviceTypesAddedToFilter() throws Exception {
        DeviceType deviceType1 = mock(DeviceType.class);
        DeviceType deviceType2 = mock(DeviceType.class);
        DeviceType deviceType3 = mock(DeviceType.class);

        when(deviceConfigurationService.findDeviceType(101L)).thenReturn(Optional.of(deviceType1));
        when(deviceConfigurationService.findDeviceType(102L)).thenReturn(Optional.of(deviceType2));
        when(deviceConfigurationService.findDeviceType(103L)).thenReturn(Optional.of(deviceType3));

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("deviceTypes", Arrays.asList(101L, 102L, 103L))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

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

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("currentStates", Arrays.asList("Busy", "OnHold"))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

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
        when(engineConfigurationService.findAllComPortPools()).thenReturn(Arrays.asList(comPortPool1, comPortPool2));

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("comPortPools", Arrays.asList(1001L, 1002L))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().comPortPools).contains(comPortPool1).contains(comPortPool2).hasSize(2);

    }

    @Test
    public void testDeviceGroupsAddedToFilter() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup1 = mock(QueryEndDeviceGroup.class);
        when(meteringGroupsService.findEndDeviceGroup(11)).thenReturn(Optional.of(queryEndDeviceGroup1));
        QueryEndDeviceGroup queryEndDeviceGroup2 = mock(QueryEndDeviceGroup.class);
        when(meteringGroupsService.findEndDeviceGroup(12)).thenReturn(Optional.of(queryEndDeviceGroup2));

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("deviceGroups", Arrays.asList(11L, 12L))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().deviceGroups).contains(queryEndDeviceGroup1).contains(queryEndDeviceGroup2).hasSize(2);

    }

    @Test
    public void testConnectionTypesAddedToFilter() throws Exception {
        ConnectionTypePluggableClass connectionType1 = mock(ConnectionTypePluggableClass.class);
        when(protocolPluggableService.findConnectionTypePluggableClass(2001)).thenReturn(Optional.of(connectionType1));
        ConnectionTypePluggableClass connectionType2 = mock(ConnectionTypePluggableClass.class);
        when(protocolPluggableService.findConnectionTypePluggableClass(2002)).thenReturn(Optional.of(connectionType2));

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("connectionTypes", Arrays.asList(2001l, 2002l))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().connectionTypes).contains(connectionType1).contains(connectionType2).hasSize(2);
    }

    @Test
    public void testLatestResultsAddedToFilter() throws Exception {
        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("latestResults", Arrays.asList("Success", "SetupError", "Broken"))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().latestResults).contains(ComSession.SuccessIndicator.Success).contains(ComSession.SuccessIndicator.Broken).contains(ComSession.SuccessIndicator.Broken).hasSize(3);
    }

    @Test
    public void testLatestStatesAddedToFilter() throws Exception {
        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("latestStates", Arrays.asList("Success", "NotApplicable", "Failure"))).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().latestStatuses).contains(ConnectionTask.SuccessIndicator.SUCCESS).contains(ConnectionTask.SuccessIndicator.FAILURE).contains(ConnectionTask.SuccessIndicator.NOT_APPLICABLE).hasSize(3);
    }

    @Test
    public void testRunConnectionsWithFilter() throws Exception {
        mockAppServers(ConnectionTaskService.FILTER_ITEMIZER_QUEUE_DESTINATION, ConnectionTaskService.CONNECTION_RESCHEDULER_QUEUE_DESTINATION);
        when(jsonService.serialize(any())).thenReturn("json");
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(messageService.getDestinationSpec("ItemizeConnFilterQD")).thenReturn(Optional.of(destinationSpec));
        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        when(destinationSpec.message("json")).thenReturn(messageBuilder);

        ConnectionTaskFilterSpecificationMessage message = new ConnectionTaskFilterSpecificationMessage();
        message.comPortPools.add(1001L);
        message.currentStates.add("OnHold");
        message.connectionTypes.add(1002L);
        message.deviceGroups.add(1003L);
        message.latestResults.add("Broken");
        message.latestStates.add("Failure");
        message.deviceTypes.add(1004L);
        message.deviceTypes.add(1005L);
        Instant now = Instant.now();
        message.startIntervalFrom = now;
        message.startIntervalTo = now;
        message.finishIntervalFrom = now;
        message.finishIntervalTo = now;

        ConnectionsBulkRequestInfo info = new ConnectionsBulkRequestInfo();
        info.filter = message;
        Response response = target("/connections/run").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        verify(jsonService).serialize(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue() instanceof ItemizeConnectionFilterRescheduleQueueMessage);
        ItemizeConnectionFilterRescheduleQueueMessage itemizeConnectionFilterRescheduleQueueMessage = (ItemizeConnectionFilterRescheduleQueueMessage) argumentCaptor.getValue();
        assertThat(itemizeConnectionFilterRescheduleQueueMessage.connectionTaskFilterSpecification.comPortPools).containsOnly(1001L);
        assertThat(itemizeConnectionFilterRescheduleQueueMessage.connectionTaskFilterSpecification.currentStates).containsOnly(TaskStatus.OnHold.name());
        assertThat(itemizeConnectionFilterRescheduleQueueMessage.connectionTaskFilterSpecification.connectionTypes).containsOnly(1002L);
        assertThat(itemizeConnectionFilterRescheduleQueueMessage.connectionTaskFilterSpecification.deviceGroups).containsOnly(1003L);
        assertThat(itemizeConnectionFilterRescheduleQueueMessage.connectionTaskFilterSpecification.latestResults).containsOnly(ComSession.SuccessIndicator.Broken.name());
        assertThat(itemizeConnectionFilterRescheduleQueueMessage.connectionTaskFilterSpecification.latestStates).containsOnly( ConnectionTask.SuccessIndicator.FAILURE.name());
        assertThat(itemizeConnectionFilterRescheduleQueueMessage.connectionTaskFilterSpecification.deviceTypes).containsOnly(1004L, 1005L);
        assertThat(itemizeConnectionFilterRescheduleQueueMessage.connectionTaskFilterSpecification.startIntervalFrom).isEqualTo(now);
        assertThat(itemizeConnectionFilterRescheduleQueueMessage.connectionTaskFilterSpecification.startIntervalTo).isEqualTo(now);
        assertThat(itemizeConnectionFilterRescheduleQueueMessage.connectionTaskFilterSpecification.finishIntervalFrom).isEqualTo(now);
        assertThat(itemizeConnectionFilterRescheduleQueueMessage.connectionTaskFilterSpecification.finishIntervalTo).isEqualTo(now);
    }

    @Test
    public void testStartIntervalFromAddedToFilter() throws Exception {
        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("startIntervalFrom", 1407916436000L)).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionStart.getStart()).isEqualTo(Instant.ofEpochMilli(1407916436000L));
        assertThat(captor.getValue().lastSessionStart.getEnd()).isNull();
        assertThat(captor.getValue().lastSessionEnd).isNull();
    }

    @Test
    public void testStartIntervalToAddedToFilter() throws Exception {

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("startIntervalTo", 1407916436000L)).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionStart.getEnd()).isEqualTo(Instant.ofEpochMilli(1407916436000L));
        assertThat(captor.getValue().lastSessionStart.getStart()).isNull();
        assertThat(captor.getValue().lastSessionEnd).isNull();
    }

    @Test
    public void testStartAndFinishIntervalFromAndToAddedToFilter() throws Exception {

        Map<String, Object> map = target("/connections").queryParam("filter",
                ExtjsFilter.filter()
                        .property("startIntervalFrom", 1407916436000L).property("startIntervalTo", 1407916784000L)
                        .property("finishIntervalFrom", 1407916436000L).property("finishIntervalTo", 1407916784000L)
                        .create()).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionStart.getStart()).isEqualTo(Instant.ofEpochMilli(1407916436000L));
        assertThat(captor.getValue().lastSessionStart.getEnd()).isEqualTo(Instant.ofEpochMilli(1407916784000L));
        assertThat(captor.getValue().lastSessionEnd.getStart()).isEqualTo(Instant.ofEpochMilli(1407916436000L));
        assertThat(captor.getValue().lastSessionEnd.getEnd()).isEqualTo(Instant.ofEpochMilli(1407916784000L));
    }

    @Test
    public void testEndIntervalFromAddedToFilter() throws Exception {
        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("finishIntervalFrom", 1407916436000L)).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionEnd.getStart()).isEqualTo(Instant.ofEpochMilli(1407916436000L));
        assertThat(captor.getValue().lastSessionEnd.getEnd()).isNull();
        assertThat(captor.getValue().lastSessionStart).isNull();
    }

    @Test
    public void testEndIntervalToAddedToFilter() throws Exception {

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter("finishIntervalTo", 1407916436000L)).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionEnd.getEnd()).isEqualTo(Instant.ofEpochMilli(1407916436000L));
        assertThat(captor.getValue().lastSessionEnd.getStart()).isNull();
        assertThat(captor.getValue().lastSessionStart).isNull();
    }

    @Test
    public void testEndIntervalFromAndToAddedToFilter() throws Exception {

        Map<String, Object> map = target("/connections").queryParam("filter", ExtjsFilter.filter().property("finishIntervalFrom", 1407916436000L).property("finishIntervalTo", 1407916784000L).create()).queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        ArgumentCaptor<ConnectionTaskFilterSpecification> captor = ArgumentCaptor.forClass(ConnectionTaskFilterSpecification.class);
        verify(connectionTaskService).findConnectionTasksByFilter(captor.capture(), anyInt(), anyInt());
        assertThat(captor.getValue().lastSessionEnd.getStart()).isEqualTo(Instant.ofEpochMilli(1407916436000L));
        assertThat(captor.getValue().lastSessionEnd.getEnd()).isEqualTo(Instant.ofEpochMilli(1407916784000L));
        assertThat(captor.getValue().lastSessionStart).isNull();
    }

    @Test
    public void testConnectionTaskJsonBinding() throws Exception {
        Instant startDate = Instant.ofEpochMilli(1412771995988L);
        Instant endDate = startDate.plus(1, ChronoUnit.HOURS);
        LocalDateTime now = LocalDateTime.now();
        Instant plannedNext = now.plusHours(2).toInstant(ZoneOffset.UTC);

        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(deviceType);
        ComSchedule comSchedule = mockComSchedule();
        PartialScheduledConnectionTask partialConnectionTask = mockPartialScheduledConnectionTask();
        ComServer comServer = mockComServer();
        ComPort comPort = mockComPort(comServer);
        OutboundComPortPool comPortPool = mockComPortPool();
        Device device = mockDevice(deviceType, deviceConfiguration);
        ComWindow window = mockWindow(PartialTime.fromHours(9), PartialTime.fromHours(17));

        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTaskService.findConnectionTasksByFilter(Matchers.<ConnectionTaskFilterSpecification>anyObject(), anyInt(), anyInt())).thenReturn(Arrays.<ConnectionTask>asList(connectionTask));
        ComSession comSession = mockComSession(startDate, endDate);
        when(connectionTask.getId()).thenReturn(1234L);
        when(connectionTask.getName()).thenReturn("fancy name");
        when(connectionTask.getPartialConnectionTask()).thenReturn(partialConnectionTask);
        when(connectionTask.isDefault()).thenReturn(true);
        when(connectionTask.getDevice()).thenReturn(device);
        ScheduledComTaskExecution comTaskExecution1 = mockScheduledComTaskExecution(comSchedule, connectionTask, device);
        when(device.getComTaskExecutions()).thenReturn(Arrays.<ComTaskExecution>asList(comTaskExecution1));
        when(connectionTask.getStatus()).thenReturn(ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        when(connectionTask.getSuccessIndicator()).thenReturn(ConnectionTask.SuccessIndicator.SUCCESS);
        when(connectionTask.getTaskStatus()).thenReturn(TaskStatus.OnHold);
        when(connectionTask.getCurrentRetryCount()).thenReturn(7);
        when(connectionTask.getExecutingComServer()).thenReturn(comServer);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionType.getDirection()).thenReturn(ConnectionType.Direction.OUTBOUND);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        when(comSession.getComPort()).thenReturn(comPort);
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        when(connectionTask.getLastComSession()).thenReturn(Optional.of(comSession));
        when(connectionTask.getNextExecutionTimestamp()).thenReturn(plannedNext);
        when(connectionTask.getCommunicationWindow()).thenReturn(window);
        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
        when(connectionTypePluggableClass.getName()).thenReturn(CONNECTION_TYPE_PLUGGABLE_CLASS_NAME);
        when(connectionTask.getPluggableClass()).thenReturn(connectionTypePluggableClass);
        ComTaskExecutionSession comTaskExecutionSession = mock(ComTaskExecutionSession.class);
        when(comTaskExecutionSession.getHighestPriorityCompletionCode()).thenReturn(CompletionCode.Ok);
        when(communicationTaskService.findLastSessionFor(comTaskExecution1)).thenReturn(Optional.of(comTaskExecutionSession));
        String response = target("/connections").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);

        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.connectionTasks")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].id")).isEqualTo(1234);
        assertThat(jsonModel.<String>get("$.connectionTasks[0].device.id")).isEqualTo("1234-5678-9012");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].device.name")).isEqualTo("some device");
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].deviceType.id")).isEqualTo(1010);
        assertThat(jsonModel.<String>get("$.connectionTasks[0].deviceType.name")).isEqualTo("device type");
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].deviceConfiguration.id")).isEqualTo(123123);
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].deviceConfiguration.deviceTypeId")).isEqualTo(1010);
        assertThat(jsonModel.<String>get("$.connectionTasks[0].deviceConfiguration.name")).isEqualTo("123123");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].currentState.id")).isEqualTo("OnHold");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].currentState.displayValue")).isEqualTo("Inactive");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].latestResult.id")).isEqualTo("Success");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].latestResult.displayValue")).isEqualTo("Success");
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].taskCount.numberOfSuccessfulTasks")).isEqualTo(12);
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].taskCount.numberOfFailedTasks")).isEqualTo(401);
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].taskCount.numberOfIncompleteTasks")).isEqualTo(3);
        assertThat(jsonModel.<Long>get("$.connectionTasks[0].startDateTime")).isEqualTo(1412771995000L);
        assertThat(jsonModel.<Long>get("$.connectionTasks[0].endDateTime")).isEqualTo(endDate.with(ChronoField.MILLI_OF_SECOND, 0).toEpochMilli());
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].duration.count")).isEqualTo(3600);
        assertThat(jsonModel.<String>get("$.connectionTasks[0].duration.timeUnit")).isEqualTo("seconds");
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].comServer.id")).isEqualTo(1212);
        assertThat(jsonModel.<String>get("$.connectionTasks[0].comServer.name")).isEqualTo("com server");
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].comPort.id")).isEqualTo(99);
        assertThat(jsonModel.<String>get("$.connectionTasks[0].comPort.name")).isEqualTo("com port");
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].comPortPool.id")).isEqualTo(1234321);
        assertThat(jsonModel.<String>get("$.connectionTasks[0].comPortPool.name")).isEqualTo("Com port pool");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].direction")).isEqualTo("Outbound");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].connectionType")).isEqualTo(CONNECTION_TYPE_PLUGGABLE_CLASS_NAME);
        assertThat(jsonModel.<Integer>get("$.connectionTasks[0].connectionMethod.id")).isEqualTo(991);
        assertThat(jsonModel.<String>get("$.connectionTasks[0].connectionMethod.name")).isEqualTo("partial connection task name (default)");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].connectionStrategy.id")).isEqualTo("asSoonAsPossible");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].connectionStrategy.displayValue")).isEqualTo("As soon as possible");
        assertThat(jsonModel.<String>get("$.connectionTasks[0].window")).isEqualTo("09:00 - 17:00");
        assertThat(jsonModel.<Long>get("$.connectionTasks[0].nextExecution")).isEqualTo(plannedNext.toEpochMilli());
    }

    @Test
    public void testGetConnectionAttributesIfNoConnectionTypesMatchFilter() throws Exception {
        when(connectionTaskService.findConnectionTypeByFilter(any())).thenReturn(Collections.emptyList());

        Response response = target("/connections/properties").queryParam("filter",
                ExtjsFilter.filter()
                        .property("startIntervalFrom", 1407916436000L).property("startIntervalTo", 1407916784000L)
                        .property("finishIntervalFrom", 1407916436000L).property("finishIntervalTo", 1407916784000L)
                        .create()).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetConnectionAttributesIfMultipleConnectionTypesMatchFilter() throws Exception {
        ConnectionTypePluggableClass connectionTypePluggableClass = mockConnectionType();
        ConnectionTypePluggableClass connectionTypePluggableClass2 = mockConnectionType();
        when(connectionTaskService.findConnectionTypeByFilter(any())).thenReturn(Arrays.asList(connectionTypePluggableClass, connectionTypePluggableClass2));

        Response response = target("/connections/properties").queryParam("filter",
                ExtjsFilter.filter()
                        .property("startIntervalFrom", 1407916436000L).property("startIntervalTo", 1407916784000L)
                        .property("finishIntervalFrom", 1407916436000L).property("finishIntervalTo", 1407916784000L)
                        .create()).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetConnectionAttributesWithFilter() throws Exception {
        ConnectionTypePluggableClass connectionTypePluggableClass = mockConnectionType();

        when(connectionTaskService.findConnectionTypeByFilter(any())).thenReturn(Arrays.asList(connectionTypePluggableClass));

        Response response = target("/connections/properties").queryParam("filter",
                ExtjsFilter.filter()
                        .property("startIntervalFrom", 1407916436000L).property("startIntervalTo", 1407916784000L)
                        .property("finishIntervalFrom", 1407916436000L).property("finishIntervalTo", 1407916784000L)
                        .create()).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetConnectionAttributesWithListMultipleConnectionTypes() throws Exception {
        Optional<ConnectionTask> connectionTask1 = mockConnectionTask("someClass");
        Optional<ConnectionTask> connectionTask2 = mockConnectionTask("someClassBis");
        Optional<ConnectionTask> connectionTask3 = mockConnectionTask("someClassTris");
        when(connectionTaskService.findConnectionTask(1L)).thenReturn(connectionTask1);
        when(connectionTaskService.findConnectionTask(2L)).thenReturn(connectionTask2);
        when(connectionTaskService.findConnectionTask(3L)).thenReturn(connectionTask3);
        ConnectionTypePluggableClass pluggableClass = mockPluggableClass("someClass");
        ConnectionTypePluggableClass pluggableClass1 = mockPluggableClass("someClassBis");
        ConnectionTypePluggableClass pluggableClass2 = mockPluggableClass("someClassTris");
        ConnectionTypePluggableClass pluggableClass3 = mockPluggableClass("someClass4");
        when(protocolPluggableService.findAllConnectionTypePluggableClasses()).thenReturn(Arrays.asList(pluggableClass, pluggableClass1, pluggableClass3, pluggableClass2));
        Response response = target("/connections/properties").queryParam("connections", "[1,2,3]").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetConnectionAttributesWithList() throws Exception {
        Optional<ConnectionTask> connectionTask1 = mockConnectionTask("someClass");
        Optional<ConnectionTask> connectionTask2 = mockConnectionTask("someClass");
        Optional<ConnectionTask> connectionTask3 = mockConnectionTask("someClass");
        when(connectionTaskService.findConnectionTask(1L)).thenReturn(connectionTask1);
        when(connectionTaskService.findConnectionTask(2L)).thenReturn(connectionTask2);
        when(connectionTaskService.findConnectionTask(3L)).thenReturn(connectionTask3);
        ConnectionTypePluggableClass pluggableClass = mockPluggableClass("someClass");
        ConnectionTypePluggableClass pluggableClass1 = mockPluggableClass("someClassBis");
        ConnectionTypePluggableClass pluggableClass2 = mockPluggableClass("someClassTris");
        ConnectionTypePluggableClass pluggableClass3 = mockPluggableClass("someClass4");
        when(protocolPluggableService.findAllConnectionTypePluggableClasses()).thenReturn(Arrays.asList(pluggableClass, pluggableClass1, pluggableClass3, pluggableClass2));
        Response response = target("/connections/properties").queryParam("connections", "[1,2,3]").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testUpdateConnectionAttributesWithFilter() throws Exception {
        mockAppServers(ConnectionTaskService.FILTER_ITEMIZER_PROPERTIES_QUEUE_DESTINATION, ConnectionTaskService.CONNECTION_PROP_UPDATER_QUEUE_DESTINATION);
        when(jsonService.serialize(any())).thenReturn("json");
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(messageService.getDestinationSpec("ItemizeConnPropFilterQD")).thenReturn(Optional.of(destinationSpec));
        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        when(destinationSpec.message("json")).thenReturn(messageBuilder);

        Optional<ConnectionTask> connectionTask1 = mockConnectionTask("someClass");
        when(connectionTaskService.findConnectionTask(1L)).thenReturn(connectionTask1);
        ConnectionTypePluggableClass pluggableClass = mockPluggableClass("someClass");
//        when(protocolPluggableService.findAllConnectionTypePluggableClasses()).thenReturn(Arrays.asList(pluggableClass));
        when(connectionTaskService.findConnectionTypeByFilter(any())).thenReturn(Arrays.asList(pluggableClass));

        PropertiesBulkRequestInfo info = new PropertiesBulkRequestInfo();
        info.properties= new ArrayList<>();
        info.properties.add(new PropertyInfo("hostName", "hostName", new PropertyValueInfo<>("google.com", null), null, false));
        info.filter = new ConnectionTaskFilterSpecificationMessage();
        info.filter.currentStates.add("Busy");

        Response response = target("/connections/properties").
                request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
    }

    private ConnectionTypePluggableClass mockPluggableClass(String name) {
        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        PropertySpec hostName = mock(PropertySpec.class);
        when(hostName.getName()).thenReturn("hostName");
        when(hostName.getValueFactory()).thenReturn(new StringFactory());
        when(pluggableClass.getPropertySpecs()).thenReturn(Arrays.asList(hostName));
        when(pluggableClass.getJavaClassName()).thenReturn(name);
        TypedProperties properties = TypedProperties.empty();
        when(pluggableClass.getProperties(any())).thenReturn(properties);
        return pluggableClass;
    }

    private Optional<ConnectionTask> mockConnectionTask(String javaClassName) {
        ConnectionTask mock = mock(ConnectionTask.class);
        PluggableClass pluggableClass = mockConnectionType();
        when(mock.getPluggableClass()).thenReturn(pluggableClass);
        when(pluggableClass.getJavaClassName()).thenReturn(javaClassName);
        return Optional.of(mock);
    }

    private ConnectionTypePluggableClass mockConnectionType() {
        ConnectionTypePluggableClass mock = mock(ConnectionTypePluggableClass.class);
        BasicPropertySpec name = new BasicPropertySpec("name", new StringFactory());
        BasicPropertySpec id = new BasicPropertySpec("id", new StringFactory());

        when(mock.getPropertySpecs()).thenReturn(Arrays.asList(id, name));
        TypedProperties inheritedProperties = TypedProperties.empty();
        inheritedProperties.setProperty("name", "MyName");
        inheritedProperties.setProperty("id", "HXG7-OPW1");
        TypedProperties typedProperties = TypedProperties.inheritingFrom(inheritedProperties);
        when(mock.getProperties(any())).thenReturn(typedProperties);
        return mock;
    }

    private OutboundComPortPool mockComPortPool() {
        OutboundComPortPool comPortPool = mock(OutboundComPortPool.class);
        when(comPortPool.getName()).thenReturn("Com port pool");
        when(comPortPool.getId()).thenReturn(1234321L);
        return comPortPool;
    }

    private ComWindow mockWindow(PartialTime start, PartialTime end) {
        ComWindow window = mock(ComWindow.class);
        when(window.getStart()).thenReturn(start);
        when(window.getEnd()).thenReturn(end);
        return window;
    }

    private ComServer mockComServer() {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(1212L);
        when(comServer.getName()).thenReturn("com server");
        return comServer;
    }

    private ComPort mockComPort(ComServer comServer) {
        ComPort comPort = mock(ComPort.class);
        when(comPort.getName()).thenReturn("com port");
        when(comPort.getId()).thenReturn(99L);
        when(comPort.getComServer()).thenReturn(comServer);
        return comPort;
    }

    private PartialScheduledConnectionTask mockPartialScheduledConnectionTask() {
        PartialScheduledConnectionTask partialConnectionTask = mock(PartialScheduledConnectionTask.class);
        when(partialConnectionTask.getName()).thenReturn("partial connection task name");
        when(partialConnectionTask.getId()).thenReturn(991L);
        return partialConnectionTask;
    }

    private Device mockDevice(DeviceType deviceType, DeviceConfiguration deviceConfiguration) {
        Device device = mock(Device.class);
        when(device.getmRID()).thenReturn("1234-5678-9012");
        when(device.getName()).thenReturn("some device");
        when(device.getDeviceType()).thenReturn(deviceType);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        return device;
    }

    private ScheduledComTaskExecution mockScheduledComTaskExecution(ComSchedule comSchedule, ConnectionTask connectionTask, Device device) {
        ScheduledComTaskExecution comTaskExecution1 = mock(ScheduledComTaskExecution.class);
        when(comTaskExecution1.getConnectionTask()).thenReturn(connectionTask);
        when(comTaskExecution1.getCurrentTryCount()).thenReturn(999);
        when(comTaskExecution1.getDevice()).thenReturn(device);
        when(comTaskExecution1.getStatus()).thenReturn(TaskStatus.NeverCompleted);
        when(comTaskExecution1.getComSchedule()).thenReturn(comSchedule);
        when(comTaskExecution1.getExecutionPriority()).thenReturn(100);
        when(comTaskExecution1.getLastExecutionStartTimestamp()).thenReturn(Instant.now());
        when(comTaskExecution1.getLastSuccessfulCompletionTimestamp()).thenReturn(Instant.now());
        when(comTaskExecution1.getNextExecutionTimestamp()).thenReturn(Instant.now());
        return comTaskExecution1;
    }

    private DeviceConfiguration mockDeviceConfiguration(DeviceType deviceType) {
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(123123L);
        when(deviceConfiguration.getName()).thenReturn("123123");
        when(deviceConfiguration.getDeviceType()).thenReturn(deviceType);
        return deviceConfiguration;
    }

    private ComSession mockComSession(Instant startDate, Instant endDate) {
        ComSession comSession = mock(ComSession.class);
        when(comSession.getNumberOfFailedTasks()).thenReturn(401);
        when(comSession.getNumberOfSuccessFulTasks()).thenReturn(12);
        when(comSession.getNumberOfPlannedButNotExecutedTasks()).thenReturn(3);
        when(comSession.getSuccessIndicator()).thenReturn(ComSession.SuccessIndicator.Success);
        when(comSession.getStartDate()).thenReturn(startDate);
        when(comSession.getStopDate()).thenReturn(endDate);
        return comSession;
    }

    @Test

    public void testCommunicationTaskJsonBinding() throws Exception {
        long connectionTaskId = 30L;

        Instant startDate = Instant.ofEpochMilli(1412771995988L);
        Instant endDate = startDate.plus(1, ChronoUnit.HOURS);
        Instant lastExecStart = startDate.plus(2, ChronoUnit.HOURS);
        Instant lastSuccess = startDate.plus(3, ChronoUnit.HOURS);
        LocalDateTime now = LocalDateTime.now();
        Instant plannedNext = now.plusHours(2).toInstant(ZoneOffset.UTC);

        ComSession comSession = mockComSession(startDate, endDate);
        DeviceType deviceType = mockDeviceType();
        DeviceConfiguration deviceConfiguration = mockDeviceConfiguration(deviceType);
        Device device = mockDevice(deviceType, deviceConfiguration);

        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getId()).thenReturn(connectionTaskId);
        when(connectionTask.getLastComSession()).thenReturn(Optional.of(comSession));
        ComTask comTask1 = mock(ComTask.class);
        when(comTask1.getId()).thenReturn(1111L);
        when(comTask1.getName()).thenReturn("Read all");
        ComTask comTask2 = mock(ComTask.class);
        when(comTask2.getName()).thenReturn("Basic check");
        ComSchedule comSchedule = mockComSchedule();
        ScheduledComTaskExecution comTaskExecution1 = mockScheduledComTaskExecution(lastExecStart, lastSuccess, plannedNext, connectionTask, comSchedule, Arrays.asList(comTask1, comTask2));

        ComTaskExecutionSession comTaskExecutionSession1 = mock(ComTaskExecutionSession.class);
        when(comTaskExecutionSession1.getComTaskExecution()).thenReturn(comTaskExecution1);
        when(comTaskExecutionSession1.getDevice()).thenReturn(device);
        when(comTaskExecutionSession1.getHighestPriorityCompletionCode()).thenReturn(CompletionCode.Ok);
        when(comSession.getComTaskExecutionSessions()).thenReturn(Arrays.asList(comTaskExecutionSession1));
        when(comTaskExecutionSession1.getComTask()).thenReturn(comTask1);
        when(comTaskExecutionSession1.getStartDate()).thenReturn(lastExecStart);
        when(comTaskExecutionSession1.getStopDate()).thenReturn(endDate);
        when(connectionTaskService.findConnectionTask(30L)).thenReturn(Optional.<ConnectionTask>of(connectionTask));
        ComTaskExecutionSession comTaskExecutionSession = mock(ComTaskExecutionSession.class);
        when(comTaskExecutionSession.getHighestPriorityCompletionCode()).thenReturn(CompletionCode.Ok);
        String response = target("/connections/" + connectionTaskId + "/latestcommunications").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);

        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.communications")).hasSize(1);
        assertThat(jsonModel.<String>get("$.communications[0].name")).isEqualTo("Read all + Basic check");
        assertThat(jsonModel.<String>get("$.communications[0].device.id")).isEqualTo("1234-5678-9012");
        assertThat(jsonModel.<String>get("$.communications[0].device.name")).isEqualTo("some device");
        assertThat(jsonModel.<Integer>get("$.communications[0].deviceType.id")).isEqualTo(1010);
        assertThat(jsonModel.<String>get("$.communications[0].deviceType.name")).isEqualTo("device type");
        assertThat(jsonModel.<Integer>get("$.communications[0].deviceConfiguration.id")).isEqualTo(123123);
        assertThat(jsonModel.<Integer>get("$.communications[0].deviceConfiguration.deviceTypeId")).isEqualTo(1010);
        assertThat(jsonModel.<String>get("$.communications[0].deviceConfiguration.name")).isEqualTo("123123");
        assertThat(jsonModel.<String>get("$.communications[0].currentState.id")).isEqualTo("NeverCompleted");
        assertThat(jsonModel.<String>get("$.communications[0].currentState.displayValue")).isEqualTo("Never completed");
        assertThat(jsonModel.<String>get("$.communications[0].result.id")).isEqualTo("OK");
        assertThat(jsonModel.<String>get("$.communications[0].result.displayValue")).isEqualTo("Ok");
        assertThat(jsonModel.<Long>get("$.communications[0].startTime")).isEqualTo(lastExecStart.toEpochMilli());
        assertThat(jsonModel.<Integer>get("$.communications[0].comTask.id")).isEqualTo(1111);
        assertThat(jsonModel.<String>get("$.communications[0].comTask.name")).isEqualTo("Read all");
        assertThat(jsonModel.<String>get("$.communications[0].comScheduleName")).isEqualTo("Weekly billing");
        assertThat(jsonModel.<Integer>get("$.communications[0].comScheduleFrequency.every.count")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.communications[0].comScheduleFrequency.every.timeUnit")).isEqualTo("weeks");
        assertThat(jsonModel.<Integer>get("$.communications[0].comScheduleFrequency.offset.count")).isEqualTo(43200);
        assertThat(jsonModel.<String>get("$.communications[0].comScheduleFrequency.offset.timeUnit")).isEqualTo("seconds");
        assertThat(jsonModel.<Integer>get("$.communications[0].urgency")).isEqualTo(100);
        assertThat(jsonModel.<Long>get("$.communications[0].stopTime")).isEqualTo(endDate.toEpochMilli());
        assertThat(jsonModel.<Long>get("$.communications[0].nextCommunication")).isEqualTo(plannedNext.toEpochMilli());
        assertThat(jsonModel.<Boolean>get("$.communications[0].alwaysExecuteOnInbound")).isEqualTo(false);
        assertThat(jsonModel.<Object>get("$.communications[0].connectionTask")).isNull();
    }

    private ScheduledComTaskExecution mockScheduledComTaskExecution(Instant lastExecStart, Instant lastSuccess, Instant plannedNext, ConnectionTask connectionTask, ComSchedule comSchedule, List<ComTask> comTasks) {
        ScheduledComTaskExecution comTaskExecution1 = mock(ScheduledComTaskExecution.class);
        when(comTaskExecution1.getComTasks()).thenReturn(comTasks);
        when(comTaskExecution1.getConnectionTask()).thenReturn(connectionTask);
        when(comTaskExecution1.getCurrentTryCount()).thenReturn(999);
        when(comTaskExecution1.getStatus()).thenReturn(TaskStatus.NeverCompleted);
        when(comTaskExecution1.getComSchedule()).thenReturn(comSchedule);
        when(comTaskExecution1.getExecutionPriority()).thenReturn(100);
        when(comTaskExecution1.getLastExecutionStartTimestamp()).thenReturn(lastExecStart);
        when(comTaskExecution1.getLastSuccessfulCompletionTimestamp()).thenReturn(lastSuccess);
        when(comTaskExecution1.getNextExecutionTimestamp()).thenReturn(plannedNext);
        return comTaskExecution1;
    }

    private DeviceType mockDeviceType() {
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getId()).thenReturn(1010L);
        when(deviceType.getName()).thenReturn("device type");
        return deviceType;
    }

    private ComSchedule mockComSchedule() {
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(comSchedule.getName()).thenReturn("Weekly billing");
        when(comSchedule.getTemporalExpression()).thenReturn(new TemporalExpression(new TimeDuration(1, TimeDuration.TimeUnit.WEEKS), new TimeDuration(12, TimeDuration.TimeUnit.HOURS)));
        return comSchedule;
    }


}
