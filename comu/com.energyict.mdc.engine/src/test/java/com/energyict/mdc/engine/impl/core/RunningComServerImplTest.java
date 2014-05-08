package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.impl.core.CleanupDuringStartup;
import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.OnlineRunningComServerImpl;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.core.ScheduledComPort;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.factories.ComPortListenerFactory;
import com.energyict.mdc.engine.impl.core.factories.ScheduledComPortFactory;
import com.energyict.mdc.engine.impl.web.DefaultEmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.servers.ComServerFactory;
import com.energyict.mdc.engine.model.RemoteComServer;
import com.energyict.mdc.shadow.servers.OnlineComServerShadow;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.RunningComServerImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (10:40)
 */
@RunWith(MockitoJUnitRunner.class)
public class RunningComServerImplTest {

    private static final String COMSERVER_NAME = "RunningComserverImplTest";
    @Mock
    private ServerManager manager;
    @Mock
    private ComServerFactory comServerFactory;
    @Mock
    private EmbeddedWebServerFactory embeddedWebServerFactory;

    @Before
    public void initializeMocksAndFactories() {
        when(this.manager.getComServerFactory()).thenReturn(this.comServerFactory);
        ManagerFactory.setCurrent(this.manager);
        EmbeddedWebServerFactory.DEFAULT.set(this.embeddedWebServerFactory);
    }

    @After
    public void resetEmbeddedWebServerFactory() {
        EmbeddedWebServerFactory.DEFAULT.set(new DefaultEmbeddedWebServerFactory());
    }

    @Test
    public void testConstructorWithoutComPorts() throws SQLException, BusinessException {
        ServerOnlineComServer comServer = mock(ServerOnlineComServer.class);
        doThrow(BusinessException.class).when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getInboundComPorts()).thenReturn(new ArrayList<InboundComPort>(0));
        when(comServer.getOutboundComPorts()).thenReturn(new ArrayList<OutboundComPort>(0));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(OnlineComServerShadow.DEFAULT_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_QUEUE_SIZE);
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ThreadFactory threadFactory = mock(ThreadFactory.class);

        // Business method
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, mock(CleanupDuringStartup.class));

        // Asserts
        verify(scheduledComPortFactory, times(0)).newFor(any(OutboundComPort.class), issueService);
        verify(comPortListenerFactory, times(0)).newFor(any(InboundComPort.class), issueService);
        verify(threadFactory, times(0)).newThread(any(Runnable.class));
    }

    @Test
    public void testConstructorWithSomeComPorts() throws BusinessException {
        int numberOfInactiveInboundComPorts = 2;
        int numberOfInactiveOutboundComPorts = 3;
        ServerOnlineComServer comServer = mock(ServerOnlineComServer.class);
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
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(OnlineComServerShadow.DEFAULT_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_QUEUE_SIZE);

        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ThreadFactory threadFactory = mock(ThreadFactory.class);

        // Business method
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, mock(CleanupDuringStartup.class));

        // Asserts
        verify(scheduledComPortFactory, times(numberOfInactiveOutboundComPorts)).newFor(any(OutboundComPort.class), issueService);
        verify(comPortListenerFactory, times(numberOfInactiveInboundComPorts)).newFor(any(InboundComPort.class), issueService);
        verify(threadFactory, times(0)).newThread(any(Runnable.class));
    }

    @Test
    public void testStartOnlineWithoutComPortsAndRemoteComServers() throws SQLException, BusinessException {
        ServerOnlineComServer comServer = mock(ServerOnlineComServer.class);
        doThrow(BusinessException.class).when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getInboundComPorts()).thenReturn(new ArrayList<InboundComPort>(0));
        when(comServer.getOutboundComPorts()).thenReturn(new ArrayList<OutboundComPort>(0));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(OnlineComServerShadow.DEFAULT_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_QUEUE_SIZE);
        when(comServer.getName()).thenReturn(COMSERVER_NAME);
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread systemTopicHandlerThread = this.mockedThread();
        Thread timeOutMonitorThread = this.mockedThread();
        Thread changesMonitorThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(systemTopicHandlerThread, timeOutMonitorThread, changesMonitorThread);
        List<RemoteComServer> noRemoteComServers = new ArrayList<>(0);
        when(this.comServerFactory.findRemoteComServersWithOnlineComServer(comServer)).thenReturn(noRemoteComServers);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        CleanupDuringStartup cleanupDuringStartup = mock(CleanupDuringStartup.class);
        OnlineRunningComServerImpl runningComServer = new OnlineRunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, cleanupDuringStartup);

        // Business method
        runningComServer.start();

        // Asserts
        verify(threadFactory, times(4)).newThread(any(Runnable.class));
        verify(systemTopicHandlerThread).start();
        verify(changesMonitorThread).start();
        verify(timeOutMonitorThread).start();
        verify(cleanupDuringStartup).releaseInterruptedTasks();
        verify(this.comServerFactory).findRemoteComServersWithOnlineComServer(comServer);
        verify(this.embeddedWebServerFactory, never()).findOrCreateRemoteQueryWebServer(comServer);
    }

    @Test
    public void testStartOnlineWithRemoteComServers() throws SQLException, BusinessException {
        ServerOnlineComServer comServer = mock(ServerOnlineComServer.class);
        doThrow(BusinessException.class).when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getInboundComPorts()).thenReturn(new ArrayList<InboundComPort>(0));
        when(comServer.getOutboundComPorts()).thenReturn(new ArrayList<OutboundComPort>(0));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(OnlineComServerShadow.DEFAULT_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_QUEUE_SIZE);
        when(comServer.getName()).thenReturn(COMSERVER_NAME);
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread systemTopicHandlerThread = this.mockedThread();
        Thread timeOutMonitorThread = this.mockedThread();
        Thread changesMonitorThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(systemTopicHandlerThread, timeOutMonitorThread, changesMonitorThread);
        RemoteComServer remoteComServer = mock(RemoteComServer.class);
        when(this.comServerFactory.findRemoteComServersWithOnlineComServer(comServer)).thenReturn(Arrays.asList(remoteComServer));
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);
        EmbeddedWebServer queryWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateRemoteQueryWebServer(comServer)).thenReturn(queryWebServer);

        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        CleanupDuringStartup cleanupDuringStartup = mock(CleanupDuringStartup.class);
        OnlineRunningComServerImpl runningComServer = new OnlineRunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, cleanupDuringStartup);

        // Business method
        runningComServer.start();

        // Asserts
        verify(threadFactory, times(4)).newThread(any(Runnable.class));
        verify(systemTopicHandlerThread).start();
        verify(changesMonitorThread).start();
        verify(timeOutMonitorThread).start();
        verify(cleanupDuringStartup).releaseInterruptedTasks();
        verify(this.comServerFactory).findRemoteComServersWithOnlineComServer(comServer);
        verify(this.embeddedWebServerFactory).findOrCreateRemoteQueryWebServer(comServer);
        verify(queryWebServer).start();
    }

    @Test
    public void testShutdownWithoutComPorts() throws BusinessException {
        ServerOnlineComServer comServer = mock(ServerOnlineComServer.class);
        doThrow(BusinessException.class).when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getInboundComPorts()).thenReturn(new ArrayList<InboundComPort>(0));
        when(comServer.getOutboundComPorts()).thenReturn(new ArrayList<OutboundComPort>(0));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(OnlineComServerShadow.DEFAULT_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_QUEUE_SIZE);
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread timeOutMonitorThread = this.mockedThread();
        Thread changesMonitorThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(timeOutMonitorThread, changesMonitorThread);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        RunningComServerImpl runningComServer = new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, mock(CleanupDuringStartup.class));
        runningComServer.start();

        // Business method
        runningComServer.shutdown();

        // Asserts
        verify(changesMonitorThread, times(1)).interrupt();
        verify(timeOutMonitorThread, times(1)).interrupt();
    }

    @Test
    public void testShutdownWithRemoteComServers() throws BusinessException {
        ServerOnlineComServer comServer = mock(ServerOnlineComServer.class);
        doThrow(BusinessException.class).when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getInboundComPorts()).thenReturn(new ArrayList<InboundComPort>(0));
        when(comServer.getOutboundComPorts()).thenReturn(new ArrayList<OutboundComPort>(0));
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(OnlineComServerShadow.DEFAULT_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_QUEUE_SIZE);
        when(comServer.getName()).thenReturn(COMSERVER_NAME);
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread systemTopicHandlerThread = this.mockedThread();
        Thread timeOutMonitorThread = this.mockedThread();
        Thread changesMonitorThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(systemTopicHandlerThread, timeOutMonitorThread, changesMonitorThread);
        RemoteComServer remoteComServer = mock(RemoteComServer.class);
        when(this.comServerFactory.findRemoteComServersWithOnlineComServer(comServer)).thenReturn(Arrays.asList(remoteComServer));
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);
        EmbeddedWebServer queryWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateRemoteQueryWebServer(comServer)).thenReturn(queryWebServer);

        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        OnlineRunningComServerImpl runningComServer = new OnlineRunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, mock(CleanupDuringStartup.class));
        runningComServer.start();
        when(queryWebServer.getStatus()).thenReturn(ServerProcessStatus.STARTED, ServerProcessStatus.SHUTDOWN);

        // Business method
        runningComServer.shutdown();

        // Asserts
        verify(systemTopicHandlerThread, never()).interrupt();
        verify(changesMonitorThread, times(1)).interrupt();
        verify(timeOutMonitorThread, times(1)).interrupt();
        verify(queryWebServer, times(1)).shutdown();
    }

    @Test
    public void testStartOnlineWithSomeComPorts() throws SQLException, BusinessException {
        int numberOfInactiveInboundComPorts = 1;
        int numberOfInactiveOutboundComPorts = 1;
        ServerOnlineComServer comServer = mock(ServerOnlineComServer.class);
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
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(OnlineComServerShadow.DEFAULT_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_QUEUE_SIZE);

        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ScheduledComPort scheduledComPort = mock(ScheduledComPort.class);
        when(scheduledComPortFactory.newFor(any(OutboundComPort.class), issueService)).thenReturn(scheduledComPort);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ComPortListener comPortListener = mock(ComPortListener.class);
        when(comPortListenerFactory.newFor(any(InboundComPort.class), issueService)).thenReturn(comPortListener);
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread timeOutMonitorThread = this.mockedThread();
        Thread changesMonitorThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(timeOutMonitorThread, changesMonitorThread);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        CleanupDuringStartup cleanupDuringStartup = mock(CleanupDuringStartup.class);
        RunningComServerImpl runningComServer = new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, cleanupDuringStartup);

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
        ServerOnlineComServer comServer = mock(ServerOnlineComServer.class);
        doThrow(BusinessException.class).when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(OnlineComServerShadow.DEFAULT_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_QUEUE_SIZE);

        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ScheduledComPort scheduledComPort = mock(ScheduledComPort.class);
        when(scheduledComPortFactory.newFor(any(OutboundComPort.class), issueService)).thenReturn(scheduledComPort);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ComPortListener comPortListener = mock(ComPortListener.class);
        when(comPortListenerFactory.newFor(any(InboundComPort.class), issueService)).thenReturn(comPortListener);
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread timeOutMonitorThread = this.mockedThread();
        Thread changesMonitorThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(timeOutMonitorThread, changesMonitorThread);
        CleanupDuringStartup cleanupDuringStartup = mock(CleanupDuringStartup.class);
        doThrow(SQLException.class).when(cleanupDuringStartup).releaseInterruptedTasks();
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        RunningComServerImpl runningComServer = new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, cleanupDuringStartup);

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
        ServerOnlineComServer comServer = mock(ServerOnlineComServer.class);
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
        when(comServer.getNumberOfStoreTaskThreads()).thenReturn(OnlineComServerShadow.DEFAULT_NUMBER_OF_STORE_TASK_THREADS);
        when(comServer.getStoreTaskThreadPriority()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_THREAD_PRIORITY);
        when(comServer.getStoreTaskQueueSize()).thenReturn(OnlineComServerShadow.DEFAULT_STORE_TASK_QUEUE_SIZE);

        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ScheduledComPort scheduledComPort = mock(ScheduledComPort.class);
        when(scheduledComPort.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(scheduledComPortFactory.newFor(any(OutboundComPort.class), issueService)).thenReturn(scheduledComPort);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ComPortListener comPortListener = mock(ComPortListener.class);
        when(comPortListener.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(comPortListenerFactory.newFor(any(InboundComPort.class), issueService)).thenReturn(comPortListener);
        ThreadFactory threadFactory = mock(ThreadFactory.class);
        Thread timeOutMonitorThread = this.mockedThread();
        Thread changesMonitorThread = this.mockedThread();
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(timeOutMonitorThread, changesMonitorThread);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        RunningComServerImpl runningComServer = new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, threadFactory, mock(CleanupDuringStartup.class));
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