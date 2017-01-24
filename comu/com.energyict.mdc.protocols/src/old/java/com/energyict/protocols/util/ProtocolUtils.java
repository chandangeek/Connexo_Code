/*
 * ProtocolUtils.java
 *
 * Created on 17 januari 2003, 15:15
 */

package com.energyict.protocols.util;

import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.cbo.Quantity;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ProtocolUtils contains only static helper methods for parsing data.
 */
public class ProtocolUtils {

    private static final Logger LOGGER = Logger.getLogger(ProtocolUtils.class.getName());

    /**
     * Creates a new instance of ProtocolUtil
     */
    public ProtocolUtils() {
    }

    public static String getDateTimeWithTimeZone(Date date, TimeZone timeZone) {
        DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        format.setTimeZone(timeZone);
        return format.format(date);
    }

    public static Date parseDateTimeWithTimeZone(String date, String pattern, TimeZone timeZone) throws ParseException {
        DateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(timeZone);
        return format.parse(date);
    }

    public static void debugMessage(String message) {
        debugMessage(-1, message);
    }

    public static void debugMessage(int index, String message) {
        if (index == -1) {
            LOGGER.fine(" KV_DEBUG> " + message);
        } else {
            LOGGER.fine(" KV_DEBUG> index " + index + ", " + message);
        }
    }

    /**
     * Concatenate data1 and data2 together. Result is a byte array
     * of length data1+data2 and containing data1 followed by data2.
     *
     * @param data1 first byte array
     * @param data2 second byte array
     * @return concatenation of data1 and data2
     */

    public static byte[] concatByteArrays(byte[] data1, byte[] data2) {
        byte[] data = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data, 0, data1.length);
        System.arraycopy(data2, 0, data, data1.length, data2.length);
        return data;
    }

    /**
     * Construct a concatenated byteArray for the given ArrayList of byteArrays
     *
     * @param byteArrays the <code>byte[]</code> to concatenate
     * @return 1 <code>byte[]</code> with all given arrays after each other
     */
    public static byte[] concatListOfByteArrays(List<byte[]> byteArrays) {
        int counter = 0;
        for (byte[] byteArray : byteArrays) {
            counter += byteArray.length;
        }
        byte[] concatenatedArray = new byte[counter];
        int position = 0;
        for (byte[] byteArray : byteArrays) {
            System.arraycopy(byteArray, 0, concatenatedArray, position, byteArray.length);
            position += byteArray.length;
        }
        return concatenatedArray;
    }

    /**
     * Remove '(' and ')' from a String and return the enclosed string
     *
     * @param str input String with brackets
     * @return enclosed string
     */
    public static String stripBrackets(String str) {
        int index1 = str.indexOf("(");
        int index2 = str.indexOf(")");
        if ((index1 != -1) && (index2 != -1)) {
            return str.substring(index1 + 1, index2);
        } else if (index1 != -1) {
            return str.substring(index1 + 1);
        } else if (index2 != -1) {
            return str.substring(0, index2);
        } else {
            return str;
        }
    }

    /**
     * Flush the InputStream data
     *
     * @param inputStream InputStream data
     * @throws IOException Thrown in case of an exception
     */
    public static void flushInputStream(InputStream inputStream) throws IOException {
        while (inputStream.available() != 0) {
            inputStream.read();
        } // flush inputbuffer
    }

    /**
     * Sleep for nr of milliseconds
     *
     * @param iProtocolDelayAfterFail nr of milliseconds to sleep
     * @throws IOException Thrown in case of an exception
     */
    public static void delayProtocol(int iProtocolDelayAfterFail) throws IOException {
        try {
            Thread.sleep(iProtocolDelayAfterFail);
        } catch (InterruptedException e) {
            throw new IOException("delayProtocol() " + e.getMessage());
        }
    }

    /**
     * Build a String with the data converted to hex.
     *
     * @param byteBuffer data to build string from
     * @return String with representation of the data
     */
    public static String getResponseData(byte[] byteBuffer) {
        int i;
        StringBuilder builder = new StringBuilder();
        for (i = 1; i <= byteBuffer.length; i++) {
            builder.append(outputHexString((int) byteBuffer[i - 1] & 0x000000FF));
        }
        return builder.toString();
    }

    /**
     * Converts a buffer to a binary representation of the buffer. The return buffer
     * size is twice the length of the input buffer. E.g. input: 0x30 0x33 --> return: 0x33 0x30 0x33 0x33
     *
     * @param byteBuffer buffer to build string from
     * @return buffer that is the representation
     */
    public static byte[] convertAscii2Binary(byte[] byteBuffer) {
        byte[] data = new byte[byteBuffer.length * 2];
        for (int i = 0; i < byteBuffer.length; i++) {
            data[i * 2] = (byte) convertHexLSB(byteBuffer[i] & 0xFF);
            data[i * 2 + 1] = (byte) convertHexMSB(byteBuffer[i] & 0xFF);
        }
        return data;
    }

    /**
     * System.out.print of the input buffer byte array data. Output as 104F50 ...
     *
     * @param byteBuffer byte array
     */
    public static void printResponseData(byte[] byteBuffer) {
        int i;
        System.out.println("Received data:");
        for (i = 1; i <= byteBuffer.length; i++) {
            if ((i % 64) == 0) {
                System.out.println();
            }
            outputHex((int) byteBuffer[i - 1] & 0x000000FF);
        }
        System.out.println();
    }

    /**
     * System.out.print of the input buffer int array data. The data is interpreted as
     * a byte array. Output as 104F50 ...
     *
     * @param intBuffer int array
     */
    public static void printResponseData(int[] intBuffer) {
        int i;
        System.out.println("Received data:");
        for (i = 1; i <= intBuffer.length; i++) {

            if ((i % 64) == 0) {
                System.out.println();
            }
            outputHex(intBuffer[i - 1] & 0x000000FF);
        }
        System.out.println();
    }

    /**
     * Formatted System.out.print of the input buffer byte array data. Output as 0x10,0x4F,0x50, ...
     *
     * @param byteBuffer byte array
     */
    public static void printResponseDataFormatted(byte[] byteBuffer) {
        int i;
        System.out.println("Received data:");
        for (i = 1; i <= byteBuffer.length; i++) {

            if ((i % 64) == 0) {
                System.out.println();
            }
            outputHexFormatted((int) byteBuffer[i - 1] & 0x000000FF);
        }
        System.out.println();
    }

    /**
     * Formatted System.out.print of the input buffer byte array data. Output as 0x10,0x4F,0x50, ...
     *
     * @param byteBuffer byte array
     */
    public static void printResponseDataFormatted2(byte[] byteBuffer) {
        int i;
        for (i = 1; i <= byteBuffer.length; i++) {
            if ((i % 64) == 0) {
                System.out.println();
            }
            System.out.print("$" + buildStringHex(((int) byteBuffer[i - 1] & 0x000000FF), 2));
        }
        System.out.println();
    }

    /**
     * Formatted System.out.print of the input buffer int array data. The data is interpreted as
     * a byte array. Output as 0x10,0x4F,0x50, ...
     *
     * @param intBuffer int array
     */
    public static void printResponseDataFormatted(int[] intBuffer) {
        int i;
        System.out.println("Received data:");
        for (i = 1; i <= intBuffer.length; i++) {

            if ((i % 64) == 0) {
                System.out.println();
            }
            outputHexFormatted(intBuffer[i - 1] & 0x000000FF);
        }
        System.out.println();
    }

    /**
     * Convert int to byte and build String.
     * E.g. val: 10, String output: "0A"
     *
     * @param bKar int value to convert
     * @return String result
     */
    public static String outputHexString(int bKar) {
        String str = new String();
        str += String.valueOf((char) convertHexLSB(bKar));
        str += String.valueOf((char) convertHexMSB(bKar));
        return str;
    }

    /**
     * Convert given byteArray to hexString.
     * E.g. val: {10, 0, 9}, String output: "0A0009"
     *
     * @param data byteArray to convert
     * @return String result
     */
    public static String outputHexString(byte[] data) {
        return DatatypeConverter.printHexBinary(data);
    }

    /**
     * System.out int to byte.
     * E.g. val: 10, System.out.println: "0A"
     *
     * @param bKar int value to System.out
     */
    public static void outputHex(int bKar) {
        System.out.print((char) convertHexLSB(bKar));
        System.out.print((char) convertHexMSB(bKar));
    }

    public static String outputHexToString(int bKar) {
        String result = "" + (char) convertHexLSB(bKar);
        result += (char) convertHexMSB(bKar);
        return result;
    }

    /**
     * Convert int to byte to String in hexadecimal notation.
     * E.g. val: 10, System.out.println: "0A"
     *
     * @param bKar int value to convert
     * @return result String
     */
    public static String hex2String(int bKar) {
        return String.valueOf((char) convertHexLSB(bKar)) + (char) convertHexMSB(bKar);
    }

    /**
     * System.out int to byte.
     * E.g. val: 10, System.out.println: "0x0A,"
     *
     * @param bKar int value to System.out
     */
    public static void outputHexFormatted(int bKar) {
        System.out.print("(byte)0x");
        System.out.print((char) convertHexLSB(bKar));
        System.out.print((char) convertHexMSB(bKar));
        System.out.print(",");
    }

    /**
     * Return low nibble, converted to ascii
     *
     * @param bKar int value to extract low nibble from
     * @return int value of low nibble
     */
    public static int convertHexLSB(int bKar) {
        if (((bKar / 16) >= 0) && ((bKar / 16) <= 9)) {
            return (((bKar / 16) + 48));
        } else if (((bKar / 16) >= 0xA) && ((bKar / 16) <= 0xF)) {
            return (((bKar / 16) + 55));
        } else {
            return 0;
        }
    }

    /**
     * Return high nibble, converted to ascii
     *
     * @param bKar int value to extract high nibble from
     * @return int value of high nibble
     */
    public static int convertHexMSB(int bKar) {
        if (((bKar % 16) >= 0) && ((bKar % 16) <= 9)) {
            return (((bKar % 16) + 48));
        } else if (((bKar % 16) >= 0xA) && ((bKar % 16) <= 0xF)) {
            return (((bKar % 16) + 55));
        } else {
            return 0;
        }
    }


    // vb 0x15 --> 0x21

    /**
     * Convert hex byte value to bcd byte value. E.g. 0x15 --> 0x21
     *
     * @param bHexVal byte val
     * @return result byte val
     */
    public static byte hex2BCD(byte bHexVal) {
        return (byte) (((bHexVal / 10) << 4) | (bHexVal % 10));
    }

    // vb 0x15 --> 0x21

    /**
     * Convert hex int value to bcd byte value. E.g. 0x15 --> 0x21
     * Interprete int values as byte!
     *
     * @param iHexVal int value
     * @return result int value
     */
    public static byte hex2BCD(int iHexVal) {
        return (byte) ((((byte) iHexVal / 10) << 4) | ((byte) iHexVal % 10));
    }

    // vb 0x15 --> 0x0F

    /**
     * convert byte bcd value to hex. E.g. 0x15 --> 0x0F
     *
     * @param bBCDVal byte value
     * @return int value
     * @throws IOException thrown if input byte is no BCD presentation
     */
    public static int BCD2hex(byte bBCDVal) throws IOException {
        if (!isBCD(bBCDVal)) {
            throw new ProtocolException("ProtocolUtils, BCD2hex, BCD error!");
        }
        return ((((((int) bBCDVal & 0x000000FF) / 16) * 10) + (((int) bBCDVal & 0x000000FF) % 16)) & 0x000000FF);
    }


    private static boolean isBCD(byte bBCDVal) {
        int val = (int) bBCDVal & 0xFF;
        if (((val / 16) > 9) || ((val % 16) > 9)) {
            return false;
        } else {
            return true;
        }
    }

    private static byte toBCD(byte data) throws IOException {
        if (data - 0x30 > 9) {
            throw new ProtocolException("ProtocolUtils, toBCD, BCD error!");
        }
        return (byte) (data - 0x30);
    }

    /**
     * Convert ascii data to binary. The output array is half the size of the input
     * array. E.g. 0x30 0x33 0x35 0x45 ---> 0x03 0x5E
     * 0x30 0x33 0x31 0x35 ---> 0x03 0x15
     * The input data should contain only ascii data.
     *
     * @param data byte array
     * @return converted byte array
     * @throws IOException thrown when data length is not even
     */
    public static byte[] convert2ascii(byte[] data) throws IOException {
        if ((data.length % 2) != 0) {
            throw new ProtocolException("ProtocolUtils, convert2ascii, data length not even!");
        }
        byte[] converted = new byte[data.length / 2];
        for (int iOffset = 0; iOffset < data.length / 2; iOffset++) {
            converted[iOffset] = hex2byte(data, iOffset * 2);
        }
        return converted;
    }

    // 0x30 0x33 0x31 0x35 ---> 0x03 0x0F

    /**
     * Convert ascii bcd data to binary. The output array is half the size of the input
     * array. E.g. 0x30 0x33 0x31 0x35 ---> 0x03 0x0F
     * The input data should contain only ascii BCD data.
     *
     * @param data byte array
     * @return converted byte array
     * @throws IOException Thrown when datalength is not even or a byte is no BCD presentation
     */
    public static byte[] convertBCD2ascii(byte[] data) throws IOException {
        if ((data.length % 2) != 0) {
            throw new ProtocolException("ProtocolUtils, convert2ascii, data length not even!");
        }
        byte[] converted = new byte[data.length / 2];
        for (int iOffset = 0; iOffset < data.length / 2; iOffset++) {
            byte val = ProtocolUtils.hex2byte(data, iOffset * 2);
            converted[iOffset] = (byte) ProtocolUtils.BCD2hex(val);
        }
        return converted;
    }

    /**
     * Convert 2 ascii BCD bytes to byte
     *
     * @param data    input data array. At least length 2!
     * @param iOffset offety in the array where to start processing
     * @return byte value
     * @throws IOException when an exception happens
     */
    public static byte bcd2byte(byte[] data, int iOffset) throws IOException {
        return (byte) ((toBCD(data[iOffset]) * 10) + toBCD(data[iOffset + 1]));
    }

    /**
     * Convert 2 ascii BCD bytes to int
     *
     * @param data    input data array. At least length 2!
     * @param iOffset offety in the array where to start processing
     * @return int value
     * @throws IOException Thrown when an exception happens
     */
    public static int bcd2int(byte[] data, int iOffset) throws IOException {
        byte b = (byte) ((toBCD(data[iOffset]) * 10) + toBCD(data[iOffset + 1]));
        return (int) b & 0xFF;
    }

    /**
     * Convert 2 ascii bytes to byte
     *
     * @param data    input data array. At least length 2!
     * @param iOffset offety in the array where to start processing
     * @return byte value
     * @throws IOException thrown when data is invalid hex
     */
    public static byte hex2byte(byte[] data, int iOffset) throws IOException {
        int lowNibble = hex2nibble(data[iOffset + 1]) & 0xFF;
        int highNibble = hex2nibble(data[iOffset]) & 0xFF;
        return (byte) ((highNibble * 16) + lowNibble);
    }

    /**
     * Convert 1 ascii character to byte value
     *
     * @param data ascii byte value
     * @return converted byte value
     * @throws IOException thrown when data is invalid hex
     */
    public static byte hex2nibble(byte data) throws IOException {
        int nibble;
        if ((data >= 0x30) && (data <= 0x39)) {
            nibble = (((int) data & 0xff) - 0x30);
        } else if ((data >= 0x41) && (data <= 0x46)) {
            nibble = (((int) data & 0xff) - 0x37);
        } else if ((data >= 0x61) && (data <= 0x66)) {
            nibble = (((int) data & 0xff) - 0x57);
        } else {
            throw new ProtocolException("ProtocolUtils, hex2nibble, invalid hex data 0x" + Integer.toHexString((int) data & 0xff));
        }
        return (byte) nibble;
    }

    /**
     * Convert 1 ascii character from a byte array to a byte value
     *
     * @param data    ascii byte array
     * @param iOffset offset in the array
     * @return byte value
     */
    public static byte bcd2nibble(byte[] data, int iOffset) {
        return (byte) (data[iOffset] - 0x30);
    }

    /**
     * Get next value from stream
     *
     * @param bai byte input stream
     * @return next value from inputstream
     * @throws IOException Thrown when trying to read at the end of the stream
     */
    public static int getVal(ByteArrayInputStream bai) throws IOException {
        int val = bai.read();
        if (val == -1) {
            throw new ProtocolException("ProtocolUtils, getVal, Error end of file in stream");
        }
        return val;
    }

    /**
     * Get value from input array at a certain position
     *
     * @param data   input array
     * @param offset position int the input array
     * @return int value from array
     */
    public static int getByte2Int(byte[] data, int offset) {
        return ((int) data[offset] & 0xFF);
    }

    /**
     * Get little endian 32 bit integer from stream
     *
     * @param bai input byte stream
     * @return little endian 32 bit integer retrieved from stream
     * @throws IOException thrown when an exception happens
     */
    public static int getIntLE(ByteArrayInputStream bai) throws IOException {
        return ((getVal(bai) & 0x000000FF) |
                ((getVal(bai) << 8) & 0x0000FF00) |
                ((getVal(bai) << 16) & 0x00FF0000) |
                ((getVal(bai) << 24) & 0xFF000000));
    }

    /**
     * Get little endian 32 bit integer from byte array
     *
     * @param byteBuffer byte array
     * @return little endian 32 bit integer
     */
    public static int getIntLE(byte[] byteBuffer) {
        return getIntLE(byteBuffer, 0);
    }

    /**
     * Get little endian 32 bit integer from byte array starting at offset
     *
     * @param byteBuffer byte array
     * @param iOffset    offset
     * @return little endian 32 bit integer
     */
    public static int getIntLE(byte[] byteBuffer, int iOffset) {
        return ((((int) byteBuffer[iOffset]) & 0x000000FF) |
                (((int) byteBuffer[iOffset + 1] << 8) & 0x0000FF00) |
                (((int) byteBuffer[iOffset + 2] << 16) & 0x00FF0000) |
                (((int) byteBuffer[iOffset + 3] << 24) & 0xFF000000));
    }

    /**
     * Get 32 bit integer from byte array
     *
     * @param byteBuffer byte array
     * @return 32 bit integer
     */
    public static int getInt(byte[] byteBuffer) {
        return getInt(byteBuffer, 0);
    }

    /**
     * Get 32 bit integer from byte array starting at offset
     *
     * @param byteBuffer byte array
     * @param iOffset    offset
     * @return 32 bit integer
     */
    public static int getInt(byte[] byteBuffer, int iOffset) {
        return ((((int) byteBuffer[iOffset] << 24) & 0xFF000000) |
                (((int) byteBuffer[iOffset + 1] << 16) & 0x00FF0000) |
                (((int) byteBuffer[iOffset + 2] << 8) & 0x0000FF00) |
                (((int) byteBuffer[iOffset + 3]) & 0x000000FF));
    }

    /**
     * Get 64 bit little endian long from byte array starting at offset
     *
     * @param byteBuffer byte array
     * @param iOffset    offset
     * @return 64 bit little endian long
     */
    public static long getLongLE(byte[] byteBuffer, int iOffset) {
        return ((((long) byteBuffer[iOffset]) & 0x00000000000000FFL) |
                (((long) byteBuffer[iOffset + 1] << 8) & 0x000000000000FF00L) |
                (((long) byteBuffer[iOffset + 2] << 16) & 0x0000000000FF0000L) |
                (((long) byteBuffer[iOffset + 3] << 24) & 0x00000000FF000000L) |
                (((long) byteBuffer[iOffset + 4] << 32) & 0x000000FF00000000L) |
                (((long) byteBuffer[iOffset + 5] << 40) & 0x0000FF0000000000L) |
                (((long) byteBuffer[iOffset + 6] << 48) & 0x00FF000000000000L) |
                (((long) byteBuffer[iOffset + 7] << 56) & 0xFF00000000000000L));
    }


    /**
     * Get 64 bit long from byte array starting at offset
     *
     * @param byteBuffer byte array
     * @param iOffset    offset
     * @return 64 bit long
     */
    public static long getLong(byte[] byteBuffer, int iOffset) {
        return ((((long) byteBuffer[iOffset] << 56) & 0xFF00000000000000L) |
                (((long) byteBuffer[iOffset + 1] << 48) & 0x00FF000000000000L) |
                (((long) byteBuffer[iOffset + 2] << 40) & 0x0000FF0000000000L) |
                (((long) byteBuffer[iOffset + 3] << 32) & 0x000000FF00000000L) |
                (((long) byteBuffer[iOffset + 4] << 24) & 0x00000000FF000000L) |
                (((long) byteBuffer[iOffset + 5] << 16) & 0x0000000000FF0000L) |
                (((long) byteBuffer[iOffset + 6] << 8) & 0x000000000000FF00L) |
                (((long) byteBuffer[iOffset + 7]) & 0x00000000000000FFL));
    }

    /**
     * Get 16 bit little endian short from byte inputstream
     *
     * @param bai byte inputstream
     * @return 16 bit little endian short
     * @throws IOException Thrown when an exception happens
     */
    public static short getShortLE(ByteArrayInputStream bai) throws IOException {
        return (short) ((((short) getVal(bai)) & 0x00FF) |
                (((short) getVal(bai) << 8) & 0xFF00));
    }

    /**
     * Get 16 bit little endian short from byte array at offset
     *
     * @param byteBuffer byte array
     * @param iOffset    offset
     * @return 16 bit little endian short
     */
    public static short getShortLE(byte[] byteBuffer, int iOffset) {
        return (short) ((((short) byteBuffer[iOffset]) & 0x00FF) |
                (((short) byteBuffer[iOffset + 1] << 8) & 0xFF00));
    }

    /**
     * Get 16 bit short from byte inputstream
     *
     * @param bai byte inputstream
     * @return 16 bit short
     * @throws IOException thrown when an exception happens
     */
    public static short getShort(ByteArrayInputStream bai) throws IOException {
        return (short) ((((short) getVal(bai) << 8) & 0xFF00) |
                ((short) getVal(bai) & 0x00FF));
    }

    /**
     * Get 16 bit short from byte array at offset
     *
     * @param byteBuffer byte array
     * @param iOffset    offset
     * @return 16 bit short
     */
    public static short getShort(byte[] byteBuffer, int iOffset) {
        return (short) ((((short) byteBuffer[iOffset] << 8) & 0xFF00) |
                ((short) byteBuffer[iOffset + 1] & 0x00FF));
    }

    /**
     * Convert short to int
     *
     * @param val short value
     * @return int value
     */
    public static int short2int(short val) {
        return (int) val & 0x0000FFFF;
    }

    /**
     * Get integer value from byte array, starting at offset for length bytes using big endian orientation.
     *
     * @param byteBuffer byte array
     * @param offset     0-based offset
     * @param length     number of bytes to use for calculation (min 1, max 4)
     * @return integer value
     * @throws IOException thrown when an exception happens
     */
    public static int getInt(byte[] byteBuffer, int offset, int length) throws ProtocolException {
        int retval = 0;
        int shift;
        try {
            if ((length > 4) || (length < 1)) {
                throw new ProtocolException("ProtocolUtils, getInt, invalid length");
            }
            for (int i = 0; i < length; i++) {
                shift = 8 * (length - (1 + i));
                retval |= ((int) byteBuffer[offset + i] & 0xff) << shift;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ProtocolException("ProtocolUtils, getInt, ArrayIndexOutOfBoundsException, " + e.getMessage());
        }
        return retval;
    }

    /**
     * Get integer value from byte array, starting at offset for length bytes using little endian orientation.
     *
     * @param byteBuffer byte array
     * @param offset     0-based offset in byte array
     * @param length     number of bytes to use for calculation (min 1, max 4)
     * @return integer value
     * @throws IOException thrown when an exception happens
     */
    public static int getIntLE(byte[] byteBuffer, int offset, int length) throws IOException {
        int retval = 0;
        int shift;
        try {
            if ((length > 4) || (length < 1)) {
                throw new ProtocolException("ProtocolUtils, getIntLE, invalid length");
            }
            for (int i = 0; i < length; i++) {
                shift = 8 * i;
                retval |= ((int) byteBuffer[offset + i] & 0xff) << shift;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ProtocolException("ProtocolUtils, getIntLE, ArrayIndexOutOfBoundsException, " + e.getMessage());
        }
        return retval;
    }

    /**
     * Extract a int value from the BCD byte array starting at offset for length. The byte array is in little endial.
     *
     * @param byteBuffer byte array
     * @param offset     0-based offset in byte array
     * @param length     number of bytes to use for calculation (min 1, max 4)
     * @return int value
     * @throws IOException thrown when an exception happens
     */
    public static int getBCD2IntLE(byte[] byteBuffer, int offset, int length) throws IOException {
        int val = 0;
        int multiplier = 1;
        try {
            for (int i = offset; i < (offset + length); i++) {
                val += ((((byte2int(byteBuffer[i]) >> 4) * 10) + (byte2int(byteBuffer[i]) & 0x0F)) * multiplier);
                multiplier *= 100;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ProtocolException("ProtocolUtils, getBCD2IntLE, ArrayIndexOutOfBoundsException, " + e.getMessage());
        }
        return val;
    }

    /**
     * Extract an int value from the BCD byte array starting at offset for length.
     *
     * @param byteBuffer byte array
     * @param offset     offset
     * @param length     length
     * @return int value
     * @throws IOException Thrown when an exception happens
     */
    public static int getBCD2Int(byte[] byteBuffer, int offset, int length) throws IOException {
        int val = 0;
        int multiplier = 1;
        try {
            for (int i = ((offset + length) - 1); i >= offset; i--) {
                val += ((((byte2int(byteBuffer[i]) >> 4) * 10) + (byte2int(byteBuffer[i]) & 0x0F)) * multiplier);
                multiplier *= 100;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ProtocolException("ProtocolUtils, getBCD2IntLE, ArrayIndexOutOfBoundsException, " + e.getMessage());
        }
        return val;
    }


    /**
     * Extract a byte buffer whos length< 8 to 8 bytes.
     * Copy the most significant bit data[0]:7 to all extended bytes.
     *
     * @param data   byte array
     * @param offset offset
     * @param length length
     * @return byte[8] array
     */
    public static byte[] signExtend2Long(byte[] data, int offset, int length) {
        byte[] extendeddata = new byte[8];
        System.arraycopy(data, offset, extendeddata, 8 - length, length);
        if ((extendeddata[8 - length] & 0x80) == 0x80) {
            for (int i = 0; i < (8 - length); i++) {
                extendeddata[i] = (byte) 0xFF;
            }
        }
        return extendeddata;
    }

    /**
     * Extract a long value from the byte array starting at offset for length. Extend the most significant bit to build a signed long.
     * This is used to convert signed 16,32,40,48 and 56 bit values.
     *
     * @param byteBuffer byte array
     * @param offset     offset
     * @param length     length
     * @return long value
     * @throws IOException Thrown when an exception happens
     */
    public static long getExtendedLong(byte[] byteBuffer, int offset, int length) throws IOException {
        byte[] extendedByteBuffer = signExtend2Long(byteBuffer, offset, length);
        return getLong(extendedByteBuffer, 0, 8);
    }


    /**
     * Extract a long value from the byte array starting at offset for length.
     *
     * @param byteBuffer byte array
     * @param offset     offset
     * @param length     length
     * @return long value
     * @throws IOException Thrown when an exception happens
     */
    public static long getLong(byte[] byteBuffer, int offset, int length) throws IOException {
        long retval = 0;
        int shift;
        try {
            if (length > 8) {
                throw new ProtocolException("ProtocolUtils, getLong, invalid length");
            }
            for (int i = 0; i < length; i++) {
                shift = 8 * (length - (1 + i));
                retval |= ((long) byteBuffer[offset + i] & 0xffL) << shift;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ProtocolException("ProtocolUtils, getLong, ArrayIndexOutOfBoundsException, " + e.getMessage());
        }
        return retval;
    }

    /**
     * Extract a long value from an input byte stream starting for length.
     *
     * @param bai    input byte stream
     * @param length length
     * @return long value
     * @throws IOException Thrown when an exception happens
     */
    public static long getLong(ByteArrayInputStream bai, int length) throws IOException {
        long retval = 0;
        int shift;
        if (length > 8) {
            throw new ProtocolException("ProtocolUtils, getLong, invalid length");
        }
        for (int i = 0; i < length; i++) {
            shift = 8 * (length - (1 + i));
            retval |= ((long) getVal(bai) & 0xffL) << shift;
        }
        return retval;
    }

    /**
     * Extract a little endian long value from the byte array starting at offset for length.
     *
     * @param byteBuffer byte array
     * @param offset     offset
     * @param length     length
     * @return little endian long value
     * @throws IOException Thrown when an exception happens
     */
    public static long getLongLE(byte[] byteBuffer, int offset, int length) throws IOException {
        long retval = 0;
        int shift;
        try {
            if (length > 8) {
                throw new ProtocolException("ProtocolUtils, getLongLE, invalid length");
            }
            for (int i = 0; i < length; i++) {
                shift = 8 * i;
                retval |= ((long) byteBuffer[offset + i] & 0xffL) << shift;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ProtocolException("ProtocolUtils, getLongLE, ArrayIndexOutOfBoundsException, " + e.getMessage());
        }
        return retval;
    }

    /**
     * Extract a little endian long value from a byte input stream for length.
     *
     * @param bai    byte input stream
     * @param length length
     * @return little endian long value
     * @throws IOException Thrown when an exception happens
     */
    public static long getLongLE(ByteArrayInputStream bai, int length) throws IOException {
        long retval = 0;
        int shift;
        if (length > 8) {
            throw new ProtocolException("ProtocolUtils, getLongLE, invalid length");
        }
        for (int i = 0; i < length; i++) {
            shift = 8 * i;
            retval |= ((long) getVal(bai) & 0xffL) << shift;
        }
        return retval;
    }


    /**
     * Convert a TimeZone to its GMT+rawoffset+1 (summertime) equivalent
     * E.g. ECT returns GMT+2
     * WET returns GMT+1
     *
     * @param timeZone Any java TimeZone abbreviation
     * @return GMT+n TimeZone
     */
    public static TimeZone getSummerTimeZone(TimeZone timeZone) {

        int offset = (timeZone.getRawOffset() / 3600000) + 1;
        return TimeZone.getTimeZone("GMT" + (offset < 0 ? "" : "+") + offset);
    }

    /**
     * Convert a TimeZone to its GMT+rawoffset+1 (summertime) equivalent
     * E.g. ECT returns GMT+1
     * WET returns GMT
     *
     * @param timeZone Any java TimeZone abbreviation
     * @return GMT+n TimeZone
     */
    public static TimeZone getWinterTimeZone(TimeZone timeZone) {
        int offset = (timeZone.getRawOffset() / 3600000);
        return TimeZone.getTimeZone("GMT" + (offset < 0 ? "" : "+") + offset);
    }

    /**
     * Initialize and return a Calendar for a certain TimeZone.
     * If booleanDaylightSavingEnabled false, return Calendar created with TimeZone.
     * If booleanDaylightSavingEnabled true, extract the GMT+n summertime from timeZone.
     *
     * @param booleanDaylightSavingEnabled Force DST
     * @param timeZone                     TimeZone
     * @return Calendar
     */
    public static Calendar initCalendar(boolean booleanDaylightSavingEnabled, TimeZone timeZone) {
        Calendar calendar;
        if (booleanDaylightSavingEnabled) {
            calendar = Calendar.getInstance(getSummerTimeZone(timeZone));
        } else {
            calendar = Calendar.getInstance(timeZone);
        }
        return calendar;
    }

    /**
     * Return a Calendar using a TimeZone constructed with a deviation in hours. If DSA true, 1 is added to the iTimeZoneDeviation.
     *
     * @param dsa               Force to summertime timezone
     * @param timeZoneDeviation deviation in hours
     * @return Calendar
     */
    public static Calendar getCalendar(boolean dsa, int timeZoneDeviation) {
        int dev = timeZoneDeviation;
        dev += (dsa ? 1 : 0);
        String str = new String("GMT" + (dev < 0 ? "" : "+") + dev);

        return Calendar.getInstance(TimeZone.getTimeZone(str));
    }

    /**
     * Create a Calendar using a meter's seconds offset from UTC and a meter TimeZone
     * <I>Some meters report time in seconds from UTC. So if we use that shift to set the initial calendar
     * with setTimeInMillis(), time is set from UTC, so the TimeZone used is ignored. Therefor,
     * we add the initial timezone offset to the time in seconds from the meter.</I>
     *
     * @param timeZone       meter TimeZone
     * @param shiftInSeconds seconds offset from UTC
     * @return Calendar
     */
    public static Calendar getCalendar(TimeZone timeZone, long shiftInSeconds) {

        /* Onderstaande code is beter omdat we daar geen twijfelachtige resultaten kunnen krijgen tijdens
         * het overgangsuur zomer --> winter
        Calendar calendar = Calendar.getInstance(timeZone);
        Date date = new Date(shiftInSeconds*1000);
        int offset = timeZone.inDaylightTime(date) ? 3600000 : 0;
        calendar.setTimeInMillis(shiftInSeconds*1000-(timeZone.getRawOffset()+offset));
        */

        Date date = new Date(shiftInSeconds * 1000);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        Calendar calendar2 = Calendar.getInstance(timeZone);
        calendar2.clear();
        calendar2.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        calendar2.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR));
        calendar2.set(Calendar.SECOND, (int) (shiftInSeconds % 86400));
        return calendar2;
    }


    /*
     public static Calendar getCalendarKV(TimeZone timeZone, long shiftInSeconds) {
         // ????????????????????????????
         // de DST omschakeling om 1AM GMT, dus bij init van de calendar moeten we timeZone.getRawOffset
         // aftrekken van de date in ms teneinde juiste timeZone.inDaylightTime terug te krijgen!
         Calendar calendar = Calendar.getInstance(timeZone);
         Date date = new Date(shiftInSeconds*1000-timeZone.getRawOffset());
         int offset = timeZone.inDaylightTime(date) ? 3600000 : 0;
         long l = shiftInSeconds*1000-(timeZone.getRawOffset()+offset);
         calendar.setTimeInMillis(l);
         return calendar;
     }
    */

    /**
     * Extract and convert to byte a nibble value from a byte array starting at offset.
     * The offset is the nibble offset. The orientation is little endian.
     * E.g. nibble0 = byte[0] bit 7..4, nibble1 = bit 3..0, nible2 = byte[1] bit 7..4, ...
     *
     * @param data   byte array from which to extract nibble value
     * @param offset offset in the byte array (nibble offset)
     * @return nibble byte value
     */
    public static byte getNibble(byte[] data, int offset) {
        int byteval = data[offset / 2];

        return (byte) (byteval >> (Math.abs((offset % 2) - 1) * 4) & 0x0F);
    }

    /**
     * Extract a BigDecimal Number from the byte array starting a offset for length.
     * The byte array is interpreted as nibbles in little endian notation. See
     * description of getNibble() for an example of how the data is retrieved.
     *
     * @param data   byte array from which to extract the BigDecimal
     * @param offset index where to start extraction in the byte array
     * @param size   how many nibbles to use to build BigDecimal value
     * @return Number representing a BigDecimal
     * @throws IOException thrown when an incorrect size is passed
     */
    public static Number getLongFromNibblesLE(byte[] data, int offset, int size) throws IOException {
        long lVal = 0;
        long lNibbleVal;
        int i;
        if (size > 16) {
            throw new ProtocolException("ProtocolUtils, getLongFromNibblesLE, size too big to fit in long!");
        }
        for (i = 0; i < size; i++) {
            lNibbleVal = (long) getNibble(data, i + offset);
            lVal |= (lNibbleVal << (i * 4));
        }
        return BigDecimal.valueOf(lVal);
    }

    /**
     * Extract a BigDecimal Number from the byte array starting a offset for length.
     * The byte array is in little endian notation.
     *
     * @param data   byte array from which to extract the BigDecimal
     * @param offset index where to start extraction in the byte array
     * @param size   how many bytes to use to build BigDecimal value
     * @return Number representing a BigDecimal
     * @throws IOException thrown when an incorrect size is passed
     */
    public static Number getLongFromBytesLE(byte[] data, int offset, int size) throws IOException {
        long lVal = 0;
        long lByteVal;
        int i;
        if (size > 8) {
            throw new ProtocolException("ProtocolUtils, getLongFromBytes, size too big to fit in long!");
        }
        for (i = 0; i < size; i++) {
            lByteVal = (long) data[i + offset] & 0xFF;
            lVal |= (lByteVal << (i * 8));
        }
        return BigDecimal.valueOf(lVal);
    }

    /**
     * Extract a BigDecimal Number from the byte array starting a offset for length.
     * The byte array is in big endian notation.
     *
     * @param data   byte array from which to extract the BigDecimal
     * @param offset index where to start extraction in the byte array
     * @param size   how many bytes to use to build BigDecimal value
     * @return Number representing a BigDecimal
     * @throws IOException thrown when an incorrect size is passed
     */
    public static Number getLongFromBytes(byte[] data, int offset, int size) throws IOException {
        long lVal = 0;
        long lByteVal;
        int i;
        if (size > 8) {
            throw new ProtocolException("ProtocolUtils, getLongFromBytes, size too big to fit in long!");
        }
        for (i = 0; i < size; i++) {
            lByteVal = (long) data[i + offset] & 0xFF;
            lVal |= (lByteVal << (((size - 1) - i) * 8));
        }
        return BigDecimal.valueOf(lVal);
    }


    /**
     * Parse an integer from a String (Ascii byte array)
     *
     * @param data   Ascii byte array
     * @param offset offset in the ascii byte array
     * @param length nr of ascii characters from byte array to parse
     * @return return the int value
     */
    public static int parseIntFromStr(byte[] data, int offset, int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append((char) data[i + offset]);
        }
        return Integer.parseInt(builder.toString());
    }

    /**
     * Return a clean Calendar for TimeZone
     *
     * @param timeZone TimeZone to create the Calendar
     * @return Calendar
     */
    public static Calendar getCleanCalendar(TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.clear();
        return calendar;
    }

    public static Calendar getCleanGMTCalendar() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.clear();
        return calendar;
    }

    /**
     * Return a Calendar for TimeZone
     *
     * @param timeZone TimeZone to create the Calendar
     * @return Calendar
     */
    public static Calendar getCalendar(TimeZone timeZone) {
        return Calendar.getInstance(timeZone);
    }

    /**
     * Convert val to a BCD ascii byte array of length 2 (16 bit) and copy in the
     * passed output byte array at position offset.
     *
     * @param val    value to parse
     * @param data   Output byte array where to copy the result in
     * @param offset offset position int the output byte array where to copy the 2 bytes
     */
    public static void val2BCDascii(int val, byte[] data, int offset) {
        byte bcd = hex2BCD(val);
        byte[] ascii = new byte[2];
        ascii[0] = (byte) convertHexLSB((int) bcd & 0xFF);
        ascii[1] = (byte) convertHexMSB((int) bcd & 0xFF);
        data[offset] = ascii[0];
        data[offset + 1] = ascii[1];
    }

    /**
     * Convert val to a HEX ascii byte array of length 2 (16 bit) and copy in the
     * passed output byte array at position offset.
     *
     * @param val    value to parse
     * @param data   Output byte array where to copy the result in
     * @param offset offset position int the output byte array where to copy the 2 bytes
     */
    public static void val2HEXascii(int val, byte[] data, int offset) {
        byte[] ascii = new byte[2];
        ascii[0] = (byte) convertHexLSB(val & 0xFF);
        ascii[1] = (byte) convertHexMSB(val & 0xFF);
        data[offset] = ascii[0];
        data[offset + 1] = ascii[1];
    }

    /**
     * Return the toString() result for Integer, Long, BigDecimal, String, Quantity
     * objects. If non of the above, an exception is returned.
     *
     * @param obj Object to invoke toString() on
     * @return toString() result
     * @throws IOException Thrown when the object is none of the supported.
     */
    public static String obj2String(Object obj) throws IOException {
        if (obj.getClass().getName().contains("Integer")) {
            return obj.toString();
        } else if (obj.getClass().getName().contains("Long")) {
            return obj.toString();
        } else if (obj.getClass().getName().contains("BigDecimal")) {
            return obj.toString();
        } else if (obj.getClass().getName().contains("String")) {
            return (String) obj;
        } else if (obj.getClass().getName().contains("Quantity")) {
            return obj.toString();
        } else {
            throw new IOException("ProtocolUtils, obj2String, class " + obj.getClass().getName() + " not supported");
        }
    }

    /**
     * Return the int value for Integer, Long, BigDecimal, String, Quantity
     * objects. If non of the above, an exception is returned.
     *
     * @param obj Object to get the int value from
     * @return int value
     * @throws IOException Thrown when the object is none of the supported.
     */
    public static int obj2int(Object obj) throws IOException {
        if (obj.getClass().getName().contains("Integer")) {
            return ((Integer) obj).intValue();
        } else if (obj.getClass().getName().contains("Long")) {
            return ((Long) obj).intValue();
        } else if (obj.getClass().getName().contains("BigDecimal")) {
            return ((BigDecimal) obj).intValue();
        } else if (obj.getClass().getName().contains("String")) {
            return Integer.parseInt((String) obj);
        } else if (obj.getClass().getName().contains("Quantity")) {
            return ((Quantity) obj).getAmount().intValue();
        } else {
            throw new IOException("ProtocolUtils, obj2int, class " + obj.getClass().getName() + " not supported");
        }
    }

    /**
     * Copy a source (src) array into a destination (dest) array at a position defined by offset.
     *
     * @param src    source array to copy
     * @param dest   array to copy to
     * @param offset in dest array to copy to
     * @return new offset in dest array
     * @throws IOException Thrown when the source array length > dest array length - offset
     */
    public static int arrayCopy(byte[] src, byte[] dest, int offset) throws IOException {
        if (src.length > (dest.length - offset)) {
            throw new ProtocolException("ProtocolUtils, arrayCopy, src.length=" + src.length + " dest.length=" + dest.length + " offset=" + offset);
        }
        for (int i = 0; i < src.length; i++) {
            dest[offset + i] = src[i];
        }
        return offset + src.length;
    }

    /**
     * Convert input byte array to an int array. Avoid sign extension!
     * This method is used to convert signed byte to unsigned byte values.
     *
     * @param ba byte array
     * @return int array
     */
    public static int[] toIntArray(byte[] ba) {
        int[] ia = new int[ba.length];
        for (int i = 0; i < ba.length; i++) {
            ia[i] = (int) ba[i] & 0xff;
        }
        return ia;
    }

    /**
     * returns a sub array from index to to index, to index byte included
     *
     * @param data source array
     * @param from from index
     * @param to   to index
     * @return subarray
     */
    public static byte[] getSubArray(byte[] data, int from, int to) {
        byte[] subArray = new byte[(to + 1) - from];
        for (int i = 0; i < subArray.length; i++) {
            subArray[i] = data[i + from];
        }
        return subArray;
    }

    /**
     * returns a sub array from offset to offset + length
     *
     * @param data   source array
     * @param offset from index
     * @param length length of sub array
     * @return subarray
     */
    public static byte[] getSubArray2(byte[] data, int offset, int length) {
        byte[] subArray = new byte[length];
        for (int i = 0; i < subArray.length; i++) {
            subArray[i] = data[i + offset];
        }
        return subArray;
    }

    /**
     * returns a sub array from index to end
     *
     * @param data source array
     * @param from from index
     * @return subarray
     */
    public static byte[] getSubArray(byte[] data, int from) {
        byte[] subArray = new byte[data.length - from];
        for (int i = 0; i < subArray.length; i++) {
            subArray[i] = data[i + from];
        }
        return subArray;
    }

    /**
     * Convert byte to int. Avoid sign extension!
     * This method is used to convert signed byte to unsigned byte value.
     *
     * @param b value to convert
     * @return int value
     */
    public static int byte2int(byte b) {
        return ((int) b & 0xFF);
    }

    /**
     * Build a hexadecimal String representation from an int value an 0-extend the value to length.
     * E.g. buildStringHex(10,4) returns "000A" String
     *
     * @param value  Value to convert
     * @param length length of the String
     * @return 0-extended String value
     */
    public static String buildStringHex(int value, int length) {
        String str = Integer.toHexString(value);
        StringBuilder builder = new StringBuilder();
        if (length >= str.length()) {
            for (int i = 0; i < (length - str.length()); i++) {
                builder.append('0');
            }
        }
        builder.append(str);
        return builder.toString();
    }

    /**
     * Build a hexadecimal String representation from an long value an 0-extend the value to length.
     * E.g. buildStringHex(10,4) returns "000A" String
     *
     * @param value  Value to convert
     * @param length length of the String
     * @return 0-extended String value
     */
    public static String buildStringHex(long value, int length) {
        String str = Long.toHexString(value);
        StringBuilder builder = new StringBuilder();
        if (length >= str.length()) {
            for (int i = 0; i < (length - str.length()); i++) {
                builder.append('0');
            }
        }
        builder.append(str);
        return builder.toString();
    }

    /**
     * Build a decimal String representation from an int value an 0-extend the value to length.
     * E.g. buildStringHex(10,4) returns "0010" String
     *
     * @param value  Value to convert
     * @param length length of the String
     * @return 0-extended String value
     */
    public static String buildStringDecimal(int value, int length) {
        String str = Integer.toString(value);
        StringBuilder builder = new StringBuilder();
        if (length >= str.length()) {
            for (int i = 0; i < (length - str.length()); i++) {
                builder.append('0');
            }
        }
        builder.append(str);
        return builder.toString();
    }

    // for testing purposes

    public static byte[] readFile(String fileName) {
        try {
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            return data;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public static List checkOnOverlappingEvents(List meterEvents) {
        Map eventsMap = new HashMap();
        int size = meterEvents.size();
        for (int i = 0; i < size; i++) {
            MeterEvent event = (MeterEvent) meterEvents.get(i);
            Date time = event.getTime();
            MeterEvent eventInMap = (MeterEvent) eventsMap.get(time);
            while (eventInMap != null) {
                time.setTime(time.getTime() + 1000); // add one second
                eventInMap = (MeterEvent) eventsMap.get(time);
            }
            MeterEvent newMeterEvent =
                    new MeterEvent(time, event.getEiCode(), event.getProtocolCode(), event.getMessage());
            eventsMap.put(time, newMeterEvent);
        }
        Iterator it = eventsMap.values().iterator();
        List result = new ArrayList();
        while (it.hasNext()) {
            result.add(it.next());
        }
        return result;
    }

    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Create a midnight date from one month ago. If no timeZone is given, then the {@link java.util.TimeZone#getDefault()} will be used.
     *
     * @param deviceTimeZone the timezone to use in the <CODE>Calendar</CODE>
     * @return a date from 1 month ago at 00h00
     */
    public static Date getClearLastMonthDate(TimeZone deviceTimeZone) {
        Calendar tempCalendar = Calendar.getInstance(deviceTimeZone != null ? deviceTimeZone : TimeZone.getDefault());
        tempCalendar.add(Calendar.MONTH, -1);
        tempCalendar.set(Calendar.HOUR_OF_DAY, 0);
        tempCalendar.set(Calendar.MINUTE, 0);
        tempCalendar.set(Calendar.SECOND, 0);
        tempCalendar.set(Calendar.MILLISECOND, 0);
        return tempCalendar.getTime();
    }

    /**
     * Create a midnight date from one day ago. If no timeZone is given, then the {@link java.util.TimeZone#getDefault()} will be used.
     *
     * @param deviceTimeZone the timezone to use in the <CODE>Calendar</CODE>
     * @return a date from 1 day ago at 00h00
     */
    public static Date getClearLastDayDate(TimeZone deviceTimeZone) {
        Calendar tempCalendar = Calendar.getInstance(deviceTimeZone != null ? deviceTimeZone : TimeZone.getDefault());
        tempCalendar.add(Calendar.DAY_OF_YEAR, -1);
        tempCalendar.set(Calendar.HOUR_OF_DAY, 0);
        tempCalendar.set(Calendar.MINUTE, 0);
        tempCalendar.set(Calendar.SECOND, 0);
        tempCalendar.set(Calendar.MILLISECOND, 0);
        return tempCalendar.getTime();
    }

    public static String stack2string(Throwable e) {
        OutputStream out = new ByteArrayOutputStream();
        PrintStream prnout = new PrintStream(out);
        e.printStackTrace(prnout);
        return out.toString();
    }

}