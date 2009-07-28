package com.energyict.dlms.aso;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.mocks.MockSecurityProvider;


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
	
	@Test
	public void logicalDataTransportDecryptionTest(){
		
		try {
			String globalKey = "000102030405060708090A0B0C0D0E0F";
			String authenticationKey = "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF";
			String testDecryptA = "C81E1001234567C0010000080000010000FF020006725D910F9221D263877516";
			String testDecryptE = "C8122001234567411312FF935A47566827C467BC";
			String testDecryptAE = "C81E3001234567411312FF935A47566827C467BC7D825C3BE4A77C3FCC056B6B";
			MockSecurityProvider msp = new MockSecurityProvider();
			msp.setAuthenticationKey(DLMSUtils.hexStringToByteArray(authenticationKey));
			msp.setGlobalkey(DLMSUtils.hexStringToByteArray(globalKey));
			
			// Only authentication
			SecurityContext sc = new SecurityContext(SecurityContext.SECURITYPOLICY_AUTHENTICATION, 0,0,"MMM0000012345678", msp);
			byte[] unCipherResponse = sc.dataTransportDecryption(DLMSUtils.hexStringToByteArray(testDecryptA));
			assertArrayEquals(DLMSUtils.hexStringToByteArray("C0010000080000010000FF0200"), unCipherResponse);
			
			// Only encryption
			sc = new SecurityContext(SecurityContext.SECURITYPOLICY_ENCRYPTION, 0, 0, "MMM0000012345678", msp);
			unCipherResponse = sc.dataTransportDecryption(DLMSUtils.hexStringToByteArray(testDecryptE));
			assertArrayEquals(DLMSUtils.hexStringToByteArray("C0010000080000010000FF0200"), unCipherResponse);
			
			// Both encryption/authentication
			sc = new SecurityContext(SecurityContext.SECURITYPOLICY_BOTH, 0, 0, "MMM0000012345678", msp);
			unCipherResponse = sc.dataTransportDecryption(DLMSUtils.hexStringToByteArray(testDecryptAE));
			assertArrayEquals(DLMSUtils.hexStringToByteArray("C0010000080000010000FF0200"), unCipherResponse);
			
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

}
