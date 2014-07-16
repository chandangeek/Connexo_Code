package com.energyict.mdc.engine.impl.core.factories;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ComServerThreadFactory;
import com.energyict.mdc.engine.impl.core.MultiThreadedComPortListener;
import com.energyict.mdc.engine.impl.core.ServletInboundComPortListener;
import com.energyict.mdc.engine.impl.core.SingleThreadedComPortListener;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;
import com.energyict.mdc.issues.IssueService;

import com.energyict.protocols.mdc.services.SocketService;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ThreadFactory;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ComPortListenerFactoryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (12:05)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComPortListenerFactoryImplTest {

    @Mock
    private ComServer comServer;
    @Mock
    private SocketService socketService;
    @Mock
    private ServerSocket serverSocket;

    private ThreadFactory threadFactory;
    private FakeServiceProvider serviceProvider = new FakeServiceProvider();

    @Before
    public void setUpServiceProvider () throws IOException {
        this.serviceProvider.setIssueService(mock(IssueService.class));
        when(this.socketService.newTCPSocket(anyInt())).thenReturn(this.serverSocket);
        this.serviceProvider.setSocketService(this.socketService);
    }

    @Before
    public void setUpThreadFactory () {
        when(this.comServer.getName()).thenReturn("ComPortListenerFactoryImplTest");
        this.threadFactory = new ComServerThreadFactory(this.comServer);
    }

    @Before
    public void setupSocketService() {
        serviceProvider.setSocketService(socketService);
    }

    @Test
    public void testWithActivePort () {
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.threadFactory, this.serviceProvider);

        // Busines method
        ComPortListener comPortListener = factory.newFor(this.activeComPort());

        // Asserts
        assertNotNull("Was NOT expecting the factory to return null for an active port", comPortListener);
    }

    @Test
    public void testWithInactivePort () {
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.threadFactory, this.serviceProvider);
        assertNull("Was expecting the factory to return null for an inactive port", factory.newFor(this.inactiveComPort()));
    }

    @Test
    public void testWithActivePortWithZeroSimultaneousConnections () {
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.threadFactory, this.serviceProvider);
        assertNull("Was expecting the factory to return null for active port with 0 simultaneous connections", factory.newFor(this.activeComPortWithZeroSimultaneousConnections()));
    }

    @Test
    public void testServletBasedInboundComPortListener(){
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.threadFactory, this.serviceProvider);
        final ComPortListener comPortListener = factory.newFor(this.servletBasedInboundComPort());
        assertNotNull(comPortListener);
        assertTrue(comPortListener instanceof ServletInboundComPortListener);
    }

    @Test
    public void testSingleThreadedComPortListener(){
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.threadFactory, this.serviceProvider);
        final ComPortListener comPortListener = factory.newFor(this.singleThreadedInboundComPort());
        assertNotNull(comPortListener);
        assertTrue(comPortListener instanceof SingleThreadedComPortListener);
    }

    @Test
    public void testMultiThreadedComPortListener(){
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.threadFactory, this.serviceProvider);
        final ComPortListener comPortListener = factory.newFor(this.multiThreadedInboundComPort());
        assertNotNull(comPortListener);
        assertTrue(comPortListener instanceof MultiThreadedComPortListener);
    }

    private ComServerDAO comServerDAO () {
        return mock(ComServerDAO.class);
    }

    private DeviceCommandExecutor deviceCommandExecutor() {
        return mock(DeviceCommandExecutor.class);
    }

    private InboundComPort activeComPort () {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        InboundComPort comPort = mock(TCPBasedInboundComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(1);
        return comPort;
    }

    private InboundComPort inactiveComPort () {
        InboundComPort comPort = mock(TCPBasedInboundComPort.class);
        when(comPort.isActive()).thenReturn(false);
        return comPort;
    }

    private InboundComPort activeComPortWithZeroSimultaneousConnections () {
        InboundComPort comPort = mock(TCPBasedInboundComPort.class);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(0);
        return comPort;
    }

    private InboundComPort servletBasedInboundComPort(){
        ComServer comServer = mock(ComServer.class);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        InboundComPort comPort = mock(ServletBasedInboundComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comServer.getChangesInterPollDelay()).thenReturn(new TimeDuration(1));
        when(comPort.isActive()).thenReturn(true);
        when(comPort.isServletBased()).thenReturn(true);
        return comPort;
    }

    private InboundComPort singleThreadedInboundComPort(){
        ComServer comServer = mock(ComServer.class);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        InboundComPort comPort = mock(TCPBasedInboundComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.isServletBased()).thenReturn(false);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(1);
        return comPort;
    }

    private InboundComPort multiThreadedInboundComPort() {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        InboundComPort comPort = mock(TCPBasedInboundComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.isServletBased()).thenReturn(false);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(10000);
        return comPort;
    }

}