package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.store.CompositeDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CreateOutboundComSession;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.PublishConnectionCompletionEvent;
import com.energyict.mdc.engine.impl.commands.store.PublishConnectionSetupFailureEvent;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceContext;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.StatusInformationTask;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import org.assertj.core.api.Condition;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests for the {@link ScheduledJobImpl} component.
 * <p>
 * Copyrights EnergyICT
 * Date: 10/07/12
 * Time: 15:42
 */
@RunWith(MockitoJUnitRunner.class)
public class ScheduledJobImplTest {

    private static final long COM_SERVER_ID = 1;
    private static final long COM_PORT_POOL_ID = COM_SERVER_ID + 1;
    private static final long COM_PORT_ID = COM_PORT_POOL_ID + 1;
    private static final long CONNECTION_TASK_ID = COM_PORT_ID + 1;
    private static final long DEVICE_ID = CONNECTION_TASK_ID + 1;
    private static final long COM_TASK_EXECUTION_ID = DEVICE_ID + 1;

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
    private ComTaskEnablement comTaskEnablement;
    @Mock(extraInterfaces = OfflineDeviceContext.class)
    private BasicCheckTask basicCheckTask;
    @Mock(extraInterfaces = OfflineDeviceContext.class)
    private StatusInformationTask statusInformationTask;
    @Mock
    private ConnectionTaskService connectionTaskService;
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
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private User user;
    @Mock
    private IssueService issueService;
    @Mock
    private EventService eventService;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private ThreadPrincipalService threadprincipalService;

    private Clock clock = Clock.systemDefaultZone();
    private TransactionService transactionService = new FakeTransactionService();
    private FakeServiceProvider serviceProvider = new FakeServiceProvider();

    @Before
    public void setupServiceProvider() {
        ServiceProvider.instance.set(this.serviceProvider);
        this.serviceProvider.setEventService(this.eventService);
        this.serviceProvider.setIdentificationService(this.identificationService);
        this.serviceProvider.setIssueService(this.issueService);
        this.serviceProvider.setUserService(this.userService);
        this.serviceProvider.setClock(this.clock);
        this.serviceProvider.setTransactionService(this.transactionService);
        this.serviceProvider.setConnectionTaskService(this.connectionTaskService);
        this.serviceProvider.setDeviceConfigurationService(this.deviceConfigurationService);
        this.serviceProvider.setEngineService(engineService);
        this.serviceProvider.setThreadPrincipalService(threadPrincipalService);
        when(this.userService.findUser(anyString())).thenReturn(Optional.of(user));
        when(this.connectionTaskService.buildComSession(any(ConnectionTask.class), any(ComPortPool.class), any(ComPort.class), any(Instant.class))).
                thenReturn(comSessionBuilder);
        when(this.deviceConfigurationService.findComTaskEnablement(any(ComTask.class), any(DeviceConfiguration.class))).thenReturn(Optional.empty());
        when(this.engineService.findDeviceCacheByDevice(any(Device.class))).thenReturn(Optional.empty());
        when(comSessionBuilder.addComTaskExecutionSession(Matchers.<ComTaskExecution>any(), any(ComTask.class), any(Device.class), any(Instant.class))).thenReturn(comTaskExecutionSessionBuilder);
    }

    @After
    public void resetServiceProvider() {
        ServiceProvider.instance.set(null);
    }

    @Before
    public void setupEventPublisher() {
        when(this.eventPublisher.serviceProvider()).thenReturn(this.serviceProvider);
        EventPublisherImpl.setInstance(this.eventPublisher);
    }

    @After
    public void resetEventPublisher() {
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
        Device device = createMockDevice();
        OnlineComServer comServer = createMockOnlineComServer();
        OutboundComPort comPort = createMockOutBoundComPort(comServer);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        OutboundConnectionTask connectionTask = createMockScheduledConnectionTask();
        ComTask comTask = createMockComTask();
        ProtocolDialectConfigurationProperties mockProtocolDialectConfigurationProperties = createMockProtocolDialectConfigurationProperties();

        ComTaskExecution scheduledComTask = createMockServerScheduledComTask(device, connectionTask, comTask, mockProtocolDialectConfigurationProperties);

        ScheduledComTaskExecutionJob scheduledComTaskExecutionJob = new ScheduledComTaskExecutionJob(comPort, comServerDAO, this.deviceCommandExecutor, scheduledComTask, this.serviceProvider);
        scheduledComTaskExecutionJob.createExecutionContext();
        scheduledComTaskExecutionJob.establishConnection();
        JobExecution.PreparedComTaskExecution preparedComTaskExecution = scheduledComTaskExecutionJob.prepareOne(scheduledComTask);

        // asserts
        assertThat(preparedComTaskExecution.getCommandRoot()).isNotNull();
        Map<ComCommandType, ComCommand> commandMap = preparedComTaskExecution.getCommandRoot().getCommands();
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
        Device device = createMockDevice();
        OnlineComServer comServer = createMockOnlineComServer();
        OutboundComPort comPort = createMockOutBoundComPort(comServer);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        ScheduledConnectionTask connectionTask = createMockScheduledConnectionTask();
        ComTask comTask = createMockComTask();
        ProtocolDialectConfigurationProperties mockProtocolDialectConfigurationProperties = createMockProtocolDialectConfigurationProperties();

        ComTaskExecution comTask1 = createMockServerScheduledComTask(device, connectionTask, comTask, mockProtocolDialectConfigurationProperties);
        ComTaskExecution comTask2 = createMockServerScheduledComTask(device, connectionTask, comTask, mockProtocolDialectConfigurationProperties);
        ComTaskExecution comTask3 = createMockServerScheduledComTask(device, connectionTask, comTask, mockProtocolDialectConfigurationProperties);

        ScheduledComTaskExecutionGroup group = new ScheduledComTaskExecutionGroup(comPort, comServerDAO, this.deviceCommandExecutor, connectionTask, this.serviceProvider);
        group.createExecutionContext();
        group.establishConnection();
        group.add(comTask1);
        group.add(comTask2);
        group.add(comTask3);

        JobExecution.PreparedComTaskExecution preparedComTaskExecution = group.prepareOne(comTask1);

        // asserts
        assertThat(preparedComTaskExecution.getCommandRoot()).isNotNull();
        Map<ComCommandType, ComCommand> commandMap = preparedComTaskExecution.getCommandRoot().getCommands();
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
    public void testThatThreadInterruptSetsAppropriateSuccessIndicator() throws ConnectionException, InterruptedException {
        OnlineComServer comServer = this.createMockOnlineComServer();
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
        OutboundComPort comPort = this.createMockOutBoundComPort(comServer);
        when(comPort.getComServer()).thenReturn(comServer);
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        Device device = this.createMockDevice();
        when(connectionTask.getDevice()).thenReturn(device);
        when(connectionTask.connect(eq(comPort), anyList())).thenReturn(mock(ComChannel.class));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = this.createMockProtocolDialectConfigurationProperties();
        CountDownLatch deviceCommandExecutionStartedLatch = new CountDownLatch(1);
        DeviceCommandExecutor deviceCommandExecutor = new LatchDrivenDeviceCommandExecutor(this.deviceCommandExecutor, deviceCommandExecutionStartedLatch);
        ComTaskExecution scheduledComTask = this.createMockServerScheduledComTask(device, connectionTask, this.createMockComTask(), protocolDialectConfigurationProperties);
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        when(comServerDAO.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAO.attemptLock(connectionTask, comServer)).thenReturn(connectionTask);
        when(comServerDAO.attemptLock(any(ComTaskExecution.class), any(ComPort.class))).thenReturn(true);
        when(comServerDAO.createComSession(any(ComSessionBuilder.class), any(ComSession.SuccessIndicator.class))).thenCallRealMethod();
        final ScheduledComTaskExecutionJob job = new ScheduledComTaskExecutionJob(comPort, comServerDAO, deviceCommandExecutor, scheduledComTask, this.serviceProvider);
        BlockingQueue<ScheduledJob> blockingQueue = mock(BlockingQueue.class);
        final ScheduledJobExecutor jobExecutor = new MultiThreadedScheduledJobExecutor(comPort, blockingQueue, this.deviceCommandExecutor, this.transactionService, this.threadprincipalService, userService);
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

    @Test
    public void testThatConnectionCompletionPublishesEventForIssueModule() throws ConnectionException, InterruptedException {
        OnlineComServer comServer = this.createMockOnlineComServer();
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
        final OutboundComPort comPort = this.createMockOutBoundComPort(comServer);
        when(comPort.getComServer()).thenReturn(comServer);
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        Device device = this.createMockDevice();
        when(connectionTask.getDevice()).thenReturn(device);
        when(connectionTask.connect(eq(comPort), anyList())).thenReturn(mock(ComChannel.class));
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = this.createMockProtocolDialectConfigurationProperties();
        CountDownLatch deviceCommandExecutionStartedLatch = new CountDownLatch(1);
        DeviceCommandExecutor deviceCommandExecutor = new LatchDrivenDeviceCommandExecutor(this.deviceCommandExecutor, deviceCommandExecutionStartedLatch);
        ComTaskExecution scheduledComTask = this.createMockServerScheduledComTask(device, connectionTask, this.createMockComTask(), protocolDialectConfigurationProperties);
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        when(comServerDAO.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAO.attemptLock(connectionTask, comServer)).thenReturn(connectionTask);
        when(comServerDAO.attemptLock(any(ComTaskExecution.class), any(ComPort.class))).thenReturn(true);
        when(comServerDAO.createComSession(any(ComSessionBuilder.class), any(ComSession.SuccessIndicator.class))).thenCallRealMethod();
        final ScheduledComTaskExecutionJob job = new ScheduledComTaskExecutionJob(comPort, comServerDAO, deviceCommandExecutor, scheduledComTask, this.serviceProvider);
        BlockingQueue<ScheduledJob> blockingQueue = mock(BlockingQueue.class);
        final ScheduledJobExecutor jobExecutor = new MultiThreadedScheduledJobExecutor(comPort, blockingQueue, this.deviceCommandExecutor, this.transactionService, this.threadprincipalService, userService);
        final CountDownLatch startLatch = new CountDownLatch(2);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        Thread jobThread = new Thread(() -> {
            jobExecutor.execute(job);
            startLatch.countDown();
        });
        jobThread.setName("ScheduledComTaskExecution for testThatConnectionCompletionPublishesEventForIssueModule");
        jobThread.start();

        // Trigger the job
        startLatch.countDown();

        // Wait until the job has prepared the work
        startLatch.await();

        // For for completion
        deviceCommandExecutionStartedLatch.await();

        // Asserts
        ArgumentCaptor<DeviceCommand> deviceCommandArgumentCaptor = ArgumentCaptor.forClass(DeviceCommand.class);
        verify(this.deviceCommandExecutor).execute(deviceCommandArgumentCaptor.capture(), any(DeviceCommandExecutionToken.class));
        DeviceCommand rootCommand = deviceCommandArgumentCaptor.getValue();
        assertThat(rootCommand).isInstanceOf(CompositeDeviceCommand.class);
        CompositeDeviceCommand compositeDeviceCommand = (CompositeDeviceCommand) rootCommand;
        assertThat(compositeDeviceCommand.getChildren()).areAtLeast(1, new Condition<DeviceCommand>() {
            @Override
            public boolean matches(DeviceCommand deviceCommand) {
                return deviceCommand instanceof PublishConnectionCompletionEvent;
            }
        });
    }

    @Test
    public void testThatConnectionFailurePublishesEventForIssueModule() throws ConnectionException, InterruptedException {
        OnlineComServer comServer = this.createMockOnlineComServer();
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
        final OutboundComPort comPort = this.createMockOutBoundComPort(comServer);
        when(comPort.getComServer()).thenReturn(comServer);
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        Device device = this.createMockDevice();
        when(connectionTask.getDevice()).thenReturn(device);
        doThrow(ConnectionException.class).when(connectionTask).connect(eq(comPort), anyList());
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = this.createMockProtocolDialectConfigurationProperties();
        CountDownLatch deviceCommandExecutionStartedLatch = new CountDownLatch(1);
        DeviceCommandExecutor deviceCommandExecutor = new LatchDrivenDeviceCommandExecutor(this.deviceCommandExecutor, deviceCommandExecutionStartedLatch);
        ComTaskExecution scheduledComTask = this.createMockServerScheduledComTask(device, connectionTask, this.createMockComTask(), protocolDialectConfigurationProperties);
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        when(comServerDAO.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAO.attemptLock(connectionTask, comServer)).thenReturn(connectionTask);
        when(comServerDAO.attemptLock(any(ComTaskExecution.class), any(ComPort.class))).thenReturn(true);
        when(comServerDAO.createComSession(any(ComSessionBuilder.class), any(ComSession.SuccessIndicator.class))).thenCallRealMethod();
        final AlwaysFailComTaskExecutionJob job = new AlwaysFailComTaskExecutionJob(comPort, comServerDAO, deviceCommandExecutor, scheduledComTask, this.serviceProvider);
        BlockingQueue<ScheduledJob> blockingQueue = mock(BlockingQueue.class);
        final ScheduledJobExecutor jobExecutor = new MultiThreadedScheduledJobExecutor(comPort, blockingQueue, this.deviceCommandExecutor, this.transactionService, this.threadprincipalService, userService);
        final CountDownLatch startLatch = new CountDownLatch(2);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        Thread jobThread = new Thread(() -> {
            jobExecutor.execute(job);
            startLatch.countDown();
        });
        jobThread.setName("ScheduledComTaskExecution for testThatConnectionFailurePublishesEventForIssueModule");
        jobThread.start();

        // Trigger the job
        startLatch.countDown();

        // Wait until the job has prepared the work
        startLatch.await();

        // For for completion
        deviceCommandExecutionStartedLatch.await();

        // Asserts
        ArgumentCaptor<DeviceCommand> deviceCommandArgumentCaptor = ArgumentCaptor.forClass(DeviceCommand.class);
        verify(this.deviceCommandExecutor).execute(deviceCommandArgumentCaptor.capture(), any(DeviceCommandExecutionToken.class));
        DeviceCommand rootCommand = deviceCommandArgumentCaptor.getValue();
        assertThat(rootCommand).isInstanceOf(CompositeDeviceCommand.class);
        CompositeDeviceCommand compositeDeviceCommand = (CompositeDeviceCommand) rootCommand;
        assertThat(compositeDeviceCommand.getChildren()).areAtLeast(1, new Condition<DeviceCommand>() {
            @Override
            public boolean matches(DeviceCommand deviceCommand) {
                return deviceCommand instanceof PublishConnectionSetupFailureEvent;
            }
        });
    }

    private ComTaskExecution createMockServerScheduledComTask(Device device, ConnectionTask connectionTask, ComTask comTask, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
        ManuallyScheduledComTaskExecution scheduledComTask = mock(ManuallyScheduledComTaskExecution.class, withSettings().extraInterfaces(ComTaskExecution.class));
        when(scheduledComTask.getId()).thenReturn(COM_TASK_EXECUTION_ID);
        when(scheduledComTask.getDevice()).thenReturn(device);
        when(scheduledComTask.getConnectionTask()).thenReturn(connectionTask);
        when(scheduledComTask.getComTasks()).thenReturn(Arrays.asList(comTask));
        List<ProtocolTask> protocolTasks = comTask.getProtocolTasks();
        when(scheduledComTask.getProtocolTasks()).thenReturn(protocolTasks);
        when(scheduledComTask.getProtocolDialectConfigurationProperties()).thenReturn(protocolDialectConfigurationProperties);
        return (ComTaskExecution) scheduledComTask;
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
        when(comPort.getId()).thenReturn(COM_PORT_ID);
        when(comPort.getName()).thenReturn("ScheduledJobImplTest");
        return comPort;
    }

    private OnlineComServer createMockOnlineComServer() {
        OnlineComServer comServer = mock(OnlineComServer.class);
        when(comServer.getId()).thenReturn(COM_SERVER_ID);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        return comServer;
    }

    private Device createMockDevice() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn(DEVICE_ID);
        when(device.getDeviceType()).thenReturn(this.deviceType);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
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

    private class AlwaysFailComTaskExecutionJob extends ScheduledComTaskExecutionJob {
        private AlwaysFailComTaskExecutionJob(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
            super(comPort, comServerDAO, deviceCommandExecutor, comTaskExecution, serviceProvider);
        }

        @Override
        boolean execute(PreparedComTaskExecution preparedComTaskExecution) {
            throw new RuntimeException("For unit testing purposes only");
        }

    }

}