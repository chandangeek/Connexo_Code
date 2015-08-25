package com.energyict.mdc.io.impl;

import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.ConnectionCommunicationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides an implementation of the {@link ComChannel} interface
 * that uses synchronous communication, i.e. data is always written first
 * and then data can be read. Reading and writing at the same time is not permitted.
 * The latter will throw a {@link AsynchronousCommunicationIsNotSupportedException}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-15 (10:11)
 */
public abstract class SynchronousComChannel extends AbstractComChannel {

    private AtomicBoolean reading;
    private InputStream in;
    private OutputStream out;

    /**
     * Creates a new SynchronousComChannel that uses the specified
     * InputStream and OutputStream as underlying communication mechanisms.
     * The ComChannel is open for writing.
     *
     * @param in The InputStream
     * @param out The OutputStream
     */
    public SynchronousComChannel(InputStream in, OutputStream out) {
        super();
        this.in = in;
        this.out = out;
        this.reading = new AtomicBoolean(false);
    }

    @Override
    public boolean doStartReading () {
        boolean previousValue = this.reading.getAndSet(true);
        try {
            this.out.flush();
        }
        catch (IOException e) {
            throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
        return !previousValue;  // Status was changed if previousValue != true, i.e. if it false
    }

    private void checkNotWriting () {
        if (!this.reading.get()) {
            throw new AsynchronousCommunicationIsNotSupportedException();
        }
    }

    /**
     * Executes a read operation, checking first that reading is
     * permitted and wrapping all IOExceptions in a {@link CommunicationException}.
     *
     * @param operation The ReadOperation
     * @return The value returned by the ReadOperation
     */
    private int executeReadOperation (ReadOperation operation) {
        this.checkNotWriting();
        try {
            return operation.doRead();
        }
        catch (IOException e) {
            throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }

    @Override
    public int doRead () {
        return this.executeReadOperation(in::read);
    }

    @Override
    public int doRead (final byte[] buffer) {
        return this.executeReadOperation(() -> in.read(buffer));
    }

    @Override
    public int doRead (final byte[] buffer, final int offset, final int length) {
        return this.executeReadOperation(() -> in.read(buffer, offset, length));
    }

    @Override
    public int available () {
        return this.executeReadOperation(in::available);
    }

    @Override
    public boolean doStartWriting () {
        return this.reading.getAndSet(false);   // Status changed if the old value != false, i.e. if it is true
    }

    private void checkNotReading () {
        if (this.reading.get()) {
            throw new AsynchronousCommunicationIsNotSupportedException();
        }
    }

    /**
     * Executes a write operation, checking first that writing is
     * permitted and wrapping all IOExceptions in a {@link CommunicationException}.
     *
     * @param operation The WriteOperation
     */
    private void executeWriteOperation (WriteOperation operation) {
        this.checkNotReading();
        try {
            operation.doWrite();
        }
        catch (IOException e) {
            throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }

    @Override
    public int doWrite (final int b) {
        this.executeWriteOperation(() -> out.write(b));
        return 1;
    }

    @Override
    public int doWrite (final byte[] bytes) {
        this.executeWriteOperation(() -> out.write(bytes));
        return bytes.length;
    }

    @Override
    protected void doClose () {
        try {
            this.in.close();
            this.out.close();
        }
        catch (IOException e) {
            throw new ConnectionCommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }

    @Override
    public void doFlush() throws IOException {
        this.out.flush();
    }

    private interface ReadOperation {
        public int doRead () throws IOException;
    }

    private interface WriteOperation {
        public void doWrite () throws IOException;
    }

}