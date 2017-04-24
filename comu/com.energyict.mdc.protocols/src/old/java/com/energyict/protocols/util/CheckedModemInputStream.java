/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.util;

import java.io.IOException;
import java.io.InputStream;

public class CheckedModemInputStream extends InputStreamDecorator {

    private final char[] buffer;
    private int pos = 0;

    private static final String[] MODEM_ERRORS = new String[]{
            "NO DIALTONE",
            "BUSY",
            "NO CARRIER",
            "ERROR",
            "NO ANSWER"
    };

    /**
     * Use this class as a wrapper for the input stream you'd like to check for modem errors
     */
    public CheckedModemInputStream(InputStream in) {
        super(in);
        int bufferLength = 0;
        for (String modemError : MODEM_ERRORS) {
            bufferLength = Math.max(bufferLength, modemError.length());
        }
        buffer = new char[bufferLength];
    }

    @Override
    public int read() throws IOException {
        int read = getStream().read();
        updateBufferAndCheck(read);
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = getStream().read(b);
        if (read > 0) {
            for (int i = 0; i < read; i++) {
                updateBufferAndCheck(b[i]);
            }
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = getStream().read(b, off, len);
        if (read > 0) {
            for (int i = 0; i < read; i++) {
                updateBufferAndCheck(b[off + i]);
            }
        }
        return read;
    }

    /**
     * Check if the buffer contains a word that was flagged as a modem error. {@link CheckedModemInputStream#MODEM_ERRORS}
     *
     * @param byteValue the new received byte
     * @throws IOException If there was an modem error message found in the buffer
     */
    private void updateBufferAndCheck(int byteValue) throws IOException {
        if (byteValue != -1) {
            buffer[pos] = (char) (byteValue & 0x0FF);
            pos = (pos + 1) % buffer.length;
            String bufferValue = getBufferAsString();
            for (String modemError : MODEM_ERRORS) {
                if (bufferValue.contains(modemError)) {
                    throw new IOException("Received modem error [" + modemError + "]");
                }
            }
        }
    }

    /**
     * Convert the circular char buffer to a String by appending the chars in the correct order
     *
     * @return The string contents of the buffer
     */
    private String getBufferAsString() {
        String bufferValue = "";
        for (int i = 0; i < buffer.length; i++) {
            bufferValue += buffer[(pos + i) % buffer.length];
        }
        return bufferValue;
    }

}
