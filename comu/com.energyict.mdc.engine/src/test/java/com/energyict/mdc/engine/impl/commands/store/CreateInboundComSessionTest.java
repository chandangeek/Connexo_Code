/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.impl.core.ComServerDAO;

import java.time.Clock;
import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
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

    private static final long CONNECTION_TASK_ID = 1;
    private static final long COMPORT_ID = CONNECTION_TASK_ID + 1;
    private final Clock clock = Clock.systemDefaultZone();

    @Mock
    private DeviceCommand.ExecutionLogger executionLogger;
    @Mock
    private ComServer comServer;
    @Mock
    private InboundComPort comPort;
    @Mock
    private ComServerDAO comServerDao;

    @Before
    public void initializeMocks() {
        when(this.comPort.getComServer()).thenReturn(this.comServer);
        when(this.comPort.getId()).thenReturn(COMPORT_ID);
        when(this.comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
    }

    @Test
    public void testConstructor() {
        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);

        // Business method
        CreateInboundComSession command = new CreateInboundComSession(Instant.now(), this.comPort, mock(InboundConnectionTask.class), comSessionBuilder, ComSession.SuccessIndicator.Success, clock);

        // Asserts
        assertThat(command.getComSessionBuilder()).isEqualTo(comSessionBuilder);
    }

    @Test
    public void testExecuteDelegatesToComServerDAO() {
        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        Instant now = Instant.now();
        CreateInboundComSession command = new CreateInboundComSession(now, this.comPort, connectionTask, comSessionBuilder, ComSession.SuccessIndicator.Success, clock);
        command.setStopWatch(new StopWatch());
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        command.execute(comServerDAO);

        // Asserts
        verify(comServerDAO).createComSession(comSessionBuilder, now, ComSession.SuccessIndicator.Success);
    }

    @Test
    public void testExecuteDuringShutdownDelegatesToComServerDAO() {
        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        Instant now = Instant.now();
        CreateInboundComSession command = new CreateInboundComSession(now, this.comPort, connectionTask, comSessionBuilder, ComSession.SuccessIndicator.Success, clock);
        command.setStopWatch(new StopWatch());
        command.logExecutionWith(this.executionLogger);
        ComServerDAO comServerDAO = mock(ComServerDAO.class);

        // Business method
        command.executeDuringShutdown(comServerDAO);

        // Asserts
        verify(comServerDAO).createComSession(comSessionBuilder, now, ComSession.SuccessIndicator.Success);
    }

    @Test(expected = MockedComServerDAOFailure.class)
    public void testExecuteWithFailureRethrowsFailure() {
        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        CreateInboundComSession command = new CreateInboundComSession(Instant.now(), this.comPort, connectionTask, comSessionBuilder, ComSession.SuccessIndicator.Success, clock);
        command.setStopWatch(new StopWatch());
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        doThrow(MockedComServerDAOFailure.class).when(comServerDAO).createComSession(any(ComSessionBuilder.class), any(Instant.class), any(ComSession.SuccessIndicator.class));

        // Business method
        command.execute(comServerDAO);

        // Expected MockedComServerDAOFailure
    }

    @Test(expected = MockedComServerDAOFailure.class)
    public void testExecuteDuringShutdownWithFailureRethrowsFailure() {
        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        CreateInboundComSession command = new CreateInboundComSession(Instant.now(), this.comPort, connectionTask, comSessionBuilder, ComSession.SuccessIndicator.Success, clock);
        command.setStopWatch(new StopWatch());
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        doThrow(MockedComServerDAOFailure.class).when(comServerDAO).createComSession(any(ComSessionBuilder.class), any(Instant.class), any(ComSession.SuccessIndicator.class));

        // Business method
        command.executeDuringShutdown(comServerDAO);

        // Expected MockedComServerDAOFailure
    }

    @Test
    public void testToJournalMessageDescription() {
        InboundConnectionTask connectionTask = mock(InboundConnectionTask.class);
        when(connectionTask.getId()).thenReturn(CONNECTION_TASK_ID);
        ComSessionBuilder comSessionBuilder = mock(ComSessionBuilder.class);
        ComSessionBuilder.EndedComSessionBuilder endedComSessionBuilder = mock(ComSessionBuilder.EndedComSessionBuilder.class);
        when(comSessionBuilder.endSession(any(Instant.class), any(ComSession.SuccessIndicator.class))).thenReturn(endedComSessionBuilder);
        ComSession comSession = mock(ComSession.class);
        when(endedComSessionBuilder.create()).thenReturn(comSession);
        when(comSession.getSuccessIndicator()).thenReturn(ComSession.SuccessIndicator.Success);
        when(comSession.getComPort()).thenReturn(comPort);
        when(comSession.getConnectionTask()).thenReturn(connectionTask);
        when(comServerDao.createComSession(eq(comSessionBuilder), any(), eq(ComSession.SuccessIndicator.Success))).thenReturn(comSession);
        CreateInboundComSession command = new CreateInboundComSession(Instant.now(), comPort, connectionTask, comSessionBuilder, ComSession.SuccessIndicator.Success, clock);
        command.setStopWatch(new StopWatch());
        command.execute(comServerDao);

        // Business method
        String journalMessage = command.toJournalMessageDescription(ComServer.LogLevel.DEBUG);

        // Asserts
        assertThat(journalMessage).contains("{indicator: Success; connectionTaskId: 1; comPortId: 2; number of tasks: 0; number of journal entries: 0}");
    }

    private class MockedComServerDAOFailure extends RuntimeException {
        private MockedComServerDAOFailure() {
            super();
        }
    }

}