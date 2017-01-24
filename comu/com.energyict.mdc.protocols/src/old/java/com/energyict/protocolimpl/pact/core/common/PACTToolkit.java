/*
 * PACTToolkit.java
 *
 * Created on 1 april 2004, 11:14
 */

package com.energyict.protocolimpl.pact.core.common;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 *
 * @author Koen
 */
public class PACTToolkit {

	private static final int DEBUG = 0;

	static {
		System.loadLibrary("PACTtoolkitWrapper");
	}

	final String[] RESULTCODES = { "Invalid time", // -1
			"Invalid key supplied", // -2
			"Invalid COP5 level", // -3
			"Buffer not allocated", // -4
			"Bad survey blocks", // -5
			"Bad readings blocks or file not found", // -6
			"Readings decryption failed", // -7
			"Survey decryption failed", // -8
			"Allocation error", // -9
			"Authentication failed (probably missing or wrong password)", // -10
			"Readings not encrypted", // -11
			"Readings fail, survey ok", // -12
			"Readings ok, survey fail", // -13
			"Bad output file" }; // -14

	private static final int MAX_ERROR_CODE = -14;

	private int highKeyRef;
	private int lowKey;
	private String highKey;

	/** Creates a new instance of PACTToolkit */
	public PACTToolkit(int highKeyRef, String highKey, int lowKey) {
		this.highKeyRef = highKeyRef;
		this.highKey = highKey;
		this.lowKey = lowKey;
	}

	public byte[] generateTimeSetMessage(Calendar calendar, int oldTimeSeed, int newTimeseed) throws IOException {
		byte[] frame = new byte[9];
		int retval = generateTimeSetMessage(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar
				.get(Calendar.SECOND), calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH) + 1, calendar
				.get(Calendar.YEAR), highKeyRef, highKey, lowKey, oldTimeSeed, newTimeseed, // KV 07082006 changed cause
																							// the ICM200 needs
																							// newTimeseed set to 0
				frame);

		if (DEBUG >= 1) {
			System.out.println("KV_DEBUG> HOUR_OF_DAY=" + calendar.get(Calendar.HOUR_OF_DAY));
		}
		if (DEBUG >= 1) {
			System.out.println("KV_DEBUG> MINUTE=" + calendar.get(Calendar.MINUTE));
		}
		if (DEBUG >= 1) {
			System.out.println("KV_DEBUG> SECOND=" + calendar.get(Calendar.SECOND));
		}
		if (DEBUG >= 1) {
			System.out.println("KV_DEBUG> DATE=" + calendar.get(Calendar.DATE));
		}
		if (DEBUG >= 1) {
			System.out.println("KV_DEBUG> MONTH=" + (calendar.get(Calendar.MONTH) + 1));
		}
		if (DEBUG >= 1) {
			System.out.println("KV_DEBUG> YEAR=" + calendar.get(Calendar.YEAR));
		}
		if (DEBUG >= 1) {
			System.out.println("KV_DEBUG> highKeyRef=" + highKeyRef);
		}
		if (DEBUG >= 1) {
			System.out.println("KV_DEBUG> highKey=" + highKey);
		}
		if (DEBUG >= 1) {
			System.out.println("KV_DEBUG> lowKey=" + lowKey);
		}
		if (DEBUG >= 1) {
			System.out.println("KV_DEBUG> old timeSeed=0x" + Integer.toHexString(oldTimeSeed));
		}
		if (DEBUG >= 1) {
			System.out.println("KV_DEBUG> new timeSeed=0x" + Integer.toHexString(newTimeseed)
					+ " (should be kept the same as old timeseed following the doc i have)");
		}
		if (DEBUG >= 1) {
			System.out.println("KV_DEBUG> frame=" + ProtocolUtils.outputHexString(frame));
		}

		if ((retval < 0) && (retval >= MAX_ERROR_CODE)) {
			throw new IOException("PACTToolkit, generateTimeSetMessage, error " + retval + " ("
					+ RESULTCODES[(retval * -1) - 1] + "), unable to generate timeset message!");
		} else if (retval < MAX_ERROR_CODE) {
			throw new IOException("PACTToolkit, generateTimeSetMessage, error " + retval
					+ ", unable to generate timeset message!");
		} else {
			if (DEBUG >= 1) {
				System.out.println("KV_DEBUG> generateTimeSetMessage(" + calendar.getTime() + ")");
			}
			return frame;
		}
	}

	public int validateData(String fileName) throws IOException {
		int retval = authenticateReadings(getFullPath(fileName), highKeyRef, highKey, lowKey);
		if ((retval < 0) && (retval >= MAX_ERROR_CODE)) {
			throw new IOException("PACTToolkit, validateData, error " + retval + " (" + RESULTCODES[(retval * -1) - 1]
					+ "), unable to validate data!");
		} else if (retval < MAX_ERROR_CODE) {
			throw new IOException("PACTToolkit, validateData, error " + retval + ", unable to validate data!");
		} else {
			if (DEBUG >= 1) {
				System.out.println("KV_DEBUG> validateData(" + fileName + ")");
			}
			return retval;
		}
	}

	private String getFullPath(String fileName) {
		File file = new File(fileName);
		return file.getAbsolutePath();
	}

	private native int authenticateReadings(String fileName, int highKeyRef, String highKey, int lowKey);

	private native int generateTimeSetMessage(int hour, int min, int second, int date, int month, int year,
			int highKeyRef, String highKey, int lowKey, int oldTimeSeed, int newTimeSeed, byte[] frame);

	public native String getVersion();

}
