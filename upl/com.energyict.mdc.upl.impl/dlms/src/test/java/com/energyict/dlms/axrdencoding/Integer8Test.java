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
public class Integer8Test {

	private static final int INTEGER8_SIZE = 2;
	private static final int OFFSET 	   = 1;

	private static final int VALUE1 = 0x12;
	private static final int VALUE2 = 0x13;
	private static final int VALUE3 = -1;
	private static final int VALUE4 = 0x7F;

	private static final byte[] VALUE1_BYTES = new byte[] {AxdrType.INTEGER.getTag(), VALUE1};
	private static final byte[] VALUE2_BYTES = new byte[] {AxdrType.INTEGER.getTag(), VALUE2};
	private static final byte[] VALUE3_BYTES = new byte[] {AxdrType.INTEGER.getTag(), VALUE3};
	private static final byte[] VALUE4_BYTES = new byte[] {AxdrType.INTEGER.getTag(), VALUE4};

	private static final byte[] VALUE1_BYTES_OFFSET = new byte[] {0, AxdrType.INTEGER.getTag(), VALUE1};
	private static final byte[] VALUE2_BYTES_OFFSET = new byte[] {0, AxdrType.INTEGER.getTag(), VALUE2};
	private static final byte[] VALUE3_BYTES_OFFSET = new byte[] {0, AxdrType.INTEGER.getTag(), VALUE3};
	private static final byte[] VALUE4_BYTES_OFFSET = new byte[] {0, AxdrType.INTEGER.getTag(), VALUE4};

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer8#doGetBEREncodedByteArray()}.
	 */
	@Test
	public final void testDoGetBEREncodedByteArray() {
		Integer8 integer8 = new Integer8(VALUE1);
		assertNotNull(integer8.doGetBEREncodedByteArray());
		assertEquals(INTEGER8_SIZE, integer8.doGetBEREncodedByteArray().length);
		assertEquals(AxdrType.INTEGER.getTag(), integer8.doGetBEREncodedByteArray()[0]);
		assertEquals(VALUE1, integer8.doGetBEREncodedByteArray()[1]);

		integer8.setValue(VALUE2);
		assertEquals(VALUE2, integer8.doGetBEREncodedByteArray()[1]);
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer8#size()}.
	 */
	@Test
	public final void testSize() {
		assertEquals(INTEGER8_SIZE, new Integer8(VALUE1).size());
		assertEquals(INTEGER8_SIZE, new Integer8(VALUE2).size());
		assertEquals(INTEGER8_SIZE, new Integer8(VALUE3).size());
		assertEquals(INTEGER8_SIZE, new Integer8(VALUE4).size());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer8#toBigDecimal()}.
	 */
	@Test
	public final void testToBigDecimal() {
		assertEquals(new BigDecimal(VALUE1), new Integer8(VALUE1).toBigDecimal());
		assertEquals(new BigDecimal(VALUE2), new Integer8(VALUE2).toBigDecimal());
		assertEquals(new BigDecimal(VALUE3), new Integer8(VALUE3).toBigDecimal());
		assertEquals(new BigDecimal(VALUE4), new Integer8(VALUE4).toBigDecimal());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer8#intValue()}.
	 */
	@Test
	public final void testIntValue() {
		assertEquals(VALUE1, new Integer8(VALUE1).intValue());
		assertEquals(VALUE2, new Integer8(VALUE2).intValue());
		assertEquals(VALUE3, new Integer8(VALUE3).intValue());
		assertEquals(VALUE4, new Integer8(VALUE4).intValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer8#longValue()}.
	 */
	@Test
	public final void testLongValue() {
		assertEquals(VALUE1, new Integer8(VALUE1).longValue());
		assertEquals(VALUE2, new Integer8(VALUE2).longValue());
		assertEquals(VALUE3, new Integer8(VALUE3).longValue());
		assertEquals(VALUE4, new Integer8(VALUE4).longValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer8#Integer8(byte[], int)}.
	 * @throws IOException
	 */
	@Test
	public final void testInteger8ByteArrayInt() throws IOException {
		assertEquals(new Integer8(VALUE1).intValue(), new Integer8(VALUE1_BYTES, 0).intValue());
		assertEquals(new Integer8(VALUE2).intValue(), new Integer8(VALUE2_BYTES, 0).intValue());
		assertEquals(new Integer8(VALUE3).intValue(), new Integer8(VALUE3_BYTES, 0).intValue());
		assertEquals(new Integer8(VALUE4).intValue(), new Integer8(VALUE4_BYTES, 0).intValue());

		assertEquals(new Integer8(VALUE1).intValue(), new Integer8(VALUE1_BYTES_OFFSET, OFFSET).intValue());
		assertEquals(new Integer8(VALUE2).intValue(), new Integer8(VALUE2_BYTES_OFFSET, OFFSET).intValue());
		assertEquals(new Integer8(VALUE3).intValue(), new Integer8(VALUE3_BYTES_OFFSET, OFFSET).intValue());
		assertEquals(new Integer8(VALUE4).intValue(), new Integer8(VALUE4_BYTES_OFFSET, OFFSET).intValue());

		try {
			new Integer8(VALUE1_BYTES_OFFSET, 0);
			fail("Expected an IOException because we are creating a Integer8 with the wrong type, but didn't receive one!");
		} catch (Exception e) {
			assertTrue("Expected an IOException because we are creating a Integer8 with the wrong type, but received " + e.getClass().getCanonicalName() + "!", e instanceof IOException);
		}

	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer8#Integer8(int)}.
	 */
	@Test
	public final void testInteger8Int() {
		assertEquals(VALUE1, new Integer8(VALUE1).getValue());
		assertEquals(VALUE2, new Integer8(VALUE2).getValue());
		assertEquals(VALUE3, new Integer8(VALUE3).getValue());
		assertEquals(VALUE4, new Integer8(VALUE4).getValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer8#getValue()}.
	 */
	@Test
	public final void testGetValue() {
		assertEquals(VALUE1, new Integer8(VALUE1).getValue());
		assertEquals(VALUE2, new Integer8(VALUE2).getValue());
		assertEquals(VALUE3, new Integer8(VALUE3).getValue());
		assertEquals(VALUE4, new Integer8(VALUE4).getValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer8#setValue(int)}.
	 */
	@Test
	public final void testSetValue() {
		Integer8 integer8 = new Integer8(0);
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
	 * Test method for {@link com.energyict.dlms.axrdencoding.Integer8#toString()}.
	 */
	@Test
	public final void testToString() {
		assertNotNull(new Integer8(VALUE1).toString());
		assertTrue(new Integer8(VALUE1).toString().contains(String.valueOf(VALUE1)));
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getDecodedSize()}.
	 */
	@Test
	public final void testGetDecodedSize() {
		assertEquals(INTEGER8_SIZE, new Integer8(VALUE1).getDecodedSize());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getBEREncodedByteArray()}.
	 * @throws IOException
	 */
	@Test
	public final void testGetBEREncodedByteArray() throws IOException {
		assertArrayEquals(new Integer8(VALUE1).doGetBEREncodedByteArray(), new Integer8(VALUE1).getBEREncodedByteArray());
		assertArrayEquals(new Integer8(VALUE2).doGetBEREncodedByteArray(), new Integer8(VALUE2).getBEREncodedByteArray());
		assertArrayEquals(new Integer8(VALUE3).doGetBEREncodedByteArray(), new Integer8(VALUE3).getBEREncodedByteArray());
		assertArrayEquals(new Integer8(VALUE4).doGetBEREncodedByteArray(), new Integer8(VALUE4).getBEREncodedByteArray());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getStructure()}.
	 */
	@Test
	public final void testGetStructure() {
		assertNull(new Integer8(VALUE1).getStructure());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getArray()}.
	 */
	@Test
	public final void testGetArray() {
		assertNull(new Integer8(VALUE1).getArray());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getTypeEnum()}.
	 */
	@Test
	public final void testGetTypeEnum() {
		assertNull(new Integer8(VALUE1).getTypeEnum());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isStructure()}.
	 */
	@Test
	public final void testIsStructure() {
		assertFalse(new Integer8(VALUE1).isStructure());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isUnsigned8()}.
	 */
	@Test
	public final void testIsUnsigned8() {
		assertFalse(new Integer8(VALUE1).isUnsigned8());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isVisibleString()}.
	 */
	@Test
	public final void testIsVisibleString() {
		assertFalse(new Integer8(VALUE1).isVisibleString());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isOctetString()}.
	 */
	@Test
	public final void testIsOctetString() {
		assertFalse(new Integer8(VALUE1).isOctetString());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isInteger32()}.
	 */
	@Test
	public final void testIsInteger32() {
		assertFalse(new Integer8(VALUE1).isInteger32());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isNullData()}.
	 */
	@Test
	public final void testIsNullData() {
		assertFalse(new Integer8(VALUE1).isNullData());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isArray()}.
	 */
	@Test
	public final void testIsArray() {
		assertFalse(new Integer8(VALUE1).isArray());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isUnsigned32()}.
	 */
	@Test
	public final void testIsUnsigned32() {
		assertFalse(new Integer8(VALUE1).isUnsigned32());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isTypeEnum()}.
	 */
	@Test
	public final void testIsTypeEnum() {
		assertFalse(new Integer8(VALUE1).isTypeEnum());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#isInteger16()}.
	 */
	@Test
	public final void testIsInteger16() {
		assertFalse(new Integer8(VALUE1).isInteger16());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getVisibleString()}.
	 */
	@Test
	public final void testGetVisibleString() {
		assertNull(new Integer8(VALUE1).getVisibleString());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getOctetString()}.
	 */
	@Test
	public final void testGetOctetString() {
		assertNull(new Integer8(VALUE1).getOctetString());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getInteger8()}.
	 */
	@Test
	public final void testGetInteger8() {
		assertNotNull(new Integer8(VALUE1).getInteger8());
		assertEquals(new Integer8(VALUE1).intValue(), new Integer8(VALUE1).getInteger8().intValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getInteger16()}.
	 */
	@Test
	public final void testGetInteger16() {
		assertNull(new Integer8(VALUE1).getInteger16());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getInteger64()}.
	 */
	@Test
	public final void testGetInteger64() {
		assertNull(new Integer8(VALUE1).getInteger64());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getInteger32()}.
	 */
	@Test
	public final void testGetInteger32() {
		assertNull(new Integer8(VALUE1).getInteger32());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getUnsigned8()}.
	 */
	@Test
	public final void testGetUnsigned8() {
		assertNull(new Integer8(VALUE1).getUnsigned8());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getUnsigned16()}.
	 */
	@Test
	public final void testGetUnsigned16() {
		assertNull(new Integer8(VALUE1).getUnsigned16());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getUnsigned32()}.
	 */
	@Test
	public final void testGetUnsigned32() {
		assertNull(new Integer8(VALUE1).getUnsigned32());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getBitString()}.
	 */
	@Test
	public final void testGetBitString() {
		assertNull(new Integer8(VALUE1).getBitString());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getNullData()}.
	 */
	@Test
	public final void testGetNullData() {
		assertNull(new Integer8(VALUE1).getNullData());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#getLevel()}.
	 */
	@Test
	public final void testGetLevel() {
		Integer8 int8 = new Integer8(VALUE1);
		assertEquals(0, int8.getLevel());
		int8.setLevel(1);
		assertEquals(1, int8.getLevel());
		int8.setLevel(0);
		assertEquals(0, int8.getLevel());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.AbstractDataType#setLevel(int)}.
	 */
	@Test
	public final void testSetLevel() {
		Integer8 int8 = new Integer8(VALUE1);
		assertNotNull(int8.toString());
		assertEquals(0, int8.getLevel());

		int8.setLevel(1);
		assertNotNull(int8.toString());
		assertEquals(1, int8.getLevel());

		int8.setLevel(0);
		assertNotNull(int8.toString());
		assertEquals(0, int8.getLevel());
	}

}
