package com.energyict.mdc.engine.impl.core.aspects.logging;

import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.logging.LogLevelMapper;
import com.energyict.mdc.engine.impl.logging.LoggerFactory;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.protocols.mdc.channels.AbstractComChannel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Defines pointcuts and advice that will log the actual bytes
 * of read and write sessions on a {@link AbstractComChannel}s.
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

    private ByteArrayOutputStream AbstractComChannel.bytesReadForLogging;
    private ByteArrayOutputStream AbstractComChannel.bytesWrittenForLogging;
    private JobExecution.ExecutionContext AbstractComChannel.executionContext;

    private pointcut findOrCreateComChannel (JobExecution jobExecution):
            call(ServerComChannel findOrCreateComChannel())
         && target(jobExecution);

    after (JobExecution jobExecution) returning (ComPortRelatedComChannel serverComChannel): findOrCreateComChannel(jobExecution) {
        AbstractComChannel comChannel = (AbstractComChannel) serverComChannel;
        if (comChannel != null) {
            JobExecution.ExecutionContext executionContext = jobExecution.getExecutionContext();
            executionContext.setComChannelLogger(this.newComChannelLogger(executionContext));
            comChannel.executionContext = executionContext;
        }
    }

    private pointcut startReading (AbstractComChannel comChannel):
            execution(boolean AbstractComChannel.startReading())
                    && target(comChannel);

    after (AbstractComChannel comChannel) returning (boolean changed) : startReading(comChannel) {
        if (changed) {
            this.logBytesWrittenIfAny(comChannel);
            comChannel.bytesReadForLogging = null;
        }
    }

    private pointcut startWriting (AbstractComChannel comChannel):
            execution(boolean AbstractComChannel.startWriting())
         && target(comChannel);

    after (AbstractComChannel comChannel) returning (boolean changed) : startWriting(comChannel) {
        if (changed) {
            this.logBytesReadIfAny(comChannel);
            comChannel.bytesWrittenForLogging = null;
        }
    }

    private pointcut close (AbstractComChannel comChannel):
            execution(void AbstractComChannel.close())
         && target(comChannel);

    after (AbstractComChannel comChannel): close(comChannel) {
        this.logRemainingBytesOnClose(comChannel);
    }

    private void logRemainingBytesOnClose (AbstractComChannel comChannel) {
        this.logBytesReadIfAny(comChannel);
        this.logBytesWrittenIfAny(comChannel);
    }

    private void logRemainingBytesOnClose (JobExecution jobExecution) {
        ComPortRelatedComChannel comChannel = jobExecution.getExecutionContext().getComChannel();
        if (comChannel != null && comChannel instanceof AbstractComChannel) {
            this.logRemainingBytesOnClose((AbstractComChannel) comChannel);
        }
    }

    private pointcut readSingleByte (AbstractComChannel comChannel):
            execution(int AbstractComChannel+.read())
                    && target(comChannel);

    after (AbstractComChannel comChannel) returning (int byteRead) : readSingleByte(comChannel) {
        if (byteRead != -1) {
            this.startReading(comChannel);
            comChannel.bytesReadForLogging.write(byteRead);
        }
    }

    private pointcut readBytes (AbstractComChannel comChannel, byte[] bytes):
            execution(int AbstractComChannel+.read(byte[]))
                    && target(comChannel)
                    && args(bytes);

    after (AbstractComChannel comChannel, byte[] bytes) returning (int numberOfBytesRead) : readBytes(comChannel, bytes) {
        if (numberOfBytesRead != -1) {
            this.startReading(comChannel);
            this.safeWriteTo(bytes, comChannel.bytesReadForLogging);
        }
    }

    private pointcut readBytesWithOffset (AbstractComChannel comChannel, byte[] bytes, int offset, int length):
            execution(int AbstractComChannel+.read(byte[], int, int))
                    && target(comChannel)
                    && args(bytes, offset, length);

    after (AbstractComChannel comChannel, byte[] bytes, int offset, int length) returning (int numberOfBytesRead) : readBytesWithOffset(comChannel, bytes, offset, length) {
        if (numberOfBytesRead != -1) {
            this.startReading(comChannel);
            this.safeWriteTo(bytes, offset, length, comChannel.bytesReadForLogging);
        }
    }

    private pointcut writeSingleByte (AbstractComChannel comChannel, int singleByte):
            execution(int AbstractComChannel.write(int))
                    && target(comChannel)
                    && args(singleByte);

    after (AbstractComChannel comChannel, int singleByte) returning (int numberOfBytesWritten) : writeSingleByte(comChannel, singleByte) {
        if (numberOfBytesWritten != -1) {
            this.startWriting(comChannel);
            comChannel.bytesWrittenForLogging.write(singleByte);
        }
    }

    private pointcut writeBytes (AbstractComChannel comChannel, byte[] bytes):
            execution(int AbstractComChannel.write(byte[]))
                    && target(comChannel)
                    && args(bytes);

    after (AbstractComChannel comChannel, byte[] bytes) returning (int numberOfBytesWritten) : writeBytes(comChannel, bytes) {
        if (numberOfBytesWritten != -1) {
            this.startWriting(comChannel);
            this.safeWriteTo(bytes, comChannel.bytesWrittenForLogging);
        }
    }

    private void startReading (AbstractComChannel comChannel) {
        if (comChannel.bytesReadForLogging == null) {
            comChannel.bytesReadForLogging = new ByteArrayOutputStream();
        }
    }

    private void startWriting (AbstractComChannel comChannel) {
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

    private void logBytesReadIfAny (AbstractComChannel comChannel) {
        if (comChannel.bytesReadForLogging != null && comChannel.executionContext != null) {
            this.logBytesRead(comChannel, comChannel.bytesReadForLogging);
            comChannel.bytesReadForLogging = null;
        }
    }

    private void logBytesWrittenIfAny (AbstractComChannel comChannel) {
        if (comChannel.bytesWrittenForLogging != null && comChannel.executionContext != null) {
            this.logBytesWritten(comChannel, comChannel.bytesWrittenForLogging);
            comChannel.bytesWrittenForLogging = null;
        }
    }

    private void logBytesWritten (AbstractComChannel comChannel, ByteArrayOutputStream bytes) {
        String hexBytes = ServiceProvider.instance.get().hexService().toHexString(bytes.toByteArray());
        comChannel.executionContext.getComChannelLogger().bytesWritten(hexBytes);
    }

    private void logBytesRead (AbstractComChannel comChannel, ByteArrayOutputStream bytes) {
        String hexBytes = ServiceProvider.instance.get().hexService().toHexString(bytes.toByteArray());
        comChannel.executionContext.getComChannelLogger().bytesRead(hexBytes);
    }

    private ComChannelLogger newComChannelLogger (JobExecution.ExecutionContext executionContext) {
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

    private LogLevel getServerLogLevel (JobExecution.ExecutionContext executionContext) {
        return this.getServerLogLevel(executionContext.getComPort());
    }

    private LogLevel getServerLogLevel (ComPort comPort) {
        return this.getServerLogLevel(comPort.getComServer());
    }

    private LogLevel getServerLogLevel (ComServer comServer) {
        return LogLevelMapper.map(comServer.getServerLogLevel());
    }

}