package com.energyict.dlms.cosem;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author jme
 *
 */
public class ObjectReferenceTest {

	private static final int CLASS_ID = 123;
	private static final int SN = 456;
	private static final byte[] LN_REAL_1 = new byte[] { 0x01, 0x00, 0x03, 0x04, 0x05, 0x06 };
	private static final byte[] LN_REAL_2 = new byte[] { 0x00, 0x01, 0x03, 0x04, 0x05, 0x06 };
	private static final byte[] LN_REAL_3 = new byte[] { 0x01, 0x01, 0x03, 0x04, 0x05, 0x06 };
	private static final byte[] LN_ABSTRACT = new byte[] { 0x00, 0x00, 0x02, 0x03, 0x45, 0x05 };

	/**
	 * Test method for {@link com.energyict.dlms.cosem.ObjectReference#ObjectReference(byte[])}.
	 */
	@Test
	public final void testObjectReferenceByteArray() {
		assertNotNull(new ObjectReference(LN_ABSTRACT));
		assertTrue(new ObjectReference(LN_ABSTRACT).isAbstract());
		assertFalse(new ObjectReference(LN_REAL_1).isAbstract());
		assertFalse(new ObjectReference(LN_REAL_2).isAbstract());
		assertFalse(new ObjectReference(LN_REAL_3).isAbstract());
		assertTrue(new ObjectReference(LN_ABSTRACT).isLNReference());
		assertFalse(new ObjectReference(LN_ABSTRACT).isSNReference());
		assertArrayEquals(LN_ABSTRACT, new ObjectReference(LN_ABSTRACT).getLn());
		assertArrayEquals(LN_REAL_2, new ObjectReference(LN_REAL_2).getLn());
		assertEquals(-1, new ObjectReference(LN_REAL_2).getSn());
		assertEquals(-1, new ObjectReference(LN_REAL_2).getClassId());
		assertNotNull(new ObjectReference(LN_REAL_2).toString());
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.ObjectReference#ObjectReference(byte[], int)}.
	 */
	@Test
	public final void testObjectReferenceByteArrayInt() {
		assertNotNull(new ObjectReference(LN_ABSTRACT, CLASS_ID));
		assertTrue(new ObjectReference(LN_ABSTRACT, CLASS_ID).isAbstract());
		assertFalse(new ObjectReference(LN_REAL_1, CLASS_ID).isAbstract());
		assertFalse(new ObjectReference(LN_REAL_2, CLASS_ID).isAbstract());
		assertFalse(new ObjectReference(LN_REAL_3, CLASS_ID).isAbstract());
		assertTrue(new ObjectReference(LN_ABSTRACT, CLASS_ID).isLNReference());
		assertFalse(new ObjectReference(LN_ABSTRACT, CLASS_ID).isSNReference());
		assertArrayEquals(LN_ABSTRACT, new ObjectReference(LN_ABSTRACT, CLASS_ID).getLn());
		assertArrayEquals(LN_REAL_2, new ObjectReference(LN_REAL_2, CLASS_ID).getLn());
		assertEquals(-1, new ObjectReference(LN_REAL_2, CLASS_ID).getSn());
		assertEquals(CLASS_ID, new ObjectReference(LN_REAL_2, CLASS_ID).getClassId());
		assertNotNull(new ObjectReference(LN_REAL_2, CLASS_ID).toString());
	}

	/**
	 * Test method for {@link com.energyict.dlms.cosem.ObjectReference#ObjectReference(int)}.
	 */
	@Test
	public final void testObjectReferenceInt() {
		assertNotNull(new ObjectReference(SN));
		assertFalse(new ObjectReference(SN).isAbstract());
		assertFalse(new ObjectReference(SN).isLNReference());
		assertTrue(new ObjectReference(SN).isSNReference());
		assertNull(new ObjectReference(SN).getLn());
		assertEquals(SN, new ObjectReference(SN).getSn());
		assertEquals(-1, new ObjectReference(SN).getClassId());
		assertNotNull(new ObjectReference(SN).toString());
	}

	/**
	 * Test method for {@link ObjectReference#getObisCode()}.
	 */
	@Test
	public final void testGetObisCode() {
        assertNull(new ObjectReference(SN).getObisCode());

        assertNotNull(new ObjectReference(LN_ABSTRACT).getObisCode());
        assertArrayEquals(new ObjectReference(LN_ABSTRACT).getObisCode().getLN(), LN_ABSTRACT);

        assertNotNull(new ObjectReference(LN_REAL_1).getObisCode());
        assertArrayEquals(new ObjectReference(LN_REAL_1).getObisCode().getLN(), LN_REAL_1);

        assertNotNull(new ObjectReference(LN_REAL_2).getObisCode());
        assertArrayEquals(new ObjectReference(LN_REAL_2).getObisCode().getLN(), LN_REAL_2);

        assertNotNull(new ObjectReference(LN_REAL_3).getObisCode());
        assertArrayEquals(new ObjectReference(LN_REAL_3).getObisCode().getLN(), LN_REAL_3);

        assertNull(new ObjectReference(new byte[2]).getObisCode());
        assertNull(new ObjectReference(new byte[7]).getObisCode());

	}



}
