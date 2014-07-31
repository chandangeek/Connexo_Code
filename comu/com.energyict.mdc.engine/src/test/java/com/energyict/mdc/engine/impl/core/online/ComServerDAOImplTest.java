package com.energyict.mdc.engine.impl.core.online;

import com.energyict.mdc.common.BusinessEvent;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundCapableComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import com.elster.jupiter.transaction.TransactionService;
import com.google.common.base.Optional;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
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
    private ServerComTaskExecution scheduledComTask;
    @Mock
    private BaseDevice device;
    @Mock
    private DeviceIdentifier deviceIdentifier;
    @Mock
    private DeviceIdentifier gatewayDeviceIdentifier;
    @Mock
    private BusinessEvent businessEvent;
    @Mock
    private EngineModelService engineModelService;
    @Mock
    private DeviceDataService deviceDataService;

    private final FakeServiceProvider serviceProvider = new FakeServiceProvider();
    private TransactionService transactionService;

    private ComServerDAO comServerDAO = new ComServerDAOImpl(serviceProvider);

    public ComServerDAOImplTest() {
    }

    @Before
    public void setupServiceProvider () {
        this.transactionService = new FakeTransactionService();
        this.serviceProvider.setTransactionService(this.transactionService);
        this.serviceProvider.setEngineModelService(this.engineModelService);
        this.serviceProvider.setDeviceDataService(this.deviceDataService);
        when(this.engineModelService.findComServerBySystemName()).thenReturn(Optional.<ComServer>of(this.comServer));
        when(this.engineModelService.findComServer(COMSERVER_ID)).thenReturn(Optional.<ComServer>of(this.comServer));
        when(this.engineModelService.findComPort(COMPORT_ID)).thenReturn(this.comPort);
        when(this.deviceDataService.findComTaskExecution(SCHEDULED_COMTASK_ID)).thenReturn(this.scheduledComTask);
        when(this.comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.comPort.getId()).thenReturn(COMPORT_ID);
        when(this.scheduledComTask.getId()).thenReturn(SCHEDULED_COMTASK_ID);
    }

    @Before
    public void initializeMocks () throws SQLException, BusinessException {
        when(this.comServer.getId()).thenReturn(Long.valueOf(COMSERVER_ID));
        when(this.comPort.getId()).thenReturn(Long.valueOf(COMPORT_ID));
        when(this.scheduledComTask.getId()).thenReturn(SCHEDULED_COMTASK_ID);
    }


    @Test
    public void testGetThisComServer () {
        // Business method and asserts
        assertThat(this.comServerDAO.getThisComServer()).isNotNull();
        verify(this.engineModelService).findComServerBySystemName();
    }

    @Test
    public void testRefreshComServerThatHasNotChanged () {
        Date modificationDate = new Date();
        when(this.comServer.getModificationDate()).thenReturn(modificationDate);
        OutboundCapableComServer reloaded = mock(OutboundCapableComServer.class);
        when(reloaded.getModificationDate()).thenReturn(modificationDate);
        when(this.engineModelService.findComServer(COMSERVER_ID)).thenReturn(Optional.<ComServer>of(reloaded));

        // Business method and asserts
        ComServer refreshed = this.comServerDAO.refreshComServer(this.comServer);
        assertThat(refreshed).isNotNull();
        assertThat(refreshed).isSameAs(this.comServer);
    }

    @Test
    public void testRefreshComServerThatChanged () {
        ComServer changed = mock(ComServer.class);
        Date january1st2012 = this.newDate(YEAR, Calendar.JANUARY, 1);
        Date february1st2012 = this.newDate(YEAR, Calendar.FEBRUARY, 1);
        when(this.comServer.getModificationDate()).thenReturn(january1st2012);
        when(changed.getModificationDate()).thenReturn(february1st2012);
        when(this.engineModelService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(changed));

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComServer(this.comServer)).isSameAs(changed);
    }

    @Test
    public void testRefreshComServerThatWasMadeObsolete () {
        ComServer obsolete = mock(ComServer.class);
        when(obsolete.isObsolete()).thenReturn(true);
        when(this.engineModelService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(obsolete));

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComServer(this.comServer)).isNull();
    }

    @Test
    public void testRefreshComServerThatWasDeleted () {
        when(this.engineModelService.findComServer(COMSERVER_ID)).thenReturn(Optional.<ComServer>absent());

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComServer(this.comServer)).isNull();
    }

    @Test
    public void testRefreshComPortThatHasNotChanged () {
        Date modificationDate = new Date();
        OutboundComPort reloaded = mock(OutboundComPort.class);
        when(reloaded.getModificationDate()).thenReturn(modificationDate);
        when(this.engineModelService.findComPort(COMPORT_ID)).thenReturn(reloaded);
        when(this.comPort.getModificationDate()).thenReturn(modificationDate);

        // Business method and asserts
        ComPort refreshed = this.comServerDAO.refreshComPort(this.comPort);
        assertThat(refreshed).isNotNull();
        assertThat(refreshed).isSameAs(this.comPort);
    }

    @Test
    public void testRefreshComPortThatChanged () {
        OutboundComPort changed = mock(OutboundComPort.class);
        Date january1st2012 = this.newDate(YEAR, Calendar.JANUARY, 1);
        Date february1st2012 = this.newDate(YEAR, Calendar.FEBRUARY, 1);
        when(this.comPort.getModificationDate()).thenReturn(january1st2012);
        when(changed.getModificationDate()).thenReturn(february1st2012);
        when(this.engineModelService.findComPort(COMPORT_ID)).thenReturn(changed);

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComPort(this.comPort)).isSameAs(changed);
    }

    @Test
    public void testRefreshComPortThatWasMadeObsolete () {
        ComPort obsolete = mock(ComPort.class);
        when(obsolete.isObsolete()).thenReturn(true);
        when(this.engineModelService.findComPort(COMPORT_ID)).thenReturn(obsolete);

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComPort(this.comPort)).isNull();
    }

    @Test
    public void testRefreshComPortThatWasDeleted () {
        when(this.engineModelService.findComPort(COMPORT_ID)).thenReturn(null);

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComPort(this.comPort)).isNull();
    }

    @Test
    public void testExecutionStarted () throws SQLException {
        // Business method
        this.comServerDAO.executionStarted(this.scheduledComTask, this.comPort);

        // Asserts
        verify(this.scheduledComTask).executionStarted(this.comPort);
    }

    @Test
    public void testComTaskExecutionCompleted () throws SQLException, BusinessException {
        // Business method
        this.comServerDAO.executionCompleted(this.scheduledComTask);

        // Asserts
        verify(this.scheduledComTask).executionCompleted();
    }

    @Test
    public void testConnectionTaskExecutionCompleted () throws SQLException, BusinessException {
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);

        // Business method
        this.comServerDAO.executionCompleted(connectionTask);

        // Asserts
        verify(connectionTask).executionCompleted();
    }

    @Test
    public void testExecutionFailed () throws SQLException, BusinessException {
        // Business method
        this.comServerDAO.executionFailed(this.scheduledComTask);

        // Asserts
        verify(this.scheduledComTask).executionFailed();
    }

    public void testReleaseInterruptedComTasks () throws SQLException, BusinessException {
        // Business method
        this.comServerDAO.releaseInterruptedTasks(this.comServer);

        // Asserts
        verify(this.deviceDataService).releaseInterruptedConnectionTasks(this.comServer);
        verify(this.deviceDataService).releaseInterruptedComTasks(this.comServer);
    }

    @Test
    public void testReleaseTimedOutComTasks () throws SQLException, BusinessException {
        // Business method
        this.comServerDAO.releaseTimedOutTasks(this.comServer);

        // Asserts
        verify(this.deviceDataService).releaseTimedOutConnectionTasks(this.comServer);
        verify(this.deviceDataService).releaseTimedOutComTasks(this.comServer);
    }

    @Test
    public void testUpdateGateway_GatewayRemoved() throws Exception {
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);
        when(this.gatewayDeviceIdentifier.findDevice()).thenReturn(null);

        // Business method
        this.comServerDAO.updateGateway(this.deviceIdentifier, this.gatewayDeviceIdentifier);

        // Asserts
        verify(this.device).setPhysicalGateway(null);
    }

    @Test
    public void testUpdateGateway_DifferentGateway() throws Exception {
        when(this.deviceIdentifier.findDevice()).thenReturn(this.device);
        when(this.gatewayDeviceIdentifier.findDevice()).thenReturn(this.device);

        // Business method
        this.comServerDAO.updateGateway(this.deviceIdentifier, this.gatewayDeviceIdentifier);

        // Asserts
        verify(this.device).setPhysicalGateway(this.device);
    }

    @Test
    public void testIsStillPendingDelegatesToComTaskExecutionFactory () {
        int id = 97;
        when(this.deviceDataService.areComTasksStillPending(anyList())).thenReturn(true);

        // Business method
        boolean stillPending = this.comServerDAO.isStillPending(id);

        // Asserts
        assertThat(stillPending).isTrue();
        verify(this.deviceDataService).areComTasksStillPending(anyList());
    }

    @Test
    public void testAreStillPendingDelegatesToComTaskExecutionFactory () {
        long id1 = 97;
        long id2 = 101;
        long id3 = 103;
        List<Long> comTaskExecutionIds = Arrays.asList(id1, id2, id3);
        when(this.deviceDataService.areComTasksStillPending(comTaskExecutionIds)).thenReturn(true);

        // Business method
        boolean stillPending = this.comServerDAO.areStillPending(comTaskExecutionIds);

        // Asserts
        assertThat(stillPending).isTrue();
        verify(this.deviceDataService).areComTasksStillPending(comTaskExecutionIds);
    }

    @Test
    public void testUpdateIpAddress () throws SQLException, BusinessException {
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

    private Date newDate (int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

}