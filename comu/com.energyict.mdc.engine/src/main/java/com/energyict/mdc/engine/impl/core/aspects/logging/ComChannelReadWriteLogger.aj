package com.energyict.mdc.engine.impl.core.aspects.logging;

import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.ComChannel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Defines pointcuts and advice that will log the actual bytes
 * of read and write sessions on {@link ComChannel}s.
 * A read session is defined as a series of read method invocations
 * marked by:
 * <ul>
 * <li>the creation of the ComChannel and a startWriting method</li>
 * <li>the creation of the ComChannel and a close method</li>
 * <li>a startReading and a startWriting method</li>
 * <li>a startReading and a close method</li>
 * </ul>
 * A write session is defined as a series of write method invocations
 * marked by:
 * <ul>
 * <li>the creation of the ComChannel and a startReading method</li>
 * <li>the creation of the ComChannel and a close method</li>
 * <li>a startWriting and a startReading method</li>
 * <li>a startWriting and a close method</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-14 (09:45)
 */
public aspect ComChannelReadWriteLogger {

    private ByteArrayOutputStream ComPortRelatedComChannel.bytesReadForLogging;
    private ByteArrayOutputStream ComPortRelatedComChannel.bytesWrittenForLogging;
    private ExecutionContext ComPortRelatedComChannel.executionContext;

    private pointcut findOrCreateComChannel (JobExecution jobExecution):
            call(ComPortRelatedComChannel findOrCreateComChannel())
         && target(jobExecution);

    after (JobExecution jobExecution) returning (ComPortRelatedComChannel comChannel): findOrCreateComChannel(jobExecution) {
        if (comChannel != null) {
            ExecutionContext executionContext = jobExecution.getExecutionContext();
            executionContext.setComChannelLogger(this.newComChannelLogger(executionContext));
            comChannel.executionContext = executionContext;
        }
    }

    private pointcut startReading (ComPortRelatedComChannel comChannel):
            execution(boolean ComPortRelatedComChannel.startReading())
         && target(comChannel);

    after (ComPortRelatedComChannel comChannel) returning (boolean changed) : startReading(comChannel) {
        if (changed) {
            this.logBytesWrittenIfAny(comChannel);
            comChannel.bytesReadForLogging = null;
        }
    }

    private pointcut startWriting (ComPortRelatedComChannel comChannel):
            execution(boolean ComPortRelatedComChannel.startWriting())
         && target(comChannel);

    after (ComPortRelatedComChannel comChannel) returning (boolean changed) : startWriting(comChannel) {
        if (changed) {
            this.logBytesReadIfAny(comChannel);
            comChannel.bytesWrittenForLogging = null;
        }
    }

    private pointcut close (ComPortRelatedComChannel comChannel):
            execution(void ComPortRelatedComChannel.close())
         && target(comChannel);

    after (ComPortRelatedComChannel comChannel): close(comChannel) {
        this.logRemainingBytesOnClose(comChannel);
    }

    private void logRemainingBytesOnClose (ComPortRelatedComChannel comChannel) {
        this.logBytesReadIfAny(comChannel);
        this.logBytesWrittenIfAny(comChannel);
    }

    private void logRemainingBytesOnClose (JobExecution jobExecution) {
        ComPortRelatedComChannel comChannel = jobExecution.getExecutionContext().getComChannel();
        if (comChannel != null) {
            this.logRemainingBytesOnClose(comChannel);
        }
    }

    private pointcut readSingleByte (ComPortRelatedComChannel comChannel):
            execution(int ComPortRelatedComChannel+.read())
         && target(comChannel);

    after (ComPortRelatedComChannel comChannel) returning (int byteRead) : readSingleByte(comChannel) {
        if (byteRead != -1) {
            this.startReading(comChannel);
            comChannel.bytesReadForLogging.write(byteRead);
        }
    }

    private pointcut readBytes (ComPortRelatedComChannel comChannel, byte[] bytes):
            execution(int ComPortRelatedComChannel+.read(byte[]))
         && target(comChannel)
         && args(bytes);

    after (ComPortRelatedComChannel comChannel, byte[] bytes) returning (int numberOfBytesRead) : readBytes(comChannel, bytes) {
        if (numberOfBytesRead != -1) {
            this.startReading(comChannel);
            this.safeWriteTo(bytes, comChannel.bytesReadForLogging);
        }
    }

    private pointcut readBytesWithOffset (ComPortRelatedComChannel comChannel, byte[] bytes, int offset, int length):
            execution(int ComPortRelatedComChannel+.read(byte[], int, int))
         && target(comChannel)
         && args(bytes, offset, length);

    after (ComPortRelatedComChannel comChannel, byte[] bytes, int offset, int length) returning (int numberOfBytesRead) : readBytesWithOffset(comChannel, bytes, offset, length) {
        if (numberOfBytesRead != -1) {
            this.startReading(comChannel);
            this.safeWriteTo(bytes, offset, length, comChannel.bytesReadForLogging);
        }
    }

    private pointcut writeSingleByte (ComPortRelatedComChannel comChannel, int singleByte):
            execution(int ComPortRelatedComChannel.write(int))
         && target(comChannel)
         && args(singleByte);

    after (ComPortRelatedComChannel comChannel, int singleByte) returning (int numberOfBytesWritten) : writeSingleByte(comChannel, singleByte) {
        if (numberOfBytesWritten != -1) {
            this.startWriting(comChannel);
            comChannel.bytesWrittenForLogging.write(singleByte);
        }
    }

    private pointcut writeBytes (ComPortRelatedComChannel comChannel, byte[] bytes):
            execution(int ComPortRelatedComChannel.write(byte[]))
         && target(comChannel)
         && args(bytes);

    after (ComPortRelatedComChannel comChannel, byte[] bytes) returning (int numberOfBytesWritten) : writeBytes(comChannel, bytes) {
        if (numberOfBytesWritten != -1) {
            this.startWriting(comChannel);
            this.safeWriteTo(bytes, comChannel.bytesWrittenForLogging);
        }
    }

    private void startReading (ComPortRelatedComChannel comChannel) {
        if (comChannel.bytesReadForLogging == null) {
            comChannel.bytesReadForLogging = new ByteArrayOutputStream();
        }
    }

    private void startWriting (ComPortRelatedComChannel comChannel) {
        if (comChannel.bytesWrittenForLogging == null) {
            comChannel.bytesWrittenForLogging = new ByteArrayOutputStream();
        }
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

    private void logBytesReadIfAny (ComPortRelatedComChannel comChannel) {
        if (comChannel.bytesReadForLogging != null && comChannel.executionContext != null) {
            this.logBytesRead(comChannel, comChannel.bytesReadForLogging);
            comChannel.bytesReadForLogging = null;
        }
    }

    private void logBytesWrittenIfAny (ComPortRelatedComChannel comChannel) {
        if (comChannel.bytesWrittenForLogging != null && comChannel.executionContext != null) {
            this.logBytesWritten(comChannel, comChannel.bytesWrittenForLogging);
            comChannel.bytesWrittenForLogging = null;
        }
    }

    private void logBytesWritten (ComPortRelatedComChannel comChannel, ByteArrayOutputStream bytes) {
        String hexBytes = ServiceProvider.instance.get().hexService().toHexString(bytes.toByteArray());
        comChannel.executionContext.getComChannelLogger().bytesWritten(hexBytes);
    }

    private void logBytesRead (ComPortRelatedComChannel comChannel, ByteArrayOutputStream bytes) {
        String hexBytes = ServiceProvider.instance.get().hexService().toHexString(bytes.toByteArray());
        comChannel.executionContext.getComChannelLogger().bytesRead(hexBytes);
    }

    private ComChannelLogger newComChannelLogger (ExecutionContext executionContext) {
        ComChannelLogger logger = LoggerFactory.getUniqueLoggerFor(ComChannelLogger.class, this.getServerLogLevel(executionContext));
        LoggerFactory.LoggerHolder loggerHolder = (LoggerFactory.LoggerHolder) logger;
        loggerHolder.getLogger().addHandler(new ComChannelLogHandler(executionContext));
        return logger;
    }

    private pointcut comTaskExecutionCompletes (JobExecution scheduledJob, ComTaskExecution comTaskExecution):
            execution(void JobExecution.completeExecutedComTask(ComTaskExecution))
         && target(scheduledJob)
         && args(comTaskExecution);

    before (JobExecution jobExecution, ComTaskExecution comTaskExecution): comTaskExecutionCompletes(jobExecution, comTaskExecution) {
        this.logRemainingBytesOnClose(jobExecution);
    }

    private pointcut comTaskExecutionFails (JobExecution scheduledJob, ComTaskExecution comTaskExecution, Throwable t):
            execution(void JobExecution.failure(ComTaskExecution, java.lang.Throwable))
         && target(scheduledJob)
         && args(comTaskExecution, t);

    before (JobExecution jobExecution, ComTaskExecution comTaskExecution, Throwable t): comTaskExecutionFails(jobExecution, comTaskExecution, t) {
        this.logRemainingBytesOnClose(jobExecution);
    }

    private LogLevel getServerLogLevel (ExecutionContext executionContext) {
        return this.getServerLogLevel(executionContext.getComPort());
    }

    private LogLevel getServerLogLevel (ComPort comPort) {
        return this.getServerLogLevel(comPort.getComServer());
    }

    private LogLevel getServerLogLevel (ComServer comServer) {
        return LogLevelMapper.map(comServer.getServerLogLevel());
    }

}