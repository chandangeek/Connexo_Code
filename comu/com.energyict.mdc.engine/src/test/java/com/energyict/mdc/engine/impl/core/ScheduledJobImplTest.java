package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.impl.ServerComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.ComSessionRootDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CompositeDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.CreateComSessionDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.PublishConnectionCompletionEvent;
import com.energyict.mdc.engine.impl.commands.store.PublishConnectionSetupFailureEvent;
import com.energyict.mdc.engine.impl.core.online.ComServerDAOImpl;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.tasks.StatusInformationTask;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.OfflineDeviceContext;
import com.energyict.protocol.exceptions.ConnectionException;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyCollectionOf;
import static org.mockito.Mockito.doAnswer;
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
    public DeviceProtocolDialect deviceProtocolDialect;
    @Mock
    private OutboundComPortPool comPortPool;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
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
    @Mock
    private SecurityPropertySet securityPropertySet;
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
    private User comServerUser;
    @Mock
    private IssueService issueService;
    @Mock
    private EventService eventService;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private ThreadPrincipalService threadprincipalService;
    @Mock
    private JobExecution.ServiceProvider serviceProvider;

    private Clock clock = Clock.systemDefaultZone();
    private TransactionService transactionService = new FakeTransactionService();

    @Before
    public void setupServiceProvider() {
        when(this.serviceProvider.eventPublisher()).thenReturn(this.eventPublisher);
        when(this.serviceProvider.eventService()).thenReturn(this.eventService);
        when(this.serviceProvider.identificationService()).thenReturn(this.identificationService);
        when(this.serviceProvider.issueService()).thenReturn(this.issueService);
        when(this.serviceProvider.clock()).thenReturn(this.clock);
        when(this.serviceProvider.transactionService()).thenReturn(this.transactionService);
        when(this.serviceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.serviceProvider.deviceConfigurationService()).thenReturn(this.deviceConfigurationService);
        when(this.serviceProvider.engineService()).thenReturn(engineService);
        when(this.userService.findUser(anyString())).thenReturn(Optional.of(comServerUser));
        when(this.connectionTaskService.buildComSession(any(ConnectionTask.class), any(ComPortPool.class), any(ComPort.class), any(Instant.class))).
                thenReturn(comSessionBuilder);
        when(this.engineService.findDeviceCacheByDevice(any(Device.class))).thenReturn(Optional.empty());
        when(comSessionBuilder.addComTaskExecutionSession(Matchers.<ComTaskExecution>any(), any(ComTask.class), any(Instant.class))).thenReturn(comTaskExecutionSessionBuilder);
    }

    @Before
    public void initMocksAndFactories() {
        when(this.comPortPool.getId()).thenReturn(COM_PORT_POOL_ID);
        when(this.comPortPool.getComPortType()).thenReturn(ComPortType.TCP);
        when(this.deviceConfiguration.getDeviceType()).thenReturn(this.deviceType);
        when(this.deviceConfiguration.getComTaskEnablementFor(any(ComTask.class))).thenReturn(Optional.of(this.comTaskEnablement));
        when(this.basicCheckTask.getMaximumClockDifference()).thenReturn(Optional.<TimeDuration>empty());
        when(this.comTaskEnablement.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.comTaskEnablement.getSecurityPropertySet()).thenReturn(this.securityPropertySet);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationDeviceAccessLevel.getId()).thenReturn(DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID);
        when(this.securityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionDeviceAccessLevel.getId()).thenReturn(DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID);
        when(this.securityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
    }

    @Test(timeout = 5000)
    public void prepareDeviceProtocolTest() {
        DeviceProtocol deviceProtocol = mock(MeterProtocolAdapter.class);
        when(deviceProtocol.getDeviceProtocolDialects()).thenReturn(Collections.<DeviceProtocolDialect>emptyList());
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        OfflineDevice offlineDevice = createMockOfflineDevice();
        Device device = createMockDevice();
        OnlineComServer comServer = createMockOnlineComServer();
        OutboundComPort comPort = createMockOutBoundComPort(comServer);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(comServerDAO.findOfflineDevice(any(DeviceIdentifier.class), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineDevice));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ScheduledConnectionTask connectionTask = createMockOutboundConnectionTask();
        ComTask comTask = createMockComTask();
        ProtocolDialectConfigurationProperties mockProtocolDialectConfigurationProperties = createMockProtocolDialectConfigurationProperties();

        ServerComTaskExecution scheduledComTask = createMockServerScheduledComTask(device, connectionTask, comTask, mockProtocolDialectConfigurationProperties);

        ScheduledComTaskExecutionGroup comTaskExecutionGroup = new ScheduledComTaskExecutionGroup(comPort, comServerDAO, this.deviceCommandExecutor, connectionTask, serviceProvider);
        comTaskExecutionGroup.establishConnectionFor(comPort);
        CommandRoot commandRoot = comTaskExecutionGroup.prepareAll(Collections.singletonList(scheduledComTask));

        // asserts
        Map<ComCommandType, ComCommand> commandMap = commandRoot.getCommands();
        assertThat(commandMap).hasSize(11);
        assertThat(commandMap.keySet()).containsSequence(
                ComCommandTypes.DEVICE_PROTOCOL_SET_CACHE_COMMAND,
                ComCommandTypes.INIT_LOGGER_COMMAND,
                ComCommandTypes.ADD_PROPERTIES_COMMAND,
                ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE,
                ComCommandTypes.HAND_HELD_UNIT_ENABLER,
                ComCommandTypes.LOGON,
                ComCommandTypes.BASIC_CHECK_COMMAND,
                ComCommandTypes.STATUS_INFORMATION_COMMAND,
                ComCommandTypes.LOGOFF,
                ComCommandTypes.DEVICE_PROTOCOL_TERMINATE,
                ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);
    }

    @Test(timeout = 5000)
    public void prepareWithMultipleTasks() {
        DeviceProtocol deviceProtocol = mock(MeterProtocolAdapter.class);
        when(deviceProtocol.getDeviceProtocolDialects()).thenReturn(Collections.<DeviceProtocolDialect>emptyList());
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        OfflineDevice offlineDevice = createMockOfflineDevice();
        Device device = createMockDevice();
        OnlineComServer comServer = createMockOnlineComServer();
        OutboundComPort comPort = createMockOutBoundComPort(comServer);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(comServerDAO.findOfflineDevice(any(DeviceIdentifier.class), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineDevice));
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        ScheduledConnectionTask connectionTask = createMockOutboundConnectionTask();
        ComTask comTask = createMockComTask();
        ProtocolDialectConfigurationProperties mockProtocolDialectConfigurationProperties = createMockProtocolDialectConfigurationProperties();

        ServerComTaskExecution comTask1 = createMockServerScheduledComTask(device, connectionTask, comTask, mockProtocolDialectConfigurationProperties);
        ServerComTaskExecution comTask2 = createMockServerScheduledComTask(device, connectionTask, comTask, mockProtocolDialectConfigurationProperties);
        ServerComTaskExecution comTask3 = createMockServerScheduledComTask(device, connectionTask, comTask, mockProtocolDialectConfigurationProperties);

        ScheduledComTaskExecutionGroup group = new ScheduledComTaskExecutionGroup(comPort, comServerDAO, this.deviceCommandExecutor, connectionTask, serviceProvider);
        group.establishConnectionFor(comPort);
        group.add(comTask1);
        group.add(comTask2);
        group.add(comTask3);

        CommandRoot commandRoot = group.prepareAll(Collections.singletonList(comTask1));


        Map<ComCommandType, ComCommand> commandMap = commandRoot.getCommands();
        assertThat(commandMap).hasSize(11);
        assertThat(commandMap.keySet()).containsSequence(
                ComCommandTypes.DEVICE_PROTOCOL_SET_CACHE_COMMAND,
                ComCommandTypes.INIT_LOGGER_COMMAND,
                ComCommandTypes.ADD_PROPERTIES_COMMAND,
                ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE,
                ComCommandTypes.HAND_HELD_UNIT_ENABLER,
                ComCommandTypes.LOGON,
                ComCommandTypes.BASIC_CHECK_COMMAND,
                ComCommandTypes.STATUS_INFORMATION_COMMAND,
                ComCommandTypes.LOGOFF,
                ComCommandTypes.DEVICE_PROTOCOL_TERMINATE,
                ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);

        // creating the commands for the middle comtask
        CommandRoot commandRoot1 = group.prepareAll(Collections.singletonList(comTask2));

        commandMap = commandRoot1.getCommands();
        assertThat(commandMap).hasSize(11);
        assertThat(commandMap.keySet()).containsSequence(
                ComCommandTypes.DEVICE_PROTOCOL_SET_CACHE_COMMAND,
                ComCommandTypes.INIT_LOGGER_COMMAND,
                ComCommandTypes.ADD_PROPERTIES_COMMAND,
                ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE,
                ComCommandTypes.HAND_HELD_UNIT_ENABLER,
                ComCommandTypes.LOGON,
                ComCommandTypes.BASIC_CHECK_COMMAND,
                ComCommandTypes.STATUS_INFORMATION_COMMAND,
                ComCommandTypes.LOGOFF,
                ComCommandTypes.DEVICE_PROTOCOL_TERMINATE,
                ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);


        // creating the commands for the last comtask
        CommandRoot commandRoot2 = group.prepareAll(Collections.singletonList(comTask3));

        commandMap = commandRoot2.getCommands();
        assertThat(commandMap).hasSize(11);
        assertThat(commandMap.keySet()).containsSequence(
                ComCommandTypes.DEVICE_PROTOCOL_SET_CACHE_COMMAND,
                ComCommandTypes.INIT_LOGGER_COMMAND,
                ComCommandTypes.ADD_PROPERTIES_COMMAND,
                ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE,
                ComCommandTypes.HAND_HELD_UNIT_ENABLER,
                ComCommandTypes.LOGON,
                ComCommandTypes.BASIC_CHECK_COMMAND,
                ComCommandTypes.STATUS_INFORMATION_COMMAND,
                ComCommandTypes.LOGOFF,
                ComCommandTypes.DEVICE_PROTOCOL_TERMINATE,
                ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);

        // creating the commands for the two tasks at once
        CommandRoot commandRoot3 = group.prepareAll(Arrays.asList(comTask2, comTask3));

        commandMap = commandRoot3.getCommands();
        assertThat(commandMap).hasSize(12);
        assertThat(commandMap.keySet()).containsSequence(
                ComCommandTypes.DEVICE_PROTOCOL_SET_CACHE_COMMAND,
                ComCommandTypes.INIT_LOGGER_COMMAND,
                ComCommandTypes.ADD_PROPERTIES_COMMAND,
                ComCommandTypes.DEVICE_PROTOCOL_INITIALIZE,
                ComCommandTypes.HAND_HELD_UNIT_ENABLER,
                ComCommandTypes.LOGON,
                ComCommandTypes.BASIC_CHECK_COMMAND,
                ComCommandTypes.STATUS_INFORMATION_COMMAND,
                ComCommandTypes.ALREADY_EXECUTED,
                ComCommandTypes.LOGOFF,
                ComCommandTypes.DEVICE_PROTOCOL_TERMINATE,
                ComCommandTypes.DEVICE_PROTOCOL_UPDATE_CACHE_COMMAND);

    }

    @Test(timeout = 5000)
    public void testThatThreadInterruptSetsAppropriateSuccessIndicator()
            throws
            SQLException,
            ConnectionException, InterruptedException {
        OnlineComServer comServer = this.createMockOnlineComServer();
        final OutboundComPort comPort = this.createMockOutBoundComPort(comServer);
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        when(connectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        OfflineDevice offlineDevice = this.createMockOfflineDevice();
        Device device = this.createMockDevice();
        when(connectionTask.getDevice()).thenReturn(device);
        when(connectionTask.connect(comPort)).thenReturn(new VoidTestComChannel());
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = this.createMockProtocolDialectConfigurationProperties();
        CountDownLatch deviceCommandExecutionStartedLatch = new CountDownLatch(1);
        DeviceCommandExecutor deviceCommandExecutor = new LatchDrivenDeviceCommandExecutor(this.deviceCommandExecutor, deviceCommandExecutionStartedLatch);
        ServerComTaskExecution scheduledComTask = this.createMockServerScheduledComTask(device, connectionTask, this.createMockComTask(), protocolDialectConfigurationProperties);

        final ComServerDAO comServerDAO = getMockedComServerDAO();
        when(comServerDAO.findOfflineDevice(any(DeviceIdentifier.class), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineDevice));
        when(comServerDAO.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAO.areStillPending(anyCollectionOf(Long.class))).thenReturn(true);
        when(comServerDAO.createComSession(any(ComSessionBuilder.class), any(Instant.class), any(ComSession.SuccessIndicator.class))).thenReturn(mock(ComSession.class));
        when(comServerDAO.attemptLock(connectionTask, comServer)).thenReturn(connectionTask);
        when(comServerDAO.attemptLock(any(ComTaskExecution.class), any(ComPort.class))).thenReturn(true);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);

        final ScheduledComTaskExecutionGroup job = new ScheduledComTaskExecutionGroup(comPort, comServerDAO, deviceCommandExecutor, connectionTask, serviceProvider);
        job.add(scheduledComTask);
        final ScheduledJobExecutor jobExecutor = new MultiThreadedScheduledJobExecutor(job, transactionService, ComServer.LogLevel.TRACE, deviceCommandExecutor, threadprincipalService, comServerUser);
        final CountDownLatch startLatch = new CountDownLatch(1);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedFirmwareVersion collectedFirmwareVersion = mock(CollectedFirmwareVersion.class, withSettings().extraInterfaces(ServerCollectedData.class));
        when(collectedFirmwareVersion.getResultType()).thenReturn(ResultType.NotSupported);
        when(((ServerCollectedData) collectedFirmwareVersion).toDeviceCommand(any(MeterDataStoreCommand.class), any(DeviceCommand.ServiceProvider.class))).thenReturn(mock(DeviceCommand.class));
        when(deviceProtocol.getFirmwareVersions()).thenReturn(collectedFirmwareVersion);
        CollectedBreakerStatus collectedBreakerStatus = mock(CollectedBreakerStatus.class, withSettings().extraInterfaces(ServerCollectedData.class));
        when(((ServerCollectedData) collectedBreakerStatus).toDeviceCommand(any(MeterDataStoreCommand.class), any(DeviceCommand.ServiceProvider.class))).thenReturn(mock(DeviceCommand.class));
        when(collectedBreakerStatus.getResultType()).thenReturn(ResultType.NotSupported);
        when(deviceProtocol.getBreakerStatus()).thenReturn(collectedBreakerStatus);
        CollectedCalendar collectedCalendar = mock(CollectedCalendar.class, withSettings().extraInterfaces(ServerCollectedData.class));
        when(((ServerCollectedData) collectedCalendar).toDeviceCommand(any(MeterDataStoreCommand.class), any(DeviceCommand.ServiceProvider.class))).thenReturn(mock(DeviceCommand.class));
        when(collectedCalendar.getResultType()).thenReturn(ResultType.NotSupported);
        when(deviceProtocol.getCollectedCalendar()).thenReturn(collectedCalendar);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));

        when(this.deviceCommandExecutor.execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class))).thenAnswer(
                invocationOnMock -> {
                    DeviceCommand deviceCommand = (DeviceCommand) invocationOnMock.getArguments()[0];
                    deviceCommand.execute(comServerDAO);
                    return null;
                }
        );

        Runnable jobRunnable = () -> {
            startLatch.countDown();
            Thread.currentThread().interrupt();
            jobExecutor.execute(job);
        };
        Thread jobThread = new Thread(jobRunnable);
        jobThread.setName("ScheduledComTaskExecution for testThatThreadInterruptSetsAppropriateSuccessIndicator");
        jobThread.start();

        // Wait until the job has prepared the work
        startLatch.await();
        //this.sleep(50);

        // Business method
        deviceCommandExecutionStartedLatch.await();

        // Asserts
        ArgumentCaptor<DeviceCommand> deviceCommandArgumentCaptor = ArgumentCaptor.forClass(DeviceCommand.class);
        verify(this.deviceCommandExecutor).execute(deviceCommandArgumentCaptor.capture(), any(DeviceCommandExecutionToken.class));
        DeviceCommand deviceCommand = deviceCommandArgumentCaptor.getValue();
        assertThat(deviceCommand).isInstanceOf(CompositeDeviceCommand.class);
        ComSessionRootDeviceCommand compositeDeviceCommand = (ComSessionRootDeviceCommand) deviceCommand;
        CreateComSessionDeviceCommand createComSession = compositeDeviceCommand.getCreateComSessionDeviceCommand();

        assertThat(createComSession).isNotNull();
        assertThat(createComSession.getComSessionBuilder()).isNotNull();
        verify(comServerDAO).createComSession(eq(createComSession.getComSessionBuilder()), any(Instant.class), eq(ComSession.SuccessIndicator.Broken));
    }

    private ComServerDAOImpl getMockedComServerDAO() {
        ComServerDAOImpl comServerDAO = mock(ComServerDAOImpl.class);
        doAnswer(invocationOnMock -> Stream.of(invocationOnMock.getArguments()).filter(o ->  o instanceof ConnectionTask).findAny().orElse(null)).when(comServerDAO).executionStarted(any(ConnectionTask.class), any(ComServer.class));
        return comServerDAO;
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
        ComServerDAOImpl comServerDAO = getMockedComServerDAO();
        when(comServerDAO.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAO.areStillPending(anyCollectionOf(Long.class))).thenReturn(true);
        when(comServerDAO.attemptLock(connectionTask, comServer)).thenReturn(connectionTask);
        when(comServerDAO.attemptLock(any(ComTaskExecution.class), any(ComPort.class))).thenReturn(true);
        when(comServerDAO.createComSession(any(ComSessionBuilder.class), any(Instant.class), any(ComSession.SuccessIndicator.class))).thenCallRealMethod();
        final ScheduledComTaskExecutionGroup job = new ScheduledComTaskExecutionGroup(comPort, comServerDAO, deviceCommandExecutor, connectionTask, serviceProvider);

        final ScheduledJobExecutor jobExecutor = new MultiThreadedScheduledJobExecutor(job, transactionService, ComServer.LogLevel.TRACE, deviceCommandExecutor, threadprincipalService, comServerUser);
        final CountDownLatch startLatch = new CountDownLatch(2);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedFirmwareVersion collectedFirmwareVersion = mock(CollectedFirmwareVersion.class, withSettings().extraInterfaces(ServerCollectedData.class));
        when(collectedFirmwareVersion.getResultType()).thenReturn(ResultType.NotSupported);
        when(deviceProtocol.getFirmwareVersions()).thenReturn(collectedFirmwareVersion);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
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

    @Test(timeout = 5000)
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
        doThrow(ConnectionException.class).when(connectionTask).connect(eq(comPort), anyList());    //Simulate a connection failure
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = this.createMockProtocolDialectConfigurationProperties();
        CountDownLatch deviceCommandExecutionStartedLatch = new CountDownLatch(1);
        DeviceCommandExecutor deviceCommandExecutor = new LatchDrivenDeviceCommandExecutor(this.deviceCommandExecutor, deviceCommandExecutionStartedLatch);
        ComTaskExecution scheduledComTask = this.createMockServerScheduledComTask(device, connectionTask, this.createMockComTask(), protocolDialectConfigurationProperties);
        ComServerDAOImpl comServerDAO = getMockedComServerDAO();
        when(comServerDAO.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAO.areStillPending(anyCollectionOf(Long.class))).thenReturn(true);
        when(comServerDAO.attemptLock(connectionTask, comServer)).thenReturn(connectionTask);
        when(comServerDAO.attemptLock(any(ComTaskExecution.class), any(ComPort.class))).thenReturn(true);
        when(comServerDAO.createComSession(any(ComSessionBuilder.class), any(Instant.class), any(ComSession.SuccessIndicator.class))).thenCallRealMethod();
        final AlwaysFailComTaskExecutionJob job = new AlwaysFailComTaskExecutionJob(comPort, comServerDAO, deviceCommandExecutor, connectionTask, this.serviceProvider);
        final ScheduledJobExecutor jobExecutor = new MultiThreadedScheduledJobExecutor(job, transactionService, ComServer.LogLevel.TRACE, deviceCommandExecutor, threadprincipalService, comServerUser);
        final CountDownLatch startLatch = new CountDownLatch(2);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        CollectedFirmwareVersion collectedFirmwareVersion = mock(CollectedFirmwareVersion.class, withSettings().extraInterfaces(ServerCollectedData.class));
        when(collectedFirmwareVersion.getResultType()).thenReturn(ResultType.NotSupported);
        when(deviceProtocol.getFirmwareVersions()).thenReturn(collectedFirmwareVersion);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        when(this.deviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
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


    private ServerComTaskExecution createMockServerScheduledComTask(Device device, OutboundConnectionTask connectionTask, ComTask comTask, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
        ServerComTaskExecution scheduledComTask = mock(ServerComTaskExecution.class);
        when(scheduledComTask.getDevice()).thenReturn(device);
        when(scheduledComTask.getConnectionTask()).thenReturn(Optional.of(connectionTask));
        when(scheduledComTask.getComTasks()).thenReturn(Arrays.asList(comTask));
        return scheduledComTask;
    }

    private ProtocolDialectConfigurationProperties createMockProtocolDialectConfigurationProperties() {
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = mock(ProtocolDialectConfigurationProperties.class);
        when(protocolDialectConfigurationProperties.getDeviceProtocolDialectName()).thenReturn("MyDialect");
        return protocolDialectConfigurationProperties;
    }

    private ComTask createMockComTask() {
        ComTask comTask = mock(ComTask.class);
        when(comTask.getProtocolTasks()).thenReturn(Arrays.<ProtocolTask>asList(basicCheckTask, statusInformationTask));
        return comTask;
    }

    private ScheduledConnectionTask createMockOutboundConnectionTask() {
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
        when(device.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(device.getProtocolDialectProperties(anyString())).thenReturn(Optional.<ProtocolDialectProperties>empty());
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

        LatchDrivenDeviceCommandExecutor(DeviceCommandExecutor actualExecutor, CountDownLatch executeLatch) {
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
        public Future<Boolean> execute(DeviceCommand command, DeviceCommandExecutionToken token) {
            try {
                return this.actualExecutor.execute(command, token);
            } finally {
                this.executeLatch.countDown();
            }
        }

        @Override
        public void free(DeviceCommandExecutionToken unusedToken) {
            this.actualExecutor.free(unusedToken);
        }

        @Override
        public ComServer.LogLevel getLogLevel() {
            return null;
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

        @Override
        public String getAcquiredTokenThreadNames() {
            return "";
        }
    }

    private class AlwaysFailComTaskExecutionJob extends ScheduledComTaskExecutionGroup {
        private AlwaysFailComTaskExecutionJob(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ScheduledConnectionTask scheduledConnectionTask, ServiceProvider serviceProvider) {
            super(comPort, comServerDAO, deviceCommandExecutor, scheduledConnectionTask, serviceProvider);
        }
    }
}