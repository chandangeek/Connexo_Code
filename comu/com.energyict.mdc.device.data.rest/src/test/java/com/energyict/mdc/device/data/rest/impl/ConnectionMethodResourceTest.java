package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask.ConnectionTaskLifecycleStatus;
import com.energyict.mdc.device.data.tasks.ConnectionTask.SuccessIndicator;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.pluggable.rest.impl.properties.SimplePropertyType;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.ConnectionType.Direction;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConnectionMethodResourceTest extends DeviceDataRestApplicationJerseyTest {

    Instant comSessionStart = Instant.now();
    Instant comSessionEnd = comSessionStart.plus(Duration.ofMinutes(1));
    Instant nextExecution = Instant.now();
    private ScheduledConnectionTask connectionTask;
    private Device device;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        device = mock(Device.class);
        when(deviceService.findByUniqueMrid("ZABF0000000")).thenReturn(Optional.of(device));
        connectionTask = mockConnectionTask(9);
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        PartialConnectionTask partialConnectionTask = mockPartialConnectionTask(31L, "AS1440");
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Arrays.asList(partialConnectionTask));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
    }

    @Test
    public void testGetAllConnections() {
        String response = target("/devices/ZABF0000000/connectionmethods").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>> get("$.connectionMethods")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.connectionMethods[0].id")).isEqualTo(9);
        assertThat(jsonModel.<Integer>get("$.connectionMethods[0].comWindowStart")).isEqualTo(60);
        assertThat(jsonModel.<Integer>get("$.connectionMethods[0].comWindowEnd")).isEqualTo(120);
        assertThat(jsonModel.<Boolean>get("$.connectionMethods[0].isDefault")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.connectionMethods[0].allowSimultaneousConnections")).isEqualTo(false);
        assertThat(jsonModel.<String>get("$.connectionMethods[0].direction")).isEqualTo("Outbound");
        assertThat(jsonModel.<String>get("$.connectionMethods[0].name")).isEqualTo("it's me");
        assertThat(jsonModel.<String>get("$.connectionMethods[0].status")).isEqualTo("connectionTaskStatusActive");
        assertThat(jsonModel.<String>get("$.connectionMethods[0].connectionType")).isEqualTo("Pluggable class");
        assertThat(jsonModel.<String>get("$.connectionMethods[0].connectionStrategy")).isEqualTo(ConnectionStrategy.AS_SOON_AS_POSSIBLE.name());
        assertThat(jsonModel.<String>get("$.connectionMethods[0].comPortPool")).isEqualTo("com port pool");
        assertThat(jsonModel.<List>get("$.connectionMethods[0].properties")).isEmpty();
    }

    @Test
    public void testUpdateScheduledConnectionMethodWithNewSchedule() {
        ScheduledConnectionMethodInfo info = new ScheduledConnectionMethodInfo();
        info.name = "AS1440";
        info.status = ConnectionTaskLifecycleStatus.INCOMPLETE;
        info.nextExecutionSpecs = new TemporalExpressionInfo();
        info.nextExecutionSpecs.every= new TimeDurationInfo();
        info.nextExecutionSpecs.every.count= 15;
        info.nextExecutionSpecs.every.timeUnit= "minutes";

        Response response = target("/devices/ZABF0000000/connectionmethods/9").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<TemporalExpression> captor = ArgumentCaptor.forClass(TemporalExpression.class);
        verify(connectionTask).setNextExecutionSpecsFrom(captor.capture());
        assertThat(captor.getValue().getEvery()).isEqualTo(new TimeDuration("15 minutes"));
        assertThat(captor.getValue().getOffset()).isEqualTo(new TimeDuration(0));
    }

    @Test
    public void testUpdateScheduledConnectionMethodWithIllegalSchedule() throws IOException {
        ScheduledConnectionMethodInfo info = new ScheduledConnectionMethodInfo();
        info.name = "AS1440";
        info.status = ConnectionTaskLifecycleStatus.INCOMPLETE;
        info.nextExecutionSpecs = new TemporalExpressionInfo();
        info.nextExecutionSpecs.every= new TimeDurationInfo();
        info.nextExecutionSpecs.every.count= 15;
        info.nextExecutionSpecs.every.timeUnit= ""; // ILLEGAL

        Response response = target("/devices/ZABF0000000/connectionmethods/9").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("nextExecutionSpecs.every.timeUnit");
    }

    @Test
    public void testUpdateScheduledConnectionMethodWithIllegalScheduleOffset() throws IOException {
        ScheduledConnectionMethodInfo info = new ScheduledConnectionMethodInfo();
        info.name = "AS1440";
        info.status = ConnectionTaskLifecycleStatus.INCOMPLETE;
        info.nextExecutionSpecs = new TemporalExpressionInfo();
        info.nextExecutionSpecs.every= new TimeDurationInfo();
        info.nextExecutionSpecs.every.count= 15;
        info.nextExecutionSpecs.every.timeUnit= "minutes";
        info.nextExecutionSpecs.offset= new TimeDurationInfo();
        info.nextExecutionSpecs.offset.count= 13;
        info.nextExecutionSpecs.offset.timeUnit= "illegal"; // ILLEGAL

        Response response = target("/devices/ZABF0000000/connectionmethods/9").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("nextExecutionSpecs.offset.timeUnit");
    }

    @Test
    public void testUpdateScheduledConnectionMethodWithIllegalProperty() throws IOException {
        ScheduledConnectionMethodInfo info = new ScheduledConnectionMethodInfo();
        info.name = "AS1440";
        info.status = ConnectionTaskLifecycleStatus.INCOMPLETE;
        info.properties = new ArrayList<>();
        PropertyInfo propertyInfo = new PropertyInfo("connectionTimeout","connectionTimeout",new PropertyValueInfo<>(new TimeDuration("15 seconds"),null,null,null),new PropertyTypeInfo(SimplePropertyType.TIMEDURATION,null,null,null),false);
        info.properties.add(propertyInfo);

        Response response = target("/devices/ZABF0000000/connectionmethods/9").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.errors[0].id")).contains("properties.connectionTimeout");
    }

    @Test
    public void testCreateScheduledConnectionMethodWithEmptyUserProperty() {
        ScheduledConnectionMethodInfo info = new ScheduledConnectionMethodInfo();
        info.name = "AS1440";
        info.status = ConnectionTaskLifecycleStatus.INCOMPLETE;
        PropertyInfo propertyInfo = new PropertyInfo("connectionTimeout", "connectionTimeout",
                new PropertyValueInfo("", "", null, null),
                new PropertyTypeInfo(SimplePropertyType.TIMEDURATION, null, null, null),
                false);
        info.properties = Arrays.asList(propertyInfo);
        Device.ScheduledConnectionTaskBuilder connectionTaskBuilder = mock(Device.ScheduledConnectionTaskBuilder.class);
        when(device.getScheduledConnectionTaskBuilder(Matchers.any())).thenReturn(connectionTaskBuilder);
        when(connectionTaskBuilder.add()).thenReturn(connectionTask);

        Response response = target("/devices/ZABF0000000/connectionmethods").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(connectionTaskBuilder).setProperty("connectionTimeout", null);
    }

    @Test
    public void testCreateScheduledConnectionMethodWithInheritedProperty() {
        ScheduledConnectionMethodInfo info = new ScheduledConnectionMethodInfo();
        info.name = "AS1440";
        info.status = ConnectionTaskLifecycleStatus.INCOMPLETE;
        PropertyInfo propertyInfo = new PropertyInfo("connectionTimeout", "connectionTimeout",
                new PropertyValueInfo(null, new TimeDurationInfo(new TimeDuration("15 minutes")), null, null),
                new PropertyTypeInfo(SimplePropertyType.TIMEDURATION, null, null, null),
                false);
        info.properties = Arrays.asList(propertyInfo);
        Device.ScheduledConnectionTaskBuilder connectionTaskBuilder = mock(Device.ScheduledConnectionTaskBuilder.class);
        when(device.getScheduledConnectionTaskBuilder(Matchers.any())).thenReturn(connectionTaskBuilder);
        when(connectionTaskBuilder.add()).thenReturn(connectionTask);

        Response response = target("/devices/ZABF0000000/connectionmethods").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(connectionTaskBuilder, never()).setProperty("connectionTimeout", null);
    }

    @Test
    public void testUpdateScheduledConnectionMethodWithEmptyProperty() throws IOException {
        ScheduledConnectionMethodInfo info = new ScheduledConnectionMethodInfo();
        info.name = "AS1440";
        info.status = ConnectionTaskLifecycleStatus.INCOMPLETE;
        PropertyInfo propertyInfo = new PropertyInfo("connectionTimeout", "connectionTimeout",
                new PropertyValueInfo<>(null, null, null, null),
                new PropertyTypeInfo(SimplePropertyType.TIMEDURATION, null, null, null),
                false);
        info.properties = Arrays.asList(propertyInfo);

        Response response = target("/devices/ZABF0000000/connectionmethods/9").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(connectionTask).setProperty("connectionTimeout", null);
    }

    @Test
    public void testUpdateScheduledConnectionMethodWithInheritedProperty() throws IOException {
        ScheduledConnectionMethodInfo info = new ScheduledConnectionMethodInfo();
        info.name = "AS1440";
        info.status = ConnectionTaskLifecycleStatus.INCOMPLETE;
        PropertyInfo propertyInfo = new PropertyInfo("connectionTimeout", "connectionTimeout",
                new PropertyValueInfo<>(null, new TimeDurationInfo(new TimeDuration("15 minutes")), null, null),
                new PropertyTypeInfo(SimplePropertyType.TIMEDURATION, null, null, null),
                false);
        info.properties = Arrays.asList(propertyInfo);

        Response response = target("/devices/ZABF0000000/connectionmethods/9").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(connectionTask).removeProperty("connectionTimeout");
    }

    private ScheduledConnectionTask mockConnectionTask(long id) {
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        ComSession comSession = mockComSession(comSessionStart, comSessionEnd);
        when(connectionTask.getLastComSession()).thenReturn(Optional.of(comSession));
        when(connectionTask.getId()).thenReturn(id);
        when(connectionTask.getName()).thenReturn("it's me");
        when(connectionTask.getSuccessIndicator()).thenReturn(SuccessIndicator.FAILURE);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionType.getDirection()).thenReturn(Direction.OUTBOUND);
        ConnectionTypePluggableClass pluggableClass = mockPluggableClass();
        when(connectionTask.getPluggableClass()).thenReturn(pluggableClass);
        doReturn(mockPartialScheduledConnectionTask()).when(connectionTask).getPartialConnectionTask();
        when(connectionTask.getStatus()).thenReturn(ConnectionTaskLifecycleStatus.ACTIVE);
        when(connectionTask.isDefault()).thenReturn(true);
        when(connectionTask.getTaskStatus()).thenReturn(TaskStatus.Pending);
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        ComWindow window = mockWindow(PartialTime.fromMinutes(1), PartialTime.fromMinutes(2));
        when(connectionTask.getCommunicationWindow()).thenReturn(window);
        when(connectionTask.getNextExecutionTimestamp()).thenReturn(nextExecution);
        OutboundComPortPool comPortPool = mockComPortPool();
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        return connectionTask;
    }

    private <T> PartialScheduledConnectionTask mockPartialConnectionTask(long id, String name) {
        PartialScheduledConnectionTask connectionTask = mock(PartialScheduledConnectionTask.class);
        when(connectionTask.getId()).thenReturn(id);
        when(connectionTask.getName()).thenReturn(name);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionType.getDirection()).thenReturn(Direction.OUTBOUND);
        ConnectionTypePluggableClass pluggableClass = mockPluggableClass();
        BasicPropertySpec propertySpec = new BasicPropertySpec("connectionTimeout", new TimeDurationValueFactory());
        when(pluggableClass.getPropertySpecs()).thenReturn(Arrays.asList(propertySpec));
        when(connectionTask.getPluggableClass()).thenReturn(pluggableClass);
        when(connectionTask.isDefault()).thenReturn(true);
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        ComWindow window = mockWindow(PartialTime.fromMinutes(1), PartialTime.fromMinutes(2));
        when(connectionTask.getCommunicationWindow()).thenReturn(window);
        OutboundComPortPool comPortPool = mockComPortPool();
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        return connectionTask;
    }

    private ComSession mockComSession(Instant startDate, Instant endDate) {
        ComSession comSession = mock(ComSession.class);
        when(comSession.getId()).thenReturn(1L);
        when(comSession.getNumberOfFailedTasks()).thenReturn(401);
        when(comSession.getNumberOfSuccessFulTasks()).thenReturn(12);
        when(comSession.getNumberOfPlannedButNotExecutedTasks()).thenReturn(3);
        when(comSession.getSuccessIndicator()).thenReturn(ComSession.SuccessIndicator.Success);
        when(comSession.getStartDate()).thenReturn(startDate);
        when(comSession.getStopDate()).thenReturn(endDate);
        ComPort comPort = mock(ComPort.class);
        when(comSession.getComPort()).thenReturn(comPort);
        when(comPort.getId()).thenReturn(1L);
        when(comPort.getName()).thenReturn("com port");
        ComServer comServer = mock(ComServer.class);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comServer.getId()).thenReturn(1L);
        when(comServer.getName()).thenReturn("com server");
        return comSession;
    }

    private PartialScheduledConnectionTask mockPartialScheduledConnectionTask() {
        PartialScheduledConnectionTask partialConnectionTask = mock(PartialScheduledConnectionTask.class);
        when(partialConnectionTask.getName()).thenReturn("partial connection task name");
        when(partialConnectionTask.getId()).thenReturn(991L);
        doReturn(mockPluggableClass()).when(partialConnectionTask).getPluggableClass();
        return partialConnectionTask;
    }

    private ComWindow mockWindow(PartialTime start, PartialTime end) {
        ComWindow window = mock(ComWindow.class);
        when(window.getStart()).thenReturn(start);
        when(window.getEnd()).thenReturn(end);
        return window;
    }

    private OutboundComPortPool mockComPortPool() {
        OutboundComPortPool portPool = mock(OutboundComPortPool.class);
        when(portPool.getId()).thenReturn(1L);
        when(portPool.getName()).thenReturn("com port pool");
        return portPool;
    }

    private ConnectionTypePluggableClass mockPluggableClass() {
        ConnectionTypePluggableClass pluggableClass = mock(ConnectionTypePluggableClass.class);
        when(pluggableClass.getName()).thenReturn("Pluggable class");
        return pluggableClass;
    }
}
