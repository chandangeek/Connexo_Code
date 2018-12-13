package com.energyict.dlms.aso;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;


public class XdlmsAseTest {

	@Test
	public void initiateRequestTest(){
		
			/**
			 * xDLMS context with:
			 * <pre>
			 * - no ciphering
			 * - response allowed false
			 * - no proposed-quality of service
			 * - the proposed-dlms-version is 6
			 * - the proposed-conformance is the default LN conformance
			 * - the client-max-receive-pdu-size is 1200
			 */
			byte[] expected = new byte[]{(byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06,
					(byte)0x5F, (byte)0x1F, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x7E, (byte)0x1F,
					(byte)0x04, (byte)0xB0};
			
			ConformanceBlock conformanceBlock = new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
			XdlmsAse ase = new XdlmsAse(null, true, -1, 6, conformanceBlock, 1200);
			
			assertArrayEquals(expected, ase.getInitiatRequestByteArray());
			
			//NullPointerTest
			ase = new XdlmsAse(null, false, -1, 6, null, 1200);
			ase.getInitiatRequestByteArray();
			

	}
	
}
