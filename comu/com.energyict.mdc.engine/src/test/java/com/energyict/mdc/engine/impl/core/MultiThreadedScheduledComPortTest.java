package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
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
import com.energyict.mdc.engine.config.InboundCapableComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.engine.impl.EngineServiceImpl;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.verification.CounterVerifierFactory;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.impl.monitor.ScheduledComPortMonitorImplMBean;
import com.energyict.mdc.engine.impl.monitor.ServerScheduledComPortOperationalStatistics;
import com.energyict.mdc.engine.monitor.ScheduledComPortMonitor;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.upl.issue.Problem;
import com.energyict.mdc.upl.issue.Warning;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.OfflineDeviceContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.LogManager;

import static com.elster.jupiter.util.Checks.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
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
    private static final long SIMULTANEOUS_CONNECTION_TASK_ID_1 = 4456;
    private static final long SIMULTANEOUS_CONNECTION_TASK_ID_2 = 1416;
    private static final long SIMULTANEOUS_CONNECTION_TASK_ID_3 = 12313;
    private static final long SERIAL_CONNECTION_TASK_ID_1 = 444;
    private static final long SERIAL_CONNECTION_TASK_ID_2 = 7778;
    private static final long SERIAL_CONNECTION_TASK_ID_3 = 6541;

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
    private static final int TIMEOUT = 120000;
    private final int PARALLEL_CONNECTIONS = 3;

    @Mock
    private ConnectionType simultaneousConnectionType;
    @Mock
    private OutboundComPortPool comPortPool;
    @Mock
    private OfflineDevice offlineDevice;
    @Mock
    private ScheduledConnectionTask simultaneousConnectionTask1;
    @Mock
    private ScheduledConnectionTask simultaneousConnectionTask2;
    @Mock
    private ScheduledConnectionTask simultaneousConnectionTask;
    @Mock
    private ConnectionType serialConnectionType;
    @Mock
    private ScheduledConnectionTask serialConnectionTask1;
    @Mock
    private ScheduledConnectionTask serialConnectionTask2;
    @Mock
    private ScheduledConnectionTask serialConnectionTask3;
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
    private ComTaskEnablement comTaskEnablement;
    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    @Mock
    private IssueService issueService;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private UserService userService;
    @Mock
    private User user;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock
    private DeviceService deviceService;
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
    private ServerScheduledComPortOperationalStatistics operationalStatistics;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private ScheduledComPortImpl.ServiceProvider serviceProvider;
    @Mock
    private RunningComServer runningComServer;

    private Clock clock = Clock.systemUTC();
    private ComPortRelatedComChannel comChannel;
    private CounterVerifierFactory counterVerifierFactory;

    @BeforeClass
    public static void initializeLogging() {
        InputStream configStream = MultiThreadedScheduledComPortTest.class.getClassLoader().getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(configStream);
        } catch (IOException e) {
            System.err.println("Logging will most likely not work as expected due to the error below.");
            e.printStackTrace(System.err);
        }
    }

    @Before
    public void setupServiceProvider() {
        when(simultaneousConnectionTask.getId()).thenReturn(SIMULTANEOUS_CONNECTION_TASK_ID_1);
        when(simultaneousConnectionTask1.getId()).thenReturn(SIMULTANEOUS_CONNECTION_TASK_ID_2);
        when(simultaneousConnectionTask2.getId()).thenReturn(SIMULTANEOUS_CONNECTION_TASK_ID_3);
        when(serialConnectionTask1.getId()).thenReturn(SERIAL_CONNECTION_TASK_ID_1);
        when(serialConnectionTask2.getId()).thenReturn(SERIAL_CONNECTION_TASK_ID_2);
        when(serialConnectionTask3.getId()).thenReturn(SERIAL_CONNECTION_TASK_ID_3);
        when(connectionTaskService.findConnectionTask(SIMULTANEOUS_CONNECTION_TASK_ID_1)).thenReturn(Optional.of(simultaneousConnectionTask));
        when(connectionTaskService.findConnectionTask(SIMULTANEOUS_CONNECTION_TASK_ID_2)).thenReturn(Optional.of(simultaneousConnectionTask1));
        when(connectionTaskService.findConnectionTask(SIMULTANEOUS_CONNECTION_TASK_ID_3)).thenReturn(Optional.of(simultaneousConnectionTask2));
        when(connectionTaskService.findConnectionTask(SERIAL_CONNECTION_TASK_ID_1)).thenReturn(Optional.of(serialConnectionTask1));
        when(connectionTaskService.findConnectionTask(SERIAL_CONNECTION_TASK_ID_2)).thenReturn(Optional.of(serialConnectionTask2));
        when(connectionTaskService.findConnectionTask(SERIAL_CONNECTION_TASK_ID_3)).thenReturn(Optional.of(serialConnectionTask3));
        when(this.serviceProvider.eventPublisher()).thenReturn(this.eventPublisher);
        when(this.serviceProvider.eventService()).thenReturn(this.eventService);
        when(this.serviceProvider.identificationService()).thenReturn(this.identificationService);
        when(this.serviceProvider.issueService()).thenReturn(this.issueService);
        when(issueService.newProblem(any(Object.class), any(MessageSeed.class))).thenReturn(mock(Problem.class));
        when(issueService.newProblem(any(Object.class), any(MessageSeed.class), any(Object.class))).thenReturn(mock(Problem.class));
        when(issueService.newProblem(any(Object.class), any(MessageSeed.class), any(Object.class), any(Object.class))).thenReturn(mock(Problem.class));
        when(issueService.newProblem(any(Object.class), any(MessageSeed.class), any(Object.class), any(Object.class), any(Object.class))).thenReturn(mock(Problem.class));
        when(issueService.newWarning(any(Object.class), any(MessageSeed.class))).thenReturn(mock(Warning.class));
        when(issueService.newWarning(any(Object.class), any(MessageSeed.class), any(Object.class))).thenReturn(mock(Warning.class));
        when(issueService.newWarning(any(Object.class), any(MessageSeed.class), any(Object.class), any(Object.class))).thenReturn(mock(Warning.class));
        when(issueService.newWarning(any(Object.class), any(MessageSeed.class), any(Object.class), any(Object.class), any(Object.class))).thenReturn(mock(Warning.class));
        when(this.serviceProvider.userService()).thenReturn(this.userService);
        when(this.userService.findUser(anyString())).thenReturn(Optional.of(user));
        when(this.user.getLocale()).thenReturn(Optional.empty());
        when(this.serviceProvider.clock()).thenReturn(this.clock);
        when(this.serviceProvider.transactionService()).thenReturn(new FakeTransactionService());
        when(this.serviceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.serviceProvider.deviceService()).thenReturn(this.deviceService);
        when(this.serviceProvider.deviceConfigurationService()).thenReturn(this.deviceConfigurationService);
        when(this.serviceProvider.engineService()).thenReturn(engineService);
        when(this.serviceProvider.threadPrincipalService()).thenReturn(threadPrincipalService);
        when(this.serviceProvider.managementBeanFactory()).thenReturn(this.managementBeanFactory);
    }

    public void setupEventPublisher() {
        this.setupServiceProvider();
    }

    @Before
    public void initializeMocksAndFactories() throws ConnectionException {
        when(this.managementBeanFactory.findOrCreateFor(any(ScheduledComPort.class))).thenReturn(this.scheduledComPortMonitor);
        ScheduledComPortMonitor comPortMonitor = (ScheduledComPortMonitor) this.scheduledComPortMonitor;
        when(comPortMonitor.getOperationalStatistics()).thenReturn(this.operationalStatistics);
        when(this.userService.findUser(anyString())).thenReturn(Optional.of(user));
        when(this.connectionTaskService.buildComSession(any(ConnectionTask.class), any(ComPortPool.class), any(ComPort.class), any(Instant.class))).
                thenReturn(comSessionBuilder);
        when(this.engineService.findDeviceCacheByDevice(any(Device.class))).thenReturn(Optional.empty());
        when(comSessionBuilder.addComTaskExecutionSession(Matchers.<ComTaskExecution>any(), any(ComTask.class), any(Instant.class))).thenReturn(comTaskExecutionSessionBuilder);
        when(this.protocolDialectProperties.getId()).thenReturn(PROTOCOL_DIALECT_PROPERTIES);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(this.deviceProtocol);
        when(this.deviceProtocolPluggableClass.getJavaClassName()).thenReturn(MultiThreadedScheduledComPortTest.class.getName());
        when(this.device.getDeviceType()).thenReturn(this.deviceType);
        when(this.device.getDeviceConfiguration()).thenReturn(this.deviceConfiguration);
        when(this.device.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(this.device.getProtocolDialectProperties(anyString())).thenReturn(Optional.<ProtocolDialectProperties>empty());
        when(this.device.getId()).thenReturn(DEVICE_ID);
        when(this.deviceCommandExecutor.getLogLevel()).thenReturn(ComServer.LogLevel.ERROR);
        when(this.device.getDeviceProtocolProperties()).thenReturn(TypedProperties.empty());
        when(comTask.getId()).thenAnswer(new Answer<Long>() {
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

        when(this.offlineDevice.getDeviceProtocolPluggableClass()).thenReturn(this.deviceProtocolPluggableClass);
        when(this.offlineDevice.getId()).thenReturn(DEVICE_ID);


        DeviceProtocolService deviceProtocolService = mock(DeviceProtocolService.class);
        when(deviceProtocolService.createProtocol(MultiThreadedScheduledComPortTest.class.getName())).thenReturn(new MultiThreadedScheduledComPortTest());

        when(this.comPortPool.getId()).thenReturn(COM_PORT_POOL_ID);
        when(this.comPortPool.getComPortType()).thenReturn(ComPortType.TCP);
        when(this.simultaneousConnectionType.allowsSimultaneousConnections()).thenReturn(true);
        when(this.simultaneousConnectionType.allowsSimultaneousConnections()).thenReturn(true);

        when(this.simultaneousConnectionTask1.getDevice()).thenReturn(this.device);
        when(this.simultaneousConnectionTask1.getConnectionType()).thenReturn(this.simultaneousConnectionType);
        when(this.simultaneousConnectionTask1.getComPortPool()).thenReturn(this.comPortPool);
        when(this.simultaneousConnectionTask1.getConnectionStrategy()).thenReturn(ConnectionStrategy.MINIMIZE_CONNECTIONS);

        when(this.simultaneousConnectionTask2.getDevice()).thenReturn(this.device);
        when(this.simultaneousConnectionTask2.getConnectionType()).thenReturn(this.simultaneousConnectionType);
        when(this.simultaneousConnectionTask2.getComPortPool()).thenReturn(this.comPortPool);
        when(this.simultaneousConnectionTask2.getConnectionStrategy()).thenReturn(ConnectionStrategy.MINIMIZE_CONNECTIONS);

        when(this.simultaneousConnectionTask.getDevice()).thenReturn(this.device);
        when(this.simultaneousConnectionTask.getConnectionType()).thenReturn(this.simultaneousConnectionType);
        when(this.simultaneousConnectionTask.getComPortPool()).thenReturn(this.comPortPool);
        when(this.simultaneousConnectionTask.getConnectionStrategy()).thenReturn(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        when(this.simultaneousConnectionTask.getNumberOfSimultaneousConnections()).thenReturn(PARALLEL_CONNECTIONS);

        when(this.serialConnectionType.allowsSimultaneousConnections()).thenReturn(false);
        when(this.serialConnectionType.getSupportedComPortTypes()).thenReturn(EnumSet.allOf(ComPortType.class));
        when(this.serialConnectionTask1.getDevice()).thenReturn(this.device);
        when(this.serialConnectionTask1.getConnectionType()).thenReturn(this.serialConnectionType);
        when(this.serialConnectionTask1.getComPortPool()).thenReturn(this.comPortPool);
        when(this.serialConnectionTask1.getConnectionStrategy()).thenReturn(ConnectionStrategy.AS_SOON_AS_POSSIBLE);
        when(this.serialConnectionTask2.getDevice()).thenReturn(this.device);
        when(this.serialConnectionTask2.getConnectionType()).thenReturn(this.serialConnectionType);
        when(this.serialConnectionTask2.getComPortPool()).thenReturn(this.comPortPool);
        when(this.serialConnectionTask2.getConnectionStrategy()).thenReturn(ConnectionStrategy.MINIMIZE_CONNECTIONS);
        when(this.serialConnectionTask3.getDevice()).thenReturn(this.device);
        when(this.serialConnectionTask3.getConnectionType()).thenReturn(this.serialConnectionType);
        when(this.serialConnectionTask3.getComPortPool()).thenReturn(this.comPortPool);
        when(this.serialConnectionTask3.getConnectionStrategy()).thenReturn(ConnectionStrategy.MINIMIZE_CONNECTIONS);
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

    private ComServerDAO getMockedComServerDAO() {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(comServerDAO.getComServerUser()).thenReturn(user);
        return comServerDAO;
    }

    @Test(timeout = TIMEOUT)
    public void minimalGetUserTest() throws InterruptedException {
        int numberOfSimultaneousConnections = 5;
        final CountDownLatch countDownLatch = new CountDownLatch(numberOfSimultaneousConnections);
        ComServerDAO comServerDAO = getMockedComServerDAO();
        OutboundComPort comPort = this.mockComPort(numberOfSimultaneousConnections, "minimalGetUserTest");
        doAnswer(invocationOnMock -> {
            countDownLatch.countDown();
            return null;
        }).when(threadPrincipalService).set(user, "MultiThreadedComPort", "Executing", Locale.ENGLISH);
        when(this.simultaneousConnectionTask.getNumberOfSimultaneousConnections()).thenReturn(numberOfSimultaneousConnections);

        final List<ComJob> work = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            work.add(this.toComJob(this.mockComTask(i + 1, this.simultaneousConnectionTask)));
        }
        final AtomicBoolean returnActualWork = new AtomicBoolean(true);
        final CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comServerDAO.findExecutableOutboundComTasks(comPort)).then(provideSingleJobFromDB(work, returnActualWork, stopLatch));

        MultiThreadedScheduledComPort scheduledComPort = new MultiThreadedScheduledComPort(runningComServer, comPort, comServerDAO, this.deviceCommandExecutor, Executors.defaultThreadFactory(), serviceProvider);

        // Business method
        scheduledComPort.start();

        countDownLatch.await();

        // Asserts
        verify(userService, never()).findUser(EngineServiceImpl.COMSERVER_USER); // the user should be 'cached' on the dao
        verify(threadPrincipalService, times(numberOfSimultaneousConnections)).set(user, "MultiThreadedComPort", "Executing", Locale.ENGLISH);
    }

    @Test(timeout = TIMEOUT)
    public void testStartWithWork() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + 1);
        ThreadFactory threadFactory = spy(new ThreadFactory() {
            private ThreadFactory actual = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
                countDownLatch.countDown();
                return actual.newThread(r);
            }
        });
        ComServerDAO comServerDAO = getMockedComServerDAO();
        OutboundComPort comPort = this.mockComPort("testStartWithWork");

        final List<ComJob> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.toComJob(this.mockComTask(i + 1, this.simultaneousConnectionTask1)));
        }
        final AtomicBoolean returnActualWork = new AtomicBoolean(true);
        final CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comServerDAO.findExecutableOutboundComTasks(comPort)).then(provideSingleJobFromDB(work, returnActualWork, stopLatch));

        MultiThreadedScheduledComPort scheduledComPort = new MultiThreadedScheduledComPort(runningComServer, comPort, comServerDAO, this.deviceCommandExecutor, threadFactory, serviceProvider);

        // Business method
        scheduledComPort.start();

        countDownLatch.await();

        // Asserts
        verify(threadFactory, times(NUMBER_OF_SIMULTANEOUS_CONNECTIONS + 1)).newThread(any(Runnable.class)); // the plus 1 is for the ScheduledComPort
    }

    @Test(timeout = TIMEOUT)
    public void testStartWithoutWork() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        ThreadFactory threadFactory = spy(new ThreadFactory() {
            private ThreadFactory actual = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
                countDownLatch.countDown();
                return actual.newThread(r);
            }
        });
        ComServerDAO comServerDAO = getMockedComServerDAO();
        OutboundComPort comPort = this.mockComPort("testStartWithWork");

        final List<ComJob> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.toComJob(this.mockComTask(i + 1, this.simultaneousConnectionTask1)));
        }
        final AtomicBoolean returnActualWork = new AtomicBoolean(true);
        final CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comServerDAO.findExecutableOutboundComTasks(comPort)).then(provideSingleJobFromDB(work, returnActualWork, stopLatch));

        MultiThreadedScheduledComPort scheduledComPort = new MultiThreadedScheduledComPort(runningComServer, comPort, comServerDAO, this.deviceCommandExecutor, threadFactory, serviceProvider);

        // Business method
        scheduledComPort.start();

        countDownLatch.await();

        // Asserts
        verify(threadFactory, times(1)).newThread(any(Runnable.class));
    }

    @Test
    public void startParallelTest() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(4); // one for the comport - one for the parallelRoot - two for the workers
        ThreadFactory threadFactory = spy(new ThreadFactory() {

            private ThreadFactory actual = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
                countDownLatch.countDown();
                return actual.newThread(r);
            }
        });
        ComServerDAO comServerDAO = getMockedComServerDAO();
        OutboundComPort comPort = this.mockComPort("startParallelWithoutWorkTest");

        final List<ComJob> work = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            work.add(this.toComJob(this.mockComTask(i + 1, this.simultaneousConnectionTask)));
        }
        final AtomicBoolean returnActualWork = new AtomicBoolean(true);
        final CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comServerDAO.findExecutableOutboundComTasks(comPort)).then(provideSingleJobFromDB(work, returnActualWork, stopLatch));

        MultiThreadedScheduledComPort scheduledComPort = new MultiThreadedScheduledComPort(runningComServer, comPort, comServerDAO, this.deviceCommandExecutor, threadFactory, serviceProvider);

        // Business method
        scheduledComPort.start();

        countDownLatch.await();

        // Asserts
        verify(threadFactory, times(4)).newThread(any(Runnable.class));  // one for the comport - one for the parallelRoot - two for the workers
    }

    @Test
    public void startWithMoreParallelThanComportCanHandleTest() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(4); // one for the comport - one for the parallelRoot - two for the workers
        ThreadFactory threadFactory = spy(new ThreadFactory() {

            private ThreadFactory actual = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
                countDownLatch.countDown();
                return actual.newThread(r);
            }
        });
        ComServerDAO comServerDAO = getMockedComServerDAO();
        OutboundComPort comPort = this.mockComPort("startWithMoreParallelThanComportCanHandleTest");

        when(simultaneousConnectionTask.getNumberOfSimultaneousConnections()).thenReturn(1000);

        final List<ComJob> work = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            work.add(this.toComJob(this.mockComTask(i + 1, this.simultaneousConnectionTask)));
        }
        final AtomicBoolean returnActualWork = new AtomicBoolean(true);
        final CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comServerDAO.findExecutableOutboundComTasks(comPort)).then(provideSingleJobFromDB(work, returnActualWork, stopLatch));

        MultiThreadedScheduledComPort scheduledComPort = new MultiThreadedScheduledComPort(runningComServer, comPort, comServerDAO, this.deviceCommandExecutor, threadFactory, serviceProvider);

        // Business method
        scheduledComPort.start();

        countDownLatch.await();

        // despite the fact that we allow 1000 parallel connections, we will only be able to create 3 of them (and one for the comport)
        // Asserts
        verify(threadFactory, times(4)).newThread(any(Runnable.class));  // one for the comport - one for the parallelRoot - two for the workers
    }

    private Answer<List<ComJob>> provideSingleJobFromDB(final List<ComJob> work, final AtomicBoolean returnActualWork, final CountDownLatch stopLatch) {
        return new Answer<List<ComJob>>() {
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
        };
    }

    @Test(timeout = TIMEOUT)
    public void testExecuteTasksWithNoWork() throws InterruptedException {
        ComServerDAO comServerDAO = getMockedComServerDAO();
        OutboundComPort comPort = this.mockComPort("testExecuteTasksWithNoWork");
        when(comServerDAO.refreshComPort(comPort)).thenReturn(comPort);
        MultiThreadedScheduledComPort scheduledComPort = new MultiThreadedScheduledComPort(runningComServer, comPort, comServerDAO, deviceCommandExecutor, this.serviceProvider);
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
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = TIMEOUT)
    public void startupCleansTasks() throws InterruptedException {
        ComServerDAO comServerDAO = getMockedComServerDAO();
        OutboundComPort comPort = this.mockComPort("startupCleansInterruptedTasks");
        when(comServerDAO.refreshComPort(comPort)).thenReturn(comPort);
        MultiThreadedScheduledComPort scheduledComPort = new MultiThreadedScheduledComPort(runningComServer, comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
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
            verify(comServerDAO, times(1)).releaseTasksFor(comPort);
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = TIMEOUT)
    public void testExecuteTasksInParallel() throws InterruptedException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = getMockedComServerDAO();
        OutboundComPort comPort = this.mockComPort("testExecuteTasksInParallel");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.createComSession(any(ComSessionBuilder.class), any(Instant.class), any(ComSession.SuccessIndicator.class))).thenReturn(mock(ComSession.class));
        when(comServerDAOMock.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(true);
        this.comChannel = new ComPortRelatedComChannelImpl(mock(ComChannel.class), comPort, clock, this.hexService, eventPublisher);
        when(this.simultaneousConnectionTask1.connect(eq(comPort), anyList())).thenReturn(this.comChannel);
        final List<ComJob> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.toComJob(this.mockComTask(i + 1, this.simultaneousConnectionTask1)));
        }
        final AtomicBoolean returnActualWork = new AtomicBoolean(true);
        final CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comServerDAOMock.findExecutableOutboundComTasks(comPort)).then(provideSingleJobFromDB(work, returnActualWork, stopLatch));

        when(comServerDAOMock.findOfflineDevice(any(DeviceIdentifier.class), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineDevice));
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.acquireTokens(1)).thenReturn(tokens);

        when(this.deviceCommandExecutor.execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class))).thenAnswer(
                invocationOnMock -> {
                    DeviceCommand deviceCommand = (DeviceCommand) invocationOnMock.getArguments()[0];
                    deviceCommand.execute(comServerDAO);
                    return null;
                }
        );

        AlwaysAttemptLockMultiThreadedScheduledComPort scheduledComPort = new AlwaysAttemptLockMultiThreadedScheduledComPort(runningComServer, comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider);

        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(comServerDAOMock.getDeviceProtocolSecurityProperties(Matchers.<DeviceIdentifier>any(), any(InboundComPort.class))).thenReturn(Collections.<SecurityProperty>emptyList());

        try {
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionStarted(mock(ComTaskExecution.class), comPort, true);
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionCompleted(mock(ComTaskExecution.class));
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionCompleted(this.simultaneousConnectionTask1);
            verify(this.deviceCommandExecutor, atLeastOnce()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
            verify(this.deviceCommandExecutor, atMost(NUMBER_OF_TASKS)).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = TIMEOUT)
    public void testExecuteTasksInParallelWithConnectionFailure() throws InterruptedException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = getMockedComServerDAO();
        OutboundComPort comPort = this.mockComPort("testExecuteTasksInParallelWithConnectionFailure");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(true);
        when(comServerDAOMock.createComSession(any(ComSessionBuilder.class), any(Instant.class), any(ComSession.SuccessIndicator.class))).thenReturn(mock(ComSession.class));
        // Force the connection to fail
        doThrow(ConnectionException.class).when(this.simultaneousConnectionTask1).connect(eq(comPort), anyList());
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
                return result;
            }
        });
        //when(comServerDAOMock.findPropertiesForConnectionTask(any(Integer.class))).thenReturn(new ArrayList<ConnectionTaskProperty>());
        when(comServerDAOMock.findOfflineDevice(any(DeviceIdentifier.class), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineDevice));
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.tryAcquireTokens(1)).thenReturn(tokens);

        when(this.deviceCommandExecutor.execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class))).thenAnswer(
                invocationOnMock -> {
                    DeviceCommand deviceCommand = (DeviceCommand) invocationOnMock.getArguments()[0];
                    deviceCommand.execute(comServerDAO);
                    return null;
                }
        );

        NotExecutedTaskMultiThreadedScheduledComPort scheduledComPort = new NotExecutedTaskMultiThreadedScheduledComPort(runningComServer, comPort, comServerDAO, this.deviceCommandExecutor, serviceProvider, stopLatch);

        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(comServerDAOMock.getDeviceProtocolSecurityProperties(Matchers.<DeviceIdentifier>any(), any(InboundComPort.class))).thenReturn(Collections.<SecurityProperty>emptyList());

        try {
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.atLeast(6)).executionStarted(mock(ComTaskExecution.class), comPort, true);
            comServerDAO.verify(this.counterVerifierFactory.atLeast(6)).executionRescheduled(mock(ComTaskExecution.class), Instant.now());
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionFailed(this.simultaneousConnectionTask1);
            verify(this.deviceCommandExecutor, atLeastOnce()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
            verify(this.deviceCommandExecutor, atMost(NUMBER_OF_TASKS)).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = TIMEOUT)
    public void testExecuteTasksOneByOne() throws InterruptedException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = getMockedComServerDAO();
        OutboundComPort comPort = this.mockComPort("testExecuteTasksOneByOne");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.createComSession(any(ComSessionBuilder.class), any(Instant.class), any(ComSession.SuccessIndicator.class))).thenReturn(mock(ComSession.class));
        when(comServerDAOMock.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(true);
        this.comChannel = new ComPortRelatedComChannelImpl(mock(ComChannel.class), comPort, clock, this.hexService, eventPublisher);
        when(this.serialConnectionTask1.connect(eq(comPort), anyList())).thenReturn(this.comChannel);
        final List<ComTaskExecution> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.mockComTask(i + 1, this.serialConnectionTask1));
        }
        final AtomicBoolean returnActualWork = new AtomicBoolean(true);
        final CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comServerDAOMock.findExecutableOutboundComTasks(comPort)).then(invocation -> {
            List<ComJob> result;
            if (returnActualWork.getAndSet(false)) {
                result = toComJob(work);
            } else {
                result = new ArrayList<>(0);
            }
            stopLatch.countDown();
            return result;
        });
        when(comServerDAOMock.findOfflineDevice(any(DeviceIdentifier.class), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineDevice));
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.acquireTokens(1)).thenReturn(tokens);
        AlwaysAttemptLockMultiThreadedScheduledComPort scheduledComPort = new AlwaysAttemptLockMultiThreadedScheduledComPort(runningComServer, comPort, comServerDAO, this.deviceCommandExecutor, serviceProvider);

        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(comServerDAOMock.getDeviceProtocolSecurityProperties(Matchers.<DeviceIdentifier>any(), any(InboundComPort.class))).thenReturn(Collections.<SecurityProperty>emptyList());

        when(this.deviceCommandExecutor.execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class))).thenAnswer(
                invocationOnMock -> {
                    DeviceCommand deviceCommand = (DeviceCommand) invocationOnMock.getArguments()[0];
                    deviceCommand.execute(comServerDAO);
                    return null;
                }
        );

        try {
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionStarted(mock(ComTaskExecution.class), comPort, true);
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionCompleted(mock(ComTaskExecution.class));
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionCompleted(this.serialConnectionTask1);

            Thread.sleep(this.counterVerifierFactory.getIdleMillis());
            verify(this.deviceCommandExecutor, times(1)).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = TIMEOUT)
    public void testExecuteTasksOneByOneWithConnectionFailure() throws InterruptedException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = getMockedComServerDAO();
        OutboundComPort comPort = this.mockComPort("testExecuteTasksOneByOneWithConnectionFailure");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(true);
        // Force the connection to fail
        doThrow(ConnectionException.class).when(this.serialConnectionTask1).connect(eq(comPort), anyList());
        final List<ComTaskExecution> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.mockComTask(i + 1, this.serialConnectionTask1));
        }
        final AtomicBoolean returnActualWork = new AtomicBoolean(true);
        final CountDownLatch stopLatch = new CountDownLatch(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(comServerDAOMock.findExecutableOutboundComTasks(comPort)).then(invocation -> {
            List<ComJob> result;
            if (returnActualWork.getAndSet(false)) {
                result = toComJob(work);
            } else {
                result = new ArrayList<>(0);
            }
            stopLatch.countDown();
            return result;
        });
        when(comServerDAOMock.findOfflineDevice(any(DeviceIdentifier.class), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineDevice));
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.acquireTokens(1)).thenReturn(tokens);
        AlwaysAttemptLockMultiThreadedScheduledComPort scheduledComPort = new AlwaysAttemptLockMultiThreadedScheduledComPort(runningComServer, comPort, comServerDAO, this.deviceCommandExecutor, serviceProvider);


        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(comServerDAOMock.getDeviceProtocolSecurityProperties(Matchers.<DeviceIdentifier>any(), any(InboundComPort.class))).thenReturn(Collections.<SecurityProperty>emptyList());


        try {
            // Business method
            scheduledComPort.start();

            // Wait for all processes
            stopLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionStarted(mock(ComTaskExecution.class), comPort, true);
            comServerDAO.verify(this.counterVerifierFactory.atMost(work.size())).executionStarted(mock(ComTaskExecution.class), comPort, true);
            verify(this.deviceCommandExecutor, times(1)).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }

    }

    @Test(timeout = TIMEOUT)
    public void testExecuteTasksInParallelWithWorkThatIsAlwaysStolenByOtherComponents() throws InterruptedException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = getMockedComServerDAO();
        OutboundComPort comPort = this.mockComPort("testExecuteTasksInParallelWithWorkThatIsAlwaysStolenByOtherComponents");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(true);
        this.comChannel = new ComPortRelatedComChannelImpl(mock(ComChannel.class), comPort, clock, this.hexService, eventPublisher);
        when(this.simultaneousConnectionTask1.connect(eq(comPort), anyList())).thenReturn(this.comChannel);
        final List<ComJob> noWork = new ArrayList<>(0);
        final List<ComJob> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.toComJob(this.mockComTask(i + 1, this.simultaneousConnectionTask1)));
        }
        CountDownLatch workDeliveredLatch = new CountDownLatch(1);
        final Set<Thread> threadsThatHaveAskedForWork = new HashSet<>();
        when(comServerDAOMock.findExecutableOutboundComTasks(comPort)).thenAnswer(invocation -> {
            if (threadsThatHaveAskedForWork.contains(Thread.currentThread())) {
                return noWork;
            } else {
                threadsThatHaveAskedForWork.add(Thread.currentThread());
                return work;
            }
        });
        when(comServerDAOMock.findOfflineDevice(any(DeviceIdentifier.class), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineDevice));
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        DeviceCommandExecutionToken deviceCommandExecutionToken = mock(DeviceCommandExecutionToken.class);
        when(this.deviceCommandExecutor.acquireTokens(1)).thenReturn(Arrays.<DeviceCommandExecutionToken>asList(deviceCommandExecutionToken));
        NeverAttemptLockMultiThreadedScheduledComPort scheduledComPort = new NeverAttemptLockMultiThreadedScheduledComPort(runningComServer, comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider, workDeliveredLatch);

        try {
            // Business method
            scheduledComPort.start();

            // Wait until the first job is delivered
            workDeliveredLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionStarted(mock(ComTaskExecution.class), comPort, true);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionCompleted(mock(ComTaskExecution.class));
            comServerDAO.verify(this.counterVerifierFactory.never()).executionCompleted(this.simultaneousConnectionTask1);
            verify(this.deviceCommandExecutor, never()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
            verify(this.deviceCommandExecutor, atLeastOnce()).free(any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = TIMEOUT)
    public void testExecuteTasksOneByOneWithWorkThatIsAlwaysStolenByOtherComponents() throws InterruptedException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = getMockedComServerDAO();
        OutboundComPort comPort = this.mockComPort("testExecuteTasksOneByOneWithWorkThatIsAlwaysStolenByOtherComponents");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(true);
        this.comChannel = new ComPortRelatedComChannelImpl(mock(ComChannel.class), comPort, clock, this.hexService, eventPublisher);
        when(this.serialConnectionTask1.connect(eq(comPort), anyList())).thenReturn(this.comChannel);
        final List<ComTaskExecution> work = new ArrayList<>();
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
        when(comServerDAOMock.findOfflineDevice(any(DeviceIdentifier.class), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineDevice));
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.acquireTokens(1)).thenReturn(tokens);
        NeverAttemptLockMultiThreadedScheduledComPort scheduledComPort = new NeverAttemptLockMultiThreadedScheduledComPort(runningComServer, comPort, comServerDAO, this.deviceCommandExecutor, this.serviceProvider, workDeliveredLatch);

        try {
            // Business method
            scheduledComPort.start();

            // Wait until the first job is delivered
            workDeliveredLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionStarted(mock(ComTaskExecution.class), comPort, true);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionCompleted(mock(ComTaskExecution.class));
            comServerDAO.verify(this.counterVerifierFactory.never()).executionCompleted(this.serialConnectionTask1);
            verify(this.deviceCommandExecutor, never()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
            verify(this.deviceCommandExecutor, atLeastOnce()).free(any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = TIMEOUT)
    public void testExecuteTasksInParallelWithWorkThatWasAlreadyExecutedByOtherComponents() throws InterruptedException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = getMockedComServerDAO();
        OutboundComPort comPort = this.mockComPort("testExecuteTasksInParallelWithWorkThatIsAlwaysStolenByOtherComponents");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.isStillPending(anyLong())).thenReturn(false);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(false);
        this.comChannel = new ComPortRelatedComChannelImpl(mock(ComChannel.class), comPort, clock, this.hexService, eventPublisher);
        when(this.simultaneousConnectionTask1.connect(eq(comPort), anyList())).thenReturn(this.comChannel);
        final List<ComJob> noWork = new ArrayList<>(0);
        final List<ComJob> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.toComJob(this.mockComTask(i + 1, this.simultaneousConnectionTask1)));
        }
        CountDownLatch workDeliveredLatch = new CountDownLatch(1);
        final Set<Thread> threadsThatHaveAskedForWork = new HashSet<>();
        when(comServerDAOMock.findExecutableOutboundComTasks(comPort)).thenAnswer(invocation -> {
            if (threadsThatHaveAskedForWork.contains(Thread.currentThread())) {
                return noWork;
            } else {
                threadsThatHaveAskedForWork.add(Thread.currentThread());
                return work;
            }
        });
        when(comServerDAOMock.findOfflineDevice(any(DeviceIdentifier.class), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineDevice));
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        DeviceCommandExecutionToken deviceCommandExecutionToken = mock(DeviceCommandExecutionToken.class);
        when(this.deviceCommandExecutor.acquireTokens(1)).thenReturn(Arrays.<DeviceCommandExecutionToken>asList(deviceCommandExecutionToken));
        AlwaysAttemptLockMultiThreadedScheduledComPort scheduledComPort = new AlwaysAttemptLockMultiThreadedScheduledComPort(runningComServer, comPort, comServerDAO, this.deviceCommandExecutor, serviceProvider, workDeliveredLatch);

        try {
            // Business method
            scheduledComPort.start();

            // Wait until the first job is delivered
            workDeliveredLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionStarted(mock(ComTaskExecution.class), comPort, true);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionCompleted(mock(ComTaskExecution.class));
            comServerDAO.verify(this.counterVerifierFactory.never()).executionCompleted(this.simultaneousConnectionTask1);
            verify(this.deviceCommandExecutor, never()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = TIMEOUT)
    public void testExecuteTasksOneByOneWithWorkThatWasAlreadyExecutedByOtherComponents() throws InterruptedException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = getMockedComServerDAO();
        OutboundComPort comPort = this.mockComPort("testExecuteTasksOneByOneWithWorkThatIsAlwaysStolenByOtherComponents");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.isStillPending(anyLong())).thenReturn(false);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(false);
        this.comChannel = new ComPortRelatedComChannelImpl(mock(ComChannel.class), comPort, clock, this.hexService, eventPublisher);
        when(this.serialConnectionTask1.connect(eq(comPort), anyList())).thenReturn(this.comChannel);
        final List<ComTaskExecution> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.mockComTask(i + 1, this.serialConnectionTask1));
        }
        CountDownLatch workDeliveredLatch = new CountDownLatch(1);
        final List<ComJob> noJobs = new ArrayList<>(0);
        final List<ComJob> jobs = this.toComJob(work);
        final Set<Thread> threadsThatHaveAskedForWork = new HashSet<>();
        when(comServerDAOMock.findExecutableOutboundComTasks(comPort)).thenAnswer(invocation -> {
            if (threadsThatHaveAskedForWork.contains(Thread.currentThread())) {
                return noJobs;
            } else {
                threadsThatHaveAskedForWork.add(Thread.currentThread());
                return jobs;
            }
        });
        when(comServerDAOMock.findOfflineDevice(any(DeviceIdentifier.class), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineDevice));
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.acquireTokens(1)).thenReturn(tokens);
        AlwaysAttemptLockMultiThreadedScheduledComPort scheduledComPort = new AlwaysAttemptLockMultiThreadedScheduledComPort(runningComServer, comPort, comServerDAO, this.deviceCommandExecutor, serviceProvider, workDeliveredLatch);

        try {
            // Business method
            scheduledComPort.start();

            // Wait until the first job is delivered
            workDeliveredLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionStarted(mock(ComTaskExecution.class), comPort, true);
            comServerDAO.verify(this.counterVerifierFactory.never()).executionCompleted(mock(ComTaskExecution.class));
            comServerDAO.verify(this.counterVerifierFactory.never()).executionCompleted(this.serialConnectionTask1);
            verify(this.deviceCommandExecutor, never()).execute(any(DeviceCommand.class), any(DeviceCommandExecutionToken.class));
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    @Test(timeout = TIMEOUT)
    public void testConnectionFailureReschedulesTask() throws InterruptedException, SQLException, ConnectionException {
        ComServerDAO comServerDAOMock = getMockedComServerDAO();
        OutboundComPort comPort = this.mockComPort("testConnectionFailureReschedulesTask");
        when(comServerDAOMock.refreshComPort(comPort)).thenReturn(comPort);
        when(comServerDAOMock.isStillPending(anyLong())).thenReturn(true);
        when(comServerDAOMock.areStillPending(anyCollection())).thenReturn(true);
        doThrow(ConnectionException.class).when(this.serialConnectionTask1).connect(eq(comPort), anyList());
        final List<ComTaskExecution> work = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_TASKS; i++) {
            work.add(this.mockComTask(i + 1, this.serialConnectionTask1));
        }
        CountDownLatch workDeliveredLatch = new CountDownLatch(1);
        final List<ComJob> jobs = this.toComJob(work);
        final List<ComJob> noJobs = new ArrayList<>(0);
        final Set<Thread> threadsThatHaveAskedForWork = new HashSet<>();
        when(comServerDAOMock.findExecutableOutboundComTasks(comPort)).thenAnswer(invocation -> {
            if (threadsThatHaveAskedForWork.contains(Thread.currentThread())) {
                return noJobs;
            } else {
                threadsThatHaveAskedForWork.add(Thread.currentThread());
                return jobs;
            }
        });
        when(comServerDAOMock.findOfflineDevice(any(DeviceIdentifier.class), any(OfflineDeviceContext.class))).thenReturn(Optional.of(offlineDevice));
        MonitoringComServerDAO comServerDAO = new MonitoringComServerDAO(comServerDAOMock);
        List<DeviceCommandExecutionToken> tokens = this.mockTokens(1);
        when(this.deviceCommandExecutor.acquireTokens(1)).thenReturn(tokens);
        AlwaysAttemptLockMultiThreadedScheduledComPort scheduledComPort = new AlwaysAttemptLockMultiThreadedScheduledComPort(runningComServer, comPort, comServerDAO, this.deviceCommandExecutor, serviceProvider, workDeliveredLatch);

        SecurityPropertySet securityPropertySet = mock(SecurityPropertySet.class);
        when(comTaskEnablement.getSecurityPropertySet()).thenReturn(securityPropertySet);
        when(comServerDAOMock.getDeviceProtocolSecurityProperties(Matchers.<DeviceIdentifier>any(), any(InboundComPort.class))).thenReturn(Collections.<SecurityProperty>emptyList());


        try {
            // Business method
            scheduledComPort.start();

            // Wait until the first job is delivered
            workDeliveredLatch.await();

            // Asserts
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).findExecutableOutboundComTasks(comPort);
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionStarted(mock(ComTaskExecution.class), comPort, true);
            comServerDAO.verify(this.counterVerifierFactory.atLeastOnce()).executionRescheduled(mock(ComTaskExecution.class), Instant.now());
        } finally {
            this.shutdown(scheduledComPort);
        }
    }

    private OutboundComPort mockComPort(String name) {
        return this.mockComPort(NUMBER_OF_SIMULTANEOUS_CONNECTIONS, name);
    }

    private OutboundComPort mockComPort(int numberOfSimultaneousConnections, String name) {
        InboundCapableComServer comServer = mock(InboundCapableComServer.class);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(new TimeDuration(1, TimeDuration.TimeUnit.SECONDS));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.TRACE);
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getName()).thenReturn("MultiThreadedScheduledComPortTest#" + name);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(numberOfSimultaneousConnections);
        when(comPort.getComServer()).thenReturn(comServer);
        return comPort;
    }

    private ComTaskExecution mockComTask(long id, OutboundConnectionTask connectionTask) {
        ManuallyScheduledComTaskExecution comTaskExecution = mock(ManuallyScheduledComTaskExecution.class, withSettings().extraInterfaces(ComTaskExecution.class));
        when(comTaskExecution.getId()).thenReturn(id);
        when(comTaskExecution.getConnectionTask()).thenReturn(Optional.of(connectionTask));
        when(comTaskExecution.getDevice()).thenReturn(this.device);
        when(comTaskExecution.getComTasks()).thenReturn(Arrays.asList(this.comTask));
        when(comTaskExecution.getProtocolDialectConfigurationProperties()).thenReturn(this.protocolDialectConfigurationProperties);
        return comTaskExecution;
    }

    private ComJob toComJob(ComTaskExecution comTask) {
        ComTaskExecutionGroup comTaskExecutionGroup = new ComTaskExecutionGroup(((OutboundConnectionTask) comTask.getConnectionTask().get()).getId());
        comTaskExecutionGroup.add(comTask);
        return comTaskExecutionGroup;
    }

    private List<ComJob> toComJob(List<ComTaskExecution> serialComTasks) {
        ScheduledConnectionTask connectionTask = (ScheduledConnectionTask) serialComTasks.get(0).getConnectionTask().get();
        ComTaskExecutionGroup group = new ComTaskExecutionGroup(connectionTask.getId());
        for (ComTaskExecution comTask : serialComTasks) {
            group.add(comTask);
        }
        List<ComJob> jobs = new ArrayList<>(1);
        jobs.add(group);
        return jobs;
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

        private AlwaysAttemptLockMultiThreadedScheduledComPort(RunningComServer runningComServer, OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider) {
            this(runningComServer, comPort, comServerDAO, deviceCommandExecutor, serviceProvider, new CountDownLatch(0));
        }

        private AlwaysAttemptLockMultiThreadedScheduledComPort(RunningComServer runningComServer, OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, CountDownLatch workDeliveredLatch) {
            super(runningComServer, comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
            this.workDeliveredLatch = workDeliveredLatch;
        }

        @Override
        protected ScheduledComTaskExecutionGroup newComTaskGroup(ScheduledConnectionTask connectionTask) {
            return new AlwaysAttemptComTaskExecutionGroup(this.getComPort(), this.getComServerDAO(), deviceCommandExecutor, serviceProvider, connectionTask);
        }

        public class AlwaysAttemptComTaskExecutionGroup extends ScheduledComTaskExecutionGroup {

            public AlwaysAttemptComTaskExecutionGroup(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, ScheduledConnectionTask connectionTask) {
                super(comPort, comServerDAO, deviceCommandExecutor, connectionTask, serviceProvider);
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

    private class NotExecutedTaskMultiThreadedScheduledComPort extends MultiThreadedScheduledComPort {

        private CountDownLatch workDeliveredLatch;

        private NotExecutedTaskMultiThreadedScheduledComPort(RunningComServer runningComServer, OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, CountDownLatch workDeliveredLatch) {
            super(runningComServer, comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
            this.workDeliveredLatch = workDeliveredLatch;
        }

        @Override
        protected ScheduledComTaskExecutionGroup newComTaskGroup(ScheduledConnectionTask connectionTask) {
            return new AlwaysAttemptComTaskExecutionGroup(this.getComPort(), this.getComServerDAO(), deviceCommandExecutor, serviceProvider, connectionTask);
        }

        public class AlwaysAttemptComTaskExecutionGroup extends ScheduledComTaskExecutionGroup {

            public AlwaysAttemptComTaskExecutionGroup(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, ScheduledConnectionTask connectionTask) {
                super(comPort, comServerDAO, deviceCommandExecutor, connectionTask, serviceProvider);
            }

            @Override
            public void closeConnection() {
                super.closeConnection();
                workDeliveredLatch.countDown();
            }

            @Override
            protected boolean attemptLock(ScheduledConnectionTask connectionTask) {
                return true;
            }

            @Override
            public boolean isStillPending() {
                return super.isStillPending();
            }
        }
    }

    private class NeverAttemptLockMultiThreadedScheduledComPort extends MultiThreadedScheduledComPort {

        private CountDownLatch workDeliveredLatch;

        private NeverAttemptLockMultiThreadedScheduledComPort(RunningComServer runningComServer, OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ServiceProvider serviceProvider, CountDownLatch workDeliveredLatch) {
            super(runningComServer, comPort, comServerDAO, deviceCommandExecutor, serviceProvider);
            this.workDeliveredLatch = workDeliveredLatch;
        }

        @Override
        protected ScheduledComTaskExecutionGroup newComTaskGroup(ScheduledConnectionTask connectionTask) {
            return new NeverAttemptComTaskExecutionGroup(this.getComPort(), this.getComServerDAO(), deviceCommandExecutor, connectionTask);
        }

        public class NeverAttemptComTaskExecutionGroup extends ScheduledComTaskExecutionGroup {
            public NeverAttemptComTaskExecutionGroup(OutboundComPort comPort, ComServerDAO comServerDAO, DeviceCommandExecutor deviceCommandExecutor, ScheduledConnectionTask connectionTask) {
                super(comPort, comServerDAO, deviceCommandExecutor, connectionTask, serviceProvider);
            }


            @Override
            protected boolean attemptLock(ScheduledConnectionTask connectionTask) {
                workDeliveredLatch.countDown();
                return false;
            }

        }
    }
}