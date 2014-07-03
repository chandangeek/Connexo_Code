package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.impl.tasks.ConnectionMethod;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.FakeTransactionService;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannelImpl;
import com.energyict.mdc.engine.impl.core.verification.CounterVerifierFactory;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundCapableComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.history.ComSessionBuilder;
import com.energyict.mdc.tasks.history.ComTaskExecutionSessionBuilder;
import com.energyict.mdc.tasks.history.TaskHistoryService;

import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
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

import static com.elster.jupiter.util.Checks.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.MultiThreadedScheduledComPort} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (12:49)
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiThreadedScheduledComPortTest {

    private static final int NUMBER_OF_SIMULTANEOUS_CONNECTIONS = 3;
    private static final int NUMBER_OF_TASKS = NUMBER_OF_SIMULTANEOUS_CONNECTIONS * 2;
    private static final long DEVICE_ID = 1;
    private static final long COM_PORT_POOL_ID = 2;
    private static final long PROTOCOL_DIALECT_PROPERTIES = 651;

    /**
     * The default number of attempts to verify assertion on method invocation.
     * To override the default value specify -Dmulti.threaded.assert.attempts system property
     */
    private static final int DEFAULT_ASSERT_ATTEMPTS = 4;

    /**
     * The name of the system property that overrides the {@link #DEFAULT_ASSERT_ATTEMPTS}.
     */
    private static final String ASSERT_ATTEMPTS_SYSTEM_PROPERTY_NAME = "multi.threaded.assert.attempts";

    /**
     * The default number of milliseconds the actual thread needs (or gets) to query for executable com tasks
     * To override the default value specify -Dmulti.threaded.query.millise system property
     */
    private static final int DEFAULT_THREAD_QUERY_MILLIS = 100;

    /**
     * The name of the system property that overrides the {@link #DEFAULT_THREAD_QUERY_MILLIS}.
     */
    private static final String THREAD_QUERY_MILLIS_SYSTEM_PROPERTY_NAME = "multi.threaded.query.millis";

    /**
     * The number of millisencs that is waited to assert stuff after
     * the scheduled comport has been shutdown.
     */
    private static final int SHUTDOWN_MILLIS = 50;

    private static final String LOG4J_PROPERTIES_FILE_NAME = "log4j.properties";

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
    private ScheduledConnectionTask serialConnectionTask3;
    @Mock
    private ConnectionMethod connectionMethod;
    @Mock
    private ComTask comTask;
    @Mock
    private DeviceType deviceType;
    @Mock
    private Device device;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private DeviceCommunicationConfiguration deviceCommunicationConfiguration;
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
    private ComTaskEnablement comTaskEnablement;
    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    @Mock
    private IssueService issueService;
    @Mock
    private UserService userService;
    @Mock
    private User user;
    @Mock
    private Clock clock;
    @Mock
    private TaskHistoryService taskHistoryService;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private EngineService engineService;
    @Mock
    private EventPublisherImpl eventPublisher;
    @Mock
    private ComSessionBuilder comSessionBuilder;
    @Mock
    private ComTaskExecutionSessionBuilder comTaskExecutionSessionBuilder;

    private FakeServiceProvider serviceProvider = new FakeServiceProvider();
    private ComPortRelatedComChannel comChannel = new ComPortRelatedComChannelImpl(mock(ComChannel.class));
    private CounterVerifierFactory counterVerifierFactory;

    @BeforeClass
    public static void initializeLogging() {
        initializeLog4J();
        initializeJavaUtilLogging();
    }

    private static void initializeJavaUtilLogging() {
        InputStream configStream = MultiThreadedScheduledComPortTest.class.getClassLoader().getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(configStream);
        } catch (IOException e) {
            System.err.println("Logging will most likely not work as expected due to the error below.");
            e.printStackTrace(System.err);
        }
    }

    private static void initializeLog4J() {
        try {
            URL configURL = MultiThreadedScheduledComPortTest.class.getClassLoader().getResource(LOG4J_PROPERTIES_FILE_NAME);
            if (configURL == null) {
                initializeLog4JWithFingersCrossed();
            } else {
                PropertyConfigurator.configure(configURL);
                File configFile = new File(configURL.toURI());
                PropertyConfigurator.configureAndWatch(configFile.getAbsolutePath());
            }
        } catch (URISyntaxException e) {
            initializeLog4JWithFingersCrossed();
        }
    }

    private static void initializeLog4JWithFingersCrossed() {
        String configFilename = LOG4J_PROPERTIES_FILE_NAME;
        PropertyConfigurator.configure(configFilename);
        PropertyConfigurator.configureAndWatch(configFilename);
    }

    @Before
    public void setupServiceProvider() {
        ServiceProvider.instance.set(this.serviceProvider);
        this.serviceProvider.setIssueService(this.issueService);
        this.serviceProvider.setUserService(this.userService);
        this.serviceProvider.setClock(this.clock);
        this.serviceProvider.setTransactionService(new FakeTransactionService());
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
    public void resetServiceProvider () {
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
    public void initializeMocksAndFactories() throws ConnectionException, BusinessException {
        when(this.protocolDialectProperties.getId()).thenReturn(PROTOCOL_DIALECT_PROPERTIES);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        when(this.deviceProtocolPluggableClass.getJavaClassName()).thenReturn(MultiThreadedScheduledComPortTest.class.getName());
        when(this.device.getDeviceType()).thenReturn(this.deviceType);
        when(this.device.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.deviceConfiguration.getCommunicationConfiguration()).thenReturn(this.deviceCommunicationConfiguration);
        when(this.device.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(this.deviceCommandExecutor.getLogLevel()).thenReturn(ComServer.LogLevel.ERROR);
        when(this.device.getDeviceProtocolProperties()).thenReturn(TypedProperties.empty());

        DeviceProtocolService deviceProtocolService = mock(DeviceProtocolService.class);
        when(deviceProtocolService.loadProtocolClass(MultiThreadedScheduledComPortTest.class.getName())).thenReturn(MultiThreadedScheduledComPortTest.class);

        when(this.comPortPool.getId()).thenReturn(COM_PORT_POOL_ID);
        when(this.comPortPool.getComPortType()).thenReturn(ComPortType.TCP);
        when(this.simultaneousConnectionType.allowsSimultaneousConnections()).thenReturn(true);
        when(this.simultaneousConnectionType.allowsSimultaneousConnections()).thenReturn(true);
        when(this.simultaneousConnectionTask1.getDevice()).thenReturn(this.device);
        when(this.simultaneousConnectionTask1.getConnectionType()).thenReturn(this.simultaneousConnectionType);
        when(this.simultaneousConnectionTask1.getComPortPool()).thenReturn(this.comPortPool);
        when(this.simultaneousConnectionTask1.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        when(this.simultaneousConnectionTask2.getDevice()).thenReturn(this.device);
        when(this.simultaneousConnectionTask2.getConnectionType()).thenReturn(this.simultaneousConnectionType);
        when(this.simultaneousConnectionTask2.getComPortPool()).thenReturn(this.comPortPool);
        when(this.simultaneousConnectionTask2.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        when(this.serialConnectionType.allowsSimultaneousConnections()).thenReturn(false);
        when(this.serialConnectionType.getSupportedComPortTypes()).thenReturn(EnumSet.allOf(ComPortType.class));
        when(this.serialConnectionTask1.getDevice()).thenReturn(this.device);
        when(this.serialConnectionTask1.getConnectionType()).thenReturn(this.serialConnectionType);
        when(this.serialConnectionTask1.getComPortPool()).thenReturn(this.comPortPool);
        when(this.serialConnectionTask1.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        when(this.serialConnectionTask2.getDevice()).thenReturn(this.device);
        when(this.serialConnectionTask2.getConnectionType()).thenReturn(this.serialConnectionType);
        when(this.serialConnectionTask2.getComPortPool()).thenReturn(this.comPortPool);
        when(this.serialConnectionTask2.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        when(this.serialConnectionTask3.getDevice()).thenReturn(this.device);
        when(this.serialConnectionTask3.getConnectionType()).thenReturn(this.serialConnectionType);
        when(this.serialConnectionTask3.getComPortPool()).thenReturn(this.comPortPool);
        when(this.serialConnectionTask3.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
    }

    @Before
    public void initializeCounterVerifierFactory() {
        this.counterVerifierFactory = new CounterVerifierFactory(this.assertAttempts(), this.threadQueryMillis());
    }

    private int assertAttempts() {
        String property = System.getProperty(ASSERT_ATTEMPTS_SYSTEM_PROPERTY_NAME);
        if (is(property).empty()) {
            return DEFAULT_ASSERT_ATTEMPTS;
        } else {
            return this.parseNumericSystemProperty(property, ASSERT_ATTEMPTS_SYSTEM_PROPERTY_NAME, DEFAULT_ASSERT_ATTEMPTS);
        }
    }

    private int threadQueryMillis() {
        String property = System.getProperty(THREAD_QUERY_MILLIS_SYSTEM_PROPERTY_NAME);
        if (is(property).empty()) {
            return DEFAULT_THREAD_QUERY_MILLIS;
        } else {
            return this.parseNumericSystemProperty(property, THREAD_QUERY_MILLIS_SYSTEM_PROPERTY_NAME, DEFAULT_THREAD_QUERY_MILLIS);
        }
    }

    private int parseNumericSystemProperty(String property, String systemPropertyName, int defaultValue) {
        try {
            return Integer.parseInt(property);
        } catch (NumberFormatException e) {
            fail("Value for " + systemPropertyName + " system property is not a number: " + property);
        }
        return defaultValue;
    }

    @Test(timeout = 7000)
    public void testStart() {
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread mockedThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(mockedThread);
        MultiThreadedScheduledComPort scheduledComPort = new MultiThreadedScheduledComPort(this.mockComPort("testStart"), mock(ComServerDAO.class), this.deviceCommandExecutor, threadFactory, this.serviceProvider);

        // Business method
        scheduledComPort.start();

        // Asserts
        verify(threadFactory, times(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + 1)).newThread(any(Runnable.class)); // the plus 1 is for the mock
        verify(mockedThread, times(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + 1)).start();
    }

    @Test(timeout = 7000)
    public void testExecuteTasksWithNoWork() throws InterruptedException {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksWithNoWork");
        when(comServerDAO.refreshComPort(comPort)).thenReturn(comPort);
        MultiThreadedScheduledComPort scheduledComPort = new MultiThreadedScheduledComPort(comPort, comServerDAO, deviceCommandExecutor, this.serviceProvider);
        final CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comServerDAO.findExecutableOutboundComTasks(comPort)).then(new Answer<List<ComJob>>() {
            @Override
            public List<ComJob> answer(InvocationOnMock invocation) {
                stopLatch.countDown();
                return new ArrayList<>(0);
            }
        });

        try {
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            verify(comServerDAO, atLeastOnce()).findExecutableOutboundComTasks(comPort);
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = 7000)
    public void testExecuteTasksInParallel() throws InterruptedException, BusinessException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksInParallel");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.isStillPending(anyInt())).thenReturn(true);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(true);
        when(this.simultaneousConnectionTask1.connect(comPort)).thenReturn(this.comChannel);
        final List<ComJob> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.toComJob(this.mockComTask(i + 1, this.simultaneousConnectionTask1)));
        }
        final AtomicBoolean returnActualWork = new AtomicBoolean(true);
        final CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comServerDAOMock.findExecutableOutboundComTasks(comPort)).then(new Answer<List<ComJob>>() {
            @Override
            public List<ComJob> answer(InvocationOnMock invocation) {
                List<ComJob> result;
                if (returnActualWork.getAndSet(false)) {
                    result = work;
                } else {
                    result = new ArrayList<>(0);
                }
                stopLatch.countDown();
                return result;
            }
        });
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(tokens);
        AlwaysAttemptLockMultiThreadedScheduledComPort scheduledComPort = new AlwaysAttemptLockMultiThreadedScheduledComPort(comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider);

        try {
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionStarted(mock(ComTaskExecution.class), comPort);
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionCompleted(mock(ComTaskExecution.class));
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionCompleted(this.simultaneousConnectionTask1);
            verify(this.deviceCommandExecutor, atLeastOnce()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
            verify(this.deviceCommandExecutor, atMost(NUMBER_OF_TASKS)).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = 7000)
    public void testExecuteTasksInParallelWithConnectionFailure() throws InterruptedException, BusinessException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksInParallelWithConnectionFailure");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.isStillPending(anyInt())).thenReturn(true);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(true);
        // Force the connection to fail
        doThrow(ConnectionException.class).when(this.simultaneousConnectionTask1).connect(comPort);
        final List<ComJob> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.toComJob(this.mockComTask(i + 1, this.simultaneousConnectionTask1)));
        }
        final AtomicBoolean returnActualWork = new AtomicBoolean(true);
        final CountDownLatch stopLatch = new CountDownLatch(1); // we only want one ConnectionError
        when(comServerDAOMock.findExecutableOutboundComTasks(comPort)).then(new Answer<List<ComJob>>() {
            @Override
            public List<ComJob> answer(InvocationOnMock invocation) {
                List<ComJob> result;
                if (returnActualWork.getAndSet(false)) {
                    result = work;
                } else {
                    result = new ArrayList<>(0);
                }
                return result;
            }
        });
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(tokens);
        AlwaysAttemptLockMultiThreadedScheduledComPortAndCountDownOnConnectionError scheduledComPort = new AlwaysAttemptLockMultiThreadedScheduledComPortAndCountDownOnConnectionError(comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider, stopLatch);

        try {
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionStarted(mock(ComTaskExecution.class), comPort);
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionFailed(this.simultaneousConnectionTask1);
            verify(this.deviceCommandExecutor, atLeastOnce()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
            verify(this.deviceCommandExecutor, atMost(NUMBER_OF_TASKS)).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = 7000)
    public void testExecuteTasksOneByOne() throws InterruptedException, BusinessException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksOneByOne");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.isStillPending(anyInt())).thenReturn(true);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(true);
        when(this.serialConnectionTask1.connect(comPort)).thenReturn(this.comChannel);
        final List<ServerComTaskExecution> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.mockComTask(i + 1, this.serialConnectionTask1));
        }
        final AtomicBoolean returnActualWork = new AtomicBoolean(true);
        final CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comServerDAOMock.findExecutableOutboundComTasks(comPort)).then(new Answer<List<ComJob>>() {
            @Override
            public List<ComJob> answer(InvocationOnMock invocation) {
                List<ComJob> result;
                if (returnActualWork.getAndSet(false)) {
                    result = toComJob(work);
                } else {
                    result = new ArrayList<>(0);
                }
                stopLatch.countDown();
                return result;
            }
        });
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(tokens);
        AlwaysAttemptLockMultiThreadedScheduledComPort scheduledComPort = new AlwaysAttemptLockMultiThreadedScheduledComPort(comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider);

        try {
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionStarted(mock(ComTaskExecution.class), comPort);
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionCompleted(mock(ComTaskExecution.class));
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionCompleted(this.serialConnectionTask1);

            Thread.sleep(this.counterVerifierFactory.getIdleMillis());
            verify(this.deviceCommandExecutor, times(1)).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = 7000)
    public void testExecuteTasksOneByOneWithConnectionFailure() throws InterruptedException, BusinessException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksOneByOneWithConnectionFailure");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.isStillPending(anyInt())).thenReturn(true);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(true);
        // Force the connection to fail
        doThrow(ConnectionException.class).when(this.serialConnectionTask1).connect(comPort);
        final List<ServerComTaskExecution> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.mockComTask(i + 1, this.serialConnectionTask1));
        }
        final AtomicBoolean returnActualWork = new AtomicBoolean(true);
        final CountDownLatch stopLatch = new CountDownLatch(1); // we only want one ConnectionSetupError
        when(comServerDAOMock.findExecutableOutboundComTasks(comPort)).then(new Answer<List<ComJob>>() {
            @Override
            public List<ComJob> answer(InvocationOnMock invocation) {
                List<ComJob> result;
                if (returnActualWork.getAndSet(false)) {
                    result = toComJob(work);
                } else {
                    result = new ArrayList<>(0);
                }
                stopLatch.countDown();
                return result;
            }
        });
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(tokens);
        AlwaysAttemptLockMultiThreadedScheduledComPort scheduledComPort = new AlwaysAttemptLockMultiThreadedScheduledComPort(comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider);

        try {
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionStarted(mock(ComTaskExecution.class), comPort);
            verify(this.deviceCommandExecutor, times(1)).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }

    }

    @Test(timeout = 7000)
    public void testExecuteTasksInParallelWithWorkThatIsAlwaysStolenByOtherComponents() throws InterruptedException, BusinessException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksInParallelWithWorkThatIsAlwaysStolenByOtherComponents");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.isStillPending(anyInt())).thenReturn(true);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(true);
        when(this.simultaneousConnectionTask1.connect(comPort)).thenReturn(this.comChannel);
        final List<ComJob> noWork = new ArrayList<>(0);
        final List<ComJob> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.toComJob(this.mockComTask(i + 1, this.simultaneousConnectionTask1)));
        }
        CountDownLatch workDeliveredLatch = new CountDownLatch(1);
        final Set<Thread> threadsThatHaveAskedForWork = new HashSet<>();
        when(comServerDAOMock.findExecutableOutboundComTasks(comPort)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (threadsThatHaveAskedForWork.contains(Thread.currentThread())) {
                    return noWork;
                } else {
                    threadsThatHaveAskedForWork.add(Thread.currentThread());
                    return work;
                }
            }
        });
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        DeviceCommandExecutionToken deviceCommandExecutionToken = mock(DeviceCommandExecutionToken.class);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(Arrays.<DeviceCommandExecutionToken>asList(deviceCommandExecutionToken));
        NeverAttemptLockMultiThreadedScheduledComPort scheduledComPort = new NeverAttemptLockMultiThreadedScheduledComPort(comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider, workDeliveredLatch);

        try {
            // Business method
            scheduledComPort.start();

            // Wait until the first job is delivered
            workDeliveredLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionStarted(mock(ComTaskExecution.class), comPort);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionCompleted(mock(ComTaskExecution.class));
            comServerDAO.verify(this.counterVerifierFactory.never()).executionCompleted(this.simultaneousConnectionTask1);
            verify(this.deviceCommandExecutor, never()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
            verify(this.deviceCommandExecutor, atLeastOnce()).free(any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = 7000)
    public void testExecuteTasksOneByOneWithWorkThatIsAlwaysStolenByOtherComponents() throws InterruptedException, BusinessException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksOneByOneWithWorkThatIsAlwaysStolenByOtherComponents");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.isStillPending(anyInt())).thenReturn(true);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(true);
        when(this.serialConnectionTask1.connect(comPort)).thenReturn(this.comChannel);
        final List<ServerComTaskExecution> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.mockComTask(i + 1, this.serialConnectionTask1));
        }
        CountDownLatch workDeliveredLatch = new CountDownLatch(1);
        final List<ComJob> noJobs = new ArrayList<>(0);
        final List<ComJob> jobs = this.toComJob(work);
        final Set<Thread> threadsThatHaveAskedForWork = new HashSet<>();
        when(comServerDAOMock.findExecutableOutboundComTasks(comPort)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (threadsThatHaveAskedForWork.contains(Thread.currentThread())) {
                    return noJobs;
                } else {
                    threadsThatHaveAskedForWork.add(Thread.currentThread());
                    return jobs;
                }
            }
        });
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(tokens);
        NeverAttemptLockMultiThreadedScheduledComPort scheduledComPort = new NeverAttemptLockMultiThreadedScheduledComPort(comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider, workDeliveredLatch);

        try {
            // Business method
            scheduledComPort.start();

            // Wait until the first job is delivered
            workDeliveredLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionStarted(mock(ComTaskExecution.class), comPort);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionCompleted(mock(ComTaskExecution.class));
            comServerDAO.verify(this.counterVerifierFactory.never()).executionCompleted(this.serialConnectionTask1);
            verify(this.deviceCommandExecutor, never()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
            verify(this.deviceCommandExecutor, atLeastOnce()).free(any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = 7000)
    public void testExecuteTasksInParallelWithWorkThatWasAlreadyExecutedByOtherComponents() throws InterruptedException, BusinessException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksInParallelWithWorkThatIsAlwaysStolenByOtherComponents");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.isStillPending(anyInt())).thenReturn(false);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(false);
        when(this.simultaneousConnectionTask1.connect(comPort)).thenReturn(this.comChannel);
        final List<ComJob> noWork = new ArrayList<>(0);
        final List<ComJob> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.toComJob(this.mockComTask(i + 1, this.simultaneousConnectionTask1)));
        }
        CountDownLatch workDeliveredLatch = new CountDownLatch(1);
        final Set<Thread> threadsThatHaveAskedForWork = new HashSet<>();
        when(comServerDAOMock.findExecutableOutboundComTasks(comPort)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (threadsThatHaveAskedForWork.contains(Thread.currentThread())) {
                    return noWork;
                } else {
                    threadsThatHaveAskedForWork.add(Thread.currentThread());
                    return work;
                }
            }
        });
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        DeviceCommandExecutionToken deviceCommandExecutionToken = mock(DeviceCommandExecutionToken.class);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(Arrays.<DeviceCommandExecutionToken>asList(deviceCommandExecutionToken));
        AlwaysAttemptLockMultiThreadedScheduledComPort scheduledComPort = new AlwaysAttemptLockMultiThreadedScheduledComPort(comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider, workDeliveredLatch);

        try {
            // Business method
            scheduledComPort.start();

            // Wait until the first job is delivered
            workDeliveredLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionStarted(mock(ComTaskExecution.class), comPort);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionCompleted(mock(ComTaskExecution.class));
            comServerDAO.verify(this.counterVerifierFactory.never()).executionCompleted(this.simultaneousConnectionTask1);
            verify(this.deviceCommandExecutor, never()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = 7000)
    public void testExecuteTasksOneByOneWithWorkThatWasAlreadyExecutedByOtherComponents() throws InterruptedException, BusinessException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testExecuteTasksOneByOneWithWorkThatIsAlwaysStolenByOtherComponents");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.isStillPending(anyInt())).thenReturn(false);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(false);
        when(this.serialConnectionTask1.connect(comPort)).thenReturn(this.comChannel);
        final List<ServerComTaskExecution> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.mockComTask(i + 1, this.serialConnectionTask1));
        }
        CountDownLatch workDeliveredLatch = new CountDownLatch(1);
        final List<ComJob> noJobs = new ArrayList<>(0);
        final List<ComJob> jobs = this.toComJob(work);
        final Set<Thread> threadsThatHaveAskedForWork = new HashSet<>();
        when(comServerDAOMock.findExecutableOutboundComTasks(comPort)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (threadsThatHaveAskedForWork.contains(Thread.currentThread())) {
                    return noJobs;
                } else {
                    threadsThatHaveAskedForWork.add(Thread.currentThread());
                    return jobs;
                }
            }
        });
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(tokens);
        AlwaysAttemptLockMultiThreadedScheduledComPort scheduledComPort = new AlwaysAttemptLockMultiThreadedScheduledComPort(comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider, workDeliveredLatch);

        try {
            // Business method
            scheduledComPort.start();

            // Wait until the first job is delivered
            workDeliveredLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionStarted(mock(ComTaskExecution.class), comPort);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionCompleted(mock(ComTaskExecution.class));
            comServerDAO.verify(this.counterVerifierFactory.never()).executionCompleted(this.serialConnectionTask1);
            verify(this.deviceCommandExecutor, never()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = 7000)
    public void testConnectionFailureReschedulesTask() throws InterruptedException, BusinessException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = mock(ComServerDAO.class);
        OutboundComPort comPort = this.mockComPort("testConnectionFailureReschedulesTask");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.isStillPending(anyInt())).thenReturn(true);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(true);
        doThrow(ConnectionException.class).when(this.serialConnectionTask1).connect(comPort);
        final List<ServerComTaskExecution> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.mockComTask(i + 1, this.serialConnectionTask1));
        }
        CountDownLatch workDeliveredLatch = new CountDownLatch(1);
        final List<ComJob> jobs = this.toComJob(work);
        final List<ComJob> noJobs = new ArrayList<>(0);
        final Set<Thread> threadsThatHaveAskedForWork = new HashSet<>();
        when(comServerDAOMock.findExecutableOutboundComTasks(comPort)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (threadsThatHaveAskedForWork.contains(Thread.currentThread())) {
                    return noJobs;
                } else {
                    threadsThatHaveAskedForWork.add(Thread.currentThread());
                    return jobs;
                }
            }
        });
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(tokens);
        AlwaysAttemptLockMultiThreadedScheduledComPort scheduledComPort = new AlwaysAttemptLockMultiThreadedScheduledComPort(comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider, workDeliveredLatch);

        try {
            // Business method
            scheduledComPort.start();

            // Wait until the first job is delivered
            workDeliveredLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionStarted(mock(ComTaskExecution.class), comPort);
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionFailed(mock(ComTaskExecution.class));
            verify(this.deviceCommandExecutor, atLeastOnce()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));    // Session is completed even upon failure
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    private OutboundComPort mockComPort(String name) {
        return this.mockComPort(NUMBER_OF_SIMULTANEOUS_CONNECTIONS, name);
    }

    private OutboundComPort mockComPort(int numberOfSimultaneousConnections, String name) {
        InboundCapableComServer comServer = mock(InboundCapableComServer.class);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(new TimeDuration(1, TimeDuration.SECONDS));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getName()).thenReturn("MultiThreadedScheduledComPortTest#" + name);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(numberOfSimultaneousConnections);
        when(comPort.getComServer()).thenReturn(comServer);
        return comPort;
    }

    private ServerComTaskExecution mockComTask(long id, OutboundConnectionTask connectionTask) {
        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class, withSettings().extraInterfaces(ServerComTaskExecution.class));
        when(comTaskExecution.getId()).thenReturn(id);
        when(comTaskExecution.getConnectionTask()).thenReturn(connectionTask);
        when(comTaskExecution.getDevice()).thenReturn(this.device);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(this.comTask));
        when(comTaskExecution.getProtocolDialectConfigurationProperties()).thenReturn(this.protocolDialectConfigurationProperties);
        return (ServerComTaskExecution) comTaskExecution;
    }

    private ComJob toComJob(ServerComTaskExecution comTask) {
        return new ComTaskExecutionJob(comTask);
    }

    private List<ComJob> toComJob(List<ServerComTaskExecution> serialComTasks) {
        ScheduledConnectionTask connectionTask = (ScheduledConnectionTask) serialComTasks.get(0).getConnectionTask();
        ComTaskExecutionGroup group = new ComTaskExecutionGroup(connectionTask);
        for (ServerComTaskExecution comTask : serialComTasks) {
            group.add(comTask);
        }
        List<ComJob> jobs = new ArrayList<>(1);
        jobs.add(group);
        return jobs;
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

    private class AlwaysAttemptLockMultiThreadedScheduledComPort extends MultiThreadedScheduledComPort {

        private CountDownLatch workDeliveredLatch;

        private AlwaysAttemptLockMultiThreadedScheduledComPort(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
            this(comPort, comServerDAO, deviceCommandExecutor, serviceProvider, new CountDownLatch(0));
        }

        private AlwaysAttemptLockMultiThreadedScheduledComPort(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, CountDownLatch workDeliveredLatch) {
            super(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
            this.workDeliveredLatch = workDeliveredLatch;
        }

        @Override
        protected ScheduledComTaskExecutionJob newComTaskJob(ComTaskExecution comTask) {
            return new AlwaysAttemptScheduledComTaskExecutionJob(this.getComPort(), this.getComServerDAO(), deviceCommandExecutor, serviceProvider, comTask);
        }

        @Override
        protected ScheduledComTaskExecutionGroup newComTaskGroup(ScheduledConnectionTask connectionTask) {
            return new AlwaysAttemptScheduledComTaskExecutionGroup(this.getComPort(), this.getComServerDAO(), deviceCommandExecutor, getServiceProvider(), connectionTask);
        }

        private class AlwaysAttemptScheduledComTaskExecutionJob extends ScheduledComTaskExecutionJob {

            private AlwaysAttemptScheduledComTaskExecutionJob(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, ComTaskExecution comTask) {
                super(comPort, comServerDAO, deviceCommandExecutor, comTask, serviceProvider);
            }

            @Override
            protected boolean attemptLock(ComTaskExecution comTaskExecution) {
                workDeliveredLatch.countDown();
                return true;
            }

            @Override
            protected boolean attemptLock(ScheduledConnectionTask connectionTask) {
                workDeliveredLatch.countDown();
                return true;
            }

            @Override
            public boolean isStillPending() {
                workDeliveredLatch.countDown();
                return super.isStillPending();
            }

        }

        public class AlwaysAttemptScheduledComTaskExecutionGroup extends ScheduledComTaskExecutionGroup {

            public AlwaysAttemptScheduledComTaskExecutionGroup(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, ScheduledConnectionTask connectionTask) {
                super(comPort, comServerDAO, deviceCommandExecutor, connectionTask, serviceProvider);
            }

            @Override
            protected boolean attemptLock(ComTaskExecution comTaskExecution) {
                workDeliveredLatch.countDown();
                return true;
            }

            @Override
            protected boolean attemptLock(ScheduledConnectionTask connectionTask) {
                workDeliveredLatch.countDown();
                return true;
            }

            @Override
            public boolean isStillPending() {
                workDeliveredLatch.countDown();
                return super.isStillPending();
            }
        }
    }

    private class NeverAttemptLockMultiThreadedScheduledComPort extends MultiThreadedScheduledComPort {

        private CountDownLatch workDeliveredLatch;

        private NeverAttemptLockMultiThreadedScheduledComPort(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, CountDownLatch workDeliveredLatch) {
            super(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
            this.workDeliveredLatch = workDeliveredLatch;
        }

        @Override
        protected ScheduledComTaskExecutionJob newComTaskJob(ComTaskExecution comTask) {
            return new NeverAttemptScheduledComTaskExecutionJob(this.getComPort(), this.getComServerDAO(), deviceCommandExecutor, getServiceProvider(), comTask);
        }

        @Override
        protected ScheduledComTaskExecutionGroup newComTaskGroup(ScheduledConnectionTask connectionTask) {
            return new NeverAttemptScheduledComTaskExecutionGroup(this.getComPort(), this.getComServerDAO(), deviceCommandExecutor, getServiceProvider(), connectionTask);
        }

        private class NeverAttemptScheduledComTaskExecutionJob extends ScheduledComTaskExecutionJob {

            private NeverAttemptScheduledComTaskExecutionJob(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, ComTaskExecution comTask) {
                super(comPort, comServerDAO, deviceCommandExecutor, comTask, serviceProvider);
            }

            @Override
            protected boolean attemptLock(ComTaskExecution comTaskExecution) {
                workDeliveredLatch.countDown();
                return false;
            }

            @Override
            protected boolean attemptLock(ScheduledConnectionTask connectionTask) {
                workDeliveredLatch.countDown();
                return false;
            }

        }

        public class NeverAttemptScheduledComTaskExecutionGroup extends ScheduledComTaskExecutionGroup {

            public NeverAttemptScheduledComTaskExecutionGroup(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, ScheduledConnectionTask connectionTask) {
                super(comPort, comServerDAO, deviceCommandExecutor, connectionTask, serviceProvider);
            }

            @Override
            protected boolean attemptLock(ComTaskExecution comTaskExecution) {
                workDeliveredLatch.countDown();
                return false;
            }

            @Override
            protected boolean attemptLock(ScheduledConnectionTask connectionTask) {
                workDeliveredLatch.countDown();
                return false;
            }

        }
    }

    private class AlwaysAttemptLockMultiThreadedScheduledComPortAndCountDownOnConnectionError extends MultiThreadedScheduledComPort {

        private CountDownLatch connectionError;

        private AlwaysAttemptLockMultiThreadedScheduledComPortAndCountDownOnConnectionError(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, CountDownLatch connectionError) {
            super(comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
            this.connectionError = connectionError;
        }

        @Override
        protected ScheduledComTaskExecutionJob newComTaskJob(ComTaskExecution comTask) {
            return new AlwaysAttemptScheduledComTaskExecutionJob(this.getComPort(), this.getComServerDAO(), deviceCommandExecutor, serviceProvider, comTask);
        }

        @Override
        protected ScheduledComTaskExecutionGroup newComTaskGroup(ScheduledConnectionTask connectionTask) {
            return new AlwaysAttemptScheduledComTaskExecutionGroup(this.getComPort(), this.getComServerDAO(), deviceCommandExecutor, getServiceProvider(), connectionTask);
        }

        private class AlwaysAttemptScheduledComTaskExecutionJob extends ScheduledComTaskExecutionJob {

            private AlwaysAttemptScheduledComTaskExecutionJob(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, ComTaskExecution comTask) {
                super(comPort, comServerDAO, deviceCommandExecutor, comTask, serviceProvider);
            }

            @Override
            protected boolean attemptLock(ComTaskExecution comTaskExecution) {
                return true;
            }

            @Override
            protected boolean attemptLock(ScheduledConnectionTask connectionTask) {
                return true;
            }

            @Override
            public void reschedule(Throwable t, RescheduleBehavior.RescheduleReason rescheduleReason) {
                super.reschedule(t, rescheduleReason);
                if (rescheduleReason.equals(RescheduleBehavior.RescheduleReason.CONNECTION_SETUP)) {
                    connectionError.countDown();
                }
            }
        }

        public class AlwaysAttemptScheduledComTaskExecutionGroup extends ScheduledComTaskExecutionGroup {

            public AlwaysAttemptScheduledComTaskExecutionGroup(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, ScheduledConnectionTask connectionTask) {
                super(comPort, comServerDAO, deviceCommandExecutor, connectionTask, serviceProvider);
            }

            @Override
            protected boolean attemptLock(ComTaskExecution comTaskExecution) {
                return true;
            }

            @Override
            protected boolean attemptLock(ScheduledConnectionTask connectionTask) {
                return true;
            }

            @Override
            public void reschedule(Throwable t, RescheduleBehavior.RescheduleReason rescheduleReason) {
                super.reschedule(t, rescheduleReason);
                if (rescheduleReason.equals(RescheduleBehavior.RescheduleReason.CONNECTION_SETUP)) {
                    connectionError.countDown();
                }
            }
        }
    }
}