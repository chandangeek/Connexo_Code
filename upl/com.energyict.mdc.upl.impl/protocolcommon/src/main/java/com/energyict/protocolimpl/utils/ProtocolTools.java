package com.energyict.protocolimpl.utils;

import com.energyict.cbo.NestedIOException;
import com.energyict.cpo.Environment;
import com.energyict.mdw.core.CommunicationProtocol;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.MeteringWarehouseFactory;
import com.energyict.mdw.shadow.UserFileShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.Base64EncoderDecoder;
import com.energyict.protocolimplv2.MdcManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Utility class with static methods used for protocols
 *
 * @author jme
 */
public final class ProtocolTools {

    private static final int LOWEST_VISIBLE_VALUE = 0x20;
    private static final int PREFIX_AND_HEX_LENGTH = 3;
    private static final int HEX = 16;
    private static final int MILLIS = 1000;
    private static final int SECONDS = 60;
    private static final String CRLF = "\r\n";

    private static Logger logger = Logger.getLogger(ProtocolTools.class.getName());

    private ProtocolTools() {
        // Hide constructor for Util class with static methods
    }

    /**
     * @param hexString
     * @return
     */
    public static byte[] getBytesFromHexString(final String hexString) {
        ByteArrayOutputStream bb = new ByteArrayOutputStream();
        for (int i = 0; i < hexString.length(); i += PREFIX_AND_HEX_LENGTH) {
            bb.write(Integer.parseInt(hexString.substring(i + 1, i + PREFIX_AND_HEX_LENGTH), HEX));
        }
        return bb.toByteArray();
    }

    /**
     * Turn an integer into a byte array, with a given length.
     *
     * @param value
     * @param length
     * @return
     */
    public static byte[] getBytesFromInt(int value, int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < bytes.length; i++) {
            int ptr = (bytes.length - (i + 1));
            bytes[ptr] = (i < 4) ? (byte) ((value >> (i * 8))) : 0x00;
        }
        return bytes;
    }

    /**
     * Turn an long into a byte array, with a given length.
     *
     * @param value
     * @param length
     * @return
     */
    public static byte[] getBytesFromLong(long value, int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < bytes.length; i++) {
            int ptr = (bytes.length - (i + 1));
            bytes[ptr] = (i < 5) ? (byte) ((value >> (i * 8))) : 0x00;
        }
        return bytes;
    }

    /**
     * Convert int value to a hexadecimal string with a given byte length.
     * The given 'prefix' separator is used between each byte.
     *
     * @param value  The intValue to convert
     * @param length The amount of bytes that should be shown in the hex string
     * @param prefix The prefix that should be used in front of each byte
     * @return The hex string
     */
    public static String getHexStringFromInt(int value, int length, String prefix) {
        byte[] bytes = getBytesFromInt(value, length);
        return getHexStringFromBytes(bytes, prefix);
    }

    /**
     * Check if the n-th bit is set of the value. The bit number is 0 based, so it can range from 0 to 31.
     *
     * @param bitNumber The number of the bit to test
     * @return True if the bit was set, false if not.
     */
    public static boolean isBitSet(int value, int bitNumber) {
        return ((value >> bitNumber) & 0x01) == 1;
    }

    /**
     * Convert int value to a hexadecimal string with a given byte length.
     * The default separator '$' is used between each byte.
     *
     * @param value The intValue to convert
     * @param length The amount of bytes that should be shown in the hex string
     * @return The hex string
     */
    public static String getHexStringFromInt(int value, int length) {
        return getHexStringFromInt(value, length, "$");
    }

    /**
     * Convert int value to a hexadecimal string with a default byte length of 4.
     * The default separator '$' is used between each byte.
     *
     * @param value The intValue to convert
     * @return The hex string
     */
    public static String getHexStringFromInt(int value) {
        return getHexStringFromInt(value, 4);
    }

    /**
     * Build a String with the data representation using $ before each byte
     *
     * @param bytes data to build string from
     * @return String with representation of the data
     */
    public static String getHexStringFromBytes(final byte[] bytes) {
        return ProtocolUtils.getResponseData(bytes);
    }

    /**
     * Build a String with the data representation using $ before each byte
     *
     * @param bytes data to build string from
     * @return String with representation of the data
     */
    public static String getHexStringFromBytes(final byte[] bytes, String prefix) {
        return ProtocolUtils.getResponseData(bytes).replace("$", prefix);
    }

    /**
     * @param buffer
     * @return
     */
    public static byte[] getDataBetweenBrackets(final byte[] buffer) {
        byte[] data = new byte[0];
        int openIndex = indexOff(buffer, (byte) '(');
        int closeIndex = indexOff(buffer, (byte) ')', openIndex);
        if ((openIndex != -1) && (closeIndex != -1)) {
            data = getSubArray(buffer, openIndex + 1, closeIndex);
        }
        return data;
    }

    /**
     * @param data
     * @return
     */
    public static String getDataBetweenBrackets(final String data) {
        return new String(getDataBetweenBrackets(data.getBytes()));
    }

    /**
     * @param stringToPad
     * @param character
     * @param length
     * @param addToEnd
     * @return
     */
    public static String addPadding(final String stringToPad, final char character, final int length, final boolean addToEnd) {
        String paddedString = null;
        if (stringToPad != null) {
            int charactersToAdd = length - stringToPad.length();
            if (charactersToAdd > 0) {
                char[] charArray = new char[charactersToAdd];
                Arrays.fill(charArray, character);
                if (addToEnd) {
                    paddedString = stringToPad + new String(charArray);
                } else {
                    paddedString = new String(charArray) + stringToPad;
                }
            } else {
                paddedString = stringToPad;
            }
        }
        return paddedString;
    }

    /**
     * @param stringToPad
     * @param character
     * @param length
     * @param addToEnd
     * @return
     */
    public static String addPaddingAndClip(final String stringToPad, final char character, final int length, final boolean addToEnd) {
        String padded = addPadding(stringToPad, character, length, addToEnd);
        if (addToEnd) {
            return padded.substring(0, length);
        } else {
            return padded.substring(padded.length() - length);
        }
    }


    /**
     * @param array
     * @param index
     * @return
     */
    public static boolean isArrayIndexInRange(final byte[] array, final int index) {
        return (array != null) && (index >= 0) && (array.length > index);
    }

    /**
     * retrieve the subArray [from, to[  out of the given array
     * @param bytes
     * @param from  Inclusive from
     * @param to    Exclusive to
     * @return
     */
    public static byte[] getSubArray(final byte[] bytes, final int from, final int to) {
        byte[] subBytes;
        if (isArrayIndexInRange(bytes, from) && isArrayIndexInRange(bytes, to - 1) && (from < to)) {
            subBytes = new byte[to - from];
            for (int i = 0; i < subBytes.length; i++) {
                subBytes[i] = bytes[i + from];
            }
        } else {
            subBytes = new byte[0];
        }
        return subBytes;
    }

    /**
     * @param bytes
     * @param from
     * @return
     */
    public static byte[] getSubArray(final byte[] bytes, final int from) {
        int to = (bytes != null) ? (bytes.length) : -1;
        return getSubArray(bytes, from, to);
    }

    /**
     * @param firstArray
     * @param secondArray
     * @return
     */
    public static byte[] concatByteArrays(final byte[] firstArray, final byte[] secondArray) {
        if (firstArray == null) {
            if (secondArray == null) {
                return new byte[0];
            } else {
                return (byte[]) secondArray.clone();
            }
        } else {
            if (secondArray == null) {
                return (byte[]) firstArray.clone();
            }
        }

        byte[] bytes = new byte[firstArray.length + secondArray.length];
        System.arraycopy(firstArray, 0, bytes, 0, firstArray.length);
        System.arraycopy(secondArray, 0, bytes, firstArray.length, secondArray.length);
        return bytes;
    }

    public static Long[] concatLongArrays(final Long[] firstArray, final Long[] secondArray) {
        if (firstArray == null) {
            if (secondArray == null) {
                return new Long[0];
            } else {
                return secondArray.clone();
            }
        } else {
            if (secondArray == null) {
                return firstArray.clone();
            }
        }

        Long[] longs = new Long[firstArray.length + secondArray.length];
              System.arraycopy(firstArray, 0, longs, 0, firstArray.length);
        System.arraycopy(secondArray, 0, longs, firstArray.length, secondArray.length);
        return longs;
    }

    public static BigDecimal[] concatBigDecimalArrays(final BigDecimal[] firstArray, final BigDecimal[] secondArray) {
        if (firstArray == null) {
            if (secondArray == null) {
                return new BigDecimal[0];
            } else {
                return secondArray.clone();
    }
        } else {
            if (secondArray == null) {
                return firstArray.clone();
            }
        }

        BigDecimal[] bds = new BigDecimal[firstArray.length + secondArray.length];
        System.arraycopy(firstArray, 0, bds, 0, firstArray.length);
        System.arraycopy(secondArray, 0, bds, firstArray.length, secondArray.length);
        return bds;
    }

    /**
     * Construct a concatenated byteArray for the given ArrayList of byteArrays
     *
     * @param byteArrays the <code>byte[]</code> to concatenate
     * @return 1 <code>byte[]</code> with all given arrays after each other
     */
    public static byte[] concatListOfByteArrays(ArrayList<byte[]> byteArrays) {
        byte[] concatenatedArray = null;
        for (byte[] byteArray : byteArrays) {
            concatenatedArray = concatByteArrays(concatenatedArray, byteArray);
        }
        return concatenatedArray;
    }

    /**
     * @param buffer
     * @param value
     * @return
     */
    public static int indexOff(final byte[] buffer, final byte value) {
        return indexOff(buffer, value, 0);
    }

    /**
     * @param buffer
     * @param value
     * @param from
     * @return
     */
    public static int indexOff(final byte[] buffer, final byte value, final int from) {
        if (isArrayIndexInRange(buffer, from)) {
            for (int i = from; i < buffer.length; i++) {
                if (buffer[i] == value) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * @param hexString
     * @param prefix
     * @return
     */
    public static byte[] getBytesFromHexString(final String hexString, final String prefix) {
        int prefixLength = (prefix == null) ? 0 : prefix.length();
        int charsPerByte = prefixLength + 2;
        ByteArrayOutputStream bb = new ByteArrayOutputStream();
        for (int i = 0; i < hexString.length(); i += charsPerByte) {
            bb.write(Integer.parseInt(hexString.substring(i + prefixLength, i + charsPerByte), HEX));
        }
        return bb.toByteArray();
    }

    /**
     * @param fileName
     * @param bytes
     * @param append
     */
    public static void writeBytesToFile(final String fileName, final byte[] bytes, final boolean append) {
        writeBytesToFile(new File(fileName), bytes, append);
    }

    /**
     * @param fileName
     * @param text
     * @param append
     */
    public static void writeStringToFile(final String fileName, final String text, final boolean append) {
        writeBytesToFile(new File(fileName), text.getBytes(), append);
    }

    /**
     * @param file
     * @param bytes
     * @param append
     */
    public static void writeBytesToFile(final File file, final byte[] bytes, final boolean append) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file, append);
            os.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // Absorb
                }
            }
        }
    }

    /**
     * @param file
     * @return
     */
    public static byte[] readBytesFromFile(final File file) {
        byte[] buffer = new byte[file == null ? 0 : (int) file.length()];
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            int offset = 0;
            int numRead = 0;
            while ((offset < buffer.length) && ((numRead = is.read(buffer, offset, buffer.length - offset)) >= 0)) {
                offset += numRead;
            }
        } catch (IOException e) {
            buffer = new byte[0];
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Absorb
                }
            }
        }
        return buffer;
    }
    /**
     * @param fileName
     * @return
     */
    public static byte[] readBytesFromFile(final String fileName) {
        return readBytesFromFile(new File(fileName));
    }

    /**
     * @param millis
     */
    public static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MdcManager.getComServerExceptionFactory().communicationInterruptedException(e);
        }
    }

    /**
     * @param timeStamp
     * @param intervalInMinutes
     * @return
     */
    public static Date roundUpToNearestInterval(Date timeStamp, int intervalInMinutes) {
        long intervalMillis = (long) intervalInMinutes * MILLIS * SECONDS;

        Calendar cal = Calendar.getInstance();
        cal.setTime(timeStamp);
        if (Math.abs(intervalInMinutes) >= (31 * 24 * 60)) { // In case of monthly intervals
            cal.set(Calendar.DAY_OF_MONTH, 1);
        }
        if (Math.abs(intervalInMinutes) >=  (24 * 60) ) {    // In case of daily/monthly intervals
            cal.set(Calendar.HOUR_OF_DAY, 0);
        }

        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long diff = timeStamp.getTime() - cal.getTimeInMillis();
        long overTime = diff % intervalMillis;
        long beforeTime = intervalMillis - overTime;

        Calendar returnDate = Calendar.getInstance();
        returnDate.setTime(timeStamp);
        if (intervalInMinutes > 0) {
            returnDate.add(Calendar.SECOND, overTime != 0 ? (int) (beforeTime / 1000) : 0);         // The seconds      - split up to avoid int limit
            returnDate.add(Calendar.MILLISECOND, overTime != 0 ? (int) (beforeTime % 1000) : 0);    // The milliseconds
        } else {
            returnDate.add(Calendar.SECOND, (overTime != 0 ? (int) (overTime / 1000) : 0) * (-1));          // The seconds      - split up to avoid int limit
            returnDate.add(Calendar.MILLISECOND, (overTime != 0 ? (int) (overTime % 1000) : 0) * (-1));     // The milliseconds
        }
        return returnDate.getTime();
    }

    /**
     * Convert a given byte array into an integer
     *
     * @param byteArray a given byte array
     * @return the suiting integer
     */
    public static int getIntFromBytes(byte[] byteArray) {
        int value = 0;
        for (int i = 0; i < byteArray.length; i++) {
            int intByte = byteArray[i] & 0x0FF;
            value += intByte << ((byteArray.length - (i + 1)) * 8);
        }
        return value;
    }

    /**
     * Convert a given byte array into an integer
     */
    public static int getIntFromBytes(byte[] bytes, int offset, int length) {
        byte[] byteArray = getSubArray(bytes, offset, offset + length);
        int value = 0;
        for (int i = 0; i < byteArray.length; i++) {
            int intByte = byteArray[i] & 0x0FF;
            value += intByte << ((byteArray.length - (i + 1)) * 8);
        }
        return value;
    }

    /**
     * Creates an unsigned int value that represents a given byte array
     *
     * @param value: the given byte array
     * @return the resulting BigDecimal
     */
    public static int getUnsignedIntFromBytes(byte[] value) {
        value = ProtocolTools.concatByteArrays(new byte[]{0x00}, value);
        BigInteger convertedValue = new BigInteger(value);
        return convertedValue.intValue();
    }

    /**
     * Creates an unsigned int value that represents a given byte array.
     * Takes an offset (where to start in the byte array), and a length.
     *
     * @param value: the given byte array
     * @return the resulting BigDecimal
     */
    public static int getUnsignedIntFromBytes(byte[] value, int offset, int length) {
        value = ProtocolTools.getSubArray(value, offset, offset + length);
        value = ProtocolTools.concatByteArrays(new byte[]{0x00}, value);
        BigInteger convertedValue = new BigInteger(value);
        return convertedValue.intValue();
    }

    /*
    Same but for Little Endian order.
     */
    public static int getUnsignedIntFromBytesLE(byte[] value, int offset, int length) {
        value = ProtocolTools.getSubArray(value, offset, offset + length);
        value = ProtocolTools.getReverseByteArray(value);
        return getUnsignedIntFromBytes(value);
    }

    /**
     * Creates an unsigned BigInteger value that represents a given byte array
     *
     * @param value: the given byte array
     * @return the resulting BigInteger
     */
    public static BigInteger getUnsignedBigIntegerFromBytes(byte[] value) {
        return new BigInteger(1, value);    // Forcing the signum to positive gives you the unsigned equivalent
    }

    /**
     * Creates an unsigned BigInteger value that represents a given byte array, taking into account Little Endian byte order
     *
     * @param value: the given byte array
     * @return the resulting BigInteger
     */
    public static BigInteger getUnsignedBigIntegerFromBytesLE(byte[] value, int offset, int length) {
        value = ProtocolTools.getSubArray(value, offset, offset + length);
        value = ProtocolTools.getReverseByteArray(value);
        return getUnsignedBigIntegerFromBytes(value);
    }

    /**
     * Creates an signed BigInteger value that represents a given byte array
     *
     * @param value: the given byte array
     * @return the resulting BigInteger
     */
    public static BigInteger getSignedBigIntegerFromBytes(byte[] value) {
        return new BigInteger(value);
    }

    /**
     * Creates an signed BigInteger value that represents a given byte array, taking into account Little Endian byte order
     *
     * @param value: the given byte array
     * @return the resulting BigInteger
     */
    public static BigInteger getSignedBigIntegerFromBytesLE(byte[] value, int offset, int length) {
        value = ProtocolTools.getSubArray(value, offset, offset + length);
        value = ProtocolTools.getReverseByteArray(value);
        return getSignedBigIntegerFromBytes(value);
    }

    /**
     * Rounds down to the nearest interval or to the nearest hour if the interval is daily/monthly
     *
     * @param timeStamp
     * @param intervalInMinutes
     * @return
     */
    public static Date roundDownToNearestInterval(Date timeStamp, int intervalInMinutes) {
        return roundUpToNearestInterval(timeStamp, intervalInMinutes * (-1));
    }

    /**
     * Reverse the order of elements in a byte array
     */
    public static byte[] reverseByteArray(byte[] array) {
        if (array == null) {
            return array;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
        return array;
    }


    /**
     * @param profileData
     * @return
     */
    public static String getProfileInfo(ProfileData profileData) {
        StringBuffer sb = new StringBuffer();

        Date oldest = null;
        Date newest = null;

        List intervals = profileData.getIntervalDatas();
        for (Iterator iterator = intervals.iterator(); iterator.hasNext();) {
            IntervalData intervalData = (IntervalData) iterator.next();
            if ((oldest == null) || (newest == null)) {
                oldest = intervalData.getEndTime();
                newest = intervalData.getEndTime();
            } else {
                if (oldest.after(intervalData.getEndTime())) {
                    oldest = intervalData.getEndTime();
                }
                if (newest.before(intervalData.getEndTime())) {
                    newest = intervalData.getEndTime();
                }
            }
        }

        sb.append("Channels:   ").append(profileData.getNumberOfChannels()).append(CRLF);
        sb.append("Intervals:  ").append(profileData.getNumberOfIntervals()).append(CRLF);
        sb.append("Events:     ").append(profileData.getNumberOfEvents()).append(CRLF);
        sb.append("First data: ").append(oldest).append(CRLF);
        sb.append("Lates data: ").append(newest).append(CRLF);

        for (Object channelObject : profileData.getChannelInfos()) {
            ChannelInfo channelInfo = (ChannelInfo) channelObject;
            sb.append("[").append(channelInfo.getId()).append("]");
            sb.append("[").append(channelInfo.getChannelId()).append("] ");
            sb.append(channelInfo.getName()).append(", ");
            sb.append(channelInfo.getUnit()).append(", ");
            sb.append(channelInfo.getMultiplier()).append(", ");
            sb.append(channelInfo.getCumulativeWrapValue()).append(CRLF);
        }

        sb.append(CRLF);

        return sb.toString();
    }

    /**
     * @param intervals
     * @return
     */
    public static List<IntervalData> mergeDuplicateIntervals(List<IntervalData> intervals) {
        List<IntervalData> mergedIntervals = new ArrayList<IntervalData>();
        for (IntervalData id2compare : intervals) {
            boolean allreadyProcessed = false;
            for (IntervalData merged : mergedIntervals) {
                if (merged.getEndTime().compareTo(id2compare.getEndTime()) == 0) {
                    allreadyProcessed = true;
                    break;
                }
            }

            if (!allreadyProcessed) {
                List<IntervalData> toAdd = new ArrayList<IntervalData>();
                for (IntervalData id : intervals) {
                    if (id.getEndTime().compareTo(id2compare.getEndTime()) == 0) {
                        toAdd.add(id);
                    }
                }
                Number[] value = new Number[id2compare.getValueCount()];
                IntervalData md = new IntervalData(id2compare.getEndTime());
                for (IntervalData intervalData : toAdd) {
                    for (int i = 0; i < value.length; i++) {
                        if (value[i] == null) {
                            value[i] = intervalData.get(i);
                        } else {
                            value[i] = NumberTools.add(value[i], intervalData.get(i));
                        }
                    }
                }
                md.addValues(value);
                mergedIntervals.add(md);
            }

        }
        return mergedIntervals;
    }

    /**
     * Merge the duplicate intervals from the given list of IntervalData elements.
     * When merging multiple IntervalData elements, the Eis/protocol statuses are merged as well.
     *  @deprecated this method can only be used if the channels contain simple consumption; if the channels contain cumulative
     * values, then the merging will be wrong!
     *
     * @param intervals the list of IntervalData elements which should be checked for doubles
     * @return the merged list of IntervalData elements (which now should no longer contain duplicate intervals)
     */
    public static List<IntervalData> mergeDuplicateIntervalsIncludingIntervalStatus(List<IntervalData> intervals) {
        List<IntervalData> mergedIntervals = new ArrayList<IntervalData>();
        for (IntervalData id2compare : intervals) {
            boolean allreadyProcessed = false;
            for (IntervalData merged : mergedIntervals) {
                if (merged.getEndTime().compareTo(id2compare.getEndTime()) == 0) {
                    allreadyProcessed = true;
                    break;
                }
            }

            if (!allreadyProcessed) {
                List<IntervalData> toAdd = new ArrayList<IntervalData>();
                for (IntervalData id : intervals) {
                    if (id.getEndTime().compareTo(id2compare.getEndTime()) == 0) {
                        toAdd.add(id);
                    }
                }
                IntervalValue[] intervalValues = new IntervalValue[id2compare.getValueCount()];
                IntervalData md = new IntervalData(id2compare.getEndTime());

                for (IntervalData intervalData : toAdd) {
                    for (int i = 0; i < intervalValues.length; i++) {
                        IntervalValue intervalValue = (IntervalValue) intervalData.getIntervalValues().get(i);
                        if (intervalValues[i] == null) {
                            intervalValues[i] = intervalValue;
                        } else {
                            intervalValues[i] = new IntervalValue(
                                    NumberTools.add(intervalValues[i].getNumber(), intervalValue.getNumber()),
                                    intervalValues[i].getProtocolStatus() | intervalValue.getProtocolStatus(),
                                    intervalValues[i].getEiStatus() | intervalValue.getEiStatus()
                            );
                        }
                        md.addEiStatus(intervalData.getEiStatus());
                        md.addProtocolStatus(intervalData.getProtocolStatus());
                    }
                }

                md.setIntervalValues(Arrays.asList(intervalValues));
                mergedIntervals.add(md);
            }

        }
        return mergedIntervals;
    }

    /**
     * This method converts a byte array to a readable string. Unprintable
     * characters are replaced by a '.'
     *
     * @param b
     * @return
     */
    public static String getAsciiFromBytes(byte[] b) {
        return getAsciiFromBytes(b, '.');
    }

    /**
     * This method converts a byte array to a readable string. Unprintable
     * characters are replaced by the parameter character
     *
     * @param b
     * @return
     */
    public static String getAsciiFromBytes(byte[] b, char character) {
        if (b != null) {
            StringBuilder sb = new StringBuilder(b.length);
            for (int i = 0; i < b.length; i++) {
                if (b[i] < LOWEST_VISIBLE_VALUE) {
                    sb.append(character);
                } else {
                    sb.append((char) b[i]);
                }
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    /**
     * @param obis    The obiscode to change the given field value
     * @param fieldNr The 0-based field number of the obisCode (0 = A, 1 = B, 2 = C, 3 = D, 4 = E, 5 = F)
     * @param value   The new value of the given field
     * @return The new obis code with the changed field
     */
    public static ObisCode setObisCodeField(ObisCode obis, int fieldNr, byte value) {
        if ((obis == null) || (fieldNr < 0) || (fieldNr >= obis.getLN().length)) {
            String message = "Obis should not be null and fieldNr must be 0 <= fieldNr < 6. ";
            message += "Current values: obis=" + obis + " fieldNr=" + fieldNr;
            throw new IllegalArgumentException(message);
        }

        byte[] ln = obis.getLN();
        ln[fieldNr] = value;
        return ObisCode.fromByteArray(ln);
    }

    public static final boolean equalsIgnoreBField(final ObisCode first, final ObisCode second) {
        final ObisCode firstNoBField = setObisCodeField(first, 1, (byte) 0);
        final ObisCode secondNoBField = setObisCodeField(second, 1, (byte) 0);
        return firstNoBField.equals(secondNoBField);
    }

    /**
     * @param registerValue
     * @param obisCode
     * @return
     */
    public static RegisterValue setRegisterValueObisCode(RegisterValue registerValue, ObisCode obisCode) {
        return new RegisterValue(
                obisCode,
                registerValue.getQuantity(),
                registerValue.getEventTime(),
                registerValue.getFromTime(),
                registerValue.getToTime(),
                registerValue.getReadTime(),
                registerValue.getRtuRegisterId(),
                registerValue.getText()
        );
    }

    /**
     * Create a new instance of the Calendar with a given timestamp
     *
     * @param year
     * @param month
     * @param dayOfMonth
     * @param hourOfDay
     * @param minutes
     * @param seconds
     * @param millis
     * @return the new Calendar
     */
    public static Calendar createCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minutes, int seconds, int millis) {
        Calendar returnValue = Calendar.getInstance();
        returnValue.set(Calendar.YEAR, year);
        returnValue.set(Calendar.MONTH, month - 1);
        returnValue.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        returnValue.set(Calendar.HOUR_OF_DAY, hourOfDay);
        returnValue.set(Calendar.MINUTE, minutes);
        returnValue.set(Calendar.SECOND, seconds);
        returnValue.set(Calendar.MILLISECOND, millis);
        return returnValue;
    }

    /**
     * Create a new instance of the Calendar with a given timestamp
     *
     * @param year
     * @param month
     * @param dayOfMonth
     * @return the new Calendar
     */
    public static Calendar createCalendar(int year, int month, int dayOfMonth) {
        return createCalendar(year, month, dayOfMonth, 0, 0, 0, 0);
    }

    /**
     *
     * @param year
     * @param month
     * @param dayOfMonth
     * @param hourOfDay
     * @param minutes
     * @param seconds
     * @param millis
     * @param timeZone
     * @return
     */
    public static Calendar createCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minutes, int seconds, int millis, TimeZone timeZone) {
        Calendar returnValue = Calendar.getInstance(timeZone);
        returnValue.set(Calendar.YEAR, year);
        returnValue.set(Calendar.MONTH, month - 1);
        returnValue.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        returnValue.set(Calendar.HOUR_OF_DAY, hourOfDay);
        returnValue.set(Calendar.MINUTE, minutes);
        returnValue.set(Calendar.SECOND, seconds);
        returnValue.set(Calendar.MILLISECOND, millis);
        return returnValue;
    }

    /**
     * @param from
     * @param to
     * @param profileData
     * @return
     */
    public static ProfileData clipProfileData(Date from, Date to, ProfileData profileData) {
        ProfileData clippedProfileData = new ProfileData();
        clippedProfileData.setLoadProfileId(profileData.getLoadProfileId());
        clippedProfileData.setChannelInfos(profileData.getChannelInfos());
        clippedProfileData.setIntervalDatas(clipIntervalDatas(from, to, profileData.getIntervalDatas()));
        clippedProfileData.setMeterEvents(clipMeterEvents(from, to, profileData.getMeterEvents()));
        return clippedProfileData;
    }

    /**
     * @param from
     * @param to
     * @param meterEvents
     * @return
     */
    public static List<MeterEvent> clipMeterEvents(Date from, Date to, List<MeterEvent> meterEvents) {
        List<MeterEvent> clippedMeterEvents = new ArrayList<MeterEvent>();
        if (meterEvents != null) {
            for (int i = 0; i < meterEvents.size(); i++) {
                MeterEvent meterEvent = meterEvents.get(i);
                if ((!from.after(meterEvent.getTime())) && (to.after(meterEvent.getTime()))) {
                    clippedMeterEvents.add(meterEvent);
                }
            }
        }
        return clippedMeterEvents;
    }

    /**
     * @param from
     * @param to
     * @param intervalDatas
     * @return
     */
    public static List<IntervalData> clipIntervalDatas(Date from, Date to, List<IntervalData> intervalDatas) {
        List<IntervalData> clippedIntervalDatas = new ArrayList<IntervalData>();
        if (intervalDatas != null) {
            for (int i = 0; i < intervalDatas.size(); i++) {
                IntervalData intervalData = intervalDatas.get(i);
                if ((intervalData.getEndTime().after(from)) && (!intervalData.getEndTime().after(to))) {
                    clippedIntervalDatas.add(intervalData);
                }
            }
        }
        return clippedIntervalDatas;
    }

    /**
     * Get the epoch time as string (seconds after January 1, 1970, 00:00:00 GMT)
     * The given epochtime should be one of the following formats (Time in UTC):
     * <pre>
     * dd/MM/yyyy HH:mm:ss
     * dd\MM\yyyy HH:mm:ss
     * dd-MM-yyyy HH:mm:ss
     * yyyy/MM/dd HH:mm:ss
     * yyyy\MM\dd HH:mm:ss
     * yyyy-MM-dd HH:mm:ss
     * dd/MM/yyyy HH:mm
     * dd\MM\yyyy HH:mm
     * dd-MM-yyyy HH:mm
     * yyyy/MM/dd HH:mm
     * yyyy\MM\dd HH:mm
     * yyyy-MM-dd HH:mm
     * </pre>
     *
     * @param epochTime
     * @return
     */
    public static String getEpochTimeFromString(String epochTime) {
        if ((epochTime != null) && (epochTime.contains(":"))) {
            epochTime = epochTime.replace("\\", "/").replace("-", "/");
            String pattern = (epochTime.indexOf("/") == 2) ? "dd/MM/yyyy " : "yyyy/MM/dd ";
            pattern += (epochTime.split(":").length == 2) ? "HH:mm Z" : "HH:mm:ss Z";
            try {
                return String.valueOf(new SimpleDateFormat(pattern).parse(epochTime + " UTC").getTime() / 1000);
            } catch (ParseException e) {
            }
        }
        return epochTime;
    }

    /**
     * Get the epoch time as Date (seconds after January 1, 1970, 00:00:00 GMT)
     * Returns null if the epoch time was in an incorrect format.
     * The given epochtime should be one of the following formats (Time in UTC):
     * <pre>
     * dd/MM/yyyy HH:mm:ss
     * dd\MM\yyyy HH:mm:ss
     * dd-MM-yyyy HH:mm:ss
     * yyyy/MM/dd HH:mm:ss
     * yyyy\MM\dd HH:mm:ss
     * yyyy-MM-dd HH:mm:ss
     * dd/MM/yyyy HH:mm
     * dd\MM\yyyy HH:mm
     * dd-MM-yyyy HH:mm
     * yyyy/MM/dd HH:mm
     * yyyy\MM\dd HH:mm
     * yyyy-MM-dd HH:mm
     * </pre>
     *
     * @param epochTime
     * @return
     */
    public static Date getEpochDateFromString(String epochTime) {
        String time = getEpochTimeFromString(epochTime);
        try {
            return new Date(Long.valueOf(time) * 1000);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Construct a concatenated byteArray for the given byteArrays
     *
     * @param byteArrays the <code>byte[]</code> to concatenate
     * @return 1 <code>byte[]</code> with all given arrays after each other
     */
    public static byte[] concatByteArrays(byte[]... byteArrays) {
        int length = 0;
        for (byte[] byteArray : byteArrays) {
            if (byteArray != null) {
                length += byteArray.length;
            }
        }
        int offset = 0;
        byte[] concatenatedArray = new byte[length];
        for (byte[] byteArray : byteArrays) {
            if (byteArray != null) {
                System.arraycopy(byteArray, 0, concatenatedArray, offset, byteArray.length);
                offset += byteArray.length;
            }
        }
        return concatenatedArray;
    }

    public static byte[] addOneToByteArray(byte[] byteArray) {
        if ((byteArray != null) && (byteArray.length > 0)) {
            byte[] bytes = byteArray.clone();
            int value = (bytes[bytes.length - 1] & 0x0FF) + 1;
            if (value > 0x0FF) {
                byte[] zero = {0x00};
                byte[] subResult = getSubArray(bytes, 0, bytes.length - 1);
                subResult = addOneToByteArray(subResult);
                return concatByteArrays(subResult, zero);
            } else {
                bytes[bytes.length - 1] = (byte) (value & 0x0FF);
                return bytes;
            }
        } else {
            return new byte[0];
        }

    }

    /**
     * Get an int from a property, and check for invalid values (resulting in a NumberFormatException)
     * Throws a InvalidPropertyException is the property does not exist, and no default value is given,
     * or if the value is not a number.
     *
     * @param properties
     * @param key
     * @param defaultValue
     * @return
     * @throws InvalidPropertyException
     */
    public static int getPropertyAsInt(Properties properties, String key, String defaultValue) throws InvalidPropertyException {
        String value = properties.getProperty(key, defaultValue);
        if (value == null) {
            throw new InvalidPropertyException("Property [" + key + "] returned 'null'");
        } else {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new InvalidPropertyException("Property [" + key + "] is not a number. Contains [" + value + "]");
            }
        }
    }

    /**
     * Get the properties for a given rtu. This incluides the protocol properties
     *
     * @param rtu
     * @return
     */
    public static Properties getRtuProperties(Device rtu) {
        Properties properties = new Properties();
        if (rtu != null) {
            CommunicationProtocol protocol = rtu.getDeviceType().getProtocol();
            if (protocol != null) {
                properties.putAll(protocol.getProperties().toStringProperties());
            }
            properties.putAll(rtu.getProtocolProperties().toStringProperties());
        }
        return properties;
    }

    public static byte[] getReverseByteArray(byte[] bytes) {
        byte[] reverseBytes = new byte[bytes != null ? bytes.length : 0];
        for (int i = 0; i < reverseBytes.length; i++) {
            reverseBytes[i] = bytes[bytes.length - (i+1)];
        }
        return reverseBytes;
    }

    /**
     * Checks if all characters in a given string are numbers (0-9)
     * For example: "19658" will return true, "12A4" and "-16599" will return false
     * An empty string will return true. The stringToCheck cannot be null.
     *
     * @param stringToCheck This is the string we would like to check if it's a number.
     * @return true if its a string only containing digits (0-9)
     */
    public static boolean isNumber(String stringToCheck) {
        for (char c : stringToCheck.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Derives a boolean valus from a given string. The following string contents will return true:
     * <ul>
     * <li>"1"</li>
     * <li>"true"</li>
     * <li>"yes"</li>
     * <li>"enable"</li>
     * <li>"enabled"</li>
     * <li>"on"</li>
     * <li>"active"</li>
     * </ul>
     *
     * @param boolAsString The string that should be converted to a boolean
     * @return the result of the conversion
     */
    public static boolean getBooleanFromString(String boolAsString) {
        if (boolAsString != null) {
            boolean isTrue = false;
            String bool = boolAsString.trim();
            isTrue |= bool.equalsIgnoreCase("true");
            isTrue |= bool.equalsIgnoreCase("1");
            isTrue |= bool.equalsIgnoreCase("yes");
            isTrue |= bool.equalsIgnoreCase("enable");
            isTrue |= bool.equalsIgnoreCase("enabled");
            isTrue |= bool.equalsIgnoreCase("on");
            isTrue |= bool.equalsIgnoreCase("active");
            return isTrue;
        } else {
            return false;
        }
    }

    /**
     * Generate a new date object, derived from the given string.
     * This string should allways have the following format: yyyy-MM-dd hh:mm:ss
     *
     * @param yyyyMMddhhmmss the date in string format (yyyy-MM-dd hh:mm:ss) or null if the string vas invalid
     * @return The new date object
     */
    public static Date getDateFromYYYYMMddhhmmss(String yyyyMMddhhmmss) {
        try {
            if (yyyyMMddhhmmss != null) {
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(yyyyMMddhhmmss);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTime();
            } else {
                return null;
            }
        } catch (ParseException e) {
            return null;
        }
    }

    public static boolean isInDST(Calendar calendar) {
        return calendar.getTimeZone().inDaylightTime(calendar.getTime());

    }

    /**
     * @param value
     * @return
     */
    public static boolean isEven(int value) {
        return (value % 2) == 0;
    }

    /**
     * @param value
     * @return
     */
    public static boolean isOdd(int value) {
        return !isEven(value);
    }

    /**
     * Creates an int value from a byte. The byte is handled as an unsigned value,
     * this means that for example 0xFF will return 255, and no negative value.
     *
     * @param b the byte to convert
     * @return the converted positive value from 0-255
     */
    public static int getIntFromByte(byte b) {
        return ((int) b) & 0x0FF;
    }

    public static String compress(String uncompressedContent) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(gzipOutputStream);
        objectOutputStream.writeObject(uncompressedContent);
        objectOutputStream.flush();
        objectOutputStream.close();
        return new Base64EncoderDecoder().encode(byteArrayOutputStream.toByteArray());
    }

    /**
     * Decompress the Zipped AND Base64 encoded String
     *
     * @param compressedBase64Content the Zipped and Base64 encoded String
     * @return the decompressed String
     * @throws IOException if decompression failed
     */
    public static String decompress(String compressedBase64Content) throws IOException {
        try {
            byte[] compressedContent = new Base64EncoderDecoder().decode(compressedBase64Content);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedContent);
            GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(gzipInputStream);
            Object object = objectInputStream.readObject();
            if (object instanceof String) {
                return (String) object;
            } else {
                throw new ProtocolException("Compressed object should be a java.lang.String but was [" + object.getClass().getName() + "]");
            }
        } catch (ClassNotFoundException e) {
            throw new ProtocolException(e.getMessage());
        }
    }

    /**
     * Decompress the Zipped AND Base64 encoded String to a byte array
     *
     * @param compressedBase64Content the Zipped and Base64 encoded String
     * @return the decompressed byte array
     * @throws IOException if decompression failed
     */
    public static byte[] decompressBytes(String compressedBase64Content) throws IOException {
        try {
            byte[] compressedContent = new Base64EncoderDecoder().decode(compressedBase64Content);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedContent);
            GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(gzipInputStream);
            Object object = objectInputStream.readObject();
            if (object instanceof byte[]) {
                return (byte[]) object;
            } else {
                throw new ProtocolException("Compressed object should be a byte[] but was [" + object.getClass().getName() + "]");
            }
        } catch (ClassNotFoundException e) {
            throw new ProtocolException(e.getMessage());
        }
    }

    /**
     * Create a UserFileShadow with the given parameters.
     *
     * @param name      the name of the userfile
     * @param content   the content of the userfile
     * @param folderId  the folderId where to put the userFile
     * @param extension the extension of the userfile
     * @return the expected UserFileShadow
     * @throws IOException if an error occurred during the creation of the file
     */
    public static UserFileShadow createUserFileShadow(String name, byte[] content, int folderId, String extension) throws IOException {
        UserFileShadow ufs = new UserFileShadow();
        ufs.setName(name);
        ufs.setExtension(extension);
        ufs.setFolderId(folderId);
        File file = File.createTempFile("TempUserFile", extension);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content);
        fos.close();
        file.deleteOnExit();
        ufs.setFile(file);
        return ufs;
    }

    /**
     * Check if the ipAddress contains a PortNumber, if not then add the given one
     *
     * @param ipAddress
     * @param portNumber
     * @return the ipAddress concatenated with a colon and the portNumber
     */
    public static String checkIPAddressForPortNumber(String ipAddress, String portNumber) {
        if (!ipAddress.contains(":")) {
            StringBuffer strBuff = new StringBuffer();
            strBuff.append(ipAddress);
            strBuff.append(":");
            strBuff.append(portNumber);
            return strBuff.toString();
        }
        return ipAddress;
    }

    /**
     * Short notation for MeteringWarehouse.getCurrent()
     */
    public static MeteringWarehouse mw() {
        MeteringWarehouse result = MeteringWarehouse.getCurrent();
        if (result == null) {
            return new MeteringWarehouseFactory().getBatch(false);
        } else {
            return result;
        }
    }

    /**
     * Close the databaseConnection when it is not needed so optimal database pooling can be used
     */
    public static void closeConnection() {
        if(!Environment.getDefault().isInTransaction()){
            Environment.getDefault().closeConnection();
        }
    }

    public static boolean isCorrectIntervalBoundary(Calendar toDate, int intervalInSeconds) {
        if (toDate == null) {
            return false;
        }
        toDate.set(Calendar.MILLISECOND, 0);
        int toDateSec = toDate.get(Calendar.MINUTE) * 60;
        toDateSec += toDate.get(Calendar.SECOND);
        for (int sec = 0; (sec < (3600)) || (sec < (toDateSec * 60)); sec += intervalInSeconds) {
            if (toDateSec == sec) {
                return true;
            }
        }
        return false;
    }

    public static String getFormattedDate(String format, Date dateToFormat) {
        return new SimpleDateFormat(format).format((dateToFormat == null) ? new Date() : dateToFormat);
    }

    public static String getFormattedDate(String format) {
        return getFormattedDate(format, null);
    }

    public static void closeQuietly(Closeable output) {
        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    /**
     * Deserialize a given base64 string to the original object
     *
     * @param base64 The BASE64 encoded string, containing the object to deserialize
     * @param <T>    The expected class (should implement Serializable interface)
     * @return The new deserialized object
     * @throws IOException If there went something wrong while deserializing the base64 string
     */
    public static final <T extends Serializable> T deserializeFromBase64(final String base64) {

        ByteArrayInputStream byteStream = null;
        ObjectInputStream objectStream = null;
        try {
            final byte[] rawBytes = new Base64EncoderDecoder().decode(base64);
            byteStream = new ByteArrayInputStream(rawBytes);
            objectStream = new ObjectInputStream(byteStream);
            return (T) objectStream.readObject();
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Unable to deserialize from base64 string. Got ClassNotFoundException while deserializing: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to deserialize from base64 string. Got IOException while deserializing: " + e.getMessage(), e);
        } finally {
            closeQuietly(byteStream);
            closeQuietly(objectStream);
        }

        return null;
    }

    /**
     * Serialize a given object top a BASE64 encoded string
     *
     * @param object The object that should be serialized
     * @param <T>    The type of class that should be serialized (should implement Serializable interface)
     * @return The base64 encoded serialized object as string
     * @throws IOException If there went something wrong while serializing the object
     */
    public static final <T extends Serializable> String serializeToBase64(final T object) {
        if (object == null) {
            throw new IllegalArgumentException("Unable to serialize 'null' to base64!");
        }

        ByteArrayOutputStream byteStream = null;
        ObjectOutputStream objectStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            objectStream = new ObjectOutputStream(byteStream);
            objectStream.writeObject(object);
            objectStream.flush();
            return new Base64EncoderDecoder().encode(byteStream.toByteArray());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to serialize to base64 string. Got ClassNotFoundException while serializing: " + e.getMessage(), e);
        } finally {
            closeQuietly(byteStream);
            closeQuietly(objectStream);
        }

        return null;
    }

    /**
     * Get the root cause of a {@link com.energyict.cbo.NestedIOException}
     *
     * @param e the NestedIOException, for which the root cause should be extracted
     * @return the {@link Throwable}, which is the root cause
     */
    public static Throwable getRootCause(NestedIOException e) {
        Throwable cause = e.getCause();
        while (cause instanceof NestedIOException) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * Visualise the given number taking into account the maximum number of characters the visualisation can use.<br></br>
     * If the visualisation would take more characters, the number is rounded.
     *
     * @param value The Number to format
     * @param maximumNumberOfChars The maximum number of (non-negative) characters to limit the number to<br></br>
     * Examples:<br></br>
     *  <ul>
     *      <li>visualiseFormattedNumber(123.456f, 5) = 123.4</li>
     *      <li>visualiseFormattedNumber(-123.456f, 5) = -123.4</li>
     *      <li>visualiseFormattedNumber(12345678, 5) = 12345678</li>
     *  </ul>
     *
     * @return the formatted number visualisation
     */
    public static String visualiseFormattedNumber(Number value, int maximumNumberOfChars) {
        MathContext mathContext = new MathContext(maximumNumberOfChars -1, RoundingMode.HALF_DOWN);
        BigDecimal bigDecimal = new BigDecimal(value.toString(), mathContext);
        return bigDecimal.toPlainString();
    }

    /**
     * Remove all leading zeros from a given string.
     * If the text contains only zeros, "0" will be returned;
     */
    public static String removeLeadingZerosFromString(String text) {
        text = text.replaceFirst("^0*", "");
        if (text.isEmpty()) {
            text = "0";
        }
        return text;
    }
}
