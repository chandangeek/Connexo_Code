package com.energyict.dlms.aso;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;


public class SecurityContextTest {
	
	@Test
	public void getInitializationVectorTest(){
		
		SecurityContext sc = new SecurityContext(0, 1,0,"KAMM1436321499", null);
		byte[] iv = sc.getInitializationVector();
		
		assertArrayEquals(new byte[]{(byte)0x4B, (byte)0x41, (byte)0x4D, (byte)0x00, (byte)0x55, (byte)0x9C, (byte)0x86, (byte)0xDB,
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}, iv);

		sc.setFrameCounter(10);
		iv = sc.getInitializationVector();
		
		assertArrayEquals(new byte[]{(byte)0x4B, (byte)0x41, (byte)0x4D, (byte)0x00, (byte)0x55, (byte)0x9C, (byte)0x86, (byte)0xDB,
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0A}, iv);
	}

}
