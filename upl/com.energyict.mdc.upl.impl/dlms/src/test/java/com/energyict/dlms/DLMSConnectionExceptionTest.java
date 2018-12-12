/**
 *
 */
package com.energyict.dlms;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

/**
 * @author jme
 *
 */
public class DLMSConnectionExceptionTest {

	private static final String MESSAGE_STRING = "TestReason";
	private static final short REASON_CODE = 15;
	private static final int UNKNOWN_REASON = -1;

	/**
	 * Test method for {@link com.energyict.dlms.DLMSConnectionException#getReason()}.
	 */
	@Test
	public final void testGetReason() {
		DLMSConnectionException dce;
		dce = new DLMSConnectionException(MESSAGE_STRING, REASON_CODE);
		assertEquals(REASON_CODE, dce.getReason());
		dce = new DLMSConnectionException(MESSAGE_STRING);
		assertEquals(UNKNOWN_REASON, dce.getReason());
		dce = new DLMSConnectionException(new IOException());
		assertEquals(UNKNOWN_REASON, dce.getReason());
		dce = new DLMSConnectionException();
		assertEquals(UNKNOWN_REASON, dce.getReason());
	}

	/**
	 * Test method for {@link com.energyict.dlms.DLMSConnectionException#DLMSConnectionException(java.lang.String)}.
	 */
	@Test
	public final void testDLMSConnectionExceptionString() {
		DLMSConnectionException dce = new DLMSConnectionException(MESSAGE_STRING);
		assertEquals(MESSAGE_STRING, dce.getMessage());
		assertEquals(UNKNOWN_REASON, dce.getReason());
	}

	/**
	 * Test method for {@link com.energyict.dlms.DLMSConnectionException#DLMSConnectionException()}.
	 */
	@Test
	public final void testDLMSConnectionException() {
		DLMSConnectionException dce = new DLMSConnectionException();
		assertEquals(null, dce.getMessage());
		assertEquals(UNKNOWN_REASON, dce.getReason());
	}

	/**
	 * Test method for {@link com.energyict.dlms.DLMSConnectionException#DLMSConnectionException(java.lang.String, short)}.
	 */
	@Test
	public final void testDLMSConnectionExceptionStringShort() {
		DLMSConnectionException dce = new DLMSConnectionException(MESSAGE_STRING, REASON_CODE);
		assertEquals(MESSAGE_STRING, dce.getMessage());
		assertEquals(REASON_CODE, dce.getReason());
	}

	/**
	 * Test method for {@link com.energyict.dlms.DLMSConnectionException#DLMSConnectionException(java.lang.Throwable)}.
	 */
	@Test
	public final void testDLMSConnectionExceptionThrowable() {
		IOException ioException = new IOException(MESSAGE_STRING);
		DLMSConnectionException dce = new DLMSConnectionException(ioException);
		assertEquals(ioException.toString(), dce.getMessage());
		assertEquals(UNKNOWN_REASON, dce.getReason());
	}

}
