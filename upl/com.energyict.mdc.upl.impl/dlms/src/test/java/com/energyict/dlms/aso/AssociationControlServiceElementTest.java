package com.energyict.dlms.aso;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.mocks.MockSecurityProvider;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;


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

    	MockSecurityProvider sp = new MockSecurityProvider();
    	SecurityContext sc = new SecurityContext(0, 0, 0, sp, SecurityContext.CIPHERING_TYPE_GLOBAL);

    	AssociationControlServiceElement acse = new AssociationControlServiceElement(null, 1, sc);

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

        	MockSecurityProvider sp = new MockSecurityProvider();
        	SecurityContext sc = new SecurityContext(0, 0, 0, sp, SecurityContext.CIPHERING_TYPE_GLOBAL);

    		AssociationControlServiceElement acse = new AssociationControlServiceElement(ase, 1, sc);
			acse.setUserInformation(ase.getInitiatRequestByteArray());
			assertArrayEquals(aarqNoAuthentication, acse.buildAARQApdu());


			// AARQ using low level authentication
			acse.setAuthMechanismId(1);
			String passw = "12345678";
			byte[] authValue = new byte[passw.length()];
			for(int i = 0; i < passw.length(); i++){
				authValue[i] = (byte)passw.charAt(i);
			}
			sp.setCallingAuthenticationValue(authValue);
			//acse.setCallingAuthenticationValue(authValue);
			assertArrayEquals(aarqlowlevel, acse.buildAARQApdu());

			acse.setAuthMechanismId(5);
			passw = "K56iVagY";
			authValue = new byte[passw.length()];
			for(int i = 0; i < passw.length(); i++){
				authValue[i] = (byte)passw.charAt(i);
			}
			sp.setCallingAuthenticationValue(authValue);
			//acse.setCallingAuthenticationValue(authValue);
			assertArrayEquals(DLMSUtils.hexStringToByteArray("6036A1090607608574050801018A0207808B0760857405080205AC0A80084B35366956616759BE10040E01000000065F1F0400007E1F04B0"),
					acse.buildAARQApdu());

		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
    }

    /**
     * Test to analyze the AARE
     */
    @Test
    public void analyzeResponseTest() {
        try {

            AssociationControlServiceElement acse;

            acse = new AssociationControlServiceElement(null, 1, new SecurityContext(2, 2, 2, new MockSecurityProvider(), SecurityContext.CIPHERING_TYPE_GLOBAL));
            String hlSecurityResponse = "6141A109060760857405080101A203020100A305A10302010E88020780890760857405080202AA0A8008503677524A323146BE10040E0800065F1F040000501F01F40007";
            acse.analyzeAARE(DLMSUtils.hexStringToByteArray(hlSecurityResponse));
            assertNotNull(acse.getRespondingAuthenticationValue());
            assertEquals("P6wRJ21F", new String(acse.getRespondingAuthenticationValue()));
            assertEquals(0x00, acse.getXdlmsAse().getNegotiatedQOS());
            assertEquals(0x06, acse.getXdlmsAse().getNegotiatedDLMSVersion());
            assertEquals(0x01F4, acse.getXdlmsAse().getMaxRecPDUServerSize());
            assertEquals(0x0000501F, acse.getXdlmsAse().getNegotiatedConformanceBlock().getValue());

            acse = new AssociationControlServiceElement(null, 1, new SecurityContext(2, 2, 2, new MockSecurityProvider(), SecurityContext.CIPHERING_TYPE_GLOBAL));
            String responseWithQOS = "000100010064002c612aa109060760857405080101a203020100a305a103020100be11040f080112065f1f0400007c1f04000007";
            acse.analyzeAARE(DLMSUtils.hexStringToByteArray(responseWithQOS));
            assertNull(acse.getRespondingAuthenticationValue());
            assertEquals(0x12, acse.getXdlmsAse().getNegotiatedQOS());
            assertEquals(0x06, acse.getXdlmsAse().getNegotiatedDLMSVersion());
            assertEquals(0x0400, acse.getXdlmsAse().getMaxRecPDUServerSize());
            assertEquals(0x00007C1F, acse.getXdlmsAse().getNegotiatedConformanceBlock().getValue());

            acse = new AssociationControlServiceElement(null, 1, new SecurityContext(2, 2, 2, new MockSecurityProvider(), SecurityContext.CIPHERING_TYPE_GLOBAL));
            String responseWithStrangeShortConformanceLength = "000100010064002c612aa109060760857405080101a203020100a305a103020100be11040f080112065f1f03007c1f04000007";
            acse.analyzeAARE(DLMSUtils.hexStringToByteArray(responseWithStrangeShortConformanceLength));
            assertNull(acse.getRespondingAuthenticationValue());
            assertEquals(0x12, acse.getXdlmsAse().getNegotiatedQOS());
            assertEquals(0x06, acse.getXdlmsAse().getNegotiatedDLMSVersion());
            assertEquals(0x0400, acse.getXdlmsAse().getMaxRecPDUServerSize());
            assertEquals(0x00007C1F, acse.getXdlmsAse().getNegotiatedConformanceBlock().getValue());

            acse = new AssociationControlServiceElement(null, 1, new SecurityContext(2, 2, 2, new MockSecurityProvider(), SecurityContext.CIPHERING_TYPE_GLOBAL));
            String responseWithStrangeLongConformanceLength = "000100010064002c612aa109060760857405080101a203020100a305a103020100be11040f080112065f1f110000000000000000000000000000007c1f04000007";
            acse.analyzeAARE(DLMSUtils.hexStringToByteArray(responseWithStrangeLongConformanceLength));
            assertNull(acse.getRespondingAuthenticationValue());
            assertEquals(0x12, acse.getXdlmsAse().getNegotiatedQOS());
            assertEquals(0x06, acse.getXdlmsAse().getNegotiatedDLMSVersion());
            assertEquals(0x0400, acse.getXdlmsAse().getMaxRecPDUServerSize());
            assertEquals(0x00007C1F, acse.getXdlmsAse().getNegotiatedConformanceBlock().getValue());

            acse = new AssociationControlServiceElement(null, 1, new SecurityContext(2, 2, 2, new MockSecurityProvider(), SecurityContext.CIPHERING_TYPE_GLOBAL));
            acse.analyzeAARE(noOrLowLevelAuthentication);
            assertNull(acse.getRespondingAuthenticationValue());
            assertEquals(0x00, acse.getXdlmsAse().getNegotiatedQOS());
            assertEquals(0x06, acse.getXdlmsAse().getNegotiatedDLMSVersion());
            assertEquals(0x01F4, acse.getXdlmsAse().getMaxRecPDUServerSize());
            assertEquals(0x0000501F, acse.getXdlmsAse().getNegotiatedConformanceBlock().getValue());

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test(expected = IOException.class)
    public void analyzeConfirmedServiceErrosResponseTest1() throws Exception {
        MockSecurityProvider sp = new MockSecurityProvider();
        SecurityContext sc = new SecurityContext(2, 2, 2, sp, SecurityContext.CIPHERING_TYPE_GLOBAL);
        AssociationControlServiceElement acse = new AssociationControlServiceElement(null, 1, sc);
        acse.analyzeAARE(DLMSUtils.hexStringToByteArray("6129a109060760857405080101a203020101a305a10302010dbe10040e0800065f1f000c00101904180007"));
    }

    @Test(expected = IOException.class)
    public void analyzeConfirmedServiceErrosResponseTest2() throws Exception {
        MockSecurityProvider sp = new MockSecurityProvider();
        SecurityContext sc = new SecurityContext(2, 2, 2, sp, SecurityContext.CIPHERING_TYPE_GLOBAL);
        AssociationControlServiceElement acse = new AssociationControlServiceElement(null, 1, sc);
        acse.analyzeAARE(DLMSUtils.hexStringToByteArray("6133a1090607608574050801010a03020101a305a10302010da4084b414d0000000009be10040e0800065f1f040000101904180007"));
    }

    /**
     * Test if successful RLRE "$80$01$00" can be parsed (length = 1)
     */
    @Test
    public void analyzeCorrectRLRE() {
        byte[] rlre = ProtocolTools.getBytesFromHexString("$00$00$00$63$28$80$01$00$BE$23$04$21$28$1F$30$00$00$10$24$8E$6B$96$CA$25$EA$66$DC$3C$5A$F0$65$FD$57$5B$19$FC$42$B8$36$68$AA$7F$B7$D1$80");
        try {
            SecurityContext sc = new SecurityContext(2, 2, 2, new MockSecurityProvider(), SecurityContext.CIPHERING_TYPE_GLOBAL);
            AssociationControlServiceElement acse = new AssociationControlServiceElement(null, 1, sc);
            acse.analyzeRLRE(rlre);
        } catch (Exception e) {
            fail("Unexpexted Exception: " + e.getMessage());
        }
    }

    /**
     * Test if RLRE with no reason "$80$00" can be parsed (length = 0)
     */
    @Test
    public void analyzeCorrectNoReasonRLRE() {
        byte[] rlre = ProtocolTools.getBytesFromHexString("$00$00$00$63$28$80$00$BE$23$04$21$28$1F$30$00$00$10$24$8E$6B$96$CA$25$EA$66$DC$3C$5A$F0$65$FD$57$5B$19$FC$42$B8$36$68$AA$7F$B7$D1$80");
        try {
            SecurityContext sc = new SecurityContext(2, 2, 2, new MockSecurityProvider(), SecurityContext.CIPHERING_TYPE_GLOBAL);
            AssociationControlServiceElement acse = new AssociationControlServiceElement(null, 1, sc);
            acse.analyzeRLRE(rlre);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail("Unexpexted Exception: " + e.getMessage());
        }
    }

    /**
     * Test if RLRE from iskra devices can be parsed "$80$03$02$01$00" can be parsed (length = 3, reason = INT8("$02$01$00") )
     */
    @Test
    public void analyzeOldIskraRLRE() {
        byte[] rlre = DLMSUtils.hexStringToByteArray("000100010001000763058003020100");
        SecurityContext sc = new SecurityContext(0, 1, 0, new MockSecurityProvider(), 0);
        AssociationControlServiceElement acse = new AssociationControlServiceElement(null, 1, sc);
        try {
            acse.analyzeRLRE(rlre);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            fail("Unexpexted Exception: " + e.getMessage());
        }
    }

    /**
     * Test if a 'User defined' RLRE "$80$01$30" can be parsed (length = 1)
     */
    @Test
    public void analyzeUserDefinedRLRE() {
        byte[] rlre = ProtocolTools.getBytesFromHexString("$00$00$00$63$28$80$01$30$BE$23$04$21$28$1F$30$00$00$10$24$8E$6B$96$CA$25$EA$66$DC$3C$5A$F0$65$FD$57$5B$19$FC$42$B8$36$68$AA$7F$B7$D1$80");
        SecurityContext sc = new SecurityContext(2, 2, 2, new MockSecurityProvider(), SecurityContext.CIPHERING_TYPE_GLOBAL);
        AssociationControlServiceElement acse = new AssociationControlServiceElement(null, 1, sc);
        try {
            acse.analyzeRLRE(rlre);
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("userDefined"));
        }
    }

    /**
     * Test if a 'Not finished' RLRE "$80$01$01" can be parsed (length = 1)
     */
    @Test
    public void analyzeNotFinishedRLRE() {
        byte[] rlre = ProtocolTools.getBytesFromHexString("$00$00$00$63$28$80$01$01$BE$23$04$21$28$1F$30$00$00$10$24$8E$6B$96$CA$25$EA$66$DC$3C$5A$F0$65$FD$57$5B$19$FC$42$B8$36$68$AA$7F$B7$D1$80");
        SecurityContext sc = new SecurityContext(2, 2, 2, new MockSecurityProvider(), SecurityContext.CIPHERING_TYPE_GLOBAL);
        AssociationControlServiceElement acse = new AssociationControlServiceElement(null, 1, sc);
        try {
            acse.analyzeRLRE(rlre);
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("finished"));
        }
    }

    /**
     * Test if a 'Unknown' RLRE "$80$01$22" can be parsed (length = 1)
     */
    @Test
    public void analyzeUnknownRLRE() {
        byte[] rlre = ProtocolTools.getBytesFromHexString("$00$00$00$63$28$80$01$22$BE$23$04$21$28$1F$30$00$00$10$24$8E$6B$96$CA$25$EA$66$DC$3C$5A$F0$65$FD$57$5B$19$FC$42$B8$36$68$AA$7F$B7$D1$80");
        SecurityContext sc = new SecurityContext(2, 2, 2, new MockSecurityProvider(), SecurityContext.CIPHERING_TYPE_GLOBAL);
        AssociationControlServiceElement acse = new AssociationControlServiceElement(null, 1, sc);
        try {
            acse.analyzeRLRE(rlre);
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Unknown"));
        }
    }

    @Test
    public void analyzeAS1440RLRE() {
        byte[] rlre = ProtocolTools.getBytesFromHexString("$00$00$00$63$28$80$01$00$BE$23$04$21$28$1F$30$00$00$00$62$AE$4B$17$8A$8C$55$31$DC$A7$46$1C$F8$B6$4C$BB$9F$7A$0F$7D$9B$3C$1C$E2$A6$4B$80");
        SecurityContext sc = new SecurityContext(2, 2, 2, new MockSecurityProvider(), SecurityContext.CIPHERING_TYPE_GLOBAL);
        AssociationControlServiceElement acse = new AssociationControlServiceElement(null, 1, sc);
        try {
            acse.analyzeRLRE(rlre);
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Unknown"));
        }
    }

//    0001002d0030009c618199a109060760857405080103a203020100a305a103020100a40a0408454c53303030303088020780890760857405080205aa428040713a35673f232a624f293e35656c355b57276575542e76467e395b5b5d527b79797726772f642c227d74623a71296575296b784257577e6b312a6a37326b6b51be230421281f3000000001bf8c78b1336f8f1123128cd74dc14dea3824011801954a9ecfa1
//
//    @Test
//    public void as300Test() throws IOException {
//        AssociationControlServiceElement acse;
//
//            acse = new AssociationControlServiceElement(null, 1, new SecurityContext(3, 5, 2, new MockSecurityProvider(), SecurityContext.CIPHERING_TYPE_GLOBAL));
//            String hlSecurityResponse = "618199a109060760857405080103a203020100a305a103020100a40a0408454c53303030303088020780890760857405080205aa428040713a35673f232a624f293e35656c355b57276575542e76467e395b5b5d527b79797726772f642c227d74623a71296575296b784257577e6b312a6a37326b6b51be230421281f3000000001bf8c78b1336f8f1123128cd74dc14dea3824011801954a9ecfa1";
//            acse.analyzeAARE(DLMSUtils.hexStringToByteArray(hlSecurityResponse));
//    }
}
