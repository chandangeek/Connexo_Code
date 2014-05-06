package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.DatabaseException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.comserver.core.ComServerDAO;
import com.energyict.comserver.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.comserver.core.ServerProcess;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.comserver.exceptions.CodingException;
import com.energyict.comserver.time.Clocks;
import com.energyict.comserver.time.FrozenClock;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.MdwInterface;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.engine.impl.core.remote.QueryMethod;
import com.energyict.mdc.ports.ComPortFactory;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.servers.ComServerFactory;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.communication.tasks.ServerComTaskExecutionFactory;
import com.energyict.mdc.communication.tasks.ServerConnectionTaskFactory;
import org.junit.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.remote.QueryMethod} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-21 (15:30)
 */
public class QueryMethodTest {

    private static final int COMSERVER_ID = 951;
    private static final int COMPORT_ID = COMSERVER_ID + 1;
    private static final int COMTASKEXECUTION_ID = COMPORT_ID + 1;
    private static final int CONNECTIONTASK_ID = COMTASKEXECUTION_ID + 1;

    @Test
    public void testAllComServerDAOMethodsHaveAnEnumElement () {
        for (Method method : this.getComServerDAOMethods()) {
            // Business method
            QueryMethod queryMethod = QueryMethod.byName(method.getName());

            // Asserts
            Assertions.assertThat(queryMethod).as(method.toString() + " is missing as an enum element in QueryMethod").isNotNull();
        }
    }

    @Test
    public void testMessageNotUnderstood () {
        Assertions.assertThat(QueryMethod.byName("anythingbutAnExistingMethodOfTheComServerDAOInterface")).isEqualTo(QueryMethod.MessageNotUnderstood);
    }

    @Test
    public void testGetThisComServerDelegation () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);

        // Business method
        QueryMethod.GetThisComServer.execute(new HashMap<String, Object>(), comServerDAO);

        // Asserts
        verify(comServerDAO).getThisComServer();
    }

    @Test
    public void testGetComServerDelegation () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        String hostName = "testGetComServerDelegation";
        parameters.put(RemoteComServerQueryJSonPropertyNames.HOSTNAME, hostName);
        QueryMethod.GetComServer.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).getComServer(hostName);
    }

    @Test
    public void testRefreshComServerWithoutModifications () throws IOException {
        Date now = Clocks.getAppServerClock().now();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComServerFactory comServerFactory = mock(ComServerFactory.class);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(comServer.getModificationDate()).thenReturn(now);
        when(comServerFactory.find(COMSERVER_ID)).thenReturn(comServer);
        when(manager.getComServerFactory()).thenReturn(comServerFactory);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE, now.getTime());
        Object refreshed = QueryMethod.RefreshComServer.execute(parameters, comServerDAO);

        // Asserts
        Assertions.assertThat(refreshed).isNull();
    }

    @Test
    public void testRefreshComServerWithModifications () throws IOException {
        Date modificationDateBeforeChanges = FrozenClock.frozenOn(2013, Calendar.APRIL, 29, 11, 56, 8, 0).now();
        Date modificationDateAfterChanges = FrozenClock.frozenOn(2013, Calendar.APRIL, 29, 12, 13, 59, 0).now();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComServerFactory comServerFactory = mock(ComServerFactory.class);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(comServer.getModificationDate()).thenReturn(modificationDateAfterChanges);
        when(comServerFactory.find(COMSERVER_ID)).thenReturn(comServer);
        when(manager.getComServerFactory()).thenReturn(comServerFactory);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE, modificationDateBeforeChanges.getTime());
        Object refreshed = QueryMethod.RefreshComServer.execute(parameters, comServerDAO);

        // Asserts
        Assertions.assertThat(refreshed).isNotNull();
        Assertions.assertThat(refreshed).isSameAs(comServer);
    }

    @Test
    public void testRefreshComPortWithoutModifications () throws IOException {
        Date now = Clocks.getAppServerClock().now();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComPortFactory comPortFactory = mock(ComPortFactory.class);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.getModificationDate()).thenReturn(now);
        when(comPortFactory.find(COMPORT_ID)).thenReturn(comPort);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, COMPORT_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE, now.getTime());
        Object refreshed = QueryMethod.RefreshComPort.execute(parameters, comServerDAO);

        // Asserts
        Assertions.assertThat(refreshed).isNull();
    }

    @Test
    public void testRefreshComPortWithModifications () throws IOException {
        Date modificationDateBeforeChanges = FrozenClock.frozenOn(2013, Calendar.APRIL, 29, 11, 56, 8, 0).now();
        Date modificationDateAfterChanges = FrozenClock.frozenOn(2013, Calendar.APRIL, 29, 12, 13, 59, 0).now();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComPortFactory comPortFactory = mock(ComPortFactory.class);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.getModificationDate()).thenReturn(modificationDateAfterChanges);
        when(comPortFactory.find(COMPORT_ID)).thenReturn(comPort);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, COMPORT_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE, modificationDateBeforeChanges.getTime());
        Object refreshed = QueryMethod.RefreshComPort.execute(parameters, comServerDAO);

        // Asserts
        Assertions.assertThat(refreshed).isNotNull();
        Assertions.assertThat(refreshed).isSameAs(comPort);
    }

    @Test
    public void testComTaskExecutionStarted () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComPortFactory comPortFactory = mock(ComPortFactory.class);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPortFactory.find(COMPORT_ID)).thenReturn(comPort);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        ServerComTaskExecutionFactory comTaskExecutionFactory = mock(ServerComTaskExecutionFactory.class);
        when(comTaskExecutionFactory.find(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);
        when(manager.getComTaskExecutionFactory()).thenReturn(comTaskExecutionFactory);
        MdwInterface mdwInterface = this.mdwInterfaceWithDefaultTransactionExecutor();
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, COMPORT_ID);
        QueryMethod.ExecutionStarted.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).executionStarted(comTaskExecution, comPort);
    }

    @Test
    public void testComTaskExecutionStartedRunsInTransaction () throws IOException, SQLException, BusinessException {
        MdwInterface mdwInterface = mock(MdwInterface.class);
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComPortFactory comPortFactory = mock(ComPortFactory.class);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPortFactory.find(COMPORT_ID)).thenReturn(comPort);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        ServerComTaskExecutionFactory comTaskExecutionFactory = mock(ServerComTaskExecutionFactory.class);
        when(comTaskExecutionFactory.find(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);
        when(manager.getComTaskExecutionFactory()).thenReturn(comTaskExecutionFactory);
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, COMPORT_ID);
        QueryMethod.ExecutionStarted.execute(parameters, comServerDAO);

        // Asserts
        verify(mdwInterface).execute(any(Transaction.class));
    }

    @Test
    public void testAttemptLockOfComTaskExecution () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComPortFactory comPortFactory = mock(ComPortFactory.class);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPortFactory.find(COMPORT_ID)).thenReturn(comPort);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        ServerComTaskExecutionFactory comTaskExecutionFactory = mock(ServerComTaskExecutionFactory.class);
        when(comTaskExecutionFactory.find(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);
        when(manager.getComTaskExecutionFactory()).thenReturn(comTaskExecutionFactory);
        MdwInterface mdwInterface = this.mdwInterfaceWithDefaultTransactionExecutor();
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, COMPORT_ID);
        QueryMethod.AttemptLock.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).attemptLock(comTaskExecution, comPort);
    }

    @Test
    public void testAttemptLockOfComTaskExecutionRunsInTransaction () throws IOException, SQLException, BusinessException {
        MdwInterface mdwInterface = mock(MdwInterface.class);
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComPortFactory comPortFactory = mock(ComPortFactory.class);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPortFactory.find(COMPORT_ID)).thenReturn(comPort);
        when(manager.getComPortFactory()).thenReturn(comPortFactory);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        ServerComTaskExecutionFactory comTaskExecutionFactory = mock(ServerComTaskExecutionFactory.class);
        when(comTaskExecutionFactory.find(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);
        when(manager.getComTaskExecutionFactory()).thenReturn(comTaskExecutionFactory);
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, COMPORT_ID);
        QueryMethod.AttemptLock.execute(parameters, comServerDAO);

        // Asserts
        verify(mdwInterface).execute(any(Transaction.class));
    }

    @Test
    public void testUnlockOfComTaskExecution () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        ServerComTaskExecutionFactory comTaskExecutionFactory = mock(ServerComTaskExecutionFactory.class);
        when(comTaskExecutionFactory.find(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);
        when(manager.getComTaskExecutionFactory()).thenReturn(comTaskExecutionFactory);
        MdwInterface mdwInterface = this.mdwInterfaceWithDefaultTransactionExecutor();
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.Unlock.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).unlock(comTaskExecution);
    }

    @Test
    public void testUnlockOfComTaskExecutionRunsInTransaction () throws IOException, SQLException, BusinessException {
        MdwInterface mdwInterface = mock(MdwInterface.class);
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        ServerComTaskExecutionFactory comTaskExecutionFactory = mock(ServerComTaskExecutionFactory.class);
        when(comTaskExecutionFactory.find(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);
        when(manager.getComTaskExecutionFactory()).thenReturn(comTaskExecutionFactory);
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.Unlock.execute(parameters, comServerDAO);

        // Asserts
        verify(mdwInterface).execute(any(Transaction.class));
    }

    @Test
    public void testConnectionTaskExecutionStarted () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        ComServerFactory comServerFactory = mock(ComServerFactory.class);
        when(comServerFactory.find(COMSERVER_ID)).thenReturn(comServer);
        when(manager.getComServerFactory()).thenReturn(comServerFactory);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        when(connectionTaskFactory.find(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        MdwInterface mdwInterface = this.mdwInterfaceWithDefaultTransactionExecutor();
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionStarted.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).executionStarted(connectionTask, comServer);
    }

    @Test
    public void testConnectionTaskExecutionStartedRunsInTransaction () throws IOException, SQLException, BusinessException {
        MdwInterface mdwInterface = mock(MdwInterface.class);
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        ComServerFactory comServerFactory = mock(ComServerFactory.class);
        when(comServerFactory.find(COMSERVER_ID)).thenReturn(comServer);
        when(manager.getComServerFactory()).thenReturn(comServerFactory);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        when(connectionTaskFactory.find(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionStarted.execute(parameters, comServerDAO);

        // Asserts
        verify(mdwInterface).execute(any(Transaction.class));
    }

    @Test
    public void testAttemptLockOfConnectionTask () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        ComServerFactory comServerFactory = mock(ComServerFactory.class);
        when(comServerFactory.find(COMSERVER_ID)).thenReturn(comServer);
        when(manager.getComServerFactory()).thenReturn(comServerFactory);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        when(connectionTaskFactory.find(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(connectionTaskFactory.findOutbound(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        MdwInterface mdwInterface = this.mdwInterfaceWithDefaultTransactionExecutor();
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.AttemptLock.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).attemptLock(connectionTask, comServer);
    }

    @Test
    public void testAttemptLockOfConnectionTaskRunsInTransaction () throws IOException, SQLException, BusinessException {
        MdwInterface mdwInterface = mock(MdwInterface.class);
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        ComServerFactory comServerFactory = mock(ComServerFactory.class);
        when(comServerFactory.find(COMSERVER_ID)).thenReturn(comServer);
        when(manager.getComServerFactory()).thenReturn(comServerFactory);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        when(connectionTaskFactory.find(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.AttemptLock.execute(parameters, comServerDAO);

        // Asserts
        verify(mdwInterface).execute(any(Transaction.class));
    }

    @Test
    public void testUnlockOfConnectionTask () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        when(connectionTaskFactory.find(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(connectionTaskFactory.findOutbound(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        MdwInterface mdwInterface = this.mdwInterfaceWithDefaultTransactionExecutor();
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.Unlock.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).unlock(connectionTask);
    }

    @Test
    public void testUnlockOfConnectionTaskRunsInTransaction () throws IOException, SQLException, BusinessException {
        MdwInterface mdwInterface = mock(MdwInterface.class);
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        ComServerFactory comServerFactory = mock(ComServerFactory.class);
        when(comServerFactory.find(COMSERVER_ID)).thenReturn(comServer);
        when(manager.getComServerFactory()).thenReturn(comServerFactory);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        when(connectionTaskFactory.find(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(connectionTaskFactory.findOutbound(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.Unlock.execute(parameters, comServerDAO);

        // Asserts
        verify(mdwInterface).execute(any(Transaction.class));
    }

    @Test
    public void testComTaskExecutionCompleted () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        ServerComTaskExecutionFactory comTaskExecutionFactory = mock(ServerComTaskExecutionFactory.class);
        when(comTaskExecutionFactory.find(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);
        when(manager.getComTaskExecutionFactory()).thenReturn(comTaskExecutionFactory);
        MdwInterface mdwInterface = this.mdwInterfaceWithDefaultTransactionExecutor();
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.ExecutionCompleted.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).executionCompleted(comTaskExecution);
    }

    @Test
    public void testComTaskExecutionCompletedRunsInTransaction () throws IOException, SQLException, BusinessException {
        MdwInterface mdwInterface = mock(MdwInterface.class);
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        ServerComTaskExecutionFactory comTaskExecutionFactory = mock(ServerComTaskExecutionFactory.class);
        when(comTaskExecutionFactory.find(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);
        when(manager.getComTaskExecutionFactory()).thenReturn(comTaskExecutionFactory);
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.ExecutionCompleted.execute(parameters, comServerDAO);

        // Asserts
        verify(mdwInterface).execute(any(Transaction.class));
    }

    @Test
    public void testComTaskExecutionFailed () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        ServerComTaskExecutionFactory comTaskExecutionFactory = mock(ServerComTaskExecutionFactory.class);
        when(comTaskExecutionFactory.find(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);
        when(manager.getComTaskExecutionFactory()).thenReturn(comTaskExecutionFactory);
        MdwInterface mdwInterface = this.mdwInterfaceWithDefaultTransactionExecutor();
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.ExecutionFailed.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).executionFailed(comTaskExecution);
    }

    @Test
    public void testComTaskExecutionFailedRunsInTransaction () throws IOException, SQLException, BusinessException {
        MdwInterface mdwInterface = mock(MdwInterface.class);
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        ServerComTaskExecutionFactory comTaskExecutionFactory = mock(ServerComTaskExecutionFactory.class);
        when(comTaskExecutionFactory.find(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);
        when(manager.getComTaskExecutionFactory()).thenReturn(comTaskExecutionFactory);
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.ExecutionFailed.execute(parameters, comServerDAO);

        // Asserts
        verify(mdwInterface).execute(any(Transaction.class));
    }

    @Test
    public void testConnectionTaskExecutionCompleted () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(connectionTaskFactory.find(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(connectionTaskFactory.findOutbound(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        MdwInterface mdwInterface = this.mdwInterfaceWithDefaultTransactionExecutor();
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionCompleted.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).executionCompleted(connectionTask);
    }

    @Test
    public void testConnectionTaskExecutionCompletedRunsInTransaction () throws IOException, SQLException, BusinessException {
        MdwInterface mdwInterface = mock(MdwInterface.class);
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(connectionTaskFactory.find(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(connectionTaskFactory.findOutbound(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionCompleted.execute(parameters, comServerDAO);

        // Asserts
        verify(mdwInterface).execute(any(Transaction.class));
    }

    @Test
    public void testConnectionTaskExecutionFailed () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(connectionTaskFactory.find(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(connectionTaskFactory.findOutbound(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        MdwInterface mdwInterface = this.mdwInterfaceWithDefaultTransactionExecutor();
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionFailed.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).executionFailed(connectionTask);
    }

    @Test
    public void testConnectionTaskExecutionFailedRunsInTransaction () throws IOException, SQLException, BusinessException {
        MdwInterface mdwInterface = mock(MdwInterface.class);
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ServerConnectionTaskFactory connectionTaskFactory = mock(ServerConnectionTaskFactory.class);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(connectionTaskFactory.find(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(connectionTaskFactory.findOutbound(CONNECTIONTASK_ID)).thenReturn(connectionTask);
        when(manager.getConnectionTaskFactory()).thenReturn(connectionTaskFactory);
        when(manager.getMdwInterface()).thenReturn(mdwInterface);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionFailed.execute(parameters, comServerDAO);

        // Asserts
        verify(mdwInterface).execute(any(Transaction.class));
    }

    @Test
    public void testReleaseInterruptedComTasks () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComServerFactory comServerFactory = mock(ComServerFactory.class);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(comServerFactory.find(COMSERVER_ID)).thenReturn(comServer);
        when(manager.getComServerFactory()).thenReturn(comServerFactory);
        ManagerFactory.setCurrent(manager);

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        QueryMethod.ReleaseInterruptedComTasks.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).releaseInterruptedTasks(comServer);
    }

    @Test
    public void testReleaseTimedOutComTasks () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ServerManager manager = mock(ServerManager.class);
        ComServerFactory comServerFactory = mock(ComServerFactory.class);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(comServerFactory.find(COMSERVER_ID)).thenReturn(comServer);
        when(manager.getComServerFactory()).thenReturn(comServerFactory);
        ManagerFactory.setCurrent(manager);
        when(comServerDAO.releaseTimedOutTasks(comServer)).thenReturn(new TimeDuration(951));

        // Business method
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        QueryMethod.ReleaseTimedOutComTasks.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).releaseTimedOutTasks(comServer);
    }

    private List<Method> getComServerDAOMethods () {
        ArrayList<Method> methods = new ArrayList<>();
        this.collectMethods(ComServerDAO.class, methods);
        return methods;
    }

    private void collectMethods (Class clazz, List<Method> methods) {
        if (!clazz.equals(ServerProcess.class)) {
            Collections.addAll(methods, clazz.getDeclaredMethods());
            if (clazz.getSuperclass() != null) {
                this.collectMethods(clazz.getSuperclass(), methods);
            }
            for (Class anInterface : clazz.getInterfaces()) {
                this.collectMethods(anInterface, methods);
            }
        }
    }

    private MdwInterface mdwInterfaceWithDefaultTransactionExecutor () {
        MdwInterface mdwInterface = mock(MdwInterface.class);
        try {
            when(mdwInterface.execute(any(Transaction.class))).thenAnswer(new Answer<Object>() {
                @Override
                public Object answer (InvocationOnMock invocation) throws Throwable {
                    Object transaction = invocation.getArguments()[0];
                    return ((Transaction) transaction).doExecute();
                }
            });
        }
        catch (BusinessException e) {
            // Code is not really executing yet so not expecting the exception for now
            CodingException.unexpectedBusinessException(e);
        }
        catch (SQLException e) {
            throw new DatabaseException(e);
        }
        return mdwInterface;
    }

}