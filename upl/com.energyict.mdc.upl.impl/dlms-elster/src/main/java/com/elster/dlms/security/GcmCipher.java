/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/security/GcmCipher.java $
 * Version:     
 * $Id: GcmCipher.java 4022 2012-02-16 17:07:53Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Jan 18, 2011 3:22:09 PM
 */
package com.elster.dlms.security;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * Encapsulates the bouncy castle AES-GCM cipher.<P>
 * The cipher is encapsulated for easier replacement of the security provider.
 *
 * @author osse
 */
public class GcmCipher
{
  private final GCMBlockCipher gcmBlockCipher;

  public GcmCipher(final byte[] initializationVector, final byte[] blockCipherKey,final byte[] additionalAuthenticationData,
                    final int macSize,final boolean forEncryption)
  {

    final AEADParameters aeadParameters = new AEADParameters(new KeyParameter(blockCipherKey), macSize,
                                                       initializationVector, additionalAuthenticationData);

    gcmBlockCipher = new GCMBlockCipher(new AESEngine());
    gcmBlockCipher.init(forEncryption, aeadParameters);
  }

  public int processBytes(final byte[] in,final int inOff,final int len,final byte[] out,final int outOff)
  {
    return gcmBlockCipher.processBytes(in, inOff, len, out, outOff);
  }

  public int doFinal(final byte[] out,final int outOff) throws CipherException
  {
    try
    {
      return gcmBlockCipher.doFinal(out, outOff);
    }
    catch (IllegalStateException ex)
    {
      throw new CipherException("doFinal: " + ex.getMessage(),ex);
    }
    catch (InvalidCipherTextException ex)
    {
      throw new CipherException("doFinal: " + ex.getMessage(),ex);
    }
  }

}
