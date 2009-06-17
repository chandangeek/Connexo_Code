package com.energyict.dlms.aso;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.mocks.MockDLMSConnection;
import com.energyict.dlms.mocks.MockProtocolLink;
import com.energyict.dlms.mocks.MockSecurityProvider;


public class ApplicationServiceObjectTest {

	@Test
	public void handleHighLevelSecurityAuthenticationTest(){
		
		ApplicationServiceObject aso;
		MockDLMSConnection dConnection = new MockDLMSConnection();
		MockProtocolLink dpl = new MockProtocolLink(dConnection);
		MockSecurityProvider dsp = new MockSecurityProvider();
		
		
		try {
			dsp.setSecurityLevel(3);	// MD5
			dsp.setAlgorithm("MD5");
			dConnection.setResponseByte(DLMSUtils.hexStringToByteArray("640007C701810009108dd44b47c06b0d86cea4a09ecbf156b9"));
			aso = new ApplicationServiceObject(null, dpl, dsp, 1);
			aso.acse.setRespondingAuthenticationValue(DLMSUtils.hexStringToByteArray("9999")); // This value doesn't matter
			dsp.setCTOs(DLMSUtils.hexStringToByteArray("0102030405060708"));
			aso.handleHighLevelSecurityAuthentication();	// this may not fail!
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		
		try {
			dsp.setSecurityLevel(4);
			dsp.setAlgorithm("SHA-1");
			dConnection.setResponseByte(DLMSUtils.hexStringToByteArray("640007C70181000914fbcadd395d8edd8b7b53006cdf1367fbf370e780"));
			aso = new ApplicationServiceObject(null, dpl, dsp, 1);
			aso.acse.setRespondingAuthenticationValue(DLMSUtils.hexStringToByteArray("9999")); // This value doesn't matter
			dsp.setCTOs(DLMSUtils.hexStringToByteArray("0102030405060708"));
			aso.handleHighLevelSecurityAuthentication();	// this may not fail!
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		
	}
	
}
