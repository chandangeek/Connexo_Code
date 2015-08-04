package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundCapableComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.devices.DeviceCommandExecutorImpl;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.impl.monitor.ScheduledComPortMonitor;
import com.energyict.mdc.engine.impl.monitor.ScheduledComPortMonitorImplMBean;
import com.energyict.mdc.engine.impl.monitor.ScheduledComPortOperationalStatistics;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.LogManager;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.SingleThreadedScheduledComPort} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-04 (14:15)
 */
@RunWith(MockitoJUnitRunner.class)
public class SingleThreadedScheduledComPortTest {

    private static final int NUMBER_OF_SIMULTANEOUS_CONNECTIONS = 3;
    private static final int NUMBER_OF_TASKS = NUMBER_OF_SIMULTANEOUS_CONNECTIONS * 2;
    private static final long DEVICE_ID = 1;
    private static final long COM_PORT_POOL_ID = 2;
    private static final long PROTOCOL_DIALECT_PROPERTIES = 456;

    /**
     * The number of milliseconds that is waited to assert stuff after
     * the scheduled comport has been shutdown.
     */
    private static final int SHUTDOWN_MILLIS = 50;

    @Mock
    private ConnectionType simultaneousConnectionType;
    @Mock
    private OutboundComPortPool comPortPool;
    @Mock
    private ScheduledConnectionTask simultaneousConnectionTask1;
    @Mock
    private ScheduledConnectionTask simultaneousConnectionTask2;
    @Mock
    private ConnectionType serialConnectionType;
    @Mock
    private ScheduledConnectionTask serialConnectionTask1;
    @Mock
    private ScheduledConnectionTask serialConnectionTask2;
    @Mock
    private ComTask comTask;
    @Mock
    private DeviceType deviceType;
    @Mock
    private Device device;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private DeviceProtocolDialect deviceProtocolDialect;
    @Mock
    private ProtocolDialectProperties protocolDialectProperties;
    @Mock
    private ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties;
    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private UserService userService;
    @Mock
    private User user;
    @Mock
    private IssueService issueService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock
    private EngineService engineService;
    @Mock
    private HexService hexService;
    @Mock
    private EventService eventService;
    @Mock
    private EventPublisherImpl eventPublisher;
    @Mock
    private ComSessionBuilder comSessionBuilder;
    @Mock
    private ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder;
    @Mock
    private ManagementBeanFactory managementBeanFactory;
    @Mock(extraInterfaces = ScheduledComPortMonitor.class)
    private ScheduledComPortMonitorImplMBean scheduledComPortMonitor;
    @Mock
    private ScheduledComPortOperationalStatistics operationalStatistics;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private ScheduledComPortImpl.ServiceProvider serviceProvider;

    private Clock clock = Clock.systemUTC();
    private ComPortRelatedComChannel comChannel;
    private ConnectionStrategy connectionStrategy = ConnectionStrategy.AS_SOON_AS_POSSIBLE;

    @BeforeClass
    public static void initializeLogging() {
        InputStream configStream = SingleThreadedScheduledComPortTest.class.getClassLoader().getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(configStream);
        } catch (IOException e) {
            System.err.println("Logging will most likely not work as expected due to the error below.");
            e.printStackTrace(System.err);
        }
    }

    @Before
    public void setupServiceProvider() {
        when(this.serviceProvider.hexService()).thenReturn(this.hexService);
        when(this.serviceProvider.eventPublisher()).thenReturn(this.eventPublisher);
        when(this.serviceProvider.issueService()).thenReturn(this.issueService);
        when(this.serviceProvider.userService()).thenReturn(this.userService);
        when(this.serviceProvider.clock()).thenReturn(this.clock);
        when(this.serviceProvider.transactionService()).thenReturn(new FakeTransactionService());
        when(this.serviceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.serviceProvider.deviceConfigurationService()).thenReturn(this.deviceConfigurationService);
        when(this.serviceProvider.engineService()).thenReturn(engineService);
        when(this.serviceProvider.threadPrincipalService()).thenReturn(threadPrincipalService);
        when(this.serviceProvider.managementBeanFactory()).thenReturn(this.managementBeanFactory);
        when(this.serviceProvider.eventService()).thenReturn(this.eventService);
        when(this.serviceProvider.identificationService()).thenReturn(this.identificationService);
    }

    @Before
    public void initializeMocksAndFactories() throws ConnectionException, BusinessException {
        when(this.managementBeanFactory.findOrCreateFor(any(ScheduledComPort.class))).thenReturn(this.scheduledComPortMonitor);
        ScheduledComPortMonitor comPortMonitor = (ScheduledComPortMonitor) this.scheduledComPortMonitor;
        when(comPortMonitor.getOperationalStatistics()).thenReturn(this.operationalStatistics);
        when(this.userService.findUser(anyString())).thenReturn(Optional.of(user));
        when(this.connectionTaskService.buildComSession(any(ConnectionTask.class), any(ComPortPool.class), any(ComPort.class), any(Instant.class))).
                thenReturn(comSessionBuilder);
        when(this.engineService.findDeviceCacheByDevice(any(Device.class))).thenReturn(Optional.empty());
        when(comSessionBuilder.addComTaskExecutionSession(Matchers.<ComTaskExecution>any(), any(ComTask.class), any(Device.class), any(Instant.class))).thenReturn(comTaskExecutionSessionBuilder);
        when(this.protocolDialectProperties.getId()).thenReturn(PROTOCOL_DIALECT_PROPERTIES);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        when(this.deviceProtocolPluggableClass.getJavaClassName()).thenReturn(SingleThreadedScheduledComPortTest.class.getName());
        when(this.device.getDeviceType()).thenReturn(this.deviceType);
        when(this.device.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.device.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        when(this.device.getProtocolDialectProperties(anyString())).thenReturn(Optional.<ProtocolDialectProperties>empty());
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(this.deviceCommandExecutor.getLogLevel()).thenReturn(ComServer.LogLevel.ERROR);
        when(this.device.getDeviceProtocolProperties()).thenReturn(TypedProperties.empty());
        when(this.comTask.getId()).thenAnswer(new Answer<Long>() {
            long counter = 0;

            @Override
            public Long answer(InvocationOnMock invocationOnMock) throws Throwable {
                return counter++;
            }
        });
        ComTaskEnablement comTaskEnablement = mock(ComTaskEnablement.class);
        when(comTaskEnablement.getComTask()).thenReturn(this.comTask);
        when(comTaskEnablement.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        AuthenticationDeviceAccessLevel authenticationDeviceAccessLevel = mock(AuthenticationDeviceAccessLevel.class);
        when(authenticationDeviceAccessLevel.getId()).thenReturn(DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID);
        when(securityPropertySet.getAuthenticationDeviceAccessLevel()).thenReturn(authenticationDeviceAccessLevel);
        EncryptionDeviceAccessLevel encryptionDeviceAccessLevel = mock(EncryptionDeviceAccessLevel.class);
        when(encryptionDeviceAccessLevel.getId()).thenReturn(DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID);
        when(securityPropertySet.getEncryptionDeviceAccessLevel()).thenReturn(encryptionDeviceAccessLevel);
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(this.deviceConfiguration.getComTaskEnablementFor(this.comTask)).thenReturn(Optional.of(comTaskEnablement));

        DeviceProtocolService deviceProtocolService = mock(DeviceProtocolService.class);
        when(deviceProtocolService.createProtocol(SingleThreadedScheduledComPortTest.class.getName())).thenReturn(new SingleThreadedScheduledComPortTest());

        when(this.comPortPool.getId()).thenReturn(COM_PORT_POOL_ID);
        when(this.comPortPool.getComPortType()).thenReturn(ComPortType.TCP);
        when(this.simultaneousConnectionType.allowsSimultaneousConnections()).thenReturn(true);
        when(this.simultaneousConnectionType.getSupportedComPortTypes()).thenReturn(EnumSet.allOf(ComPortType.class));
        when(this.simultaneousConnectionTask1.getDevice()).thenReturn(this.device);
        when(this.simultaneousConnectionTask1.getConnectionType()).thenReturn(this.simultaneousConnectionType);
        when(this.simultaneousConnectionTask1.getComPortPool()).thenReturn(this.comPortPool);
        when(this.simultaneousConnectionTask1.getConnectionStrategy()).thenReturn(this.connectionStrategy);
        when(this.simultaneousConnectionTask2.getDevice()).thenReturn(this.device);
        when(this.simultaneousConnectionTask2.getConnectionType()).thenReturn(this.simultaneousConnectionType);
        when(this.simultaneousConnectionTask2.getComPortPool()).thenReturn(this.comPortPool);
        when(this.simultaneousConnectionTask2.getConnectionStrategy()).thenReturn(this.connectionStrategy);
        when(this.serialConnectionType.allowsSimultaneousConnections()).thenReturn(false);
        when(this.serialConnectionType.getSupportedComPortTypes()).thenReturn(EnumSet.allOf(ComPortType.class));
        when(this.serialConnectionTask1.getDevice()).thenReturn(this.device);
        when(this.serialConnectionTask1.getConnectionType()).thenReturn(this.serialConnectionType);
        when(this.serialConnectionTask1.getComPortPool()).thenReturn(this.comPortPool);
        when(this.serialConnectionTask1.getConnectionStrategy()).thenReturn(this.connectionStrategy);
        when(this.serialConnectionTask2.getDevice()).thenReturn(this.device);
        when(this.serialConnectionTask2.getConnectionType()).thenReturn(this.serialConnectionType);
        when(this.serialConnectionTask2.getComPortPool()).thenReturn(this.comPortPool);
        when(this.serialConnectionTask2.getConnectionStrategy()).thenReturn(this.connectionStrategy);
    }

    @Test(timeout = 7000)
    public void testStart() {
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread mockedThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(mockedThread);
        SingleThreadedScheduledComPort scheduledComPort =
                new SingleThreadedScheduledComPort(
                        this.mockComPort("testStart"),
                        mock(ComServerDAO.class),
                        this.deviceCommandExecutor,
                        threadFactory,
                        this.serviceProvider);

        // Business method
        scheduledComPort.start();

        // Asserts
        verify(threadFactory, times(1)).newThread(any(Runnable.class));
        verify(mockedThread, times(1)).start();
    }

    @Test(timeout = 7000)
    public void testShutdown() {
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread mockedThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(mockedThread);
        SingleThreadedScheduledComPort scheduledComPort = new SingleThreadedScheduledComPort(this.mockComPort("testShutdown"), mock(ComServerDAO.class), this.deviceCommandExecutor, threadFactory, this.serviceProvider);

        scheduledComPort.start();

        // Business method
        scheduledComPort.shutdown();

        // Asserts
        verify(threadFactory, times(1)).newThread(any(Runnable.class));
        verify(mockedThread, times(1)).interrupt();
    }

    @Test(timeout = 7000)
    public void testExecuteTasksWithNoWork() throws InterruptedException, BusinessException, SQLException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksWithNoWork");
        when(comServerDAO.refreshComPort(comPort)).thenReturn(comPort);
        SpySingleThreadedScheduledComPort scheduledComPort = new SpySingleThreadedScheduledComPort(comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider);
        final CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comServerDAO.findExecutableOutboundComTasks(comPort)).then(invocation -> {
            stopLatch.countDown();
            return new ArrayList<>(0);
        });

        try {
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            verify(comServerDAO, atLeastOnce()).findExecutableOutboundComTasks(comPort);
            scheduledComPort.verifyNoConnectionTaskLockAttemptCalls();
            scheduledComPort.verifyNoComTaskLockAttemptCalls();
            scheduledComPort.verifyNoExecuteComTaskCalls();
            verify(this.deviceCommandExecutor, never()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = 7000)
    public void testExecuteTasksInParallel() throws InterruptedException, BusinessException, SQLException, ConnectionException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksInParallel");
        when(comServerDAO.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAO.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAO.areStillPending(anyCollection())).thenReturn(true);
        when(this.simultaneousConnectionTask1.connect(any(ComPort.class), anyList())).thenReturn(mock(ComChannel.class));
        final List<ComJob> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.toComJob(this.mockComTaskExecution(i + 1, this.simultaneousConnectionTask1)));
        }
        final AtomicBoolean returnActualWork = new AtomicBoolean(true);
        final CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comServerDAO.findExecutableOutboundComTasks(comPort)).then(invocation -> {
            List<ComJob> result;
            if (returnActualWork.getAndSet(false)) {
                result = work;
            } else {
                result = new ArrayList<>(0);
            }
            stopLatch.countDown();
            return result;
        });
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.acquireTokens(1)).thenReturn(tokens);
        SpySingleThreadedScheduledComPort scheduledComPort = new SpySingleThreadedScheduledComPort(comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider);

        try {
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            verify(comServerDAO, atLeastOnce()).findExecutableOutboundComTasks(comPort);
            scheduledComPort.verifyNoConnectionTaskLockAttemptCalls();
            scheduledComPort.verifyNoExecuteComTaskGroupCalls();
            scheduledComPort.verifyComTaskLockAttemptCalls();
            scheduledComPort.verifyExecuteComTaskJobCalls();
            // the above assert are sufficient to validate that the tasks are executed parallel
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = 7000)
    public void testExecuteTasksInParallelWithConnectionFailure() throws ConnectionException, InterruptedException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksInParallelWithConnectionFailure");
        when(comServerDAO.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAO.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAO.areStillPending(anyCollection())).thenReturn(true);
        this.comChannel = new ComPortRelatedComChannelImpl(mock(ComChannel.class), comPort, clock, this.hexService, eventPublisher);
        when(this.simultaneousConnectionTask1.connect(eq(comPort), anyList())).thenReturn(comChannel);
        final List<ComJob> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.toComJob(this.mockComTaskExecution(i + 1, this.simultaneousConnectionTask1)));
        }
        final AtomicBoolean returnActualWork = new AtomicBoolean(true);
        final CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comServerDAO.findExecutableOutboundComTasks(comPort)).then(invocation -> {
            List<ComJob> result;
            if (returnActualWork.getAndSet(false)) {
                result = work;
            } else {
                result = new ArrayList<>(0);
            }
            stopLatch.countDown();
            return result;
        });
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.acquireTokens(1)).thenReturn(tokens);
        SpySingleThreadedScheduledComPort scheduledComPort = new SpySingleThreadedScheduledComPort(comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider);

        try {
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            verify(comServerDAO, atLeastOnce()).findExecutableOutboundComTasks(comPort);
            scheduledComPort.verifyNoConnectionTaskLockAttemptCalls();
            scheduledComPort.verifyNoExecuteComTaskGroupCalls();
            scheduledComPort.verifyComTaskLockAttemptCalls();
            scheduledComPort.verifyExecuteComTaskJobCalls();
            verify(this.deviceCommandExecutor, atLeastOnce()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
            verify(this.deviceCommandExecutor, atMost(NUMBER_OF_TASKS)).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = 7000)
    public void testExecuteTasksInParallelWithComTaskLockAttemptFailures() throws InterruptedException, BusinessException, SQLException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksInParallelWithComTaskLockAttemptFailures");
        when(comServerDAO.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAO.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAO.areStillPending(anyCollection())).thenReturn(true);
        final List<ComJob> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.toComJob(this.mockComTaskExecution(i + 1, this.simultaneousConnectionTask1)));
        }
        final CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comServerDAO.findExecutableOutboundComTasks(comPort)).then(invocation -> {
            stopLatch.countDown();
            return work;
        });
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(NUMBER_OF_TASKS);

        when(this.deviceCommandExecutor.acquireTokens(1)).thenReturn(tokens);
        SpySingleThreadedScheduledComPort scheduledComPort = new SpySingleThreadedScheduledComPort(comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider, true, false);

        try {
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            verify(comServerDAO, atLeastOnce()).findExecutableOutboundComTasks(comPort);
            scheduledComPort.verifyNoConnectionTaskLockAttemptCalls();
            scheduledComPort.verifyNoExecuteComTaskGroupCalls();
            scheduledComPort.verifyComTaskLockAttemptCalls();
            scheduledComPort.verifyNoExecuteComTaskCalls();
            verify(this.deviceCommandExecutor, never()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
            verify(this.deviceCommandExecutor, atLeastOnce()).free(any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

//    @Test(timeout = 7000)
    @Test
    public void testExecuteTasksOneByOne() throws InterruptedException, BusinessException, SQLException, ConnectionException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksOneByOne");
        when(comServerDAO.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAO.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAO.areStillPending(anyCollection())).thenReturn(true);
        this.comChannel = new ComPortRelatedComChannelImpl(mock(ComChannel.class), comPort, clock, this.hexService, eventPublisher);
        when(this.serialConnectionTask1.connect(eq(comPort), anyList())).thenReturn(comChannel);
        List<ComTaskExecution> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.mockComTaskExecution(i + 1, this.serialConnectionTask1));
        }
        List<ComJob> jobs = toComJob(work);
        when(comServerDAO.findExecutableOutboundComTasks(comPort)).thenReturn(jobs).thenReturn(Collections.<ComJob>emptyList());
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.acquireTokens(1)).thenReturn(tokens);
        CountDownLatch stopLatch = new CountDownLatch(1);
        DeviceCommandExecutor deviceCommandExecutor = new LatchDrivenDeviceCommandExecutor(this.deviceCommandExecutor, stopLatch);
        SpySingleThreadedScheduledComPort scheduledComPort = new SpySingleThreadedScheduledComPort(comPort, comServerDAO, deviceCommandExecutor, this.serviceProvider);

        try {
            // Business method
            scheduledComPort.start();

            // Wait until work is executed
            stopLatch.await();

            // Asserts
            verify(comServerDAO, atLeastOnce()).findExecutableOutboundComTasks(comPort);
            scheduledComPort.verifyConnectionTaskLockAttemptCalls();
            scheduledComPort.verifyNoComTaskLockAttemptCalls();
            scheduledComPort.verifyExecuteComTaskGroupCalls();
            scheduledComPort.verifyExecuteComTaskJobCalls();
            verify(this.deviceCommandExecutor, times(1)).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = 7000)
    public void testExecuteTasksOneByOneWithConnectionFailure() throws InterruptedException, BusinessException, SQLException, ConnectionException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksOneByOneWithConnectionFailure");
        when(comServerDAO.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAO.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAO.areStillPending(anyCollection())).thenReturn(true);
        this.comChannel = new ComPortRelatedComChannelImpl(mock(ComChannel.class), comPort, clock, this.hexService, eventPublisher);
        when(this.serialConnectionTask1.connect(eq(comPort), anyList())).thenReturn(comChannel);
        List<ComTaskExecution> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.mockComTaskExecution(i + 1, this.serialConnectionTask1));
        }
        List<ComJob> jobs = this.toComJob(work);
        CountDownLatch stopLatch = new CountDownLatch(1);
        when(comServerDAO.findExecutableOutboundComTasks(comPort)).thenReturn(jobs).thenReturn(Collections.<ComJob>emptyList());
        ;
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.acquireTokens(1)).thenReturn(tokens);
        DeviceCommandExecutor deviceCommandExecutor = new LatchDrivenDeviceCommandExecutor(this.deviceCommandExecutor, stopLatch);
        SpySingleThreadedScheduledComPort scheduledComPort = new SpySingleThreadedScheduledComPort(comPort, comServerDAO, deviceCommandExecutor, this.serviceProvider);

        try {
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            verify(comServerDAO, atLeastOnce()).findExecutableOutboundComTasks(comPort);
            scheduledComPort.verifyConnectionTaskLockAttemptCalls();
            scheduledComPort.verifyNoComTaskLockAttemptCalls();
            scheduledComPort.verifyExecuteComTaskGroupCalls();
            scheduledComPort.verifyExecuteComTaskJobCalls();
            verify(this.deviceCommandExecutor, times(1)).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = 7000)
    public void testExecuteTasksOneByOneOutsideComWindow() throws InterruptedException, BusinessException, SQLException, ConnectionException {
        Date now = new DateTime(2013, 8, 22, 9, 0, 0, 0).toDate();    // It's now 9 am
        this.clock = mock(Clock.class);
        when(this.clock.instant()).thenReturn(now.toInstant());
        when(this.serviceProvider.clock()).thenReturn(this.clock);
        ComWindow comWindow = new ComWindow(DateTimeConstants.SECONDS_PER_HOUR * 4, DateTimeConstants.SECONDS_PER_HOUR * 6); // Window is from 4 am to 6 am
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksOneByOneOutsideComWindow");
        when(comServerDAO.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAO.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAO.areStillPending(anyCollection())).thenReturn(true);
        this.comChannel = new ComPortRelatedComChannelImpl(mock(ComChannel.class), comPort, clock, this.hexService, eventPublisher);
        when(this.serialConnectionTask1.connect(eq(comPort), anyList())).thenReturn(this.comChannel);
        when(this.serialConnectionTask1.getCommunicationWindow()).thenReturn(comWindow);
        List<ComTaskExecution> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.mockComTaskExecution(i + 1, this.serialConnectionTask1));
        }
        List<ComJob> jobs = this.toComJob(work);
        List<ComJob> noJobs = new ArrayList<>(0);
        CountDownLatch stopLatch = new CountDownLatch(1);
        when(comServerDAO.findExecutableOutboundComTasks(comPort)).thenReturn(jobs, noJobs);
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.acquireTokens(1)).thenReturn(tokens);
        DeviceCommandExecutor deviceCommandExecutor = new LatchDrivenDeviceCommandExecutor(this.deviceCommandExecutor, stopLatch);
        SpySingleThreadedScheduledComPort scheduledComPort = new SpySingleThreadedScheduledComPort(comPort, comServerDAO, deviceCommandExecutor, this.serviceProvider);

        try {
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            verify(comServerDAO, atLeastOnce()).findExecutableOutboundComTasks(comPort);
            verify(this.serialConnectionTask1, atLeastOnce()).getCommunicationWindow();
            scheduledComPort.verifyNoConnectionTaskLockAttemptCalls();
            scheduledComPort.verifyNoComTaskLockAttemptCalls();
            verify(this.deviceCommandExecutor, times(1)).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    //@Test(timeout = 5000)
    @Test
    public void testPreventNoResourcesAcquiredExceptionWhenSchedulingJustOutsideComWindow() throws ConnectionException, InterruptedException {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getName()).thenReturn("RealTimeDevComExec");
        Date now = new DateTime(2013, Calendar.AUGUST, 22, 9, 1, 0, 0).toDate();    // It's now 09:01 am
        this.clock = mock(Clock.class);
        when(this.clock.instant()).thenReturn(now.toInstant());
        when(this.serviceProvider.clock()).thenReturn(this.clock);
        ComWindow comWindow = new ComWindow(DateTimeConstants.SECONDS_PER_HOUR * 4, DateTimeConstants.SECONDS_PER_HOUR * 9); // Window is from 4 am to 9 am
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksOneByOneOutsideComWindow");
        when(comServerDAO.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAO.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAO.areStillPending(anyCollection())).thenReturn(true);
        this.comChannel = new ComPortRelatedComChannelImpl(mock(ComChannel.class), comPort, clock, this.hexService, eventPublisher);
        when(this.serialConnectionTask1.connect(eq(comPort), anyList())).thenReturn(this.comChannel);
        when(this.serialConnectionTask1.getCommunicationWindow()).thenReturn(comWindow);
        List<ComTaskExecution> work = new ArrayList<>();
        work.add(this.mockComTaskExecution(1, this.serialConnectionTask1));
        List<ComJob> jobs = this.toComJob(work);
        CountDownLatch stopLatch = new CountDownLatch(1);
        CountDownLatch deviceCommandExecutorStartedLatch = new CountDownLatch(1);
        when(comServerDAO.findExecutableOutboundComTasks(comPort)).thenReturn(jobs).thenReturn(Collections.<ComJob>emptyList());
        DeviceCommandExecutor deviceCommandExecutor = spy(new RealTimeWorkingLatchDrivenDeviceCommandExecutor(comServer.getName(), 10, 1, 1, ComServer.LogLevel.TRACE, new ComServerThreadFactory(comServer), comServerDAO, deviceCommandExecutorStartedLatch, stopLatch));
        deviceCommandExecutor.start();
        SpySingleThreadedScheduledComPort scheduledComPort = new SpySingleThreadedScheduledComPort(comPort, comServerDAO, deviceCommandExecutor, this.serviceProvider);

        try {
            deviceCommandExecutorStartedLatch.await();
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            verify(deviceCommandExecutor, never()).free(Matchers.<DeviceCommandExecutionToken>any());
            verify(deviceCommandExecutor, atLeastOnce()).execute(Matchers.<DeviceCommand>any(), Matchers.<DeviceCommandExecutionToken>any());
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = 7000)
    public void testExecuteTasksOneByOneWithConnectionTaskLockAttemptFailures() throws InterruptedException, BusinessException, SQLException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksOneByOneWithConnectionTaskLockAttemptFailures");
        when(comServerDAO.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAO.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAO.areStillPending(anyCollection())).thenReturn(true);
        List<ComTaskExecution> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.mockComTaskExecution(i + 1, this.serialConnectionTask1));
        }
        final List<ComJob> jobs = this.toComJob(work);
        final CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comServerDAO.findExecutableOutboundComTasks(comPort)).then(new Answer<List<ComJob>>() {
            @Override
            public List<ComJob> answer(InvocationOnMock invocation) {
                stopLatch.countDown();
                return jobs;
            }
        });
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.acquireTokens(1)).thenReturn(tokens);
        SpySingleThreadedScheduledComPort scheduledComPort = new SpySingleThreadedScheduledComPort(comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider, false, true);

        try {
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            verify(comServerDAO, atLeastOnce()).findExecutableOutboundComTasks(comPort);
            scheduledComPort.verifyConnectionTaskLockAttemptCalls();
            scheduledComPort.verifyNoExecuteComTaskGroupCalls();
            scheduledComPort.verifyNoComTaskLockAttemptCalls();
            scheduledComPort.verifyNoExecuteComTaskCalls();
            verify(this.deviceCommandExecutor, never()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
            verify(this.deviceCommandExecutor, atLeastOnce()).free(any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    private OutboundComPort mockComPort(String name) {
        InboundCapableComServer comServer = mock(InboundCapableComServer.class);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS));
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getName()).thenReturn("SingleThreadedScheduledComPortTest#" + name);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comPort.getComServer()).thenReturn(comServer);
        return comPort;
    }

    private ComTaskExecution mockComTaskExecution(long id, OutboundConnectionTask connectionTask) {
        ManuallyScheduledComTaskExecution comTask = mock(ManuallyScheduledComTaskExecution.class, withSettings().extraInterfaces(ComTaskExecution.class));
        when(comTask.getId()).thenReturn(id);
        when(comTask.getConnectionTask()).thenReturn(connectionTask);
        when(comTask.getDevice()).thenReturn(this.device);
        when(comTask.getComTasks()).thenReturn(Arrays.asList(this.comTask));
        when(comTask.getProtocolDialectConfigurationProperties()).thenReturn(this.protocolDialectConfigurationProperties);
        return comTask;
    }

    private ComJob toComJob(ComTaskExecution comTask) {
        return new ComTaskExecutionJob(comTask);
    }

    private List<ComJob> toComJob(List<ComTaskExecution> serialComTasks) {
        ScheduledConnectionTask connectionTask = (ScheduledConnectionTask) serialComTasks.get(0).getConnectionTask();
        ComTaskExecutionGroup group = new ComTaskExecutionGroup(connectionTask);
        serialComTasks.forEach(group::add);
        return Arrays.asList(group);
    }

    private Thread mockedThread() {
        return mock(Thread.class);
    }

    private void shutdown(ScheduledComPort scheduledComPort) throws InterruptedException {
        scheduledComPort.shutdownImmediate();
        Thread.sleep(SHUTDOWN_MILLIS);
    }

    private List<DeviceCommandExecutionToken> mockTokens(int numberOfTokens) {
        List<DeviceCommandExecutionToken> tokens = new ArrayList<>(numberOfTokens);
        for (int i = 0; i < numberOfTokens; i++) {
            tokens.add(mock(DeviceCommandExecutionToken.class));
        }
        return tokens;
    }

    private class SpySingleThreadedScheduledComPort extends SingleThreadedScheduledComPort {

        private boolean neverFailConnectionTaskLockAttempt;
        private boolean neverFailComTaskLockAttempt;
        private int numberOfConnectionTaskLockAttemptCalls = 0;
        private int numberOfComTaskLockAttemptCalls = 0;
        private int numberOfGroupExecuteCalls = 0;
        private int numberOfJobExecuteCalls = 0;

        private SpySingleThreadedScheduledComPort(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
            this(comPort, comServerDAO, deviceCommandExecutor, serviceProvider, true, true);
        }

        private SpySingleThreadedScheduledComPort(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, boolean neverFailConnectionTaskLockAttempt, boolean neverFailComTaskLockAttempt) {
            super(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
            this.neverFailConnectionTaskLockAttempt = neverFailConnectionTaskLockAttempt;
            this.neverFailComTaskLockAttempt = neverFailComTaskLockAttempt;
        }

        public void verifyComTaskLockAttemptCalls() {
            assertTrue("Was expecting at least one attempt to lock a ScheduledComTask.", this.numberOfComTaskLockAttemptCalls > 0);
        }

        public void verifyNoComTaskLockAttemptCalls() {
            assertEquals("Was NOT expecting attempts to lock a ScheduledComTask.", 0, this.numberOfComTaskLockAttemptCalls);
        }

        public void verifyNoConnectionTaskLockAttemptCalls() {
            assertEquals("Was NOT expecting attempts to lock a ConnectionTask.", 0, this.numberOfConnectionTaskLockAttemptCalls);
        }

        public void verifyConnectionTaskLockAttemptCalls() {
            assertTrue("Was expecting at least one attempt to lock a ConnectionTask.", this.numberOfConnectionTaskLockAttemptCalls > 0);
        }

        public void verifyNoExecuteComTaskCalls() {
            int totalNumberOfExecuteCalls = this.numberOfJobExecuteCalls + this.numberOfGroupExecuteCalls;
            assertEquals("Was NOT expecting calls to execute(ScheduledComTask).", 0, totalNumberOfExecuteCalls);
        }

        public void verifyExecuteComTaskJobCalls() {
            assertTrue("Was expecting at least one call to ComTaskExecutionJob#execute()", this.numberOfJobExecuteCalls > 0);
        }

        public void verifyExecuteComTaskGroupCalls() {
            assertTrue("Was expecting at least one call to ComTaskExecutionGroup#execute()", this.numberOfGroupExecuteCalls > 0);
        }

        public void verifyNoExecuteComTaskGroupCalls() {
            assertEquals("Was NOT expecting calls to ComTaskExecutionGroup#execute()", 0, this.numberOfGroupExecuteCalls);
        }

        public void verifyNoExecuteComTaskJobCalls() {
            assertEquals("Was NOT expecting calls to ComTaskExecutionJob#execute()", 0, this.numberOfJobExecuteCalls);
        }

        @Override
        protected ScheduledComTaskExecutionJob newComTaskJob(ComTaskExecution comTaskExecution) {
            return new CountingComTaskExecutionJob(this.getComPort(), this.getComServerDAO(), this.getDeviceCommandExecutor(), serviceProvider, comTaskExecution);
        }

        @Override
        protected ScheduledComTaskExecutionGroup newComTaskGroup(ScheduledConnectionTask connectionTask) {
            return new CountingComTaskExecutionGroup(this.getComPort(), this.getComServerDAO(), this.getDeviceCommandExecutor(), serviceProvider, connectionTask);
        }

        private class CountingComTaskExecutionJob extends ScheduledComTaskExecutionJob {
            private CountingComTaskExecutionJob(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, ComTaskExecution comTask) {
                super(comPort, comServerDAO, deviceCommandExecutor, comTask, serviceProvider);
            }

            @Override
            protected boolean attemptLock(ComTaskExecution comTaskExecution) {
                numberOfComTaskLockAttemptCalls++;
                return neverFailComTaskLockAttempt;
            }

            @Override
            protected boolean attemptLock(ScheduledConnectionTask connectionTask) {
                numberOfConnectionTaskLockAttemptCalls++;
                return neverFailConnectionTaskLockAttempt;
            }

            @Override
            public void execute() {
                numberOfJobExecuteCalls++;
                super.execute();
            }
        }

        private class CountingComTaskExecutionGroup extends ScheduledComTaskExecutionGroup {
            private CountingComTaskExecutionGroup(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, ScheduledConnectionTask connectionTask) {
                super(comPort, comServerDAO, deviceCommandExecutor, connectionTask, serviceProvider);
            }

            @Override
            protected boolean execute(JobExecution.PreparedComTaskExecution preparedComTaskExecution) {
                numberOfJobExecuteCalls++;
                return super.execute(preparedComTaskExecution);
            }

            @Override
            protected boolean attemptLock(ComTaskExecution comTaskExecution) {
                numberOfComTaskLockAttemptCalls++;
                return neverFailComTaskLockAttempt;
            }

            @Override
            protected boolean attemptLock(ScheduledConnectionTask connectionTask) {
                numberOfConnectionTaskLockAttemptCalls++;
                return neverFailConnectionTaskLockAttempt;
            }

            @Override
            public void execute() {
                numberOfGroupExecuteCalls++;
                super.execute();
            }
        }

    }

    private class RealTimeWorkingLatchDrivenDeviceCommandExecutor extends DeviceCommandExecutorImpl {

        private CountDownLatch startedLatch;
        private CountDownLatch executeLatch;

        private RealTimeWorkingLatchDrivenDeviceCommandExecutor(String comServerName, int queueCapacity, int numberOfThreads, int threadPriority, ComServer.LogLevel logLevel, ThreadFactory threadFactory, ComServerDAO comServerDAO, CountDownLatch startedLatch, CountDownLatch executeLatch) {
            super(comServerName, queueCapacity, numberOfThreads, threadPriority, logLevel, threadFactory, clock, comServerDAO, eventPublisher, threadPrincipalService, userService);
            this.startedLatch = startedLatch;
            this.executeLatch = executeLatch;
        }

        @Override
        public void start() {
            try {
                super.start();
            } finally {
                this.startedLatch.countDown();
            }
        }

        @Override
        public Future<Boolean> execute(DeviceCommand command, DeviceCommandExecutionToken token) {
            try {
                return super.execute(command, token);
            } finally {
                this.executeLatch.countDown();
            }
        }
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
        public Future<Boolean> execute(DeviceCommand command, DeviceCommandExecutionToken token) {
            Future<Boolean> execute = this.actualExecutor.execute(command, token);
            this.executeLatch.countDown();
            return execute;
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

}