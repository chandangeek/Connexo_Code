package com.energyict.protocolimpl.iec1107.a1440;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * @author jme
 * @since 19-aug-2009
 */
public class A1440Test {

	@Test
	public void testMethods() {
		A1440 a1440 = new A1440();
		assertNotNull(a1440.getProtocolVersion());
	}

}
