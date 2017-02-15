/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.messaging;

import com.elster.jupiter.orm.UnderlyingIOException;
import com.energyict.mdc.protocol.api.DeviceMessageFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Provides an implementation for the Consumer interface
 * that is required to read the contents of a
 * {@link com.energyict.mdc.protocol.api.DeviceMessageFile}.
 * This consumer will read the contents into a byte array.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-12 (13:07)
 */
public class DeviceMessageFileByteContentConsumer implements Consumer<InputStream> {
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private byte[] bytes;
    private final int bufferSize;

    public static byte[] readFrom(DeviceMessageFile deviceMessageFile) {
        DeviceMessageFileByteContentConsumer consumer = new DeviceMessageFileByteContentConsumer();
        deviceMessageFile.readWith(consumer);
        return consumer.getBytes();
    }

    byte[] getBytes() {
        return this.bytes;
    }

    DeviceMessageFileByteContentConsumer() {
        this(DEFAULT_BUFFER_SIZE);
    }

    DeviceMessageFileByteContentConsumer(int bufferSize) {
        super();
        this.bufferSize = bufferSize;
    }

    @Override
    public void accept(InputStream inputStream) {
        try {
            byte[] buffer = new byte[this.bufferSize];
            int bytesRead;
            boolean proceed = true;
            while (proceed) {
                bytesRead = inputStream.read(buffer, 0, this.bufferSize);
                proceed = bytesRead == this.bufferSize;
                if (bytesRead != -1) {
                    this.append(buffer, bytesRead);
                }
            }
        } catch (IOException e) {
            throw new UnderlyingIOException(e);
        }
    }

    private void append(byte[] buffer, int size) {
        if (this.bytes == null) {
            this.bytes = new byte[size];
            System.arraycopy(buffer, 0, this.bytes, 0, size);
        } else {
            byte[] extended = new byte[this.bytes.length + size];
            System.arraycopy(this.bytes, 0, extended, 0, this.bytes.length);
            System.arraycopy(buffer, 0, extended, this.bytes.length, size);
            this.bytes = extended;
        }
    }

}