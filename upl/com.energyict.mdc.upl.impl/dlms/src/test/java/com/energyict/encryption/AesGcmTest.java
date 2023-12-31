package com.energyict.encryption;

import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.security.GeneralSecurityException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AesGcmTest {

    /**
     * AES-256 key.
     */
    private static final byte[] ENCRYPTION_KEY_AES256_AUTH_ENC = ProtocolTools.getBytesFromHexString("E3C08A8F06C6E3AD95A70557B23F75483CE33021A9C72B7025666204C69C0B72", "");

    /**
     * Plaintext.
     */
    private static final byte[] PLAINTEXT_AES256_AUTH_ENC = ProtocolTools.getBytesFromHexString("08000F101112131415161718191A1B1C" +
            "1D1E1F202122232425262728292A2B2C" +
            "2D2E2F303132333435363738393A0002", "");

    /**
     * Additional data.
     */
    private static final byte[] ADDITIONAL_DATA_AES256_AUTH_ENC = ProtocolTools.getBytesFromHexString("D609B1F056637A0D46DF998D88E52E00B2C2846512153524C0895E81", "");

    /**
     * IV with AES256.
     */
    private static final byte[] IV_AES256_AUTH_ENC = ProtocolTools.getBytesFromHexString("12153524C0895E81B2C28465", "");

    /**
     * Expected cipher test for AES-256.
     */
    private static final byte[] AES256_CIPHER_TEXT = ProtocolTools.getBytesFromHexString("E2006EB42F5277022D9B19925BC419D7" +
            "A592666C925FE2EF718EB4E308EFEAA7" +
            "C5273B394118860A5BE2A97F56AB7836", "");

    /**
     * Expected tag.
     */
    private static final byte[] AES256_EXPECTED_TAG = ProtocolTools.getBytesFromHexString("5CA597CDBB3EDB8D1A1151EA0AF7B436", "");

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testBitVectorInit() {
        String r = "e1000000000000000000000000000000";
        BitVector R = new BitVector(r);
        assertEquals(0xe1, (R.getValue(0) & 0xFF));
        assertEquals(r, R.toString());

    }

    @Test
    public void testMultiplication() {
        String c = "0388dace60b6a392f328c2b971b2fe78";
        String h = "66e94bd4ef8a2c3b884cfa59ca342b2e";
        String r = "5e2ec746917062882c85b0685353deb7";

        BitVector C = new BitVector(c);
        BitVector H = new BitVector(h);

        BitVector R = BitVector.multiplication(C, H);

        assertEquals(r, R.toString());
    }

    @Test
    public void testAesEncryption1() {
        String key = "00000000000000000000000000000000";
        AesGcm testFixture = new AesGcm(new BitVector(key));
        String y0 = "00000000000000000000000000000001";
        BitVector plain = new BitVector(y0);
        BitVector result = testFixture.aesEncrypt(plain);
        assertEquals(result.toString(), "58e2fccefa7e3061367f1d57a4e7455a");
    }

    @Test
    public void testAesEncryption2() {
        String key = "feffe9928665731c6d6a8f9467308308";
        AesGcm testFixture = new AesGcm(new BitVector(key));
        String y0 = "cafebabefacedbaddecaf88800000001";
        BitVector plain = new BitVector(y0);
        BitVector result = testFixture.aesEncrypt(plain);
        assertEquals(result.toString(), "3247184b3c4f69a44dbcd22887bbb418");
    }

    @Test
    public void testAesEncryption3() {
        String key = "feffe9928665731c6d6a8f9467308308";
        AesGcm testFixture = new AesGcm(new BitVector(key));
        String y0 = "cafebabefacedbaddecaf88800000002";
        BitVector plain = new BitVector(y0);
        BitVector result = testFixture.aesEncrypt(plain);
        assertEquals(result.toString(), "9bb22ce7d9f372c1ee2b28722b25f206");
    }

    @Test
    public void testAesGcmEncryption1() {
        String key = "00000000000000000000000000000000";
        String p = "";
        String iv = "000000000000000000000000";
        String a = "";
        AesGcm testFixture = new AesGcm(new BitVector(key));
        testFixture.setInitializationVector(new BitVector(iv));
        testFixture.setPlainText(new BitVector(p));
        testFixture.setAdditionalAuthenticationData(new BitVector(a));

        testFixture.encrypt();
        assertEquals(testFixture.getCipherText().toString(), "");
        assertEquals(testFixture.getTag().toString(), "58e2fccefa7e3061367f1d57a4e7455a");
    }

    @Test
    public void testAesGcmDecryption1() {
        String key = "00000000000000000000000000000000";
        String c = "";
        String t = "58e2fccefa7e3061367f1d57a4e7455a";
        String iv = "000000000000000000000000";
        String a = "";
        AesGcm testFixture = new AesGcm(new BitVector(key));
        testFixture.setInitializationVector(new BitVector(iv));
        testFixture.setAdditionalAuthenticationData(new BitVector(a));
        testFixture.setCipherText(new BitVector(c));
        testFixture.setTag(new BitVector(t));

        assertTrue(testFixture.decrypt());
        assertEquals(testFixture.getPlainText().toString(), "");
    }

    @Test
    public void testAesGcmEncryption2() {
        String key = "00000000000000000000000000000000";
        String p = "00000000000000000000000000000000";
        String iv = "000000000000000000000000";
        String a = "";
        AesGcm testFixture = new AesGcm(new BitVector(key));
        testFixture.setInitializationVector(new BitVector(iv));
        testFixture.setPlainText(new BitVector(p));
        testFixture.setAdditionalAuthenticationData(new BitVector(a));

        testFixture.encrypt();
        assertEquals(testFixture.getCipherText().toString(), "0388dace60b6a392f328c2b971b2fe78");
        assertEquals(testFixture.getTag().toString(), "ab6e47d42cec13bdf53a67b21257bddf");
    }

    @Test
    public void testAesGcmDecryption2() {
        String key = "00000000000000000000000000000000";
        String c = "0388dace60b6a392f328c2b971b2fe78";
        String t = "ab6e47d42cec13bdf53a67b21257bddf";
        String iv = "000000000000000000000000";
        String a = "";
        AesGcm testFixture = new AesGcm(new BitVector(key));
        testFixture.setInitializationVector(new BitVector(iv));
        testFixture.setAdditionalAuthenticationData(new BitVector(a));
        testFixture.setCipherText(new BitVector(c));
        testFixture.setTag(new BitVector(t));

        assertTrue(testFixture.decrypt());
        assertEquals(testFixture.getPlainText().toString(), "00000000000000000000000000000000");
    }


    @Test
    public void testAesGcmEncryption3() {
        String key = "feffe9928665731c6d6a8f9467308308";
        String p = "d9313225f88406e5a55909c5aff5269a" +
                "86a7a9531534f7da2e4c303d8a318a72" +
                "1c3c0c95956809532fcf0e2449a6b525" +
                "b16aedf5aa0de657ba637b391aafd255";
        String iv = "cafebabefacedbaddecaf888";
        String a = "";
        AesGcm testFixture = new AesGcm(new BitVector(key));
        testFixture.setInitializationVector(new BitVector(iv));
        testFixture.setPlainText(new BitVector(p));
        testFixture.setAdditionalAuthenticationData(new BitVector(a));

        testFixture.encrypt();

        assertEquals(testFixture.getCipherText().toString(), "42831ec2217774244b7221b784d0d49c" +
                "e3aa212f2c02a4e035c17e2329aca12e" +
                "21d514b25466931c7d8f6a5aac84aa05" +
                "1ba30b396a0aac973d58e091473f5985");
        assertEquals(testFixture.getTag().toString(), "4d5c2af327cd64a62cf35abd2ba6fab4");
    }

    @Test
    public void testAesGcmDecryption3() {
        String key = "feffe9928665731c6d6a8f9467308308";
        String c = "42831ec2217774244b7221b784d0d49c" +
                "e3aa212f2c02a4e035c17e2329aca12e" +
                "21d514b25466931c7d8f6a5aac84aa05" +
                "1ba30b396a0aac973d58e091473f5985";
        String t = "4d5c2af327cd64a62cf35abd2ba6fab4";
        String iv = "cafebabefacedbaddecaf888";
        String a = "";
        AesGcm testFixture = new AesGcm(new BitVector(key));
        testFixture.setInitializationVector(new BitVector(iv));
        testFixture.setAdditionalAuthenticationData(new BitVector(a));
        testFixture.setCipherText(new BitVector(c));
        testFixture.setTag(new BitVector(t));

        assertTrue(testFixture.decrypt());
        assertEquals(testFixture.getPlainText().toString(), "d9313225f88406e5a55909c5aff5269a" +
                "86a7a9531534f7da2e4c303d8a318a72" +
                "1c3c0c95956809532fcf0e2449a6b525" +
                "b16aedf5aa0de657ba637b391aafd255");
    }


    @Test
    public void testAesGcmEncryption4() {
        String key = "feffe9928665731c6d6a8f9467308308";
        String p = "d9313225f88406e5a55909c5aff5269a" +
                "86a7a9531534f7da2e4c303d8a318a72" +
                "1c3c0c95956809532fcf0e2449a6b525" +
                "b16aedf5aa0de657ba637b39";
        String iv = "cafebabefacedbaddecaf888";
        String a = "feedfacedeadbeeffeedfacedeadbeef" +
                "abaddad2";
        AesGcm testFixture = new AesGcm(new BitVector(key));
        testFixture.setInitializationVector(new BitVector(iv));
        testFixture.setPlainText(new BitVector(p));
        testFixture.setAdditionalAuthenticationData(new BitVector(a));

        testFixture.encrypt();

        assertEquals(testFixture.getCipherText().toString(), "42831ec2217774244b7221b784d0d49c" +
                "e3aa212f2c02a4e035c17e2329aca12e" +
                "21d514b25466931c7d8f6a5aac84aa05" +
                "1ba30b396a0aac973d58e091");
        assertEquals(testFixture.getTag().toString(), "5bc94fbc3221a5db94fae95ae7121a47");
    }

    @Test
    public void testAesGcmDecryption4() {
        String key = "feffe9928665731c6d6a8f9467308308";
        String c = "42831ec2217774244b7221b784d0d49c" +
                "e3aa212f2c02a4e035c17e2329aca12e" +
                "21d514b25466931c7d8f6a5aac84aa05" +
                "1ba30b396a0aac973d58e091";
        String t = "5bc94fbc3221a5db94fae95ae7121a47";
        String iv = "cafebabefacedbaddecaf888";
        String a = "feedfacedeadbeeffeedfacedeadbeef" +
                "abaddad2";
        AesGcm testFixture = new AesGcm(new BitVector(key));
        testFixture.setInitializationVector(new BitVector(iv));
        testFixture.setAdditionalAuthenticationData(new BitVector(a));
        testFixture.setCipherText(new BitVector(c));
        testFixture.setTag(new BitVector(t));

        assertTrue(testFixture.decrypt());
        assertEquals(testFixture.getPlainText().toString(), "d9313225f88406e5a55909c5aff5269a" +
                "86a7a9531534f7da2e4c303d8a318a72" +
                "1c3c0c95956809532fcf0e2449a6b525" +
                "b16aedf5aa0de657ba637b39");
    }

    @Test
    public void testPiet() {
        String eKey = "43218765AABBCCDD55443322ABABCDCD";
        String akey = "12348765AABBCCDD55443322ABABCDCD";
        String iv = "4B414D0000000013C057F8A3";
        String ad = "3012348765AABBCCDD55443322ABABCDCD";
        String c = "D7F68E0A72DDE3A53367BDD49E";
        String at = "E58DC6BAA86EB6FD0B284DDD";

        AesGcm ag = new AesGcm(new BitVector(eKey));
        ag.setTagSize(12);
        ag.setInitializationVector(new BitVector(iv));
        ag.setAdditionalAuthenticationData(new BitVector(ad));
        ag.setCipherText(new BitVector(c));

        ag.decrypt();

    }

    /**
     * Tests authenticated encryption using AES-GCM-256.
     * If this test fails, it might be that the 'Java unlimited strength files' are missing.
     * Download the file from http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html
     * (or other similar link corresponding to the used java version), extract the jar files from the zip and
     * save them in ${java.home}/jre/lib/security/
     * See http://stackoverflow.com/questions/6481627/java-security-illegal-key-size-or-default-parameters
     */
    @Test
    public final void testAuthenticatedEncryption256() throws GeneralSecurityException {
        AesGcm aesGcm = new AesGcm(ENCRYPTION_KEY_AES256_AUTH_ENC, 12);

        aesGcm.setAdditionalAuthenticationData(new BitVector(ADDITIONAL_DATA_AES256_AUTH_ENC));
        aesGcm.setInitializationVector(new BitVector(IV_AES256_AUTH_ENC));
        aesGcm.setPlainText(new BitVector(PLAINTEXT_AES256_AUTH_ENC));
        aesGcm.encrypt();

        byte[] cipherText = aesGcm.getCipherText().getValue();
        byte[] tag = aesGcm.getTag().getValue();

        assertThat(cipherText).isEqualTo(AES256_CIPHER_TEXT);
        assertThat(tag).isEqualTo(ProtocolTools.getSubArray(AES256_EXPECTED_TAG, 0, 12));


        aesGcm = new AesGcm(ENCRYPTION_KEY_AES256_AUTH_ENC, 12);
        aesGcm.setCipherText(new BitVector(cipherText));
        aesGcm.setTag(new BitVector(tag));
        aesGcm.setAdditionalAuthenticationData(new BitVector(ADDITIONAL_DATA_AES256_AUTH_ENC));
        aesGcm.setInitializationVector(new BitVector(IV_AES256_AUTH_ENC));
        aesGcm.decrypt();

        assertThat(aesGcm.getPlainText().getValue()).isEqualTo(PLAINTEXT_AES256_AUTH_ENC);
    }
}