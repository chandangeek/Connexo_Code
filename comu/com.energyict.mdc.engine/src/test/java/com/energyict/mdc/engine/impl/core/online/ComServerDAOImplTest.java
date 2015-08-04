package com.energyict.mdc.engine.impl.core.online;

import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.config.*;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceFirmwareVersion;
import com.energyict.mdc.firmware.*;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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

    @Mock
    private OutboundCapableComServer comServer;
    @Mock
    private OutboundComPort comPort;
    @Mock
    private ComTaskExecution scheduledComTask;
    @Mock
    private Device device;
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

    private ComServerDAO comServerDAO;

    @Before
    public void initializeMocks() {
        when(device.getDeviceType()).thenReturn(deviceType);
        TransactionService transactionService = new FakeTransactionService();
        when(this.serviceProvider.transactionService()).thenReturn(transactionService);
        when(this.serviceProvider.engineConfigurationService()).thenReturn(this.engineConfigurationService);
        when(this.serviceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.serviceProvider.communicationTaskService()).thenReturn(this.communicationTaskService);
        when(this.serviceProvider.topologyService()).thenReturn(this.topologyService);
        when(this.serviceProvider.firmwareService()).thenReturn(this.firmwareService);
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
        this.comServerDAO = new ComServerDAOImpl(this.serviceProvider);
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
    public void testComTaskExecutionCompleted() throws SQLException, BusinessException {
        // Business method
        this.comServerDAO.executionCompleted(this.scheduledComTask);

        // Asserts
        verify(this.communicationTaskService).executionCompletedFor(this.scheduledComTask);
    }

    @Test
    public void testConnectionTaskExecutionCompleted() throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);

        // Business method
        this.comServerDAO.executionCompleted(connectionTask);

        // Asserts
        verify(connectionTask).executionCompleted();
    }

    @Test
    public void testExecutionFailed() throws SQLException, BusinessException {
        // Business method
        this.comServerDAO.executionFailed(this.scheduledComTask);

        // Asserts
        verify(this.communicationTaskService).executionFailedFor(this.scheduledComTask);
    }

    public void testReleaseInterruptedComTasks() throws SQLException, BusinessException {
        // Business method
        this.comServerDAO.releaseInterruptedTasks(this.comServer);

        // Asserts
        verify(this.connectionTaskService).releaseInterruptedConnectionTasks(this.comServer);
        verify(this.communicationTaskService).releaseInterruptedComTasks(this.comServer);
    }

    @Test
    public void testReleaseTimedOutComTasks() throws SQLException, BusinessException {
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
    public void testUpdateIpAddress() throws SQLException, BusinessException {
        TypedProperties properties = mock(TypedProperties.class);
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getTypedProperties()).thenReturn(properties);

        // Business method
        this.comServerDAO.updateIpAddress(IP_ADDRESS, connectionTask, IP_ADDRESS_PROPERTY_NAME);

        // Asserts
        verify(connectionTask).getTypedProperties();
        verify(properties).setProperty(IP_ADDRESS_PROPERTY_NAME, IP_ADDRESS);
        // Todo (JP-1123) verify(connectionTask).updateProperties(properties);
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
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.GHOST);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(this.firmwareService.getFirmwareVersionByVersionAndType(newActiveMeterFirmwareVersion, FirmwareType.METER, deviceType)).thenReturn(Optional.empty());
        ActivatedFirmwareVersion activatedFirmwareVersion = mock(ActivatedFirmwareVersion.class);
        when(this.firmwareService.newActivatedFirmwareVersionFrom(any(Device.class), any(FirmwareVersion.class), any(Interval.class))).thenReturn(activatedFirmwareVersion);
        when(this.firmwareService.newFirmwareVersion(deviceType, newActiveMeterFirmwareVersion, FirmwareStatus.GHOST, FirmwareType.METER)).thenReturn(firmwareVersion);

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
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.GHOST);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(this.firmwareService.getFirmwareVersionByVersionAndType(newActiveMeterFirmwareVersion, FirmwareType.METER, deviceType)).thenReturn(Optional.empty());
        FirmwareVersion communicationVersionWithSameName = mock(FirmwareVersion.class);
        when(communicationVersionWithSameName.getFirmwareVersion()).thenReturn(newActiveMeterFirmwareVersion);
        when(this.firmwareService.getFirmwareVersionByVersionAndType(newActiveMeterFirmwareVersion, FirmwareType.COMMUNICATION, deviceType)).thenReturn(Optional.of(communicationVersionWithSameName));
        ActivatedFirmwareVersion activatedFirmwareVersion = mock(ActivatedFirmwareVersion.class);
        when(this.firmwareService.newActivatedFirmwareVersionFrom(any(Device.class), any(FirmwareVersion.class), any(Interval.class))).thenReturn(activatedFirmwareVersion);
        when(this.firmwareService.newFirmwareVersion(deviceType, newActiveMeterFirmwareVersion, FirmwareStatus.GHOST, FirmwareType.METER)).thenReturn(firmwareVersion);

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
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.GHOST);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);
        when(this.firmwareService.getFirmwareVersionByVersionAndType(myActiveCommunicationFirmwareVersion, FirmwareType.COMMUNICATION, deviceType)).thenReturn(Optional.empty());
        ActivatedFirmwareVersion activatedFirmwareVersion = mock(ActivatedFirmwareVersion.class);
        when(this.firmwareService.newActivatedFirmwareVersionFrom(any(Device.class), any(FirmwareVersion.class), any(Interval.class))).thenReturn(activatedFirmwareVersion);
        when(this.firmwareService.newFirmwareVersion(deviceType, myActiveCommunicationFirmwareVersion, FirmwareStatus.GHOST, FirmwareType.COMMUNICATION)).thenReturn(firmwareVersion);

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
        FirmwareVersion firmwareVersion = mock(FirmwareVersion.class);
        when(firmwareVersion.getFirmwareStatus()).thenReturn(FirmwareStatus.GHOST);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.COMMUNICATION);
        when(this.firmwareService.getFirmwareVersionByVersionAndType(myActiveCommunicationFirmwareVersion, FirmwareType.COMMUNICATION, deviceType)).thenReturn(Optional.empty());
        FirmwareVersion meterVersionWithSameName = mock(FirmwareVersion.class);
        when(meterVersionWithSameName.getFirmwareVersion()).thenReturn(myActiveCommunicationFirmwareVersion);
        when(this.firmwareService.getFirmwareVersionByVersionAndType(myActiveCommunicationFirmwareVersion, FirmwareType.METER, deviceType)).thenReturn(Optional.of(meterVersionWithSameName));
        ActivatedFirmwareVersion activatedFirmwareVersion = mock(ActivatedFirmwareVersion.class);
        when(this.firmwareService.newActivatedFirmwareVersionFrom(any(Device.class), any(FirmwareVersion.class), any(Interval.class))).thenReturn(activatedFirmwareVersion);
        when(this.firmwareService.newFirmwareVersion(deviceType, myActiveCommunicationFirmwareVersion, FirmwareStatus.GHOST, FirmwareType.COMMUNICATION)).thenReturn(firmwareVersion);

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
}