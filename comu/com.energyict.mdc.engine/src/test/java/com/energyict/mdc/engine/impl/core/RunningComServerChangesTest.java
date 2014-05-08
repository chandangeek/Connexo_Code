package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.BusinessException;
import com.energyict.cbo.PooledThreadFactory;
import com.energyict.cbo.TimeConstants;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.impl.core.CleanupDuringStartupImpl;
import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.impl.core.PooledThreadFactory;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.core.ScheduledComPort;
import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import com.energyict.mdc.engine.impl.core.factories.ComPortListenerFactory;
import com.energyict.mdc.engine.impl.core.factories.ScheduledComPortFactory;
import com.energyict.comserver.scheduling.mocks.MockComServerDAO;
import com.energyict.comserver.scheduling.mocks.MockOnlineComServer;
import com.energyict.comserver.scheduling.mocks.MockOutboundComPort;
import com.energyict.comserver.scheduling.mocks.MockTCPInboundComPort;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.OutboundComPort;
import org.junit.*;

import java.sql.SQLException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
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
public class RunningComServerChangesTest {

    private static final int HALF_A_SECOND = 500;

    @Test
    public void testAddOutboundComPort () throws InterruptedException, BusinessException, SQLException {
        MockComServerDAO comServerDAO = new MockComServerDAO();
        comServerDAO.addEmptyComServer();
        ServerOnlineComServer comServer = (ServerOnlineComServer) comServerDAO.getThisComServer();
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ScheduledComPort scheduledComPort = mock(ScheduledComPort.class);
        when(scheduledComPort.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(scheduledComPortFactory.newFor(any(OutboundComPort.class), issueService)).thenReturn(scheduledComPort);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);

        RunningComServerImpl runningComServer = new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new PooledThreadFactory(), new CleanupDuringStartupImpl(comServer, comServerDAO));
        runningComServer.start();

        // Add an active outbound ComPort with 1 simultaneous connection
        OutboundComPort newOutboundComPort = comServerDAO.createOutbound(0, true, 1);
        when(scheduledComPort.getComPort()).thenReturn(newOutboundComPort);

        this.waitForComServerToPickupChanges();
        when(scheduledComPort.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        runningComServer.shutdownImmediate();

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(scheduledComPort, times(1)).start();
    }

    @Test
    public void testAddOutboundComPortThatShouldBeIgnored () throws InterruptedException, BusinessException, SQLException {
        MockComServerDAO comServerDAO = new MockComServerDAO();
        comServerDAO.addEmptyComServer();
        ServerOnlineComServer comServer = (ServerOnlineComServer) comServerDAO.getThisComServer();
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        when(scheduledComPortFactory.newFor(any(OutboundComPort.class), issueService)).thenReturn(null);  // The ScheduledComPortFactory returns null when a ComPort should be ignored
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);

        RunningComServerImpl runningComServer = new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new PooledThreadFactory(), new CleanupDuringStartupImpl(comServer, comServerDAO));
        runningComServer.start();

        // Add an active outbound ComPort with zero simultaneous connection
        OutboundComPort newOutboundComPort = comServerDAO.createOutbound(0, true, 0);

        this.waitForComServerToPickupChanges();
        runningComServer.shutdownImmediate();

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(scheduledComPortFactory).newFor(newOutboundComPort, issueService);
    }

    @Test
    public void testDeactivateOutboundComPort () throws InterruptedException, BusinessException, SQLException {
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
        when(scheduledComPortFactory.newFor(firstComPort, issueService)).thenReturn(firstScheduledComPort);
        when(scheduledComPortFactory.newFor(secondComPort, issueService)).thenReturn(secondScheduledComPort);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);

        RunningComServerImpl runningComServer = new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new PooledThreadFactory(), new CleanupDuringStartupImpl(comServer, comServerDAO));
        runningComServer.start();

        // Deactivate first ComPort
        comServerDAO.deactivateOutbound(0, 1);

        this.waitForComServerToPickupChanges();

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(firstScheduledComPort, times(1)).shutdown();
        verify(secondScheduledComPort, times(0)).shutdown();

        when(firstScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        when(secondScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        runningComServer.shutdownImmediate();
    }

    @Test
    public void testDeleteOutboundComPort () throws InterruptedException, BusinessException, SQLException {
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
        when(scheduledComPortFactory.newFor(firstComPort, issueService)).thenReturn(firstScheduledComPort);
        when(scheduledComPortFactory.newFor(secondComPort, issueService)).thenReturn(secondScheduledComPort);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);

        RunningComServerImpl runningComServer = new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new PooledThreadFactory(), new CleanupDuringStartupImpl(comServer, comServerDAO));
        runningComServer.start();

        // Delete first ComPort
        comServerDAO.deleteOutbound(0, 1);

        this.waitForComServerToPickupChanges();

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(firstScheduledComPort, times(1)).shutdown();
        verify(secondScheduledComPort, times(0)).shutdown();

        when(firstScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        when(secondScheduledComPort.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        runningComServer.shutdownImmediate();
    }

    @Test
    public void testSetNumberOfSimultaneousOutboundConnectionsOnComPort () throws InterruptedException, BusinessException, SQLException {
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
        when(scheduledComPortFactory.newFor(firstComPort, issueService)).thenReturn(firstScheduledComPort);
        when(scheduledComPortFactory.newFor(secondComPort, issueService)).thenReturn(secondScheduledComPort);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);

        RunningComServerImpl runningComServer = new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new PooledThreadFactory(), new CleanupDuringStartupImpl(comServer, comServerDAO));
        runningComServer.start();

        // Change the number of simultaneous connection of the first ComPort
        MockOutboundComPort changedComPort = comServerDAO.setNumberOfSimultaneousOutboundConnections(0, 1, 3);
        when(scheduledComPortFactory.newFor(changedComPort, issueService)).thenReturn(firstScheduledComPortAfterChanges);

        this.waitForComServerToPickupChanges();

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
    public void testAddInboundComPort () throws InterruptedException, BusinessException, SQLException {
        MockComServerDAO comServerDAO = new MockComServerDAO();
        comServerDAO.addEmptyComServer();
        ServerOnlineComServer comServer = (ServerOnlineComServer) comServerDAO.getThisComServer();
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        ComPortListener comPortListener = mock(ComPortListener.class);
        when(comPortListener.getStatus()).thenReturn(ServerProcessStatus.STARTED);
        when(comPortListenerFactory.newFor(any(InboundComPort.class), issueService)).thenReturn(comPortListener);

        RunningComServerImpl runningComServer = new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new PooledThreadFactory(), new CleanupDuringStartupImpl(comServer, comServerDAO));
        runningComServer.start();

        // Add an active inbound ComPort with 1 simultaneous connection
        InboundComPort newInboundComPort = comServerDAO.createInbound(0, true, 1);
        when(comPortListener.getComPort()).thenReturn(newInboundComPort);

        this.waitForComServerToPickupChanges();
        when(comPortListener.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        runningComServer.shutdownImmediate();

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(comPortListener, times(1)).start();
    }

    @Test
    public void testAddInboundComPortThatShouldBeIgnored () throws InterruptedException, BusinessException, SQLException {
        MockComServerDAO comServerDAO = new MockComServerDAO();
        comServerDAO.addEmptyComServer();
        ServerOnlineComServer comServer = (ServerOnlineComServer) comServerDAO.getThisComServer();
        ScheduledComPortFactory scheduledComPortFactory = mock(ScheduledComPortFactory.class);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);
        when(comPortListenerFactory.newFor(any(InboundComPort.class), issueService)).thenReturn(null);    // The ComPortListenerFactory returns null when the ComPort should be ignored

        RunningComServerImpl runningComServer = new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new PooledThreadFactory(), new CleanupDuringStartupImpl(comServer, comServerDAO));
        runningComServer.start();

        // Add an active inbound ComPort with zero simultaneous connection
        InboundComPort newInboundComPort = comServerDAO.createInbound(0, true, 0);

        this.waitForComServerToPickupChanges();
        runningComServer.shutdownImmediate();

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(comPortListenerFactory).newFor(newInboundComPort, issueService);
    }

    @Test
    public void testDeactivateInboundComPort () throws InterruptedException, BusinessException, SQLException {
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
        when(comPortListenerFactory.newFor(firstComPort, issueService)).thenReturn(firstComPortListener);
        when(comPortListenerFactory.newFor(secondComPort, issueService)).thenReturn(secondComPortListener);

        RunningComServerImpl runningComServer = new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new PooledThreadFactory(), new CleanupDuringStartupImpl(comServer, comServerDAO));
        runningComServer.start();

        // Deactivate first ComPort
        comServerDAO.deactivateInbound(0, 1);

        this.waitForComServerToPickupChanges();

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(firstComPortListener, times(1)).shutdown();
        verify(secondComPortListener, times(0)).shutdown();

        when(firstComPortListener.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        when(secondComPortListener.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        runningComServer.shutdownImmediate();
    }

    @Test
    public void testDeleteInboundComPort () throws InterruptedException, BusinessException, SQLException {
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
        when(comPortListenerFactory.newFor(firstComPort, issueService)).thenReturn(firstComPortListener);
        when(comPortListenerFactory.newFor(secondComPort, issueService)).thenReturn(secondComPortListener);

        RunningComServerImpl runningComServer = new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new PooledThreadFactory(), new CleanupDuringStartupImpl(comServer, comServerDAO));
        runningComServer.start();

        // Delete first ComPort
        comServerDAO.deleteInbound(0, 1);

        this.waitForComServerToPickupChanges();

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(firstComPortListener, times(1)).shutdown();
        verify(secondComPortListener, times(0)).shutdown();

        when(firstComPortListener.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        when(secondComPortListener.getStatus()).thenReturn(ServerProcessStatus.SHUTDOWN);
        runningComServer.shutdownImmediate();
    }

    @Test
    public void testSetNumberOfSimultaneousInboundConnectionsOnComPort () throws InterruptedException, BusinessException, SQLException {
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
        when(comPortListenerFactory.newFor(firstComPort, issueService)).thenReturn(firstComPortListener);
        when(comPortListenerFactory.newFor(secondComPort, issueService)).thenReturn(secondComPortListener);

        RunningComServerImpl runningComServer = new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new PooledThreadFactory(), new CleanupDuringStartupImpl(comServer, comServerDAO));
        runningComServer.start();

        // Deactivate first ComPort
        MockTCPInboundComPort changedComPort = comServerDAO.setNumberOfSimultaneousInboundConnections(0, 1, 3);
        when(comPortListenerFactory.newFor(changedComPort, issueService)).thenReturn(firstComPortListenerAfterChanges);

        this.waitForComServerToPickupChanges();

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
    public void testChangeSchedulingInterPollDelay () throws InterruptedException, BusinessException, SQLException {
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
        when(scheduledComPortFactory.newFor(firstComPort, issueService)).thenReturn(firstScheduledComPort);
        when(scheduledComPortFactory.newFor(secondComPort, issueService)).thenReturn(secondScheduledComPort);
        ComPortListenerFactory comPortListenerFactory = mock(ComPortListenerFactory.class);

        RunningComServerImpl runningComServer = new RunningComServerImpl(comServer, comServerDAO, scheduledComPortFactory, comPortListenerFactory, new PooledThreadFactory(), new CleanupDuringStartupImpl(comServer, comServerDAO));
        runningComServer.start();

        // Change the scheduling interpoll delay
        TimeDuration newSchedulingInterPollDelay = new TimeDuration(5, TimeDuration.MINUTES);
        comServerDAO.setSchedulingInterPollDelay(0, newSchedulingInterPollDelay);

        this.waitForComServerToPickupChanges();
        runningComServer.shutdownImmediate();

        assertTrue("Was expecting the ComServer to have requested at least once for changes.", comServerDAO.getAndResetComServerRefreshCount() > 0);
        verify(firstScheduledComPort, times(1)).schedulingInterpollDelayChanged(newSchedulingInterPollDelay);
        verify(firstScheduledComPort, never()).changesInterpollDelayChanged(any(TimeDuration.class));
        verify(secondScheduledComPort, times(1)).schedulingInterpollDelayChanged(newSchedulingInterPollDelay);
        verify(secondScheduledComPort, never()).changesInterpollDelayChanged(any(TimeDuration.class));
    }

    private void waitForComServerToPickupChanges () throws InterruptedException {
        Thread.sleep(MockComServerDAO.CHANGES_DELAY_SECONDS * TimeConstants.MILLISECONDS_IN_SECOND + HALF_A_SECOND);   // Wait half a second longer than ComServer
    }

}