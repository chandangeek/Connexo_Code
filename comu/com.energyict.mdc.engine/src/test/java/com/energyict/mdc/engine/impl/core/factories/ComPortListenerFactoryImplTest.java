package com.energyict.mdc.engine.impl.core.factories;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.impl.core.ComServerThreadFactory;
import com.energyict.mdc.engine.impl.core.MultiThreadedComPortListener;
import com.energyict.mdc.engine.impl.core.ServletInboundComPortListener;
import com.energyict.mdc.engine.impl.core.SingleThreadedComPortListener;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;
import com.energyict.mdc.issues.IssueService;

import java.util.concurrent.ThreadFactory;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

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

    private ThreadFactory threadFactory;
    private FakeServiceProvider serviceProvider = new FakeServiceProvider();

    @Before
    public void setUpServiceProvider () {
        this.serviceProvider.setIssueService(mock(IssueService.class));
    }

    @Before
    public void setUpThreadFactory () {
        when(this.comServer.getName()).thenReturn("ComPortListenerFactoryImplTest");
        this.threadFactory = new ComServerThreadFactory(this.comServer);
    }

    @Test
    public void testWithActivePort () {
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.threadFactory);
        assertNotNull("Was NOT expecting the factory to return null for an active port", factory.newFor(this.activeComPort(), this.serviceProvider));
    }

    @Test
    public void testWithInactivePort () {
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.threadFactory);
        assertNull("Was expecting the factory to return null for an inactive port", factory.newFor(this.inactiveComPort(), this.serviceProvider));
    }

    @Test
    public void testWithActivePortWithZeroSimultaneousConnections () {
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.threadFactory);
        assertNull("Was expecting the factory to return null for active port with 0 simultaneous connections", factory.newFor(this.activeComPortWithZeroSimultaneousConnections(), this.serviceProvider));
    }

    @Test
    public void testServletBasedInboundComPortListener(){
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.threadFactory);
        final ComPortListener comPortListener = factory.newFor(this.servletBasedInboundComPort(), this.serviceProvider);
        assertNotNull(comPortListener);
        assertTrue(comPortListener instanceof ServletInboundComPortListener);
    }

    @Test
    public void testSingleThreadedComPortListener(){
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.threadFactory);
        final ComPortListener comPortListener = factory.newFor(this.singleThreadedInboundComPort(), this.serviceProvider);
        assertNotNull(comPortListener);
        assertTrue(comPortListener instanceof SingleThreadedComPortListener);
    }

    @Test
    public void testMultiThreadedComPortListener(){
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor(), this.threadFactory);
        final ComPortListener comPortListener = factory.newFor(this.multiThreadedInboundComPort(), this.serviceProvider);
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
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(1);
        return comPort;
    }

    private InboundComPort inactiveComPort () {
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.isActive()).thenReturn(false);
        return comPort;
    }

    private InboundComPort activeComPortWithZeroSimultaneousConnections () {
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(0);
        return comPort;
    }

    private InboundComPort servletBasedInboundComPort(){
        ComServer comServer = mock(ComServer.class);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        InboundComPort comPort = mock(InboundComPort.class, withSettings().extraInterfaces(ServletBasedInboundComPort.class));
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
        InboundComPort comPort = mock(InboundComPort.class);
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
        InboundComPort comPort = mock(InboundComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.isServletBased()).thenReturn(false);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(10000);
        return comPort;
    }

}