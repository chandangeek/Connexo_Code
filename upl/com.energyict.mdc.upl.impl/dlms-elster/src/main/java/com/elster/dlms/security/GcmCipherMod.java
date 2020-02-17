/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/security/GcmCipherMod.java $
 * Version:     
 * $Id: GcmCipherMod.java 4022 2012-02-16 17:07:53Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Jan 18, 2011 3:22:09 PM
 */
package com.elster.dlms.security;

import com.elster.bouncycastle.crypto.modes.GCMBlockCipherMod;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * Encapsulates the bouncy castle AES-GCM cipher.<P>
 * The cipher is encapsulated for easier replacement of the security provider.
 *
 * @author osse
 */
public class GcmCipherMod
{
  private final GCMBlockCipherMod gcmBlockCipher;
  private final byte[] NULL_BYTE= new byte[] {0};


  public GcmCipherMod(final byte[] blockCipherKey,final int macSize,final boolean forEncryption)
  {

    final AEADParameters aeadParameters = new AEADParameters(new KeyParameter(blockCipherKey), macSize,
                                                       NULL_BYTE, null);

    gcmBlockCipher = new GCMBlockCipherMod(new AESEngine());
    gcmBlockCipher.init(forEncryption, aeadParameters);
  }

  public void setIvAndAad(final byte[] initializationVector,final byte[] additionalAuthenticationData)
  {
    gcmBlockCipher.changeIvAndAad(initializationVector, additionalAuthenticationData);
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
