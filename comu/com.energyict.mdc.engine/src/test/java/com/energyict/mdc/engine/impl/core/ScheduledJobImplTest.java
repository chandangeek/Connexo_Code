package com.energyict.mdc.engine.impl.core;

import com.energyict.comserver.commands.CompositeDeviceCommand;
import com.energyict.comserver.commands.CreateOutboundComSession;
import com.energyict.comserver.commands.DeviceCommand;
import com.energyict.comserver.commands.DeviceCommandExecutionToken;
import com.energyict.comserver.commands.DeviceCommandExecutor;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.commands.ComCommand;
import com.energyict.mdc.commands.ComCommandTypes;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.communication.tasks.ProtocolDialectPropertiesFactory;
import com.energyict.mdc.communication.tasks.ServerComTaskEnablementFactory;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.journal.ComSession;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.ServerDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdw.core.CommunicationDevice;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.energyict.mdc.tasks.StatusInformationTask;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link com.energyict.mdc.engine.impl.core.ScheduledJobImpl} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 10/07/12
 * Time: 15:42
 */
@RunWith(MockitoJUnitRunner.class)
public class ScheduledJobImplTest {

    private static final long COM_PORT_POOL_ID = 1;
    private static final long CONNECTION_TASK_ID = COM_PORT_POOL_ID + 1;
    private static final long DEVICE_ID = CONNECTION_TASK_ID + 1;

    @Mock
    private ServerManager manager;
    @Mock
    private OutboundComPortPool comPortPool;
    @Mock
    private ServerDeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    public DeviceProtocolDialect deviceProtocolDialect;
    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    @Mock
    private DeviceCommandExecutionToken token;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
    @Mock
    private ServerComTaskEnablementFactory comTaskEnablementFactory;
    @Mock
    private ComTaskEnablement comTaskEnablement;
    @Mock
    private BasicCheckTask basicCheckTask;
    @Mock
    private StatusInformationTask statusInformationTask;
    @Mock
    private ProtocolDialectPropertiesFactory protocolDialectPropertiesFactory;
    @Mock
    private ScheduledJobTransactionExecutor scheduledJobTransactionExecutor;

    @Before
    public void initMocksAndFactories() throws BusinessException {
        when(this.comPortPool.getId()).thenReturn(COM_PORT_POOL_ID);
        when(this.comPortPool.getComPortType()).thenReturn(ComPortType.TCP);
        when(this.manager.getComTaskEnablementFactory()).thenReturn(this.comTaskEnablementFactory);
        when(this.comTaskEnablementFactory.
                findByDeviceCommunicationConfigurationAndComTask(
                        any(DeviceCommunicationConfiguration.class),
                        any(ComTask.class))).
                thenReturn(comTaskEnablement);
        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
        when(this.manager.getProtocolDialectPropertiesFactory()).thenReturn(this.protocolDialectPropertiesFactory);
        ManagerFactory.setCurrent(this.manager);
        when(this.scheduledJobTransactionExecutor.execute(any(Transaction.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer (InvocationOnMock invocation) throws Throwable {
                Transaction transaction = (Transaction) invocation.getArguments()[0];
                transaction.doExecute();
                return ScheduledJobExecutor.ValidationReturnStatus.ATTEMPT_LOCK_SUCCESS;
            }
        });
    }

    @Test
    public void prepareDeviceProtocolTest() throws BusinessException {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        OfflineDevice offlineDevice = createMockOfflineDevice();
        CommunicationDevice device = createMockDevice(offlineDevice);
        OnlineComServer comServer = createMockOnlineComServer();
        OutboundComPort comPort = createMockOutBoundComPort(comServer);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        OutboundConnectionTask connectionTask = createMockScheduledConnectionTask();
        ComTask comTask = createMockComTask();
        ProtocolDialectConfigurationProperties mockProtocolDialectConfigurationProperties = createMockProtocolDialectConfigurationProperties();

        ServerComTaskExecution scheduledComTask = createMockServerScheduledComTask(device, connectionTask, comTask, mockProtocolDialectConfigurationProperties);

        ScheduledComTaskExecutionJob scheduledComTaskExecutionJob = new ScheduledComTaskExecutionJob(comPort, comServerDAO, this.deviceCommandExecutor, scheduledComTask, mock(IssueService.class));
        scheduledComTaskExecutionJob.establishConnectionFor(comPort);
        JobExecution.PreparedComTaskExecution preparedComTaskExecution = scheduledComTaskExecutionJob.prepareOne(scheduledComTask);

        // asserts
        assertNotNull(preparedComTaskExecution.getCommandRoot());
        Map<ComCommandTypes, ComCommand> commandMap = preparedComTaskExecution.getCommandRoot().getCommands();
        assertNotNull(commandMap);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.BASIC_CHECK_COMMAND);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.STATUS_INFORMATION_COMMAND);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.ADD_PROPERTIES_COMMAND);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_SET_CACHE_COMMAND);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);
    }

    @Test
    public void prepareWithMultipleTasks() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        OfflineDevice offlineDevice = createMockOfflineDevice();
        CommunicationDevice device = createMockDevice(offlineDevice);
        OnlineComServer comServer = createMockOnlineComServer();
        OutboundComPort comPort = createMockOutBoundComPort(comServer);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        ScheduledConnectionTask connectionTask = createMockScheduledConnectionTask();
        ComTask comTask = createMockComTask();
        ProtocolDialectConfigurationProperties mockProtocolDialectConfigurationProperties = createMockProtocolDialectConfigurationProperties();

        ServerComTaskExecution comTask1 = createMockServerScheduledComTask(device, connectionTask, comTask, mockProtocolDialectConfigurationProperties);
        ServerComTaskExecution comTask2 = createMockServerScheduledComTask(device, connectionTask, comTask, mockProtocolDialectConfigurationProperties);
        ServerComTaskExecution comTask3 = createMockServerScheduledComTask(device, connectionTask, comTask, mockProtocolDialectConfigurationProperties);

        ScheduledComTaskExecutionGroup group = new ScheduledComTaskExecutionGroup(comPort, comServerDAO, this.deviceCommandExecutor, connectionTask, mock(IssueService.class));
        group.establishConnectionFor(comPort);
        group.add(comTask1);
        group.add(comTask2);
        group.add(comTask3);

        JobExecution.PreparedComTaskExecution preparedComTaskExecution = group.prepareOne(comTask1);

        // asserts
        assertNotNull(preparedComTaskExecution.getCommandRoot());
        Map<ComCommandTypes, ComCommand> commandMap = preparedComTaskExecution.getCommandRoot().getCommands();
        assertNotNull(commandMap);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.BASIC_CHECK_COMMAND);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.STATUS_INFORMATION_COMMAND);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.ADD_PROPERTIES_COMMAND);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_SET_CACHE_COMMAND);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);

        // creating the commands for the middle comtask
        JobExecution.PreparedComTaskExecution preparedComTaskExecution2 = group.prepareOne(comTask2);

        // asserts
        assertNotNull(preparedComTaskExecution2.getCommandRoot());
        commandMap = preparedComTaskExecution2.getCommandRoot().getCommands();
        assertNotNull(commandMap);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.BASIC_CHECK_COMMAND);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.STATUS_INFORMATION_COMMAND);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.ADD_PROPERTIES_COMMAND);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_SET_CACHE_COMMAND);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);

        // creating the commands for the last comtask
        JobExecution.PreparedComTaskExecution preparedComTaskExecution3 = group.prepareOne(comTask3);

        assertNotNull(preparedComTaskExecution3.getCommandRoot());
        commandMap = preparedComTaskExecution3.getCommandRoot().getCommands();
        assertNotNull(commandMap);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.BASIC_CHECK_COMMAND);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.STATUS_INFORMATION_COMMAND);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.ADD_PROPERTIES_COMMAND);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_SET_CACHE_COMMAND);
        Assertions.assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);
    }

    @Test(timeout = 5000)
    public void testThatThreadInterruptSetsAppropriateSuccessIndicator ()
        throws
            BusinessException,
            SQLException,
            ConnectionException, InterruptedException {
        OnlineComServer comServer = this.createMockOnlineComServer();
        final OutboundComPort comPort = this.createMockOutBoundComPort(comServer);
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        CommunicationDevice device = this.createMockDevice(this.createMockOfflineDevice());
        when(connectionTask.getDevice()).thenReturn(device);
        when(connectionTask.connect(comPort)).thenReturn(mock(ComChannel.class));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = this.createMockProtocolDialectConfigurationProperties();
        CountDownLatch deviceCommandExecutionStartedLatch = new CountDownLatch(1);
        DeviceCommandExecutor deviceCommandExecutor = new LatchDrivenDeviceCommandExecutor(this.deviceCommandExecutor, deviceCommandExecutionStartedLatch);
        ServerComTaskExecution scheduledComTask = this.createMockServerScheduledComTask(device, connectionTask, this.createMockComTask(), protocolDialectConfigurationProperties);
        final ScheduledComTaskExecutionJob job = new ScheduledComTaskExecutionJob(comPort, mock(ComServerDAO.class), deviceCommandExecutor, scheduledComTask, mock(IssueService.class));
        BlockingQueue<ScheduledJob> blockingQueue = mock(BlockingQueue.class);
        final ScheduledJobExecutor jobExecutor = new MultiThreadedScheduledJobExecutor(this.scheduledJobTransactionExecutor, ComServer.LogLevel.TRACE, blockingQueue, this.deviceCommandExecutor);
        final CountDownLatch startLatch = new CountDownLatch(2);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        Runnable jobRunnable = new Runnable() {
            @Override
            public void run() {
                startLatch.countDown();
                jobExecutor.execute(job);
            }
        };
        Thread jobThread = new Thread(jobRunnable);
        jobThread.setName("ScheduledComTaskExecution for testThatThreadInterruptSetsAppropriateSuccessIndicator");
        jobThread.start();

        // Trigger the job
        startLatch.countDown();

        // Wait until the job has prepared the work
        startLatch.await();
        //this.sleep(50);

        // Business method
        jobThread.interrupt();
        deviceCommandExecutionStartedLatch.await();

        // Asserts
        ArgumentCaptor<DeviceCommand> deviceCommandArgumentCaptor = ArgumentCaptor.forClass(DeviceCommand.class);
        verify(this.deviceCommandExecutor).execute(deviceCommandArgumentCaptor.capture(), any(DeviceCommandExecutionToken.class));
        DeviceCommand deviceCommand = deviceCommandArgumentCaptor.getValue();
        Assertions.assertThat(deviceCommand).isInstanceOf(CompositeDeviceCommand.class);
        CompositeDeviceCommand compositeDeviceCommand = (CompositeDeviceCommand) deviceCommand;
        CreateOutboundComSession createComSession = null;
        for (DeviceCommand command : compositeDeviceCommand.getChildren()) {
            if (command instanceof CreateOutboundComSession) {
                createComSession = (CreateOutboundComSession) command;
            }
        }
        Assertions.assertThat(createComSession).isNotNull();
        assertThat(createComSession.getComSessionShadow()).isNotNull();
        assertThat(createComSession.getComSessionShadow().getSuccessIndicator()).isEqualTo(ComSession.SuccessIndicator.Broken);
    }

    private ServerComTaskExecution createMockServerScheduledComTask (CommunicationDevice device, ConnectionTask connectionTask, ComTask comTask, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getDevice()).thenReturn(device);
        when(scheduledComTask.getConnectionTask()).thenReturn(connectionTask);
        when(scheduledComTask.getComTask()).thenReturn(comTask);
        when(scheduledComTask.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        return scheduledComTask;
    }

    private ProtocolDialectConfigurationProperties createMockProtocolDialectConfigurationProperties() {
        return mock(ProtocolDialectConfigurationProperties.class);
    }

    private ComTask createMockComTask() {
        ComTask comTask = mock(ComTask.class);
        when(comTask.getProtocolTasks()).thenReturn(Arrays.<ProtocolTask>asList(basicCheckTask, statusInformationTask));
        return comTask;
    }

    private ScheduledConnectionTask createMockScheduledConnectionTask() {
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        return connectionTask;
    }

    private OutboundComPort createMockOutBoundComPort(ComServer comServer){
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comPort.getName()).thenReturn("ScheduledJobImplTest");
        return comPort;
    }

    private OnlineComServer createMockOnlineComServer() {
        OnlineComServer comServer = mock(OnlineComServer.class);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        return comServer;
    }

    private CommunicationDevice createMockDevice (OfflineDevice offlineDevice) {
        CommunicationDevice device = mock(CommunicationDevice.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        when(device.goOffline(any(OfflineDeviceContext.class))).thenReturn(offlineDevice);
        when(device.getDeviceType()).thenReturn(this.deviceType);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getCommunicationConfiguration()).thenReturn(deviceCommunicationConfiguration);
        return device;
    }

    private OfflineDevice createMockOfflineDevice () {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        return offlineDevice;
    }

    private class LatchDrivenDeviceCommandExecutor implements DeviceCommandExecutor {
        private DeviceCommandExecutor actualExecutor;
        private CountDownLatch executeLatch;

        protected LatchDrivenDeviceCommandExecutor (DeviceCommandExecutor actualExecutor, CountDownLatch executeLatch) {
            super();
            this.actualExecutor = actualExecutor;
            this.executeLatch = executeLatch;
        }

        @Override
        public List<DeviceCommandExecutionToken> tryAcquireTokens (int numberOfCommands) {
            return this.actualExecutor.tryAcquireTokens(numberOfCommands);
        }

        @Override
        public List<DeviceCommandExecutionToken> acquireTokens (int numberOfCommands) throws InterruptedException {
            return this.actualExecutor.acquireTokens(numberOfCommands);
        }

        @Override
        public void execute (DeviceCommand command, DeviceCommandExecutionToken token) {
            this.actualExecutor.execute(command, token);
            this.executeLatch.countDown();
        }

        @Override
        public void free (DeviceCommandExecutionToken unusedToken) {
            this.actualExecutor.free(unusedToken);
        }

        @Override
        public ComServer.LogLevel getLogLevel () {
            return ComServer.LogLevel.ERROR;
        }

        @Override
        public ServerProcessStatus getStatus () {
            return null;
        }

        @Override
        public void start () {
        }

        @Override
        public void shutdown () {
        }

        @Override
        public void shutdownImmediate () {
        }

        @Override
        public int getCapacity () {
            return 0;
        }

        @Override
        public int getCurrentSize () {
            return 0;
        }

        @Override
        public int getCurrentLoadPercentage () {
            return 0;
        }

        @Override
        public int getNumberOfThreads () {
            return 0;
        }

        @Override
        public int getThreadPriority () {
            return 0;
        }
    }

    private class NoopScheduledJobExecutionEventListener implements ScheduledJobExecutionEventListener {
        @Override
        public void executionStarted (ScheduledJob job) {
            // Designed to ignore
        }
    }
}