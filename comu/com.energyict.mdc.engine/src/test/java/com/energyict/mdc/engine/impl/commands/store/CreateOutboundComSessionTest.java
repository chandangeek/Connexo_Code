package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.comserver.core.ComServerDAO;
import com.energyict.mdc.device.data.journal.ComSession;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.shadow.journal.ComSessionShadow;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import org.junit.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CreateOutboundComSession} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-12 (11:27)
 */
public class CreateOutboundComSessionTest {

    private static final int CONNECTION_TASK_ID = 1;

    @Test
    public void testConstructor () {
        ComSessionShadow comSessionShadow = new ComSessionShadow();

        // Business method
        CreateOutboundComSession command = new CreateOutboundComSession(ComServer.LogLevel.INFO, mock(OutboundConnectionTask.class), comSessionShadow);

        // Asserts
        assertThat(command.getComSessionShadow()).isEqualTo(comSessionShadow);
    }

    @Test
    public void testExecuteDelegatesToComServerDAO () {
        ComSessionShadow comSessionShadow = new ComSessionShadow();
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        CreateOutboundComSession command = new CreateOutboundComSession(ComServer.LogLevel.INFO, connectionTask, comSessionShadow);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(comServerDAO).createOutboundComSession(connectionTask, comSessionShadow);
    }

    @Test
    public void testExecuteDuringShutdownDelegatesToComServerDAO () {
        ComSessionShadow comSessionShadow = new ComSessionShadow();
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        CreateOutboundComSession command = new CreateOutboundComSession(ComServer.LogLevel.INFO, connectionTask, comSessionShadow);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        command.executeDuringShutdown(comServerDAO);

        // Asserts
        verify(comServerDAO).createOutboundComSession(connectionTask, comSessionShadow);
    }

    @Test(expected = MockedComServerDAOFailure.class)
    public void testExecuteWithFailureRethrowsFailure () {
        ComSessionShadow comSessionShadow = new ComSessionShadow();
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        CreateOutboundComSession command = new CreateOutboundComSession(ComServer.LogLevel.INFO, connectionTask, comSessionShadow);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        doThrow(MockedComServerDAOFailure.class).when(comServerDAO).createOutboundComSession(any(OutboundConnectionTask.class), any(ComSessionShadow.class));

        // Business method
        command.execute(comServerDAO);

        // Expected MockedComServerDAOFailure
    }

    @Test(expected = MockedComServerDAOFailure.class)
    public void testExecuteDuringShutdownWithFailureRethrowsFailure () {
        ComSessionShadow comSessionShadow = new ComSessionShadow();
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        CreateOutboundComSession command = new CreateOutboundComSession(ComServer.LogLevel.INFO, connectionTask, comSessionShadow);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        doThrow(MockedComServerDAOFailure.class).when(comServerDAO).createOutboundComSession(any(OutboundConnectionTask.class), any(ComSessionShadow.class));

        // Business method
        command.executeDuringShutdown(comServerDAO);

        // Expected MockedComServerDAOFailure
    }

    @Test
    public void testToString () {
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        ComSessionShadow comSessionShadow = new ComSessionShadow();
        comSessionShadow.setSuccessIndicator(ComSession.SuccessIndicator.Success);
        comSessionShadow.setConnectionTaskId(CONNECTION_TASK_ID);
        CreateOutboundComSession command = new CreateOutboundComSession(ComServer.LogLevel.INFO, connectionTask, comSessionShadow);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.DEBUG);

        // Asserts
        Assertions.assertThat(journalMessage).isEqualTo(CreateOutboundComSession.class.getSimpleName() +
                " {indicator: Success; connectionTaskId: 1; comPortId: 0; number of tasks: 0; number of journal entries: 0}");
    }

    private class MockedComServerDAOFailure extends RuntimeException {
        private MockedComServerDAOFailure () {
            super();
        }
    }

}