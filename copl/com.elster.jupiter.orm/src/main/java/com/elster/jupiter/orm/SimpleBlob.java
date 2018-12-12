/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Provides a simple implementation of the Blob interface
 * that can be used to initialize persistent fields of type Blob.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-06 (13:48)
 */
public final class SimpleBlob implements Blob {

    private byte[] bytes;

    public static SimpleBlob empty() {
        return fromBytes(new byte[0]);
    }

    public static SimpleBlob fromString(String aString) {
        return fromBytes(aString.getBytes());
    }

    public static SimpleBlob fromBytes(byte[] bytes) {
        SimpleBlob simpleBlob = new SimpleBlob();
        simpleBlob.bytes = Arrays.copyOf(bytes, bytes.length);
        return simpleBlob;
    }

    private SimpleBlob() {
        super();
    }

    @Override
    public long length() {
        return this.bytes.length;
    }

    @Override
    public InputStream getBinaryStream() {
        return new ByteArrayInputStream(this.bytes);
    }

    @Override
    public void clear() {
        this.bytes = new byte[0];
    }

    @Override
    public OutputStream setBinaryStream() {
        return new WriteThroughAfterCloseByteArrayOutputStream();
    }

    private class WriteThroughAfterCloseByteArrayOutputStream extends OutputStream {
        private final ByteArrayOutputStream actualStream;

        WriteThroughAfterCloseByteArrayOutputStream() {
            this.actualStream = new ByteArrayOutputStream((int) length());
        }

        @Override
        public void close() throws IOException {
            this.actualStream.close();
            bytes = this.actualStream.toByteArray();
        }

        @Override
        public void write(int b) {
            actualStream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            actualStream.write(b, off, len);
        }

        @Override
        public void write(byte[] b) throws IOException {
            actualStream.write(b);
        }

        @Override
        public void flush() throws IOException {
            actualStream.flush();
        }
    }

}