/**
 *
 */
package com.energyict.dlms.axrdencoding;

import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author jme
 *
 */
public class Integer16Test {

	private static final int BYTE_LENGTH = 8;
	private static final int BYTE_MASK = 0x0FF;

	private static final int INTEGER16_SIZE = 3;
	private static final int OFFSET = 1;

	private static final short VALUE1 = 0x1212;
	private static final short VALUE2 = 0x1313;
	private static final short VALUE3 = 0x1111;
	private static final short VALUE4 = 0x7F7F;

	private static final byte[] VALUE1_BYTES = new byte[] {AxdrType.LONG.getTag(), (VALUE1 >> BYTE_LENGTH) & BYTE_MASK, VALUE1 & BYTE_MASK};
	private static final byte[] VALUE2_BYTES = new byte[] {AxdrType.LONG.getTag(), (VALUE2 >> BYTE_LENGTH) & BYTE_MASK, VALUE2 & BYTE_MASK};
	private static final byte[] VALUE3_BYTES = new byte[] {AxdrType.LONG.getTag(), (VALUE3 >> BYTE_LENGTH) & BYTE_MASK, VALUE3 & BYTE_MASK};
	private static final byte[] VALUE4_BYTES = new byte[] {AxdrType.LONG.getTag(), (VALUE4 >> BYTE_LENGTH) & BYTE_MASK, VALUE4 & BYTE_MASK};

	private static final byte[] VALUE1_BYTES_OFFSET = new byte[] {0, AxdrType.LONG.getTag(), (VALUE1 >> BYTE_LENGTH) & BYTE_MASK, VALUE1 & BYTE_MASK};
	private static final byte[] VALUE2_BYTES_OFFSET = new byte[] {0, AxdrType.LONG.getTag(), (VALUE2 >> BYTE_LENGTH) & BYTE_MASK, VALUE2 & BYTE_MASK};
	private static final byte[] VALUE3_BYTES_OFFSET = new byte[] {0, AxdrType.LONG.getTag(), (VALUE3 >> BYTE_LENGTH) & BYTE_MASK, VALUE3 & BYTE_MASK};
	private static final byte[] VALUE4_BYTES_OFFSET = new byte[] {0, AxdrType.LONG.getTag(), (VALUE4 >> BYTE_LENGTH) & BYTE_MASK, VALUE4 & BYTE_MASK};

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer16#doGetBEREncodedByteArray()}.
	 */
	@Test
	public final void testDoGetBEREncodedByteArray() {
		Integer16 integer16 = new Integer16(VALUE1);
		assertNotNull(integer16.doGetBEREncodedByteArray());
		assertEquals(INTEGER16_SIZE, integer16.doGetBEREncodedByteArray().length);
		assertEquals(AxdrType.LONG.getTag(), integer16.doGetBEREncodedByteArray()[0]);
		assertEquals((VALUE1 >> BYTE_LENGTH) & BYTE_MASK, integer16.doGetBEREncodedByteArray()[1]);
		assertEquals(VALUE1 & BYTE_MASK, integer16.doGetBEREncodedByteArray()[2]);

		integer16.setValue(VALUE2);
		assertEquals((VALUE2 >> BYTE_LENGTH) & BYTE_MASK, integer16.doGetBEREncodedByteArray()[1]);
		assertEquals(VALUE2 & BYTE_MASK, integer16.doGetBEREncodedByteArray()[2]);
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer16#size()}.
	 */
	@Test
	public final void testSize() {
		assertEquals(INTEGER16_SIZE, new Integer16(VALUE1).size());
		assertEquals(INTEGER16_SIZE, new Integer16(VALUE2).size());
		assertEquals(INTEGER16_SIZE, new Integer16(VALUE3).size());
		assertEquals(INTEGER16_SIZE, new Integer16(VALUE4).size());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer16#toBigDecimal()}.
	 */
	@Test
	public final void testToBigDecimal() {
		assertEquals(new BigDecimal(VALUE1), new Integer16(VALUE1).toBigDecimal());
		assertEquals(new BigDecimal(VALUE2), new Integer16(VALUE2).toBigDecimal());
		assertEquals(new BigDecimal(VALUE3), new Integer16(VALUE3).toBigDecimal());
		assertEquals(new BigDecimal(VALUE4), new Integer16(VALUE4).toBigDecimal());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer16#intValue()}.
	 */
	@Test
	public final void testIntValue() {
		assertEquals(VALUE1, new Integer16(VALUE1).intValue());
		assertEquals(VALUE2, new Integer16(VALUE2).intValue());
		assertEquals(VALUE3, new Integer16(VALUE3).intValue());
		assertEquals(VALUE4, new Integer16(VALUE4).intValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer16#longValue()}.
	 */
	@Test
	public final void testLongValue() {
		assertEquals(VALUE1, new Integer16(VALUE1).longValue());
		assertEquals(VALUE2, new Integer16(VALUE2).longValue());
		assertEquals(VALUE3, new Integer16(VALUE3).longValue());
		assertEquals(VALUE4, new Integer16(VALUE4).longValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer16#Integer16(byte[], int)}.
	 * @throws IOException
	 */
	@Test
	public final void testInteger16ByteArrayInt() throws IOException {
		assertEquals(new Integer16(VALUE1).intValue(), new Integer16(VALUE1_BYTES, 0).intValue());
		assertEquals(new Integer16(VALUE2).intValue(), new Integer16(VALUE2_BYTES, 0).intValue());
		assertEquals(new Integer16(VALUE3).intValue(), new Integer16(VALUE3_BYTES, 0).intValue());
		assertEquals(new Integer16(VALUE4).intValue(), new Integer16(VALUE4_BYTES, 0).intValue());

		assertEquals(new Integer16(VALUE1).intValue(), new Integer16(VALUE1_BYTES_OFFSET, OFFSET).intValue());
		assertEquals(new Integer16(VALUE2).intValue(), new Integer16(VALUE2_BYTES_OFFSET, OFFSET).intValue());
		assertEquals(new Integer16(VALUE3).intValue(), new Integer16(VALUE3_BYTES_OFFSET, OFFSET).intValue());
		assertEquals(new Integer16(VALUE4).intValue(), new Integer16(VALUE4_BYTES_OFFSET, OFFSET).intValue());

		try {
			new Integer16(VALUE1_BYTES_OFFSET, 0);
			fail("Expected an IOException because we are creating a Integer16 with the wrong type, but didn't receive one!");
		} catch (Exception e) {
			assertTrue("Expected an IOException because we are creating a Integer16 with the wrong type, but received " + e.getClass().getCanonicalName() + "!", e instanceof IOException);
		}

	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer16#Integer16(int)}.
	 */
	@Test
	public final void testInteger16Int() {
		assertEquals(VALUE1, new Integer16(VALUE1).getValue());
		assertEquals(VALUE2, new Integer16(VALUE2).getValue());
		assertEquals(VALUE3, new Integer16(VALUE3).getValue());
		assertEquals(VALUE4, new Integer16(VALUE4).getValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer16#Integer16(short)}.
	 */
	@Test
	public final void testInteger16Short() {
		assertEquals(VALUE1, new Integer16(VALUE1).getValue());
		assertEquals(VALUE2, new Integer16(VALUE2).getValue());
		assertEquals(VALUE3, new Integer16(VALUE3).getValue());
		assertEquals(VALUE4, new Integer16(VALUE4).getValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer16#getValue()}.
	 */
	@Test
	public final void testGetValue() {
		assertEquals(VALUE1, new Integer16((int)VALUE1).getValue());
		assertEquals(VALUE2, new Integer16((int)VALUE2).getValue());
		assertEquals(VALUE3, new Integer16((int)VALUE3).getValue());
		assertEquals(VALUE4, new Integer16((int)VALUE4).getValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer16#setValue(short)}.
	 */
	@Test
	public final void testSetValue() {
		Integer16 integer8 = new Integer16(0);
		assertEquals(0, integer8.getValue());
		integer8.setValue(VALUE1);
		assertEquals(VALUE1, integer8.getValue());
		integer8.setValue(VALUE2);
		assertEquals(VALUE2, integer8.getValue());
		integer8.setValue(VALUE3);
		assertEquals(VALUE3, integer8.getValue());
		integer8.setValue(VALUE4);
		assertEquals(VALUE4, integer8.getValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer16#toString()}.
	 */
	@Test
	public final void testToString() {
		assertNotNull(new Integer16(VALUE1).toString());
		assertTrue(new Integer16(VALUE1).toString().contains(String.valueOf(VALUE1)));
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getDecodedSize()}.
	 */
	@Test
	public final void testGetDecodedSize() {
		assertEquals(INTEGER16_SIZE, new Integer16(VALUE1).getDecodedSize());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getBEREncodedByteArray()}.
	 * @throws IOException
	 */
	@Test
	public final void testGetBEREncodedByteArray() throws IOException {
		assertArrayEquals(new Integer16(VALUE1).doGetBEREncodedByteArray(), new Integer16(VALUE1).getBEREncodedByteArray());
		assertArrayEquals(new Integer16(VALUE2).doGetBEREncodedByteArray(), new Integer16(VALUE2).getBEREncodedByteArray());
		assertArrayEquals(new Integer16(VALUE3).doGetBEREncodedByteArray(), new Integer16(VALUE3).getBEREncodedByteArray());
		assertArrayEquals(new Integer16(VALUE4).doGetBEREncodedByteArray(), new Integer16(VALUE4).getBEREncodedByteArray());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getStructure()}.
	 */
	@Test
	public final void testGetStructure() {
		assertNull(new Integer16(VALUE1).getStructure());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getArray()}.
	 */
	@Test
	public final void testGetArray() {
		assertNull(new Integer16(VALUE1).getArray());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getTypeEnum()}.
	 */
	@Test
	public final void testGetTypeEnum() {
		assertNull(new Integer16(VALUE1).getTypeEnum());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isStructure()}.
	 */
	@Test
	public final void testIsStructure() {
		assertFalse(new Integer16(VALUE1).isStructure());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isUnsigned8()}.
	 */
	@Test
	public final void testIsUnsigned8() {
		assertFalse(new Integer16(VALUE1).isUnsigned8());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isVisibleString()}.
	 */
	@Test
	public final void testIsVisibleString() {
		assertFalse(new Integer16(VALUE1).isVisibleString());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isOctetString()}.
	 */
	@Test
	public final void testIsOctetString() {
		assertFalse(new Integer16(VALUE1).isOctetString());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isInteger32()}.
	 */
	@Test
	public final void testIsInteger32() {
		assertFalse(new Integer16(VALUE1).isInteger32());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isNullData()}.
	 */
	@Test
	public final void testIsNullData() {
		assertFalse(new Integer16(VALUE1).isNullData());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isArray()}.
	 */
	@Test
	public final void testIsArray() {
		assertFalse(new Integer16(VALUE1).isArray());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isUnsigned32()}.
	 */
	@Test
	public final void testIsUnsigned32() {
		assertFalse(new Integer16(VALUE1).isUnsigned32());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isTypeEnum()}.
	 */
	@Test
	public final void testIsTypeEnum() {
		assertFalse(new Integer16(VALUE1).isTypeEnum());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isInteger16()}.
	 */
	@Test
	public final void testIsInteger16() {
		assertTrue(new Integer16(VALUE1).isInteger16());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getVisibleString()}.
	 */
	@Test
	public final void testGetVisibleString() {
		assertNull(new Integer16(VALUE1).getVisibleString());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getOctetString()}.
	 */
	@Test
	public final void testGetOctetString() {
		assertNull(new Integer16(VALUE1).getOctetString());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getInteger16()}.
	 */
	@Test
	public final void testGetInteger16() {
		assertNotNull(new Integer16(VALUE1).getInteger16());
		assertEquals(new Integer16(VALUE1).intValue(), new Integer16(VALUE1).getInteger16().intValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getInteger8()}.
	 */
	@Test
	public final void testGetInteger8() {
		assertNull(new Integer16(VALUE1).getInteger8());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getInteger64()}.
	 */
	@Test
	public final void testGetInteger64() {
		assertNull(new Integer16(VALUE1).getInteger64());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getInteger32()}.
	 */
	@Test
	public final void testGetInteger32() {
		assertNull(new Integer16(VALUE1).getInteger32());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getUnsigned8()}.
	 */
	@Test
	public final void testGetUnsigned8() {
		assertNull(new Integer16(VALUE1).getUnsigned8());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getUnsigned16()}.
	 */
	@Test
	public final void testGetUnsigned16() {
		assertNull(new Integer16(VALUE1).getUnsigned16());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getUnsigned32()}.
	 */
	@Test
	public final void testGetUnsigned32() {
		assertNull(new Integer16(VALUE1).getUnsigned32());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getBitString()}.
	 */
	@Test
	public final void testGetBitString() {
		assertNull(new Integer16(VALUE1).getBitString());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getNullData()}.
	 */
	@Test
	public final void testGetNullData() {
		assertNull(new Integer16(VALUE1).getNullData());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getLevel()}.
	 */
	@Test
	public final void testGetLevel() {
		Integer16 int16 = new Integer16(VALUE1);
		assertEquals(0, int16.getLevel());
		int16.setLevel(1);
		assertEquals(1, int16.getLevel());
		int16.setLevel(0);
		assertEquals(0, int16.getLevel());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#setLevel(int)}.
	 */
	@Test
	public final void testSetLevel() {
		Integer16 int16 = new Integer16(VALUE1);
		assertNotNull(int16.toString());
		assertEquals(0, int16.getLevel());

		int16.setLevel(1);
		assertNotNull(int16.toString());
		assertEquals(1, int16.getLevel());

		int16.setLevel(0);
		assertNotNull(int16.toString());
		assertEquals(0, int16.getLevel());
	}

}
