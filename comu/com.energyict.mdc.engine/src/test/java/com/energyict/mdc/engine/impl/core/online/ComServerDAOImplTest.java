package com.energyict.mdc.engine.impl.core.online;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OutboundCapableComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import com.elster.jupiter.transaction.TransactionService;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private static final int YEAR = 2012;
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

    private final FakeServiceProvider serviceProvider = new FakeServiceProvider();
    private TransactionService transactionService;

    private ComServerDAO comServerDAO = new ComServerDAOImpl(serviceProvider);

    public ComServerDAOImplTest() {
    }

    @Before
    public void setupServiceProvider() {
        this.transactionService = new FakeTransactionService();
        this.serviceProvider.setTransactionService(this.transactionService);
        this.serviceProvider.setEngineConfigurationService(this.engineConfigurationService);
        this.serviceProvider.setConnectionTaskService(this.connectionTaskService);
        this.serviceProvider.setCommunicationTaskService(this.communicationTaskService);
        this.serviceProvider.setTopologyService(this.topologyService);
        when(this.engineConfigurationService.findComServerBySystemName()).thenReturn(Optional.<ComServer>of(this.comServer));
        when(this.engineConfigurationService.findComServer(COMSERVER_ID)).thenReturn(Optional.<ComServer>of(this.comServer));
        doReturn(Optional.of(this.comPort)).when(this.engineConfigurationService).findComPort(COMPORT_ID);
        when(this.communicationTaskService.findComTaskExecution(SCHEDULED_COMTASK_ID)).thenReturn(Optional.of(this.scheduledComTask));
        when(this.comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.comPort.getId()).thenReturn(COMPORT_ID);
        when(this.scheduledComTask.getId()).thenReturn(SCHEDULED_COMTASK_ID);
    }

    @Before
    public void initializeMocks() throws SQLException, BusinessException {
        when(this.comServer.getId()).thenReturn(Long.valueOf(COMSERVER_ID));
        when(this.comPort.getId()).thenReturn(Long.valueOf(COMPORT_ID));
        when(this.scheduledComTask.getId()).thenReturn(SCHEDULED_COMTASK_ID);
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
        Date january1st2012 = this.newDate(YEAR, Calendar.JANUARY, 1);
        Date february1st2012 = this.newDate(YEAR, Calendar.FEBRUARY, 1);
        when(this.comServer.getModificationDate()).thenReturn(january1st2012.toInstant());
        when(changed.getModificationDate()).thenReturn(february1st2012.toInstant());
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
    public void testRefreshComPortThatHasNotChanged() {
        Instant modificationDate = Instant.now();
        OutboundComPort reloaded = mock(OutboundComPort.class);
        when(reloaded.getModificationDate()).thenReturn(modificationDate);
        doReturn(Optional.of(reloaded)).when(this.engineConfigurationService).findComPort(COMPORT_ID);
        when(this.comPort.getModificationDate()).thenReturn(modificationDate);

        // Business method and asserts
        ComPort refreshed = this.comServerDAO.refreshComPort(this.comPort);
        assertThat(refreshed).isNotNull();
        assertThat(refreshed).isSameAs(this.comPort);
    }

    @Test
    public void testRefreshComPortThatChanged() {
        OutboundComPort changed = mock(OutboundComPort.class);
        Date january1st2012 = this.newDate(YEAR, Calendar.JANUARY, 1);
        Date february1st2012 = this.newDate(YEAR, Calendar.FEBRUARY, 1);
        when(this.comPort.getModificationDate()).thenReturn(january1st2012.toInstant());
        when(changed.getModificationDate()).thenReturn(february1st2012.toInstant());
        doReturn(Optional.of(changed)).when(this.engineConfigurationService).findComPort(COMPORT_ID);

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComPort(this.comPort)).isSameAs(changed);
    }

    @Test
    public void testRefreshComPortThatWasMadeObsolete() {
        ComPort obsolete = mock(ComPort.class);
        when(obsolete.isObsolete()).thenReturn(true);
        doReturn(Optional.of(obsolete)).when(this.engineConfigurationService).findComPort(COMPORT_ID);

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComPort(this.comPort)).isNull();
    }

    @Test
    public void testRefreshComPortThatWasDeleted() {
        when(this.engineConfigurationService.findComPort(COMPORT_ID)).thenReturn(Optional.empty());

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComPort(this.comPort)).isNull();
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

    private Date newDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

}