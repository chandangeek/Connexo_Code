package com.energyict.mdc.io;

import com.energyict.mdc.common.TypedProperties;

import java.io.Flushable;

/**
 * Models a channel that supports communication with a physical device.
 * It allows reading and writing but does not specify if this is
 * synchronous or asynchronous. That is left as a responsibility of
 * implementation classes. The client will call {@link #startReading()}
 * or {@link #startWriting()} to indicate the intentions.
 * Switching between writing and reading will flush the underlying
 * implementation mechanisms.
 * Note that all IOExceptions thrown by the the underlying
 * implementation mechanisms are wrapped into a RuntimeException
 * and those will be dealt with by the communication framework.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-15 (08:40)
 */
public interface ComChannel extends Flushable {

    /**
     * Prepares the ComChannel for reading and returns
     * <code>true</code> if it was not ready for reading
     * before the call, i.e. if the read/write status
     * changed as a result.
     */
    public boolean startReading ();

    /**
     * Reads the next byte of data from the connected device.
     * The value byte is returned as an <code>int</code> in
     * the range <code>0</code> to <code>255</code>.
     * If no byte is available because the device has not written
     * anything yet or everything has already been consumed,
     * the value <code>-1</code> is returned.
     * This method blocks until input data is available,
     * the end of the communication is detected,
     * or an exception is thrown.
     *
     * @return The next byte of data, or <code>-1</code> if the end of the
     *         communication is reached.
     */
    public int read ();

    /**
     * Reads some number of bytes from the connected device
     * and stores them into the buffer array <code>buffer</code>.
     * The number of bytes actually read is returned.
     * This method blocks until input data is available,
     * the end of the communication is detected, or an exception is thrown.
     * <p/>
     * If the length of <code>buffer</code> is zero, then no bytes are read and
     * <code>0</code> is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the communication has
     * ended, the value <code>-1</code> is returned; otherwise, at
     * least one byte is read and stored into <code>buffer</code>.
     * <p/>
     * The first byte read is stored into element <code>buffer[0]</code>, the
     * next one into <code>buffer[1]</code>, and so on. The number of bytes read is,
     * at most, equal to the length of <code>buffer</code>. Let <i>k</i> be the
     * number of bytes actually read; these bytes will be stored in elements
     * <code>buffer[0]</code> through <code>buffer[</code><i>k</i><code>-1]</code>,
     * leaving elements <code>buffer[</code><i>k</i><code>]</code> through
     * <code>buffer[buffer.length-1]</code> unaffected.
     * <p/>
     * The <code>read(buffer)</code> method for class <code>InputStream</code>
     * has the same effect as: <pre><code> read(buffer, 0, buffer.length) </code></pre>
     *
     * @param buffer The buffer into which the data is read.
     * @return The total number of bytes read into the buffer, or
     *         <code>-1</code> is there is no more data because the end of
     *         the communication has been reached.
     * @throws NullPointerException If <code>buffer</code> is <code>null</code>.
     * @see #read(byte[], int, int)
     */
    public int read (byte buffer[]);

    /**
     * Reads up to <code>length</code> bytes of data from the connected device
     * into the <code>buffer</code>.  An attempt is made to read as many as
     * <code>length</code> bytes, but a smaller number may be read.
     * The number of bytes actually read is returned as an integer.
     * <p/>
     * This method blocks until input data is available,
     * the end of communication is detected, or an exception is thrown.
     * <p/>
     * If <code>length</code> is zero, then no bytes are read and
     * <code>0</code> is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the communication is at end,
     * the value <code>-1</code> is returned; otherwise, at least one
     * byte is read and stored into <code>buffer</code>.
     * <p/>
     * The first byte read is stored into element <code>buffer[offset]</code>, the
     * next one into <code>buffer[offset+1]</code>, and so on. The number of bytes read
     * is, at most, equal to <code>length</code>. Let <i>k</i> be the number of
     * bytes actually read; these bytes will be stored in elements
     * <code>buffer[offset]</code> through <code>buffer[offset+</code><i>k</i><code>-1]</code>,
     * leaving elements <code>buffer[offset+</code><i>k</i><code>]</code> through
     * <code>buffer[offset+length-1]</code> unaffected.
     * <p/>
     * In every case, elements <code>buffer[0]</code> through
     * <code>buffer[offset]</code> and elements <code>buffer[offset+length]</code> through
     * <code>buffer[buffer.length-1]</code> are unaffected.
     * <p/>
     * This method blocks until the requested amount of input data <code>length</code> has been read,
     * the end of communication is detected, or an exception is thrown.
     *
     * @param buffer The buffer into which the data is read.
     * @param offset The start offset in array <code>buffer</code>
     * at which the data is written.
     * @param length The maximum number of bytes to read.
     * @return The total number of bytes read into the buffer,
     *         or <code>-1</code> if there is no more data because the end of
     *         the communication has been reached.
     * @throws NullPointerException If <code>buffer</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException If <code>offset</code> is negative,
     * <code>length</code> is negative, or <code>length</code> is greater than
     * <code>buffer.length - offset</code>
     */
    public int read (byte buffer[], int offset, int length);

    /**
     * Returns an estimate of the number of bytes that can be read (or
     * skipped over) from the communication without blocking by the next
     * invocation of a method for this communication. The next invocation
     * might be the same thread or another thread.  A single read or skip of this
     * many bytes will not block, but may read or skip fewer bytes.
     *
     * @return An estimate of the number of bytes that can be read (or skipped over)
     *         from this communication without blocking or {@code 0} when
     *         it reaches the end of the communication.
     */
    public int available ();

    /**
     * Prepares the ComChannel for writing and returns
     * <code>true</code> if it was not ready for writing
     * before the call, i.e. if the read/write status
     * changed as a result.
     */
    public boolean startWriting ();

    /**
     * Writes the specified byte to the connected device.
     * The general contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>b</code> are ignored.
     * <p/>
     * Remember that all write methods automatically flush the underlying
     * implementaiton mechanisms.
     *
     * @param b The <code>byte</code>.
     * @return The number of bytes written
     */
    public int write (int b);

    /**
     * Writes <code>bytes.length</code> bytes from the specified byte array
     * to the connected device. The general contract for <code>write(bytes)</code>
     * is that it should have exactly the same effect as the call
     * <code>write(bytes, 0, bytes.length)</code>.
     * <p/>
     * Remember that all write methods automatically flush the underlying
     * implementaiton mechanisms.
     *
     * @param bytes The data
     * @return The number of bytes written
     */
    public int write (byte bytes[]);

    /**
     * Closes this ComChannel and releases any
     * system resources associated with it.
     */
    public void close ();

    /**
     * Adds the provided connectionTaskProperties
     * @param typedProperties the connectionTaskProperties
     */
    public void addProperties(TypedProperties typedProperties);

    /**
     * @return all configured properties of the current ConnectionTask
     */
    public TypedProperties getProperties();

    /**
     * Gets the ComChannelType of this ComChannel
     */
    public ComChannelType getComChannelType();

}