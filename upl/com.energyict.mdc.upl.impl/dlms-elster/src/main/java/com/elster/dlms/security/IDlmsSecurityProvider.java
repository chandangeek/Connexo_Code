/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/security/IDlmsSecurityProvider.java $
 * Version:
 * $Id: IDlmsSecurityProvider.java 4022 2012-02-16 17:07:53Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Jan 20, 2011 4:35:33 PM
 */
package com.elster.dlms.security;

import com.elster.dlms.cosem.application.services.common.SecurityControlField;
import com.elster.dlms.cosem.application.services.open.AuthenticationValue;

/**
 * Interface for security providers.
 *
 * @author osse
 */
public interface IDlmsSecurityProvider
{
  /**
   * Builds the authentication value for the AARQ PDU.<P>
   * (E.g. the client challenge)
   * 
   * @return The authentication value.
   */
  AuthenticationValue buildCallingAuthenticationValue();

  /**
   * Returns the dedicated key (season key).
   * 
   * 
   * @return  The dedicated key.
   */
  byte[] getDedicatedKey();

  /**
   * Decodes the cipher text.
   * 
   * @param ciphertext The cipher text.
   * @param cipheringMethod The ciphering method.
   * @return The decoding result.
   * @throws CipherException 
   */
  DecodingResult decode(byte[] ciphertext, SecurityControlField.CipheringMethod cipheringMethod) throws
          CipherException;

  byte[] encode(byte[] plaintext, SecurityControlField securityControlField) throws CipherException;

  byte[] processServerChallenge(AuthenticationValue serverChallenge) throws CipherException;

  boolean checkServerReplyToChallenge(byte[] replyToChallenge) throws CipherException;

  void setRespondingApTitle(byte[] systemTitle);

  /**
   * Result for {@link IDlmsSecurityProvider#decode(byte[], com.elster.dlms.cosem.application.services.common.SecurityControlField.CipheringMethod)  }
   */
  class DecodingResult
  {
    private final SecurityControlField securityControlField;
    private final byte[] data;

    public DecodingResult(final SecurityControlField securityControlField, final byte[] data)
    {
      this.securityControlField = securityControlField;
      this.data = data;
    }

    /**
     * The security control field.
     * 
     * @return 
     */
    public SecurityControlField getSecurityControlField()
    {
      return securityControlField;
    }

    /**
     * The decoded data.
     * 
     * @return The decoded data.
     */
    public byte[] getData()
    {
      return data;
    }

  }

}
