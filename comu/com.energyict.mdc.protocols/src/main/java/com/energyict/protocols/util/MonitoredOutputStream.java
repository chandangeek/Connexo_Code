/*
 * MonitoredOutputStream.java
 *
 * Created on 6 oktober 2002, 20:25
 */

package com.energyict.protocols.util;

import com.energyict.mdc.protocol.api.dialer.core.OutputStreamObserver;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Karel
 */
public class MonitoredOutputStream extends OutputStreamDecorator {

    private OutputStreamObserver observer;

    /**
     * Creates a new instance of MonitoredOutputStream
     */
    public MonitoredOutputStream(OutputStream stream, OutputStreamObserver observer) {
        super(stream);
        this.observer = observer;
    }

    protected OutputStreamObserver getObserver() {
        return observer;
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream. The general contract of <code>close</code>
     * is that it closes the output stream. A closed stream cannot perform
     * output operations and cannot be reopened.
     * <p/>
     *
     * @throws IOException if an I/O error occurs.
     */
    public void close() throws IOException {
        try {
            super.close();
        } catch (IOException ex) {
            getObserver().threw(ex);
            throw ex;
        }
    }

    /**
     * Flushes this output stream and forces any buffered output bytes
     * to be written out. The general contract of <code>flush</code> is
     * that calling it is an indication that, if any bytes previously
     * written have been buffered by the implementation of the output
     * stream, such bytes should immediately be written to their
     * intended destination.
     * <p/>
     *
     * @throws IOException if an I/O error occurs.
     */
    public void flush() throws IOException {
        try {
            super.flush();
        } catch (IOException ex) {
            getObserver().threw(ex);
            throw ex;
        }
    }

    /**
     * Writes <code>b.length</code> bytes from the specified byte array
     * to this output stream. The general contract for <code>write(b)</code>
     * is that it should have exactly the same effect as the call
     * <code>write(b, 0, b.length)</code>.
     *
     * @param b the data.
     * @throws IOException if an I/O error occurs.
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public void write(byte[] b) throws IOException {
        try {
            super.write(b);

            Object obj = b.clone();
            getObserver().wrote((byte[]) obj);
/*
            byte bytes[] = new byte[b.length];
            for (int i = 0 ; i < b.length ; i++)
                bytes[i] = b[i];
            getObserver().wrote(bytes);
 */
        } catch (IOException ex) {
            getObserver().threw(ex);
            throw ex;
        }
    }

    /**
     * Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>b</code> are ignored.
     * <p/>
     *
     * @param b the <code>byte</code>.
     * @throws IOException if an I/O error occurs. In particular,
     *                     an <code>IOException</code> may be thrown if the
     *                     output stream has been closed.
     */
    public void write(int b) throws IOException {
        try {
            super.write(b);
            byte bytes[] = new byte[1];
            bytes[0] = (byte) b;
            getObserver().wrote(bytes);
        } catch (IOException ex) {
            getObserver().threw(ex);
            throw ex;
        }
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * The general contract for <code>write(b, off, len)</code> is that
     * some of the bytes in the array <code>b</code> are written to the
     * output stream in order; element <code>b[off]</code> is the first
     * byte written and <code>b[off+len-1]</code> is the last byte written
     * by this operation.
     * <p/>
     * If <code>b</code> is <code>null</code>, a
     * <code>NullPointerException</code> is thrown.
     * <p/>
     * If <code>off</code> is negative, or <code>len</code> is negative, or
     * <code>off+len</code> is greater than the length of the array
     * <code>b</code>, then an <tt>IndexOutOfBoundsException</tt> is thrown.
     *
     * @param b   the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException if an I/O error occurs. In particular,
     *                     an <code>IOException</code> is thrown if the output
     *                     stream is closed.
     */
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            super.write(b, off, len);
            byte bytes[] = new byte[len];
            for (int i = 0; i < len; i++) {
                bytes[i] = b[off + i];
            }
            getObserver().wrote(bytes);
        } catch (IOException ex) {
            getObserver().threw(ex);
            throw ex;
        }
    }

}
