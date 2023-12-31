/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.InboundConnectionTask;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.interval.PartialTime;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.ConnectionTask.ConnectionTaskLifecycleStatus;
import com.energyict.mdc.common.tasks.ConnectionTask.SuccessIndicator;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.common.tasks.history.ComSession;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo.ConnectionMethodInfo;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.internal.verification.VerificationModeFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConnectionResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Mock
    Device device;

    private Instant comSessionStart = Instant.now();
    private Instant comSessionEnd = comSessionStart.plus(Duration.ofMinutes(1));
    private Instant nextExecution = Instant.now();

    @Override
    protected void setupTranslations() {
        super.setupTranslations();
        Stream.of(CompletionCodeTranslationKeys.values()).forEach(this::mockTranslation);
        Stream.of(TaskStatusTranslationKeys.values()).forEach(this::mockTranslation);
        Stream.of(ConnectionTaskSuccessIndicatorTranslationKeys.values()).forEach(this::mockTranslation);
        Stream.of(ConnectionStrategyTranslationKeys.values()).forEach(this::mockTranslation);
        Stream.of(ComSessionSuccessIndicatorTranslationKeys.values()).forEach(this::mockTranslation);
    }

    private void mockTranslation(TranslationKey translationKey) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(translationKey.getDefaultFormat());
        doReturn(messageFormat).when(thesaurus).getFormat(translationKey);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(device.getName()).thenReturn("ZABF0000000");
        when(device.getVersion()).thenReturn(1L);
        when(deviceService.findDeviceByName(device.getName())).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion(device.getName(), device.getVersion())).thenReturn(Optional.of(device));
    }

    @Test
    public void testGetAllConnections() {
        ConnectionTask<?, ?> connectionTask = mockConnectionTask();

        when(device.getConnectionTasks()).thenReturn(Collections.singletonList(connectionTask));

        String response = target("/devices/ZABF0000000/connections").request().get(String.class);

        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.connections")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.connections[0].id")).isEqualTo(13);
        assertThat(jsonModel.<String>get("$.connections[0].currentState.id")).isEqualTo("Pending");
        assertThat(jsonModel.<String>get("$.connections[0].currentState.displayValue")).isEqualTo("Pending");
        assertThat(jsonModel.<String>get("$.connections[0].latestStatus.id")).isEqualTo("FAILURE");
        assertThat(jsonModel.<String>get("$.connections[0].latestStatus.displayValue")).isEqualTo("Failed");
        assertThat(jsonModel.<String>get("$.connections[0].latestResult.id")).isEqualTo("Success");
        assertThat(jsonModel.<String>get("$.connections[0].latestResult.displayValue")).isEqualTo("Successful");
        assertThat(jsonModel.<Integer>get("$.connections[0].latestResult.retries")).isEqualTo(0);
        assertThat(jsonModel.<Integer>get("$.connections[0].taskCount.numberOfSuccessfulTasks")).isEqualTo(12);
        assertThat(jsonModel.<Integer>get("$.connections[0].taskCount.numberOfFailedTasks")).isEqualTo(401);
        assertThat(jsonModel.<Integer>get("$.connections[0].taskCount.numberOfIncompleteTasks")).isEqualTo(3);
        assertThat(jsonModel.<Long>get("$.connections[0].startDateTime")).isEqualTo(Date.from(comSessionStart.with(ChronoField.MILLI_OF_SECOND, 0)).getTime());
        assertThat(jsonModel.<Long>get("$.connections[0].endDateTime")).isEqualTo(Date.from(comSessionEnd.with(ChronoField.MILLI_OF_SECOND, 0)).getTime());
        assertThat(jsonModel.<Integer>get("$.connections[0].duration.count")).isEqualTo(60);
        assertThat(jsonModel.<String>get("$.connections[0].duration.timeUnit")).isEqualTo("seconds");
        assertThat(jsonModel.<Integer>get("$.connections[0].comPort.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.connections[0].comPort.name")).isEqualTo("com port");
        assertThat(jsonModel.<Integer>get("$.connections[0].comPortPool.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.connections[0].comPortPool.name")).isEqualTo("com port pool");
        assertThat(jsonModel.<Integer>get("$.connections[0].comServer.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.connections[0].comServer.name")).isEqualTo("com server");
        assertThat(jsonModel.<String>get("$.connections[0].direction")).isEqualTo("Outbound");
        assertThat(jsonModel.<String>get("$.connections[0].connectionType")).isEqualTo("Pluggable class");
        assertThat(jsonModel.<Integer>get("$.connections[0].connectionMethod.id")).isEqualTo(991);
        assertThat(jsonModel.<String>get("$.connections[0].connectionMethod.name")).isEqualTo("partial connection task name");
        assertThat(jsonModel.<Boolean>get("$.connections[0].connectionMethod.isDefault")).isTrue();
        assertThat(jsonModel.<String>get("$.connections[0].connectionMethod.status")).isEqualTo("active");
        assertThat(jsonModel.<String>get("$.connections[0].window")).isEqualTo("00:01 - 00:02");
        assertThat(jsonModel.<String>get("$.connections[0].connectionStrategyInfo.connectionStrategy")).isEqualTo(ConnectionStrategy.AS_SOON_AS_POSSIBLE.name());
        assertThat(jsonModel.<String>get("$.connections[0].connectionStrategyInfo.localizedValue")).isEqualTo(ConnectionStrategyTranslationKeys.AS_SOON_AS_POSSIBLE.getDefaultFormat());
        assertThat(jsonModel.<Long>get("$.connections[0].nextExecution")).isEqualTo(nextExecution.toEpochMilli());
        assertThat(jsonModel.<Integer>get("$.connections[0].comSessionId")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.connections[0].protocolDialect")).isEqualTo("Protocol Dialect Name");
        assertThat(jsonModel.<String>get("$.connections[0].protocolDialectDisplayName")).isEqualTo("Display Name of Protocol Dialect");
    }

    @Test
    public void testActivateConnection() {
        ConnectionTask<?, ?> connectionTask = mockConnectionTask();

        DeviceConnectionTaskInfo info = new DeviceConnectionTaskInfo();
        info.connectionMethod = new ConnectionMethodInfo();
        info.connectionMethod.status = ConnectionTaskLifecycleStatus.ACTIVE;
        info.version = connectionTask.getVersion();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());
        Entity<?> payload = Entity.entity(info, MediaType.APPLICATION_JSON);
        Response response = target("/devices/ZABF0000000/connections/13").request().put(payload);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(connectionTask).activate();
    }

    @Test
    public void testDeactivateConnection() {
        ConnectionTask<?, ?> connectionTask = mockConnectionTask();
        when(device.getConnectionTasks()).thenReturn(Collections.singletonList(connectionTask));

        DeviceConnectionTaskInfo info = new DeviceConnectionTaskInfo();
        info.connectionMethod = new ConnectionMethodInfo();
        info.connectionMethod.status = ConnectionTaskLifecycleStatus.INACTIVE;
        info.version = connectionTask.getVersion();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());
        Entity<?> payload = Entity.entity(info, MediaType.APPLICATION_JSON);
        Response response = target("/devices/ZABF0000000/connections/13").request().put(payload);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(connectionTask).deactivate();
    }

    @Test
    public void testActivateDeactivateWrongStatus() {
        ConnectionTask<?, ?> connectionTask = mockConnectionTask();
        when(device.getConnectionTasks()).thenReturn(Collections.singletonList(connectionTask));

        DeviceConnectionTaskInfo info = new DeviceConnectionTaskInfo();
        info.connectionMethod = new ConnectionMethodInfo();
        info.connectionMethod.status = ConnectionTaskLifecycleStatus.INCOMPLETE;
        info.version = connectionTask.getVersion();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());

        Entity<?> payload = Entity.json(info);
        Response response = target("/devices/ZABF0000000/connections/13").request().put(payload);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(connectionTask, VerificationModeFactory.times(0)).activate();
        verify(connectionTask, VerificationModeFactory.times(0)).deactivate();
    }

    @Test
    public void testRunConnection() {
        ScheduledConnectionTask connectionTask = mockConnectionTask();
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));

        DeviceConnectionTaskInfo info = new DeviceConnectionTaskInfo();
        info.connectionMethod = new ConnectionMethodInfo();
        info.connectionMethod.status = ConnectionTaskLifecycleStatus.INCOMPLETE;
        info.version = connectionTask.getVersion();
        info.parent = new VersionInfo<>(device.getmRID(), device.getVersion());

        Response response = target("/devices/ZABF0000000/connections/13/run").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(connectionTask).scheduleNow();
    }

    @Test
    public void testRunConnectionBadVersion() {
        ScheduledConnectionTask connectionTask = mockConnectionTask();
        long badVersion = connectionTask.getVersion() + 1;
        when(connectionTaskService.findAndLockConnectionTaskByIdAndVersion(connectionTask.getId(), badVersion)).thenReturn(Optional.empty());
        DeviceConnectionTaskInfo info = new DeviceConnectionTaskInfo();
        info.version = badVersion;
        info.parent = new VersionInfo<>(device.getmRID(), device.getVersion());

        Response response = target("/devices/ZABF0000000/connections/13/run").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(connectionTask, never()).scheduleNow();
    }

    @Test
    public void testRunConnectionWrongConnectionType() {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(12L);
        when(connectionTask.getVersion()).thenReturn(1L);
        when(device.getConnectionTasks()).thenReturn(Arrays.asList(connectionTask));
        when(connectionTaskService.findAndLockConnectionTaskByIdAndVersion(connectionTask.getId(), connectionTask.getVersion())).thenReturn(Optional.of(connectionTask));

        DeviceConnectionTaskInfo info = new DeviceConnectionTaskInfo();
        info.version = connectionTask.getVersion();
        info.parent = new VersionInfo<>(device.getmRID(), device.getVersion());

        Response response = target("/devices/ZABF0000000/connections/12/run").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    private ScheduledConnectionTask mockConnectionTask() {
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        ComSession comSession = mockComSession(comSessionStart, comSessionEnd);
        when(connectionTask.getLastComSession()).thenReturn(Optional.of(comSession));
        when(connectionTask.getId()).thenReturn(13L);
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
        when(connectionTask.isObsolete()).thenReturn(false);
        when(connectionTaskService.findAndLockConnectionTaskByIdAndVersion(connectionTask.getId(), connectionTask.getVersion())).thenReturn(Optional.of(connectionTask));
        when(connectionTaskService.findConnectionTask(connectionTask.getId())).thenReturn(Optional.of(connectionTask));

        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        DeviceProtocolDialect deviceProtocolDialect = mock(DeviceProtocolDialect.class);
        when(deviceProtocolDialect.getDeviceProtocolDialectName()).thenReturn("Protocol Dialect Name");
        when(deviceProtocolDialect.getDeviceProtocolDialectDisplayName()).thenReturn("Display Name of Protocol Dialect");
        when(protocolDialectConfigurationProperties.getDeviceProtocolDialect()).thenReturn(deviceProtocolDialect);
        when(protocolDialectConfigurationProperties.getDeviceProtocolDialectName()).thenReturn("Protocol Dialect Name");
        when(connectionTask.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);

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
