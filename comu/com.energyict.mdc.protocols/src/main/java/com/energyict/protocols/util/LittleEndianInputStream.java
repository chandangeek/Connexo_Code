/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LittleEndianInputStream.java
 *
 * Created on 18 september 2002, 15:31
 */

package com.energyict.protocols.util;

/**
 *
 * @author Joost
 */

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;

/**
 * represent a binary stream using the little endian byte order.
 * (as opposed to the java and network default of big endian byte order)
 */
public class LittleEndianInputStream extends DataInputStream {

    DataInputStream in;

    /**
     * Constructs a new <CODE>LittleEndianInputStream</CODE>
     *
     * @param in the decorated <CODE>InputStream</CODE>
     */
    public LittleEndianInputStream(InputStream in) {
        super(in);
        this.in = new DataInputStream(in);
    }

    /**
     * Constructs a string from the underlying input stream
     *
     * @param length The length of bytes to read
     * @return the String
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public String readString(int length) throws IOException {
        byte[] array = new byte[length];
        readFully(array);
        String s = new String(array);
        return s.trim();
    }

    /**
     * Translates little endian short to big endian short
     *
     * @return short A big endian short
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public short readLEShort() throws IOException {
        int byte1 = in.read();
        int byte2 = in.read();
        if (byte2 == -1) {
            throw new EOFException();
        }
        return (short) ((byte2 << 8) + byte1);
    }

    /**
     * Translates a little endian unsigned short to big endian int
     *
     * @return int A big endian short
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public int readLEUnsignedShort() throws IOException {
        int byte1 = in.read();
        int byte2 = in.read();
        if (byte2 == -1) {
            throw new EOFException();
        }
        return (byte2 << 8) + byte1;
    }

    /**
     * Translates a little endian char into a big endian char
     *
     * @return char A big endian char
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public char readLEChar() throws IOException {
        int byte1 = in.read();
        int byte2 = in.read();
        if (byte2 == -1) {
            throw new EOFException();
        }
        return (char) ((byte2 << 8) + byte1);
    }

    /**
     * Translates a little endian int into a big endian int
     *
     * @return int A big endian int
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public int readLEInt() throws IOException {
        int byte1, byte2, byte3, byte4;
        synchronized (this) {
            byte1 = in.read();
            byte2 = in.read();
            byte3 = in.read();
            byte4 = in.read();
        }
        if (byte4 == -1) {
            throw new EOFException();
        }
        return (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
    }

    /**
     * KV 18122002
     * Translates a little endian unsigned int into a big endian unsigned int
     *
     * @return int A big endian long
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public long readLEUnsignedInt() throws IOException {
        int byte1, byte2, byte3, byte4;
        synchronized (this) {
            byte1 = in.read();
            byte2 = in.read();
            byte3 = in.read();
            byte4 = in.read();
        }
        if (byte4 == -1) {
            throw new EOFException();
        }
        return ((byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1) & 0x00000000FFFFFFFFL;
    }

    /**
     * Translates a little endian long into a big endian long
     *
     * @return long A big endian long
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public long readLELong() throws IOException {
        long byte1 = in.read();
        long byte2 = in.read();
        long byte3 = in.read();
        long byte4 = in.read();
        long byte5 = in.read();
        long byte6 = in.read();
        long byte7 = in.read();
        long byte8 = in.read();
        if (byte8 == -1) {
            throw new EOFException();
        }
        return (byte8 << 56) + (byte7 << 48) + (byte6 << 40) + (byte5 << 32) +
                (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
    }

    /**
     * read UTF string from the receiver
     *
     * @return the String
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public String readLEUTF() throws IOException {
        int byte1 = in.read();
        int byte2 = in.read();
        if (byte2 == -1) {
            throw new EOFException();
        }
        int numbytes = (byte1 << 8) + byte2;

        char result[] = new char[numbytes];
        int numread = 0;
        int numchars = 0;

        while (numread < numbytes) {
            int c1 = readUnsignedByte();
            int c2, c3;

            // look at the first four bits of c1 to determine how many
            // bytes in this char
            int test = c1 >> 4;
            if (test < 8) {  // one byte
                numread++;
                result[numchars++] = (char) c1;
            } else if (test == 12 || test == 13) { // two bytes
                numread += 2;
                if (numread > numbytes) {
                    throw new UTFDataFormatException();
                }
                c2 = readUnsignedByte();
                if ((c2 & 0xC0) != 0x80) {
                    throw new UTFDataFormatException();
                }
                result[numchars++] = (char) (((c1 & 0x1F) << 6) | (c2 & 0x3F));
            } else if (test == 14) { // three bytes
                numread += 3;
                if (numread > numbytes) {
                    throw new UTFDataFormatException();
                }
                c2 = readUnsignedByte();
                c3 = readUnsignedByte();
                if (((c2 & 0xC0) != 0x80) || ((c3 & 0xC0) != 0x80)) {
                    throw new UTFDataFormatException();
                }
                result[numchars++] = (char)
                        (((c1 & 0x0F) << 12) | ((c2 & 0x3F) << 6) | (c3 & 0x3F));
            } else { // malformed
                throw new UTFDataFormatException();
            }
        }  // end while
        return new String(result, 0, numchars);
    }

    /**
     * Reads a little endian double into a big endian double
     *
     * @return double A big endian double
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public final double readLEDouble() throws IOException {
        return Double.longBitsToDouble(this.readLELong());
    }

    /**
     * Reads a little endian float into a big endian float
     *
     * @return float A big endian float
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public final float readLEFloat() throws IOException {
        return Float.intBitsToFloat(this.readLEInt());
    }
}
