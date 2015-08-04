package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.impl.core.ServerProcess;

import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionService;
import org.joda.time.DateTime;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
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
    private ConnectionTaskService connectionTaskService;
    @Mock
    private CommunicationTaskService communicationTaskService;
    @Mock
    private EngineConfigurationService engineConfigurationService;

    private TransactionService transactionService;

    @Before
    public void fakeTransactionService() {
        this.transactionService = new FakeTransactionService();
    }

    private void mockTransactionService() {
        this.transactionService = mock(TransactionService.class);
    }

    @Test
    public void testAllComServerDAOMethodsHaveAnEnumElement() {
        for (Method method : this.getComServerDAOMethods()) {
            // Business method
            QueryMethod queryMethod = QueryMethod.byName(method.getName());

            // Asserts
            assertThat(queryMethod).as(method.toString() + " is missing as an enum element in QueryMethod").isNotNull();
        }
    }

    @Test
    public void testMessageNotUnderstood() {
        assertThat(QueryMethod.byName("anythingbutAnExistingMethodOfTheComServerDAOInterface")).isEqualTo(QueryMethod.MessageNotUnderstood);
    }

    @Test
    public void testGetThisComServerDelegation() throws IOException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        when(serviceProvider.comServerDAO()).thenReturn(comServerDAO);

        // Business method
        QueryMethod.GetThisComServer.execute(new HashMap<>(), serviceProvider);

        // Asserts
        verify(comServerDAO).getThisComServer();
    }

    @Test
    public void testGetComServerDelegation() throws IOException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        String hostName = "testGetComServerDelegation";
        parameters.put(RemoteComServerQueryJSonPropertyNames.HOSTNAME, hostName);
        QueryMethod.GetComServer.execute(parameters, serviceProvider);

        // Asserts
        verify(comServerDAO).getComServer(hostName);
    }

    @Test
    public void testRefreshComServerWithoutModifications() throws IOException {
        Instant now = Instant.now();
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(comServer.getModificationDate()).thenReturn(now);
        when(this.engineConfigurationService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE, now.toEpochMilli());
        Object refreshed = QueryMethod.RefreshComServer.execute(parameters, serviceProvider);

        // Asserts
        assertThat(refreshed).isNull();
    }

    @Test
    public void testRefreshComServerWithModifications() throws IOException {
        Instant modificationDateBeforeChanges = new DateTime(2013, 4, 29, 11, 56, 8, 0).toDate().toInstant();
        Instant modificationDateAfterChanges = new DateTime(2013, 4, 29, 12, 13, 59, 0).toDate().toInstant();
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(comServer.getModificationDate()).thenReturn(modificationDateAfterChanges);
        when(this.engineConfigurationService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.MODIFICATION_DATE, modificationDateBeforeChanges.toEpochMilli());
        Object refreshed = QueryMethod.RefreshComServer.execute(parameters, serviceProvider);

        // Asserts
        assertThat(refreshed).isNotNull();
        assertThat(refreshed).isSameAs(comServer);
    }

    @Test
    public void testComTaskExecutionStarted() throws IOException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        doReturn(Optional.of(comPort)).when(this.engineConfigurationService).findComPort(COMPORT_ID);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.communicationTaskService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(Optional.of(comTaskExecution));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, COMPORT_ID);
        QueryMethod.ExecutionStarted.execute(parameters, serviceProvider);

        // Asserts
        verify(comServerDAO).executionStarted(comTaskExecution, comPort, true);
    }

    @Test
    public void testComTaskExecutionStartedRunsInTransaction() throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        doReturn(Optional.of(comPort)).when(this.engineConfigurationService).findComPort(COMPORT_ID);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.communicationTaskService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(Optional.of(comTaskExecution));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, COMPORT_ID);
        QueryMethod.ExecutionStarted.execute(parameters, serviceProvider);

        // Asserts
        verify(transactionService).execute(any());
    }

    @Test
    public void testAttemptLockOfComTaskExecution() throws IOException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        doReturn(Optional.of(comPort)).when(this.engineConfigurationService).findComPort(COMPORT_ID);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.communicationTaskService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(Optional.of(comTaskExecution));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, COMPORT_ID);
        QueryMethod.AttemptLock.execute(parameters, serviceProvider);

        // Asserts
        verify(comServerDAO).attemptLock(comTaskExecution, comPort);
    }

    @Test
    public void testAttemptLockOfComTaskExecutionRunsInTransaction() throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        doReturn(Optional.of(comPort)).when(this.engineConfigurationService).findComPort(COMPORT_ID);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.communicationTaskService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(Optional.of(comTaskExecution));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMPORT, COMPORT_ID);
        QueryMethod.AttemptLock.execute(parameters, serviceProvider);

        // Asserts
        verify(transactionService).execute(any());
    }

    @Test
    public void testUnlockOfComTaskExecution() throws IOException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.communicationTaskService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(Optional.of(comTaskExecution));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.Unlock.execute(parameters, serviceProvider);

        // Asserts
        verify(comServerDAO).unlock(comTaskExecution);
    }

    @Test
    public void testUnlockOfComTaskExecutionRunsInTransaction() throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.communicationTaskService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(Optional.of(comTaskExecution));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.Unlock.execute(parameters, serviceProvider);

        // Asserts
        verify(transactionService).execute(any());
    }

    @Test
    public void testConnectionTaskExecutionStarted() throws IOException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.engineConfigurationService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.connectionTaskService.findConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionStarted.execute(parameters, serviceProvider);

        // Asserts
        verify(comServerDAO).executionStarted(connectionTask, comServer);
    }

    @Test
    public void testConnectionTaskExecutionStartedRunsInTransaction() throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.engineConfigurationService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.connectionTaskService.findConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionStarted.execute(parameters, serviceProvider);

        // Asserts
        verify(transactionService).execute(any());
    }

    @Test
    public void testAttemptLockOfConnectionTask() throws IOException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.engineConfigurationService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.connectionTaskService.findOutboundConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.AttemptLock.execute(parameters, serviceProvider);

        // Asserts
        verify(comServerDAO).attemptLock(connectionTask, comServer);
    }

    @Test
    public void testAttemptLockOfConnectionTaskRunsInTransaction() throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.engineConfigurationService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.connectionTaskService.findOutboundConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.AttemptLock.execute(parameters, serviceProvider);

        // Asserts
        verify(transactionService).execute(any());
    }

    @Test
    public void testUnlockOfConnectionTask() throws IOException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.connectionTaskService.findOutboundConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.Unlock.execute(parameters, serviceProvider);

        // Asserts
        verify(comServerDAO).unlock(connectionTask);
    }

    @Test
    public void testUnlockOfConnectionTaskRunsInTransaction() throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.engineConfigurationService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.connectionTaskService.findOutboundConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.Unlock.execute(parameters, serviceProvider);

        // Asserts
        verify(transactionService).execute(any());
    }

    @Test
    public void testComTaskExecutionCompleted() throws IOException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.communicationTaskService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(Optional.of(comTaskExecution));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.ExecutionCompleted.execute(parameters, serviceProvider);

        // Asserts
        verify(comServerDAO).executionCompleted(comTaskExecution);
    }

    @Test
    public void testComTaskExecutionCompletedRunsInTransaction() throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.communicationTaskService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(Optional.of(comTaskExecution));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.ExecutionCompleted.execute(parameters, serviceProvider);

        // Asserts
        verify(transactionService).execute(any());
    }

    @Test
    public void testComTaskExecutionFailed() throws IOException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.communicationTaskService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(Optional.of(comTaskExecution));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.ExecutionFailed.execute(parameters, serviceProvider);

        // Asserts
        verify(comServerDAO).executionFailed(comTaskExecution);
    }

    @Test
    public void testComTaskExecutionFailedRunsInTransaction() throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ComTaskExecution comTaskExecution = mock(ComTaskExecution.class);
        when(comTaskExecution.getId()).thenReturn(COMTASKEXECUTION_ID);
        when(this.communicationTaskService.findComTaskExecution(COMTASKEXECUTION_ID)).thenReturn(Optional.of(comTaskExecution));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMTASKEXECUTION, COMTASKEXECUTION_ID);
        QueryMethod.ExecutionFailed.execute(parameters, serviceProvider);

        // Asserts
        verify(transactionService).execute(any());
    }

    @Test
    public void testConnectionTaskExecutionCompleted() throws IOException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.connectionTaskService.findConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionCompleted.execute(parameters, serviceProvider);

        // Asserts
        verify(comServerDAO).executionCompleted(connectionTask);
    }

    @Test
    public void testConnectionTaskExecutionCompletedRunsInTransaction() throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ConnectionTask connectionTask = mock(ConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.connectionTaskService.findConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionCompleted.execute(parameters, serviceProvider);

        // Asserts
        verify(transactionService).execute(any());
    }

    @Test
    public void testConnectionTaskExecutionFailed() throws IOException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.connectionTaskService.findConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.<ConnectionTask>of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionFailed.execute(parameters, serviceProvider);

        // Asserts
        verify(comServerDAO).executionFailed(connectionTask);
    }

    @Test
    public void testConnectionTaskExecutionFailedRunsInTransaction() throws IOException, SQLException, BusinessException {
        this.mockTransactionService();
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTIONTASK_ID);
        when(this.connectionTaskService.findConnectionTask(CONNECTIONTASK_ID)).thenReturn(Optional.<ConnectionTask>of(connectionTask));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.CONNECTIONTASK, CONNECTIONTASK_ID);
        QueryMethod.ExecutionFailed.execute(parameters, serviceProvider);

        // Asserts
        verify(transactionService).execute(any());
    }

    @Test
    public void testReleaseInterruptedComTasks() throws IOException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.engineConfigurationService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        QueryMethod.ReleaseInterruptedComTasks.execute(parameters, serviceProvider);

        // Asserts
        verify(comServerDAO).releaseInterruptedTasks(comServer);
    }

    @Test
    public void testReleaseTimedOutComTasks() throws IOException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        QueryMethod.ServiceProvider serviceProvider = this.newServiceProvider(comServerDAO);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(this.engineConfigurationService.findComServer(COMSERVER_ID)).thenReturn(Optional.of(comServer));
        when(comServerDAO.releaseTimedOutTasks(comServer)).thenReturn(new TimeDuration(951));

        // Business method
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(RemoteComServerQueryJSonPropertyNames.COMSERVER, COMSERVER_ID);
        QueryMethod.ReleaseTimedOutComTasks.execute(parameters, serviceProvider);

        // Asserts
        verify(comServerDAO).releaseTimedOutTasks(comServer);
    }

    private QueryMethod.ServiceProvider newServiceProvider(ComServerDAO comServerDAO) {
        QueryMethod.ServiceProvider serviceProvider = mock(QueryMethod.ServiceProvider.class);
        when(serviceProvider.comServerDAO()).thenReturn(comServerDAO);
        when(serviceProvider.engineConfigurationService()).thenReturn(engineConfigurationService);
        when(serviceProvider.connectionTaskService()).thenReturn(connectionTaskService);
        when(serviceProvider.communicationTaskService()).thenReturn(communicationTaskService);
        when(serviceProvider.transactionService()).thenReturn(transactionService);
        return serviceProvider;
    }

    private List<Method> getComServerDAOMethods() {
        List<Method> methods = new ArrayList<>();
        this.collectMethods(ComServerDAO.class, methods);
        return methods;
    }

    private void collectMethods(Class clazz, List<Method> methods) {
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