package com.energyict.protocols.mdc.dialer.serialserviceprovider;

import com.energyict.mdc.protocol.api.dialer.serialserviceprovider.SerialPort;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author kvds
 *         Decorator over the serialport service provided outputstream
 */
public class SerOutputStream extends OutputStream {

    SerialPort serialPort;

    public SerOutputStream(SerialPort serialPort) {
        super();
        this.serialPort = serialPort;
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
        serialPort.getOutputStream().close();
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
        serialPort.getOutputStream().flush();
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
        serialPort.getOutputStream().write(b);
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
        serialPort.getOutputStream().write(b);
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
        serialPort.getOutputStream().write(b, off, len);
    }

    /**
     * @param writeDrain
     */
    public void setWriteDrain(boolean writeDrain) {
        serialPort.setWriteDrain(writeDrain);
    }


}
