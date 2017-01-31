/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.ActivatedBreakerStatus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.ServerComTaskExecution;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTask;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundCapableComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceBreakerStatus;
import com.energyict.mdc.engine.impl.meterdata.DeviceFirmwareVersion;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersionBuilder;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedCalendar;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import com.google.common.collect.Range;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests the {@link ComServerDAOImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-07 (10:02)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComServerDAOImplTest {

    private static final long COMSERVER_ID = 1;
    private static final long COMPORT_ID = 2;
    private static final long SCHEDULED_COMTASK_ID = 3;
    private static final String IP_ADDRESS = "192.168.2.100";
    private static final String IP_ADDRESS_PROPERTY_NAME = "ipAddress";
    private static final String ACTIVE_CALENDAR_NAME = "Active";
    private static final String PASSIVE_CALENDAR_NAME = "Passive";

    @Mock
    private OutboundCapableComServer comServer;
    @Mock
    private OutboundComPort comPort;
    @Mock
    private ComTaskExecution scheduledComTask;
    @Mock
    private Device device;
    @Mock
    private Device.CalendarSupport calendarSupport;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceIdentifier deviceIdentifier;
    @Mock
    private DeviceIdentifier gatewayDeviceIdentifier;
    @Mock
    private EngineConfigurationService engineConfigurationService;
    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock
    private ServerCommunicationTaskService communicationTaskService;
    @Mock
    private TopologyService topologyService;
    @Mock
    private ComServerDAOImpl.ServiceProvider serviceProvider;
    @Mock
    private FirmwareService firmwareService;
    @Mock
    private DeviceService deviceDataModelService;
    @Mock
    private User comServerUser;

    private ComServerDAO comServerDAO;

    @Before
    public void initializeMocks() {
        when(this.device.getDeviceType()).thenReturn(this.deviceType);
        when(this.device.calendars()).thenReturn(this.calendarSupport);
        TransactionService transactionService = new FakeTransactionService();
        when(this.serviceProvider.transactionService()).thenReturn(transactionService);
        when(this.serviceProvider.engineConfigurationService()).thenReturn(this.engineConfigurationService);
        when(this.serviceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.serviceProvider.communicationTaskService()).thenReturn(this.communicationTaskService);
        when(this.serviceProvider.topologyService()).thenReturn(this.topologyService);
        when(this.serviceProvider.firmwareService()).thenReturn(this.firmwareService);
        when(this.serviceProvider.deviceService()).thenReturn(this.deviceDataModelService);
        when(this.serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
        when(this.engineConfigurationService.findComServerBySystemName()).thenReturn(Optional.<ComServer>of(this.comServer));
        when(this.engineConfigurationService.findComServer(COMSERVER_ID)).thenReturn(Optional.<ComServer>of(this.comServer));
        doReturn(Optional.of(this.comPort)).when(this.engineConfigurationService).findComPort(COMPORT_ID);
        when(this.communicationTaskService.findComTaskExecution(SCHEDULED_COMTASK_ID)).thenReturn(Optional.of(this.scheduledComTask));
        when(this.comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.comPort.getId()).thenReturn(COMPORT_ID);
        when(this.scheduledComTask.getId()).thenReturn(SCHEDULED_COMTASK_ID);
        when(this.comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.comPort.getId()).thenReturn(COMPORT_ID);
        when(this.scheduledComTask.getId()).thenReturn(SCHEDULED_COMTASK_ID);
        this.comServerDAO = new ComServerDAOImpl(this.serviceProvider, comServerUser);
    }

    @Test
    public void testGetThisComServer() {
        // Business method and asserts
        assertThat(this.comServerDAO.getThisComServer()).isNotNull();
        verify(this.engineConfigurationService).findComServerBySystemName();
    }

    @Test
    public void testRefreshComServerThatHasNotChanged() {
        Instant modificationDate = Instant.now();
        when(this.comServer.getModificationDate()).thenReturn(modificationDate);
        OutboundCapableComServer reloaded = mock(OutboundCapableComServer.class);
        when(reloaded.getModificationDate()).thenReturn(modificationDate);
        when(this.engineConfigurationService.findComServer(COMSERVER_ID)).thenReturn(Optional.<ComServer>of(reloaded));

        // Business method and asserts
        ComServer refreshed = this.comServerDAO.refreshComServer(this.comServer);
        assertThat(refreshed).isNotNull();
        assertThat(refreshed).isSameAs(this.comServer);
    }

    @Test
    public void testRefreshComServerThatChanged() {
        ComServer changed = mock(ComServer.class);
        when(this.comServer.getVersion()).thenReturn(1L);
        when(changed.getVersion()).thenReturn(2L);
        when(this.engineConfigurationService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(changed));

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComServer(this.comServer)).isSameAs(changed);
    }

    @Test
    public void testRefreshComServerThatWasMadeObsolete() {
        ComServer obsolete = mock(ComServer.class);
        when(obsolete.isObsolete()).thenReturn(true);
        when(this.engineConfigurationService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(obsolete));

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComServer(this.comServer)).isNull();
    }

    @Test
    public void testRefreshComServerThatWasDeleted() {
        when(this.engineConfigurationService.findComServer(COMSERVER_ID)).thenReturn(Optional.empty());

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComServer(this.comServer)).isNull();
    }

    @Test
    public void testExecutionStarted() throws SQLException {
        // Business method
        this.comServerDAO.executionStarted(this.scheduledComTask, this.comPort, true);

        // Asserts
        verify(this.communicationTaskService).executionStartedFor(this.scheduledComTask, this.comPort);
    }

    @Test
    public void testComTaskExecutionCompleted() throws SQLException {
        // Business method
        this.comServerDAO.executionCompleted(this.scheduledComTask);

        // Asserts
        verify(this.communicationTaskService).executionCompletedFor(this.scheduledComTask);
    }

    @Test
    public void testConnectionTaskExecutionCompleted() throws SQLException {
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getId()).thenReturn(10L);
        when(connectionTaskService.findConnectionTask(anyLong())).thenReturn(Optional.of(connectionTask));

        // Business method
        this.comServerDAO.executionCompleted(connectionTask);

        // Asserts
        verify(connectionTask).executionCompleted();
    }

    @Test
    public void testExecutionFailed() throws SQLException {
        // Business method
        this.comServerDAO.executionFailed(this.scheduledComTask);

        // Asserts
        verify(this.communicationTaskService).executionFailedFor(this.scheduledComTask);
    }

    public void testReleaseInterruptedComTasks() throws SQLException {
        // Business method
        this.comServerDAO.releaseInterruptedTasks(this.comServer);

        // Asserts
        verify(this.connectionTaskService).releaseInterruptedConnectionTasks(this.comServer);
        verify(this.communicationTaskService).releaseInterruptedComTasks(this.comServer);
    }

    @Test
    public void testReleaseTimedOutComTasks() throws SQLException {
        // Business method
        this.comServerDAO.releaseTimedOutTasks(this.comServer);

        // Asserts
        verify(this.connectionTaskService).releaseTimedOutConnectionTasks(this.comServer);
        verify(this.communicationTaskService).releaseTimedOutComTasks(this.comServer);
    }

    @Test
    public void testUpdateGateway_GatewayRemoved() throws Exception {
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);
        when(this.gatewayDeviceIdentifier.findDevice()).thenReturn(null);

        // Business method
        this.comServerDAO.updateGateway(this.deviceIdentifier, this.gatewayDeviceIdentifier);

        // Asserts
        verify(this.topologyService).clearPhysicalGateway(this.device);
    }

    @Test
    public void testUpdateGateway_DifferentGateway() throws Exception {
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);
        Device gateway = mock(Device.class);
        when(this.gatewayDeviceIdentifier.findDevice()).thenReturn(gateway);

        // Business method
        this.comServerDAO.updateGateway(this.deviceIdentifier, this.gatewayDeviceIdentifier);

        // Asserts
        verify(this.topologyService).setPhysicalGateway(this.device, gateway);
    }

    @Test
    public void testIsStillPendingDelegatesToComTaskExecutionFactory() {
        int id = 97;
        when(this.communicationTaskService.isComTaskStillPending(id)).thenReturn(true);

        // Business method
        boolean stillPending = this.comServerDAO.isStillPending(id);

        // Asserts
        assertThat(stillPending).isTrue();
        verify(this.communicationTaskService).isComTaskStillPending(id);
    }

    @Test
    public void testAreStillPendingDelegatesToComTaskExecutionFactory() {
        long id1 = 97;
        long id2 = 101;
        long id3 = 103;
        List<Long> comTaskExecutionIds = Arrays.asList(id1, id2, id3);
        when(this.communicationTaskService.areComTasksStillPending(comTaskExecutionIds)).thenReturn(true);

        // Business method
        boolean stillPending = this.comServerDAO.areStillPending(comTaskExecutionIds);

        // Asserts
        assertThat(stillPending).isTrue();
        verify(this.communicationTaskService).areComTasksStillPending(comTaskExecutionIds);
    }

    @Test
    public void testUpdateIpAddress() throws SQLException {
        TypedProperties properties = mock(TypedProperties.class);
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getId()).thenReturn(10L);
        when(connectionTask.getTypedProperties()).thenReturn(properties);
        when(connectionTaskService.findConnectionTask(anyLong())).thenReturn(Optional.of(connectionTask));

        // Business method
        this.comServerDAO.updateIpAddress(IP_ADDRESS, connectionTask, IP_ADDRESS_PROPERTY_NAME);

        // Asserts
        verify(connectionTask).setProperty(IP_ADDRESS_PROPERTY_NAME, IP_ADDRESS);
        verify(connectionTask).save();
    }

    @Test
    public void releaseComTasksForComPortTest() throws SQLException {
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class, withSettings().extraInterfaces(ServerConnectionTask.class));
        ServerComTaskExecution comTaskExecution1 = mock(ServerComTaskExecution.class);
        when(comTaskExecution1.getConnectionTask()).thenReturn(Optional.of(connectionTask));
        ServerComTaskExecution comTaskExecution2 = mock(ServerComTaskExecution.class);
        when(comTaskExecution2.getConnectionTask()).thenReturn(Optional.of(connectionTask));
        ServerComTaskExecution comTaskExecution3 = mock(ServerComTaskExecution.class);
        when(comTaskExecution3.getConnectionTask()).thenReturn(Optional.of(connectionTask));
        when(communicationTaskService.findComTaskExecutionsWhichAreExecuting(this.comPort)).thenReturn(Arrays.<ComTaskExecution>asList(comTaskExecution1, comTaskExecution2, comTaskExecution3));
        when(connectionTaskService.findConnectionTask(anyLong())).thenReturn(Optional.of(connectionTask));

        // Business method
        this.comServerDAO.releaseTasksFor(this.comPort);

        // Asserts
        verify(communicationTaskService).unlockComTaskExecution(comTaskExecution1);
        verify(communicationTaskService).unlockComTaskExecution(comTaskExecution2);
        verify(communicationTaskService).unlockComTaskExecution(comTaskExecution3);
        verify(connectionTaskService).unlockConnectionTask(eq(connectionTask));
    }

    @Test
    public void updateFirmwareVersionsTest() {
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);
        when(this.firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.METER)).thenReturn(Optional.empty());
        when(this.firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.COMMUNICATION)).thenReturn(Optional.empty());

        CollectedFirmwareVersion collectedFirmwareVersion = new DeviceFirmwareVersion(deviceIdentifier);
        this.comServerDAO.updateFirmwareVersions(collectedFirmwareVersion);

        verify(this.firmwareService, never()).newFirmwareVersion(any(DeviceType.class), any(String.class), any(FirmwareStatus.class), any(FirmwareType.class));
    }

    @Test
    public void updateWithCreationOfGhostMeterFirmwareVersionTest() {
        // setup
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);
        when(this.firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.METER)).thenReturn(Optional.empty());
        when(this.firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.COMMUNICATION)).thenReturn(Optional.empty());
        String newActiveMeterFirmwareVersion = "MyActiveMeterFirmwareVersion";
        CollectedFirmwareVersion collectedFirmwareVersion = new DeviceFirmwareVersion(deviceIdentifier);
        collectedFirmwareVersion.setActiveMeterFirmwareVersion(newActiveMeterFirmwareVersion);
        FirmwareVersionBuilder firmwareVersionBuilder = mock(FirmwareVersionBuilder.class);
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(firmwareVersionBuilder.create()).thenReturn(firmwareVersion);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.GHOST);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(this.firmwareService.getFirmwareVersionByVersionAndType(newActiveMeterFirmwareVersion, FirmwareType.METER, deviceType)).thenReturn(Optional.empty());
        ActivatedFirmwareVersion activatedFirmwareVersion = mock(ActivatedFirmwareVersion.class);
        when(this.firmwareService.newActivatedFirmwareVersionFrom(any(Device.class), any(FirmwareVersion.class), any(Interval.class))).thenReturn(activatedFirmwareVersion);
        when(this.firmwareService.newFirmwareVersion(deviceType, newActiveMeterFirmwareVersion, FirmwareStatus.GHOST, FirmwareType.METER)).thenReturn(firmwareVersionBuilder);

        // business logic
        this.comServerDAO.updateFirmwareVersions(collectedFirmwareVersion);

        // asserts
        verify(this.firmwareService).newFirmwareVersion(deviceType, newActiveMeterFirmwareVersion, FirmwareStatus.GHOST, FirmwareType.METER);
        verify(this.firmwareService, never()).newFirmwareVersion(deviceType, newActiveMeterFirmwareVersion, FirmwareStatus.GHOST, FirmwareType.COMMUNICATION);
    }

    @Test
    public void updateWithCreationOfGhostMeterFirmwareVersionWhileCommunicationVersionExistsTest() {
        // setup
        ActivatedFirmwareVersion communicationVersion = mock(ActivatedFirmwareVersion.class);
        Optional<ActivatedFirmwareVersion> communicationVersionWithSameVersion = Optional.of(communicationVersion);
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);
        when(this.firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.METER)).thenReturn(Optional.empty());
        when(this.firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.COMMUNICATION)).thenReturn(communicationVersionWithSameVersion);
        String newActiveMeterFirmwareVersion = "SameVersion";
        CollectedFirmwareVersion collectedFirmwareVersion = new DeviceFirmwareVersion(deviceIdentifier);
        collectedFirmwareVersion.setActiveMeterFirmwareVersion(newActiveMeterFirmwareVersion);
        FirmwareVersionBuilder firmwareVersionBuilder = mock(FirmwareVersionBuilder.class);
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(firmwareVersionBuilder.create()).thenReturn(firmwareVersion);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.GHOST);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(this.firmwareService.getFirmwareVersionByVersionAndType(newActiveMeterFirmwareVersion, FirmwareType.METER, deviceType)).thenReturn(Optional.empty());
        FirmwareVersion communicationVersionWithSameName = mock(FirmwareVersion.class);
        when(communicationVersionWithSameName.getFirmwareVersion()).thenReturn(newActiveMeterFirmwareVersion);
        when(this.firmwareService.getFirmwareVersionByVersionAndType(newActiveMeterFirmwareVersion, FirmwareType.COMMUNICATION, deviceType)).thenReturn(Optional.of(communicationVersionWithSameName));
        ActivatedFirmwareVersion activatedFirmwareVersion = mock(ActivatedFirmwareVersion.class);
        when(this.firmwareService.newActivatedFirmwareVersionFrom(any(Device.class), any(FirmwareVersion.class), any(Interval.class))).thenReturn(activatedFirmwareVersion);
        when(this.firmwareService.newFirmwareVersion(deviceType, newActiveMeterFirmwareVersion, FirmwareStatus.GHOST, FirmwareType.METER)).thenReturn(firmwareVersionBuilder);

        // business logic
        this.comServerDAO.updateFirmwareVersions(collectedFirmwareVersion);

        // asserts
        verify(this.firmwareService).newFirmwareVersion(deviceType, newActiveMeterFirmwareVersion, FirmwareStatus.GHOST, FirmwareType.METER);
        verify(this.firmwareService, never()).newFirmwareVersion(deviceType, newActiveMeterFirmwareVersion, FirmwareStatus.GHOST, FirmwareType.COMMUNICATION);
    }

    @Test
    public void updateWithCreationOfGhostCommunicationFirmwareVersionTest() {
        // setup
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);
        when(this.firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.METER)).thenReturn(Optional.empty());
        when(this.firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.COMMUNICATION)).thenReturn(Optional.empty());
        String myActiveCommunicationFirmwareVersion = "MyActiveCommunicationFirmwareVersion";
        CollectedFirmwareVersion collectedFirmwareVersion = new DeviceFirmwareVersion(deviceIdentifier);
        collectedFirmwareVersion.setActiveCommunicationFirmwareVersion(myActiveCommunicationFirmwareVersion);
        FirmwareVersionBuilder firmwareVersionBuilder = mock(FirmwareVersionBuilder.class);
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(firmwareVersionBuilder.create()).thenReturn(firmwareVersion);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.GHOST);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);
        when(this.firmwareService.getFirmwareVersionByVersionAndType(myActiveCommunicationFirmwareVersion, FirmwareType.COMMUNICATION, deviceType)).thenReturn(Optional.empty());
        ActivatedFirmwareVersion activatedFirmwareVersion = mock(ActivatedFirmwareVersion.class);
        when(this.firmwareService.newActivatedFirmwareVersionFrom(any(Device.class), any(FirmwareVersion.class), any(Interval.class))).thenReturn(activatedFirmwareVersion);
        when(this.firmwareService.newFirmwareVersion(deviceType, myActiveCommunicationFirmwareVersion, FirmwareStatus.GHOST, FirmwareType.COMMUNICATION)).thenReturn(firmwareVersionBuilder);

        // business logic
        this.comServerDAO.updateFirmwareVersions(collectedFirmwareVersion);

        // asserts
        verify(this.firmwareService).newFirmwareVersion(deviceType, myActiveCommunicationFirmwareVersion, FirmwareStatus.GHOST, FirmwareType.COMMUNICATION);
        verify(this.firmwareService, never()).newFirmwareVersion(deviceType, myActiveCommunicationFirmwareVersion, FirmwareStatus.GHOST, FirmwareType.METER);
    }

    @Test
    public void updateWithCreationOfGhostCommunicationFirmwareVersionWhileMeterVersionExistsTest() {
        // setup
        ActivatedFirmwareVersion meterVersion = mock(ActivatedFirmwareVersion.class);
        Optional<ActivatedFirmwareVersion> meterVersionWithSameVersion = Optional.of(meterVersion);
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);
        when(this.firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.METER)).thenReturn(meterVersionWithSameVersion);
        when(this.firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.COMMUNICATION)).thenReturn(Optional.empty());
        String myActiveCommunicationFirmwareVersion = "SameVersion";
        CollectedFirmwareVersion collectedFirmwareVersion = new DeviceFirmwareVersion(deviceIdentifier);
        collectedFirmwareVersion.setActiveCommunicationFirmwareVersion(myActiveCommunicationFirmwareVersion);
        FirmwareVersionBuilder firmwareVersionBuilder = mock(FirmwareVersionBuilder.class);
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(firmwareVersionBuilder.create()).thenReturn(firmwareVersion);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.GHOST);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);
        when(this.firmwareService.getFirmwareVersionByVersionAndType(myActiveCommunicationFirmwareVersion, FirmwareType.COMMUNICATION, deviceType)).thenReturn(Optional.empty());
        FirmwareVersion meterVersionWithSameName = mock(FirmwareVersion.class);
        when(meterVersionWithSameName.getFirmwareVersion()).thenReturn(myActiveCommunicationFirmwareVersion);
        when(this.firmwareService.getFirmwareVersionByVersionAndType(myActiveCommunicationFirmwareVersion, FirmwareType.METER, deviceType)).thenReturn(Optional.of(meterVersionWithSameName));
        ActivatedFirmwareVersion activatedFirmwareVersion = mock(ActivatedFirmwareVersion.class);
        when(this.firmwareService.newActivatedFirmwareVersionFrom(any(Device.class), any(FirmwareVersion.class), any(Interval.class))).thenReturn(activatedFirmwareVersion);
        when(this.firmwareService.newFirmwareVersion(deviceType, myActiveCommunicationFirmwareVersion, FirmwareStatus.GHOST, FirmwareType.COMMUNICATION)).thenReturn(firmwareVersionBuilder);

        // business logic
        this.comServerDAO.updateFirmwareVersions(collectedFirmwareVersion);

        // asserts
        verify(this.firmwareService).newFirmwareVersion(deviceType, myActiveCommunicationFirmwareVersion, FirmwareStatus.GHOST, FirmwareType.COMMUNICATION);
        verify(this.firmwareService, never()).newFirmwareVersion(deviceType, myActiveCommunicationFirmwareVersion, FirmwareStatus.GHOST, FirmwareType.METER);
    }

    @Test
    public void updateLastCheckedForExistingMeterFirmwareVersionTest() {
        // setup
        String myActiveFirmwareVersion = "MyActiveMeterFirmwareVersion";
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);

        ActivatedFirmwareVersion activatedFirmwareVersion = mock(ActivatedFirmwareVersion.class);
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(activatedFirmwareVersion.getFirmwareVersion()).thenReturn(firmwareVersion);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.GHOST);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(firmwareVersion.getFirmwareVersion()).thenReturn(myActiveFirmwareVersion);
        when(this.firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.METER)).thenReturn(Optional.of(activatedFirmwareVersion));
        when(this.firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.COMMUNICATION)).thenReturn(Optional.empty());
        CollectedFirmwareVersion collectedFirmwareVersion = new DeviceFirmwareVersion(deviceIdentifier);
        collectedFirmwareVersion.setActiveMeterFirmwareVersion(myActiveFirmwareVersion);
        when(this.firmwareService.getFirmwareVersionByVersionAndType(myActiveFirmwareVersion, FirmwareType.METER, deviceType)).thenReturn(Optional.of(firmwareVersion));

        // business logic
        this.comServerDAO.updateFirmwareVersions(collectedFirmwareVersion);

        // asserts
        verify(activatedFirmwareVersion).setLastChecked(any(Instant.class));
        verify(this.firmwareService, never()).newFirmwareVersion(any(DeviceType.class), any(String.class), any(FirmwareStatus.class), any(FirmwareType.class));
    }

    @Test
    public void updateLastCheckedForExistingCommunicationFirmwareVersionTest() {
        // setup
        String myActiveCommunicationFirmwareVersion = "MyActiveCommunicationFirmwareVersionTest";
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);

        ActivatedFirmwareVersion activatedFirmwareVersion = mock(ActivatedFirmwareVersion.class);
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(activatedFirmwareVersion.getFirmwareVersion()).thenReturn(firmwareVersion);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.GHOST);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);
        when(firmwareVersion.getFirmwareVersion()).thenReturn(myActiveCommunicationFirmwareVersion);
        when(this.firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.COMMUNICATION)).thenReturn(Optional.of(activatedFirmwareVersion));
        when(this.firmwareService.getActiveFirmwareVersion(this.device, FirmwareType.METER)).thenReturn(Optional.empty());
        CollectedFirmwareVersion collectedFirmwareVersion = new DeviceFirmwareVersion(deviceIdentifier);
        collectedFirmwareVersion.setActiveCommunicationFirmwareVersion(myActiveCommunicationFirmwareVersion);
        when(this.firmwareService.getFirmwareVersionByVersionAndType(myActiveCommunicationFirmwareVersion, FirmwareType.COMMUNICATION, deviceType)).thenReturn(Optional.of(firmwareVersion));

        // business logic
        this.comServerDAO.updateFirmwareVersions(collectedFirmwareVersion);

        // asserts
        verify(activatedFirmwareVersion).setLastChecked(any(Instant.class));
        verify(this.firmwareService, never()).newFirmwareVersion(any(DeviceType.class), any(String.class), any(FirmwareStatus.class), any(FirmwareType.class));
    }

    @Test
    public void updateBreakerStatusHavingEmptyBreakerStatusTest() {
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);
        when(this.deviceDataModelService.getActiveBreakerStatus(this.device)).thenReturn(Optional.empty());

        // Business methods
        CollectedBreakerStatus collectedBreakerStatus = new DeviceBreakerStatus(deviceIdentifier);
        this.comServerDAO.updateBreakerStatus(collectedBreakerStatus);

        // Asserts
        verify(this.deviceDataModelService, never()).getActiveBreakerStatus(any(Device.class));
        verify(this.deviceDataModelService, never()).newActivatedBreakerStatusFrom(any(Device.class), any(BreakerStatus.class), any(Interval.class));
    }

    @Test
    public void updateBreakerStatusTest() {
        when(this.deviceIdentifier.findDevice()).thenReturn(device);
        when(this.deviceDataModelService.getActiveBreakerStatus(device)).thenReturn(Optional.empty());
        CollectedBreakerStatus collectedBreakerStatus = new DeviceBreakerStatus(deviceIdentifier);
        collectedBreakerStatus.setBreakerStatus(BreakerStatus.ARMED);
        when(this.deviceDataModelService.getActiveBreakerStatus(device)).thenReturn(Optional.empty());

        ArgumentCaptor<Device> deviceCaptor = ArgumentCaptor.forClass(Device.class);
        ArgumentCaptor<BreakerStatus> breakerStatusCaptor = ArgumentCaptor.forClass(BreakerStatus.class);
        ArgumentCaptor<Interval> intervalCaptor = ArgumentCaptor.forClass(Interval.class);
        when(this.deviceDataModelService.newActivatedBreakerStatusFrom(deviceCaptor.capture(), breakerStatusCaptor.capture(), intervalCaptor.capture())).thenReturn(mock(ActivatedBreakerStatus.class));

        // Business methods
        this.comServerDAO.updateBreakerStatus(collectedBreakerStatus);

        // asserts
        verify(this.deviceDataModelService, times(1)).newActivatedBreakerStatusFrom(any(Device.class), any(BreakerStatus.class), any(Interval.class));

        assertThat(deviceCaptor.getValue()).isEqualTo(this.device);
        assertThat(breakerStatusCaptor.getValue()).isEqualTo(BreakerStatus.ARMED);
        assertThat(intervalCaptor.getValue()).isNotNull();
        Range<Instant> range = intervalCaptor.getValue().toClosedRange();
        assertThat(range.hasUpperBound()).isFalse();
        assertThat(range.hasLowerBound()).isTrue();
        assertThat(range.lowerEndpoint().minusMillis(1)).isLessThan(Instant.now());
    }

    @Test
    public void updateBreakerStatusWhenDifferentActivatedBreakerStatusAlreadyExistsTest() {
        when(this.deviceIdentifier.findDevice()).thenReturn(device);
        ActivatedBreakerStatus activatedBreakerStatus = mock(ActivatedBreakerStatus.class);
        when(activatedBreakerStatus.getBreakerStatus()).thenReturn(BreakerStatus.DISCONNECTED);
        when(this.deviceDataModelService.getActiveBreakerStatus(device)).thenReturn(Optional.of(activatedBreakerStatus));

        CollectedBreakerStatus collectedBreakerStatus = new DeviceBreakerStatus(deviceIdentifier);
        collectedBreakerStatus.setBreakerStatus(BreakerStatus.ARMED);

        ArgumentCaptor<Device> deviceCaptor = ArgumentCaptor.forClass(Device.class);
        ArgumentCaptor<BreakerStatus> breakerStatusCaptor = ArgumentCaptor.forClass(BreakerStatus.class);
        ArgumentCaptor<Interval> intervalCaptor = ArgumentCaptor.forClass(Interval.class);
        when(this.deviceDataModelService.newActivatedBreakerStatusFrom(deviceCaptor.capture(), breakerStatusCaptor.capture(), intervalCaptor.capture())).thenReturn(mock(ActivatedBreakerStatus.class));

        // Business methods
        this.comServerDAO.updateBreakerStatus(collectedBreakerStatus);

        // asserts
        verify(this.deviceDataModelService, times(1)).newActivatedBreakerStatusFrom(any(Device.class), any(BreakerStatus.class), any(Interval.class));

        assertThat(deviceCaptor.getValue()).isEqualTo(device);
        assertThat(breakerStatusCaptor.getValue()).isEqualTo(BreakerStatus.ARMED);
        assertThat(intervalCaptor.getValue()).isNotNull();
        Range<Instant> range = intervalCaptor.getValue().toClosedRange();
        assertThat(range.hasUpperBound()).isFalse();
        assertThat(range.hasLowerBound()).isTrue();
        assertThat(range.lowerEndpoint().minusMillis(1)).isLessThan(Instant.now());
    }

    @Test
    public void updateLastCheckedForExistingBreakerStatusTest() {
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);
        ActivatedBreakerStatus activatedBreakerStatus = mock(ActivatedBreakerStatus.class);
        when(activatedBreakerStatus.getBreakerStatus()).thenReturn(BreakerStatus.DISCONNECTED);
        when(this.deviceDataModelService.getActiveBreakerStatus(device)).thenReturn(Optional.of(activatedBreakerStatus));

        CollectedBreakerStatus collectedBreakerStatus = new DeviceBreakerStatus(deviceIdentifier);
        collectedBreakerStatus.setBreakerStatus(BreakerStatus.DISCONNECTED);

        // business method
        this.comServerDAO.updateBreakerStatus(collectedBreakerStatus);

        // asserts
        verify(activatedBreakerStatus).setLastChecked(any(Instant.class));
        verify(this.deviceDataModelService, never()).newActivatedBreakerStatusFrom(any(Device.class), any(BreakerStatus.class), any(Interval.class));
    }

    @Test
    public void updateCalendarsDelegatesToDevice() {
        CollectedCalendar collectedCalendar = mock(CollectedCalendar.class);
        when(collectedCalendar.isEmpty()).thenReturn(false);
        when(collectedCalendar.getActiveCalendar()).thenReturn(Optional.of(ACTIVE_CALENDAR_NAME));
        when(collectedCalendar.getPassiveCalendar()).thenReturn(Optional.of(PASSIVE_CALENDAR_NAME));
        when(collectedCalendar.getDeviceIdentifier()).thenReturn(this.deviceIdentifier);
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);

        // Business method
        this.comServerDAO.updateCalendars(collectedCalendar);

        // Asserts
        verify(this.device).calendars();
        verify(this.calendarSupport).updateCalendars(collectedCalendar);
    }

    @Test
    public void updateDeviceMessageInformationTest() {
        MessageIdentifier messageIdentifier = mock(MessageIdentifier.class);
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(messageIdentifier.getDeviceMessage()).thenReturn(deviceMessage);

        DeviceMessageStatus deviceMessageStatus = DeviceMessageStatus.CONFIRMED;
        Instant sentDate = Instant.now();
        String protocolInfo = "protocolInfo";

        // business method
        this.comServerDAO.updateDeviceMessageInformation(messageIdentifier, deviceMessageStatus, sentDate, protocolInfo);

        // asserts
        verify(deviceMessage).setSentDate(sentDate);
        verify(deviceMessage).setProtocolInformation(protocolInfo);
        verify(deviceMessage).updateDeviceMessageStatus(deviceMessageStatus);
    }
}
