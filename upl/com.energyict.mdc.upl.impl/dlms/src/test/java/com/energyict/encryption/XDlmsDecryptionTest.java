/**
 *
 */
package com.energyict.encryption;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jme
 *
 */
public class XDlmsDecryptionTest {

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsDecryption#setSystemTitle(byte[])}.
	 */
	@Test @Ignore
	public final void testSetSystemTitle() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsDecryption#setFrameCounter(byte[])}.
	 */
	@Test @Ignore
	public final void testSetFrameCounter() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsDecryption#setGlobalKey(byte[])}.
	 */
	@Test @Ignore
	public final void testSetGlobalKey() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsDecryption#setAuthenticationKey(byte[])}.
	 */
	@Test @Ignore
	public final void testSetAuthenticationKey() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsDecryption#setCipheredText(byte[])}.
	 */
	@Test @Ignore
	public final void testSetCipheredText() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsDecryption#setAuthenticationTag(byte[])}.
	 */
	@Test @Ignore
	public final void testSetAuthenticationTag() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsDecryption#setSecurityControlByte(byte)}.
	 */
	@Test @Ignore
	public final void testSetSecurityControlByte() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsDecryption#generatePlainText()}.
	 */
	@Test
	public final void testGeneratePlainText() {

		final byte[] systemTitle = new byte[] { 0x30, 0x31, 0x35, 0x30, 0x32, 0x33};
		final byte[] FRAME_COUNTER = new byte[] { 0x00, 0x00, 0x00, 0x38 };

		byte[] ciphered = new byte[] {
				(byte) 0x34, (byte) 0x86, (byte) 0xE9, (byte) 0x77,
				(byte) 0x9A, (byte) 0x65, (byte) 0x51, (byte) 0x3B,
				(byte) 0x30, (byte) 0x5A, (byte) 0x91, (byte) 0xEE,
				(byte) 0x1F, (byte) 0x86
		};

		byte[] tag = new byte[] {
				(byte) 0xD1, (byte) 0xEE, (byte) 0x28, (byte) 0xA7,
				(byte) 0xD4, (byte) 0xAC, (byte) 0x27, (byte) 0xC1,
				(byte) 0xAD, (byte) 0x56, (byte) 0xB8, (byte) 0x02
		};

		final byte[] globalKey = new byte[] {
			(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
			(byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
			(byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B,
			(byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F
		};

		final byte[] authKey = new byte[] {
			(byte) 0xD0, (byte) 0xD1, (byte) 0xD2, (byte) 0xD3,
			(byte) 0xD4, (byte) 0xD5, (byte) 0xD6, (byte) 0xD7,
			(byte) 0xD8, (byte) 0xD9, (byte) 0xDA, (byte) 0xDB,
			(byte) 0xDC, (byte) 0xDD, (byte) 0xDE, (byte) 0xDF
		};

		final byte[] plainText = new byte[] {
			(byte) 0x08, (byte) 0x00, (byte) 0x06, (byte) 0x5F,
			(byte) 0x1F, (byte) 0x04, (byte) 0x00, (byte) 0x1C,
			(byte) 0x02, (byte) 0x20, (byte) 0x00, (byte) 0xE1,
			(byte) 0x00, (byte) 0x01
		};

		XDlmsDecryption xdlms = new XDlmsDecryption();
		xdlms.setCipheredText(ciphered);
		xdlms.setSystemTitle(systemTitle);
		xdlms.setFrameCounter(FRAME_COUNTER);
		xdlms.setGlobalKey(globalKey);
		xdlms.setAuthenticationKey(authKey);
		xdlms.setSecurityControlByte((byte) 0x30);
		xdlms.setAuthenticationTag(tag);
		assertArrayEquals(plainText, xdlms.generatePlainText());

	}

	/**
	 * Test method for {@link com.energyict.encryption.XDlmsDecryption#toString()}.
	 */
	@Test @Ignore
	public final void testToString() {
		fail("Not yet implemented"); // TODO
	}

}
