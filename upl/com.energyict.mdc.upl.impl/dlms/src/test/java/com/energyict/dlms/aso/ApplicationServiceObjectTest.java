package com.energyict.dlms.aso;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.mocks.MockDLMSConnection;
import com.energyict.dlms.mocks.MockProtocolLink;
import com.energyict.dlms.mocks.MockSecurityProvider;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.fail;


public class ApplicationServiceObjectTest {

    /**
     * TODO make it more useful. The test should check the results!
     */
    @Test
    public void handleHighLevelSecurityAuthenticationTest() {

        ApplicationServiceObject aso;
        DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance("WKP::OLD");
        MockDLMSConnection dConnection = new MockDLMSConnection();
        MockProtocolLink dpl = new MockProtocolLink(dConnection, meterConfig);
        MockSecurityProvider dsp = new MockSecurityProvider();
        SecurityContext sc;

        try {
            dsp.setAlgorithm("MD5");
            sc = new SecurityContext(0, 3, 0, null, dsp, SecurityContext.CIPHERING_TYPE_GLOBAL);
            dConnection.setResponseByte(DLMSUtils.hexStringToByteArray("640007C701810009108dd44b47c06b0d86cea4a09ecbf156b9"));
            aso = new ApplicationServiceObject(null, dpl, sc, 1);
            aso.acse.setRespondingAuthenticationValue(DLMSUtils.hexStringToByteArray("9999")); // This value doesn't matter
            dsp.setCTOs(DLMSUtils.hexStringToByteArray("0102030405060708"));
            dsp.setCallingAuthenticationValue(DLMSUtils.hexStringToByteArray("0102030405060708"));    // just need to set
            aso.handleHighLevelSecurityAuthentication();    // this may not fail!
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }

        try {
            dsp.setAlgorithm("SHA-1");
            sc = new SecurityContext(0, 4, 0, null, dsp, SecurityContext.CIPHERING_TYPE_GLOBAL);
            dConnection.setResponseByte(DLMSUtils.hexStringToByteArray("640007C70181000914fbcadd395d8edd8b7b53006cdf1367fbf370e780"));
            aso = new ApplicationServiceObject(null, dpl, sc, 1);
            aso.acse.setRespondingAuthenticationValue(DLMSUtils.hexStringToByteArray("9999")); // This value doesn't matter
            dsp.setCTOs(DLMSUtils.hexStringToByteArray("0102030405060708"));
            dsp.setCallingAuthenticationValue(DLMSUtils.hexStringToByteArray("0102030405060708"));    // just need to set
            aso.handleHighLevelSecurityAuthentication();    // this may not fail!
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test for the HLS5_GMAC encryption response method. (We tested the implementation with the help of an Iskra device...)
     */
    @Test
    public void analyzeIskraHLS5EncryptedResponse() {

        ApplicationServiceObject aso;
        DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance("WKP");
        MockDLMSConnection dConnection = new MockDLMSConnection();
        MockProtocolLink dpl = new MockProtocolLink(dConnection, meterConfig);
        MockSecurityProvider sp = new MockSecurityProvider();
        sp.setAuthenticationKey(DLMSUtils.hexStringToByteArray(new String("417A6572747926315177657274792632")));
        sp.setGlobalkey(DLMSUtils.hexStringToByteArray(new String("5177657274792632417A657274792631")));
        sp.setHLSSecretString(new String("bEd1RbAxI19n1epO"));
        SecurityContext sc = new SecurityContext(0, 5, 0, sp, SecurityContext.CIPHERING_TYPE_GLOBAL);
        sc.setResponseSystemTitle(new byte[]{73, 83, 75, -1, 1, -40, 60, -68});


        try {
            sp.setAlgorithm("GMAC");
            dConnection.setResponseByte(DLMSUtils.hexStringToByteArray("640007C701810009108dd44b47c06b0d86cea4a09ecbf156b9"));
            aso = new ApplicationServiceObject(null, dpl, sc, 1);
            aso.acse.setRespondingAuthenticationValue(new byte[]{53, 73, 86, 107, 86, 49, 115, 68, 88, 78, 115, 72, 84, 70, 68, 114}); // This value doesn't matter
            sp.setCallingAuthenticationValue(new byte[]{-10, -89, 17, 88, 28, 60, 11, 118});    // just need to set
            aso.analyzeEncryptedResponse(new byte[]{16, 0, 0, 1, 41, -86, -43, 19, -112, 26, -18, 85, -83, 89, 65, -66, -26});
        } catch (IOException e) {
            // If this fails its because the challenges do not match
            e.printStackTrace();
            fail();
        }
    }
}
