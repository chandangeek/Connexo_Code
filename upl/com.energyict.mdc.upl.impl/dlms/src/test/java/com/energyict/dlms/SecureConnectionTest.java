package com.energyict.dlms;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.aso.AssociationControlServiceElement;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.mocks.MockDLMSConnection;
import com.energyict.dlms.mocks.MockSecurityProvider;
import com.energyict.protocol.ProtocolUtils;


public class SecureConnectionTest {
	
	@Test
	public void sendRequest(){

		String globalKey = "000102030405060708090A0B0C0D0E0F";
		String authenticationKey = "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF";
		String unSecuredRequest = "E6E600C0010000080000010000FF0200";							// first three bytes are redundant
		
		MockDLMSConnection mdc = new MockDLMSConnection();
		MockSecurityProvider msp = new MockSecurityProvider();
		msp.setAuthenticationKey(DLMSUtils.hexStringToByteArray(authenticationKey));
		msp.setGlobalkey(DLMSUtils.hexStringToByteArray(globalKey));
		ConformanceBlock cb = new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
		XdlmsAse xDlmsAse = new XdlmsAse(null, true, -1, 6, cb, 1200);
		ApplicationServiceObject aso;
		
		try {
			
			// Only authentication
			int dataTransportSecurityType = SecurityContext.SECURITYPOLICY_AUTHENTICATION;	 
			SecurityContext sc = new SecurityContext(dataTransportSecurityType, 0,0,"MMM0000012345678", msp);
			sc.setFrameCounter(19088743); 		// this is '0x01234567'
			aso = new ApplicationServiceObject(xDlmsAse, null, sc, AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_NO_CIPHERING);
			SecureConnection sConnection = new SecureConnection(aso, mdc);
			aso.setAssociationState(aso.ASSOCIATION_CONNECTED);
			
			byte[] unEncryptedResponse = sConnection.sendRequest(DLMSUtils.hexStringToByteArray(unSecuredRequest));
			
			assertArrayEquals(DLMSUtils.hexStringToByteArray("C81E1001234567C0010000080000010000FF020006725D910F9221D263877516"), ProtocolUtils.getSubArray(unEncryptedResponse,3));
			
			// Only encryption
			dataTransportSecurityType = SecurityContext.SECURITYPOLICY_ENCRYPTION;
			sc = new SecurityContext(dataTransportSecurityType, 0,0,"MMM0000012345678", msp);
			sc.setFrameCounter(19088743); 		// this is '0x01234567'
			aso = new ApplicationServiceObject(xDlmsAse, null, sc, AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_NO_CIPHERING);
			sConnection = new SecureConnection(aso, mdc);
			aso.setAssociationState(aso.ASSOCIATION_CONNECTED);
			
			unEncryptedResponse = sConnection.sendRequest(DLMSUtils.hexStringToByteArray(unSecuredRequest));
			
			assertArrayEquals(DLMSUtils.hexStringToByteArray("C8122001234567411312FF935A47566827C467BC"), ProtocolUtils.getSubArray(unEncryptedResponse,3));
			
			// Authentication and Encryption
			dataTransportSecurityType = SecurityContext.SECURITYPOLICY_BOTH;
			sc = new SecurityContext(dataTransportSecurityType, 0,0,"MMM0000012345678", msp);
			sc.setFrameCounter(19088743); 		// this is '0x01234567'
			aso = new ApplicationServiceObject(xDlmsAse, null, sc, AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_NO_CIPHERING);
			sConnection = new SecureConnection(aso, mdc);
			aso.setAssociationState(aso.ASSOCIATION_CONNECTED);
			
			unEncryptedResponse = sConnection.sendRequest(DLMSUtils.hexStringToByteArray(unSecuredRequest));
			
			assertArrayEquals(DLMSUtils.hexStringToByteArray("C81E3001234567411312FF935A47566827C467BC7D825C3BE4A77C3FCC056B6B"), ProtocolUtils.getSubArray(unEncryptedResponse,3));
			
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
}
