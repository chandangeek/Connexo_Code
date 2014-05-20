package com.energyict.mdc.engine.impl.core.factories;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.comserver.commands.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.impl.core.MultiThreadedComPortListener;
import com.energyict.mdc.engine.impl.core.ServletInboundComPortListener;
import com.energyict.mdc.engine.impl.core.SingleThreadedComPortListener;
import com.energyict.mdc.engine.impl.core.factories.ComPortListenerFactoryImpl;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.ComServer;
import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.factories.ComPortListenerFactoryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (12:05)
 */
public class ComPortListenerFactoryImplTest {

    @Test
    public void testWithActivePort () {
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor());
        assertNotNull("Was NOT expecting the factory to return null for an active port", factory.newFor(this.activeComPort(), issueService));
    }

    @Test
    public void testWithInactivePort () {
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor());
        assertNull("Was expecting the factory to return null for an inactive port", factory.newFor(this.inactiveComPort(), issueService));
    }

    @Test
    public void testWithActivePortWithZeroSimultaneousConnections () {
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor());
        assertNull("Was expecting the factory to return null for active port with 0 simultaneous connections", factory.newFor(this.activeComPortWithZeroSimultaneousConnections(), issueService));
    }

    @Test
    public void testServletBasedInboundComPortListener(){
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor());
        final ComPortListener comPortListener = factory.newFor(this.servletBasedInboundComPort(), issueService);
        assertNotNull(comPortListener);
        assertTrue(comPortListener instanceof ServletInboundComPortListener);
    }

    @Test
    public void testSingleThreadedComPortListener(){
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor());
        final ComPortListener comPortListener = factory.newFor(this.singleThreadedInboundComPort(), issueService);
        assertNotNull(comPortListener);
        assertTrue(comPortListener instanceof SingleThreadedComPortListener);
    }

    @Test
    public void testMultiThreadedComPortListener(){
        ComPortListenerFactoryImpl factory = new ComPortListenerFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor());
        final ComPortListener comPortListener = factory.newFor(this.multiThreadedInboundComPort(), issueService);
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
        InboundComPort comPort = mock(InboundComPort.class, withSettings().extraInterfaces(ServerComChannelBasedInboundComPort.class));
        when(comPort.getComServer()).thenReturn(comServer);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(1);
        return comPort;
    }

    private InboundComPort inactiveComPort () {
        InboundComPort comPort = mock(InboundComPort.class, withSettings().extraInterfaces(ServerComChannelBasedInboundComPort.class));
        when(comPort.isActive()).thenReturn(false);
        return comPort;
    }

    private InboundComPort activeComPortWithZeroSimultaneousConnections () {
        InboundComPort comPort = mock(InboundComPort.class, withSettings().extraInterfaces(ServerComChannelBasedInboundComPort.class));
        when(comPort.isActive()).thenReturn(true);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(0);
        return comPort;
    }

    private InboundComPort servletBasedInboundComPort(){
        ComServer comServer = mock(ComServer.class);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        InboundComPort comPort = mock(InboundComPort.class, withSettings().extraInterfaces(ServerComChannelBasedInboundComPort.class, ServerServletBasedInboundComPort.class));
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
        InboundComPort comPort = mock(InboundComPort.class, withSettings().extraInterfaces(ServerComChannelBasedInboundComPort.class));
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
        InboundComPort comPort = mock(InboundComPort.class, withSettings().extraInterfaces(ServerComChannelBasedInboundComPort.class));
        when(comPort.getComServer()).thenReturn(comServer);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.isServletBased()).thenReturn(false);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(10000);
        return comPort;
    }

}