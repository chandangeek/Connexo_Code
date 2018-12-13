/**
 *
 */
package com.energyict.dlms.axrdencoding.util;

import com.energyict.dlms.axrdencoding.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author jme
 *
 */
public class AXDRPropertiesTest {

	private static final String	STRING_KEY = "KEY1";
	private static final Integer INTEGER_KEY	= new Integer(1);

	private static final String	VALUE1	= "VALUE1";
	private static final String	VALUE2	= "VALUE2";
	private static final String	VALUE3	= "VALUE3";
	private static final String	VALUE4	= "VALUE4";

	private static final Properties	STRING_KEY_PROPERTIES	= new Properties();
	private static final Properties	INTEGER_KEY_PROPERTIES	= new Properties();
	private static final Properties	INVALID_KEY_PROPERTIES	= new Properties();
	private static final Properties	EMPTY_KEY_PROPERTIES	= new Properties();

	private static final Array		STRING_KEY_ARRAY		= new Array();
	private static final Array		INTEGER_KEY_ARRAY		= new Array();
	private static final Array		INVALID_KEY_ARRAY		= new Array();
	private static final Array		INVALID_VALUE_ARRAY		= new Array();

	static {
		STRING_KEY_PROPERTIES.setProperty("1", VALUE1);
		STRING_KEY_PROPERTIES.setProperty("2", VALUE2);

		INTEGER_KEY_PROPERTIES.put(new Integer(1), VALUE1);
		INTEGER_KEY_PROPERTIES.put(new Integer(2), VALUE2);
		INTEGER_KEY_PROPERTIES.put(new Integer(3), VALUE3);

		INVALID_KEY_PROPERTIES.put(new Long(1), VALUE1);
		INVALID_KEY_PROPERTIES.put(new Long(2), VALUE2);
		INVALID_KEY_PROPERTIES.put(new Long(3), VALUE3);
		INVALID_KEY_PROPERTIES.put(new Long(4), VALUE4);

		Structure invalidKey = new Structure();
		invalidKey.addDataType(new Integer16(INTEGER_KEY));
		invalidKey.addDataType(OctetString.fromByteArray(VALUE1.getBytes()));
		INVALID_KEY_ARRAY.addDataType(invalidKey);

		Structure invalidValue = new Structure();
		invalidValue.addDataType(OctetString.fromByteArray(STRING_KEY.getBytes()));
		invalidValue.addDataType(new Integer16(INTEGER_KEY));
		INVALID_VALUE_ARRAY.addDataType(invalidValue);

		Structure stringKey = new Structure();
		stringKey.addDataType(OctetString.fromByteArray(STRING_KEY.getBytes()));
		stringKey.addDataType(OctetString.fromByteArray(VALUE1.getBytes()));
		STRING_KEY_ARRAY.addDataType(stringKey);

		Structure integerKey = new Structure();
		integerKey.addDataType(new Integer32(INTEGER_KEY));
		integerKey.addDataType(OctetString.fromByteArray(VALUE2.getBytes()));
		INTEGER_KEY_ARRAY.addDataType(integerKey);

	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRProperties#encode(java.util.Properties)}.
	 * @throws IOException
	 */
	@Test
	public final void testEncode() throws IOException {
		assertNotNull(AXDRProperties.encode(null));
		assertNotNull(AXDRProperties.encode(STRING_KEY_PROPERTIES));
		assertNotNull(AXDRProperties.encode(INTEGER_KEY_PROPERTIES));
		assertNotNull(AXDRProperties.encode(EMPTY_KEY_PROPERTIES));

		assertTrue(AXDRProperties.encode(null).isArray());
		assertTrue(AXDRProperties.encode(STRING_KEY_PROPERTIES).isArray());
		assertTrue(AXDRProperties.encode(INTEGER_KEY_PROPERTIES).isArray());
		assertTrue(AXDRProperties.encode(EMPTY_KEY_PROPERTIES).isArray());

		assertEquals(0, AXDRProperties.encode(null).getArray().nrOfDataTypes());
		assertEquals(STRING_KEY_PROPERTIES.size(), AXDRProperties.encode(STRING_KEY_PROPERTIES).getArray().nrOfDataTypes());
		assertEquals(INTEGER_KEY_PROPERTIES.size(), AXDRProperties.encode(INTEGER_KEY_PROPERTIES).getArray().nrOfDataTypes());
		assertEquals(EMPTY_KEY_PROPERTIES.size(), AXDRProperties.encode(EMPTY_KEY_PROPERTIES).getArray().nrOfDataTypes());

		for (int i = 0; i < STRING_KEY_PROPERTIES.size(); i++) {
			assertTrue(AXDRProperties.encode(STRING_KEY_PROPERTIES).getArray().getDataType(i).isStructure());
			assertTrue(AXDRProperties.encode(STRING_KEY_PROPERTIES).getArray().getDataType(i).getStructure().getDataType(0).isOctetString());
			assertTrue(AXDRProperties.encode(STRING_KEY_PROPERTIES).getArray().getDataType(i).getStructure().getDataType(1).isOctetString());
			String key = new String(AXDRProperties.encode(STRING_KEY_PROPERTIES).getArray().getDataType(i).getStructure().getDataType(0).getOctetString().getOctetStr());
			String value = new String(AXDRProperties.encode(STRING_KEY_PROPERTIES).getArray().getDataType(i).getStructure().getDataType(1).getOctetString().getOctetStr());
			assertEquals(STRING_KEY_PROPERTIES.get(key), value);
		}

		for (int i = 0; i < INTEGER_KEY_PROPERTIES.size(); i++) {
			assertTrue(AXDRProperties.encode(INTEGER_KEY_PROPERTIES).getArray().getDataType(i).isStructure());
			assertTrue(AXDRProperties.encode(INTEGER_KEY_PROPERTIES).getArray().getDataType(i).getStructure().getDataType(0).isInteger32());
			assertTrue(AXDRProperties.encode(INTEGER_KEY_PROPERTIES).getArray().getDataType(i).getStructure().getDataType(1).isOctetString());
			Integer key = new Integer(AXDRProperties.encode(INTEGER_KEY_PROPERTIES).getArray().getDataType(i).getStructure().getDataType(0).getInteger32().getValue());
			String value = new String(AXDRProperties.encode(INTEGER_KEY_PROPERTIES).getArray().getDataType(i).getStructure().getDataType(1).getOctetString().getOctetStr());
			assertEquals(INTEGER_KEY_PROPERTIES.get(key), value);
		}

		try {
			AXDRProperties.encode(INVALID_KEY_PROPERTIES);
			fail("Expected IOException, but catched nothing.");
		} catch (Exception e) {
			assertTrue("Expected IOException, but catched another one: " + e.getClass(), e instanceof IOException);
		}

	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRProperties#decode(com.energyict.dlms.axrdencoding.AbstractDataType)}.
	 * @throws IOException
	 */
	@Test
	public final void testDecode() throws IOException {
		assertNotNull(AXDRProperties.decode(null));
		assertNotNull(AXDRProperties.decode(new NullData()));
		assertNotNull(AXDRProperties.decode(AXDRProperties.encode(STRING_KEY_PROPERTIES)));
		assertNotNull(AXDRProperties.decode(AXDRProperties.encode(INTEGER_KEY_PROPERTIES)));
		assertNotNull(AXDRProperties.decode(AXDRProperties.encode(EMPTY_KEY_PROPERTIES)));

		assertNotNull(AXDRProperties.decode(STRING_KEY_ARRAY));
		assertNotNull(AXDRProperties.decode(INTEGER_KEY_ARRAY));
		assertEquals(1, AXDRProperties.decode(STRING_KEY_ARRAY).size());
		assertEquals(1, AXDRProperties.decode(INTEGER_KEY_ARRAY).size());
		assertEquals(VALUE1, AXDRProperties.decode(STRING_KEY_ARRAY).get(STRING_KEY));
		assertEquals(VALUE2, AXDRProperties.decode(INTEGER_KEY_ARRAY).get(INTEGER_KEY));

		try {
			AXDRProperties.decode(INVALID_KEY_ARRAY);
			fail("Expected IOException, but catched nothing.");
		} catch (Exception e) {
			assertTrue("Expected IOException, but catched another one: " + e.getClass(), e instanceof IOException);
		}

		try {
			AXDRProperties.decode(INVALID_VALUE_ARRAY);
			fail("Expected IOException, but catched nothing.");
		} catch (Exception e) {
			assertTrue("Expected IOException, but catched another one: " + e.getClass(), e instanceof IOException);
		}

	}

}
