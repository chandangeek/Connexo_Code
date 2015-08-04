package com.energyict.mdc.engine.impl.core.factories;

import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ScheduledComPortImpl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import java.time.Clock;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.core.factories.ScheduledComPortFactoryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-10 (11:14)
 */
@RunWith(MockitoJUnitRunner.class)
public class ScheduledComPortFactoryImplTest {

    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    @Mock
    private User user;
    @Mock
    private UserService userService;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private ScheduledComPortImpl.ServiceProvider serviceProvider;

    private Clock clock = Clock.systemDefaultZone();

    @Before
    public void initBefore() {
        when(this.userService.findUser(anyString())).thenReturn(Optional.of(this.user));
        when(this.serviceProvider.userService()).thenReturn(this.userService);
        when(this.serviceProvider.threadPrincipalService()).thenReturn(this.threadPrincipalService);
        when(this.serviceProvider.clock()).thenReturn(this.clock);
    }

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
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
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