package com.energyict.protocolimpl.utils;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private static final int HEX_PRESENTATION = HEX;
    private static final String CRLF = "\r\n";

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
     * @param array
     * @param index
     * @return
     */
    public static boolean isArrayIndexInRange(final byte[] array, final int index) {
        return (array != null) && (index >= 0) && (array.length > index);
    }

    /**
     * @param bytes
     * @param from
     * @param to
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
            bb.write(Integer.parseInt(hexString.substring(i + prefixLength, i + charsPerByte), HEX_PRESENTATION));
        }
        return bb.toByteArray();
    }

    /**
     * @param fileName
     * @param bytes
     * @param append
     */
    public static void writeBytesToFile(final String fileName, final byte[] bytes, final boolean append) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(fileName, append);
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
     * @param fileName
     * @return
     */
    public static byte[] readBytesFromFile(final String fileName) {
        File file = new File(fileName);
        byte[] buffer = new byte[(int) file.length()];
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
     * @param millis
     */
    public static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new ProtocolInterruptedException(e);
        }
    }

    /**
     * @param timeStamp
     * @param intervalInMinutes
     * @return
     */
    public static Date roundUpToNearestInterval(Date timeStamp, int intervalInMinutes) {
        int intervalMillis = intervalInMinutes * MILLIS * SECONDS;

        Calendar cal = Calendar.getInstance();
        cal.setTime(timeStamp);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long diff = timeStamp.getTime() - cal.getTimeInMillis();
        long overTime = diff % intervalMillis;
        long beforeTime = intervalMillis - overTime;

        Calendar returnDate = Calendar.getInstance();
        returnDate.setTime(timeStamp);
        if (intervalInMinutes > 0) {
            returnDate.add(Calendar.MILLISECOND, overTime != 0 ? (int) beforeTime : 0);
        } else {
            returnDate.add(Calendar.MILLISECOND, (overTime != 0 ? (int) overTime : 0) * (-1));
        }

        return returnDate.getTime();
    }

    /**
     * @param timeStamp
     * @param intervalInMinutes
     * @return
     */
    public static Object roundDownToNearestInterval(Date timeStamp, int intervalInMinutes) {
        return roundUpToNearestInterval(timeStamp, intervalInMinutes * (-1));
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
     * This method converts a byte array to a readable string. Unprintable
     * characters are replaced by a '.'
     *
     * @param b
     * @return
     */
    public static String getAsciiFromBytes(byte[] b) {
        if (b != null) {
            StringBuilder sb = new StringBuilder(b.length);
            for (int i = 0; i < b.length; i++) {
                if (b[i] < LOWEST_VISIBLE_VALUE) {
                    sb.append(".");
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
     * @param obis
     * @param fieldNr
     * @param value
     * @return
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
                if ((!from.after(intervalData.getEndTime())) && (to.after(intervalData.getEndTime()))) {
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

}
