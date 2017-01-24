/*
 * LittleEndianOutputStream.java
 *
 * Created on 20 september 2002, 10:19
 */

package com.energyict.protocols.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;

/**
 * represent a binary stream using the little endian byte order.
 * (as opposed to the java and network default of big endian byte order)
 *
 * @author Joost
 */


public class LittleEndianOutputStream extends DataOutputStream {

    /**
     * number of bytes written to the stream
     */
    protected int written;

    /**
     * write a string to the receiver
     *
     * @param string String to write
     * @param length length of the string to write
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public void writeString(String string, int length) throws IOException {
        if (string.length() < length) {
            String newstring = zeroFill(string, length);
            byte[] bytes = newstring.getBytes();
//         if(length==11){
//  	   System.out.println("bytes.length=" + bytes.length);
//         }
            out.write(bytes);
        } else {
            String newstring = string.substring(0, length);
            byte[] bytes = newstring.getBytes();
//        if(length==11){
//          System.out.println("bytes.length=" + bytes.length);
//        }
            out.write(bytes);
        }
    }

    private String zeroFill(String string, int length) {
        char[] oldchars = string.toCharArray();
        char[] newchars = new char[length];
        for (int i = 0; i <= newchars.length - 1; i++) {
            if (i <= oldchars.length - 1) {
                newchars[i] = oldchars[i];
            } else {
                newchars[i] = 0;
            }
        }
        return new String(newchars);
    }

    /**
     * creates a new instance of <CODE>LittleEndianOutputStream</CODE>
     *
     * @param out the output stream to chain this one to.
     */
    public LittleEndianOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Writes a number of type short in little endian
     *
     * @param s A number of type short
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public void writeLEShort(short s) throws IOException {
        out.write(s & 0xFF);
        out.write((s >>> 8) & 0xFF);
        written += 2;
    }

    /**
     * Writes a number of type char in little endian
     * param c An integer that is upcast from a Char data type.
     *
     * @param c character to write
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public void writeLEChar(int c) throws IOException {
        out.write(c & 0xFF);
        out.write((c >>> 8) & 0xFF);
        written += 2;
    }

    /**
     * Writes a number of type int in little endian
     *
     * @param i A number of type int
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public void writeLEInt(int i) throws IOException {
        out.write(i & 0xFF);
        out.write((i >>> 8) & 0xFF);
        out.write((i >>> 16) & 0xFF);
        out.write((i >>> 24) & 0xFF);
        written += 4;
    }

    /**
     * Writes a number of type long in little endian
     *
     * @param l A number of type long
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public void writeLELong(long l) throws IOException {
        out.write((int) l & 0xFF);
        out.write((int) (l >>> 8) & 0xFF);
        out.write((int) (l >>> 16) & 0xFF);
        out.write((int) (l >>> 24) & 0xFF);
        out.write((int) (l >>> 32) & 0xFF);
        out.write((int) (l >>> 40) & 0xFF);
        out.write((int) (l >>> 48) & 0xFF);
        out.write((int) (l >>> 56) & 0xFF);
        written += 8;
    }

    /**
     * Writes a number of type float in little endian
     *
     * @param f A number of type float.
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public final void writeLEFloat(float f) throws IOException {
        this.writeLEInt(Float.floatToIntBits(f));
    }

    /**
     * Writes a number a number of type double in little endian
     *
     * @param d A number of type double
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public final void writeLEDouble(double d) throws IOException {
        this.writeLELong(Double.doubleToLongBits(d));
    }

    /**
     * Writes a String in little endian
     *
     * @param s A string
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public void writeLEChars(String s) throws IOException {
        int length = s.length();
        for (int i = 0; i < length; i++) {
            int c = s.charAt(i);
            out.write(c & 0xFF);
            out.write((c >>> 8) & 0xFF);
        }
        written += length * 2;
    }

    /**
     * write a string to the receiver in little endian UTF format
     *
     * @param s the string to write
     * @throws IOException if the corresponding call to the underlying <CODE>InputStream</CODE> throws
     *                     an <CODE>IOException</CODE>
     */
    public void writeLEUTF(String s) throws IOException {
        int numchars = s.length();
        int numbytes = 0;
        for (int i = 0; i < numchars; i++) {
            int c = s.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                numbytes++;
            } else if (c > 0x07FF) {
                numbytes += 3;
            } else {
                numbytes += 2;
            }
        }

        if (numbytes > 65535) {
            throw new UTFDataFormatException();
        }

        out.write((numbytes >>> 8) & 0xFF);
        out.write(numbytes & 0xFF);
        for (int i = 0; i < numchars; i++) {
            int c = s.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                out.write(c);
            } else if (c > 0x07FF) {
                out.write(0xE0 | ((c >> 12) & 0x0F));
                out.write(0x80 | ((c >> 6) & 0x3F));
                out.write(0x80 | (c & 0x3F));
                written += 2;
            } else {
                out.write(0xC0 | ((c >> 6) & 0x1F));
                out.write(0x80 | (c & 0x3F));
                written += 1;
            }
        }
        written += numchars + 2;
    }
}
