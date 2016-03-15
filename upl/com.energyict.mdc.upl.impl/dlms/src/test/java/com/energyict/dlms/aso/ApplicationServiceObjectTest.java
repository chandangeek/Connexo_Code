package com.energyict.dlms.aso;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.mocks.MockDLMSConnection;
import com.energyict.dlms.mocks.MockProtocolLink;
import com.energyict.dlms.mocks.MockSecurityProvider;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertArrayEquals;


public class ApplicationServiceObjectTest {

    //Test vectors from http://www.di-mgt.com.au/sha_testvectors.html
    private static final String INPUT = "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq";
    private static final byte[] SYSTEM_IDENTIFIER = "EICTCOMM".getBytes();

    /**
     * TODO make it more useful. The test should check the results!
     */
    @Test
    public void handleHighLevelSecurityAuthenticationTest() throws DLMSConnectionException, IOException {

        ApplicationServiceObject aso;
        DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance("WKP::OLD");
        MockDLMSConnection dConnection = new MockDLMSConnection();
        MockProtocolLink dpl = new MockProtocolLink(dConnection, meterConfig);
        MockSecurityProvider dsp = new MockSecurityProvider();
        SecurityContext sc;

        dsp.setAlgorithm("MD5");
        sc = new SecurityContext(0, 3, 0, null, dsp, CipheringType.GLOBAL.getType());
        dConnection.setResponseByte(DLMSUtils.hexStringToByteArray("640007C701810009108dd44b47c06b0d86cea4a09ecbf156b9"));
        aso = new ApplicationServiceObject(null, dpl, sc, 1);
        aso.acse.setRespondingAuthenticationValue(DLMSUtils.hexStringToByteArray("9999")); // This value doesn't matter
        dsp.setCTOs(DLMSUtils.hexStringToByteArray("0102030405060708"));
        dsp.setCallingAuthenticationValue(DLMSUtils.hexStringToByteArray("0102030405060708"));    // just need to set
        aso.handleHighLevelSecurityAuthentication();    // this may not fail!

        dsp.setAlgorithm("SHA-1");
        sc = new SecurityContext(0, 4, 0, null, dsp, CipheringType.GLOBAL.getType());
        dConnection.setResponseByte(DLMSUtils.hexStringToByteArray("640007C70181000914fbcadd395d8edd8b7b53006cdf1367fbf370e780"));
        aso = new ApplicationServiceObject(null, dpl, sc, 1);
        aso.acse.setRespondingAuthenticationValue(DLMSUtils.hexStringToByteArray("9999")); // This value doesn't matter
        dsp.setCTOs(DLMSUtils.hexStringToByteArray("0102030405060708"));
        dsp.setCallingAuthenticationValue(DLMSUtils.hexStringToByteArray("0102030405060708"));    // just need to set
        aso.handleHighLevelSecurityAuthentication();    // this may not fail!
    }

    /**
     * Test for the HLS5_GMAC encryption response method. (We tested the implementation with the help of an Iskra device...)
     */
    @Test
    public void analyzeIskraHLS5EncryptedResponse() throws DLMSConnectionException, IOException {

        ApplicationServiceObject aso;
        DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance("WKP");
        MockDLMSConnection dConnection = new MockDLMSConnection();
        MockProtocolLink dpl = new MockProtocolLink(dConnection, meterConfig);
        MockSecurityProvider sp = new MockSecurityProvider();
        sp.setAuthenticationKey(DLMSUtils.hexStringToByteArray(new String("417A6572747926315177657274792632")));
        sp.setGlobalkey(DLMSUtils.hexStringToByteArray(new String("5177657274792632417A657274792631")));
        sp.setHLSSecretString(new String("bEd1RbAxI19n1epO"));
        SecurityContext sc = new SecurityContext(0, 5, 0, sp, CipheringType.GLOBAL.getType());
        sc.setResponseSystemTitle(new byte[]{73, 83, 75, -1, 1, -40, 60, -68});

        sp.setAlgorithm("GMAC");
        dConnection.setResponseByte(DLMSUtils.hexStringToByteArray("640007C701810009108dd44b47c06b0d86cea4a09ecbf156b9"));
        aso = new ApplicationServiceObject(null, dpl, sc, 1);
        aso.acse.setRespondingAuthenticationValue(new byte[]{53, 73, 86, 107, 86, 49, 115, 68, 88, 78, 115, 72, 84, 70, 68, 114}); // This value doesn't matter
        sp.setCallingAuthenticationValue(new byte[]{-10, -89, 17, 88, 28, 60, 11, 118});    // just need to set
        aso.analyzeDecryptedResponse(new byte[]{16, 0, 0, 1, 41, -86, -43, 19, -112, 26, -18, 85, -83, 89, 65, -66, -26});

    }

    @Test
    public void testSHA256Hashing() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        //Test vectors from http://www.di-mgt.com.au/sha_testvectors.html
        byte[] output = ProtocolTools.getBytesFromHexString("248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1", "");

        SecurityContext securityContext = new SecurityContext(0, AuthenticationTypes.HLS6_SHA256.getLevel(), 0, SYSTEM_IDENTIFIER, new MockSecurityProvider(), CipheringType.GLOBAL.getType());
        byte[] digest = securityContext.associationEncryption(INPUT.getBytes("UTF-8"));

        assertArrayEquals(digest, output);
    }

    @Test
    public void testSHA1Hashing() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] output = ProtocolTools.getBytesFromHexString("84983e441c3bd26ebaae4aa1f95129e5e54670f1", "");

        SecurityContext securityContext = new SecurityContext(0, AuthenticationTypes.HLS4_SHA1.getLevel(), 0, SYSTEM_IDENTIFIER, new MockSecurityProvider(), CipheringType.GLOBAL.getType());
        byte[] digest = securityContext.associationEncryption(INPUT.getBytes("UTF-8"));

        assertArrayEquals(digest, output);
    }

    @Test
    public void testMD5Hashing() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] output = ProtocolTools.getBytesFromHexString("8215EF0796A20BCAAAE116D3876C664A", "");

        SecurityContext securityContext = new SecurityContext(0, AuthenticationTypes.HLS3_MD5.getLevel(), 0, SYSTEM_IDENTIFIER, new MockSecurityProvider(), CipheringType.GLOBAL.getType());
        byte[] digest = securityContext.associationEncryption(INPUT.getBytes("UTF-8"));

        assertArrayEquals(digest, output);
    }
}