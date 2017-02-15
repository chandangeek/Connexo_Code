/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.messaging.proxy;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ProxyUtils {

    private ProxyUtils() {

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
     * xxxxxxxxxx (epoch time in seconds since 1 jan 1970)
     * </pre>
     *
     * @param value The date represented as text
     * @return the date or null if the value is invalid
     */
    public static Date toDate(final String value) {
        final String epochTime;
        if ((value != null) && (value.contains(":"))) {
            epochTime = value.replace("\\", "/").replace("-", "/");
            String pattern = (epochTime.indexOf("/") == 2) ? "dd/MM/yyyy " : "yyyy/MM/dd ";
            pattern += (epochTime.split(":").length == 2) ? "HH:mm Z" : "HH:mm:ss Z";
            try {
                return new SimpleDateFormat(pattern).parse(epochTime + " UTC");
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            epochTime = value.trim() + "000";
            try {
                final long epochValue = Long.valueOf(epochTime);
                return new Date(epochValue);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Derives a boolean valus from a given string. The following string contents will return true:
     * <ul>
     * <li>"1"</li>
     * <li>"true"</li>
     * <li>"enable"</li>
     * <li>"enabled"</li>
     * <li>"on"</li>
     * <li>"active"</li>
     * </ul>
     *
     * @param value The string that should be converted to a boolean
     * @return the result of the conversion
     */
    public static final boolean toBoolean(String value) {
        if (value != null) {
            boolean isTrue = false;
            String bool = value.trim();
            isTrue |= bool.equalsIgnoreCase("true");
            isTrue |= bool.equalsIgnoreCase("1");
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
     * Convert a given hex String value to a byte array
     *
     * @param value The hex string to convert to bytes
     * @return The bytes
     */
    private static byte[] toByteArray(final String value) {
        final String cleanHexString = value.toUpperCase().replaceAll("[^0-9A-F]", "");
        return ProtocolTools.getBytesFromHexString(cleanHexString, "");
    }

    /**
     * Converts a list of 1's and 0's into a boolean array, e.g. 10101 = [true, false, true, false, true]
     */
    private static boolean[] toBooleanArray(final String value) {
        final String cleanBooleanString = value.toUpperCase().replaceAll("[^0-1]", "");
        boolean[] booleans = new boolean[cleanBooleanString.length()];
        for (int i = 0; i < booleans.length; i++) {
            booleans[i] = cleanBooleanString.charAt(i) == '1';
        }
        return booleans;
    }

    /**
     * Convert a given String value to the expected return type. The following types are supported:
     * <p/>
     * {@link String}, {@link java.util.Date}, {@link java.util.TimeZone}, {@link ObisCode}, {@link Unit}, {@link java.math.BigDecimal}
     * {@link Float}, {@link Double}, {@link Long}, {@link Integer}, {@link Short}, {@link Byte} and {@link Boolean}
     *
     * @param value      The value to convert to the expected return type
     * @param returnType The expected return type
     * @return An instance of the expected return type, with the given value
     */
    public static final Object toCorrectReturnType(final String value, final Class<?> returnType) {

        if (returnType.equals(String.class)) {
            return value;
        }

        if (returnType.equals(Date.class)) {
            return ProxyUtils.toDate(value);
        }

        if (returnType.equals(TimeZone.class)) {
            return TimeZone.getTimeZone(value);
        }

        if (returnType.equals(ObisCode.class)) {
            return ObisCode.fromString(value);
        }

        if (returnType.equals(Unit.class)) {
            return Unit.get(value);
        }

        if (returnType.equals(BigDecimal.class)) {
            return new BigDecimal(value);
        }

        if (returnType.equals(float.class) || returnType.equals(Float.class)) {
            return Float.valueOf(value);
        }

        if (returnType.equals(double.class) || returnType.equals(Double.class)) {
            return Double.valueOf(value);
        }

        if (returnType.equals(long.class) || returnType.equals(Long.class)) {
            return Long.valueOf(value);
        }

        if (returnType.equals(int.class) || returnType.equals(Integer.class)) {
            return Integer.valueOf(value);
        }

        if (returnType.equals(short.class) || returnType.equals(Short.class)) {
            return Short.valueOf(value);
        }

        if (returnType.equals(byte.class) || returnType.equals(Byte.class)) {
            return Byte.valueOf(value);
        }

        if (returnType.equals(boolean.class) || returnType.equals(Boolean.class)) {
            return ProxyUtils.toBoolean(value);
        }

        if (returnType.equals(byte[].class)) {
            return ProxyUtils.toByteArray(value);
        }

        if (returnType.equals(boolean[].class)) {
            return ProxyUtils.toBooleanArray(value);
        }

        return null;
    }
}
