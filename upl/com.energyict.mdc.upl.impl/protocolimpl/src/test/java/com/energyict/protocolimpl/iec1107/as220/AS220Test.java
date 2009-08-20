package com.energyict.protocolimpl.iec1107.as220;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * @author jme
 * @since 17-aug-2009
 */
public class AS220Test {

	@Test
	public void testMethods() {
		AS220 as220 = new AS220();
		assertNotNull(as220.getProtocolVersion());
	}

}
