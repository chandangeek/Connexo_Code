package com.energyict.protocolimpl.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;

/**
 * Utility class with static methods used for protocols
 *
 * @author jme
 */
public final class ProtocolTools {

	private static final int	HEX_PRESENTATION	= 16;
	private static final String	CRLF				= "\r\n";

	private ProtocolTools() {
		// Hide constructor for Util class with static methods
	}

	/**
	 * @param hexString
	 * @return
	 */
	public static byte[] getBytesFromHexString(final String hexString) {
		ByteArrayOutputStream bb = new ByteArrayOutputStream();
		for (int i = 0; i < hexString.length(); i += 3) {
			bb.write(Integer.parseInt(hexString.substring(i + 1, i + 3), 16));
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
			// Absorb
		}
	}

	/**
	 * @param profileData
	 * @return
	 */
	public static String getProfileInfo(ProfileData profileData) {
		StringBuilder sb = new StringBuilder();

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
		sb.append("Lates data: ").append(newest).append(CRLF).append(CRLF);

		return sb.toString();
	}

}
