/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.TemporalAmountValueFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.ConnectionTask.ConnectionTaskLifecycleStatus;
import com.energyict.mdc.common.tasks.ConnectionTask.SuccessIndicator;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.upl.TypedProperties;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
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
    private DeviceConfiguration deviceConfiguration;
    private PartialScheduledConnectionTask partialConnectionTask;
    private ConnectionFunction connectionFunction;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        connectionFunction = mockConnectionFunction(1, "CF_1", "CF 1");
        device = mock(Device.class);
        when(device.getName()).thenReturn("ZABF0000000");
        when(device.getVersion()).thenReturn(1L);
        when(deviceService.findDeviceByName(device.getName())).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion(device.getName(), device.getVersion())).thenReturn(Optional.of(device));
        deviceConfiguration = mock(DeviceConfiguration.class);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        connectionTask = mockConnectionTask(9);
        when(device.getConnectionTasks()).thenReturn(Collections.singletonList(connectionTask));
        when(connectionTask.getDevice()).thenReturn(device);
        partialConnectionTask = mockPartialConnectionTask(31L, "AS1440");
        when(deviceConfiguration.getPartialConnectionTasks()).thenReturn(Collections.singletonList(partialConnectionTask));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(connectionTaskService.findAndLockConnectionTaskByIdAndVersion(connectionTask.getId(), connectionTask.getVersion())).thenReturn(Optional.of(connectionTask));
        when(connectionTaskService.findConnectionTask(connectionTask.getId())).thenReturn(Optional.of(connectionTask));
    }

    @Test
    public void testGetAllConnections() {
        String response = target("/devices/ZABF0000000/connectionmethods").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.connectionMethods")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.connectionMethods[0].id")).isEqualTo(9);
        assertThat(jsonModel.<Integer>get("$.connectionMethods[0].comWindowStart")).isEqualTo(60);
        assertThat(jsonModel.<Integer>get("$.connectionMethods[0].comWindowEnd")).isEqualTo(120);
        assertThat(jsonModel.<Boolean>get("$.connectionMethods[0].isDefault")).isEqualTo(true);
        assertThat(jsonModel.<Integer>get("$.connectionMethods[0].numberOfSimultaneousConnections")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.connectionMethods[0].direction")).isEqualTo("Outbound");
        assertThat(jsonModel.<String>get("$.connectionMethods[0].name")).isEqualTo("it's me");
        assertThat(jsonModel.<String>get("$.connectionMethods[0].status")).isEqualTo("connectionTaskStatusActive");
        assertThat(jsonModel.<String>get("$.connectionMethods[0].connectionType")).isEqualTo("Pluggable class");
        assertThat(jsonModel.<String>get("$.connectionMethods[0].connectionStrategyInfo.connectionStrategy")).isEqualTo(ConnectionStrategy.AS_SOON_AS_POSSIBLE.name());
        assertThat(jsonModel.<String>get("$.connectionMethods[0].comPortPool")).isEqualTo("com port pool");
        assertThat(jsonModel.<List>get("$.connectionMethods[0].properties")).isEmpty();
        assertThat(jsonModel.<String>get("$.connectionMethods[0].protocolDialect")).isEqualTo("Protocol Dialect Name");
        assertThat(jsonModel.<String>get("$.connectionMethods[0].protocolDialectDisplayName")).isEqualTo("Protocol Dialect DisplayName");
        assertThat(jsonModel.<Integer>get("$.connectionMethods[0].connectionFunctionInfo.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.connectionMethods[0].connectionFunctionInfo.localizedValue")).isEqualTo("CF 1");
    }

    @Test
    public void testGetAllConnectionsForFullTopology() {
        doReturn(Arrays.asList(connectionTask, mockConnectionTask(123L), mockConnectionTask(421L))).when(topologyService).findAllConnectionTasksForTopology(device);

        String response = target("/devices/ZABF0000000/connectionmethods").queryParam("fullTopology", true).request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<?>>get("$.connectionMethods")).hasSize(3);
        Integer connectionTaskId1 = jsonModel.<Integer>get("$.connectionMethods[0].id");
        Integer connectionTaskId2 = jsonModel.<Integer>get("$.connectionMethods[1].id");
        Integer connectionTaskId3 = jsonModel.<Integer>get("$.connectionMethods[2].id");
        assertThat(connectionTaskId1).isNotEqualTo(connectionTaskId2);
        assertThat(connectionTaskId1).isNotEqualTo(connectionTaskId3);
        assertThat(connectionTaskId2).isNotEqualTo(connectionTaskId3);
        assertThat(connectionTaskId1).isIn(9, 123, 421);
        assertThat(connectionTaskId2).isIn(9, 123, 421);
        assertThat(connectionTaskId3).isIn(9, 123, 421);
    }

    @Test
    public void testGetAllConnectionsForDeviceOnly() {
        doReturn(Arrays.asList(connectionTask, mockConnectionTask(123L), mockConnectionTask(421L))).when(topologyService).findAllConnectionTasksForTopology(device);

        String response = target("/devices/ZABF0000000/connectionmethods").queryParam("fullTopology", favoritesService).request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.connectionMethods")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.connectionMethods[0].id")).isEqualTo(9);
    }

    @Test
    public void testUpdateScheduledConnectionMethodWithNewSchedule() {
        ScheduledConnectionMethodInfo info = new ScheduledConnectionMethodInfo();
        info.name = "AS1440";
        info.status = ConnectionTaskLifecycleStatus.INCOMPLETE;
        info.nextExecutionSpecs = new TemporalExpressionInfo();
        info.nextExecutionSpecs.every = new TimeDurationInfo();
        info.nextExecutionSpecs.every.count = 15;
        info.nextExecutionSpecs.every.timeUnit = "minutes";
        info.version = connectionTask.getVersion();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());

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
        info.nextExecutionSpecs.every = new TimeDurationInfo();
        info.nextExecutionSpecs.every.count = 15;
        info.nextExecutionSpecs.every.timeUnit = ""; // ILLEGAL
        info.version = connectionTask.getVersion();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());
        info.connectionStrategyInfo = new DeviceConnectionTaskInfo.ConnectionStrategyInfo();
        info.connectionStrategyInfo.connectionStrategy = ConnectionStrategy.AS_SOON_AS_POSSIBLE.name();
        info.connectionStrategyInfo.localizedValue = "Localized " + ConnectionStrategy.AS_SOON_AS_POSSIBLE.name();

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
        info.connectionStrategyInfo = new DeviceConnectionTaskInfo.ConnectionStrategyInfo();
        info.connectionStrategyInfo.connectionStrategy = ConnectionStrategy.AS_SOON_AS_POSSIBLE.name();
        info.connectionStrategyInfo.localizedValue = "Localized " + ConnectionStrategy.AS_SOON_AS_POSSIBLE.name();
        info.nextExecutionSpecs = new TemporalExpressionInfo();
        info.nextExecutionSpecs.every = new TimeDurationInfo();
        info.nextExecutionSpecs.every.count = 15;
        info.nextExecutionSpecs.every.timeUnit = "minutes";
        info.nextExecutionSpecs.offset = new TimeDurationInfo();
        info.nextExecutionSpecs.offset.count = 13;
        info.nextExecutionSpecs.offset.timeUnit = "illegal"; // ILLEGAL
        info.version = connectionTask.getVersion();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());
        info.protocolDialect = "Protocol Dialect Name";
        info.protocolDialectDisplayName = "Protocol Dialect DisplayName";

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
        info.protocolDialect = "Protocol Dialect Name";
        info.protocolDialectDisplayName = "Protocol Dialect DisplayName";
        info.properties = Collections.singletonList(
                new PropertyInfo("connectionTimeout", "connectionTimeout",
                        new PropertyValueInfo<>(new TimeDuration("15 seconds"), null, null, null),
                        new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.TEMPORALAMOUNT, null, null, null),
                        false));
        info.version = connectionTask.getVersion();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());

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
        info.connectionStrategyInfo = new DeviceConnectionTaskInfo.ConnectionStrategyInfo();
        info.connectionStrategyInfo.connectionStrategy = ConnectionStrategy.AS_SOON_AS_POSSIBLE.name();
        info.connectionStrategyInfo.localizedValue = "Translation for " + ConnectionStrategy.AS_SOON_AS_POSSIBLE.name();
        info.protocolDialect = "Protocol Dialect Name";
        info.protocolDialectDisplayName = "Protocol Dialect DisplayName";
        PropertyInfo propertyInfo = new PropertyInfo("connectionTimeout", "connectionTimeout",
                new PropertyValueInfo("", "", null, null),
                new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.TEMPORALAMOUNT, null, null, null),
                false);
        info.properties = Collections.singletonList(propertyInfo);
        Device.ScheduledConnectionTaskBuilder connectionTaskBuilder = mock(Device.ScheduledConnectionTaskBuilder.class);
        when(device.getScheduledConnectionTaskBuilder(Matchers.any())).thenReturn(connectionTaskBuilder);
        when(connectionTaskBuilder.add()).thenReturn(connectionTask);

        Response response = target("/devices/ZABF0000000/connectionmethods").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        verify(connectionTaskBuilder).setProperty("connectionTimeout", null);
    }

    @Test
    public void testCreateScheduledConnectionMethodWithInheritedProperty() {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty("connectionTimeout", new TimeDuration(60, TimeDuration.TimeUnit.SECONDS));
        when(partialConnectionTask.getTypedProperties()).thenReturn(typedProperties);

        ScheduledConnectionMethodInfo info = new ScheduledConnectionMethodInfo();
        info.name = "AS1440";
        info.status = ConnectionTaskLifecycleStatus.INCOMPLETE;
        info.connectionStrategyInfo = new DeviceConnectionTaskInfo.ConnectionStrategyInfo();
        info.connectionStrategyInfo.connectionStrategy = ConnectionStrategy.AS_SOON_AS_POSSIBLE.name();
        info.connectionStrategyInfo.localizedValue = "Translation for " + ConnectionStrategy.AS_SOON_AS_POSSIBLE.name();
        info.protocolDialect = "Protocol Dialect Name";
        info.protocolDialectDisplayName = "Protocol Dialect DisplayName";
        PropertyInfo propertyInfo = new PropertyInfo("connectionTimeout", "connectionTimeout",
                new PropertyValueInfo(null, new TimeDurationInfo(new TimeDuration("15 minutes")), null, null),
                new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.TEMPORALAMOUNT, null, null, null),
                false);
        info.properties = Collections.singletonList(propertyInfo);
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
        info.protocolDialect = "Protocol Dialect Name";
        info.protocolDialectDisplayName = "Protocol Dialect DisplayName";
        PropertyInfo propertyInfo = new PropertyInfo("connectionTimeout", "connectionTimeout",
                new PropertyValueInfo<>(null, null, null, null),
                new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.TEMPORALAMOUNT, null, null, null),
                false);
        info.properties = Collections.singletonList(propertyInfo);
        info.version = connectionTask.getVersion();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());

        Response response = target("/devices/ZABF0000000/connectionmethods/9").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(connectionTask).setProperty("connectionTimeout", null);
    }

    @Test
    public void testUpdateScheduledConnectionMethodWithInheritedProperty() throws IOException {
        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty("connectionTimeout", new TimeDuration(60, TimeDuration.TimeUnit.SECONDS));
        when(partialConnectionTask.getTypedProperties()).thenReturn(typedProperties);
        ScheduledConnectionMethodInfo info = new ScheduledConnectionMethodInfo();
        info.name = "AS1440";
        info.status = ConnectionTaskLifecycleStatus.INCOMPLETE;
        info.protocolDialect = "Protocol Dialect Name";
        info.protocolDialectDisplayName = "Protocol Dialect DisplayName";
        PropertyInfo propertyInfo = new PropertyInfo("connectionTimeout", "connectionTimeout",
                new PropertyValueInfo<>(null, new TimeDurationInfo(new TimeDuration("15 minutes")), null, null),
                new PropertyTypeInfo(com.elster.jupiter.properties.rest.SimplePropertyType.TEMPORALAMOUNT, null, null, null),
                false);
        info.properties = Collections.singletonList(propertyInfo);
        info.version = connectionTask.getVersion();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());

        Response response = target("/devices/ZABF0000000/connectionmethods/9").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(connectionTask).removeProperty("connectionTimeout");
    }

    private ScheduledConnectionTask mockConnectionTask(long id) {
        ProtocolDialectConfigurationProperties properties = mockProtocolDialectConfigurationProperties();
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getProtocolDialectConfigurationProperties()).thenReturn(properties);
        ComSession comSession = mockComSession(comSessionStart, comSessionEnd);
        when(connectionTask.getLastComSession()).thenReturn(Optional.of(comSession));
        when(connectionTask.getId()).thenReturn(id);
        when(connectionTask.getName()).thenReturn("it's me");
        when(connectionTask.getSuccessIndicator()).thenReturn(SuccessIndicator.FAILURE);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionType.getDirection()).thenReturn(com.energyict.mdc.upl.io.ConnectionType.ConnectionTypeDirection.OUTBOUND);
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
        when(connectionTask.getDevice()).thenReturn(device);
        when(connectionTask.getVersion()).thenReturn(1L);
        when(connectionTask.getNumberOfSimultaneousConnections()).thenReturn(1);
        when(connectionTask.getProtocolDialectConfigurationProperties()).thenReturn(properties);
        when(deviceConfiguration.getProtocolDialectConfigurationPropertiesList()).thenReturn(Collections.singletonList(properties));
        return connectionTask;
    }

    private PartialScheduledConnectionTask mockPartialConnectionTask(long id, String name) {
        ProtocolDialectConfigurationProperties properties = mockProtocolDialectConfigurationProperties();
        PartialScheduledConnectionTask connectionTask = mock(PartialScheduledConnectionTask.class);
        when(connectionTask.getId()).thenReturn(id);
        when(connectionTask.getName()).thenReturn(name);
        when(connectionTask.getProtocolDialectConfigurationProperties()).thenReturn(properties);
        ConnectionType connectionType = mock(ConnectionType.class);
        when(connectionTask.getConnectionType()).thenReturn(connectionType);
        when(connectionType.getDirection()).thenReturn(com.energyict.mdc.upl.io.ConnectionType.ConnectionTypeDirection.OUTBOUND);
        ConnectionTypePluggableClass pluggableClass = mockPluggableClass();
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn("connectionTimeout");
        when(propertySpec.getDisplayName()).thenReturn("Connection timeout");
        when(propertySpec.isRequired()).thenReturn(false);
        when(propertySpec.getValueFactory()).thenReturn(new TemporalAmountValueFactory());
        when(pluggableClass.getPropertySpecs()).thenReturn(Collections.singletonList(propertySpec));
        when(connectionTask.getPluggableClass()).thenReturn(pluggableClass);
        when(connectionTask.isDefault()).thenReturn(true);
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        ComWindow window = mockWindow(PartialTime.fromMinutes(1), PartialTime.fromMinutes(2));
        when(connectionTask.getCommunicationWindow()).thenReturn(window);
        OutboundComPortPool comPortPool = mockComPortPool();
        when(connectionTask.getComPortPool()).thenReturn(comPortPool);
        TypedProperties typedProperties = TypedProperties.empty();
        when(connectionTask.getTypedProperties()).thenReturn(typedProperties);
        when(connectionTask.getConnectionFunction()).thenReturn(Optional.of(connectionFunction));
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
        when(partialConnectionTask.getConnectionFunction()).thenReturn(Optional.of(connectionFunction));
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

    private ProtocolDialectConfigurationProperties mockProtocolDialectConfigurationProperties() {
        DeviceProtocolDialect protocolDialect = mock(DeviceProtocolDialect.class);
        when(protocolDialect.getDeviceProtocolDialectDisplayName()).thenReturn("Protocol Dialect DisplayName");
        ProtocolDialectConfigurationProperties properties = mock(ProtocolDialectConfigurationProperties.class);
        when(properties.getDeviceProtocolDialectName()).thenReturn("Protocol Dialect Name");
        when(properties.getName()).thenReturn("Protocol Dialect Name");
        when(properties.getDeviceProtocolDialect()).thenReturn(protocolDialect);
        return properties;
    }

    private ConnectionFunction mockConnectionFunction(int id, String name, String displayName) {
        return new ConnectionFunction() {
            @Override
            public long getId() {
                return id;
            }

            @Override
            public String getConnectionFunctionName() {
                return name;
            }

            @Override
            public String getConnectionFunctionDisplayName() {
                return displayName;
            }
        };
    }
}
