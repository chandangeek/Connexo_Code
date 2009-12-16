/**
 *
 */
package com.energyict.protocolimpl.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jme
 *
 */
public class ProtocolToolsTest {

	private static final byte[]	BYTE_ARRAY				= new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 };
	private static final String	BYTE_ARRAY_AS_STRING	= "$00$01$02$03$04$05";
	private static final String	BYTE_ARRAY_AS_STRING_0x	= " 0x00 0x01 0x02 0x03 0x04 0x05";
	private static final String	CUSTOM_PREFIX			= " 0x";
	private static final String	DEFAULT_PREFIX			= "$";
	private static final int	PADDING_TEST_LENGTH		= 20;
	private static final char	PADDING_TEST_CHARACTER	= '-';
	private static final String	PADDING_TEST_STRING		= "123";

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getBytesFromHexString(java.lang.String)}.
	 */
	@Test
	public final void testGetBytesFromHexStringString() {
		assertNotNull(ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING));
		assertTrue(Arrays.equals(BYTE_ARRAY, ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING)));
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getHexStringFromBytes(byte[])}.
	 */
	@Test
	public final void testGetHexStringFromBytes() {
		assertNotNull(ProtocolTools.getHexStringFromBytes(BYTE_ARRAY));
		assertEquals(BYTE_ARRAY_AS_STRING, ProtocolTools.getHexStringFromBytes(BYTE_ARRAY));
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getDataBetweenBrackets(byte[])}.
	 */
	@Test
	public final void testGetDataBetweenBracketsByteArray() {
		assertTrue(Arrays.equals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets("123(123456)321".getBytes())));
		assertTrue(Arrays.equals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets("(123456)321".getBytes())));
		assertTrue(Arrays.equals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets("(123456)".getBytes())));
		assertTrue(Arrays.equals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets(")(123456)".getBytes())));
		assertTrue(Arrays.equals("(123456".getBytes(), ProtocolTools.getDataBetweenBrackets("((123456)".getBytes())));
		assertTrue(Arrays.equals("".getBytes(), ProtocolTools.getDataBetweenBrackets("()123456)".getBytes())));
		assertTrue(Arrays.equals("".getBytes(), ProtocolTools.getDataBetweenBrackets(")123456)".getBytes())));
		assertTrue(Arrays.equals("".getBytes(), ProtocolTools.getDataBetweenBrackets(")(123456".getBytes())));
		assertTrue(Arrays.equals("".getBytes(), ProtocolTools.getDataBetweenBrackets(")(123456".getBytes())));
		assertTrue(Arrays.equals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets("(123456)(123)".getBytes())));
		assertTrue(Arrays.equals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets("123)(123456)".getBytes())));
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getDataBetweenBrackets(java.lang.String)}.
	 */
	@Test
	public final void testGetDataBetweenBracketsString() {
		assertEquals("123456", ProtocolTools.getDataBetweenBrackets("123(123456)321"));
		assertEquals("123456", ProtocolTools.getDataBetweenBrackets("(123456)321"));
		assertEquals("123456", ProtocolTools.getDataBetweenBrackets("(123456)"));
		assertEquals("123456", ProtocolTools.getDataBetweenBrackets(")(123456)"));
		assertEquals("(123456", ProtocolTools.getDataBetweenBrackets("((123456)"));
		assertEquals("", ProtocolTools.getDataBetweenBrackets("()123456)"));
		assertEquals("", ProtocolTools.getDataBetweenBrackets(")123456)"));
		assertEquals("", ProtocolTools.getDataBetweenBrackets(")(123456"));
		assertEquals("", ProtocolTools.getDataBetweenBrackets(")(123456"));
		assertEquals("123456", ProtocolTools.getDataBetweenBrackets("(123456)(123)"));
		assertEquals("123456", ProtocolTools.getDataBetweenBrackets("123)(123456)"));
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#addPadding(java.lang.String, char, int, boolean)}.
	 */
	@Test
	public final void testAddPadding() {

		assertNotNull(ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, 0, false));
		assertNotNull(ProtocolTools.addPadding("", PADDING_TEST_CHARACTER, 0, false));

		assertEquals(null, ProtocolTools.addPadding(null, PADDING_TEST_CHARACTER, 0, false));
		assertEquals(null, ProtocolTools.addPadding(null, PADDING_TEST_CHARACTER, PADDING_TEST_LENGTH, false));

		assertEquals(PADDING_TEST_STRING.length(), ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, 0, false).length());
		assertEquals(PADDING_TEST_STRING.length(), ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, PADDING_TEST_STRING.length(), false).length());
		assertEquals(PADDING_TEST_LENGTH, ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, PADDING_TEST_LENGTH, false).length());

		assertEquals(PADDING_TEST_STRING.length(), ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, 0, true).length());
		assertEquals(PADDING_TEST_STRING.length(), ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, PADDING_TEST_STRING.length(), true).length());
		assertEquals(PADDING_TEST_LENGTH, ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, PADDING_TEST_LENGTH, true).length());

		assertEquals(0, ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, PADDING_TEST_LENGTH, true).indexOf(PADDING_TEST_STRING));
		assertEquals(PADDING_TEST_LENGTH - PADDING_TEST_STRING.length(), ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, PADDING_TEST_LENGTH, false).indexOf(PADDING_TEST_STRING));

		assertEquals(PADDING_TEST_STRING.length(), ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, PADDING_TEST_LENGTH, true).indexOf(PADDING_TEST_CHARACTER));
		assertEquals(0, ProtocolTools.addPadding(PADDING_TEST_STRING, PADDING_TEST_CHARACTER, PADDING_TEST_LENGTH, false).indexOf(PADDING_TEST_CHARACTER));

	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#isArrayIndexInRange(byte[], int)}.
	 */
	@Test
	public final void testIsArrayIndexInRange() {
		assertFalse(ProtocolTools.isArrayIndexInRange(BYTE_ARRAY, -1));
		assertTrue(ProtocolTools.isArrayIndexInRange(BYTE_ARRAY, 0));
		assertTrue(ProtocolTools.isArrayIndexInRange(BYTE_ARRAY, 1));
		assertTrue(ProtocolTools.isArrayIndexInRange(BYTE_ARRAY, BYTE_ARRAY.length - 1));
		assertFalse(ProtocolTools.isArrayIndexInRange(BYTE_ARRAY, BYTE_ARRAY.length));
		assertFalse(ProtocolTools.isArrayIndexInRange(BYTE_ARRAY, BYTE_ARRAY.length + 1));

		assertFalse(ProtocolTools.isArrayIndexInRange(null, -1));
		assertFalse(ProtocolTools.isArrayIndexInRange(null, 0));
		assertFalse(ProtocolTools.isArrayIndexInRange(null, 1));
		assertFalse(ProtocolTools.isArrayIndexInRange(null, BYTE_ARRAY.length - 1));
		assertFalse(ProtocolTools.isArrayIndexInRange(null, BYTE_ARRAY.length));
		assertFalse(ProtocolTools.isArrayIndexInRange(null, BYTE_ARRAY.length + 1));
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getSubArray(byte[], int, int)}.
	 */
	@Test
	@Ignore
	public final void testGetSubArray() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getMergedArray(byte[], byte[])}.
	 */
	@Test
	@Ignore
	public final void testGetMergedArray() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#indexOff(byte[], byte)}.
	 */
	@Test
	@Ignore
	public final void testIndexOffByteArrayByte() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#indexOff(byte[], byte, int)}.
	 */
	@Test
	@Ignore
	public final void testIndexOffByteArrayByteInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getBytesFromHexString(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testGetBytesFromHexStringStringString() {
		assertNotNull(ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING, DEFAULT_PREFIX));
		assertNotNull(ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING_0x, CUSTOM_PREFIX));
		assertTrue(Arrays.equals(BYTE_ARRAY, ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING, DEFAULT_PREFIX)));
		assertTrue(Arrays.equals(BYTE_ARRAY, ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING_0x, CUSTOM_PREFIX)));
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#writeBytesToFile(java.lang.String, byte[], boolean)}.
	 */
	@Test
	@Ignore
	public final void testWriteBytesToFile() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#readBytesFromFile(java.lang.String)}.
	 */
	@Test
	@Ignore
	public final void testReadBytesFromFile() {
		fail("Not yet implemented"); // TODO
	}

}
