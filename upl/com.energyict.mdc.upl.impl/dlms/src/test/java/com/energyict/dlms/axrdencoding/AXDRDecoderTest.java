package com.energyict.dlms.axrdencoding;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author jme
 *
 */
public class AXDRDecoderTest {


	private static final int	LEVEL	= 1;
	private static final int	OFFSET	= 2;
	private static final int	VALUE	= 123;

	private static final byte[]	NULLDATA_BYTES						= new byte[] {AxdrType.NULL.getTag()};
	private static final byte[]	INTEGER_BYTES						= new byte[] {AxdrType.INTEGER.getTag(), VALUE };
	private static final byte[]	LONG_BYTES							= new byte[] {AxdrType.LONG.getTag(), VALUE, VALUE };
	private static final byte[]	DOUBLE_LONG_BYTES					= new byte[] {AxdrType.DOUBLE_LONG.getTag(), VALUE, VALUE, VALUE, VALUE };
	private static final byte[]	UNSIGNED_BYTES						= new byte[] {AxdrType.UNSIGNED.getTag(), VALUE };
	private static final byte[]	LONG_UNSIGNED_BYTES					= new byte[] {AxdrType.LONG_UNSIGNED.getTag(), VALUE, VALUE };
	private static final byte[]	DOUBLE_LONG_UNSIGNED_BYTES			= new byte[] {AxdrType.DOUBLE_LONG_UNSIGNED.getTag(), VALUE, VALUE, VALUE, VALUE };
	private static final byte[]	LONG64_BYTES						= new byte[] {AxdrType.LONG64.getTag(), VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, VALUE, VALUE };
	private static final byte[]	BOOLEAN_BYTES						= new byte[] {AxdrType.BOOLEAN.getTag(), VALUE };
	private static final byte[]	ENUM_BYTES							= new byte[] {AxdrType.ENUM.getTag(), VALUE };
	private static final byte[]	BITSTRING_BYTES						= new byte[] {AxdrType.BIT_STRING.getTag(), 1, VALUE };
	private static final byte[]	OCTET_STRING_BYTES					= new byte[] {AxdrType.OCTET_STRING.getTag(), 1, VALUE };
	private static final byte[]	VISIBLE_STRING_BYTES				= new byte[] {AxdrType.VISIBLE_STRING.getTag(), 1, VALUE };
	private static final byte[]	ARRAY_BYTES							= new byte[] {AxdrType.ARRAY.getTag(), 0 };
	private static final byte[]	STRUCTURE_BYTES						= new byte[] {AxdrType.STRUCTURE.getTag(), 0 };

	private static final byte[]	NULLDATA_BYTES_OFFSET				= new byte[NULLDATA_BYTES.length + OFFSET];
	private static final byte[]	INTEGER_BYTES_OFFSET				= new byte[INTEGER_BYTES.length + OFFSET];
	private static final byte[]	LONG_BYTES_OFFSET					= new byte[LONG_BYTES.length + OFFSET];
	private static final byte[]	DOUBLE_LONG_BYTES_OFFSET			= new byte[DOUBLE_LONG_BYTES.length + OFFSET];
	private static final byte[]	UNSIGNED_BYTES_OFFSET				= new byte[UNSIGNED_BYTES.length + OFFSET];
	private static final byte[]	LONG_UNSIGNED_BYTES_OFFSET			= new byte[LONG_UNSIGNED_BYTES.length + OFFSET];
	private static final byte[]	DOUBLE_LONG_UNSIGNED_BYTES_OFFSET	= new byte[DOUBLE_LONG_UNSIGNED_BYTES.length + OFFSET];
	private static final byte[]	LONG64_BYTES_OFFSET					= new byte[LONG64_BYTES.length + OFFSET];
	private static final byte[]	BOOLEAN_BYTES_OFFSET				= new byte[BOOLEAN_BYTES.length + OFFSET];
	private static final byte[]	ENUM_BYTES_OFFSET					= new byte[ENUM_BYTES.length + OFFSET];
	private static final byte[]	BITSTRING_BYTES_OFFSET				= new byte[BITSTRING_BYTES.length + OFFSET];
	private static final byte[]	OCTET_STRING_BYTES_OFFSET			= new byte[OCTET_STRING_BYTES.length + OFFSET];
	private static final byte[]	VISIBLE_STRING_BYTES_OFFSET			= new byte[VISIBLE_STRING_BYTES.length + OFFSET];
	private static final byte[]	ARRAY_BYTES_OFFSET					= new byte[ARRAY_BYTES.length + OFFSET];
	private static final byte[]	STRUCTURE_BYTES_OFFSET				= new byte[STRUCTURE_BYTES.length + OFFSET];

	static {
		System.arraycopy(NULLDATA_BYTES, 0, NULLDATA_BYTES_OFFSET, OFFSET, NULLDATA_BYTES.length);
		System.arraycopy(INTEGER_BYTES, 0, INTEGER_BYTES_OFFSET, OFFSET, INTEGER_BYTES.length);
		System.arraycopy(LONG_BYTES, 0, LONG_BYTES_OFFSET, OFFSET, LONG_BYTES.length);
		System.arraycopy(DOUBLE_LONG_BYTES, 0, DOUBLE_LONG_BYTES_OFFSET, OFFSET, DOUBLE_LONG_BYTES.length);
		System.arraycopy(UNSIGNED_BYTES, 0, UNSIGNED_BYTES_OFFSET, OFFSET, UNSIGNED_BYTES.length);
		System.arraycopy(LONG_UNSIGNED_BYTES, 0, LONG_UNSIGNED_BYTES_OFFSET, OFFSET, LONG_UNSIGNED_BYTES.length);
		System.arraycopy(DOUBLE_LONG_UNSIGNED_BYTES, 0, DOUBLE_LONG_UNSIGNED_BYTES_OFFSET, OFFSET, DOUBLE_LONG_UNSIGNED_BYTES.length);
		System.arraycopy(LONG64_BYTES, 0, LONG64_BYTES_OFFSET, OFFSET, LONG64_BYTES.length);
		System.arraycopy(BOOLEAN_BYTES, 0, BOOLEAN_BYTES_OFFSET, OFFSET, BOOLEAN_BYTES.length);
		System.arraycopy(ENUM_BYTES, 0, ENUM_BYTES_OFFSET, OFFSET, ENUM_BYTES.length);
		System.arraycopy(BITSTRING_BYTES, 0, BITSTRING_BYTES_OFFSET, OFFSET, BITSTRING_BYTES.length);
		System.arraycopy(OCTET_STRING_BYTES, 0, OCTET_STRING_BYTES_OFFSET, OFFSET, OCTET_STRING_BYTES.length);
		System.arraycopy(VISIBLE_STRING_BYTES, 0, VISIBLE_STRING_BYTES_OFFSET, OFFSET, VISIBLE_STRING_BYTES.length);
		System.arraycopy(ARRAY_BYTES, 0, ARRAY_BYTES_OFFSET, OFFSET, ARRAY_BYTES.length);
		System.arraycopy(STRUCTURE_BYTES, 0, STRUCTURE_BYTES_OFFSET, OFFSET, STRUCTURE_BYTES.length);
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AXDRDecoder#decode(byte[])}.
	 * @throws IOException
	 */
	@Test
	public final void testDecodeByteArray() throws IOException {

		assertNotNull(AXDRDecoder.decode(NULLDATA_BYTES));
		assertNotNull(AXDRDecoder.decode(INTEGER_BYTES));
		assertNotNull(AXDRDecoder.decode(LONG_BYTES));
		assertNotNull(AXDRDecoder.decode(DOUBLE_LONG_BYTES));
		assertNotNull(AXDRDecoder.decode(UNSIGNED_BYTES));
		assertNotNull(AXDRDecoder.decode(LONG_UNSIGNED_BYTES));
		assertNotNull(AXDRDecoder.decode(DOUBLE_LONG_UNSIGNED_BYTES));
		assertNotNull(AXDRDecoder.decode(LONG64_BYTES));
		assertNotNull(AXDRDecoder.decode(BOOLEAN_BYTES));
		assertNotNull(AXDRDecoder.decode(ENUM_BYTES));
		assertNotNull(AXDRDecoder.decode(BITSTRING_BYTES));
		assertNotNull(AXDRDecoder.decode(OCTET_STRING_BYTES));
		assertNotNull(AXDRDecoder.decode(VISIBLE_STRING_BYTES));
		assertNotNull(AXDRDecoder.decode(ARRAY_BYTES));
		assertNotNull(AXDRDecoder.decode(STRUCTURE_BYTES));

		assertTrue(AXDRDecoder.decode(NULLDATA_BYTES) instanceof NullData);
		assertTrue(AXDRDecoder.decode(INTEGER_BYTES) instanceof Integer8);
		assertTrue(AXDRDecoder.decode(LONG_BYTES) instanceof Integer16);
		assertTrue(AXDRDecoder.decode(DOUBLE_LONG_BYTES) instanceof Integer32);
		assertTrue(AXDRDecoder.decode(UNSIGNED_BYTES) instanceof Unsigned8);
		assertTrue(AXDRDecoder.decode(LONG_UNSIGNED_BYTES) instanceof Unsigned16);
		assertTrue(AXDRDecoder.decode(DOUBLE_LONG_UNSIGNED_BYTES) instanceof Unsigned32);
		assertTrue(AXDRDecoder.decode(LONG64_BYTES) instanceof Integer64);
		assertTrue(AXDRDecoder.decode(BOOLEAN_BYTES) instanceof BooleanObject);
		assertTrue(AXDRDecoder.decode(ENUM_BYTES) instanceof TypeEnum);
		assertTrue(AXDRDecoder.decode(BITSTRING_BYTES) instanceof BitString);
		assertTrue(AXDRDecoder.decode(OCTET_STRING_BYTES) instanceof OctetString);
		assertTrue(AXDRDecoder.decode(VISIBLE_STRING_BYTES) instanceof VisibleString);
		assertTrue(AXDRDecoder.decode(ARRAY_BYTES) instanceof Array);
		assertTrue(AXDRDecoder.decode(STRUCTURE_BYTES) instanceof Structure);

	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AXDRDecoder#decode(byte[], int)}.
	 * @throws IOException
	 */
	@Test
	public final void testDecodeByteArrayInt() throws IOException {

		assertNotNull(AXDRDecoder.decode(NULLDATA_BYTES_OFFSET, OFFSET));
		assertNotNull(AXDRDecoder.decode(INTEGER_BYTES_OFFSET, OFFSET));
		assertNotNull(AXDRDecoder.decode(LONG_BYTES_OFFSET, OFFSET));
		assertNotNull(AXDRDecoder.decode(DOUBLE_LONG_BYTES_OFFSET, OFFSET));
		assertNotNull(AXDRDecoder.decode(UNSIGNED_BYTES_OFFSET, OFFSET));
		assertNotNull(AXDRDecoder.decode(LONG_UNSIGNED_BYTES_OFFSET, OFFSET));
		assertNotNull(AXDRDecoder.decode(DOUBLE_LONG_UNSIGNED_BYTES_OFFSET, OFFSET));
		assertNotNull(AXDRDecoder.decode(LONG64_BYTES_OFFSET, OFFSET));
		assertNotNull(AXDRDecoder.decode(BOOLEAN_BYTES_OFFSET, OFFSET));
		assertNotNull(AXDRDecoder.decode(ENUM_BYTES_OFFSET, OFFSET));
		assertNotNull(AXDRDecoder.decode(BITSTRING_BYTES_OFFSET, OFFSET));
		assertNotNull(AXDRDecoder.decode(OCTET_STRING_BYTES_OFFSET, OFFSET));
		assertNotNull(AXDRDecoder.decode(VISIBLE_STRING_BYTES_OFFSET, OFFSET));
		assertNotNull(AXDRDecoder.decode(ARRAY_BYTES_OFFSET, OFFSET));
		assertNotNull(AXDRDecoder.decode(STRUCTURE_BYTES_OFFSET, OFFSET));

		assertTrue(AXDRDecoder.decode(NULLDATA_BYTES_OFFSET, OFFSET) instanceof NullData);
		assertTrue(AXDRDecoder.decode(INTEGER_BYTES_OFFSET, OFFSET) instanceof Integer8);
		assertTrue(AXDRDecoder.decode(LONG_BYTES_OFFSET, OFFSET) instanceof Integer16);
		assertTrue(AXDRDecoder.decode(DOUBLE_LONG_BYTES_OFFSET, OFFSET) instanceof Integer32);
		assertTrue(AXDRDecoder.decode(UNSIGNED_BYTES_OFFSET, OFFSET) instanceof Unsigned8);
		assertTrue(AXDRDecoder.decode(LONG_UNSIGNED_BYTES_OFFSET, OFFSET) instanceof Unsigned16);
		assertTrue(AXDRDecoder.decode(DOUBLE_LONG_UNSIGNED_BYTES_OFFSET, OFFSET) instanceof Unsigned32);
		assertTrue(AXDRDecoder.decode(LONG64_BYTES_OFFSET, OFFSET) instanceof Integer64);
		assertTrue(AXDRDecoder.decode(BOOLEAN_BYTES_OFFSET, OFFSET) instanceof BooleanObject);
		assertTrue(AXDRDecoder.decode(ENUM_BYTES_OFFSET, OFFSET) instanceof TypeEnum);
		assertTrue(AXDRDecoder.decode(BITSTRING_BYTES_OFFSET, OFFSET) instanceof BitString);
		assertTrue(AXDRDecoder.decode(OCTET_STRING_BYTES_OFFSET, OFFSET) instanceof OctetString);
		assertTrue(AXDRDecoder.decode(VISIBLE_STRING_BYTES_OFFSET, OFFSET) instanceof VisibleString);
		assertTrue(AXDRDecoder.decode(ARRAY_BYTES_OFFSET, OFFSET) instanceof Array);
		assertTrue(AXDRDecoder.decode(STRUCTURE_BYTES_OFFSET, OFFSET) instanceof Structure);

	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AXDRDecoder#decode(byte[], int, int)}.
	 * @throws IOException
	 */
	@Test
	public final void testDecodeByteArrayIntInt() throws IOException {

		assertNotNull(AXDRDecoder.decode(NULLDATA_BYTES_OFFSET, OFFSET, LEVEL));
		assertNotNull(AXDRDecoder.decode(INTEGER_BYTES_OFFSET, OFFSET, LEVEL));
		assertNotNull(AXDRDecoder.decode(LONG_BYTES_OFFSET, OFFSET, LEVEL));
		assertNotNull(AXDRDecoder.decode(DOUBLE_LONG_BYTES_OFFSET, OFFSET, LEVEL));
		assertNotNull(AXDRDecoder.decode(UNSIGNED_BYTES_OFFSET, OFFSET, LEVEL));
		assertNotNull(AXDRDecoder.decode(LONG_UNSIGNED_BYTES_OFFSET, OFFSET, LEVEL));
		assertNotNull(AXDRDecoder.decode(DOUBLE_LONG_UNSIGNED_BYTES_OFFSET, OFFSET, LEVEL));
		assertNotNull(AXDRDecoder.decode(LONG64_BYTES_OFFSET, OFFSET, LEVEL));
		assertNotNull(AXDRDecoder.decode(BOOLEAN_BYTES_OFFSET, OFFSET, LEVEL));
		assertNotNull(AXDRDecoder.decode(ENUM_BYTES_OFFSET, OFFSET, LEVEL));
		assertNotNull(AXDRDecoder.decode(BITSTRING_BYTES_OFFSET, OFFSET, LEVEL));
		assertNotNull(AXDRDecoder.decode(OCTET_STRING_BYTES_OFFSET, OFFSET, LEVEL));
		assertNotNull(AXDRDecoder.decode(VISIBLE_STRING_BYTES_OFFSET, OFFSET, LEVEL));
		assertNotNull(AXDRDecoder.decode(ARRAY_BYTES_OFFSET, OFFSET, LEVEL));
		assertNotNull(AXDRDecoder.decode(STRUCTURE_BYTES_OFFSET, OFFSET, LEVEL));

		assertTrue(AXDRDecoder.decode(NULLDATA_BYTES_OFFSET, OFFSET, LEVEL) instanceof NullData);
		assertTrue(AXDRDecoder.decode(INTEGER_BYTES_OFFSET, OFFSET, LEVEL) instanceof Integer8);
		assertTrue(AXDRDecoder.decode(LONG_BYTES_OFFSET, OFFSET, LEVEL) instanceof Integer16);
		assertTrue(AXDRDecoder.decode(DOUBLE_LONG_BYTES_OFFSET, OFFSET, LEVEL) instanceof Integer32);
		assertTrue(AXDRDecoder.decode(UNSIGNED_BYTES_OFFSET, OFFSET, LEVEL) instanceof Unsigned8);
		assertTrue(AXDRDecoder.decode(LONG_UNSIGNED_BYTES_OFFSET, OFFSET, LEVEL) instanceof Unsigned16);
		assertTrue(AXDRDecoder.decode(DOUBLE_LONG_UNSIGNED_BYTES_OFFSET, OFFSET, LEVEL) instanceof Unsigned32);
		assertTrue(AXDRDecoder.decode(LONG64_BYTES_OFFSET, OFFSET, LEVEL) instanceof Integer64);
		assertTrue(AXDRDecoder.decode(BOOLEAN_BYTES_OFFSET, OFFSET, LEVEL) instanceof BooleanObject);
		assertTrue(AXDRDecoder.decode(ENUM_BYTES_OFFSET, OFFSET, LEVEL) instanceof TypeEnum);
		assertTrue(AXDRDecoder.decode(BITSTRING_BYTES_OFFSET, OFFSET, LEVEL) instanceof BitString);
		assertTrue(AXDRDecoder.decode(OCTET_STRING_BYTES_OFFSET, OFFSET, LEVEL) instanceof OctetString);
		assertTrue(AXDRDecoder.decode(VISIBLE_STRING_BYTES_OFFSET, OFFSET, LEVEL) instanceof VisibleString);
		assertTrue(AXDRDecoder.decode(ARRAY_BYTES_OFFSET, OFFSET, LEVEL) instanceof Array);
		assertTrue(AXDRDecoder.decode(STRUCTURE_BYTES_OFFSET, OFFSET, LEVEL) instanceof Structure);

	}

	/**
	 * This JUnit test checks the IOException, thrown by the decoder when an invalid data type is passed through
	 *
	 * Test method for {@link com.energyict.dlms.axrdencoding.AXDRDecoder#decode(byte[])}.
	 * Test method for {@link com.energyict.dlms.axrdencoding.AXDRDecoder#decode(byte[], int)}.
	 * Test method for {@link com.energyict.dlms.axrdencoding.AXDRDecoder#decode(byte[], int, int)}.
	 */
	@Test
	public final void testDecodeByteArrayInvalidDataTypes() {
		for (int i = 0; i < 255; i++) {
			byte[] testArray = new byte[1 + OFFSET];
			Arrays.fill(testArray, (byte) i);
			if (!isTypeIdSupportedByAXDRDecoder(i)) {
				try {
					AXDRDecoder.decode(testArray);
					fail("Expected IOException, because type " + i + " is not a valid dlms data type");
				} catch (Exception e) {
					assertTrue("Catched an exception, but not the expected IOException! " + e.getClass().getCanonicalName(), e instanceof IOException);
				}
				try {
					AXDRDecoder.decode(testArray, OFFSET);
					fail("Expected IOException, because type " + i + " is not a valid dlms data type");
				} catch (Exception e) {
					assertTrue("Catched an exception, but not the expected IOException! " + e.getClass().getCanonicalName(), e instanceof IOException);
				}
				try {
					AXDRDecoder.decode(testArray, OFFSET, LEVEL);
					fail("Expected IOException, because type " + i + " is not a valid dlms data type");
				} catch (Exception e) {
					assertTrue("Catched an exception, but not the expected IOException! " + e.getClass().getCanonicalName(), e instanceof IOException);
				}
			}
		}
	}

	/**
	 * Method to check if a given typeId can be decoded by the {@link AXDRDecoder}
	 * @param typeId the dlms typeId
	 * @return true if this typeId can be parsed by the {@link AXDRDecoder}
	 */
	private boolean isTypeIdSupportedByAXDRDecoder(int typeId) {
		switch (typeId) {
			case AxdrType.NULL.getTag():
			case AxdrType.ARRAY.getTag():
			case AxdrType.STRUCTURE.getTag():
			case AxdrType.INTEGER.getTag():
			case AxdrType.LONG.getTag():
			case AxdrType.DOUBLE_LONG.getTag():
			case AxdrType.UNSIGNED.getTag():
			case AxdrType.LONG_UNSIGNED.getTag():
			case AxdrType.ENUM.getTag():
			case AxdrType.BIT_STRING.getTag():
			case AxdrType.VISIBLE_STRING.getTag():
			case AxdrType.OCTET_STRING.getTag():
			case AxdrType.DOUBLE_LONG_UNSIGNED.getTag():
			case AxdrType.LONG64.getTag():
			case AxdrType.LONG64_UNSIGNED.getTag():
			case AxdrType.BOOLEAN.getTag():
				return true;
			default:
				return false;
		}
	}
}
