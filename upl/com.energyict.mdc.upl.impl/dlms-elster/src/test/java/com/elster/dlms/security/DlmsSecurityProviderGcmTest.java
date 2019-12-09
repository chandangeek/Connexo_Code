/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.dlms.security;

import com.elster.dlms.security.IDlmsSecurityProvider.DecodingResult;
import java.security.InvalidKeyException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import com.elster.dlms.cosem.application.services.common.SecurityControlField.CipheringMethod;
import com.elster.dlms.cosem.application.services.common.SecurityControlField;
import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.application.services.open.AuthenticationValue;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import javax.crypto.NoSuchPaddingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Examples from GB ed.7 p.133-134
 *
 * @author osse
 */
public class DlmsSecurityProviderGcmTest
{
  public DlmsSecurityProviderGcmTest()
  {
  }

  @BeforeClass
  public static void setUpClass() throws Exception
  {
  }

  @AfterClass
  public static void tearDownClass() throws Exception
  {
  }

  /**
   * Test of encode method, of class DlmsSecurityProviderGcm.
   */
  @Test
  public void testEncodeAuthenticate() throws Exception
  {
    System.out.println("encode: authenticate");

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");


    byte[] plaintext = CodingUtils.string2ByteArray("C0010000080000010000FF0200");
    SecurityControlField securityControlField = new SecurityControlField(0, true, false,
                                                                         CipheringMethod.GLOBAL_UNICAST);

    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle,
                                                                   systemTitle, 0x01234567);

    byte[] expResult = CodingUtils.string2ByteArray(
            "1001234567C0010000080000010000FF020006725D910F9221D263877516");
    byte[] result = instance.encode(plaintext, securityControlField);

    assertArrayEquals(expResult, result);
  }

  /**
   * Test of encode method, of class DlmsSecurityProviderGcm.
   */
  @Test
  public void testEncodeEncrypt() throws Exception
  {
    System.out.println("encode: encrypt");

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");


    byte[] plaintext = CodingUtils.string2ByteArray("C0010000080000010000FF0200");
    SecurityControlField securityControlField = new SecurityControlField(0, false, true,
                                                                         CipheringMethod.GLOBAL_UNICAST);

    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle,
                                                                   systemTitle, 0x01234567);

    byte[] expResult = CodingUtils.string2ByteArray("2001234567411312FF935A47566827C467BC");
    byte[] result = instance.encode(plaintext, securityControlField);

    assertArrayEquals(expResult, result);
  }

  /**
   * Test of encode method, of class DlmsSecurityProviderGcm.
   */
  @Test
  public void testEncodeFramecounter() throws Exception
  {
    System.out.println("encode: encrypt");

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");


    byte[] plaintext = CodingUtils.string2ByteArray("C0010000080000010000FF0200");
    SecurityControlField securityControlField = new SecurityControlField(0, false, true,
                                                                         CipheringMethod.GLOBAL_UNICAST);

    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle,
                                                                   systemTitle);

    String frame1 = CodingUtils.byteArrayToString(instance.encode(plaintext, securityControlField), "");
    long fc1 = Long.parseLong(frame1.substring(2, 2 + 8), 16);

    String frame2 = CodingUtils.byteArrayToString(instance.encode(plaintext, securityControlField), "");
    long fc2 = Long.parseLong(frame2.substring(2, 2 + 8), 16);
    assertEquals("Framecounter must increase", fc1 + 1, fc2);
  }

  /**
   * Test of encode method, of class DlmsSecurityProviderGcm.
   */
  @Test
  public void testEncodeAutheticatedEncryption() throws Exception
  {
    System.out.println("encode: autheticated encryption");

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");


    byte[] plaintext = CodingUtils.string2ByteArray("C0010000080000010000FF0200");
    SecurityControlField securityControlField = new SecurityControlField(0, true, true,
                                                                         CipheringMethod.GLOBAL_UNICAST);

    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle,
                                                                   systemTitle, 0x01234567);

    byte[] expResult = CodingUtils.string2ByteArray(
            "3001234567411312FF935A47566827C467BC7D825C3BE4A77C3FCC056B6B");
    byte[] result = instance.encode(plaintext, securityControlField);

    assertArrayEquals(expResult, result);
  }

  /**
   * xDLMS InitiateRequest (GB ed.7 p.265)
   */
  @Test
  public void testEncodeAutheticatedEncryption2() throws Exception
  {
    System.out.println("encode: autheticated encryption");

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");


    byte[] plaintext = CodingUtils.string2ByteArray(
            "01011000112233445566778899AABBCC DDEEFF0000065F1F0400007E1F04B0");
    SecurityControlField securityControlField = new SecurityControlField(0, true, true,
                                                                         CipheringMethod.GLOBAL_UNICAST);


    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle,
                                                                   systemTitle, 0x01234567);

    byte[] expResult =
            CodingUtils.string2ByteArray(
            "3001234567801302FF8A7874133D414CED25B42534D28DB0047720606B175BD52211BE6841DB204D39EE6FDB8E356855");
    byte[] result = instance.encode(plaintext, securityControlField);

    assertArrayEquals(expResult, result);
  }

  /**
   * Test of encode method, of class DlmsSecurityProviderGcm.
   */
  @Test
  public void testDecodeAuthenticated() throws Exception
  {
    System.out.println("decode: authenticated");

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");


    byte[] ciphertext = CodingUtils.string2ByteArray(
            "1001234567C0010000080000010000FF020006725D910F9221D263877516");

    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle,
                                                                   systemTitle, 0x01234567);

    byte[] expResult = CodingUtils.string2ByteArray("C0010000080000010000FF0200");
    DecodingResult result = instance.decode(ciphertext, SecurityControlField.CipheringMethod.GLOBAL_UNICAST);

    assertArrayEquals(expResult, result.getData());
    assertEquals(new SecurityControlField(0, true, false, CipheringMethod.GLOBAL_UNICAST), result.
            getSecurityControlField());
  }

  /**
   * Test of encode method, of class DlmsSecurityProviderGcm.
   */
  @Test(expected = CipherException.class)
  public void testDecodeAuthenticatedFailure() throws Exception
  {
    System.out.println("decode: authenticated (exp. failure)");

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");


    byte[] ciphertext = CodingUtils.string2ByteArray(
            "1001234567C0010000080000010000FF020006725D910F9221D263877426");

    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle,
                                                                   systemTitle, 0x01234567);

    byte[] expResult = CodingUtils.string2ByteArray("C0010000080000010000FF0200");
    DecodingResult result = instance.decode(ciphertext, SecurityControlField.CipheringMethod.GLOBAL_UNICAST);

    assertArrayEquals(expResult, result.getData());
    assertEquals(new SecurityControlField(0, true, false, CipheringMethod.GLOBAL_UNICAST), result.
            getSecurityControlField());
  }

  /**
   * Test of encode method, of class DlmsSecurityProviderGcm.
   */
  @Test
  public void testDecodeEncrypted() throws Exception
  {
    System.out.println("decode: encrypted");

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");


    byte[] ciphertext = CodingUtils.string2ByteArray(
            "2001234567411312FF935A47566827C467BC");

    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle,
                                                                   systemTitle, 0x01234567);

    byte[] expResult = CodingUtils.string2ByteArray("C0010000080000010000FF0200");
    DecodingResult result = instance.decode(ciphertext, SecurityControlField.CipheringMethod.GLOBAL_UNICAST);

    assertArrayEquals(expResult, result.getData());
    assertEquals(new SecurityControlField(0, false, true, CipheringMethod.GLOBAL_UNICAST), result.
            getSecurityControlField());
  }

  /**
   * Test of encode method, of class DlmsSecurityProviderGcm.
   */
  @Test(expected = CipherException.class)
  public void testDecodeFcCheck() throws Exception
  {
    System.out.println("decode double frame counter check");

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");
    byte[] systemTitle2 = CodingUtils.string2ByteArray("4D4D4D0000BC624E");


    byte[] ciphertext = CodingUtils.string2ByteArray(
            "2001234567411312FF935A47566827C467BC");

    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle);
    instance.setRespondingApTitle(systemTitle2);
    instance.decode(ciphertext, SecurityControlField.CipheringMethod.GLOBAL_UNICAST);
    instance.decode(ciphertext, SecurityControlField.CipheringMethod.GLOBAL_UNICAST);

    fail("Exception expected");
  }

  /**
   * Test of encode method, of class DlmsSecurityProviderGcm.
   */
  @Test
  public void testDecodeAuthenticatedEncrypted() throws Exception
  {
    System.out.println("decode: authenticated encrypted");

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");


    byte[] ciphertext = CodingUtils.string2ByteArray(
            "3001234567411312FF935A47566827C467BC7D825C3BE4A77C3FCC056B6B");

    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle,
                                                                   systemTitle, 0x01234567);

    byte[] expResult = CodingUtils.string2ByteArray("C0010000080000010000FF0200");
    DecodingResult result = instance.decode(ciphertext, SecurityControlField.CipheringMethod.GLOBAL_UNICAST);

    assertArrayEquals(expResult, result.getData());
    assertEquals(new SecurityControlField(0, true, true, CipheringMethod.GLOBAL_UNICAST), result.
            getSecurityControlField());
  }

  @Test(expected = CipherException.class)
  public void testDecodeAuthenticatedEncryptedFailure() throws Exception
  {
    System.out.println("decode: authenticated encrypted (exp. failure)");

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");


    byte[] ciphertext = CodingUtils.string2ByteArray(
            "3001234567411312FF935A47566827C467BC7D825C3BE5A77C3FCC056B6B");

    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle,
                                                                   systemTitle, 0x01234567);

    byte[] expResult = CodingUtils.string2ByteArray("C0010000080000010000FF0200");
    DecodingResult result = instance.decode(ciphertext, SecurityControlField.CipheringMethod.GLOBAL_UNICAST);

    assertArrayEquals(expResult, result.getData());
    assertEquals(new SecurityControlField(0, true, true, CipheringMethod.GLOBAL_UNICAST), result.
            getSecurityControlField());
  }

  /**
   * Example from GB ed 7 Amendment 2 Table 18 – HLS example with GMAC <P>
   */
  @Test
  public void testCheckServerReplyToChallenge() throws Exception
  {
    System.out.println("checkServerReplyToChallenge");
//    AuthenticationValue serverChallenge = AuthenticationValue.createBitString(new BitString(CodingUtils.string2ByteArray("503677524A323146")));
//    AuthenticationValue serverChallenge = AuthenticationValue.createCharstring("P6wRJ21F");

    final byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    final byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    final byte[] systemTitleServer = CodingUtils.string2ByteArray("4D4D4D0000BC614E");
    final byte[] systemTitleClient = CodingUtils.string2ByteArray("4D4D4D0000000001");


    final DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitleClient, systemTitleServer,
                                                                   0x00000001);
    instance.setClientChallenge("K56iVagY".getBytes());
    boolean checkServerReplyToChallenge =
            instance.checkServerReplyToChallenge(CodingUtils.string2ByteArray(
            "1001234567FE1466AFB3DBCD4F9389E2B7"));

    assertEquals(true, checkServerReplyToChallenge);
  }

  /**
   *  Example from GB ed 7 Amendment 2 Table 18 – HLS example with GMAC <P>
   *
   */
  @Test
  public void testProcessServerChallenge() throws Exception
  {
    System.out.println("processServerChallenge");
    AuthenticationValue serverChallenge = AuthenticationValue.createCharstring("P6wRJ21F");

    final byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    final byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    final byte[] systemTitleServer = CodingUtils.string2ByteArray("4D4D4D0000BC614E");
    final byte[] systemTitleClient = CodingUtils.string2ByteArray("4D4D4D0000000001");

    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitleClient,
                                                                   systemTitleServer, 0x01);

    //The test encoding changes the framecounter.
    //byte[] testtest = instance.encode(CodingUtils.string2ByteArray("503677524A323146"), new SecurityControlField(0,true,false,false));

    byte[] expResult = CodingUtils.string2ByteArray("10000000011A52FE7DD3E72748973C1E28");
    //10012345671A52FE7DD3E72748973C1E28
    byte[] result = instance.processServerChallenge(serverChallenge);

    System.out.println("   expected: " + CodingUtils.byteArrayToString(expResult));
    System.out.println("     result: " + CodingUtils.byteArrayToString(result));

    assertArrayEquals(expResult, result);
  }

  /**
   * Example from GB ed.7 p.135 (CtoS)<P>
   *
   */
  @Test
  public void testProcessServerChallenge2() throws Exception
  {
    System.out.println("processServerChallenge");
    AuthenticationValue serverChallenge = AuthenticationValue.createCharstring("K56iVagY");

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitleServer = CodingUtils.string2ByteArray("4D4D4D0000BC614E");
    byte[] systemTitleClient = CodingUtils.string2ByteArray("4D4D4D0000000001");

    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitleServer,
                                                                   systemTitleClient, 0x01234567); //Server and client switched because this is the other direction.

    //The test encoding changes the framecounter.
    //byte[] testtest = instance.encode(CodingUtils.string2ByteArray("503677524A323146"), new SecurityControlField(0,true,false,false));

    byte[] expResult = CodingUtils.string2ByteArray("1001234567FE1466AFB3DBCD4F9389E2B7");
    byte[] result = instance.processServerChallenge(serverChallenge);

    assertArrayEquals(expResult, result);
  }

  /**
   * Test of checkServerReplyToChallenge method, of class DlmsSecurityProviderGcm.
   */
  @Test
  public void testCheckServerReplyToChallenge2() throws Exception
  {
    System.out.println("checkServerReplyToChallenge 2");

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");

    byte[] replyToChallenge = CodingUtils.string2ByteArray("1001234567FE1466AFB3DBCD4F9389E2B7");
    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle,
                                                                   systemTitle, 0x01234567);
    instance.setClientChallenge(CodingUtils.string2ByteArray("4B35366956616759"));

    boolean expResult = true;
    boolean result = instance.checkServerReplyToChallenge(replyToChallenge);
    assertEquals(expResult, result);
  }

  /**
   * Test of checkServerReplyToChallenge method, of class DlmsSecurityProviderGcm.
   */
  @Test
  public void testCheckServerReplyToChallenge3() throws Exception
  {
    System.out.println("checkServerReplyToChallenge (false expected)");

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");

    byte[] replyToChallenge = CodingUtils.string2ByteArray("1001234567FE1466AFB3ABCD4F9389E2B7");
    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle,
                                                                   systemTitle, 0x01234567);

    instance.setClientChallenge(CodingUtils.string2ByteArray("4B35366956616759"));

    boolean expResult = false;
    boolean result = instance.checkServerReplyToChallenge(replyToChallenge);
    assertEquals(expResult, result);
  }

  /**
   * Test of getDedicatedKey method, of class DlmsSecurityProviderGcm.
   */
  @Test
  public void testBuildDedicatedKey()
  {
    System.out.println("buildDedicatedKey");

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");

    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle,
                                                                   systemTitle, 0x01234567);
    //AuthenticationValue expResult = null;
    byte[] result = instance.getDedicatedKey();
    assertEquals(16, result.length);

    System.out.println("   Key:" + CodingUtils.byteArrayToString(result));
  }

  /**
   * Test of buildCallingAuthenticationValue method, of class DlmsSecurityProviderGcm.
   */
  @Test
  public void testBuildCallingAuthenticationValue()
  {
    System.out.println("buildCallingAuthenticationValue");

    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");

    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle,
                                                                   systemTitle, 0x01234567);

    AuthenticationValue result = instance.buildCallingAuthenticationValue();

    assertEquals(AuthenticationValue.Type.CHARSTRING, result.getType());

    byte[] value = result.toBytes();
    assertEquals(64, value.length);
    System.out.println("   Calling authentication value:" + CodingUtils.byteArrayToString(value));
  }

  /**
   * Test of buildCallingAuthenticationValue method, of class DlmsSecurityProviderGcm.<P>
   * Test if all chars are used.<P>
   * This test can fail, but this is highly unlikely.
   */
  @Test
  public void testBuildCallingAuthenticationValue2() throws UnsupportedEncodingException
  {

    System.out.println("buildCallingAuthenticationValue 2");
    String allChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    Set<Character> charSetAll = new HashSet<Character>();
    Set<Character> charSetLeft = new HashSet<Character>();

    for (int i = 0; i < 62; i++)
    {
      charSetAll.add(allChars.charAt(i));
      charSetLeft.add(allChars.charAt(i));
    }


    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");

    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle,
                                                                   systemTitle, 0x01234567);
    int round = 0;

    while (true)
    {

      AuthenticationValue result = instance.buildCallingAuthenticationValue();

      assertEquals(AuthenticationValue.Type.CHARSTRING, result.getType());

      String value = new String(result.toBytes(), "ASCII");

      System.out.println("Round " + round);

      for (Character c : value.toCharArray())
      {
        assertTrue(charSetAll.contains(c));
        charSetLeft.remove(c);
      }

      if (charSetLeft.isEmpty())
      {
        break;
      }

      if (round > 1000)
      {
        fail("All usable chars should occur in 4-5 rounds (This test can fail, but this is highly unlikely)");
      }

      round++;
    }
  }

  /**
   * Test of buildCallingAuthenticationValue method, of class DlmsSecurityProviderGcm.<P>
   * Test if different strings are build.<P>
   * This test can fail, but this is highly unlikely.
   */
  @Test
  public void testBuildCallingAuthenticationValue3()
  {

    System.out.println("buildCallingAuthenticationValue 3");


    byte[] encryptionKey = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] authenticationKey = CodingUtils.string2ByteArray("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
    byte[] systemTitle = CodingUtils.string2ByteArray("4D4D4D0000BC614E");

    DlmsSecurityProviderGcm instance = new DlmsSecurityProviderGcm(encryptionKey, authenticationKey,
                                                                   systemTitle,
                                                                   systemTitle, 0x01234567);

    Set<AuthenticationValue> results = new HashSet<AuthenticationValue>();

    for (int i = 0; i < 10000; i++)
    {
      AuthenticationValue result = instance.buildCallingAuthenticationValue();

      if (results.contains(result))
      {
        fail("Value already created (This test can fail, but this is highly unlikely)");
      }

      results.add(result);
    }
  }

  /**
   * Test data directly from RFC3394
   */
  @Test
  public void testWrapKey() throws NoSuchAlgorithmException, NoSuchPaddingException
  {
//   KEK:            000102030405060708090A0B0C0D0E0F
//   Key Data:       00112233445566778899AABBCCDDEEFF
//  Ciphertext:  1FA68B0A8112B447 AEF34BD8FB5A7B82 9D3E862371D2CFE5
    System.out.println("wrapKey");

    byte[] kek = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] keyData = CodingUtils.string2ByteArray("00112233445566778899AABBCCDDEEFF");
    byte[] ciphertext = CodingUtils.string2ByteArray("1FA68B0A8112B447 AEF34BD8FB5A7B82 9D3E862371D2CFE5");

    byte[] result = DlmsSecurityProviderGcm.wrapKey(keyData, kek);

    assertArrayEquals(ciphertext, result);

  }

  /**
   * Test data directly from RFC3394
   */
  @Test
  public void testWrapKeyJava() throws NoSuchAlgorithmException, NoSuchPaddingException,
                                       IllegalBlockSizeException, BadPaddingException, InvalidKeyException
  {
//   KEK:            000102030405060708090A0B0C0D0E0F
//   Key Data:       00112233445566778899AABBCCDDEEFF
//  Ciphertext:  1FA68B0A8112B447 AEF34BD8FB5A7B82 9D3E862371D2CFE5
    System.out.println("wrapKey");

    byte[] kek = CodingUtils.string2ByteArray("000102030405060708090A0B0C0D0E0F");
    byte[] keyData = CodingUtils.string2ByteArray("00112233445566778899AABBCCDDEEFF");
    byte[] ciphertext = CodingUtils.string2ByteArray("1FA68B0A8112B447 AEF34BD8FB5A7B82 9D3E862371D2CFE5");

    SecretKeySpec kekKey = new SecretKeySpec(kek, "AES");
    SecretKeySpec keyDataKey = new SecretKeySpec(keyData, "AES");

    Cipher cipher = Cipher.getInstance("AESWrap");

    System.out.println("Used provider: " + cipher.getProvider().getName());

    cipher.init(Cipher.WRAP_MODE, kekKey);

    byte[] result = cipher.wrap(keyDataKey);

    assertArrayEquals(ciphertext, result);
  }

//  @Test
//  public void testConvert() throws UnsupportedEncodingException
//  {
//    final byte[] bytes = CodingUtils.string2ByteArray("FF 30 AA 00");
//    final String ascii= new String(bytes, "ASCII");
//    final byte[] decodedBytes= ascii.getBytes("ASCII");
//
//
//    System.out.println("ASCII-String:  "+ascii);
//    System.out.println("Bytes:         "+CodingUtils.byteArrayToString(bytes));
//    System.out.println("Decoded Bytes: "+CodingUtils.byteArrayToString(decodedBytes));
//
//    assertArrayEquals(bytes, decodedBytes);
//  }
}
