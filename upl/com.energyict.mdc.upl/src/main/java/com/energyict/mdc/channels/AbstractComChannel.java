package com.energyict.mdc.channels;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.exceptions.ConnectionCommunicationException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides an implementation for the {@link ComChannel} interface
 * with template methods for every ComChannel method.
 * By design, all of the template methods are final and will have an
 * abstract doX version that will need to be implemented by subclasses.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-06-06 (10:49)
 */
public abstract class AbstractComChannel implements ComChannel {

    private TypedProperties connectionTaskProperties;
    private AtomicBoolean disconnecting = new AtomicBoolean(false);

    @Override
    public final void close() {
        // Note we don't do #checkIfThreadIsNotInterrupted, because the close operation should always be allowed
        this.doClose();
    }

    protected abstract void doClose();

    @Override
    public final void flush() throws IOException {
        checkIfCommunicationShouldBeAborted();
        this.doFlush();
    }

    protected abstract void doFlush() throws IOException;

    @Override
    public int available() {
        checkIfCommunicationShouldBeAborted();
        return this.doAvailable();
    }

    protected abstract int doAvailable();

    @Override
    public final boolean startReading() {
        checkIfCommunicationShouldBeAborted();
        return this.doStartReading();
    }

    protected abstract boolean doStartReading();

    @Override
    public final int read() {
        checkIfCommunicationShouldBeAborted();
        return this.doRead();
    }

    protected abstract int doRead();

    @Override
    public final int read(byte[] buffer) {
        checkIfCommunicationShouldBeAborted();
        return this.doRead(buffer);
    }

    protected abstract int doRead(byte[] buffer);

    @Override
    public final int read(byte[] buffer, int offset, int length) {
        checkIfCommunicationShouldBeAborted();
        return this.doRead(buffer, offset, length);
    }

    protected abstract int doRead(byte[] buffer, int offset, int length);

    @Override
    public final boolean startWriting() {
        checkIfCommunicationShouldBeAborted();
        return this.doStartWriting();
    }

    protected abstract boolean doStartWriting();

    @Override
    public final int write(int b) {
        checkIfCommunicationShouldBeAborted();
        return this.doWrite(b);
    }

    protected abstract int doWrite(int b);

    @Override
    public final int write(byte[] bytes) {
        checkIfCommunicationShouldBeAborted();
        return this.doWrite(bytes);
    }

    protected abstract int doWrite(byte[] bytes);

    @Override
    public TypedProperties getProperties() {
        return this.connectionTaskProperties;
    }

    @Override
    public void addProperties(TypedProperties typedProperties) {
        this.connectionTaskProperties.setAllProperties(typedProperties);
    }

    /**
     * Check if the communication should be aborted or not. <br/>
     * If the communication should be aborted, then a {@link ConnectionCommunicationException#communicationAbortedByUserException} will be thrown;
     * else the execution can continue.
     */
    private void checkIfCommunicationShouldBeAborted() {
        // If we are disconnecting, then we want communication to continue (or in other words: ALWAYS do the disconnect)
        if (!getDisconnectingAtomicBoolean().get()) {
            if (ComChannel.abortCommunication.get()) {
                throw ConnectionCommunicationException.communicationAbortedByUserException();
            } else if (Thread.currentThread().isInterrupted()) {
                throw ConnectionCommunicationException.communicationInterruptedException();
            }
        }
    }

    @Override
    public void prepareForDisConnect() {
        disconnecting.set(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeout(long millis) {
        //Does nothing by default. Subclasses can override.
    }

    public AtomicBoolean getDisconnectingAtomicBoolean() {
        return disconnecting;
    }

    @Override
    public boolean isVoid() {
        return false;
    }
}