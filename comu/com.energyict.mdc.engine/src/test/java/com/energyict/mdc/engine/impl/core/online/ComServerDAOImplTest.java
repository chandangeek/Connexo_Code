package com.energyict.mdc.engine.impl.core.online;

import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.comserver.time.Clocks;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.MdwInterface;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.common.BusinessEvent;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.communication.tasks.ServerComTaskExecutionFactory;
import com.energyict.mdc.communication.tasks.ServerConnectionTaskFactory;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OutboundCapableComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.ports.ComPortFactory;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.servers.ComServerFactory;
import com.energyict.test.MockEnvironmentTranslations;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-07 (10:02)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComServerDAOImplTest {

    private static final int COMSERVER_ID = 1;
    private static final int COMPORT_ID = 2;
    private static final long SCHEDULED_COMTASK_ID = 3;
    private static final int YEAR = 2012;
    private static final String IP_ADDRESS = "192.168.2.100";
    private static final String IP_ADDRESS_PROPERTY_NAME = "ipAddress";

    @ClassRule
    public static TestRule mockEnvironmentTranslactions = new MockEnvironmentTranslations();

    @Mock
    private ServerManager manager;
    @Mock
    private MdwInterface mdwInterface;
    @Mock
    private OutboundCapableComServer comServer;
    @Mock
    private ComServerFactory comServerFactory;
    @Mock
    private OutboundComPort comPort;
    @Mock
    private ComPortFactory comPortFactory;
    @Mock
    private ServerComTaskExecutionFactory comTaskExecutionFactory;
    @Mock
    private ServerConnectionTaskFactory connectionTaskFactory;
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

    private ComServerDAO comServerDAO = new ComServerDAOImpl(serviceProvider);

    public ComServerDAOImplTest() {
    }

    @Before
    public void initializeMocksAndFactories () throws SQLException, BusinessException {
        when(this.manager.getComServerFactory()).thenReturn(this.comServerFactory);
        when(this.manager.getComPortFactory()).thenReturn(this.comPortFactory);
        when(this.manager.getComTaskExecutionFactory()).thenReturn(this.comTaskExecutionFactory);
        when(this.manager.getConnectionTaskFactory()).thenReturn(this.connectionTaskFactory);
        when(this.manager.getMdwInterface()).thenReturn(this.mdwInterface);
        ManagerFactory.setCurrent(this.manager);
        this.mockMdwInterfaceTransactionExecutor();

        when(this.comServer.getId()).thenReturn(Long.valueOf(COMSERVER_ID));
        when(this.comServerFactory.find(COMSERVER_ID)).thenReturn(this.comServer);
        when(this.comServerFactory.findBySystemName()).thenReturn(this.comServer);
        when(this.comPort.getId()).thenReturn(Long.valueOf(COMPORT_ID));
        when(this.comPortFactory.find(COMPORT_ID)).thenReturn(this.comPort);
        when(this.scheduledComTask.getId()).thenReturn(SCHEDULED_COMTASK_ID);
        when(this.comTaskExecutionFactory.find((int) SCHEDULED_COMTASK_ID)).thenReturn(this.scheduledComTask);
    }

    @After
    public void resetTimeFactory () {
        Clocks.resetAll();
    }

    @Test
    public void testGetThisComServer () {
        // Business method and asserts
        assertThat(this.comServerDAO.getThisComServer()).isNotNull();
        verify(this.comServerFactory).findBySystemName();
    }

    @Test
    public void testRefreshComServerThatHasNotChanged () {
        Date modificationDate = new Date();
        when(this.comServer.getModificationDate()).thenReturn(modificationDate);
        OutboundCapableComServer reloaded = mock(OutboundCapableComServer.class);
        when(reloaded.getModificationDate()).thenReturn(modificationDate);
        when(this.comServerFactory.find((int) this.comServer.getId())).thenReturn(reloaded);

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
        when(this.comServerFactory.find(COMSERVER_ID)).thenReturn(changed);

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComServer(this.comServer)).isSameAs(changed);
    }

    @Test
    public void testRefreshComServerThatWasMadeObsolete () {
        ComServer obsolete = mock(ComServer.class);
        when(obsolete.isObsolete()).thenReturn(true);
        when(this.comServerFactory.find(COMSERVER_ID)).thenReturn(obsolete);

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComServer(this.comServer)).isNull();
    }

    @Test
    public void testRefreshComServerThatWasDeleted () {
        when(this.comServerFactory.find(COMSERVER_ID)).thenReturn(null);

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComServer(this.comServer)).isNull();
    }

    @Test
    public void testRefreshComPortThatHasNotChanged () {
        Date modificationDate = new Date();
        OutboundComPort reloaded = mock(OutboundComPort.class);
        when(reloaded.getModificationDate()).thenReturn(modificationDate);
        when(this.comPortFactory.find((int) this.comPort.getId())).thenReturn(reloaded);
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
        when(this.comPortFactory.find(COMPORT_ID)).thenReturn(changed);

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComPort(this.comPort)).isSameAs(changed);
    }

    @Test
    public void testRefreshComPortThatWasMadeObsolete () {
        ComPort obsolete = mock(ComPort.class);
        when(obsolete.isObsolete()).thenReturn(true);
        when(this.comPortFactory.find(COMPORT_ID)).thenReturn(obsolete);

        // Business method and asserts
        assertThat(this.comServerDAO.refreshComPort(this.comPort)).isNull();
    }

    @Test
    public void testRefreshComPortThatWasDeleted () {
        when(this.comPortFactory.find(COMPORT_ID)).thenReturn(null);

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

    @Test(expected = DataAccessException.class)
    public void testExecutionStartedWithSQLException () throws SQLException {
        doThrow(SQLException.class).when(this.scheduledComTask).executionStarted(this.comPort);

        // Business method
        this.comServerDAO.executionStarted(this.scheduledComTask, this.comPort);

        // Expected DataAccessException that wraps the SQLException
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
        ConnectionTask connectionTask = mock(ConnectionTask.class);

        // Business method
        this.comServerDAO.executionCompleted(connectionTask);

        // Asserts
        verify(connectionTask).executionCompleted();
    }

    @Test(expected = DataAccessException.class)
    public void testComTaskExecutionCompletedWithSQLException () throws SQLException, BusinessException {
        doThrow(SQLException.class).when(this.scheduledComTask).executionCompleted();

        // Business method
        this.comServerDAO.executionCompleted(this.scheduledComTask);

        // Expected DataAccessException that wraps the SQLException
    }

    @Test(expected = DataAccessException.class)
    public void testConnectionTaskExecutionCompletedWithSQLException () throws SQLException, BusinessException {
        ConnectionTask connectionTask = mock(ConnectionTask.class);

        doThrow(SQLException.class).when(connectionTask).executionCompleted();

        // Business method
        this.comServerDAO.executionCompleted(connectionTask);

        // Expected DataAccessException that wraps the SQLException
    }

    @Test
    public void testExecutionFailed () throws SQLException, BusinessException {
        // Business method
        this.comServerDAO.executionFailed(this.scheduledComTask);

        // Asserts
        verify(this.scheduledComTask).executionFailed();
    }

    @Test(expected = DataAccessException.class)
    public void testExecutionFailedWithSQLException () throws SQLException, BusinessException {
        doThrow(SQLException.class).when(this.scheduledComTask).executionFailed();

        // Business method
        this.comServerDAO.executionFailed(this.scheduledComTask);

        // Expected DataAccessException that wraps the SQLException
    }

    @Test
    public void testReleaseInterruptedComTasks () throws SQLException, BusinessException {
        // Business method
        this.comServerDAO.releaseInterruptedTasks(this.comServer);

        // Asserts
        verify(this.connectionTaskFactory).releaseInterruptedConnectionTasks(this.comServer);
        verify(this.comTaskExecutionFactory).releaseInterruptedComTasks(this.comServer);
    }

    @Test
    public void testReleaseTimedOutComTasks () throws SQLException, BusinessException {
        // Business method
        this.comServerDAO.releaseTimedOutTasks(this.comServer);

        // Asserts
        verify(this.connectionTaskFactory).releaseTimedOutConnectionTasks(this.comServer);
        verify(this.comTaskExecutionFactory).releaseTimedOutComTasks(this.comServer);
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
        when(this.comTaskExecutionFactory.isStillPending(id)).thenReturn(true);

        // Business method
        boolean stillPending = this.comServerDAO.isStillPending(id);

        // Asserts
        assertThat(stillPending).isTrue();
        verify(this.comTaskExecutionFactory).isStillPending(id);
    }

    @Test
    public void testAreStillPendingDelegatesToComTaskExecutionFactory () {
        int id1 = 97;
        int id2 = 101;
        int id3 = 103;
        List<Integer> comTaskExecutionIds = Arrays.asList(id1, id2, id3);
        when(this.comTaskExecutionFactory.areStillPending(comTaskExecutionIds)).thenReturn(true);

        // Business method
        boolean stillPending = this.comServerDAO.areStillPending(comTaskExecutionIds);

        // Asserts
        assertThat(stillPending).isTrue();
        verify(this.comTaskExecutionFactory).areStillPending(comTaskExecutionIds);
    }

    @Test
    public void testUpdateIpAddress () throws SQLException, BusinessException {
        TypedProperties properties = mock(TypedProperties.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
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

    private void mockMdwInterfaceTransactionExecutor () throws BusinessException, SQLException {
        this.mockMdwInterfaceTransactionExecutor(this.mdwInterface);
    }

    private void mockMdwInterfaceTransactionExecutor (MdwInterface mdwInterface) throws BusinessException, SQLException {
        when(mdwInterface.execute(any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer (InvocationOnMock invocation) throws Throwable {
                Transaction transaction = (Transaction) invocation.getArguments()[0];
                transaction.doExecute();
                return null;
            }
        });
    }

}