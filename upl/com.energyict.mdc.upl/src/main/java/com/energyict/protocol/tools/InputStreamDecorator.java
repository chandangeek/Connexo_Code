/*
 * InputStreamDecorator.java
 *
 * Created on 6 oktober 2002, 12:32
 */

package com.energyict.protocol.tools;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Karel
 */
public class InputStreamDecorator extends InputStream {

    private InputStream stream;

    /**
     * Creates a new instance of InputStreamDecorator
     */
    public InputStreamDecorator(InputStream stream) {
        this.stream = stream;
    }

    protected InputStream getStream() {
        return stream;
    }

    @Override
    public int available() throws IOException {
        return getStream().available();
    }

    @Override
    public void close() throws IOException {
        getStream().close();
    }

    @Override
    public void mark(int readlimit) {
        getStream().mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return getStream().markSupported();
    }

    @Override
    public int read() throws IOException {
        return getStream().read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return getStream().read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return getStream().read(b, off, len);
    }

    @Override
    public void reset() throws IOException {
        getStream().reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return getStream().skip(n);
    }

}