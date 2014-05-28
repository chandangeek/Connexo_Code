package com.energyict.mdc.engine.impl.core.aspects.statistics;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ConfigurableReadComChannel;
import com.energyict.mdc.engine.impl.core.ScheduledComTaskExecutionJob;
import com.energyict.mdc.engine.impl.core.ScheduledJobImpl;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.core.SystemOutComChannel;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.issues.impl.IssueServiceImpl;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.tasks.history.ComSessionBuilder;
import com.energyict.mdc.tasks.history.TaskHistoryService;

import com.elster.jupiter.util.time.impl.DefaultClock;
import com.energyict.protocols.mdc.services.impl.HexServiceImpl;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.sql.SQLException;
import java.util.Date;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link InboundCommunicationStatisticsMonitor} aspects.
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
    private TaskHistoryService taskHistoryService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComSessionBuilder comSessionBuilder;
    @Mock
    private EventPublisherImpl eventPublisher;

    private FakeServiceProvider serviceProvider = new FakeServiceProvider();

    @Before
    public void initializeMocksAndFactories () {
        when(this.comPortPool.getId()).thenReturn(COM_PORT_POOL_ID);
        when(this.comPortPool.getComPortType()).thenReturn(ComPortType.TCP);
    }

    @Before
    public void setupServiceProvider() {
        DefaultClock clock = new DefaultClock();
        this.serviceProvider.setClock(clock);
        this.serviceProvider.setIssueService(new IssueServiceImpl(clock));
        this.serviceProvider.setHexService(new HexServiceImpl());
        this.serviceProvider.setTaskHistoryService(this.taskHistoryService);
        when(this.taskHistoryService.buildComSession(any(ConnectionTask.class), any(ComPortPool.class), any(ComPort.class), any(Date.class))).thenReturn(comSessionBuilder);
        ServiceProvider.instance.set(this.serviceProvider);
    }

    @After
    public void resetServiceProvider () {
        ServiceProvider.instance.set(null);
    }

    @Before
    public void setupEventPublisher () {
        EventPublisherImpl.setInstance(this.eventPublisher);
    }

    @After
    public void resetEventPublisher () {
        EventPublisherImpl.setInstance(null);
    }

    @Test
    public void testComSessionStatisticsUpdatedWhenConnectionIsBeingClosed () throws ConnectionException, BusinessException, SQLException {
        OutboundComPort comPort = mock(OutboundComPort.class);
        OutboundConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
        when(connectionTask.getComPortPool()).thenReturn(this.comPortPool);
        when(connectionTask.connect(comPort)).thenReturn(new SystemOutComChannel());
        ComTaskExecution comTask = mock(ComTaskExecution.class);
        when(comTask.getConnectionTask()).thenReturn(connectionTask);
        ComServer comServer = mock(OnlineComServer.class);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comServer.getCommunicationLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comPort.getComServer()).thenReturn(comServer);
        ScheduledJobImpl scheduledJob = new ScheduledComTaskExecutionJob(comPort, mock(ComServerDAO.class), this.deviceCommandExecutor, comTask, serviceProvider);
        try {
            scheduledJob.createExecutionContext();
            if (scheduledJob.getExecutionContext().connect()) {
                scheduledJob.getExecutionContext().getComChannel().write("Hello world".getBytes());
                scheduledJob.getExecutionContext().getComChannel().read();
            }
        }
        finally {
            scheduledJob.closeConnection();
        }

        // Asserts
        verify(comSessionBuilder).addSentBytes(longThat(IsGreaterThan.ZERO));
        verify(comSessionBuilder).addSentPackets(longThat(IsGreaterThan.ZERO));
        verify(comSessionBuilder).addReceivedBytes(0);  // Remember that SystemOutChannel always returns zero in the read method
        verify(comSessionBuilder).addReceivedPackets(longThat(IsGreaterThan.ZERO));
    }

    @Test
    public void testComSessionStatisticsCounterValues () throws ConnectionException, BusinessException, SQLException {
        OutboundComPort comPort = mock(OutboundComPort.class);
        OutboundConnectionTask connectionTask = mock(ScheduledConnectionTask.class);
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
        ScheduledJobImpl scheduledJob = new ScheduledComTaskExecutionJob(comPort, mock(ComServerDAO.class), this.deviceCommandExecutor, comTask, this.serviceProvider);
        int numberOfBytesRead = 0;
        byte[] readBuffer = new byte[replyBytes.length];
        try {
            scheduledJob.createExecutionContext();
            if (scheduledJob.getExecutionContext().connect()) {
                scheduledJob.getExecutionContext().getComChannel().write(helloWorldBytes);
                numberOfBytesRead = scheduledJob.getExecutionContext().getComChannel().read(readBuffer);
            }
        }
        finally {
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