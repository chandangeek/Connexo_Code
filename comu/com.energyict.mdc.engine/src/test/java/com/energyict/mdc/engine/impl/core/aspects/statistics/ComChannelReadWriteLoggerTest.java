package com.energyict.mdc.engine.impl.core.aspects.statistics;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.ProgrammableClock;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.commands.store.core.CommandRootServiceProviderAdapter;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ConfigurableReadComChannel;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.core.SystemOutComChannel;
import com.energyict.mdc.engine.impl.core.aspects.logging.ComChannelReadWriteLogger;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannelImpl;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.tasks.history.ComSessionBuilder;
import com.energyict.mdc.tasks.history.TaskHistoryService;
import com.energyict.protocols.mdc.services.impl.HexServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests the pointcuts and advice defined in {@link ComChannelReadWriteLogger}.
 * Uses a SystemOutComChannel to read and write from.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-14 (11:45)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComChannelReadWriteLoggerTest {

    private static final int singleByte = 97;   // I just like prime numbers
    private static final String FIRST_MESSAGE = "first series of bytes";
    private static final byte[] FIRST_SERIES_OF_BYTES = FIRST_MESSAGE.getBytes();
    private static final String SECOND_MESSAGE_PREFIX = "Com";
    private static final String SECOND_MESSAGE = "ChannelReadWriteLoggerTest";
    private static final String SECOND_MESSAGE_PART = SECOND_MESSAGE_PREFIX + SECOND_MESSAGE;
    private static final byte[] SECOND_SERIES_OF_BYTES = SECOND_MESSAGE_PART.getBytes();

    private static final int SECOND_SERIES_OF_BYTES_OFFSET = 3;
    private static final int SECOND_SERIES_OF_BYTES_LENGTH = SECOND_SERIES_OF_BYTES.length - SECOND_SERIES_OF_BYTES_OFFSET;
    private static final int COMPORT_POOL_ID = 1;

    private final FakeServiceProvider serviceProvider = new FakeServiceProvider();

    @Mock
    private EventPublisherImpl eventPublisher;
    @Mock
    private ComPort comPort;
    @Mock
    private IssueService issueService;
    @Mock
    private TaskHistoryService taskHistoryService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComSessionBuilder comSessionBuilder;
    private Clock clock = new ProgrammableClock().frozenAt(new Date(514851820000L)); // what happened in GMT+3 ?

    private String expectedMessageRead;
    private String expectedMessageWritten;
    private MockJobExecution jobExecution;

    @Before
    public void initializeMocksAndFactories () throws IOException {
        serviceProvider.setClock(clock);
        serviceProvider.setTaskHistoryService(taskHistoryService);
        when(taskHistoryService.buildComSession(any(ConnectionTask.class), any(ComPortPool.class), any(ComPort.class), any(Date.class))).thenReturn(comSessionBuilder);
        this.initializeEventPublisher();
        this.initializeExpectedMessage();
    }

    private void initializeEventPublisher () {
        EventPublisherImpl.setInstance(this.eventPublisher);
    }

    private void initializeExpectedMessage () throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(singleByte);
        os.write(FIRST_SERIES_OF_BYTES);
        os.write(SECOND_SERIES_OF_BYTES, SECOND_SERIES_OF_BYTES_OFFSET, SECOND_SERIES_OF_BYTES_LENGTH);
        String hexMessage = new HexServiceImpl().toHexString(os.toByteArray());
        this.expectedMessageRead = "RX " + hexMessage;
        this.expectedMessageWritten = "TX " + hexMessage;
    }

    @Test
    public void testReadBytesBetweenCreationAndClose () {
        ComPortRelatedComChannel comChannel = this.newComChannelForReading();
        this.readFrom(comChannel);

        // Business method that completes the reading session
        comChannel.close();

        // Asserts
        this.assertComSessionJournalMessage(this.expectedMessageRead);
    }

    @Test
    public void testReadBytesBetweenCreationAndStartWriting () {
        ComPortRelatedComChannel comChannel = this.newComChannelForReading();
        this.readFrom(comChannel);

        // Business method that completes the reading session
        comChannel.startWriting();

        // Asserts
        this.assertComSessionJournalMessage(this.expectedMessageRead);
    }

    @Test
    public void testReadBytesBetweenStartReadingAndClose () {
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
    public void testReadBytesBetweenStartReadingAndStartWriting () {
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
    public void testReadBytesBetweenCreationAndCloseWithInsufficientLogLevel () {
        ComPortRelatedComChannel comChannel = this.newComChannelForReading(ComServer.LogLevel.INFO);
        this.readFrom(comChannel);

        // Business method that completes the reading session
        comChannel.close();

        // Asserts
        this.assertNoComSessionJournalMessage();
    }

    @Test
    public void testReadBytesBetweenCreationAndStartWritingWithInsufficientLogLevel () {
        ComPortRelatedComChannel comChannel = this.newComChannelForReading(ComServer.LogLevel.INFO);
        this.readFrom(comChannel);

        // Business method that completes the reading session
        comChannel.startWriting();

        // Asserts
        this.assertNoComSessionJournalMessage();
    }

    @Test
    public void testReadBytesBetweenStartReadingAndCloseWithInsufficientLogLevel () {
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
    public void testReadBytesBetweenStartReadingAndStartWritingWithInsufficientLogLevel () {
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
    public void testWriteBytesBetweenCreationAndClose () {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting();
        this.writeTo(comChannel);

        // Business method that completes the reading session
        comChannel.close();

        // Asserts
        this.assertComSessionJournalMessage(this.expectedMessageWritten);
    }

    @Test
    public void testWriteBytesBetweenCreationAndStartReading () {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting();
        this.writeTo(comChannel);

        // Business method that completes the writing session
        comChannel.startReading();

        // Asserts
        this.assertComSessionJournalMessage(this.expectedMessageWritten);
    }

    @Test
    public void testWriteBytesBetweenStartWritingAndClose () {
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
    public void testWriteBytesBetweenStartWritingAndStartReading () {
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
    public void testWriteBytesBetweenCreationAndCloseWithInsufficientLogLevel () {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting(ComServer.LogLevel.INFO);
        this.writeTo(comChannel);

        // Business method that completes the reading session
        comChannel.close();

        // Asserts
        this.assertNoComSessionJournalMessage();
    }

    @Test
    public void testWriteBytesBetweenCreationAndStartReadingWithInsufficientLogLevel () {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting(ComServer.LogLevel.INFO);
        this.writeTo(comChannel);

        // Business method that completes the writing session
        comChannel.startReading();

        // Asserts
        this.assertNoComSessionJournalMessage();
    }

    @Test
    public void testWriteBytesBetweenStartWritingAndCloseWithInsufficientLogLevel () {
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
    public void testWriteBytesBetweenStartWritingAndStartReadingWithInsufficientLogLevel () {
        ComPortRelatedComChannel comChannel = this.newComChannelForWriting(ComServer.LogLevel.INFO);

        // Business method that starts the reading session
        comChannel.startWriting();

        this.writeTo(comChannel);

        // Business method that completes the reading session
        comChannel.startReading();

        // Asserts
        this.assertNoComSessionJournalMessage();
    }

    private void assertComSessionJournalMessage (String expectedMessage) {
        verify(comSessionBuilder).addJournalEntry(clock.now(), expectedMessage, null);
    }

    private void assertNoComSessionJournalMessage () {
        verify(comSessionBuilder, never()).addJournalEntry(any(Date.class), anyString(), any(Throwable.class));
    }

    private void readFrom (ComPortRelatedComChannel comChannel) {
        this.readSingleByte(comChannel);
        this.readBytes(comChannel);
        this.readBytesWithOffset(comChannel);
    }

    private void readSingleByte (ComPortRelatedComChannel comChannel) {
        comChannel.read();
    }

    private void readBytes (ComPortRelatedComChannel comChannel) {
        byte[] bytes = new byte[FIRST_SERIES_OF_BYTES.length];
        comChannel.read(bytes);
    }

    private void readBytesWithOffset (ComPortRelatedComChannel comChannel) {
        byte[] bytes = new byte[SECOND_SERIES_OF_BYTES.length];
        comChannel.read(bytes, SECOND_SERIES_OF_BYTES_OFFSET, SECOND_SERIES_OF_BYTES_LENGTH);
    }

    private ComPortRelatedComChannel newComChannelForReading () {
        return this.newComChannelForReading(ComServer.LogLevel.TRACE);
    }

    private ComPortRelatedComChannel newComChannelForReading (ComServer.LogLevel comServerLogLevel) {
        ConfigurableReadComChannel comChannel = new ConfigurableReadComChannel();
        ComServer comServer = mock(ComServer.class);
        when(comServer.getServerLogLevel()).thenReturn(comServerLogLevel);
        when(comServer.getCommunicationLogLevel()).thenReturn(comServerLogLevel);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        comChannel.whenRead(singleByte);
        comChannel.whenReadFromBuffer(FIRST_SERIES_OF_BYTES);
        comChannel.whenReadFromBufferWithOffset(SECOND_SERIES_OF_BYTES, SECOND_SERIES_OF_BYTES_OFFSET, SECOND_SERIES_OF_BYTES_LENGTH);
        ComPortRelatedComChannel comPortRelatedComChannel = new ComPortRelatedComChannelImpl(comChannel);
        comPortRelatedComChannel.setComPort(this.comPort);
        this.jobExecution = new MockJobExecution(comPort, comPortRelatedComChannel, serviceProvider);
        this.jobExecution.getExecutionContext().connect();   // Should initialize the ComChannelReadWriteLogger
        return comPortRelatedComChannel;
    }

    private void writeTo (ComPortRelatedComChannel comChannel) {
        this.writeSingleByteTo(comChannel);
        this.writeBytesTo(comChannel);
    }

    private void writeSingleByteTo (ComPortRelatedComChannel comChannel) {
        comChannel.write(singleByte);
    }

    private void writeBytesTo (ComPortRelatedComChannel comChannel) {
        comChannel.write(FIRST_SERIES_OF_BYTES);
        byte[] secondSeriesOfBytes = new byte[SECOND_SERIES_OF_BYTES_LENGTH];
        System.arraycopy(SECOND_SERIES_OF_BYTES, SECOND_SERIES_OF_BYTES_OFFSET, secondSeriesOfBytes, 0, SECOND_SERIES_OF_BYTES_LENGTH);
        comChannel.write(secondSeriesOfBytes);
    }

    private ComPortRelatedComChannel newComChannelForWriting () {
        return this.newComChannelForWriting(ComServer.LogLevel.TRACE);
    }

    private ComPortRelatedComChannel newComChannelForWriting (ComServer.LogLevel comServerLogLevel) {
        ComPortRelatedComChannel comChannel = new ComPortRelatedComChannelImpl(new SystemOutComChannel());
        ComServer comServer = mock(ComServer.class);
        when(comServer.getServerLogLevel()).thenReturn(comServerLogLevel);
        when(comServer.getCommunicationLogLevel()).thenReturn(comServerLogLevel);
        ComPort comPort = mock(ComPort.class);
        when(comPort.getComServer()).thenReturn(comServer);
        this.jobExecution = new MockJobExecution(comPort, comChannel, serviceProvider);
        this.jobExecution.getExecutionContext().connect();   // Should initialize the ComChannelReadWriteLogger
        comChannel.setComPort(this.comPort);
        return comChannel;
    }

    private class MockJobExecution extends JobExecution {
        private ComPortRelatedComChannel comChannel;

        private MockJobExecution (ComPort comPort, ComPortRelatedComChannel comChannel, ServiceProvider serviceProvider) {
            super(comPort, mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), serviceProvider);
            this.comChannel = comChannel;
            ConnectionTask connectionTask = mock(ConnectionTask.class);
            ComPortPool comPortPool = mock(ComPortPool.class);
            when(comPortPool.getId()).thenReturn((long)COMPORT_POOL_ID);
            when(connectionTask.getComPortPool()).thenReturn(comPortPool);
            this.setExecutionContext(new ExecutionContext(this, connectionTask, comPort, new CommandRootServiceProviderAdapter(serviceProvider)));
        }

        @Override
        protected ComPortRelatedComChannel findOrCreateComChannel () throws ConnectionException {
            return this.comChannel;
        }

        @Override
        public List<ComTaskExecution> getComTaskExecutions () {
            return new ArrayList<>(0);
        }

        @Override
        protected boolean isConnected () {
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
        public boolean isWithinComWindow () {
            return true;
        }

        @Override
        public void rescheduleToNextComWindow () {
        }

        @Override
        public void execute() {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

}