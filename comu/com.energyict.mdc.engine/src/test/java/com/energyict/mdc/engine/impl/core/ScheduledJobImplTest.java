package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.impl.DefaultClock;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.CompositeDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CreateOutboundComSession;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.tasks.history.ComSession;
import com.energyict.mdc.tasks.history.ComSessionBuilder;
import com.energyict.mdc.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.tasks.history.TaskHistoryService;

import com.elster.jupiter.transaction.TransactionService;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import com.google.common.base.Optional;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

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
    private OutboundComPortPool comPortPool;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
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
    private ComTaskEnablement comTaskEnablement;
    @Mock(extraInterfaces = OfflineDeviceContext.class)
    private BasicCheckTask basicCheckTask;
    @Mock(extraInterfaces = OfflineDeviceContext.class)
    private StatusInformationTask statusInformationTask;
    @Mock
    private TaskHistoryService taskHistoryService;
    @Mock
    private ComSessionBuilder comSessionBuilder;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private EngineService engineService;
    @Mock
    private EventPublisherImpl eventPublisher;
    @Mock
    private ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder;
    @Mock
    private UserService userService;
    @Mock
    private User user;
    @Mock
    private IssueService issueService;
    @Mock
    private Clock clock;

    private TransactionService transactionService = new FakeTransactionService();
    private FakeServiceProvider serviceProvider = new FakeServiceProvider();

    @Before
    public void setupServiceProvider() {
        ServiceProvider.instance.set(this.serviceProvider);
        this.serviceProvider.setIssueService(this.issueService);
        this.serviceProvider.setUserService(this.userService);
        this.serviceProvider.setClock(this.clock);
        this.serviceProvider.setTransactionService(this.transactionService);
        this.serviceProvider.setTaskHistoryService(this.taskHistoryService);
        this.serviceProvider.setDeviceConfigurationService(this.deviceConfigurationService);
        this.serviceProvider.setEngineService(engineService);
        when(this.userService.findUser(anyString())).thenReturn(Optional.of(user));
        when(this.taskHistoryService.buildComSession(any(ConnectionTask.class), any(ComPortPool.class), any(ComPort.class), any(Date.class))).
                thenReturn(comSessionBuilder);
        when(this.deviceConfigurationService.findComTaskEnablement(any(ComTask.class), any(DeviceConfiguration.class))).thenReturn(Optional.<ComTaskEnablement>absent());
        when(this.engineService.findDeviceCacheByDevice(any(Device.class))).thenReturn(Optional.<DeviceCache>absent());
        when(comSessionBuilder.addComTaskExecutionSession(Matchers.<ComTaskExecution>any(), any(Device.class), Matchers.<Date>any())).thenReturn(comTaskExecutionSessionBuilder);
    }

    @After
    public void resetServiceProvider() {
        ServiceProvider.instance.set(null);
    }

    @Before
    public void setupEventPublisher () {
        EventPublisherImpl.setInstance(this.eventPublisher);
    }

    @After
    public void resetEventPublisher () {
        EventPublisherImpl.setInstance(null);
    }
    @Before
    public void initMocksAndFactories() throws BusinessException {
        when(this.comPortPool.getId()).thenReturn(COM_PORT_POOL_ID);
        when(this.comPortPool.getComPortType()).thenReturn(ComPortType.TCP);
        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
    }

    @Test
    public void prepareDeviceProtocolTest() throws BusinessException {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        OfflineDevice offlineDevice = createMockOfflineDevice();
        Device device = createMockDevice(offlineDevice);
        OnlineComServer comServer = createMockOnlineComServer();
        OutboundComPort comPort = createMockOutBoundComPort(comServer);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        OutboundConnectionTask connectionTask = createMockScheduledConnectionTask();
        ComTask comTask = createMockComTask();
        ProtocolDialectConfigurationProperties mockProtocolDialectConfigurationProperties = createMockProtocolDialectConfigurationProperties();

        ServerComTaskExecution scheduledComTask = createMockServerScheduledComTask(device, connectionTask, comTask, mockProtocolDialectConfigurationProperties);

        ScheduledComTaskExecutionJob scheduledComTaskExecutionJob = new ScheduledComTaskExecutionJob(comPort, comServerDAO, this.deviceCommandExecutor, scheduledComTask, this.serviceProvider);
        scheduledComTaskExecutionJob.createExecutionContext();
        scheduledComTaskExecutionJob.establishConnectionFor();
        JobExecution.PreparedComTaskExecution preparedComTaskExecution = scheduledComTaskExecutionJob.prepareOne(scheduledComTask);

        // asserts
        assertThat(preparedComTaskExecution.getCommandRoot()).isNotNull();
        Map<ComCommandTypes, ComCommand> commandMap = preparedComTaskExecution.getCommandRoot().getCommands();
        assertThat(commandMap).isNotNull();
        assertThat(commandMap.keySet()).contains(ComCommandTypes.BASIC_CHECK_COMMAND);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.STATUS_INFORMATION_COMMAND);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.ADD_PROPERTIES_COMMAND);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_SET_CACHE_COMMAND);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);
    }

    @Test
    public void prepareWithMultipleTasks() {
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        OfflineDevice offlineDevice = createMockOfflineDevice();
        Device device = createMockDevice(offlineDevice);
        OnlineComServer comServer = createMockOnlineComServer();
        OutboundComPort comPort = createMockOutBoundComPort(comServer);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        ScheduledConnectionTask connectionTask = createMockScheduledConnectionTask();
        ComTask comTask = createMockComTask();
        ProtocolDialectConfigurationProperties mockProtocolDialectConfigurationProperties = createMockProtocolDialectConfigurationProperties();

        ServerComTaskExecution comTask1 = createMockServerScheduledComTask(device, connectionTask, comTask, mockProtocolDialectConfigurationProperties);
        ServerComTaskExecution comTask2 = createMockServerScheduledComTask(device, connectionTask, comTask, mockProtocolDialectConfigurationProperties);
        ServerComTaskExecution comTask3 = createMockServerScheduledComTask(device, connectionTask, comTask, mockProtocolDialectConfigurationProperties);

        ScheduledComTaskExecutionGroup group = new ScheduledComTaskExecutionGroup(comPort, comServerDAO, this.deviceCommandExecutor, connectionTask, this.serviceProvider);
        group.createExecutionContext();
        group.establishConnectionFor();
        group.add(comTask1);
        group.add(comTask2);
        group.add(comTask3);

        JobExecution.PreparedComTaskExecution preparedComTaskExecution = group.prepareOne(comTask1);

        // asserts
        assertThat(preparedComTaskExecution.getCommandRoot()).isNotNull();
        Map<ComCommandTypes, ComCommand> commandMap = preparedComTaskExecution.getCommandRoot().getCommands();
        assertThat(commandMap).isNotNull();
        assertThat(commandMap.keySet()).contains(ComCommandTypes.BASIC_CHECK_COMMAND);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.STATUS_INFORMATION_COMMAND);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.ADD_PROPERTIES_COMMAND);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_SET_CACHE_COMMAND);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);

        // creating the commands for the middle comtask
        JobExecution.PreparedComTaskExecution preparedComTaskExecution2 = group.prepareOne(comTask2);

        // asserts
        assertThat(preparedComTaskExecution2.getCommandRoot()).isNotNull();
        commandMap = preparedComTaskExecution2.getCommandRoot().getCommands();
        assertThat(commandMap).isNotNull();
        assertThat(commandMap.keySet()).contains(ComCommandTypes.BASIC_CHECK_COMMAND);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.STATUS_INFORMATION_COMMAND);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.ADD_PROPERTIES_COMMAND);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_SET_CACHE_COMMAND);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);

        // creating the commands for the last comtask
        JobExecution.PreparedComTaskExecution preparedComTaskExecution3 = group.prepareOne(comTask3);

        assertThat(preparedComTaskExecution3.getCommandRoot()).isNotNull();
        commandMap = preparedComTaskExecution3.getCommandRoot().getCommands();
        assertThat(commandMap).isNotNull();
        assertThat(commandMap.keySet()).contains(ComCommandTypes.BASIC_CHECK_COMMAND);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.STATUS_INFORMATION_COMMAND);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.ADD_PROPERTIES_COMMAND);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_SET_CACHE_COMMAND);
        assertThat(commandMap.keySet()).contains(ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);
    }

    @Test
    public void testThatThreadInterruptSetsAppropriateSuccessIndicator()
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
        Device device = this.createMockDevice(this.createMockOfflineDevice());
        when(connectionTask.getDevice()).thenReturn(device);
        when(connectionTask.connect(comPort)).thenReturn(mock(ComChannel.class));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = this.createMockProtocolDialectConfigurationProperties();
        CountDownLatch deviceCommandExecutionStartedLatch = new CountDownLatch(1);
        DeviceCommandExecutor deviceCommandExecutor = new LatchDrivenDeviceCommandExecutor(this.deviceCommandExecutor, deviceCommandExecutionStartedLatch);
        ServerComTaskExecution scheduledComTask = this.createMockServerScheduledComTask(device, connectionTask, this.createMockComTask(), protocolDialectConfigurationProperties);
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        when(comServerDAO.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAO.attemptLock(connectionTask, comServer)).thenReturn(connectionTask);
        when(comServerDAO.attemptLock(any(ComTaskExecution.class), any(ComPort.class))).thenReturn(true);
        when(comServerDAO.createComSession(any(ComSessionBuilder.class), any(ComSession.SuccessIndicator.class))).thenCallRealMethod();
        final ScheduledComTaskExecutionJob job = new ScheduledComTaskExecutionJob(comPort, comServerDAO, deviceCommandExecutor, scheduledComTask, this.serviceProvider);
        BlockingQueue<ScheduledJob> blockingQueue = mock(BlockingQueue.class);
        final ScheduledJobExecutor jobExecutor = new MultiThreadedScheduledJobExecutor(this.transactionService, ComServer.LogLevel.TRACE, blockingQueue, this.deviceCommandExecutor);
        final CountDownLatch startLatch = new CountDownLatch(2);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        Runnable jobRunnable = new Runnable() {
            @Override
            public void run() {
                jobExecutor.execute(job);
                startLatch.countDown();
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
        assertThat(deviceCommand).isInstanceOf(CompositeDeviceCommand.class);
        CompositeDeviceCommand compositeDeviceCommand = (CompositeDeviceCommand) deviceCommand;
        CreateOutboundComSession createComSession = null;
        for (DeviceCommand command : compositeDeviceCommand.getChildren()) {
            if (command instanceof CreateOutboundComSession) {
                createComSession = (CreateOutboundComSession) command;
            }
        }
        assertThat(createComSession).isNotNull();
        assertThat(createComSession.getComSessionBuilder()).isNotNull();
    }

    private ServerComTaskExecution createMockServerScheduledComTask(Device device, ConnectionTask connectionTask, ComTask comTask, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
        ManuallyScheduledComTaskExecution scheduledComTask = mock(ManuallyScheduledComTaskExecution.class, withSettings().extraInterfaces(ServerComTaskExecution.class));
        when(scheduledComTask.getDevice()).thenReturn(device);
        when(scheduledComTask.getConnectionTask()).thenReturn(connectionTask);
        when(scheduledComTask.getComTasks()).thenReturn(Arrays.asList(comTask));
        List<ProtocolTask> protocolTasks = comTask.getProtocolTasks();
        when(scheduledComTask.getProtocolTasks()).thenReturn(protocolTasks);
        when(scheduledComTask.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        return (ServerComTaskExecution) scheduledComTask;
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

    private OutboundComPort createMockOutBoundComPort(ComServer comServer) {
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

    private Device createMockDevice(OfflineDevice offlineDevice) {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        when(device.getDeviceType()).thenReturn(this.deviceType);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getCommunicationConfiguration()).thenReturn(deviceCommunicationConfiguration);
        when(device.getDeviceProtocolProperties()).thenReturn(TypedProperties.empty());
        when(device.getDeviceProtocolPluggableClass()).thenReturn(deviceProtocolPluggableClass);
        return device;
    }

    private OfflineDevice createMockOfflineDevice() {
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        return offlineDevice;
    }

    private class LatchDrivenDeviceCommandExecutor implements DeviceCommandExecutor {

        private DeviceCommandExecutor actualExecutor;
        private CountDownLatch executeLatch;

        protected LatchDrivenDeviceCommandExecutor(DeviceCommandExecutor actualExecutor, CountDownLatch executeLatch) {
            super();
            this.actualExecutor = actualExecutor;
            this.executeLatch = executeLatch;
        }

        @Override
        public List<DeviceCommandExecutionToken> tryAcquireTokens(int numberOfCommands) {
            return this.actualExecutor.tryAcquireTokens(numberOfCommands);
        }

        @Override
        public List<DeviceCommandExecutionToken> acquireTokens(int numberOfCommands) throws InterruptedException {
            return this.actualExecutor.acquireTokens(numberOfCommands);
        }

        @Override
        public void execute(DeviceCommand command, DeviceCommandExecutionToken token) {
            this.actualExecutor.execute(command, token);
            this.executeLatch.countDown();
        }

        @Override
        public void free(DeviceCommandExecutionToken unusedToken) {
            this.actualExecutor.free(unusedToken);
        }

        @Override
        public ComServer.LogLevel getLogLevel() {
            return ComServer.LogLevel.ERROR;
        }

        @Override
        public ServerProcessStatus getStatus() {
            return null;
        }

        @Override
        public void start() {
        }

        @Override
        public void shutdown() {
        }

        @Override
        public void shutdownImmediate() {
        }

        @Override
        public int getCapacity() {
            return 0;
        }

        @Override
        public int getCurrentSize() {
            return 0;
        }

        @Override
        public int getCurrentLoadPercentage() {
            return 0;
        }

        @Override
        public int getNumberOfThreads() {
            return 0;
        }

        @Override
        public int getThreadPriority() {
            return 0;
        }
    }

    private class NoopScheduledJobExecutionEventListener implements ScheduledJobExecutionEventListener {

        @Override
        public void executionStarted(ScheduledJob job) {
            // Designed to ignore
        }
    }
}