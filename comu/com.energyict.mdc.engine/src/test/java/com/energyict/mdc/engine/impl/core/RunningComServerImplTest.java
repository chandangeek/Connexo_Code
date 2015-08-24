package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.RemoteComServer;
import com.energyict.mdc.engine.impl.core.factories.ComPortListenerFactory;
import com.energyict.mdc.engine.impl.core.factories.ScheduledComPortFactory;
import com.energyict.mdc.engine.impl.monitor.ComServerMonitor;
import com.energyict.mdc.engine.impl.monitor.ComServerMonitorImplMBean;
import com.energyict.mdc.engine.impl.monitor.EventAPIStatistics;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.sql.SQLException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link RunningComServerImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (10:40)
 */
@RunWith(MockitoJUnitRunner.class)
public class RunningComServerImplTest {

    private static final String COMSERVER_NAME = "RunningComserverImplTest";

    @Mock
    private EmbeddedWebServerFactory embeddedWebServerFactory;
    @Mock
    private EngineConfigurationService engineConfigurationService;
    @Mock
    private EngineService engineService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private ManagementBeanFactory managementBeanFactory;
    @Mock(extraInterfaces = ComServerMonitor.class)
    private ComServerMonitorImplMBean comServerMonitor;
    @Mock
    private EventAPIStatistics eventApiStatistics;
    @Mock
    private RunningComServerImpl.ServiceProvider serviceProvider;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;

    private Clock clock = Clock.systemDefaultZone();

    @Before
    public void setupManagementBeanFactory() {
        when(this.managementBeanFactory.findOrCreateFor(any(RunningComServer.class))).thenReturn(this.comServerMonitor);
        ComServerMonitor comServerMonitor = (ComServerMonitor) this.comServerMonitor;
        when(comServerMonitor.getEventApiStatistics()).thenReturn(this.eventApiStatistics);
    }

    @Before
    public void setupServiceProvider() {
        when(this.serviceProvider.clock()).thenReturn(this.clock);
        when(this.serviceProvider.engineConfigurationService()).thenReturn(this.engineConfigurationService);
        when(this.serviceProvider.engineService()).thenReturn(this.engineService);
        when(this.serviceProvider.deviceService()).thenReturn(this.deviceService);
        when(this.serviceProvider.managementBeanFactory()).thenReturn(this.managementBeanFactory);
        when(this.serviceProvider.nlsService()).thenReturn(this.nlsService);
        when(this.nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(this.thesaurus);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
    }

    @Test
    public void testConstructorWithoutComPorts() throws SQLException, BusinessException {
        OnlineComServer comServer = mock(OnlineComServer.class);
        doThrow(BusinessException.class).when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getInboundComPorts()).thenReturn(new ArrayList<>(0));
        when(comServer.getOutboundComPorts()).thenReturn(new ArrayList<>(0));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(ComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(ComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(ComServer.MINIMUM_STORE_TASK_QUEUE_SIZE);
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ThreadFactory threadFactory = mock(ThreadFactory.class);

        // Business method
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        new RunningOnlineComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, mock(CleanupDuringStartup.class), this.serviceProvider);

        // Asserts
        verify(scheduledComPortFactory, times(0)).newFor(any(OutboundComPort.class));
        verify(comPortListenerFactory, times(0)).newFor(any(InboundComPort.class));
        verify(threadFactory, times(0)).newThread(any(Runnable.class));
    }

    @Test
    public void testConstructorWithSomeComPorts() throws BusinessException {
        int numberOfInactiveInboundComPorts = 2;
        int numberOfInactiveOutboundComPorts = 3;
        OnlineComServer comServer = mock(OnlineComServer.class);
        doThrow(BusinessException.class).when(comServer).getEventRegistrationUriIfSupported();
        List<InboundComPort> inboundComPorts = new ArrayList<>();
        for (int i = 0; i < numberOfInactiveInboundComPorts; i++) {
            inboundComPorts.add(this.inboundComPort(comServer));
        }
        when(comServer.getInboundComPorts()).thenReturn(inboundComPorts);

        List<OutboundComPort> outboundComPorts = new ArrayList<>();
        for (int i = 0; i < numberOfInactiveOutboundComPorts; i++) {
            outboundComPorts.add(this.outboundComPort(comServer));
        }
        when(comServer.getOutboundComPorts()).thenReturn(outboundComPorts);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(ComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(ComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(ComServer.MINIMUM_STORE_TASK_QUEUE_SIZE);

        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ThreadFactory threadFactory = mock(ThreadFactory.class);

        // Business method
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        new RunningOnlineComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, mock(CleanupDuringStartup.class), this.serviceProvider);

        // Asserts
        verify(scheduledComPortFactory, times(numberOfInactiveOutboundComPorts)).newFor(any(OutboundComPort.class));
        verify(comPortListenerFactory, times(numberOfInactiveInboundComPorts)).newFor(any(InboundComPort.class));
        verify(threadFactory, times(0)).newThread(any(Runnable.class));
    }

    @Test
    public void testStartOnlineWithoutComPortsAndRemoteComServers() throws SQLException, BusinessException {
        OnlineComServer comServer = mock(OnlineComServer.class);
        doThrow(BusinessException.class).when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getInboundComPorts()).thenReturn(new ArrayList<>(0));
        when(comServer.getOutboundComPorts()).thenReturn(new ArrayList<>(0));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(ComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(ComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(ComServer.MINIMUM_STORE_TASK_QUEUE_SIZE);
        when(comServer.getName()).thenReturn(COMSERVER_NAME);
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread timeOutMonitorThread = this.mockedThread();
        Thread changesMonitorThread = this.mockedThread();
        Thread shutdownHookTread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(timeOutMonitorThread, changesMonitorThread, shutdownHookTread);
        List<RemoteComServer> noRemoteComServers = new ArrayList<>(0);
        when(this.engineConfigurationService.findRemoteComServersForOnlineComServer(comServer)).thenReturn(noRemoteComServers);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        CleanupDuringStartup cleanupDuringStartup = mock(CleanupDuringStartup.class);
        RunningOnlineComServerImpl runningComServer = new RunningOnlineComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, cleanupDuringStartup, this.serviceProvider);

        // Business method
        runningComServer.start();

        // Asserts
        verify(threadFactory, times(3)).newThread(any(Runnable.class));
        verify(changesMonitorThread).start();
        verify(timeOutMonitorThread).start();
        verify(cleanupDuringStartup).releaseInterruptedTasks();
        verify(this.engineConfigurationService).findRemoteComServersForOnlineComServer(comServer);
        verify(this.embeddedWebServerFactory, never()).findOrCreateRemoteQueryWebServer(runningComServer);
    }

    @Test
    public void testStartOnlineWithRemoteComServers() throws SQLException, BusinessException {
        OnlineComServer comServer = mock(OnlineComServer.class);
        doThrow(BusinessException.class).when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getInboundComPorts()).thenReturn(new ArrayList<>(0));
        when(comServer.getOutboundComPorts()).thenReturn(new ArrayList<>(0));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(ComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(ComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(ComServer.MINIMUM_STORE_TASK_QUEUE_SIZE);
        when(comServer.getName()).thenReturn(COMSERVER_NAME);
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread timeOutMonitorThread = this.mockedThread();
        Thread changesMonitorThread = this.mockedThread();
        Thread shutdownHookTread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(timeOutMonitorThread, changesMonitorThread, shutdownHookTread);
        RemoteComServer remoteComServer = mock(RemoteComServer.class);
        when(this.engineConfigurationService.findRemoteComServersForOnlineComServer(comServer)).thenReturn(Arrays.asList(remoteComServer));
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);
        EmbeddedWebServer queryWebServer = mock(EmbeddedWebServer.class);

        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        CleanupDuringStartup cleanupDuringStartup = mock(CleanupDuringStartup.class);
        RunningOnlineComServerImpl runningComServer = new RunningOnlineComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, this.embeddedWebServerFactory, cleanupDuringStartup, this.serviceProvider);
        when(this.embeddedWebServerFactory.findOrCreateRemoteQueryWebServer(runningComServer)).thenReturn(queryWebServer);

        // Business method
        runningComServer.start();

        // Asserts
        verify(threadFactory, times(3)).newThread(any(Runnable.class));
        verify(changesMonitorThread).start();
        verify(timeOutMonitorThread).start();
        verify(cleanupDuringStartup).releaseInterruptedTasks();
        verify(this.embeddedWebServerFactory).findOrCreateRemoteQueryWebServer(runningComServer);
        verify(queryWebServer).start();
    }

    @Test
    public void testShutdownWithoutComPorts() throws BusinessException {
        OnlineComServer comServer = mock(OnlineComServer.class);
        doThrow(BusinessException.class).when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getInboundComPorts()).thenReturn(new ArrayList<>(0));
        when(comServer.getOutboundComPorts()).thenReturn(new ArrayList<>(0));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(ComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(ComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(ComServer.MINIMUM_STORE_TASK_QUEUE_SIZE);
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread timeOutMonitorThread = this.mockedThread();
        Thread changesMonitorThread = this.mockedThread();
        Thread shutdownHookTread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(timeOutMonitorThread, changesMonitorThread, shutdownHookTread);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        RunningOnlineComServerImpl runningComServer = new RunningOnlineComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, mock(CleanupDuringStartup.class), this.serviceProvider);
        runningComServer.start();

        // Business method
        runningComServer.shutdown();

        // Asserts
        verify(changesMonitorThread, times(1)).interrupt();
        verify(timeOutMonitorThread, times(1)).interrupt();
    }

    @Test
    public void testShutdownWithRemoteComServers() throws BusinessException {
        OnlineComServer comServer = mock(OnlineComServer.class);
        doThrow(BusinessException.class).when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getInboundComPorts()).thenReturn(new ArrayList<>(0));
        when(comServer.getOutboundComPorts()).thenReturn(new ArrayList<>(0));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(ComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(ComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(ComServer.MINIMUM_STORE_TASK_QUEUE_SIZE);
        when(comServer.getName()).thenReturn(COMSERVER_NAME);
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread timeOutMonitorThread = this.mockedThread();
        Thread changesMonitorThread = this.mockedThread();
        Thread shutdownHookTread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(timeOutMonitorThread, changesMonitorThread, shutdownHookTread);
        RemoteComServer remoteComServer = mock(RemoteComServer.class);
        when(this.engineConfigurationService.findRemoteComServersForOnlineComServer(comServer)).thenReturn(Arrays.asList(remoteComServer));
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);
        EmbeddedWebServer queryWebServer = mock(EmbeddedWebServer.class);

        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        RunningOnlineComServerImpl runningComServer = new RunningOnlineComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, this.embeddedWebServerFactory, mock(CleanupDuringStartup.class), this.serviceProvider);
        when(queryWebServer.getStatus()).thenReturn(ServerProcessStatus.STARTED, ServerProcessStatus.SHUTDOWN);
        when(this.embeddedWebServerFactory.findOrCreateRemoteQueryWebServer(runningComServer)).thenReturn(queryWebServer);
        runningComServer.start();

        // Business method
        runningComServer.shutdown();

        // Asserts
        verify(changesMonitorThread, times(1)).interrupt();
        verify(timeOutMonitorThread, times(1)).interrupt();
        verify(queryWebServer, times(1)).shutdown();
    }

    @Test
    public void testStartOnlineWithSomeComPorts() throws SQLException, BusinessException {
        int numberOfInactiveInboundComPorts = 1;
        int numberOfInactiveOutboundComPorts = 1;
        OnlineComServer comServer = mock(OnlineComServer.class);
        doThrow(BusinessException.class).when(comServer).getEventRegistrationUriIfSupported();
        List<InboundComPort> inboundComPorts = new ArrayList<>();
        for (int i = 0; i < numberOfInactiveInboundComPorts; i++) {
            inboundComPorts.add(this.inboundComPort(comServer));
        }
        when(comServer.getInboundComPorts()).thenReturn(inboundComPorts);

        List<OutboundComPort> outboundComPorts = new ArrayList<>();
        for (int i = 0; i < numberOfInactiveOutboundComPorts; i++) {
            outboundComPorts.add(this.outboundComPort(comServer));
        }
        when(comServer.getOutboundComPorts()).thenReturn(outboundComPorts);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(ComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(ComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(ComServer.MINIMUM_STORE_TASK_QUEUE_SIZE);

        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ScheduledComPort scheduledComPort = mock(ScheduledComPort.class);
        when(scheduledComPortFactory.newFor(any(OutboundComPort.class))).thenReturn(scheduledComPort);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ComPortListener comPortListener = mock(ComPortListener.class);
        when(comPortListenerFactory.newFor(any(InboundComPort.class))).thenReturn(comPortListener);
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread timeOutMonitorThread = this.mockedThread();
        Thread changesMonitorThread = this.mockedThread();
        Thread shutdownHookTread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(timeOutMonitorThread, changesMonitorThread, shutdownHookTread);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        CleanupDuringStartup cleanupDuringStartup = mock(CleanupDuringStartup.class);
        RunningOnlineComServerImpl runningComServer = new RunningOnlineComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, cleanupDuringStartup, this.serviceProvider);

        // Business method
        runningComServer.start();

        // Asserts
        verify(threadFactory, times(3)).newThread(any(Runnable.class));
        verify(changesMonitorThread, times(1)).start();
        verify(timeOutMonitorThread, times(1)).start();
        verify(scheduledComPort, times(1)).start();
        verify(comPortListener, times(1)).start();
        verify(cleanupDuringStartup, times(1)).releaseInterruptedTasks();
    }

    @Test
    public void testStartWithCleanupFailure() throws SQLException, BusinessException {
        OnlineComServer comServer = mock(OnlineComServer.class);
        doThrow(BusinessException.class).when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(ComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(ComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(ComServer.MINIMUM_STORE_TASK_QUEUE_SIZE);

        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ScheduledComPort scheduledComPort = mock(ScheduledComPort.class);
        when(scheduledComPortFactory.newFor(any(OutboundComPort.class))).thenReturn(scheduledComPort);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ComPortListener comPortListener = mock(ComPortListener.class);
        when(comPortListenerFactory.newFor(any(InboundComPort.class))).thenReturn(comPortListener);
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread timeOutMonitorThread = this.mockedThread();
        Thread changesMonitorThread = this.mockedThread();
        Thread shutdownHookTread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(timeOutMonitorThread, changesMonitorThread, shutdownHookTread);
        CleanupDuringStartup cleanupDuringStartup = mock(CleanupDuringStartup.class);
        doThrow(SQLException.class).when(cleanupDuringStartup).releaseInterruptedTasks();
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        RunningOnlineComServerImpl runningComServer = new RunningOnlineComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, this.embeddedWebServerFactory, cleanupDuringStartup, this.serviceProvider);

        // Business method
        runningComServer.start();

        // Asserts
        verify(threadFactory, never()).newThread(any(Runnable.class));
        verify(changesMonitorThread, never()).start();
        verify(timeOutMonitorThread, never()).start();
        verify(scheduledComPort, never()).start();
        verify(comPortListener, never()).start();
        assertThat(runningComServer.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
    }

    @Test
    public void testShutdownWithSomeComPorts() throws BusinessException {
        int numberOfInactiveInboundComPorts = 1;
        int numberOfInactiveOutboundComPorts = 1;
        OnlineComServer comServer = mock(OnlineComServer.class);
        doThrow(BusinessException.class).when(comServer).getEventRegistrationUriIfSupported();
        List<InboundComPort> inboundComPorts = new ArrayList<>();
        for (int i = 0; i < numberOfInactiveInboundComPorts; i++) {
            inboundComPorts.add(this.inboundComPort(comServer));
        }
        when(comServer.getInboundComPorts()).thenReturn(inboundComPorts);

        List<OutboundComPort> outboundComPorts = new ArrayList<>();
        for (int i = 0; i < numberOfInactiveOutboundComPorts; i++) {
            outboundComPorts.add(this.outboundComPort(comServer));
        }
        when(comServer.getOutboundComPorts()).thenReturn(outboundComPorts);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(ComServer.MINIMUM_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(ComServer.MINIMUM_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(ComServer.MINIMUM_STORE_TASK_QUEUE_SIZE);

        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ScheduledComPort scheduledComPort = mock(ScheduledComPort.class);
        when(scheduledComPort.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(scheduledComPortFactory.newFor(any(OutboundComPort.class))).thenReturn(scheduledComPort);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ComPortListener comPortListener = mock(ComPortListener.class);
        when(comPortListener.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(comPortListenerFactory.newFor(any(InboundComPort.class))).thenReturn(comPortListener);
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread timeOutMonitorThread = this.mockedThread();
        Thread changesMonitorThread = this.mockedThread();
        Thread shutdownHookTread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(timeOutMonitorThread, changesMonitorThread, shutdownHookTread);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        RunningOnlineComServerImpl runningComServer = new RunningOnlineComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, mock(CleanupDuringStartup.class), this.serviceProvider);
        runningComServer.start();

        when(scheduledComPort.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        when(comPortListener.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);

        // Business method
        runningComServer.shutdown();

        // Asserts
        verify(changesMonitorThread, times(1)).interrupt();
        verify(timeOutMonitorThread, times(1)).interrupt();
        verify(scheduledComPort, times(1)).shutdown();
        verify(comPortListener, times(1)).shutdown();
    }

    private InboundComPort inboundComPort(ComServer comServer) {
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.getComServer()).thenReturn(comServer);
        return comPort;
    }

    private OutboundComPort outboundComPort(ComServer comServer) {
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.getComServer()).thenReturn(comServer);
        return comPort;
    }

    private Thread mockedThread() {
        return mock(Thread.class);
    }

}