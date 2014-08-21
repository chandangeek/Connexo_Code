package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.engine.impl.core.aspects.logging.ComChannelLogger;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.services.HexService;

import com.elster.jupiter.util.time.StopWatch;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Provides an implementation for the {@link ComPortRelatedComChannel} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-24 (13:44)
 */
public class ComPortRelatedComChannelImpl  implements ComPortRelatedComChannel {

    private static final long NANOS_IN_MILLI = 1000000L;

    private final HexService hexService;
    private ComChannel comChannel;
    private ComChannelLogger logger;
    private ComPort comPort;
    private ByteArrayOutputStream bytesReadForLogging;
    private ByteArrayOutputStream bytesWrittenForLogging;
    private StopWatch talking;
    private Counters sessionCounters = new Counters();
    private Counters taskSessionCounters = new Counters();

    public ComPortRelatedComChannelImpl(ComChannel comChannel, HexService hexService) {
        super();
        this.comChannel = comChannel;
        this.hexService = hexService;
        this.talking = new StopWatch(false);  // No cpu required;
        this.talking.stop();
    }

    public ComPortRelatedComChannelImpl(ComChannel comChannel, ComPort comPort, HexService hexService) {
        this(comChannel, hexService);
        this.setComPort(comPort);
    }

    @Override
    public ComPort getComPort() {
        return comPort;
    }

    @Override
    public void setComPort(ComPort comPort) {
        this.comPort = comPort;
    }

    public void setJournalEntryFactory(JournalEntryFactory journalEntryFactory) {
        this.logger = LoggerFactory.getUniqueLoggerFor(ComChannelLogger.class, this.getServerLogLevel());
        LoggerFactory.LoggerHolder loggerHolder = (LoggerFactory.LoggerHolder) this.logger;
        loggerHolder.getLogger().addHandler(new ComChannelLogHandler(journalEntryFactory));
    }

    @Override
    public ComChannel getActualComChannel() {
        return comChannel;
    }

    private LogLevel getServerLogLevel() {
        return this.getServerLogLevel(this.comPort);
    }

    private LogLevel getServerLogLevel (ComPort comPort) {
        return this.getServerLogLevel(comPort.getComServer());
    }

    private LogLevel getServerLogLevel (ComServer comServer) {
        return LogLevelMapper.map(comServer.getServerLogLevel());
    }

    @Override
    public boolean startReading() {
        boolean changed = this.comChannel.startReading();
        if (changed) {
            this.logBytesWrittenIfAny();
            this.bytesReadForLogging = null;
        }
        return changed;
    }

    @Override
    public int read() {
        this.talking.start();
        int byteRead = comChannel.read();
        if (byteRead != -1) {
            this.ensureBytesReadForLogging();
            this.bytesReadForLogging.write(byteRead);
        }
        return this.afterReading(byteRead);
    }

    @Override
    public int read(byte[] buffer) {
        this.talking.start();
        int numberOfBytesRead = comChannel.read(buffer);
        if (numberOfBytesRead != -1) {
            this.ensureBytesReadForLogging();
            this.safeWriteTo(buffer, this.bytesReadForLogging);
        }
        return this.afterReading(numberOfBytesRead);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        this.talking.start();
        int numberOfBytesRead = comChannel.read(buffer, offset, length);
        if (numberOfBytesRead != -1) {
            this.ensureBytesReadForLogging();
            this.safeWriteTo(buffer, offset, length, this.bytesReadForLogging);
        }
        return this.afterReading(numberOfBytesRead);
    }

    private void ensureBytesReadForLogging() {
        if (this.bytesReadForLogging == null) {
            this.bytesReadForLogging = new ByteArrayOutputStream();
        }
    }

    private int afterReading(int bytesRead) {
        this.talking.stop();
        Counters sessionCounters = this.sessionCounters;
        sessionCounters.bytesRead(bytesRead);
        if (!sessionCounters.isReading()) {
            sessionCounters.reading();
            sessionCounters.packetRead();
        }
        Counters taskSessionCounters = this.taskSessionCounters;
        taskSessionCounters.bytesRead(bytesRead);
        if (!taskSessionCounters.isReading()) {
            taskSessionCounters.reading();
            taskSessionCounters.packetRead();
        }
        return bytesRead;
    }

    private void safeWriteTo (byte[] bytes, ByteArrayOutputStream os) {
        try {
            os.write(bytes);
        }
        catch (IOException e) {
            // Should not occur since the ByteArrayOutputStream is in memory
            e.printStackTrace(System.err);
        }
    }

    private void safeWriteTo (byte[] bytes, int offset, int length, ByteArrayOutputStream os) {
        os.write(bytes, offset, length);
    }

    @Override
    public int available() {
        return comChannel.available();
    }

    @Override
    public boolean startWriting() {
        boolean changed = this.comChannel.startWriting();
        if (changed) {
            this.logBytesReadIfAny();
            this.bytesWrittenForLogging = null;
        }
        return changed;
    }

    @Override
    public int write(int b) {
        talking.start();
        int numberOfBytesWritten = comChannel.write(b);
        if (numberOfBytesWritten != -1) {
            this.ensureBytesWrittenForLogging();
            this.bytesWrittenForLogging.write(b);
        }
        return this.afterWriting(numberOfBytesWritten);
    }

    @Override
    public int write(byte[] bytes) {
        talking.start();
        int numberOfBytesWritten = comChannel.write(bytes);
        if (numberOfBytesWritten != -1) {
            this.ensureBytesWrittenForLogging();
            this.safeWriteTo(bytes, this.bytesWrittenForLogging);
        }
        return this.afterWriting(numberOfBytesWritten);
    }

    private void ensureBytesWrittenForLogging () {
        if (this.bytesWrittenForLogging == null) {
            this.bytesWrittenForLogging = new ByteArrayOutputStream();
        }
    }

    private int afterWriting (int numberOfBytesWritten) {
        talking.stop();
        Counters sessionCounters = this.sessionCounters;
        sessionCounters.bytesSent(numberOfBytesWritten);
        if (!sessionCounters.isWriting()) {
            sessionCounters.writing();
            sessionCounters.packetSent();
        }
        Counters taskSessionCounters = this.taskSessionCounters;
        taskSessionCounters.bytesSent(numberOfBytesWritten);
        if (!taskSessionCounters.isWriting()) {
            taskSessionCounters.writing();
            taskSessionCounters.packetSent();
        }
        return numberOfBytesWritten;
    }

    @Override
    public void close() {
        this.comChannel.close();
        this.talking.stop();
        this.logRemainingBytes();
    }

    @Override
    public void logRemainingBytes() {
        this.logBytesReadIfAny();
        this.logBytesWrittenIfAny();
    }

    @Override
    public void addProperties(TypedProperties typedProperties) {
        comChannel.addProperties(typedProperties);
    }

    @Override
    public TypedProperties getProperties() {
        return comChannel.getProperties();
    }

    @Override
    public void flush() throws IOException {
        comChannel.flush();
    }

    private void logBytesWrittenIfAny () {
        if (this.bytesWrittenForLogging != null && this.logger != null) {
            this.logBytesWrittenAndReset();
            this.bytesWrittenForLogging = null;
        }
    }

    private void logBytesWrittenAndReset() {
        String hexBytes = this.hexService.toHexString(this.bytesWrittenForLogging.toByteArray());
        this.logger.bytesWritten(hexBytes);
    }

    private void logBytesReadIfAny () {
        if (this.bytesReadForLogging != null && this.logger != null) {
            this.logBytesReadAndReset();
            this.bytesReadForLogging = null;
        }
    }

    private void logBytesReadAndReset() {
        String hexBytes = this.hexService.toHexString(this.bytesReadForLogging.toByteArray());
        this.logger.bytesRead(hexBytes);
    }

    @Override
    public Duration talkTime() {
        return Duration.millis(this.talking.getElapsed() / NANOS_IN_MILLI);
    }

    @Override
    public Counters getSessionCounters() {
        return this.sessionCounters;
    }

    @Override
    public Counters getTaskSessionCounters() {
        return this.taskSessionCounters;
    }

}