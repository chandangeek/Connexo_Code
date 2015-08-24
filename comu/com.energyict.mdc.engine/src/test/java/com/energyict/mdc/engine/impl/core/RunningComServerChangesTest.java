package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.core.factories.ComPortListenerFactory;
import com.energyict.mdc.engine.impl.core.factories.ScheduledComPortFactory;
import com.energyict.mdc.engine.impl.core.mocks.MockComServerDAO;
import com.energyict.mdc.engine.impl.core.mocks.MockOnlineComServer;
import com.energyict.mdc.engine.impl.core.mocks.MockOutboundComPort;
import com.energyict.mdc.engine.impl.core.mocks.MockTCPInboundComPort;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.monitor.ComServerMonitor;
import com.energyict.mdc.engine.impl.monitor.ComServerMonitorImplMBean;
import com.energyict.mdc.engine.impl.monitor.ComServerOperationalStatistics;
import com.energyict.mdc.engine.impl.monitor.EventAPIStatistics;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.exception.MessageSeed;

import java.sql.SQLException;
import java.time.Clock;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests how the {@link com.energyict.mdc.engine.impl.core.RunningComServerImpl} component picks up on changes
 * applied to it or the related ComPorts.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (13:43)
 */
@RunWith(MockitoJUnitRunner.class)
public class RunningComServerChangesTest {

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
    private ComServerOperationalStatistics comServerOperationalStatistics;
    @Mock
    private IdentificationService identificationService;
    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock
    private CommunicationTaskService communicationTaskService;
    @Mock
    private RunningComServerImpl.ServiceProvider serviceProvider;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;

    private Clock clock = Clock.systemDefaultZone();
    private EventPublisher eventPublisher;

    @Before
    public void setupServiceProvider() {
        when(this.serviceProvider.clock()).thenReturn(this.clock);
        when(this.serviceProvider.engineService()).thenReturn(this.engineService);
        when(this.serviceProvider.engineConfigurationService()).thenReturn(this.engineConfigurationService);
        when(this.serviceProvider.deviceService()).thenReturn(this.deviceService);
        when(this.serviceProvider.managementBeanFactory()).thenReturn(this.managementBeanFactory);
        when(this.serviceProvider.nlsService()).thenReturn(this.nlsService);
        when(this.nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(this.thesaurus);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
    }

    @Before
    public void setupManagementBeanFactory() {
        when(this.managementBeanFactory.findOrCreateFor(any(RunningComServer.class))).thenReturn(this.comServerMonitor);
        ComServerMonitor comServerMonitor = (ComServerMonitor) this.comServerMonitor;
        when(comServerMonitor.getEventApiStatistics()).thenReturn(this.eventApiStatistics);
        when(comServerMonitor.getOperationalStatistics()).thenReturn(this.comServerOperationalStatistics);
    }

    public void initializeEventPublisher(RunningComServer comServer) {
        this.eventPublisher = new EventPublisherImpl(comServer);
    }

    @Test
    public void testAddOutboundComPort() throws InterruptedException, BusinessException, SQLException {
        MockComServerDAO comServerDAO = new MockComServerDAO();
        comServerDAO.addEmptyComServer();
        OnlineComServer comServer = (OnlineComServer) comServerDAO.getThisComServer();
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ScheduledComPort scheduledComPort = mock(ScheduledComPort.class);
        when(scheduledComPort.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(scheduledComPortFactory.newFor(any(OutboundComPort.class))).thenReturn(scheduledComPort);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        NotifyingRunningComServerImpl runningComServer = new NotifyingRunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new CleanupDuringStartupImpl(comServer, comServerDAO), serviceProvider);
        this.initializeEventPublisher(runningComServer);
        runningComServer.start();

        // Add an active outbound ComPort with 1 simultaneous connection
        OutboundComPort newOutboundComPort = comServerDAO.createOutbound(0, true, 1);
        when(scheduledComPort.getComPort()).thenReturn(newOutboundComPort);

        this.waitForComServerToPickupChanges(runningComServer);
        when(scheduledComPort.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        runningComServer.shutdownImmediate();

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(scheduledComPort, times(1)).start();
    }

    @Test
    public void testAddOutboundComPortThatShouldBeIgnored() throws InterruptedException, BusinessException, SQLException {
        MockComServerDAO comServerDAO = new MockComServerDAO();
        comServerDAO.addEmptyComServer();
        OnlineComServer comServer = (OnlineComServer) comServerDAO.getThisComServer();
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        when(scheduledComPortFactory.newFor(any(OutboundComPort.class))).thenReturn(null);  // The ScheduledComPortFactory returns null when a ComPort should be ignored
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        NotifyingRunningComServerImpl runningComServer = new NotifyingRunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new CleanupDuringStartupImpl(comServer, comServerDAO), serviceProvider);
        this.initializeEventPublisher(runningComServer);
        runningComServer.start();

        // Add an active outbound ComPort with zero simultaneous connections
        OutboundComPort newOutboundComPort = comServerDAO.createOutbound(0, true, 0);

        this.waitForComServerToPickupChanges(runningComServer);
        runningComServer.shutdownImmediate();

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(scheduledComPortFactory).newFor(newOutboundComPort);
    }

    @Test
    public void testDeactivateOutboundComPort() throws InterruptedException, BusinessException, SQLException {
        MockComServerDAO comServerDAO = new MockComServerDAO();
        MockOnlineComServer comServer = comServerDAO.addComServer(2, 0);
        OutboundComPort firstComPort = comServer.getOutboundComPort(1);
        OutboundComPort secondComPort = comServer.getOutboundComPort(2);

        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ScheduledComPort firstScheduledComPort = mock(ScheduledComPort.class);
        when(firstScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(firstScheduledComPort.getComPort()).thenReturn(firstComPort);
        ScheduledComPort secondScheduledComPort = mock(ScheduledComPort.class);
        when(secondScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(secondScheduledComPort.getComPort()).thenReturn(secondComPort);
        when(scheduledComPortFactory.newFor(firstComPort)).thenReturn(firstScheduledComPort);
        when(scheduledComPortFactory.newFor(secondComPort)).thenReturn(secondScheduledComPort);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        NotifyingRunningComServerImpl runningComServer = new NotifyingRunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new CleanupDuringStartupImpl(comServer, comServerDAO), serviceProvider);
        this.initializeEventPublisher(runningComServer);
        runningComServer.start();

        // Deactivate first ComPort
        comServerDAO.deactivateOutbound(0, 1);

        this.waitForComServerToPickupChanges(runningComServer);

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(firstScheduledComPort, times(1)).shutdown();
        verify(secondScheduledComPort, times(0)).shutdown();

        when(firstScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        when(secondScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        runningComServer.shutdownImmediate();
    }

    @Test
    public void testDeleteOutboundComPort() throws InterruptedException, BusinessException, SQLException {
        MockComServerDAO comServerDAO = new MockComServerDAO();
        MockOnlineComServer comServer = comServerDAO.addComServer(2, 0);
        OutboundComPort firstComPort = comServer.getOutboundComPort(1);
        OutboundComPort secondComPort = comServer.getOutboundComPort(2);

        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ScheduledComPort firstScheduledComPort = mock(ScheduledComPort.class);
        when(firstScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(firstScheduledComPort.getComPort()).thenReturn(firstComPort);
        ScheduledComPort secondScheduledComPort = mock(ScheduledComPort.class);
        when(secondScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(secondScheduledComPort.getComPort()).thenReturn(secondComPort);
        when(scheduledComPortFactory.newFor(firstComPort)).thenReturn(firstScheduledComPort);
        when(scheduledComPortFactory.newFor(secondComPort)).thenReturn(secondScheduledComPort);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        NotifyingRunningComServerImpl runningComServer = new NotifyingRunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new CleanupDuringStartupImpl(comServer, comServerDAO), serviceProvider);
        this.initializeEventPublisher(runningComServer);
        runningComServer.start();

        // Delete first ComPort
        comServerDAO.deleteOutbound(0, 1);

        this.waitForComServerToPickupChanges(runningComServer);

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(firstScheduledComPort, times(1)).shutdown();
        verify(secondScheduledComPort, times(0)).shutdown();

        when(firstScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        when(secondScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        runningComServer.shutdownImmediate();
    }

    @Test
    public void testSetNumberOfSimultaneousOutboundConnectionsOnComPort() throws InterruptedException, BusinessException, SQLException {
        MockComServerDAO comServerDAO = new MockComServerDAO();
        MockOnlineComServer comServer = comServerDAO.addComServer(2, 0);
        MockOutboundComPort firstComPort = comServer.getOutboundComPort(1);
        MockOutboundComPort secondComPort = comServer.getOutboundComPort(2);

        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ScheduledComPort firstScheduledComPort = mock(ScheduledComPort.class);
        when(firstScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(firstScheduledComPort.getComPort()).thenReturn(firstComPort);
        ScheduledComPort secondScheduledComPort = mock(ScheduledComPort.class);
        when(secondScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(secondScheduledComPort.getComPort()).thenReturn(secondComPort);
        ScheduledComPort firstScheduledComPortAfterChanges = mock(ScheduledComPort.class);
        when(firstScheduledComPortAfterChanges.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(firstScheduledComPortAfterChanges.getComPort()).thenReturn(firstComPort);
        when(scheduledComPortFactory.newFor(firstComPort)).thenReturn(firstScheduledComPort);
        when(scheduledComPortFactory.newFor(secondComPort)).thenReturn(secondScheduledComPort);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        NotifyingRunningComServerImpl runningComServer = new NotifyingRunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new CleanupDuringStartupImpl(comServer, comServerDAO), serviceProvider);
        this.initializeEventPublisher(runningComServer);
        runningComServer.start();

        // Change the number of simultaneous connection of the first ComPort
        MockOutboundComPort changedComPort = comServerDAO.setNumberOfSimultaneousOutboundConnections(0, 1, 3);
        when(scheduledComPortFactory.newFor(changedComPort)).thenReturn(firstScheduledComPortAfterChanges);

        this.waitForComServerToPickupChanges(runningComServer);

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(firstScheduledComPort, times(1)).shutdown();
        verify(secondScheduledComPort, times(0)).shutdown();
        verify(firstScheduledComPortAfterChanges, times(1)).start();

        when(firstScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        when(secondScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        when(firstScheduledComPortAfterChanges.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        runningComServer.shutdownImmediate();
    }

    @Test
    public void testAddInboundComPort() throws InterruptedException, BusinessException, SQLException {
        MockComServerDAO comServerDAO = new MockComServerDAO();
        comServerDAO.addEmptyComServer();
        OnlineComServer comServer = (OnlineComServer) comServerDAO.getThisComServer();
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ComPortListener comPortListener = mock(ComPortListener.class);
        when(comPortListener.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(comPortListenerFactory.newFor(any(InboundComPort.class))).thenReturn(comPortListener);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        NotifyingRunningComServerImpl runningComServer = new NotifyingRunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new CleanupDuringStartupImpl(comServer, comServerDAO), serviceProvider);
        this.initializeEventPublisher(runningComServer);
        runningComServer.start();

        // Add an active inbound ComPort with 1 simultaneous connection
        InboundComPort newInboundComPort = comServerDAO.createInbound(0, true, 1);
        when(comPortListener.getComPort()).thenReturn(newInboundComPort);

        this.waitForComServerToPickupChanges(runningComServer);
        when(comPortListener.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        runningComServer.shutdownImmediate();

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(comPortListener, times(1)).start();
    }

    @Test
    public void testAddInboundComPortThatShouldBeIgnored() throws InterruptedException, BusinessException, SQLException {
        MockComServerDAO comServerDAO = new MockComServerDAO();
        comServerDAO.addEmptyComServer();
        OnlineComServer comServer = (OnlineComServer) comServerDAO.getThisComServer();
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        when(comPortListenerFactory.newFor(any(InboundComPort.class))).thenReturn(null);    // The ComPortListenerFactory returns null when the ComPort should be ignored
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        NotifyingRunningComServerImpl runningComServer = new NotifyingRunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new CleanupDuringStartupImpl(comServer, comServerDAO), serviceProvider);
        this.initializeEventPublisher(runningComServer);
        runningComServer.start();

        // Add an active inbound ComPort with zero simultaneous connection
        InboundComPort newInboundComPort = comServerDAO.createInbound(0, true, 0);

        this.waitForComServerToPickupChanges(runningComServer);
        runningComServer.shutdownImmediate();

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(comPortListenerFactory).newFor(newInboundComPort);
    }

    @Test
    public void testDeactivateInboundComPort() throws InterruptedException, BusinessException, SQLException {
        MockComServerDAO comServerDAO = new MockComServerDAO();
        MockOnlineComServer comServer = comServerDAO.addComServer(0, 2);
        InboundComPort firstComPort = comServer.getInboundComPort(1);
        InboundComPort secondComPort = comServer.getInboundComPort(2);

        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ComPortListener firstComPortListener = mock(ComPortListener.class);
        when(firstComPortListener.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(firstComPortListener.getComPort()).thenReturn(firstComPort);
        ComPortListener secondComPortListener = mock(ComPortListener.class);
        when(secondComPortListener.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(secondComPortListener.getComPort()).thenReturn(secondComPort);
        when(comPortListenerFactory.newFor(firstComPort)).thenReturn(firstComPortListener);
        when(comPortListenerFactory.newFor(secondComPort)).thenReturn(secondComPortListener);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        NotifyingRunningComServerImpl runningComServer = new NotifyingRunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new CleanupDuringStartupImpl(comServer, comServerDAO), serviceProvider);
        this.initializeEventPublisher(runningComServer);
        runningComServer.start();

        // Deactivate first ComPort
        comServerDAO.deactivateInbound(0, 1);

        this.waitForComServerToPickupChanges(runningComServer);

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(firstComPortListener, times(1)).shutdown();
        verify(secondComPortListener, times(0)).shutdown();

        when(firstComPortListener.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        when(secondComPortListener.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        runningComServer.shutdownImmediate();
    }

    @Test
    public void testDeleteInboundComPort() throws InterruptedException, BusinessException, SQLException {
        MockComServerDAO comServerDAO = new MockComServerDAO();
        MockOnlineComServer comServer = comServerDAO.addComServer(0, 2);
        InboundComPort firstComPort = comServer.getInboundComPort(1);
        InboundComPort secondComPort = comServer.getInboundComPort(2);

        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ComPortListener firstComPortListener = mock(ComPortListener.class);
        when(firstComPortListener.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(firstComPortListener.getComPort()).thenReturn(firstComPort);
        ComPortListener secondComPortListener = mock(ComPortListener.class);
        when(secondComPortListener.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(secondComPortListener.getComPort()).thenReturn(secondComPort);
        when(comPortListenerFactory.newFor(firstComPort)).thenReturn(firstComPortListener);
        when(comPortListenerFactory.newFor(secondComPort)).thenReturn(secondComPortListener);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        NotifyingRunningComServerImpl runningComServer = new NotifyingRunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new CleanupDuringStartupImpl(comServer, comServerDAO), serviceProvider);
        this.initializeEventPublisher(runningComServer);
        runningComServer.start();

        // Delete first ComPort
        comServerDAO.deleteInbound(0, 1);

        this.waitForComServerToPickupChanges(runningComServer);

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(firstComPortListener, times(1)).shutdown();
        verify(secondComPortListener, times(0)).shutdown();

        when(firstComPortListener.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        when(secondComPortListener.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        runningComServer.shutdownImmediate();
    }

    @Test
    public void testSetNumberOfSimultaneousInboundConnectionsOnComPort() throws InterruptedException, BusinessException, SQLException {
        MockComServerDAO comServerDAO = new MockComServerDAO();
        MockOnlineComServer comServer = comServerDAO.addComServer(0, 2);
        InboundComPort firstComPort = comServer.getInboundComPort(1);
        InboundComPort secondComPort = comServer.getInboundComPort(2);

        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ComPortListener firstComPortListener = mock(ComPortListener.class);
        when(firstComPortListener.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(firstComPortListener.getComPort()).thenReturn(firstComPort);
        ComPortListener secondComPortListener = mock(ComPortListener.class);
        when(secondComPortListener.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        ComPortListener firstComPortListenerAfterChanges = mock(ComPortListener.class);
        when(firstComPortListenerAfterChanges.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(firstComPortListenerAfterChanges.getComPort()).thenReturn(firstComPort);
        when(secondComPortListener.getComPort()).thenReturn(secondComPort);
        when(comPortListenerFactory.newFor(firstComPort)).thenReturn(firstComPortListener);
        when(comPortListenerFactory.newFor(secondComPort)).thenReturn(secondComPortListener);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        NotifyingRunningComServerImpl runningComServer = new NotifyingRunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new CleanupDuringStartupImpl(comServer, comServerDAO), serviceProvider);
        this.initializeEventPublisher(runningComServer);
        runningComServer.start();

        // Deactivate first ComPort
        MockTCPInboundComPort changedComPort = comServerDAO.setNumberOfSimultaneousInboundConnections(0, 1, 3);
        when(comPortListenerFactory.newFor(changedComPort)).thenReturn(firstComPortListenerAfterChanges);

        this.waitForComServerToPickupChanges(runningComServer);

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(firstComPortListener, times(1)).shutdown();
        verify(secondComPortListener, times(0)).shutdown();
        verify(firstComPortListenerAfterChanges, times(1)).start();

        when(firstComPortListener.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        when(secondComPortListener.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        when(firstComPortListenerAfterChanges.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        runningComServer.shutdownImmediate();
    }

    @Test
    public void testChangeSchedulingInterPollDelay() throws InterruptedException, BusinessException, SQLException {
        MockComServerDAO comServerDAO = new MockComServerDAO();
        MockOnlineComServer comServer = comServerDAO.addComServer(2, 0);
        MockOutboundComPort firstComPort = comServer.getOutboundComPort(1);
        MockOutboundComPort secondComPort = comServer.getOutboundComPort(2);

        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ScheduledComPort firstScheduledComPort = mock(ScheduledComPort.class);
        when(firstScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(firstScheduledComPort.getComPort()).thenReturn(firstComPort);
        ScheduledComPort secondScheduledComPort = mock(ScheduledComPort.class);
        when(secondScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(secondScheduledComPort.getComPort()).thenReturn(secondComPort);
        ScheduledComPort firstScheduledComPortAfterChanges = mock(ScheduledComPort.class);
        when(firstScheduledComPortAfterChanges.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(firstScheduledComPortAfterChanges.getComPort()).thenReturn(firstComPort);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        EmbeddedWebServer eventWebServer = mock(EmbeddedWebServer.class);
        when(this.embeddedWebServerFactory.findOrCreateEventWebServer(comServer)).thenReturn(eventWebServer);

        NotifyingRunningComServerImpl runningComServer = new NotifyingRunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new CleanupDuringStartupImpl(comServer, comServerDAO), serviceProvider);
        when(scheduledComPortFactory.newFor(firstComPort)).thenReturn(firstScheduledComPort);
        when(scheduledComPortFactory.newFor(secondComPort)).thenReturn(secondScheduledComPort);
        this.initializeEventPublisher(runningComServer);
        runningComServer.start();

        // Change the scheduling interpoll delay
        TimeDuration newSchedulingInterPollDelay = new TimeDuration(5, TimeDuration.TimeUnit.MINUTES);
        comServerDAO.setSchedulingInterPollDelay(0, newSchedulingInterPollDelay);

        this.waitForComServerToPickupChanges(runningComServer);
        runningComServer.shutdownImmediate();

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(firstScheduledComPort, times(1)).schedulingInterpollDelayChanged(newSchedulingInterPollDelay);
        verify(firstScheduledComPort, never()).changesInterpollDelayChanged(any(TimeDuration.class));
        verify(secondScheduledComPort, times(1)).schedulingInterpollDelayChanged(newSchedulingInterPollDelay);
        verify(secondScheduledComPort, never()).changesInterpollDelayChanged(any(TimeDuration.class));
    }

    private void waitForComServerToPickupChanges(NotifyingRunningComServerImpl runningComServer) throws InterruptedException {
        runningComServer.waitForApplyChanges();
    }

    private class NotifyingRunningComServerImpl extends RunningOnlineComServerImpl {
        private CountDownLatch applyChangesLatch = new CountDownLatch(1);

        protected NotifyingRunningComServerImpl(OnlineComServer comServer, ComServerDAO comServerDAO, ScheduledComPortFactory scheduledComPortFactory, ComPortListenerFactory comPortListenerFactory, CleanupDuringStartup cleanupDuringStartup, ServiceProvider serviceProvider) {
            super(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new ComServerThreadFactory(comServer), cleanupDuringStartup, serviceProvider);
        }

        @Override
        protected void notifyChangesApplied() {
            super.notifyChangesApplied();
            this.applyChangesLatch.countDown();
        }

        private void waitForApplyChanges() throws InterruptedException {
            this.applyChangesLatch.await(10, TimeUnit.SECONDS);
        }

    }
}