package com.energyict.dlms.aso;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.energyict.dlms.DLMSCOSEMGlobals;


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
    	
    	AssociationControlServiceElement acse = new AssociationControlServiceElement();
    	acse.setContextId(1); // Set the value to 'Logical_Name_Referencing_no_ciphering'
    	
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
			AssociationControlServiceElement acse = new AssociationControlServiceElement();
			ConformanceBlock conformanceBlock = new ConformanceBlock(ConformanceBlock.DEFAULT_LN_CONFORMANCE_BLOCK);
			XdlmsAse ase = new XdlmsAse(null, false, -1, 6, conformanceBlock, 1200);
			acse.setUserInformation(ase.getInitiatRequestByteArray());
			acse.setContextId(1);
			assertArrayEquals(aarqNoAuthentication, acse.buildAARQApdu());
			
			
			// AARQ using low level authentication
			acse.setAuthMechanismId(1);
			acse.setCallingAuthenticationValue("12345678");
			assertArrayEquals(aarqlowlevel, acse.buildAARQApdu());
			
			
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
    }
    
    @Test
    public void analyzeResponsTest(){
    	try {
			AssociationControlServiceElement acse = new AssociationControlServiceElement();
			acse.analyzeAARE(noOrLowLevelAuthentication);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
    }
}
