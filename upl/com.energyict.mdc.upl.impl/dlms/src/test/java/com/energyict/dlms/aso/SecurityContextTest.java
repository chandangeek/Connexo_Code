package com.energyict.dlms.aso;

import com.energyict.dlms.CipheringType;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.mocks.MockRespondingFrameCounterHandler;
import com.energyict.dlms.mocks.MockSecurityProvider;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class SecurityContextTest {

    private static byte[] systemTitle = new byte[]{(byte) 0x4B, (byte) 0x41, (byte) 0x4D, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x90};

    @Test
    public void getInitializationVectorTest() {

        SecurityContext sc = new SecurityContext(0, 1, 0, systemTitle, new MockSecurityProvider(), CipheringType.GLOBAL.getType(), false);
        sc.setFrameCounter(0);
        byte[] iv = sc.getInitializationVector();

        assertArrayEquals(new byte[]{(byte) 0x4B, (byte) 0x41, (byte) 0x4D, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x90,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00}, iv);

        sc.setFrameCounter(10);
        iv = sc.getInitializationVector();

        assertArrayEquals(new byte[]{(byte) 0x4B, (byte) 0x41, (byte) 0x4D, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x90,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0A}, iv);
    }

    @Test
    public void kaifaFirmwareCrashFrame(){
        String globalKey    = "00000000000000000000000000000000";
        String dedicatedKey = "00000000000000000000000000000000";
        String authenticationKey = "00000000000000000000000000000000";

        MockSecurityProvider msp = new MockSecurityProvider();
        msp.setAuthenticationKey(DLMSUtils.hexStringToByteArray(authenticationKey));
        msp.setGlobalkey(DLMSUtils.hexStringToByteArray(globalKey));
        msp.setDedicatedKey(DLMSUtils.hexStringToByteArray(dedicatedKey));

        SecurityContext sc = new SecurityContext(SecurityPolicy.SECURITYPOLICY_BOTH, 0, 0, systemTitle, msp, CipheringType.GLOBAL.getType(), false);

        byte[] frame = DLMSUtils.getBytesFromHexString("$0F$BB$E8$95$46$AD$92$BA$0C$14$8D$CA$28$52$F3$02$39$6E$DB$0C$ED$06$F0$DF$27$D4$C3$6E$E1$46$1B$9A$EF$8B$67$06$19$ED$AB$E7$A7$EF$6F$97$FF$02$4F$69$B5$9C$B6$F1$93$D2$E4$C8$94$E9$9A$6B$71$48$CC$5E$82$D1$4A$21$8C$9C$4D$AA$64$D6$F4$3C$5C$06$23$AE$CB$3E$9B$D0$BD$8A$27$F7$C2$F6$5D$5E$1D$17$B8$F0$7F$A1$B4$8B$1B$CE$C4$CD$1D$01$B4$2B$3E$F4$C3$1F$1A$04$95$38$48$EC$BE$93$94$DE$33$9D$0B$56$51$31$8C$65$67$FF$F6$F5$0E$58$2E$78$60$8F$7F$4F$78$C4$4D$68$50$F3$9E$9A$46$AD$AE$82$E7$DE$0B$66$1A$E4$55$4F$C4$66$54$70$DA$D8$3D$93$1E$9C$41$9F$88$47$A8$9C$00$40$C4$AD$BE$8A$00$5A$2B$DA$2B$06$CF$6C$E2$E1$6D$FA$10$56$1C$06$96$F4$9D$DA$0D$96$33$C1$45$E0$D2$51$37$C0$9D$D0$12$CD$74$C4$4D$EB$05$5C$50$F1$F4$D4$17$13$67$65$3D$8B$33$7F$E7$A2$73", "$");
        byte[] authTag= DLMSUtils.getBytesFromHexString("$87$25$94$50$B1$75$EE$7E$FA$87$08$BB", "$");
        byte[] apdu = sc.createSecuredApdu(frame, authTag);

        assertEquals(259, apdu.length);
        assertArrayEquals(ProtocolTools.getBytesFromHexString("$82$01$00"), ProtocolTools.getSubArray(apdu, 0, 3));
        assertArrayEquals(frame, ProtocolTools.getSubArray(apdu, apdu.length-frame.length-authTag.length, apdu.length-authTag.length));
        assertArrayEquals(authTag, ProtocolTools.getSubArray(apdu, apdu.length-authTag.length, apdu.length));
    }

    @Test
    public void logicalDataTransportDecryptionTest() throws DLMSConnectionException, IOException {
        String globalKey = "000102030405060708090A0B0C0D0E0F";
        String dedicatedKey = "000102030405060708090A0B0C0D0E0F";
        String authenticationKey = "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF";
        String testDecryptA = "C81e1001234567C0010000080000010000FF020003C51f733571866e675bF115";
        String testDecryptE = "C8122001234567D0BC0dE872E76d3f64CE312aB1";
        String testDecryptAE = "C81E3001234567D0BC0dE872E76d3f64CE312aB1475c7dC48AB69286B4E42664";
        MockSecurityProvider msp = new MockSecurityProvider();
        msp.setAuthenticationKey(DLMSUtils.hexStringToByteArray(authenticationKey));
        msp.setGlobalkey(DLMSUtils.hexStringToByteArray(globalKey));
        msp.setDedicatedKey(DLMSUtils.hexStringToByteArray(dedicatedKey));

        // Only authentication
        SecurityContext sc = new SecurityContext(SecurityPolicy.SECURITYPOLICY_AUTHENTICATION, 0, 0, systemTitle, msp, CipheringType.GLOBAL.getType(), false);
        sc.setResponseSystemTitle(systemTitle);
        byte[] unCipherResponse = sc.dataTransportDecryption(DLMSUtils.hexStringToByteArray(testDecryptA));
        assertArrayEquals(DLMSUtils.hexStringToByteArray("C0010000080000010000FF0200"), unCipherResponse);

        // Only encryption
        sc = new SecurityContext(SecurityPolicy.SECURITYPOLICY_ENCRYPTION, 0, 0, systemTitle, msp, CipheringType.GLOBAL.getType(), false);
        sc.setResponseSystemTitle(systemTitle);
        unCipherResponse = sc.dataTransportDecryption(DLMSUtils.hexStringToByteArray(testDecryptE));
        assertArrayEquals(DLMSUtils.hexStringToByteArray("C0010000080000010000FF0200"), unCipherResponse);

        // Both encryption/authentication
        sc = new SecurityContext(SecurityPolicy.SECURITYPOLICY_BOTH, 0, 0, systemTitle, msp, CipheringType.GLOBAL.getType(), false);
        sc.setResponseSystemTitle(systemTitle);
        unCipherResponse = sc.dataTransportDecryption(DLMSUtils.hexStringToByteArray(testDecryptAE));
        assertArrayEquals(DLMSUtils.hexStringToByteArray("C0010000080000010000FF0200"), unCipherResponse);
    }

    /**
     * The intention of the test is to check for the correct positions of all fields in the frame
     */
    @Test
    public void logicalDataDecryptionHugeFrame() throws DLMSConnectionException, IOException {
        String hugeFrame = "cc820409200000002b8983ff4da4e3a8cb6d5918c521f43556dd887e66e2ac3cb075c1e0b18ba0809d69e3b20381bd0b" +
                "385b67e0da82ff71bb175ee951b3b5112c795ba46fac4253fd17d397ab1b84f27fc62e29808981778a908477c217445a0be778a71d21eb7014c0d759de1b7ff8" +
                "6f7f250540fc85f68b65e88543cb912ed15a041995c87b4f8c63048fa8a0f439b1e1c26b07faa3d22018722f7220833330d2c7186c9c59ec20ad8800f2b7ba31" +
                "91b67414eae838a4e84567e80d3b033db746e006c3edd768099d77ea9281940eba815ff13af7a5032e9d68e30854cbf40cabccc3ab585b166ffdcdd51fe8c932" +
                "53ca7057d80c00a173f85bca6c33cbd77917bd1b0723458c09031b4a90df61223892bd232bb3c2d6eec1738e2cf5e634591c1cb69cee0cb33a7b32a3ef2ac0d3" +
                "178b21a0d506474fba1a0cfb7f940af2a6f8b1c66da2f1537455d8dbb9315655aec13c27d5c88546c7da0754809783f8d8a3f076e5ac352c9e0530439517233a" +
                "3d7cc9ddf5960ef11d0efc1b1e98b5082d023faa92d8c010cfaf676145f1f7639a14b3f7fadcc44ad409a96538994241f4d74ae08abc83f98812c4dec76af7d4" +
                "f3e57f9ce5edbdd38f305d3a39cf5d846648a944e62ec5be6d747e71e5c3585aed7f3e029ad53f0074bfaacd343d8fcc1451df32cfe466bfe2910086752b93ef" +
                "66b420fd64a7a5b5e50f48beca46e03555dd6e43bf2f03cecabbf7dae1fd13f597cbf6348a56b633504c99349841134fbe7be57563ddad3c1c86ae071978a00e" +
                "c5290e9f91a6fa095c3c0263956c49136d7da9a01a6d51f7da2563106992bff97978063b6b54c9d9b0cf785fc1fea1c8112937023644a3fbaf7502e9f00722a4" +
                "92f9ffe08e90d41ce547d946216acb750f91662b4fd37418b7a6d1f7bf19746cb19167970d10fa299d5b1bf6882e07da44f5fcdc368c7c813b0010bf36292e26" +
                "de7eea0d8a132d6e33e620f06c18305133910551d4deca54e003d50cb616a650fb2601fec0db8bb393329693a762e8ed819da1f160a8926c5093843d0f624cee" +
                "4b52e281d4292bf39ee9866a00a5b11eb089e7c2b54a771b96c89d13c7b9202e28eeed80e62077c80da09b182ac314b8981e61685cdaf203665821dcc2db4a38" +
                "e3b0b1ab39f89379349f8e412482664e42a3b0e06c763df5f5d7d9605dbfbb9990bba8846ef338f1fa02f35a06d6ddbe341040e298fb594d4a30087a1383b20e" +
                "3ccf6d68f3bf1dd2e8b52ff1170c6e69a961108a25b9a7dfaae6e1620fed285f138d952bb4a3ade3c6b259f00fc463905cc15712b168bee3d770ac2fe9fbe966" +
                "87e6772ce041a2d0735b7cf8dc16222fd0cbdf81bf49c5f540e59530a61296a44825a60910c688ffcb10c000f33569db3ada6d257f457b2eaeffb3dea50ff45a" +
                "5314534f81557c8e2a4ebee09d675ca7bb137933e1cee46418e42437ad";

        String hugeUncipheredFrame = "c402c10000000001008203f8018200b00204120008110009060000010000ff0202010902030f0116030002030f0216030002030f031" +
                "6030002030f0416030002030f0516030002030f0616030002030f0716030002030f0816030002030f09160300010602020f0103ff02020f0203ff02020f0303f" +
                "f02020f0403ff02020f0503ff02020f0603ff020412001d110009060000020100ff0202010602030f0116030002030f0216030002030f0316030002030f04160" +
                "30002030f0516030002030f06160300010002041200091100090600000a0000ff0202010202030f0116030002030f02160300010102020f0103ff02041200091" +
                "100090600000a0064ff0202010202030f0116030002030f02160300010102020f0103ff02041200091100090600000a006aff0202010202030f0116030002030" +
                "f02160300010102020f0103ff020412000b1100090600000b0000ff0202010202030f0116030002030f02160300010202020f0103ff02020f0203ff020412001" +
                "41100090600000d0000ff0202010a02030f0116030002030f0216030002030f0316030002030f0416030002030f0516030002030f0616030002030f071603000" +
                "2030f0816030002030f0916030002030f0a160300010102020f0103ff02041200161100090600000f0000ff0202010402030f0116030002030f0216030002030" +
                "f0316030002030f04160300010002041200161100090600000f0001ff0202010402030f0116030002030f0216030002030f0316030002030f041603000100020" +
                "41200161100090600000f0002ff0202010402030f0116030002030f0216030002030f0316030002030f0416030001000204120047110009060000110000ff020" +
                "2010b02030f0116030002030f0216030002030f0316030002030f0416030002030f0516030002030f0616030002030f0716030002030f0816030002030f09160" +
                "30002030f0a16030002030f0b16030001000204120007110109060000150000ff0202010802030f0116030002030f0216030002030f0316030002030f0416030" +
                "002030f0516030002030f0616030002030f0716030002030f08160300010202020f0103ff02020f0203ff0204120019110009060000180000ff0202010502030" +
                "f0116030002030f0216030002030f0316030002030f0416030002030f0516030001000204120029110009060000190000ff0202010602030f0116030002030f0" +
                "216030002030f0316030002030f0416030002030f0516030002030f061603000100020412002a110009060000190100ff0202010a02030f0116030002030f021" +
                "6030002030f0316030002030f0416030002030f0516030002030f0616030002030f0716030002030f0816030002030f0916030002030f0a160300010302020f0" +
                "103ff02020f0203ff02020f0303ff";

        String key = "43218765AABBCCDD55443322ABABCDCD";
        byte[] systemIdentifier = new byte[]{(byte) 0x4B, (byte) 0x41, (byte) 0x4D, 0x00, 0x00, 0x00, 0x00, 0x09};

        MockSecurityProvider msp = new MockSecurityProvider();
        msp.setGlobalkey(DLMSUtils.hexStringToByteArray(key));
        msp.setDedicatedKey(DLMSUtils.hexStringToByteArray(key));

        SecurityContext sc = new SecurityContext(SecurityPolicy.SECURITYPOLICY_ENCRYPTION, 0, 0, systemIdentifier, msp, CipheringType.GLOBAL.getType(), false);
        sc.setResponseSystemTitle(systemIdentifier);
        byte[] unCipherResponse = sc.dataTransportDecryption(DLMSUtils.hexStringToByteArray(hugeFrame));

        assertArrayEquals(DLMSUtils.hexStringToByteArray(hugeUncipheredFrame), unCipherResponse);
    }

    @Test
    public void logicalDataEncryptionExampleFrame() throws IOException {
        byte[] sysTitle = DLMSUtils.hexStringToByteArray("4D4D4D0000BC614E");
        int frameCounter = 0x01234566;
        String frame = "C0010000080000010000FF0200";
        String cipheredFrame = "1E3001234566438AE72E709AF6AAAF4149CC13C17D7E1FF45037FB5BA1B6F9";
        String globalKey = "000102030405060708090A0B0C0D0E0F";
        String authenticationKey = "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF";
        String dedicatedKey = "000102030405060708090A0B0C0D0E0F";

        MockSecurityProvider msp = new MockSecurityProvider();
        msp.setAuthenticationKey(DLMSUtils.hexStringToByteArray(authenticationKey));
        msp.setGlobalkey(DLMSUtils.hexStringToByteArray(globalKey));
        msp.setDedicatedKey(DLMSUtils.hexStringToByteArray(dedicatedKey));

        // Both authentication and encryption
        SecurityContext sc = new SecurityContext(SecurityPolicy.SECURITYPOLICY_BOTH, 0, 0, sysTitle, msp, CipheringType.GLOBAL.getType(), false);
        sc.setFrameCounter(frameCounter);
        sc.setResponseSystemTitle(sysTitle);
        byte[] cipheredResponse = sc.dataTransportEncryption(DLMSUtils.hexStringToByteArray(frame));
        assertArrayEquals(DLMSUtils.hexStringToByteArray(cipheredFrame), cipheredResponse);

    }

    @Test
    public void logicalDataTransportEncryptionTest() throws IOException {
        String globalKey = "000102030405060708090A0B0C0D0E0F";
        String dedicatedKey = "000102030405060708090A0B0C0D0E0F";
        String authenticationKey = "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF";
        String unSecuredRequest = "C0010000080000010000FF0200";
        String testDecryptA = "1E1001234566C0010000080000010000FF020027AA7B2E5030D8F930323F0A";
        String testDecryptE = "122001234566843858BD995E0B30DD996ABB08";
        String testDecryptAE = "1E3001234566843858BD995E0B30DD996ABB08C0F222CC5B8E157DEE2B9C6D";

        MockSecurityProvider msp = new MockSecurityProvider();
        msp.setAuthenticationKey(DLMSUtils.hexStringToByteArray(authenticationKey));
        msp.setGlobalkey(DLMSUtils.hexStringToByteArray(globalKey));
        msp.setDedicatedKey(DLMSUtils.hexStringToByteArray(dedicatedKey));

        // Only authentication
        SecurityContext sc = new SecurityContext(SecurityPolicy.SECURITYPOLICY_AUTHENTICATION, 0, 0, systemTitle, msp, CipheringType.GLOBAL.getType(), false);
        sc.setFrameCounter(19088742);         // this is '0x01234567'
        byte[] cipheredResponse = sc.dataTransportEncryption(DLMSUtils.hexStringToByteArray(unSecuredRequest));
        assertArrayEquals(DLMSUtils.hexStringToByteArray(testDecryptA), cipheredResponse);

        // Only encryption
        sc = new SecurityContext(SecurityPolicy.SECURITYPOLICY_ENCRYPTION, 0, 0, systemTitle, msp, CipheringType.GLOBAL.getType(), false);
        sc.setFrameCounter(19088742);         // this is '0x01234567'
        cipheredResponse = sc.dataTransportEncryption(DLMSUtils.hexStringToByteArray(unSecuredRequest));
        assertArrayEquals(DLMSUtils.hexStringToByteArray(testDecryptE), cipheredResponse);

        // Both authentication and encryption
        sc = new SecurityContext(SecurityPolicy.SECURITYPOLICY_BOTH, 0, 0, systemTitle, msp, CipheringType.GLOBAL.getType(), false);
        sc.setFrameCounter(19088742);         // this is '0x01234567'
        cipheredResponse = sc.dataTransportEncryption(DLMSUtils.hexStringToByteArray(unSecuredRequest));
        assertArrayEquals(DLMSUtils.hexStringToByteArray(testDecryptAE), cipheredResponse);
    }

    @Test
    public void realTimeTestAuthenticationTag() throws IOException {

        String unSecuredRequest = "c001c100010000600200ff0200";

        String globalKey = "12348765AABBCCDD55443322ABABCDCD";
        String authenticationKey = "43218765AABBCCDD55443322ABABCDCD";
        String testDecryptA = "1E10DD1628BAC001C100010000600200FF02004B61FA5E6DC56FE6CEF36654";

        MockSecurityProvider msp = new MockSecurityProvider();
        msp.setAuthenticationKey(DLMSUtils.hexStringToByteArray(authenticationKey));
        msp.setGlobalkey(DLMSUtils.hexStringToByteArray(globalKey));

        byte[] temp = new byte[]{(byte) 0x4B, (byte) 0x41, (byte) 0x4D, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x51};
        // Only authentication
        SecurityContext sc = new SecurityContext(SecurityPolicy.SECURITYPOLICY_AUTHENTICATION, 0, 0, temp, msp, CipheringType.GLOBAL.getType(), false);
        sc.setFrameCounter(Long.valueOf("3709216954"));         // this is '0x01234567'
        byte[] cipheredResponse = sc.dataTransportEncryption(DLMSUtils.hexStringToByteArray(unSecuredRequest));
        assertArrayEquals(DLMSUtils.hexStringToByteArray(testDecryptA), cipheredResponse);

    }

    @Test
    public void iskraHLSTest() throws IOException, DLMSConnectionException, NoSuchAlgorithmException {
        String digest = "3F2E2FA23A4CFCD40B0CD7091300673D";

        AssociationControlServiceElement acse = new AssociationControlServiceElement(null, 1, new SecurityContext(3, 3, 3, new MockSecurityProvider(), CipheringType.GLOBAL.getType(), false));
        String hlSecurityResponse = "614AA109060760857405080101A203020100A305A10302010E88020780890760857405080203AA1280104C332F6263564447365070506C556455BE10040E0800065F1F040000FC1F04000007";
        acse.analyzeAARE(DLMSUtils.hexStringToByteArray(hlSecurityResponse));
        MockSecurityProvider msp = new MockSecurityProvider();
//			msp.setHLSSecretString("12345678");
//			msp.setHLSSecretByteArray("12345678".getBytes());
//			msp.setHLSSecretByteArray(new byte[]{0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte)0x88, (byte)0x99, (byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xEE, (byte)0xFF});
        msp.setHLSSecretByteArray(DLMSUtils.hexStringToByteArray("00112233445566778899AABBCCDDEEFF"));
        msp.setCallingAuthenticationValue(acse.getRespondingAuthenticationValue());
        SecurityContext sc = new SecurityContext(SecurityPolicy.SECURITYPOLICY_NONE, 3, 0, null, msp, CipheringType.GLOBAL.getType(), false);

        byte[] plainText = ProtocolUtils.concatByteArrays(sc.getSecurityProvider().getCallingAuthenticationValue(), sc.getSecurityProvider().getHLSSecret());

        assertArrayEquals(DLMSUtils.hexStringToByteArray(digest), sc.associationEncryption(plainText));

    }

    @Test
    public void incorrectFrameCounterTest() throws IOException, DLMSConnectionException {
        MockSecurityProvider msp = new MockSecurityProvider();
        msp.setRespondingFrameCounterHandling(new MockRespondingFrameCounterHandler());
        SecurityContext sc = new SecurityContext(SecurityPolicy.SECURITYPOLICY_AUTHENTICATION, 0, 0, systemTitle, msp, CipheringType.GLOBAL.getType(), false);
        sc.setResponseFrameCounter(0);
        assertEquals(new Long(0), sc.getResponseFrameCounter());
        sc.setResponseFrameCounter(1);
        assertEquals(new Long(1), sc.getResponseFrameCounter());
        try {
            sc.setResponseFrameCounter(1);
            fail("Should get a DLMSConnectionException for retrying the FrameCounter");
        } catch (DLMSConnectionException e) {
            assertTrue(e.getMessage().indexOf("Received incorrect FrameCounter") >= 0);
        }
        sc.setResponseFrameCounter(2);
        assertEquals(new Long(2), sc.getResponseFrameCounter());

        try {
            sc.setResponseFrameCounter(0xFFFFFFFFl);
            fail("Should get a DLMSConnectionException beacuse we reached the maximum frame counter value");
        } catch (DLMSConnectionException e) {
            assertTrue(e.getMessage().indexOf("FrameCounter reached the maximum value") >= 0);
        }
        try {
            sc.setResponseFrameCounter(1);
            fail("Should get a DLMSConnectionException for retrying the FrameCounter");
        } catch (DLMSConnectionException e) {
            assertTrue(e.getMessage().indexOf("Received incorrect FrameCounter") >= 0);
        }
        sc.getSecurityProvider().getRespondingFrameCounterHandler().setRespondingFrameCounter(-1);
        sc.setResponseFrameCounter(0);
        assertEquals(new Long(0), sc.getResponseFrameCounter());
    }
}
