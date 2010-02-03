package com.energyict.dlms.aso;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.mocks.MockDLMSConnection;
import com.energyict.dlms.mocks.MockProtocolLink;
import com.energyict.dlms.mocks.MockSecurityProvider;


public class ApplicationServiceObjectTest {

	/**
	 * TODO make it more useful. The test should check the results!
	 */
	@Test
	public void handleHighLevelSecurityAuthenticationTest(){
		
		ApplicationServiceObject aso;
		DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance("WKP::OLD");
		MockDLMSConnection dConnection = new MockDLMSConnection();
		MockProtocolLink dpl = new MockProtocolLink(dConnection, meterConfig);
		MockSecurityProvider dsp = new MockSecurityProvider();
		SecurityContext sc;
		
		try {
			dsp.setAlgorithm("MD5");
			sc = new SecurityContext(0,3,0,null,dsp);
			dConnection.setResponseByte(DLMSUtils.hexStringToByteArray("640007C701810009108dd44b47c06b0d86cea4a09ecbf156b9"));
			aso = new ApplicationServiceObject(null, dpl, sc, 1);
			aso.acse.setRespondingAuthenticationValue(DLMSUtils.hexStringToByteArray("9999")); // This value doesn't matter
			dsp.setCTOs(DLMSUtils.hexStringToByteArray("0102030405060708"));
			dsp.setCallingAuthenticationValue(DLMSUtils.hexStringToByteArray("0102030405060708"));	// just need to set
			aso.handleHighLevelSecurityAuthentication();	// this may not fail!
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		
		try {
			dsp.setAlgorithm("SHA-1");
			sc = new SecurityContext(0,4,0,null,dsp);
			dConnection.setResponseByte(DLMSUtils.hexStringToByteArray("640007C70181000914fbcadd395d8edd8b7b53006cdf1367fbf370e780"));
			aso = new ApplicationServiceObject(null, dpl, sc, 1);
			aso.acse.setRespondingAuthenticationValue(DLMSUtils.hexStringToByteArray("9999")); // This value doesn't matter
			dsp.setCTOs(DLMSUtils.hexStringToByteArray("0102030405060708"));
			dsp.setCallingAuthenticationValue(DLMSUtils.hexStringToByteArray("0102030405060708"));	// just need to set
			aso.handleHighLevelSecurityAuthentication();	// this may not fail!
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		
	}
	
}
