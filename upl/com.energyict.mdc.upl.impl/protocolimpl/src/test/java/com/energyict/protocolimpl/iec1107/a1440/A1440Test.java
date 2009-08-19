package com.energyict.protocolimpl.iec1107.a1440;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.Test;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;

/**
 * @author jme
 * @since 19-aug-2009
 */
public class A1440Test {

	private static final int REQUIRED_KEYS_COUNT = 0;
	private static final int OPTIONAL_KEYS_COUNT = 13;

	private static final int MESSAGE_CATEGORIES_COUNT = 2;

	@Test
	public void testProperties() throws MissingPropertyException, InvalidPropertyException {
		A1440 a1440 = new A1440();
		assertNotNull(a1440.getRequiredKeys());
		assertNotNull(a1440.getOptionalKeys());

		//		assertEquals(REQUIRED_KEYS_COUNT, a1440.getRequiredKeys().size());
		//		assertEquals(OPTIONAL_KEYS_COUNT, a1440.getOptionalKeys().size());

		Properties props = new Properties();
		props.put("Retries", "123");
		props.put("SecurityLevel", "99");
		props.put("DataReadout", "1");
		props.put("RequestHeader", "1");
		props.put(MeterProtocol.PASSWORD, "1234ABCDE");
		a1440.setProperties(props);

		assertEquals(props.get("Retries"), String.valueOf(a1440.getNrOfRetries()));
		assertEquals(props.get("SecurityLevel"), String.valueOf(a1440.getISecurityLevel()));
		assertEquals(props.get(MeterProtocol.PASSWORD), String.valueOf(a1440.getPassword()));
		assertEquals(true, a1440.isDataReadout());
		assertEquals(true, a1440.isRequestHeader());

	}

	@Test
	public void testMessages() {
		A1440 a1440 = new A1440();
		assertNotNull(a1440.getMessageCategories());
		assertEquals(MESSAGE_CATEGORIES_COUNT, a1440.getMessageCategories().size());
	}

	@Test
	public void testMethods() {
		A1440 a1440 = new A1440();
		assertNotNull(a1440.getProtocolVersion());
	}

}
