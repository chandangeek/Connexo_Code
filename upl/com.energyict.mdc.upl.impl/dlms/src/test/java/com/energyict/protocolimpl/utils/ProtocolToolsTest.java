package com.energyict.protocolimpl.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import antlr.Utils;

import com.energyict.protocol.IntervalData;

/**
 * @author jme
 *
 */
public class ProtocolToolsTest {

	private static final int	SECONDS_PER_MINUTE	= 60;
	private static final String	NON_EXISTING_FILE_NAME	= "nonexistingfilename";
	private static final String	FILENAME_TO_READ		= "/com/energyict/protocolimpl/utils/ProtocolToolsReadFileTest.txt";
	private static final String	FILENAME_TO_WRITE		= System.getProperty("java.io.tmpdir") + "/ProtocolToolsReadFileTest.tmp";
	private static final String	VALUE_TO_READ_FROM_FILE	= "9876543210123456789";

	private static final byte[]	BYTE_ARRAY				= new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05 };
	private static final String	BYTE_ARRAY_AS_STRING	= "000102030405";
	private static final String	BYTE_ARRAY_AS_STRING_$	= "$00$01$02$03$04$05";
	private static final String	BYTE_ARRAY_AS_STRING_0x	= " 0x00 0x01 0x02 0x03 0x04 0x05";
	private static final String	CUSTOM_PREFIX			= " 0x";
	private static final String	DEFAULT_PREFIX			= "$";
	private static final String	EMPTY_PREFIX			= "";

	private static final int	PADDING_TEST_LENGTH		= 20;
	private static final char	PADDING_TEST_CHARACTER	= '-';
	private static final String	PADDING_TEST_STRING		= "123";

	private static final byte[] MERGE_ARRAY1			= "ABC012DEF345".getBytes();
	private static final byte[] MERGE_ARRAY2			= "GHI678JKL012".getBytes();
	private static final byte[] MERGED_ARRAY			= "ABC012DEF345GHI678JKL012".getBytes();

	@BeforeClass
	@AfterClass
	public static void cleanUpData() {
		File fileToWrite = new File(FILENAME_TO_WRITE);
		if (fileToWrite.exists()) {
			fileToWrite.delete();
		}
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
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getBytesFromHexString(java.lang.String)}.
	 */
	@Test
	public final void testGetBytesFromHexStringString() {
		assertNotNull(ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING_$));
		assertArrayEquals(BYTE_ARRAY, ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING_$));
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getBytesFromHexString(java.lang.String, java.lang.String)}.
	 */
	@Test
	public final void testGetBytesFromHexStringStringString() {
		assertNotNull(ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING_$, DEFAULT_PREFIX));
		assertNotNull(ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING_0x, CUSTOM_PREFIX));
		assertNotNull(ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING, EMPTY_PREFIX));
		assertNotNull(ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING, null));
		assertArrayEquals(BYTE_ARRAY, ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING_$, DEFAULT_PREFIX));
		assertArrayEquals(BYTE_ARRAY, ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING_0x, CUSTOM_PREFIX));
		assertArrayEquals(BYTE_ARRAY, ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING, EMPTY_PREFIX));
		assertArrayEquals(BYTE_ARRAY, ProtocolTools.getBytesFromHexString(BYTE_ARRAY_AS_STRING, null));
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getDataBetweenBrackets(byte[])}.
	 */
	@Test
	public final void testGetDataBetweenBracketsByteArray() {
		assertArrayEquals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets("123(123456)321".getBytes()));
		assertArrayEquals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets("(123456)321".getBytes()));
		assertArrayEquals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets("(123456)".getBytes()));
		assertArrayEquals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets(")(123456)".getBytes()));
		assertArrayEquals("(123456".getBytes(), ProtocolTools.getDataBetweenBrackets("((123456)".getBytes()));
		assertArrayEquals("".getBytes(), ProtocolTools.getDataBetweenBrackets("()123456)".getBytes()));
		assertArrayEquals("".getBytes(), ProtocolTools.getDataBetweenBrackets(")123456)".getBytes()));
		assertArrayEquals("".getBytes(), ProtocolTools.getDataBetweenBrackets(")(123456".getBytes()));
		assertArrayEquals("".getBytes(), ProtocolTools.getDataBetweenBrackets(")(123456".getBytes()));
		assertArrayEquals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets("(123456)(123)".getBytes()));
		assertArrayEquals("123456".getBytes(), ProtocolTools.getDataBetweenBrackets("123)(123456)".getBytes()));
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getDataBetweenBrackets(java.lang.String)}.
	 */
	@Test
	public final void testGetDataBetweenBracketsString() {
		assertEquals("123456", ProtocolTools.getDataBetweenBrackets("\r\n#123(123456)321"));
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
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getHexStringFromBytes(byte[])}.
	 */
	@Test
	public final void testGetHexStringFromBytes() {
		assertNotNull(ProtocolTools.getHexStringFromBytes(BYTE_ARRAY));
		assertEquals(BYTE_ARRAY_AS_STRING_$, ProtocolTools.getHexStringFromBytes(BYTE_ARRAY));
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#concatByteArrays(byte[], byte[])}.
	 */
	@Test
	public final void testConcatByteArray() {
		assertArrayEquals(MERGE_ARRAY1, ProtocolTools.concatByteArrays(MERGE_ARRAY1, new byte[0]));
		assertArrayEquals(MERGE_ARRAY2, ProtocolTools.concatByteArrays(new byte[0], MERGE_ARRAY2));
		assertArrayEquals(MERGE_ARRAY1, ProtocolTools.concatByteArrays(MERGE_ARRAY1, null));
		assertArrayEquals(MERGE_ARRAY2, ProtocolTools.concatByteArrays(null, MERGE_ARRAY2));
		assertArrayEquals(new byte[0], ProtocolTools.concatByteArrays(new byte[0], null));
		assertArrayEquals(new byte[0], ProtocolTools.concatByteArrays(null, new byte[0]));
		assertArrayEquals(new byte[0], ProtocolTools.concatByteArrays(null, null));
		assertArrayEquals(MERGED_ARRAY, ProtocolTools.concatByteArrays(MERGE_ARRAY1, MERGE_ARRAY2));
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#getSubArray(byte[], int, int)}.
	 */
	@Test
	public final void testGetSubArray() {
		assertArrayEquals(MERGE_ARRAY1, ProtocolTools.getSubArray(MERGED_ARRAY, 0, MERGE_ARRAY1.length));
		assertArrayEquals(MERGE_ARRAY2, ProtocolTools.getSubArray(MERGED_ARRAY, MERGE_ARRAY1.length, MERGED_ARRAY.length));
		assertArrayEquals(new byte[0], ProtocolTools.getSubArray(MERGED_ARRAY, 0, MERGED_ARRAY.length + 1));
		assertArrayEquals(new byte[0], ProtocolTools.getSubArray(MERGED_ARRAY, -1, MERGE_ARRAY1.length));
		assertArrayEquals(new byte[0], ProtocolTools.getSubArray(MERGED_ARRAY, MERGE_ARRAY1.length + 1, MERGE_ARRAY1.length));
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#indexOff(byte[], byte)}.
	 */
	@Test
	public final void testIndexOffByteArrayByte() {
		assertEquals(-1, ProtocolTools.indexOff(MERGED_ARRAY, (byte) ' '));
		assertEquals(-1, ProtocolTools.indexOff(null, (byte) ' '));
		assertEquals(0, ProtocolTools.indexOff(MERGED_ARRAY, (byte) 'A'));
		assertEquals(3, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '0'));
		assertEquals(14, ProtocolTools.indexOff(MERGED_ARRAY, (byte) 'I'));
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#indexOff(byte[], byte, int)}.
	 */
	@Test
	public final void testIndexOffByteArrayByteInt() {
		assertEquals(-1, ProtocolTools.indexOff(MERGED_ARRAY, (byte) ' ', 0));
		assertEquals(-1, ProtocolTools.indexOff(null, (byte) ' '), 0);
		assertEquals(-1, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '2', MERGED_ARRAY.length));
		assertEquals(-1, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '2', MERGED_ARRAY.length + 1));
		assertEquals(MERGED_ARRAY.length - 1, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '2', MERGED_ARRAY.length - 1));
		assertEquals(0, ProtocolTools.indexOff(MERGED_ARRAY, (byte) 'A', 0));
		assertEquals(-1, ProtocolTools.indexOff(MERGED_ARRAY, (byte) 'A', 1));
		assertEquals(3, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '0', 0));
		assertEquals(3, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '0', 3));
		assertEquals(21, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '0', 5));
		assertEquals(21, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '0', 21));
		assertEquals(-1, ProtocolTools.indexOff(MERGED_ARRAY, (byte) '0', 22));
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
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#readBytesFromFile(java.lang.String)}.
	 */
	@Test
	public final void testReadBytesFromFile() {
		URL fileUrl = Utils.class.getResource(FILENAME_TO_READ);
		assertNotNull("This test needs the following file: " + FILENAME_TO_READ + " but the fileUrl was null!", fileUrl);
		String fileName = fileUrl.getFile();
		byte[] fileContent = ProtocolTools.readBytesFromFile(fileName);
		assertNotNull(fileContent);
		assertTrue(fileContent.length > 0);
		assertEquals(new File(fileName).length(), fileContent.length);
		String fileContentAsString = new String(fileContent);
		assertEquals(VALUE_TO_READ_FROM_FILE, ProtocolTools.getDataBetweenBrackets(fileContentAsString));
		assertArrayEquals(new byte[0], ProtocolTools.readBytesFromFile(NON_EXISTING_FILE_NAME));
	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#writeBytesToFile(java.lang.String, byte[], boolean)}.
	 */
	@Test
	public final void testWriteBytesToFile() {
		assertFalse("This tes will write data to " + FILENAME_TO_WRITE + ", but file already exist!", new File(FILENAME_TO_WRITE).exists());
		ProtocolTools.writeBytesToFile(FILENAME_TO_WRITE, BYTE_ARRAY, true);
		assertTrue("Wrote data to " + FILENAME_TO_WRITE + ", but file doesn't exist!", new File(FILENAME_TO_WRITE).exists());
		assertArrayEquals(BYTE_ARRAY, ProtocolTools.readBytesFromFile(FILENAME_TO_WRITE));
		ProtocolTools.writeBytesToFile(FILENAME_TO_WRITE, BYTE_ARRAY, false);
		assertArrayEquals(BYTE_ARRAY, ProtocolTools.readBytesFromFile(FILENAME_TO_WRITE));
		ProtocolTools.writeBytesToFile(FILENAME_TO_WRITE, BYTE_ARRAY, true);
		assertArrayEquals(ProtocolTools.concatByteArrays(BYTE_ARRAY, BYTE_ARRAY), ProtocolTools.readBytesFromFile(FILENAME_TO_WRITE));
	}


	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#roundUpToNearestInterval(Date, int)}.
	 */
	@Test
	public void testRoundUpToNearestInterval() {
		final int[] profileInterval = {1, 2, 5, 10, 15, 20, 30, 60};
		Calendar original;
		Calendar corrected = Calendar.getInstance();
		corrected.set(Calendar.MILLISECOND, 0);

		for (int i = 0; i < profileInterval.length; i++) {
			corrected.set(2010, 1, 1, 1, profileInterval[i], 0);
			original = (Calendar) corrected.clone();
			for (int j = 0; j < (profileInterval[i] * SECONDS_PER_MINUTE); j++) {
				assertEquals(corrected.getTime(), ProtocolTools.roundUpToNearestInterval(original.getTime(), profileInterval[i]));
				original.add(Calendar.SECOND, -1);
			}
		}

	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#roundDownToNearestInterval(Date, int)}.
	 */
	@Test
	public void testRoundDownToNearestInterval() {
		final int[] profileInterval = {1, 2, 5, 10, 15, 20, 30, 60};
		Calendar original;
		Calendar corrected = Calendar.getInstance();
		corrected.set(Calendar.MILLISECOND, 0);

		for (int i = 0; i < profileInterval.length; i++) {
			corrected.set(2010, 1, 1, 1, 0, 0);
			original = (Calendar) corrected.clone();
			for (int j = 0; j < (profileInterval[i] * SECONDS_PER_MINUTE); j++) {
				assertEquals("Error whil rounding date: " + original.getTime() , corrected.getTime(), ProtocolTools.roundDownToNearestInterval(original.getTime(), profileInterval[i]));
				original.add(Calendar.SECOND, 1);
			}
		}

	}

	/**
	 * Test method for {@link com.energyict.protocolimpl.utils.ProtocolTools#mergeDuplicateIntervals(java.util.List)}.
	 */
	@Test
	public final void testMergeDuplicateIntervals() {

		List<IntervalData> in = new ArrayList<IntervalData>();
		for (int i = 0; i < 15; i++) {
			IntervalData id = new IntervalData(new Date(123456789));
			for (int value = 0; value < 5; value++) {
				id.addValue(value);
			}
			in.add(id);
		}

		for (int i = 0; i < 10; i++) {
			IntervalData id = new IntervalData(new Date(124456788));
			for (int value = 0; value < 5; value++) {
				id.addValue(value * 2);
			}
			in.add(id);
		}

		List<IntervalData> out = ProtocolTools.mergeDuplicateIntervals(in);
		assertNotNull(out);
		assertEquals(2, out.size());

	}

}
