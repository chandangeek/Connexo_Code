package com.energyict.mdc.engine.impl.core.factories;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.factories.ScheduledComPortFactoryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (11:14)
 */
public class ScheduledComPortFactoryImplTest {

    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    private ServiceProvider serviceProvider = new FakeServiceProvider();

    @Test
    public void testWithActivePort () {
        ScheduledComPortFactoryImpl factory = new ScheduledComPortFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor, this.serviceProvider);
        assertNotNull("Was NOT expecting the factory to return null for an active port", factory.newFor(this.activeComPort()));
    }

    @Test
    public void testWithInactivePort () {
        ScheduledComPortFactoryImpl factory = new ScheduledComPortFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor, this.serviceProvider);
        assertNull("Was expecting the factory to return null for an inactive port", factory.newFor(this.inactiveComPort()));
    }

    @Test
    public void testWithActivePortWithZeroSimultaneousConnections () {
        ScheduledComPortFactoryImpl factory = new ScheduledComPortFactoryImpl(this.comServerDAO(), this.deviceCommandExecutor, this.serviceProvider);
        assertNull("Was expecting the factory to return null for active port with 0 simultaneous connections", factory.newFor(this.activeComPortWithZeroSimultaneousConnections()));
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