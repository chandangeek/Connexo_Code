package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.comserver.core.ComServerDAO;
import com.energyict.mdc.device.data.journal.ComSession;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.shadow.journal.ComSessionShadow;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CreateInboundComSession} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-12-12 (10:58)
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateInboundComSessionTest {

    private static final int CONNECTION_TASK_ID = 1;
    private static final int COMPORT_ID = CONNECTION_TASK_ID + 1;

    @Mock
    private DeviceCommand.ExecutionLogger executionLogger;
    @Mock
    private ComServer comServer;
    @Mock
    private InboundComPort comPort;

    @Before
    public void initializeMocks () {
        when(this.comPort.getComServer()).thenReturn(this.comServer);
        when(this.comPort.getId()).thenReturn(COMPORT_ID);
        when(this.comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
    }

    @Test
    public void testConstructor () {
        ComSessionShadow comSessionShadow = new ComSessionShadow();

        // Business method
        CreateInboundComSession command = new CreateInboundComSession(this.comPort, mock(InboundConnectionTask.class), comSessionShadow);

        // Asserts
        assertThat(command.getComSessionBuilder()).isEqualTo(comSessionShadow);
    }

    @Test
    public void testExecuteDelegatesToComServerDAO () {
        ComSessionShadow comSessionShadow = new ComSessionShadow();
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        CreateInboundComSession command = new CreateInboundComSession(this.comPort, connectionTask, comSessionShadow);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(comServerDAO).createInboundComSession(connectionTask, comSessionShadow);
    }

    @Test
    public void testExecuteDuringShutdownDelegatesToComServerDAO () {
        ComSessionShadow comSessionShadow = new ComSessionShadow();
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        CreateInboundComSession command = new CreateInboundComSession(this.comPort, connectionTask, comSessionShadow);
        command.logExecutionWith(this.executionLogger);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        command.executeDuringShutdown(comServerDAO);

        // Asserts
        verify(comServerDAO).createInboundComSession(connectionTask, comSessionShadow);
    }

    @Test(expected = MockedComServerDAOFailure.class)
    public void testExecuteWithFailureRethrowsFailure () {
        ComSessionShadow comSessionShadow = new ComSessionShadow();
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        CreateInboundComSession command = new CreateInboundComSession(this.comPort, connectionTask, comSessionShadow);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        doThrow(MockedComServerDAOFailure.class).when(comServerDAO).createInboundComSession(any(InboundConnectionTask.class), any(ComSessionShadow.class));

        // Business method
        command.execute(comServerDAO);

        // Expected MockedComServerDAOFailure
    }

    @Test(expected = MockedComServerDAOFailure.class)
    public void testExecuteDuringShutdownWithFailureRethrowsFailure () {
        ComSessionShadow comSessionShadow = new ComSessionShadow();
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        CreateInboundComSession command = new CreateInboundComSession(this.comPort, connectionTask, comSessionShadow);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        doThrow(MockedComServerDAOFailure.class).when(comServerDAO).createInboundComSession(any(InboundConnectionTask.class), any(ComSessionShadow.class));

        // Business method
        command.executeDuringShutdown(comServerDAO);

        // Expected MockedComServerDAOFailure
    }

    @Test
    public void testToJournalMessageDescription () {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        ComSessionShadow comSessionShadow = new ComSessionShadow();
        comSessionShadow.setSuccessIndicator(ComSession.SuccessIndicator.Success);
        comSessionShadow.setComPortId(COMPORT_ID);
        comSessionShadow.setConnectionTaskId(CONNECTION_TASK_ID);
        CreateInboundComSession command = new CreateInboundComSession(comPort, connectionTask, comSessionShadow);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.DEBUG);

        // Asserts
        Assertions.assertThat(journalMessage).isEqualTo(CreateInboundComSession.class.getSimpleName() +
                " {indicator: Success; connectionTaskId: 1; comPortId: 2; number of tasks: 0; number of journal entries: 0}");
    }

    private class MockedComServerDAOFailure extends RuntimeException {
        private MockedComServerDAOFailure () {
            super();
        }
    }

}