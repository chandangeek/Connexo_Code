package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.ProgrammableClock;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link CreateOutboundComSession} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-12 (11:27)
 */
public class CreateOutboundComSessionTest {

    private static final long CONNECTION_TASK_ID = 1;
    private Clock clock = new ProgrammableClock();

    @Test
    public void testConstructor() {
        ComSessionBuilder comSessionShadow = mock(ComSessionBuilder.class);

        // Business method
        CreateOutboundComSession command = new CreateOutboundComSession(ComServer.LogLevel.INFO, mock(ScheduledConnectionTask.class), comSessionShadow, ComSession.SuccessIndicator.Success, clock);

        // Asserts
        assertThat(command.getComSessionBuilder()).isEqualTo(comSessionShadow);
    }

    @Test
    public void testExecuteDelegatesToComServerDAO() {
        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        CreateOutboundComSession command = new CreateOutboundComSession(ComServer.LogLevel.INFO, connectionTask, comSessionBuilder, ComSession.SuccessIndicator.Success, clock);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(comServerDAO).createComSession(comSessionBuilder, ComSession.SuccessIndicator.Success);
    }

    @Test
    public void testExecuteDuringShutdownDelegatesToComServerDAO() {
        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        CreateOutboundComSession command = new CreateOutboundComSession(ComServer.LogLevel.INFO, connectionTask, comSessionBuilder, ComSession.SuccessIndicator.Success, clock);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        command.executeDuringShutdown(comServerDAO);

        // Asserts
        verify(comServerDAO).createComSession(comSessionBuilder, ComSession.SuccessIndicator.Success);
    }

    @Test(expected = MockedComServerDAOFailure.class)
    public void testExecuteWithFailureRethrowsFailure() {
        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        CreateOutboundComSession command = new CreateOutboundComSession(ComServer.LogLevel.INFO, connectionTask, comSessionBuilder, ComSession.SuccessIndicator.Success, clock);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        doThrow(MockedComServerDAOFailure.class).when(comServerDAO).createComSession(any(ComSessionBuilder.class), any(ComSession.SuccessIndicator.class));

        // Business method
        command.execute(comServerDAO);

        // Expected MockedComServerDAOFailure
    }

    @Test(expected = MockedComServerDAOFailure.class)
    public void testExecuteDuringShutdownWithFailureRethrowsFailure() {
        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        CreateOutboundComSession command = new CreateOutboundComSession(ComServer.LogLevel.INFO, connectionTask, comSessionBuilder, ComSession.SuccessIndicator.Success, clock);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        doThrow(MockedComServerDAOFailure.class).when(comServerDAO).createComSession(any(ComSessionBuilder.class), any(ComSession.SuccessIndicator.class));

        // Business method
        command.executeDuringShutdown(comServerDAO);

        // Expected MockedComServerDAOFailure
    }

    @Test
    public void testToString() {
        ComSession.SuccessIndicator successIndicator = ComSession.SuccessIndicator.Success;
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);
        ComSession comSession = mock(ComSession.class);
        when(comSession.getSuccessIndicator()).thenReturn(successIndicator);
        when(comSession.getConnectionTask()).thenReturn(connectionTask);
        ComPort comPort = mock(ComPort.class);
        when(comSession.getComPort()).thenReturn(comPort);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(comServerDAO.createComSession(comSessionBuilder, successIndicator)).thenReturn(comSession);
        CreateOutboundComSession command = new CreateOutboundComSession(ComServer.LogLevel.INFO, connectionTask, comSessionBuilder, successIndicator, clock);
        command.execute(comServerDAO);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.DEBUG);

        // Asserts
        assertThat(journalMessage).isEqualTo(CreateOutboundComSession.class.getSimpleName() +
                " {indicator: Success; connectionTaskId: 1; comPortId: 0; number of tasks: 0; number of journal entries: 0}");
    }

    private class MockedComServerDAOFailure extends RuntimeException {

        private MockedComServerDAOFailure() {
            super();
        }
    }

}