package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskPropertyProvider;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.impl.IssueServiceImpl;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.impl.HexServiceImpl;
import com.energyict.mdc.protocol.api.services.HexService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the statistics defined on {@link ComChannelBasedComPortListenerImpl}.
 * Uses a SystemOutComChannel to read and write from.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-14 (11:45)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComChannelBasedComPortListenerStatisticsTest {

    private static final int singleByte = 97;   // I just like prime numbers
    private static final String FIRST_MESSAGE = "first series of bytes";
    private static final byte[] FIRST_SERIES_OF_BYTES = FIRST_MESSAGE.getBytes();
    private static final String SECOND_MESSAGE_PREFIX = "Com";
    private static final String SECOND_MESSAGE = "ChannelReadWriteLoggerTest";
    private static final String SECOND_MESSAGE_PART = SECOND_MESSAGE_PREFIX + SECOND_MESSAGE;
    private static final byte[] SECOND_SERIES_OF_BYTES = SECOND_MESSAGE_PART.getBytes();

    private static final int SECOND_SERIES_OF_BYTES_OFFSET = 3;
    private static final int SECOND_SERIES_OF_BYTES_LENGTH = SECOND_SERIES_OF_BYTES.length - SECOND_SERIES_OF_BYTES_OFFSET;
    private static final long COMPORT_POOL_ID = 1;

    @Mock
    private EventPublisherImpl eventPublisher;
    @Mock
    private ComPort comPort;
    @Mock
    private ConnectionTaskService connectionTaskService;
    @Mock
    private DeviceService deviceService;
    @Mock
    private NlsService nlsService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComSessionBuilder comSessionBuilder;
    @Mock
    private JobExecution.ServiceProvider serviceProvider;

    private Clock clock = Clock.fixed(Instant.ofEpochMilli(514851820000L), ZoneId.systemDefault()); // what happened in GMT+3 ?
    private HexService hexService;
    private String expectedMessageRead;
    private String expectedMessageWritten;
    private MockJobExecution jobExecution;

    @Before
    public void initializeMocksAndFactories() throws IOException {
        this.initializeExpectedMessage();
    }

    @Before
    public void initializeMocks() {
        this.hexService = new HexServiceImpl();
        when(this.serviceProvider.clock()).thenReturn(this.clock);
        IssueServiceImpl issueService = new IssueServiceImpl(this.clock, this.nlsService);
        when(this.serviceProvider.issueService()).thenReturn(issueService);
        when(this.serviceProvider.hexService()).thenReturn(this.hexService);
        when(this.serviceProvider.connectionTaskService()).thenReturn(this.connectionTaskService);
        when(this.serviceProvider.deviceService()).thenReturn(this.deviceService);
        when(this.connectionTaskService.buildComSession(any(ConnectionTask.class), any(ComPortPool.class), any(ComPort.class), any(Instant.class))).thenReturn(comSessionBuilder);
    }

    private void initializeExpectedMessage() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(singleByte);
        os.write(FIRST_SERIES_OF_BYTES);
        os.write(SECOND_SERIES_OF_BYTES, SECOND_SERIES_OF_BYTES_OFFSET, SECOND_SERIES_OF_BYTES_LENGTH);
        String hexMessage = new HexServiceImpl().toHexString(os.toByteArray());
        this.expectedMessageRead = "RX " + hexMessage;
        this.expectedMessageWritten = "TX " + hexMessage;
    }

    @Test
    public void testReadBytesBetweenCreationAndClose() {
        ComPortRelatedComChannel comChannel = this.newComChannelForReading();
        this.readFrom(comChannel);

        // Business method that completes the reading session
        comChannel.close();

        // Asserts
        this.assertComSessionJournalMessage(this.expectedMessageRead);
    }

    @Test
    public void testReadBytesBetweenCreationAndStartWriting() {
        ComPortRelatedComChannel comChannel = this.newComChannelForReading();
        this.readFrom(comChannel);

        // Business method that completes the reading session
        comChannel.startWriting();

        // Asserts
        this.assertComSessionJournalMessage(this.expectedMessageRead);
    }

    @Test
    public void testReadBytesBetweenStartReadingAndClose() {
        ComPortRelatedComChannel comChannel = this.newComChannelForReading();
        this.readFrom(comChannel);

        // Business method that starts the reading session
        comChannel.startReading();

        this.readFrom(comChannel);

        // Business method that completes the reading session
        comChannel.close();

        // Asserts
        this.assertComSessionJournalMessage(this.expectedMessageRead);
    }

    @Test
    public void testReadBytesBetweenStartReadingAndStartWriting() {
        ComPortRelatedComChannel comChannel = this.newComChannelForReading();
        this.readFrom(comChannel);

        // Business method that starts the reading session
        comChannel.startReading();

        this.readFrom(comChannel);

        // Business method that completes the reading session
        comChannel.startWriting();

        // Asserts
        this.assertComSessionJournalMessage(this.expectedMessageRead);
    }

    @Test
    public void testReadBytesBetweenCreationAndCloseWithInsufficientLogLevel() {
        ComPortRelatedComChannel comChannel = this.newComChannelForReading(ComServer.LogLevel.INFO);
        this.readFrom(comChannel);

        // Business method that completes the reading session
        comChannel.close();

        // Asserts
        this.assertNoComSessionJournalMessage();
    }

    @Test
    public void testReadBytesBetweenCreationAndStartWritingWithInsufficientLogLevel() {
        ComPortRelatedComChannel comChannel = this.newComChannelForReading(ComServer.LogLevel.INFO);
        this.readFrom(comChannel);

        // Business method that completes the reading session
        comChannel.startWriting();

        // Asserts
        this.assertNoComSessionJournalMessage();
    }

    @Test
    public void testReadBytesBetweenStartReadingAndCloseWithInsufficientLogLevel() {
        ComPortRelatedComChannel comChannel = this.newComChannelForReading(ComServer.LogLevel.INFO);
        this.readFrom(comChannel);

        // Business method that starts the reading session
        comChannel.startReading();

        this.readFrom(comChannel);

        // Business method that completes the reading session
        comChannel.close();

        // Asserts
        this.assertNoComSessionJournalMessage();
    }

    @Test
    public void testReadBytesBetweenStartReadingAndStartWritingWithInsufficientLogLevel() {
        ComPortRelatedComChannel comChannel = this.newComChannelForReading(ComServer.LogLevel.INFO);
        this.readFrom(comChannel);

        // Business method that starts the reading session
        comChannel.startReading();

        this.readFrom(comChannel);

        // Business method that completes the reading session
        comChannel.startWriting();

        // Asserts
        this.assertNoComSessionJournalMessage();
    }

    @Test
    public void testWriteBytesBetweenCreationAndClose() {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting();
        this.writeTo(comChannel);

        // Business method that completes the reading session
        comChannel.close();

        // Asserts
        this.assertComSessionJournalMessage(this.expectedMessageWritten);
    }

    @Test
    public void testWriteBytesBetweenCreationAndStartReading() {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting();
        this.writeTo(comChannel);

        // Business method that completes the writing session
        comChannel.startReading();

        // Asserts
        this.assertComSessionJournalMessage(this.expectedMessageWritten);
    }

    @Test
    public void testWriteBytesBetweenStartWritingAndClose() {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting();

        // Business method that starts the reading session
        comChannel.startWriting();

        this.writeTo(comChannel);

        // Business method that completes the reading session
        comChannel.close();

        // Asserts
        this.assertComSessionJournalMessage(this.expectedMessageWritten);
    }

    @Test
    public void testWriteBytesBetweenStartWritingAndStartReading() {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting();

        // Business method that starts the reading session
        comChannel.startWriting();

        this.writeTo(comChannel);

        // Business method that completes the reading session
        comChannel.startReading();

        // Asserts
        this.assertComSessionJournalMessage(this.expectedMessageWritten);
    }

    @Test
    public void testWriteBytesBetweenCreationAndCloseWithInsufficientLogLevel() {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting(ComServer.LogLevel.INFO);
        this.writeTo(comChannel);

        // Business method that completes the reading session
        comChannel.close();

        // Asserts
        this.assertNoComSessionJournalMessage();
    }

    @Test
    public void testWriteBytesBetweenCreationAndStartReadingWithInsufficientLogLevel() {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting(ComServer.LogLevel.INFO);
        this.writeTo(comChannel);

        // Business method that completes the writing session
        comChannel.startReading();

        // Asserts
        this.assertNoComSessionJournalMessage();
    }

    @Test
    public void testWriteBytesBetweenStartWritingAndCloseWithInsufficientLogLevel() {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting(ComServer.LogLevel.INFO);

        // Business method that starts the reading session
        comChannel.startWriting();

        this.writeTo(comChannel);

        // Business method that completes the reading session
        comChannel.close();

        // Asserts
        this.assertNoComSessionJournalMessage();
    }

    @Test
    public void testWriteBytesBetweenStartWritingAndStartReadingWithInsufficientLogLevel() {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting(ComServer.LogLevel.INFO);

        // Business method that starts the reading session
        comChannel.startWriting();

        this.writeTo(comChannel);

        // Business method that completes the reading session
        comChannel.startReading();

        // Asserts
        this.assertNoComSessionJournalMessage();
    }

    private void assertComSessionJournalMessage(String expectedMessage) {
        verify(comSessionBuilder).addJournalEntry(eq(clock.instant()), any(ComServer.LogLevel.class), eq(expectedMessage));
    }

    private void assertNoComSessionJournalMessage() {
        verify(comSessionBuilder, never()).addJournalEntry(any(Instant.class), any(ComServer.LogLevel.class), anyString(), any(Throwable.class));
    }

    private void readFrom(ComPortRelatedComChannel comChannel) {
        this.readSingleByte(comChannel);
        this.readBytes(comChannel);
        this.readBytesWithOffset(comChannel);
    }

    private void readSingleByte(ComPortRelatedComChannel comChannel) {
        comChannel.read();
    }

    private void readBytes(ComPortRelatedComChannel comChannel) {
        byte[] bytes = new byte[FIRST_SERIES_OF_BYTES.length];
        comChannel.read(bytes);
    }

    private void readBytesWithOffset(ComPortRelatedComChannel comChannel) {
        byte[] bytes = new byte[SECOND_SERIES_OF_BYTES.length];
        comChannel.read(bytes, SECOND_SERIES_OF_BYTES_OFFSET, SECOND_SERIES_OF_BYTES_LENGTH);
    }

    private ComPortRelatedComChannel newComChannelForReading() {
        return this.newComChannelForReading(ComServer.LogLevel.TRACE);
    }

    private ComPortRelatedComChannel newComChannelForReading(ComServer.LogLevel comServerLogLevel) {
        ConfigurableReadComChannel comChannel = new ConfigurableReadComChannel();
        ComServer comServer = mock(ComServer.class);
        when(comServer.getServerLogLevel()).thenReturn(comServerLogLevel);
        when(comServer.getCommunicationLogLevel()).thenReturn(comServerLogLevel);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        comChannel.whenRead(singleByte);
        comChannel.whenReadFromBuffer(FIRST_SERIES_OF_BYTES);
        comChannel.whenReadFromBufferWithOffset(SECOND_SERIES_OF_BYTES, SECOND_SERIES_OF_BYTES_OFFSET, SECOND_SERIES_OF_BYTES_LENGTH);
        ComPortRelatedComChannel comPortRelatedComChannel = new ComPortRelatedComChannelImpl(comChannel, comPort, clock, this.hexService, eventPublisher);
        this.jobExecution = new MockJobExecution(comPort, comPortRelatedComChannel, serviceProvider);
        this.jobExecution.getExecutionContext().connect();   // Should initialize the communication statistics
        return comPortRelatedComChannel;
    }

    private void writeTo(ComPortRelatedComChannel comChannel) {
        this.writeSingleByteTo(comChannel);
        this.writeBytesTo(comChannel);
    }

    private void writeSingleByteTo(ComPortRelatedComChannel comChannel) {
        comChannel.write(singleByte);
    }

    private void writeBytesTo(ComPortRelatedComChannel comChannel) {
        comChannel.write(FIRST_SERIES_OF_BYTES);
        byte[] secondSeriesOfBytes = new byte[SECOND_SERIES_OF_BYTES_LENGTH];
        System.arraycopy(SECOND_SERIES_OF_BYTES, SECOND_SERIES_OF_BYTES_OFFSET, secondSeriesOfBytes, 0, SECOND_SERIES_OF_BYTES_LENGTH);
        comChannel.write(secondSeriesOfBytes);
    }

    private ComPortRelatedComChannel newComChannelForWriting() {
        return this.newComChannelForWriting(ComServer.LogLevel.TRACE);
    }

    private ComPortRelatedComChannel newComChannelForWriting(ComServer.LogLevel comServerLogLevel) {
        ComServer comServer = mock(ComServer.class);
        when(comServer.getServerLogLevel()).thenReturn(comServerLogLevel);
        when(comServer.getCommunicationLogLevel()).thenReturn(comServerLogLevel);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        ComPortRelatedComChannel comChannel = new ComPortRelatedComChannelImpl(new SystemOutComChannel(), comPort, this.clock, this.hexService, this.eventPublisher);
        this.jobExecution = new MockJobExecution(comPort, comChannel, this.serviceProvider);
        this.jobExecution.getExecutionContext().connect();
        comChannel.setComPort(this.comPort);
        return comChannel;
    }

    private static class ExecutionContextServiceProvider implements ExecutionContext.ServiceProvider {
        private final EventPublisher eventPublisher;
        private final JobExecution.ServiceProvider serviceProvider;

        private ExecutionContextServiceProvider(EventPublisher eventPublisher, JobExecution.ServiceProvider serviceProvider) {
            super();
            this.eventPublisher = eventPublisher;
            this.serviceProvider = serviceProvider;
        }

        @Override
        public EventPublisher eventPublisher() {
            return this.eventPublisher;
        }

        @Override
        public Clock clock() {
            return serviceProvider.clock();
        }

        @Override
        public NlsService nlsService() {
            return serviceProvider.nlsService();
        }

        @Override
        public EventService eventService() {
            return serviceProvider.eventService();
        }

        @Override
        public IssueService issueService() {
            return serviceProvider.issueService();
        }

        @Override
        public ConnectionTaskService connectionTaskService() {
            return serviceProvider.connectionTaskService();
        }

        @Override
        public DeviceService deviceService() {
            return serviceProvider.deviceService();
        }

        @Override
        public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
            return serviceProvider.mdcReadingTypeUtilService();
        }

        @Override
        public EngineService engineService() {
            return serviceProvider.engineService();
        }
    }

    private class MockJobExecution extends JobExecution {
        private ComPortRelatedComChannel comChannel;

        private MockJobExecution(ComPort comPort, ComPortRelatedComChannel comChannel, ServiceProvider serviceProvider) {
            super(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), serviceProvider);
            this.comChannel = comChannel;
            ConnectionTask connectionTask = mock(ConnectionTask.class);
            ComPortPool comPortPool = mock(ComPortPool.class);
            when(comPortPool.getId()).thenReturn(COMPORT_POOL_ID);
            when(connectionTask.getComPortPool()).thenReturn(comPortPool);
            this.setExecutionContext(new ExecutionContext(this, connectionTask, comPort, true, new ExecutionContextServiceProvider(eventPublisher, serviceProvider)));
        }

        @Override
        protected ComPortRelatedComChannel findOrCreateComChannel(ConnectionTaskPropertyProvider connectionTaskPropertyProvider) throws ConnectionException {
            return this.comChannel;
        }

        @Override
        public List<ComTaskExecution> getComTaskExecutions() {
            return new ArrayList<>(0);
        }

        @Override
        protected boolean isConnected() {
            return true;
        }

        @Override
        public List<ComTaskExecution> getNotExecutedComTaskExecutions() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public List<ComTaskExecution> getFailedComTaskExecutions() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public List<ComTaskExecution> getSuccessfulComTaskExecutions() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public ConnectionTask getConnectionTask() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean attemptLock() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void unlock() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isStillPending() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isWithinComWindow() {
            return true;
        }

        @Override
        public void outsideComWindow() {
        }

        @Override
        public void rescheduleToNextComWindow(ComServerDAO comServerDAO) {
        }

        @Override
        public void execute() {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

}