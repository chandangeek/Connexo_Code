/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/security/DlmsSecurityProviderGcm.java $
 * Version:     
 * $Id: DlmsSecurityProviderGcm.java 6743 2013-06-12 09:48:59Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Jan 19, 2011 3:21:51 PM
 */
package com.elster.dlms.security;

import com.elster.dlms.cosem.application.services.common.SecurityControlField;
import com.elster.dlms.cosem.application.services.common.SecurityControlField.CipheringMethod;
import com.elster.dlms.cosem.application.services.open.AuthenticationValue;
import java.security.SecureRandom;
import java.util.logging.Logger;
import org.bouncycastle.crypto.engines.AESWrapEngine;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * Security provider for security suite id 0 (AES128-GCM).
 *
 * @author osse
 */
public class DlmsSecurityProviderGcm implements IDlmsSecurityProvider
{
  private static final int ALLOWED_SERVER_FRAMERCOUNTER_WINDOW = 15;
  private static final Logger LOGGER = Logger.getLogger(DlmsSecurityProviderGcm.class.getName());
  private final byte[] globalEncryptionKey;
  private final byte[] authenticationKey;
  private final byte[] systemTitleClient;
  private final byte[] dedicatedEncryptionKey;
  private long lastServerFramecounter = -1;
  //---
  private byte[] systemTitleServer;
  private long nextFramecount;
  private final SecureRandom random = new SecureRandom();
  // private static final Charset ASCII = Charset.forName("US-ASCII"); //VisibleString== ISO/IEC 646 == US-ASCII
  private byte[] clientChallenge;

  /**
   * Constructor for the GCM security provider.
   * 
   * @param encryptionKey The encryption key.
   * @param authenticationKey The authentication key.
   * @param systemTitle  The system title (of the client)
   */
  public DlmsSecurityProviderGcm(final byte[] encryptionKey, final byte[] authenticationKey,
                                 final byte[] systemTitle)
  {
    this.globalEncryptionKey = encryptionKey.clone();
    this.authenticationKey = authenticationKey.clone();
    this.systemTitleClient = systemTitle.clone();
    this.nextFramecount = random.nextLong() & 0x3FFFFFFFL;
    dedicatedEncryptionKey = new byte[16];
    random.nextBytes(dedicatedEncryptionKey);
  }

  /**
   * Constructor for tests. (forces a special system title for decoding).<P>
   * <b>Only for testing!</b>
   */
  public DlmsSecurityProviderGcm(final byte[] globalEncryptionKey, final byte[] authenticationKey,
                                 final byte[] systemTitleEncode,
                                 final byte[] systemTitleDecode)
  {
    LOGGER.warning("Setting up the security provider with an constructor for tests.");
    this.globalEncryptionKey = globalEncryptionKey.clone();
    this.authenticationKey = authenticationKey.clone();
    this.systemTitleClient = systemTitleEncode.clone();
    this.systemTitleServer = systemTitleDecode.clone();
    this.nextFramecount = random.nextLong() & 0x3FFFFFFFL;
    dedicatedEncryptionKey = new byte[16];
    random.nextBytes(dedicatedEncryptionKey);
  }

  /**
   * Constructor for tests. (forces a special frame count).<P>
   * <b>Only for testing!</b>
   */
  DlmsSecurityProviderGcm(final byte[] globalEncryptionKey, final byte[] authenticationKey,
                          final byte[] systemTitleEncode,
                          final byte[] systemTitleDecode, final long framecounter)
  {
    LOGGER.warning("Setting up the security provider with an constructor for tests.");
    this.globalEncryptionKey = globalEncryptionKey.clone();
    this.authenticationKey = authenticationKey.clone();
    this.systemTitleClient = systemTitleEncode.clone();
    this.systemTitleServer = systemTitleDecode.clone();
    this.nextFramecount = framecounter;
    dedicatedEncryptionKey = new byte[16];
    random.nextBytes(dedicatedEncryptionKey);
  }

  public byte[] getSystemTitleServer()
  {
    return systemTitleServer == null ? null : systemTitleServer.clone();
  }

  //@Override
  public void setRespondingApTitle(final byte[] systemTitleServer)
  {
    if (this.systemTitleServer != null)
    {
      throw new IllegalStateException("System title server allready set.");
    }
    this.systemTitleServer = systemTitleServer.clone();
  }

  //@Override
  public byte[] encode(final byte[] plaintext, final SecurityControlField securityControlField) throws
          CipherException
  {

    final boolean encrypt = securityControlField.isEncrypted();
    final boolean authenticate = securityControlField.isAuthenticated();

    if (!encrypt && !authenticate)
    {
      throw new IllegalArgumentException("No crypto function selected");
    }

    if (securityControlField.getSecuritySuiteId() != 0)
    {
      throw new IllegalArgumentException("Invalid security suite id");
    }

    if (systemTitleClient == null || systemTitleClient.length < 8)
    {
      throw new IllegalStateException("System title of the client not set or to short");
    }

    final byte[] iv = new byte[12];

    System.arraycopy(systemTitleClient, systemTitleClient.length - 8, iv, 0, 8);

    final byte[] fc = getNextFrameCountAsBytes();
    System.arraycopy(fc, 0, iv, 8, 4);

    byte[] aad = null;

    if (encrypt && authenticate)
    {
      aad = new byte[1 + authenticationKey.length];
      aad[0] = (byte)securityControlField.getSecurityControlByte();
      System.arraycopy(authenticationKey, 0, aad, 1, authenticationKey.length);
    }
    else if (!encrypt && authenticate)
    {
      aad = new byte[1 + authenticationKey.length + plaintext.length];
      aad[0] = (byte)securityControlField.getSecurityControlByte();
      System.arraycopy(authenticationKey, 0, aad, 1, authenticationKey.length);
      System.arraycopy(plaintext, 0, aad, 1 + authenticationKey.length, plaintext.length);
    }
    else if (encrypt && !authenticate)
    {
      aad = null;
    }


    final GcmCipherMod cipher = getGcmCipher(iv, securityControlField.getCipheringMethod(), aad, authenticate,
                                             true);

    final int len = 5 + plaintext.length + (authenticate ? 12 : 0);
    final byte[] result = new byte[len];

    result[0] = (byte)securityControlField.getSecurityControlByte();
    System.arraycopy(fc, 0, result, 1, 4);

    if (encrypt)
    {
      final int bytesProcessed = cipher.processBytes(plaintext, 0, plaintext.length, result, 5);
      cipher.doFinal(result, bytesProcessed + 5);
    }
    else
    {
      System.arraycopy(plaintext, 0, result, 5, plaintext.length);
      final int bytesProcessed = cipher.processBytes(new byte[0], 0, 0, result, plaintext.length + 5);
      cipher.doFinal(result, bytesProcessed + plaintext.length + 5);
    }

    return result;
  }

  private byte[] getEncryptionKey(final SecurityControlField.CipheringMethod cipheringMethod)
  {
    switch (cipheringMethod)
    {
      case DEDICATED_UNICAST:
        return dedicatedEncryptionKey;
      case GLOBAL_UNICAST:
        return globalEncryptionKey;
      case GLOBAL_BROADCAST:
        throw new UnsupportedOperationException("Broadcast ciphering is not supported yet");
      default:
        throw new UnsupportedOperationException("Unknown ciphering method: " + cipheringMethod);
    }
  }

  //@Override
  public DecodingResult decode(final byte[] ciphertext,final SecurityControlField.CipheringMethod cipheringMethod) throws
          CipherException
  {

    if (systemTitleServer == null)
    {
      throw new IllegalStateException("System title of sever must be set before data can be decoded.");
    }

    if (ciphertext.length < 5)
    {
      throw new CipherException("Cipher text to short.");
    }

    final SecurityControlField securityControlField =
            new SecurityControlField(ciphertext[0] & 0xFF, cipheringMethod);
    final boolean encrypted = securityControlField.isEncrypted();
    final boolean authenticated = securityControlField.isAuthenticated();

    if (securityControlField.getSecuritySuiteId() != 0)
    {
      throw new CipherException("Wrong security suite id.");
    }

    if (authenticated && ciphertext.length < (5 + 12))
    {
      throw new CipherException("Cipher text to short.");
    }

    final long frameCounter = (((ciphertext[1] & 0xFF) << 24)
                         | ((ciphertext[2] & 0xFF) << 16)
                         | ((ciphertext[3] & 0xFF) << 8)
                         | (ciphertext[4] & 0xFF));

    //--- Frame counter check ---
    if (lastServerFramecounter >= 0)
    {
      if (frameCounter <= lastServerFramecounter || frameCounter >= lastServerFramecounter
                                                                    + ALLOWED_SERVER_FRAMERCOUNTER_WINDOW)
      {
        throw new CipherException("Unexpected frame count received: " + frameCounter
                                  + " -The frame count must be bigger than " + lastServerFramecounter
                                  + " and smaller than " + lastServerFramecounter
                                  + ALLOWED_SERVER_FRAMERCOUNTER_WINDOW);
      }
    }
    lastServerFramecounter = frameCounter;

    final byte[] iv = new byte[12];

    System.arraycopy(systemTitleServer, systemTitleServer.length - 8, iv, 0, 8);
    iv[8] = (byte)(0xFF & (frameCounter >> 24));
    iv[9] = (byte)(0xFF & (frameCounter >> 16));
    iv[10] = (byte)(0xFF & (frameCounter >> 8));
    iv[11] = (byte)(0xFF & (frameCounter));


    byte[] aad = null;

    if (encrypted && authenticated)
    {
      aad = new byte[1 + authenticationKey.length];
      aad[0] = (byte)securityControlField.getSecurityControlByte();
      System.arraycopy(authenticationKey, 0, aad, 1, authenticationKey.length);
    }
    else if (!encrypted && authenticated)
    {
      aad = new byte[1 + authenticationKey.length + (ciphertext.length - 12 - 5)];
      aad[0] = (byte)securityControlField.getSecurityControlByte();
      System.arraycopy(authenticationKey, 0, aad, 1, authenticationKey.length);
      System.arraycopy(ciphertext, 5, aad, 1 + authenticationKey.length, (ciphertext.length - 12 - 5));
    }
    else if (encrypted && !authenticated)
    {
      aad = null;
    }
    else
    {
      throw new IllegalArgumentException("no crypto function selected");
    }

    final GcmCipherMod cipher = getGcmCipher(iv, cipheringMethod, aad, authenticated, false);


    final int len = ciphertext.length - 5 -(authenticated?12:0);
    final byte[] result = new byte[len];

    if (encrypted)
    {
      final int bytesProcessed = cipher.processBytes(ciphertext, 5, ciphertext.length - 5, result, 0);
      cipher.doFinal(result, bytesProcessed);
    }
    else
    {
      System.arraycopy(ciphertext, 5, result, 0, ciphertext.length - 5 - 12);
      final int bytesProcessed = cipher.processBytes(ciphertext, ciphertext.length - 12, 12, result, 0);
      cipher.doFinal(result, bytesProcessed);
    }

    return new DecodingResult(securityControlField, result);
  }

  //@Override
  public byte[] getDedicatedKey()
  {
    return dedicatedEncryptionKey == null ? null : dedicatedEncryptionKey.clone();
  }

  /**
   * Wraps a key with the AES key encryption algorithm.
   * 
   * @param key The key to wrap.
   * @param keyEncryptionKey The key encryption key.
   * @return  The wrapped key.
   */
  public static byte[] wrapKey(final byte[] key,final byte[] keyEncryptionKey)
  {
    final AESWrapEngine wrapEngine = new AESWrapEngine();
    wrapEngine.init(true, new KeyParameter(keyEncryptionKey));
    return wrapEngine.wrap(key, 0, key.length);
  }

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  /**
   * Creates a 16 bytes random key using {@code SecureRandom}
   * 
   * @return  The generated random key.
   */
  public static byte[] createRandomKey()
  {
    final byte[] result = new byte[16];

    synchronized (SECURE_RANDOM)
    {
      SECURE_RANDOM.nextBytes(result);
    }
    return result;
  }

  //@Override
  public AuthenticationValue buildCallingAuthenticationValue()
  {
//    clientChallenge = new byte[64];
//    random.nextBytes(clientChallenge);
//    return AuthenticationValue.createBitString(new BitString(clientChallenge));

    int i = 0;
    clientChallenge = new byte[64];

    while (i < clientChallenge.length)
    {
      final int v = 48 + random.nextInt(75);
      if ((v >= 48 && v <= 57) || (v >= 65 && v <= 90) || (v >= 97 && v <= 122)) //0-9, A-Z, a-z
      {
        clientChallenge[i] = (byte)v;
        i++;
      }
    }
    return AuthenticationValue.createCharstring(clientChallenge);
  }

  private byte[] buildTag(final SecurityControlField securityControlField,final long framecounter,final byte[] systemTitle,
                          final byte[] data) throws CipherException
  {
    byte[] iv = new byte[12];

    System.arraycopy(systemTitle, systemTitle.length - 8, iv, 0, 8);
    iv[8] = (byte)(0xFF & (framecounter >> 24));
    iv[9] = (byte)(0xFF & (framecounter >> 16));
    iv[10] = (byte)(0xFF & (framecounter >> 8));
    iv[11] = (byte)(0xFF & (framecounter));

    byte[] aad = new byte[1 + authenticationKey.length + data.length];
    aad[0] = (byte)securityControlField.getSecurityControlByte();
    System.arraycopy(authenticationKey, 0, aad, 1, authenticationKey.length);
    System.arraycopy(data, 0, aad, 1 + authenticationKey.length, data.length);

    final GcmCipher cipher = new GcmCipher(iv, globalEncryptionKey, aad, 12 * 8, true);

    final byte[] result = new byte[12];

    final int bytesProcessed = cipher.processBytes(new byte[0], 0, 0, result, 0);
    cipher.doFinal(result, bytesProcessed);

    return result;
  }

  //@Override
  public byte[] processServerChallenge(final AuthenticationValue serverChallenge) throws CipherException
  {
    byte[] challenge= serverChallenge.toBytes();

    final SecurityControlField securityControlField =
            new SecurityControlField(0, true, false, SecurityControlField.CipheringMethod.GLOBAL_UNICAST);

    final byte[] iv = new byte[12];

    System.arraycopy(systemTitleClient, systemTitleClient.length - 8, iv, 0, 8);
    final byte[] fc = getNextFrameCountAsBytes();
    System.arraycopy(fc, 0, iv, 8, 4);

    byte[] aad = new byte[1 + authenticationKey.length + challenge.length];
    aad[0] = (byte)securityControlField.getSecurityControlByte();
    System.arraycopy(authenticationKey, 0, aad, 1, authenticationKey.length);
    System.arraycopy(challenge, 0, aad, 1 + authenticationKey.length, challenge.length);

    final GcmCipher cipher = new GcmCipher(iv, globalEncryptionKey, aad, 12 * 8, true);

    byte[] result = new byte[5 + 12];

    result[0] = (byte)securityControlField.getSecurityControlByte();
    System.arraycopy(fc, 0, result, 1, 4);

    final int bytesProcessed = cipher.processBytes(new byte[0], 0, 0, result, 5);
    cipher.doFinal(result, bytesProcessed + 5);
    return result;
  }

  //@Override
  public boolean checkServerReplyToChallenge(final byte[] replyToChallenge) throws CipherException
  {
    if (replyToChallenge.length != 17)
    {
      throw new CipherException("Wrong reply length.");
    }

    if ((replyToChallenge[0] & 0xFF) != 0x10)
    {
      throw new CipherException("Unexpected security header.");
    }

    final SecurityControlField securityControlField =
            new SecurityControlField(replyToChallenge[0] & 0xFF,
                                     SecurityControlField.CipheringMethod.GLOBAL_UNICAST);


    final long frameCounter = (((replyToChallenge[1] & 0xFF) << 24)
                         | ((replyToChallenge[2] & 0xFF) << 16)
                         | ((replyToChallenge[3] & 0xFF) << 8)
                         | (replyToChallenge[4] & 0xFF));
    final byte[] tag = buildTag(securityControlField, frameCounter, systemTitleServer, clientChallenge);


    boolean result = true;

    for (int i = 0; i < 12; i++)
    {
      if (tag[i] != replyToChallenge[i + 5])
      {
        result = false;
        //no break here to keep a constant checking time
      }
    }
    return result;
  }

  /**
   * <b>Only for testing!</b><P>
   * <b>Only for testing!</b>
   *
   * @param clientChallenge
   */
  void setClientChallenge(final byte[] clientChallenge)
  {
    this.clientChallenge = clientChallenge.clone();
  }

  /**
   * <b>Only for testing!</b><P>
   * <b>Only for testing!</b>
   */
  public void setFrameCounter(final long framecounter)
  {
    this.nextFramecount = framecounter;
  }

  private long getNextFrameCount()
  {
    return nextFramecount++;
  }

  private byte[] getNextFrameCountAsBytes()
  {
    final long c = getNextFrameCount();

    byte[] result = new byte[4];
    result[0] = (byte)(0xFF & (c >> 24));
    result[1] = (byte)(0xFF & (c >> 16));
    result[2] = (byte)(0xFF & (c >> 8));
    result[3] = (byte)(0xFF & (c));

    return result;
  }

  private ChipherHolder encryptionCipher;
  private ChipherHolder decryptionCipher;

  private GcmCipherMod getGcmCipher(final byte[] initializationVector,final CipheringMethod cipheringMethod,
                                    final byte[] additionalAuthenticationData,
                                    final boolean authenticate,final boolean forEncryption)
  {
    GcmCipherMod result;

    if (forEncryption)
    {
      if (encryptionCipher == null
          || cipheringMethod != encryptionCipher.getCipheringMethod()
          || authenticate != encryptionCipher.isAuthenticate())
      {
        final int macSize = authenticate ? 12 * 8 : 0;
        encryptionCipher = new ChipherHolder(authenticate, cipheringMethod,
                                             new GcmCipherMod(getEncryptionKey(cipheringMethod), macSize,
                                                              forEncryption));
      }
      result = encryptionCipher.getCipher();
    }
    else
    {
      if (decryptionCipher == null
          || cipheringMethod != decryptionCipher.getCipheringMethod()
          || authenticate != decryptionCipher.isAuthenticate())
      {
        final int macSize = authenticate ? 12 * 8 : 0;
        decryptionCipher = new ChipherHolder(authenticate, cipheringMethod,
                                             new GcmCipherMod(getEncryptionKey(cipheringMethod), macSize,
                                                              forEncryption));
      }
      result = decryptionCipher.getCipher();
    }

    result.setIvAndAad(initializationVector, additionalAuthenticationData);
    return result;
  }

  private static final class ChipherHolder
  {
    private final boolean authenticate;
    private final CipheringMethod cipheringMethod;
    private final GcmCipherMod cipher;

    public ChipherHolder(final boolean authenticate,final CipheringMethod cipheringMethod,final GcmCipherMod cipher)
    {
      this.authenticate = authenticate;
      this.cipheringMethod = cipheringMethod;
      this.cipher = cipher;
    }

    public GcmCipherMod getCipher()
    {
      return cipher;
    }

    public CipheringMethod getCipheringMethod()
    {
      return cipheringMethod;
    }

    public boolean isAuthenticate()
    {
      return authenticate;
    }

  }

}
