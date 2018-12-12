package com.energyict.dlms.aso;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;


public class ConformanceBlockTest {

	@Test
	public void axdrEncodedConfBlockTest(){
		// test default
		byte[] expected = new byte[]{(byte)0x5F, (byte)0x1F, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x7E, (byte)0x1F};
		ConformanceBlock cb = new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
		assertArrayEquals(expected, cb.getAXDREncodedConformanceBlock());
		
		// test default
		expected = new byte[]{(byte)0x5F, (byte)0x1F, (byte)0x04, (byte)0x00, (byte)0x1C, (byte)0x03, (byte)0x20};
		cb = new ConformanceBlock(ConformanceBlock.DEFAULT_SN_CONFORMANCE_BLOCK);
		assertArrayEquals(expected, cb.getAXDREncodedConformanceBlock());
		
		// test 0 limit
		expected = new byte[]{(byte)0x5F, (byte)0x1F, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
		cb = new ConformanceBlock();
		assertArrayEquals(expected, cb.getAXDREncodedConformanceBlock());
		
		// test ff limit
		expected = new byte[]{(byte)0x5F, (byte)0x1F, (byte)0x04, (byte)0x00, (byte)0xFF, (byte)0xFF, (byte)0xFF};
		cb = new ConformanceBlock((long) (Math.pow(2, 24)-1));
		assertArrayEquals(expected, cb.getAXDREncodedConformanceBlock());
		
		// test custom
		expected = new byte[]{(byte)0x5F, (byte)0x1F, (byte)0x04, (byte)0x00, (byte)0x14, (byte)0x81, (byte)0x01};
		cb = new ConformanceBlock();
		cb.setBit(ConformanceBlock.BIT_READ);
		cb.setBit(ConformanceBlock.BIT_UNCONFIRMED_WRITE);
		cb.setBit(ConformanceBlock.BIT_ATTRB0_SUPP_SET);
		cb.setBit(ConformanceBlock.BIT_INFORMATION_REPORT);
		cb.setBit(ConformanceBlock.BIT_ACTION);
		assertArrayEquals(expected, cb.getAXDREncodedConformanceBlock());
	}
	
}
