package com.energyict.protocolimpl.ansi.c12.EAXPrime;

import com.energyict.protocolimpl.utils.ProtocolTools;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Test class written for EAXPrimeEncoder class
 * All test cases are taken from document "EAX' Cipher Mode (May 2011)", provided by National Institute of Standards and Technology.
 *
 * Copyrights EnergyICT
 * User: sva
 * Date: 5/09/12
 * Time: 13:27
 */
public class EAXPrimeEncoderTest extends TestCase {

    @Test
    // Test vector #1 of document EAX' Cipher Mode (May 2011)
    public void testEAXPrimeCryptography_Encrypt_TestVector1() throws Exception {
        byte[] encryptionKey = ProtocolTools.getBytesFromHexString("01020304050607080102030405060708", "");
        EAXPrimeEncoder encoder = new EAXPrimeEncoder(encryptionKey);

        byte[] clearText = ProtocolTools.getBytesFromHexString("a20c060a607c86f7540116001702a703" +
                "020104a803020102ac0fa20da00ba109" +
                "800102810448e99388be19281781159a" +
                "a60d060b607c86f75401160017821102" +
                "48e99388", "");

        byte[] plainText = ProtocolTools.getBytesFromHexString("54454d500b40000700051a00000200e4", "");

        encoder.encrypt(clearText, plainText);
        byte[] cipherText = encoder.getCipherText();
        byte[] mac = encoder.getMac();

        byte[] expectedCipherText = ProtocolTools.getBytesFromHexString("4031cc957d4edf9a357f3db0fa9fe838", "");
        byte[] expectedMac = ProtocolTools.getBytesFromHexString("6555c029", "");

        assertEquals("CipherText doesn't match.", ProtocolTools.getHexStringFromBytes(expectedCipherText, ""), ProtocolTools.getHexStringFromBytes(cipherText, ""));
        assertEquals("Mac doesn't match.", ProtocolTools.getHexStringFromBytes(expectedMac, ""), ProtocolTools.getHexStringFromBytes(mac, ""));
    }

    @Test
    // Test vector #1 of document EAX' Cipher Mode (May 2011)
    public void testEAXPrimeCryptography_Decrypt_TestVector1() throws Exception {
        byte[] encryptionKey = ProtocolTools.getBytesFromHexString("01020304050607080102030405060708", "");
        EAXPrimeEncoder encoder = new EAXPrimeEncoder(encryptionKey);

        byte[] clearText = ProtocolTools.getBytesFromHexString("a20c060a607c86f7540116001702a703" +
                "020104a803020102ac0fa20da00ba109" +
                "800102810448e99388be19281781159a" +
                "a60d060b607c86f75401160017821102" +
                "48e99388", "");

        byte[] expectedPlainText = ProtocolTools.getBytesFromHexString("54454d500b40000700051a00000200e4", "");

        byte[] cipherText = ProtocolTools.getBytesFromHexString("4031cc957d4edf9a357f3db0fa9fe838", "");

        byte[] mac = ProtocolTools.getBytesFromHexString("6555c029", "");

         byte[] plainText = encoder.decrypt(clearText, cipherText, mac);

        assertEquals("PlainText doesn't match.", ProtocolTools.getHexStringFromBytes(expectedPlainText, ""), ProtocolTools.getHexStringFromBytes(plainText, ""));
    }

    @Test
    // Test vector #2 of document EAX' Cipher Mode (May 2011)
    public void testEAXPrimeCryptography_Encrypt_TestVector2() throws Exception {
        byte[] encryptionKey = ProtocolTools.getBytesFromHexString("01020304050607080102030405060708", "");
        EAXPrimeEncoder encoder = new EAXPrimeEncoder(encryptionKey);

        byte[] clearText = ProtocolTools.getBytesFromHexString("a20c060a607c86f7540116007b02a703" +
                "020104a803020102ac0fa20da00ba109" +
                "800102810448f3d2f8be19281781159a" +
                "a60d060b607c86f7540116007b821102" +
                "48f3d2f8", "");

        byte[] plainText = ProtocolTools.getBytesFromHexString("54454d500b40000700051a00000200e4", "");

        encoder.encrypt(clearText, plainText);
        byte[] cipherText = encoder.getCipherText();
        byte[] mac = encoder.getMac();

        byte[] expectedCipherText = ProtocolTools.getBytesFromHexString("8d2fbb7a0a8c4d40edaa10a46431c9b8", "");
        byte[] expectedMac = ProtocolTools.getBytesFromHexString("fec6d9e8", "");

        assertEquals("CipherText doesn't match.", ProtocolTools.getHexStringFromBytes(expectedCipherText, ""), ProtocolTools.getHexStringFromBytes(cipherText, ""));
        assertEquals("Mac doesn't match.", ProtocolTools.getHexStringFromBytes(expectedMac, ""), ProtocolTools.getHexStringFromBytes(mac, ""));
    }

     @Test
    // Test vector #2 of document EAX' Cipher Mode (May 2011)
    public void testEAXPrimeCryptography_Decrypt_TestVector2() throws Exception {
        byte[] encryptionKey = ProtocolTools.getBytesFromHexString("01020304050607080102030405060708", "");
        EAXPrimeEncoder encoder = new EAXPrimeEncoder(encryptionKey);

        byte[] clearText = ProtocolTools.getBytesFromHexString("a20c060a607c86f7540116007b02a703" +
                "020104a803020102ac0fa20da00ba109" +
                "800102810448f3d2f8be19281781159a" +
                "a60d060b607c86f7540116007b821102" +
                "48f3d2f8", "");

        byte[] expectedPlainText = ProtocolTools.getBytesFromHexString("54454d500b40000700051a00000200e4", "");

        byte[] cipherText = ProtocolTools.getBytesFromHexString("8d2fbb7a0a8c4d40edaa10a46431c9b8", "");

        byte[] mac = ProtocolTools.getBytesFromHexString("fec6d9e8", "");

         byte[] plainText = encoder.decrypt(clearText, cipherText, mac);

        assertEquals("PlainText doesn't match.", ProtocolTools.getHexStringFromBytes(expectedPlainText, ""), ProtocolTools.getHexStringFromBytes(plainText, ""));
    }

    @Test
    // Test vector #3 of document EAX' Cipher Mode (May 2011)
    public void testEAXPrimeCryptography_Encrypt_TestVector3() throws Exception {
        byte[] encryptionKey = ProtocolTools.getBytesFromHexString("102030405060708090a0b0c0d0e0f000", "");
        EAXPrimeEncoder encoder = new EAXPrimeEncoder(encryptionKey);

        byte[] clearText = ProtocolTools.getBytesFromHexString("a20e060c6086480186fc2f811caa4e01" +
                "a806020439a00ebbac0fa20da00ba109" +
                "80010081044bcee2c3be252823812188" +
                "a60a06082b06010401828563004bcee2" +
                "c3", "");

        byte[] plainText = ProtocolTools.getBytesFromHexString("17513030303030303030303030303030" +
                "303030303030000003300001", "");

        encoder.encrypt(clearText, plainText);
        byte[] cipherText = encoder.getCipherText();
        byte[] mac = encoder.getMac();

        byte[] expectedCipherText = ProtocolTools.getBytesFromHexString("9cf32c7ec24c250be7b0749feee71a22" +
                "0d0eee976ec23dbf0caa08ea", "");
        byte[] expectedMac = ProtocolTools.getBytesFromHexString("00543e66", "");

        assertEquals("CipherText doesn't match.", ProtocolTools.getHexStringFromBytes(expectedCipherText, ""), ProtocolTools.getHexStringFromBytes(cipherText, ""));
        assertEquals("Mac doesn't match.", ProtocolTools.getHexStringFromBytes(expectedMac, ""), ProtocolTools.getHexStringFromBytes(mac, ""));
    }

     @Test
    // Test vector #1 of document EAX' Cipher Mode (May 2011)
    public void testEAXPrimeCryptography_Decrypt_TestVector3() throws Exception {
        byte[] encryptionKey = ProtocolTools.getBytesFromHexString("102030405060708090a0b0c0d0e0f000", "");
        EAXPrimeEncoder encoder = new EAXPrimeEncoder(encryptionKey);

        byte[] clearText = ProtocolTools.getBytesFromHexString("a20e060c6086480186fc2f811caa4e01" +
                "a806020439a00ebbac0fa20da00ba109" +
                "80010081044bcee2c3be252823812188" +
                "a60a06082b06010401828563004bcee2" +
                "c3", "");

        byte[] expectedPlainText = ProtocolTools.getBytesFromHexString("17513030303030303030303030303030" +
                "303030303030000003300001", "");

        byte[] cipherText = ProtocolTools.getBytesFromHexString("9cf32c7ec24c250be7b0749feee71a22" +
                "0d0eee976ec23dbf0caa08ea", "");

        byte[] mac = ProtocolTools.getBytesFromHexString("00543e66", "");

        byte[] plainText = encoder.decrypt(clearText, cipherText, mac);

        assertEquals("PlainText doesn't match.", ProtocolTools.getHexStringFromBytes(expectedPlainText, ""), ProtocolTools.getHexStringFromBytes(plainText, ""));
    }

    @Test
    // Test vector #4 of document EAX' Cipher Mode (May 2011)
    public void testEAXPrimeCryptography_Encrypt_TestVector4() throws Exception {
        byte[] encryptionKey = ProtocolTools.getBytesFromHexString("6624c7e23034e4036fe5cb3a8b5dab44", "");
        EAXPrimeEncoder encoder = new EAXPrimeEncoder(encryptionKey);

        byte[] clearText = ProtocolTools.getBytesFromHexString("a211060f2b060104018285638e7f85f1" +
                "c24e01a80602042bc81aa1ac0fa20da0" +
                "0ba10980010081044b97d2ccbe392837" +
                "813588a60906072b060104828563004b" +
                "97d2cc", "");

        byte[] plainText = ProtocolTools.getBytesFromHexString("17513030303030303030303030303030" +
                "30303030303000000330000103300078" +
                "033000790330007a0330007b0330007d", "");

        encoder.encrypt(clearText, plainText);
        byte[] cipherText = encoder.getCipherText();
        byte[] mac = encoder.getMac();

        byte[] expectedCipherText = ProtocolTools.getBytesFromHexString("beb0989fadb020eb72ba46353cc0a2ac" +
                "2a007a101afebaf9680d3b9659f99112" +
                "1b865f254f6ac92cdd213d31e3c4d2ca", "");
        byte[] expectedMac = ProtocolTools.getBytesFromHexString("e6f89b6d", "");

        assertEquals("CipherText doesn't match.", ProtocolTools.getHexStringFromBytes(expectedCipherText, ""), ProtocolTools.getHexStringFromBytes(cipherText, ""));
        assertEquals("Mac doesn't match.", ProtocolTools.getHexStringFromBytes(expectedMac, ""), ProtocolTools.getHexStringFromBytes(mac, ""));
    }

    @Test
    // Test vector #4 of document EAX' Cipher Mode (May 2011)
    public void testEAXPrimeCryptography_Decrypt_TestVector4() throws Exception {
        byte[] encryptionKey = ProtocolTools.getBytesFromHexString("6624c7e23034e4036fe5cb3a8b5dab44", "");
        EAXPrimeEncoder encoder = new EAXPrimeEncoder(encryptionKey);

        byte[] clearText = ProtocolTools.getBytesFromHexString("a211060f2b060104018285638e7f85f1" +
                "c24e01a80602042bc81aa1ac0fa20da0" +
                "0ba10980010081044b97d2ccbe392837" +
                "813588a60906072b060104828563004b" +
                "97d2cc", "");

        byte[] expectedPlainText = ProtocolTools.getBytesFromHexString("17513030303030303030303030303030" +
                "30303030303000000330000103300078" +
                "033000790330007a0330007b0330007d", "");

         byte[] cipherText = ProtocolTools.getBytesFromHexString("beb0989fadb020eb72ba46353cc0a2ac" +
                "2a007a101afebaf9680d3b9659f99112" +
                "1b865f254f6ac92cdd213d31e3c4d2ca", "");

        byte[] mac = ProtocolTools.getBytesFromHexString("e6f89b6d", "");

        byte[] plainText =  encoder.decrypt(clearText, cipherText, mac);

        assertEquals("PlainText doesn't match.", ProtocolTools.getHexStringFromBytes(expectedPlainText, ""), ProtocolTools.getHexStringFromBytes(plainText, ""));
    }
}