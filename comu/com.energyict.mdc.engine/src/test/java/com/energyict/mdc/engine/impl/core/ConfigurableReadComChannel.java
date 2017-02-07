/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.ComChannelType;

/**
 * Provides an implementation for the {@link ComChannel} interface
 * that can be configured to return bytes while reading from it.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-13 (18:08)
 */
public class ConfigurableReadComChannel implements ComChannel {

    private int singleByte;
    private byte[] byteBuffer;
    private byte[] byteBufferWithOffset;
    private TypedProperties connectionTaskProperties = TypedProperties.empty();

    public void whenRead (int returnValue) {
        this.singleByte = returnValue;
    }

    public void whenReadFromBuffer (byte[] bytes) {
        this.byteBuffer = bytes;
    }

    public void whenReadFromBufferWithOffset (byte[] bytes, int offset, int length) {
        this.byteBufferWithOffset = bytes;
    }

    @Override
    public int available () {
        return 0;
    }

    @Override
    public final void close () {
        // nothing to close
    }

    @Override
    public final void flush () {
        // nothing to flush
    }

    @Override
    public final boolean startReading () {
        return true;
    }

    @Override
    public final int read () {
        return this.singleByte;
    }

    @Override
    public final int read (byte[] buffer) {
        int numberOfBytesRead = Math.min(buffer.length, this.byteBuffer.length);
        System.arraycopy(this.byteBuffer, 0, buffer, 0, numberOfBytesRead);
        return numberOfBytesRead;
    }

    @Override
    public final int read (byte[] buffer, int offset, int length) {
        int numberOfBytesToRead = Math.min(length, this.byteBufferWithOffset.length);
        System.arraycopy(this.byteBufferWithOffset, offset, buffer, offset, numberOfBytesToRead);
        return numberOfBytesToRead;
    }

    @Override
    public final boolean startWriting () {
        return true;
    }

    @Override
    public final int write (int b) {
        return 1;
    }

    @Override
    public final int write (byte[] bytes) {
        return bytes.length;
    }

    @Override
    public TypedProperties getProperties() {
        return this.connectionTaskProperties;
    }

    @Override
    public ComChannelType getComChannelType() {
        return ComChannelType.NOT_DEFINED;
    }

    @Override
    public void addProperties(TypedProperties typedProperties) {
        this.connectionTaskProperties.setAllProperties(typedProperties);
    }
}