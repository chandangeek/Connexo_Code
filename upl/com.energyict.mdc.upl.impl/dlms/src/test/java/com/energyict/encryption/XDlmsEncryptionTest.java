/**
 *
 */
package com.energyict.encryption;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jme
 *
 */
public class XDlmsEncryptionTest {

	private static final byte[] SYSTEM_TITLE = new byte[] { 0x4D, 0x4D, 0x4D, 0x00, 0x00, (byte) 0xBC, 0x61, 0x4E };
	private static final byte[] FRAME_COUNTER = new byte[] { 0x01, 0x23, 0x45, 0x67 };

	private static final byte[] GLOBAL_KEY = new byte[] {
		(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
		(byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
		(byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B,
		(byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F
	};

	private static final byte[] AUTHENTICATION_KEY = new byte[] {
		(byte) 0xD0, (byte) 0xD1, (byte) 0xD2, (byte) 0xD3,
		(byte) 0xD4, (byte) 0xD5, (byte) 0xD6, (byte) 0xD7,
		(byte) 0xD8, (byte) 0xD9, (byte) 0xDA, (byte) 0xDB,
		(byte) 0xDC, (byte) 0xDD, (byte) 0xDE, (byte) 0xDF
	};

	private static final byte[] PLAIN_TEXT = new byte[] {
		(byte) 0x01, (byte) 0x01, (byte) 0x10, (byte) 0x00,
		(byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44,
		(byte) 0x55, (byte) 0x66, (byte) 0x77, (byte) 0x88,
		(byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC,
		(byte) 0xDD, (byte) 0xEE, (byte) 0xFF, (byte) 0x00,
		(byte) 0x00, (byte) 0x06, (byte) 0x5F, (byte) 0x1F,
		(byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x7E,
		(byte) 0x1F, (byte) 0x04, (byte) 0xB0
	};

	private static final byte[] CIPHERED = new byte[] {
		(byte) 0x21, (byte) 0x30, (byte) 0x30, (byte) 0x01,
		(byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x80,
		(byte) 0x13, (byte) 0x02, (byte) 0xFF, (byte) 0x8A,
		(byte) 0x78, (byte) 0x74, (byte) 0x13, (byte) 0x3D,
		(byte) 0x41, (byte) 0x4C, (byte) 0xED, (byte) 0x25,
		(byte) 0xB4, (byte) 0x25, (byte) 0x34, (byte) 0xD2,
		(byte) 0x8D, (byte) 0xB0, (byte) 0x04, (byte) 0x77,
		(byte) 0x20, (byte) 0x60, (byte) 0x6B, (byte) 0x17,
		(byte) 0x5B, (byte) 0xD5, (byte) 0x22, (byte) 0x11,
		(byte) 0xBE, (byte) 0x68, (byte) 0x41, (byte) 0xDB,
		(byte) 0x20, (byte) 0x4D, (byte) 0x39, (byte) 0xEE,
		(byte) 0x6F, (byte) 0xDB, (byte) 0x8E, (byte) 0x35,
		(byte) 0x68, (byte) 0x55
	};
	private static final int	SYSTEM_TITLE_LENGTH	= 8;

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsEncryption#setSystemTitle(byte[])}.
	 */
	@Test
	public final void testSetSystemTitle() {
		XDlmsEncryption xdlms = new XDlmsEncryption();
		try {
			xdlms.setSystemTitle(null);
			fail("Previous method call should have thrown an exception");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}

		for (int i = 0; i < 0xFF; i++) {
			if (i == SYSTEM_TITLE_LENGTH) {
				try {
					xdlms.setSystemTitle(new byte[i]);
				} catch (Exception e) {
					fail("Previous method call should not throw an exception!");
				}
			} else {
				try {
					xdlms.setSystemTitle(new byte[i]);
					fail("Previous method call should have thrown an exception when passing new byte["+i+"] as argument");
				} catch (Exception e) {
					assertTrue(e instanceof IllegalArgumentException);
				}
			}
		}


	}

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsEncryption#setFrameCounter(byte[])}.
	 */
	@Test @Ignore
	public final void testSetFrameCounter() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsEncryption#setGlobalKey(byte[])}.
	 */
	@Test @Ignore
	public final void testSetGlobalKey() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsEncryption#setAuthenticationKey(byte[])}.
	 */
	@Test @Ignore
	public final void testSetAuthenticationKey() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsEncryption#setPlainText(byte[])}.
	 */
	@Test @Ignore
	public final void testSetPlainText() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsEncryption#setSecurityControlByte(byte)}.
	 */
	@Test @Ignore
	public final void testSetSecurityControlByte() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsEncryption#generateCipheredAPDU()}.
	 */
	@Test
	public final void testGenerateCipheredAPDU() {
		XDlmsEncryption xdlms = new XDlmsEncryption();
		xdlms.setSystemTitle(SYSTEM_TITLE);
		xdlms.setFrameCounter(FRAME_COUNTER);
		xdlms.setGlobalKey(GLOBAL_KEY);
		xdlms.setAuthenticationKey(AUTHENTICATION_KEY);
		xdlms.setPlainText(PLAIN_TEXT);
		xdlms.setSecurityControlByte((byte) 0x30);
		assertArrayEquals(CIPHERED, xdlms.generateCipheredAPDU());
	}

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsEncryption#toString()}.
	 */
	@Test @Ignore
	public final void testToString() {
		fail("Not yet implemented"); // TODO
	}

}
