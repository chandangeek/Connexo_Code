package com.energyict.mdc.engine.impl.core.aspects.events;

import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.events.io.ReadEvent;
import com.energyict.mdc.engine.impl.events.io.WriteEvent;
import com.energyict.mdc.engine.impl.core.inbound.ComPortRelatedComChannel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Defines pointcuts and advice that will emit
 * {@link ReadEvent}s and {@link WriteEvent}s
 * for read and write sessions on a {@link ComPortRelatedComChannel}.
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
 * @since 2012-11-12 (12:00)
 */
public aspect ComChannelReadWriteEventPublisher {

    private ByteArrayOutputStream ComPortRelatedComChannel.bytesReadForPublishing;
    private ByteArrayOutputStream ComPortRelatedComChannel.bytesWrittenForPublishing;

    private pointcut startReading (ComPortRelatedComChannel comChannel):
            execution(boolean ComPortRelatedComChannel.startReading())
                    && target(comChannel);

    after (ComPortRelatedComChannel comChannel) returning (boolean changed): startReading(comChannel) {
        if (changed) {
            this.logBytesWrittenIfAny(comChannel);
            comChannel.bytesReadForPublishing = null;
        }
    }

    private pointcut startWriting (ComPortRelatedComChannel comChannel):
            execution(boolean ComPortRelatedComChannel.startWriting())
                    && target(comChannel);

    after (ComPortRelatedComChannel comChannel) returning (boolean changed) : startWriting(comChannel) {
        if (changed) {
            this.logBytesReadIfAny(comChannel);
            comChannel.bytesWrittenForPublishing = null;
        }
    }

    private pointcut close (ComPortRelatedComChannel comChannel):
            execution(void ComPortRelatedComChannel.close())
                    && target(comChannel);

    after (ComPortRelatedComChannel comChannel): close(comChannel) {
        this.logBytesReadIfAny(comChannel);
        this.logBytesWrittenIfAny(comChannel);
    }

    private pointcut readSingleByte (ComPortRelatedComChannel comChannel):
            execution(int ComPortRelatedComChannel+.read())
                    && target(comChannel);

    after (ComPortRelatedComChannel comChannel) returning (int byteRead) : readSingleByte(comChannel) {
        if (byteRead != -1) {
            this.startReading(comChannel);
            comChannel.bytesReadForPublishing.write(byteRead);
        }
    }

    private pointcut readBytes (ComPortRelatedComChannel comChannel, byte[] bytes):
            execution(int ComPortRelatedComChannel+.read(byte[]))
                    && target(comChannel)
                    && args(bytes);

    after (ComPortRelatedComChannel comChannel, byte[] bytes) returning (int numberOfBytesRead) : readBytes(comChannel, bytes) {
        if (numberOfBytesRead != -1) {
            this.startReading(comChannel);
            this.safeWriteTo(bytes, comChannel.bytesReadForPublishing);
        }
    }

    private pointcut readBytesWithOffset (ComPortRelatedComChannel comChannel, byte[] bytes, int offset, int length):
            execution(int ComPortRelatedComChannel+.read(byte[], int, int))
                    && target(comChannel)
                    && args(bytes, offset, length);

    after (ComPortRelatedComChannel comChannel, byte[] bytes, int offset, int length) returning (int numberOfBytesRead) : readBytesWithOffset(comChannel, bytes, offset, length) {
        if (numberOfBytesRead != -1) {
            this.startReading(comChannel);
            this.safeWriteTo(bytes, offset, length, comChannel.bytesReadForPublishing);
        }
    }

    private pointcut writeSingleByte (ComPortRelatedComChannel comChannel, int singleByte):
            execution(int ComPortRelatedComChannel.write(int))
                    && target(comChannel)
                    && args(singleByte);

    after (ComPortRelatedComChannel comChannel, int singleByte) returning (int numberOfBytesWritten) : writeSingleByte(comChannel, singleByte) {
        if (numberOfBytesWritten != -1) {
            this.startWriting(comChannel);
            comChannel.bytesWrittenForPublishing.write(singleByte);
        }
    }

    private pointcut writeBytes (ComPortRelatedComChannel comChannel, byte[] bytes):
            execution(int ComPortRelatedComChannel.write(byte[]))
                    && target(comChannel)
                    && args(bytes);

    after (ComPortRelatedComChannel comChannel, byte[] bytes) returning (int numberOfBytesWritten) : writeBytes(comChannel, bytes) {
        if (numberOfBytesWritten != -1) {
            this.startWriting(comChannel);
            this.safeWriteTo(bytes, comChannel.bytesWrittenForPublishing);
        }
    }

    private void startReading (ComPortRelatedComChannel comChannel) {
        if (comChannel.bytesReadForPublishing == null) {
            comChannel.bytesReadForPublishing = new ByteArrayOutputStream();
        }
    }

    private void startWriting (ComPortRelatedComChannel comChannel) {
        if (comChannel.bytesWrittenForPublishing == null) {
            comChannel.bytesWrittenForPublishing = new ByteArrayOutputStream();
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
        if (comChannel.bytesReadForPublishing != null) {
            this.logBytesRead(comChannel, comChannel.bytesReadForPublishing);
            comChannel.bytesReadForPublishing = null;
        }
    }

    private void logBytesWrittenIfAny (ComPortRelatedComChannel comChannel) {
        if (comChannel.bytesWrittenForPublishing != null) {
            this.logBytesWritten(comChannel, comChannel.bytesWrittenForPublishing);
            comChannel.bytesWrittenForPublishing = null;
        }
    }

    private void logBytesWritten (ComPortRelatedComChannel comChannel, ByteArrayOutputStream bytes) {
        this.publish(new WriteEvent(comChannel.getComPort(), bytes.toByteArray()));
    }

    private void logBytesRead (ComPortRelatedComChannel comChannel, ByteArrayOutputStream bytes) {
        this.publish(new ReadEvent(comChannel.getComPort(), bytes.toByteArray()));
    }

    private void publish (ComServerEvent event) {
        EventPublisherImpl.getInstance().publish(event);
    }

}