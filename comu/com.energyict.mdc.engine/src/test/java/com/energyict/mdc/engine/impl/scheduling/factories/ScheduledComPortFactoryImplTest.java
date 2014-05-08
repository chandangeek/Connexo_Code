package com.energyict.mdc.engine.impl.scheduling.factories;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.comserver.commands.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.ComServer;
import org.junit.*;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ScheduledComPortFactoryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (11:14)
 */
public class ScheduledComPortFactoryImplTest {

    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;

    @Test
    public void testWithActivePort () {
        ScheduledComPortFactoryImpl factory = new ScheduledComPortFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor);
        assertNotNull("Was NOT expecting the factory to return null for an active port", factory.newFor(this.activeComPort(), issueService));
    }

    @Test
    public void testWithInactivePort () {
        ScheduledComPortFactoryImpl factory = new ScheduledComPortFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor);
        assertNull("Was expecting the factory to return null for an inactive port", factory.newFor(this.inactiveComPort(), issueService));
    }

    @Test
    public void testWithActivePortWithZeroSimultaneousConnections () {
        ScheduledComPortFactoryImpl factory = new ScheduledComPortFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor);
        assertNull("Was expecting the factory to return null for active port with 0 simultaneous connections", factory.newFor(this.activeComPortWithZeroSimultaneousConnections(), issueService));
    }

    private ComServerDAO comServerDAO () {
        return mock(ComServerDAO.class);
    }

    private OutboundComPort activeComPort () {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getSchedulingInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        when(comServer.getChangesInterPollDelay()).thenReturn(TimeDuration.minutes(1));
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(1);
        return comPort;
    }

    private OutboundComPort inactiveComPort () {
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.isActive()).thenReturn(false);
        return comPort;
    }

    private OutboundComPort activeComPortWithZeroSimultaneousConnections () {
        OutboundComPort comPort = mock(OutboundComPort.class);
        when(comPort.isActive()).thenReturn(true);
        when(comPort.getNumberOfSimultaneousConnections()).thenReturn(0);
        return comPort;
    }

}