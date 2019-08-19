/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.inbound;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.OnlineComServer;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.common.comserver.OutboundComPortPool;
import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ConfigurableReadComChannel;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.ScheduledComTaskExecutionGroup;
import com.energyict.mdc.engine.impl.core.ScheduledJobImpl;
import com.energyict.mdc.engine.impl.core.SystemOutComChannel;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.issues.impl.IssueServiceImpl;
import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.api.impl.HexServiceImpl;

import com.energyict.protocol.exceptions.ConnectionException;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the statistical monitoring aspects of the {@link InboundCommunicationHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-19 (09:42)
 */
@RunWith(MockitoJUnitRunner.class)
public class InboundCommunicationHandlerStatisticsTest {

    private static final long COM_PORT_POOL_ID = 1;

    @Mock
    private OutboundComPortPool comPortPool;
    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    @Mock
    private DeviceCommandExecutionToken token;
    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComSessionBuilder comSessionBuilder;
    @Mock
    private EventPublisherImpl eventPublisher;
    @Mock
    private JobExecution.ServiceProvider serviceProvider;
    @Mock
    private NlsService nlsService;

    @Before
    public void initializeMocksAndFactories() {
        when(this.comPortPool.getId()).thenReturn(COM_PORT_POOL_ID);
        when(this.comPortPool.getComPortType()).thenReturn(ComPortType.TCP);
    }

    @Before
    public void initializeMocks() {
        Clock clock = Clock.systemDefaultZone();
        when(this.serviceProvider.eventPublisher()).thenReturn(this.eventPublisher);
        when(this.serviceProvider.clock()).thenReturn(clock);
        IssueServiceImpl issueService = new IssueServiceImpl(clock, nlsService);
        when(this.serviceProvider.issueService()).thenReturn(issueService);
        when(this.serviceProvider.hexService()).thenReturn(new HexServiceImpl());
        when(this.serviceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.serviceProvider.transactionService()).thenReturn(TransactionModule.FakeTransactionService.INSTANCE);
        when(this.connectionTaskService.buildComSession(any(ConnectionTask.class), any(ComPortPool.class), any(ComPort.class), any(Instant.class))).thenReturn(comSessionBuilder);
    }

    @Test
    public void testComSessionStatisticsUpdatedWhenConnectionIsBeingClosed() throws ConnectionException, SQLException {
        OutboundComPort comPort = mock(OutboundComPort.class);
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        when(connectionTask.connect(eq(comPort), anyList())).thenReturn(new SystemOutComChannel());
        ComTaskExecution comTask = mock(ComTaskExecution.class);
        when(comTask.getConnectionTask()).thenReturn(Optional.of(connectionTask));
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comPort.getComServer()).thenReturn(comServer);
        ScheduledJobImpl scheduledJob = new ScheduledComTaskExecutionGroup(comPort, mock(ComServerDAO.class), this.deviceCommandExecutor, connectionTask, serviceProvider);
        try {
            scheduledJob.createExecutionContext();
            if (scheduledJob.getExecutionContext().connect()) {
                scheduledJob.getExecutionContext().getComPortRelatedComChannel().write("Hello world".getBytes());
                scheduledJob.getExecutionContext().getComPortRelatedComChannel().read();
            }
        } finally {
            scheduledJob.closeConnection();
        }

        // Asserts
        verify(comSessionBuilder).addSentBytes(longThat(IsGreaterThan.ZERO));
        verify(comSessionBuilder).addSentPackets(longThat(IsGreaterThan.ZERO));
        verify(comSessionBuilder).addReceivedBytes(1);  // Remember that SystemOutChannel always returns zero in the read method but that counts as a read byte
        verify(comSessionBuilder).addReceivedPackets(longThat(IsGreaterThan.ZERO));
    }

    @Test
    public void testComSessionStatisticsCounterValues() throws ConnectionException, SQLException {
        OutboundComPort comPort = mock(OutboundComPort.class);
        ScheduledConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        ConfigurableReadComChannel comChannel = new ConfigurableReadComChannel();
        byte[] helloWorldBytes = "Hello world".getBytes();
        byte[] replyBytes = "Reply for hello world message".getBytes();
        comChannel.whenReadFromBuffer(replyBytes);
        when(connectionTask.connect(eq(comPort), anyList())).thenReturn(comChannel);
        ComTaskExecution comTask = mock(ComTaskExecution.class);
        when(comTask.getConnectionTask()).thenReturn(Optional.of(connectionTask));
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comPort.getComServer()).thenReturn(comServer);
        ScheduledJobImpl scheduledJob = new ScheduledComTaskExecutionGroup(comPort, mock(ComServerDAO.class), this.deviceCommandExecutor, connectionTask, this.serviceProvider);
        int numberOfBytesRead = 0;
        byte[] readBuffer = new byte[replyBytes.length];
        try {
            scheduledJob.createExecutionContext();
            if (scheduledJob.getExecutionContext().connect()) {
                scheduledJob.getExecutionContext().getComPortRelatedComChannel().write(helloWorldBytes);
                numberOfBytesRead = scheduledJob.getExecutionContext().getComPortRelatedComChannel().read(readBuffer);
            }
        } finally {
            scheduledJob.closeConnection();
        }

        // Asserts
        verify(comSessionBuilder).addSentBytes(helloWorldBytes.length);
        verify(comSessionBuilder).addSentPackets(1);
        verify(comSessionBuilder).addReceivedBytes(numberOfBytesRead);
        verify(comSessionBuilder).addReceivedPackets(1);
    }

    private static class IsGreaterThan extends BaseMatcher<Long> {

        public static final IsGreaterThan ZERO = new IsGreaterThan(0);
        private final int bound;

        private IsGreaterThan(int bound) {
            this.bound = bound;
        }

        @Override
        public boolean matches(Object o) {
            return (Long) o > bound;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("is greater than 0");
        }
    }

}
