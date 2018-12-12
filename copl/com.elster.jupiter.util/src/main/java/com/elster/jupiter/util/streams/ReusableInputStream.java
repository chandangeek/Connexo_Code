/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.streams;

import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ReusableInputStream {
    private byte[] bytes;

    private ReusableInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(100);
        ByteStreams.copy(inputStream, byteArrayOutputStream);
        bytes = byteArrayOutputStream.toByteArray();
        inputStream.close();
    }

    public static ReusableInputStream from(InputStream inputStream) throws IOException {
        return new ReusableInputStream(inputStream);
    }

    public InputStream stream() {
        return new ByteArrayInputStream(bytes);
    }

    public byte[] getBytes() {
        return bytes;
    }
}
