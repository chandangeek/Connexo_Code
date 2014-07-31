package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.impl.core.ServerProcess;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.base.Optional;
import org.joda.time.DateTime;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
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
@RunWith(MockitoJUnitRunner.class)
public class QueryMethodTest {

    private static final long COMSERVER_ID = 951;
    private static final long COMPORT_ID = COMSERVER_ID + 1;
    private static final long COMTASKEXECUTION_ID = COMPORT_ID + 1;
    private static final long CONNECTIONTASK_ID = COMTASKEXECUTION_ID + 1;

    @Mock
    private DeviceDataService deviceDataService;
    @Mock
    private EngineModelService engineModelService;

    private TransactionService transactionService;
    private FakeServiceProvider serviceProvider;

    @Before
    public void setupServiceProvider () {
        this.transactionService = new FakeTransactionService();
        this.serviceProvider = new FakeServiceProvider();
        this.serviceProvider.setTransactionService(this.transactionService);
        this.serviceProvider.setDeviceDataService(this.deviceDataService);
        this.serviceProvider.setEngineModelService(this.engineModelService);
        ServiceProvider.instance.set(this.serviceProvider);
    }

    private void mockTransactionService () {
        this.transactionService = mock(TransactionService.class);
        this.serviceProvider.setTransactionService(this.transactionService);
    }

    @Test
    public void testAllComServerDAOMethodsHaveAnEnumElement () {
        for (Method method : this.getComServerDAOMethods()) {
            // Business method
            QueryMethod queryMethod = QueryMethod.byName(method.getName());

            // Asserts
            assertThat(queryMethod).as(method.toString() + " is missing as an enum element in QueryMethod").isNotNull();
        }
    }

    @Test
    public void testMessageNotUnderstood () {
        assertThat(QueryMethod.byName("anythingbutAnExistingMethodOfTheComServerDAOInterface")).isEqualTo(QueryMethod.MessageNotUnderstood);
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
        Map<String, Object> parameters = new HashMap<>();
        String hostName = "testGetComServerDelegation";
        parameters.put(RemoteComServerQueryJSonPropertyNames.HOSTNAME, hostName);
        QueryMethod.GetComServer.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).getComServer(hostName);
    }

    @Test
    public void testRefreshComServerWithoutModifications () throws IOException {
        Date now = new Date();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(comServer.getModificationDate()).thenReturn(now);
        when(this.engineModelService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE, now.getTime());
        Object refreshed = QueryMethod.RefreshComServer.execute(parameters, comServerDAO);

        // Asserts
        assertThat(refreshed).isNull();
    }

    @Test
    public void testRefreshComServerWithModifications () throws IOException {
        Date modificationDateBeforeChanges = new DateTime(2013, 4, 29, 11, 56, 8, 0).toDate();
        Date modificationDateAfterChanges = new DateTime(2013, 4, 29, 12, 13, 59, 0).toDate();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(comServer.getModificationDate()).thenReturn(modificationDateAfterChanges);
        when(this.engineModelService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE, modificationDateBeforeChanges.getTime());
        Object refreshed = QueryMethod.RefreshComServer.execute(parameters, comServerDAO);

        // Asserts
        assertThat(refreshed).isNotNull();
        assertThat(refreshed).isSameAs(comServer);
    }

    @Test
    public void testRefreshComPortWithoutModifications () throws IOException {
        Date now = new Date();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.getModificationDate()).thenReturn(now);
        when(this.engineModelService.findComPort(COMPORT_ID)).thenReturn(comPort);

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, COMPORT_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE, now.getTime());
        Object refreshed = QueryMethod.RefreshComPort.execute(parameters, comServerDAO);

        // Asserts
        assertThat(refreshed).isNull();
    }

    @Test
    public void testRefreshComPortWithModifications () throws IOException {
        Date modificationDateBeforeChanges = new DateTime(2013, 4, 29, 11, 56, 8, 0).toDate();
        Date modificationDateAfterChanges = new DateTime(2013, 4, 29, 12, 13, 59, 0).toDate();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.getModificationDate()).thenReturn(modificationDateAfterChanges);
        when(this.engineModelService.findComPort(COMPORT_ID)).thenReturn(comPort);

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, COMPORT_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE, modificationDateBeforeChanges.getTime());
        Object refreshed = QueryMethod.RefreshComPort.execute(parameters, comServerDAO);

        // Asserts
        assertThat(refreshed).isNotNull();
        assertThat(refreshed).isSameAs(comPort);
    }

    @Test
    public void testComTaskExecutionStarted () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(this.engineModelService.findComPort(COMPORT_ID)).thenReturn(comPort);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.deviceDataService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, COMPORT_ID);
        QueryMethod.ExecutionStarted.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).executionStarted(comTaskExecution, comPort);
    }

    @Test
    public void testComTaskExecutionStartedRunsInTransaction () throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(this.engineModelService.findComPort(COMPORT_ID)).thenReturn(comPort);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.deviceDataService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, COMPORT_ID);
        QueryMethod.ExecutionStarted.execute(parameters, comServerDAO);

        // Asserts
        verify(transactionService).execute(any(Transaction.class));
    }

    @Test
    public void testAttemptLockOfComTaskExecution () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(this.engineModelService.findComPort(COMPORT_ID)).thenReturn(comPort);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.deviceDataService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, COMPORT_ID);
        QueryMethod.AttemptLock.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).attemptLock(comTaskExecution, comPort);
    }

    @Test
    public void testAttemptLockOfComTaskExecutionRunsInTransaction () throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(this.engineModelService.findComPort(COMPORT_ID)).thenReturn(comPort);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.deviceDataService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, COMPORT_ID);
        QueryMethod.AttemptLock.execute(parameters, comServerDAO);

        // Asserts
        verify(transactionService).execute(any(Transaction.class));
    }

    @Test
    public void testUnlockOfComTaskExecution () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.deviceDataService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.Unlock.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).unlock(comTaskExecution);
    }

    @Test
    public void testUnlockOfComTaskExecutionRunsInTransaction () throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.deviceDataService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.Unlock.execute(parameters, comServerDAO);

        // Asserts
        verify(transactionService).execute(any(Transaction.class));
    }

    @Test
    public void testConnectionTaskExecutionStarted () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.engineModelService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.deviceDataService.findConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionStarted.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).executionStarted(connectionTask, comServer);
    }

    @Test
    public void testConnectionTaskExecutionStartedRunsInTransaction () throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.engineModelService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.deviceDataService.findConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionStarted.execute(parameters, comServerDAO);

        // Asserts
        verify(transactionService).execute(any(Transaction.class));
    }

    @Test
    public void testAttemptLockOfConnectionTask () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.engineModelService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.deviceDataService.findOutboundConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.AttemptLock.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).attemptLock(connectionTask, comServer);
    }

    @Test
    public void testAttemptLockOfConnectionTaskRunsInTransaction () throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.engineModelService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.deviceDataService.findOutboundConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.AttemptLock.execute(parameters, comServerDAO);

        // Asserts
        verify(transactionService).execute(any(Transaction.class));
    }

    @Test
    public void testUnlockOfConnectionTask () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.deviceDataService.findOutboundConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.Unlock.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).unlock(connectionTask);
    }

    @Test
    public void testUnlockOfConnectionTaskRunsInTransaction () throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.engineModelService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.deviceDataService.findOutboundConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.Unlock.execute(parameters, comServerDAO);

        // Asserts
        verify(transactionService).execute(any(Transaction.class));
    }

    @Test
    public void testComTaskExecutionCompleted () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.deviceDataService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.ExecutionCompleted.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).executionCompleted(comTaskExecution);
    }

    @Test
    public void testComTaskExecutionCompletedRunsInTransaction () throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.deviceDataService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.ExecutionCompleted.execute(parameters, comServerDAO);

        // Asserts
        verify(transactionService).execute(any(Transaction.class));
    }

    @Test
    public void testComTaskExecutionFailed () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.deviceDataService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.ExecutionFailed.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).executionFailed(comTaskExecution);
    }

    @Test
    public void testComTaskExecutionFailedRunsInTransaction () throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.deviceDataService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(comTaskExecution);

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.ExecutionFailed.execute(parameters, comServerDAO);

        // Asserts
        verify(transactionService).execute(any(Transaction.class));
    }

    @Test
    public void testConnectionTaskExecutionCompleted () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.deviceDataService.findConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionCompleted.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).executionCompleted(connectionTask);
    }

    @Test
    public void testConnectionTaskExecutionCompletedRunsInTransaction () throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.deviceDataService.findConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionCompleted.execute(parameters, comServerDAO);

        // Asserts
        verify(transactionService).execute(any(Transaction.class));
    }

    @Test
    public void testConnectionTaskExecutionFailed () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.deviceDataService.findConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.<ConnectionTask>of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionFailed.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).executionFailed(connectionTask);
    }

    @Test
    public void testConnectionTaskExecutionFailedRunsInTransaction () throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.deviceDataService.findConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.<ConnectionTask>of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionFailed.execute(parameters, comServerDAO);

        // Asserts
        verify(transactionService).execute(any(Transaction.class));
    }

    @Test
    public void testReleaseInterruptedComTasks () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.engineModelService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        QueryMethod.ReleaseInterruptedComTasks.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).releaseInterruptedTasks(comServer);
    }

    @Test
    public void testReleaseTimedOutComTasks () throws IOException {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.engineModelService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));
        when(comServerDAO.releaseTimedOutTasks(comServer)).thenReturn(new TimeDuration(951));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        QueryMethod.ReleaseTimedOutComTasks.execute(parameters, comServerDAO);

        // Asserts
        verify(comServerDAO).releaseTimedOutTasks(comServer);
    }

    private List<Method> getComServerDAOMethods () {
        List<Method> methods = new ArrayList<>();
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

}