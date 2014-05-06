package com.energyict.mdc.engine.impl.core;

import com.energyict.protocols.mdc.channels.AbstractComChannel;
import com.energyict.mdc.protocol.ComPortRelatedComChannel;

import java.io.IOException;

/**
 * Provides an implementation for the {@link ComPortRelatedComChannel} interface
 * that can be configured to return bytes while reading from it.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-13 (18:08)
 */
public class ConfigurableReadComChannel extends AbstractComChannel {

    private int singleByte;
    private byte[] byteBuffer;
    private byte[] byteBufferWithOffset;

    public void whenRead (int returnValue) {
        this.singleByte = returnValue;
    }

    @Override
    public int doRead () {
        return this.singleByte;
    }

    public void whenReadFromBuffer (byte[] bytes) {
        this.byteBuffer = bytes;
    }

    @Override
    public int doRead (byte[] buffer) {
        int numberOfBytesRead = Math.min(buffer.length, this.byteBuffer.length);
        System.arraycopy(this.byteBuffer, 0, buffer, 0, numberOfBytesRead);
        return numberOfBytesRead;
    }

    public void whenReadFromBufferWithOffset (byte[] bytes, int offset, int length) {
        this.byteBufferWithOffset = bytes;
    }

    @Override
    public int doRead (byte[] buffer, int offset, int length) {
        int numberOfBytesToRead = Math.min(length, this.byteBufferWithOffset.length);
        System.arraycopy(this.byteBufferWithOffset, offset, buffer, offset, numberOfBytesToRead);
        return numberOfBytesToRead;
    }

    @Override
    public boolean doStartReading () {
        return true;
    }

    @Override
    public int available () {
        return 0;
    }

    @Override
    public boolean doStartWriting () {
        return true;
    }

    @Override
    public int doWrite (int b) {
        return 1;
    }

    @Override
    public int doWrite (byte[] bytes) {
        return bytes.length;
    }

    @Override
    public void doClose () {
        // nothing to close
    }

    @Override
    public void doFlush() throws IOException {
        // nothing to flush
    }

}