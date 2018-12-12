package com.energyict.protocolimpl.iec1107.siemenss4s;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.energyict.protocolimpl.iec1107.siemenss4s.security.SecureAlgorithm;


public class SecureAlgorithmTest {
	
	@Test
	/**
	 * This simulates three responses from the same meter with a different responseSeed.
	 * We decoded the password as 0x201234FF so all results match the seeds
	 */
	public void calculateAntiSeedTest(){
		
		int antiSeed;

		int seed1 = 0x3A20;
		int seed2 = 0xD4C2;
		int seed3 = 0x1806;
		int seed4 = 0x0350;
		int seed5 = 0x2176;
		
		int password = 0x201234FF;
		
		int securityResponse1 = 0x38142CF9;
		int securityResponse2 = 0x83bb9756;
		int securityResponse3 = 0xd6f6c21b;
		int securityResponse4 = 0x01641589;
		int securityResponse5 = 0xef86FB6B;
		
		antiSeed = SecureAlgorithm.calculateAntiSeed(seed1);
		assertEquals(securityResponse1, ((0x0000FFFF&password)^antiSeed) + ((0xFFFF0000&password)^(antiSeed<<16)));
		
		antiSeed = SecureAlgorithm.calculateAntiSeed(seed2);
		assertEquals(securityResponse2, ((0x0000FFFF&password)^antiSeed) + ((0xFFFF0000&password)^(antiSeed<<16)));
		
		antiSeed = SecureAlgorithm.calculateAntiSeed(seed3);
		assertEquals(securityResponse3, ((0x0000FFFF&password)^antiSeed) + ((0xFFFF0000&password)^(antiSeed<<16)));
		
		antiSeed = SecureAlgorithm.calculateAntiSeed(seed4);
		assertEquals(securityResponse4, ((0x0000FFFF&password)^antiSeed) + ((0xFFFF0000&password)^(antiSeed<<16)));
		
		antiSeed = SecureAlgorithm.calculateAntiSeed(seed5);
		assertEquals(securityResponse5, ((0x0000FFFF&password)^antiSeed) + ((0xFFFF0000&password)^(antiSeed<<16)));
	}
}
