package com.energyict.protocolimpl.iec1107.as220;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.Test;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;

/**
 * @author jme
 * @since 17-aug-2009
 */
public class AS220Test {

	private static final int REQUIRED_KEYS_COUNT = 0;
	private static final int OPTIONAL_KEYS_COUNT = 13;

	private static final int MESSAGE_CATEGORIES_COUNT = 2;

	@Test
	public void testProperties() throws MissingPropertyException, InvalidPropertyException {
		AS220 as220 = new AS220();
		assertNotNull(as220.getRequiredKeys());
		assertNotNull(as220.getOptionalKeys());

		//		assertEquals(REQUIRED_KEYS_COUNT, as220.getRequiredKeys().size());
		//		assertEquals(OPTIONAL_KEYS_COUNT, as220.getOptionalKeys().size());

		Properties props = new Properties();
		props.put("Retries", "123");
		props.put("SecurityLevel", "99");
		props.put("DataReadout", "1");
		props.put("RequestHeader", "1");
		props.put(MeterProtocol.PASSWORD, "1234ABCDE");
		as220.setProperties(props);

		assertEquals(props.get("Retries"), String.valueOf(as220.getNrOfRetries()));
		assertEquals(props.get("SecurityLevel"), String.valueOf(as220.getISecurityLevel()));
		assertEquals(props.get(MeterProtocol.PASSWORD), String.valueOf(as220.getPassword()));
		assertEquals(true, as220.isDataReadout());
		assertEquals(true, as220.isRequestHeader());

	}

	@Test
	public void testMessages() {
		AS220 as220 = new AS220();
		assertNotNull(as220.getMessageCategories());
		assertEquals(MESSAGE_CATEGORIES_COUNT, as220.getMessageCategories().size());
	}

	@Test
	public void testMethods() {
		AS220 as220 = new AS220();
		assertNotNull(as220.getProtocolVersion());
	}

}
