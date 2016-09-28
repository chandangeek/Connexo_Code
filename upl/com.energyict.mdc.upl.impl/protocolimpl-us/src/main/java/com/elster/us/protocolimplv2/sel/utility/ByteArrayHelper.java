package com.elster.us.protocolimplv2.sel.utility;

import static com.elster.us.protocolimplv2.sel.Consts.ENCODING;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public final class ByteArrayHelper {
//Prevent class from being instantiated
  private ByteArrayHelper() {}

  /**
   * Builds a string given an array of bytes
   *
   * @param bytes The bytes to convert into a String
   * @return a String containing the bytes
   */
  public static String getString(byte[] bytes) {
      String retVal;
      try {
          retVal = new String(bytes, ENCODING);
      } catch (UnsupportedEncodingException uce) {
          // TODO: logging
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
          // TODO: logging
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
