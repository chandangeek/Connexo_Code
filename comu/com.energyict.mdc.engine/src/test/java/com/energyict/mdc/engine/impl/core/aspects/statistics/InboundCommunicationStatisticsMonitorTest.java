package com.energyict.mdc.engine.impl.core.aspects.statistics;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ComTaskExecutionJob;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.ScheduledJobImpl;
import com.energyict.mdc.engine.impl.core.SystemOutComChannel;
import com.energyict.mdc.issues.IssueService;
import org.fest.assertions.api.Assertions;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the InboundCommunicationStatisticsMonitor aspects.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-19 (09:42)
 */
@RunWith(MockitoJUnitRunner.class)
public class InboundCommunicationStatisticsMonitorTest {

    private static final long COM_PORT_POOL_ID = 1;
    @Mock
    private OutboundComPortPool comPortPool;
    @Mock
    private DeviceCommandExecutor deviceCommandExecutor;
    @Mock
    private DeviceCommandExecutionToken token;
    @Mock
    private IssueService issueService;

    @Before
    public void initializeMocksAndFactories () {
        when(this.comPortPool.getId()).thenReturn(COM_PORT_POOL_ID);
        when(this.comPortPool.getComPortType()).thenReturn(ComPortType.TCP);
//        when(this.manager.getComSessionFactory()).thenReturn(this.comSessionFactory);
//        ManagerFactory.setCurrent(this.manager);
    }

    @Test
    public void testComSessionStatisticsUpdatedWhenConnectionIsBeingClosed () throws ConnectionException, BusinessException, SQLException {
        OutboundComPort comPort = mock(OutboundComPort.class);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        when(connectionTask.connect(comPort)).thenReturn(new SystemOutComChannel());
        ComTaskExecution comTask = mock(ComTaskExecution.class);
        when(comTask.getConnectionTask()).thenReturn(connectionTask);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comPort.getComServer()).thenReturn(comServer);
        ScheduledJobImpl scheduledJob = new ComTaskExecutionJob(comPort, mock(ComServerDAO.class), this.deviceCommandExecutor, comTask, issueService);
        try {
            if (scheduledJob.establishConnectionFor(comPort)) {
                scheduledJob.getExecutionContext().getComChannel().write("Hello world".getBytes());
                scheduledJob.getExecutionContext().getComChannel().read();
            }
        }
        finally {
            scheduledJob.closeConnection();
        }

        // Asserts
        ExecutionContext executionContext = scheduledJob.getExecutionContext();
        assertThat(executionContext).isNotNull();
        ComSessionShadow comSessionShadow = executionContext.getComSessionBuilder();
        assertThat(comSessionShadow).isNotNull();
        ComStatisticsShadow sessionStatistics = comSessionShadow.getComStatistics();
        assertThat(sessionStatistics).isNotNull();
        Assertions.assertThat(sessionStatistics.getNrOfBytesSent()).isNotZero();
        Assertions.assertThat(sessionStatistics.getNrOfPacketsSent()).isNotZero();
        Assertions.assertThat(sessionStatistics.getNrOfBytesRead()).isZero();  // Remember that SystemOutChannel always returns zero in the read method
        Assertions.assertThat(sessionStatistics.getNrOfPacketsRead()).isNotZero();
    }

    @Test
    public void testComSessionStatisticsCounterValues () throws ConnectionException, BusinessException, SQLException {
        OutboundComPort comPort = mock(OutboundComPort.class);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        ConfigurableReadComChannel comChannel = new ConfigurableReadComChannel();
        byte[] helloWorldBytes = "Hello world".getBytes();
        byte[] replyBytes = "Reply for hello world message".getBytes();
        comChannel.whenReadFromBuffer(replyBytes);
        when(connectionTask.connect(comPort)).thenReturn(comChannel);
        ComTaskExecution comTask = mock(ComTaskExecution.class);
        when(comTask.getConnectionTask()).thenReturn(connectionTask);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comPort.getComServer()).thenReturn(comServer);
        ScheduledJobImpl scheduledJob = new ComTaskExecutionJob(comPort, mock(ComServerDAO.class), this.deviceCommandExecutor, comTask, issueService);
        int numberOfBytesRead = 0;
        byte[] readBuffer = new byte[replyBytes.length];
        try {
            if (scheduledJob.establishConnectionFor(comPort)) {
                scheduledJob.getExecutionContext().getComChannel().write(helloWorldBytes);
                numberOfBytesRead = scheduledJob.getExecutionContext().getComChannel().read(readBuffer);
            }
        }
        finally {
            scheduledJob.closeConnection();
        }

        // Asserts
        ExecutionContext executionContext = scheduledJob.getExecutionContext();
        assertThat(executionContext).isNotNull();
        ComSessionShadow comSessionShadow = executionContext.getComSessionBuilder();
        assertThat(comSessionShadow).isNotNull();
        ComStatisticsShadow sessionStatistics = comSessionShadow.getComStatistics();
        assertThat(sessionStatistics).isNotNull();
        Assertions.assertThat(sessionStatistics.getNrOfBytesSent()).isEqualTo(helloWorldBytes.length);
        Assertions.assertThat(sessionStatistics.getNrOfPacketsSent()).isEqualTo(1);
        Assertions.assertThat(sessionStatistics.getNrOfBytesRead()).isEqualTo(numberOfBytesRead);
        Assertions.assertThat(sessionStatistics.getNrOfPacketsRead()).isEqualTo(1);
    }

    @Test
    public void testComSessionStatisticsCounterValuesWithMultipleReadWriteCycles () throws ConnectionException, BusinessException, SQLException {
        OutboundComPort comPort = mock(OutboundComPort.class);
        OutboundConnectionTask connectionTask = mock(OutboundConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        ConfigurableReadComChannel comChannel = new ConfigurableReadComChannel();
        byte[] helloWorldBytes = "Hello world".getBytes();
        byte[] replyBytes = "Reply for hello world message".getBytes();
        comChannel.whenReadFromBuffer(replyBytes);
        when(connectionTask.connect(comPort)).thenReturn(comChannel);
        ComTaskExecution comTask = mock(ComTaskExecution.class);
        when(comTask.getConnectionTask()).thenReturn(connectionTask);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comPort.getComServer()).thenReturn(comServer);
        ScheduledJobImpl scheduledJob = new ComTaskExecutionJob(comPort, mock(ComServerDAO.class), this.deviceCommandExecutor, comTask, issueService);
        int numberOfBytesRead = 0;
        byte[] readBuffer = new byte[replyBytes.length];
        int numberOfReadWriteCycles = 5;
        try {
            for (int i = 0; i < numberOfReadWriteCycles; i++) {
                if (scheduledJob.establishConnectionFor(comPort)) {
                    scheduledJob.getExecutionContext().getComChannel().write(helloWorldBytes);
                    numberOfBytesRead = numberOfBytesRead + scheduledJob.getExecutionContext().getComChannel().read(readBuffer);
                }
            }
        }
        finally {
            scheduledJob.closeConnection();
        }

        // Asserts
        ExecutionContext executionContext = scheduledJob.getExecutionContext();
        assertThat(executionContext).isNotNull();
        ComSessionShadow comSessionShadow = executionContext.getComSessionBuilder();
        assertThat(comSessionShadow).isNotNull();
        ComStatisticsShadow sessionStatistics = comSessionShadow.getComStatistics();
        assertThat(sessionStatistics).isNotNull();
        Assertions.assertThat(sessionStatistics.getNrOfBytesSent()).isEqualTo(helloWorldBytes.length * numberOfReadWriteCycles);
        Assertions.assertThat(sessionStatistics.getNrOfPacketsSent()).isEqualTo(numberOfReadWriteCycles);
        Assertions.assertThat(sessionStatistics.getNrOfBytesRead()).isEqualTo(numberOfBytesRead);
        Assertions.assertThat(sessionStatistics.getNrOfPacketsRead()).isEqualTo(numberOfReadWriteCycles);
    }

}