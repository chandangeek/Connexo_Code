package com.elster.us.protocolimplv2.mercury.minimax.utility;

import static com.elster.us.protocolimplv2.mercury.minimax.Consts.*;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Helper class for dealing with byte arrays
 * Defines some useful static methods
 *
 * @author James Fox
 */
public final class ByteArrayHelper {

    // Prevent class from being instantiated
    private ByteArrayHelper() {}

    /**
     * Builds a string given an array of bytes
     *
     * @param bytes The bytes to convert into a String
     * @return a String containing the bytes
     */
    public static String getString(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("byte array cannot be null");
        }
        String retVal;
        try {
            retVal = new String(bytes, ENCODING);
        } catch (UnsupportedEncodingException uce) {
            retVal = new String(bytes);
        }
        return retVal;
    }

    /**
     * Use this for every case where a string must be converted to a byte array
     * because it enforces the correct encoding
     *
     * @param str The string to be converted to bytes
     * @return an array of byte in the correct encoding
     */
    public static byte[] getBytes(String str) {
        byte[] retVal;
        try {
            retVal = str.getBytes(ENCODING);
        } catch (UnsupportedEncodingException uce) {
            retVal = str.getBytes();
        }
        return retVal;
    }

    /**
     * Check whether two byte arrays are equal
     *
     * @param array1
     * @param array2
     * @return true if they are equal, false otherwise
     */
    public static boolean arraysEqual(byte[] array1, byte[] array2) {
        return Arrays.equals(array1, array2);
    }
}
