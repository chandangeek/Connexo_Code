package com.energyict.dlms.aso;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSUtils;


public class AssociationControlServiceElementTest {
	
    private byte[] aarqNoAuthentication={
    		DLMSCOSEMGlobals.AARQ_TAG, // AARQ
		    (byte)0x1D, // bytes to follow
		    DLMSCOSEMGlobals.AARQ_APPLICATION_CONTEXT_NAME,(byte)0x09,(byte)0x06,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x01,(byte)0x01, //application context name , LN no ciphering
		    DLMSCOSEMGlobals.AARQ_USER_INFORMATION,(byte)0x10,(byte)0x04,(byte)0x0E, // user information field context
		    DLMSCOSEMGlobals.COSEM_INITIATEREQUEST, // initiate request
		    (byte)0x00,(byte)0x00,(byte)0x00, // unused parameters
		    (byte)0x06,  // dlms version nr (xDLMS)
		    (byte)0x5F,(byte)0x1F,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x7E,(byte)0x1F, // proposed conformance
		    (byte)0x04,(byte)0xB0}; //client-max-received-pdu-size
	
    private byte[] aarqlowlevel={
		    DLMSCOSEMGlobals.AARQ_TAG, // AARQ
		    (byte)0x36, // bytes to follow
		    DLMSCOSEMGlobals.AARQ_APPLICATION_CONTEXT_NAME,(byte)0x09,(byte)0x06,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x01,(byte)0x01, //application context name , LN no ciphering
		    (byte)0x8A,(byte)0x02,(byte)0x07,(byte)0x80, // ACSE requirements
		    (byte)0x8B,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x02,(byte)0x01,
		    DLMSCOSEMGlobals.AARQ_CALLING_AUTHENTICATION_VALUE,(byte)0x0A,(byte)0x80,(byte)0x08,(byte)0x31,(byte)0x32,(byte)0x33,(byte)0x34,(byte)0x35,(byte)0x36,(byte)0x37,(byte)0x38,
		    DLMSCOSEMGlobals.AARQ_USER_INFORMATION,(byte)0x10,(byte)0x04,(byte)0x0E,
		    DLMSCOSEMGlobals.COSEM_INITIATEREQUEST, // initiate request
		    (byte)0x00,(byte)0x00,(byte)0x00, // unused parameters
		    (byte)0x06,  // dlms version nr
		    (byte)0x5F,(byte)0x1F,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x7E,(byte)0x1F, // proposed conformance
		    (byte)0x04,(byte)0xB0};//client-max-received-pdu-size
    
    private byte[] noOrLowLevelAuthentication = {
    		DLMSCOSEMGlobals.AARE_TAG,	// AARE
    		(byte)0x29,	// bytes to follow
    		DLMSCOSEMGlobals.AARE_APPLICATION_CONTEXT_NAME,(byte)0x09,(byte)0x06,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x01,(byte)0x01, //application context name , LN no ciphering
    		DLMSCOSEMGlobals.AARE_RESULT,(byte)0x03,(byte)0x02,(byte)0x01,(byte)0x00,
    		DLMSCOSEMGlobals.AARE_RESULT_SOURCE_DIAGNOSTIC,(byte)0x05,(byte)0xA1,(byte)0x03,(byte)0x02,(byte)0x01,(byte)0x00,
    		DLMSCOSEMGlobals.AARE_USER_INFORMATION,(byte)0x10,(byte)0x04,(byte)0x0E,	//user information
    		DLMSCOSEMGlobals.COSEM_INITIATERESPONSE, 
    		(byte)0x00,
    		(byte)0x06,	// dlms version nr
    		(byte)0x5F,(byte)0x1F,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x50,(byte)0x1F, // proposed conformance
    		(byte)0x01,(byte)0xF4, //server-max-received-pdu-size
    		(byte)0x00,(byte)0x07	// VAA name
    		
    		
    };
    
    @Test
    public void getObjectIdentifierNameTest(){
    	byte[] acExpected1 = new byte[]{DLMSCOSEMGlobals.AARQ_APPLICATION_CONTEXT_NAME, (byte)0x09, (byte)0x06, (byte)0x07, (byte)0x60, (byte)0x85,
    									(byte)0x74, (byte)0x05, (byte)0x08, 
    									(byte)0x01, // application context name 
    									(byte)0x01}; // context name ID 1
    	
    	AssociationControlServiceElement acse = new AssociationControlServiceElement(null, 1, 0, null);
    	
    	assertArrayEquals(acExpected1, acse.getApplicationContextName());
    	
    	byte[] acExpected2 = new byte[]{DLMSCOSEMGlobals.AARQ_APPLICATION_CONTEXT_NAME, (byte)0x09, (byte)0x06, (byte)0x07, (byte)0x60, (byte)0x85,
				(byte)0x74, (byte)0x05, (byte)0x08, 
				(byte)0x01, // application context name 
				(byte)0x04}; // context name ID 4
    	acse.setContextId(4); // Set the value to 'Logical_Name_Referencing_no_ciphering'
    	assertArrayEquals(acExpected2, acse.getApplicationContextName());
    	
    	byte[] amExpected1 = new byte[]{DLMSCOSEMGlobals.AARQ_MECHANISM_NAME,(byte)0x07,(byte)0x60,(byte)0x85,
    			(byte)0x74,(byte)0x05,(byte)0x08,
    			(byte)0x02, // authentication mechanism
    			(byte)0x01}; // mechanism ID 1
    	acse.setAuthMechanismId(1); // set to lowLevel authentication
    	assertArrayEquals(amExpected1, acse.getMechanismName());
    }
    
    @Test
    public void buildAARQApduTest(){
    	try {
    		
    		// AARQ without security mechanism
    		ConformanceBlock conformanceBlock = new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
    		XdlmsAse ase = new XdlmsAse(null, true, -1, 6, conformanceBlock, 1200);
			AssociationControlServiceElement acse = new AssociationControlServiceElement(ase, 1, 0, null);
			acse.setUserInformation(ase.getInitiatRequestByteArray());
			assertArrayEquals(aarqNoAuthentication, acse.buildAARQApdu());
			
			
			// AARQ using low level authentication
			acse.setAuthMechanismId(1);
			String passw = "12345678";
			byte[] authValue = new byte[passw.length()];
			for(int i = 0; i < passw.length(); i++){
				authValue[i] = (byte)passw.charAt(i);
			}
			acse.setCallingAuthenticationValue(authValue);
			assertArrayEquals(aarqlowlevel, acse.buildAARQApdu());
			
			acse.setAuthMechanismId(5);
			passw = "K56iVagY";
			authValue = new byte[passw.length()];
			for(int i = 0; i < passw.length(); i++){
				authValue[i] = (byte)passw.charAt(i);
			}
			acse.setCallingAuthenticationValue(authValue);
			assertArrayEquals(DLMSUtils.hexStringToByteArray("6036A1090607608574050801018A0207808B0760857405080205AC0A80084B35366956616759BE10040E01000000065F1F0400007E1F04B0"),
					acse.buildAARQApdu());
			
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
    }
    
    @Test
    public void analyzeResponsTest(){
    	try {
			
    		AssociationControlServiceElement acse = new AssociationControlServiceElement(null, 1, 2, null);
    		
    		String hlSecurityResponse = "6141A109060760857405080101A203020100A305A10302010E88020780890760857405080202AA0A8008503677524A323146BE10040E0800065F1F040000501F01F40007";
    		acse.analyzeAARE(DLMSUtils.hexStringToByteArray(hlSecurityResponse));
    		assertEquals("P6wRJ21F",new String(acse.getRespondingAuthenticationValue()));
    		
    		String str = "000100010064002c612aa109060760857405080101a203020100a305a103020100be11040f080100065f1f0400007c1f04000007";
			acse.analyzeAARE(DLMSUtils.hexStringToByteArray(str));
			
			acse.analyzeAARE(noOrLowLevelAuthentication);
			
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
    }
}
