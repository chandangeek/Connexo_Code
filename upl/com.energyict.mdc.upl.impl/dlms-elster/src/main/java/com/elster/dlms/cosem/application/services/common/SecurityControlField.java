/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/common/SecurityControlField.java $
 * Version:     
 * $Id: SecurityControlField.java 2923 2011-05-10 14:08:54Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Jan 18, 2011 5:04:46 PM
 */
package com.elster.dlms.cosem.application.services.common;

/**
 * DLMS Security control field.<P>
 * See GB ed.7 p.125 "9.2.4.6 Ciphered xDLMS APDUs"<p>
 * Additionally a property ({@link #getCipheringMethod()}) for the used ciphering method is provided.
 *
 * @author osse
 */
public class SecurityControlField
{
  public enum CipheringMethod
  {
    DEDICATED_UNICAST, GLOBAL_UNICAST, GLOBAL_BROADCAST
  };

  public static final int SECURITY_ID_MASK = 0x0F;
  public static final int AUTHENTICATED = 0x10;
  public static final int ENCRYPTED = 0x20;
  public static final int KEY_SET_BROADCAST = 0x40;

  public static final int SECURITY_SUITE_AES128_GCM=0;

  private final int securityControlByte;
  private final CipheringMethod cipheringMethod;

  /**
   * Constructor.
   *
   * @param securityControlByte The complete security byte.
   * @param  cipheringMethod  Additional information about the ciphering method to apply / applied.
   */
  public SecurityControlField(int securityControlByte, CipheringMethod cipheringMethod)
  {
    if (securityControlByte > 0xFF || securityControlByte < 0)
    {
      throw new IllegalArgumentException("Illegal security control byte");
    }

    this.cipheringMethod = cipheringMethod;
    this.securityControlByte = securityControlByte;
  }

  /**
   * Constructor
   *
   * @param securitySuiteId Security suite id (Valid range: 0-15)
   * @param authenticated {@code true} if authenticated
   * @param encrypted {@code true} if encrypted
   * @param keySetBroadcast {@code true} for the broadcast key set, {@code false} for the unicast key set.
   * @param cipheringMethod  Additional information about the ciphering method to apply / applied.
   */
  public SecurityControlField(int securitySuiteId, boolean authenticated, boolean encrypted,
                              boolean keySetBroadcast, CipheringMethod cipheringMethod)
  {
    if (securitySuiteId > SECURITY_ID_MASK || securitySuiteId < 0)
    {
      throw new IllegalArgumentException("Illegal security suite id");
    }

    this.cipheringMethod = cipheringMethod;
    this.securityControlByte = securitySuiteId | (authenticated ? AUTHENTICATED : 0) | (encrypted ? ENCRYPTED
            : 0) | (keySetBroadcast ? KEY_SET_BROADCAST : 0);
  }

  /**
   * Constructor.
   *
   * The "broadcast key set flag" is automatically selected if the {@link CipheringMethod#GLOBAL_BROADCAST} is specified.
   *
   * @param securitySuiteId Security suite id (Valid range: 0-15)
   * @param authenticated {@code true} if authenticated
   * @param encrypted {@code true} if encrypted
   * @param cipheringMethod  Additional information about the ciphering method to apply / applied.
   */
  public SecurityControlField(int securitySuiteId, boolean authenticated, boolean encrypted,
                              CipheringMethod cipheringMethod)
  {
    if (securitySuiteId > SECURITY_ID_MASK || securitySuiteId < 0)
    {
      throw new IllegalArgumentException("Illegal security suite id");
    }

    this.cipheringMethod = cipheringMethod;
    this.securityControlByte = securitySuiteId | (authenticated ? AUTHENTICATED : 0) | (encrypted ? ENCRYPTED
            : 0) | (cipheringMethod == CipheringMethod.GLOBAL_BROADCAST ? KEY_SET_BROADCAST : 0);
  }

  /**
   * Return the complete security control byte.
   *
   * @return The complete security control byte.
   */
  public int getSecurityControlByte()
  {
    return securityControlByte;
  }

  /**
   * Returns the authenticated flag.
   *
   * @return {@code true} if authenticated
   */
  public boolean isAuthenticated()
  {
    return 0 != (securityControlByte & AUTHENTICATED);
  }

  /**
   * Returns the encrypted flag.
   *
   * @return {@code true} if encrypted
   */
  public boolean isEncrypted()
  {
    return 0 != (securityControlByte & ENCRYPTED);
  }

  /**
   * Returns {@code true} if the broadcast key set should be used.
   *
   * @return {@code true} if the broadcast key set should be used.
   */
  public boolean isKeySetBroadcast()
  {
    return 0 != (securityControlByte & KEY_SET_BROADCAST);
  }

  /**
   * Returns {@code true} if the unicast key set should be used.
   *
   * @return {@code true} if the unicast key set should be used.
   */
  public boolean isKeySetUnicast()
  {
    return 0 == (securityControlByte & KEY_SET_BROADCAST);
  }

  /**
   * Returns the security suite ID.
   *
   * @return The security suite id.
   */
  public int getSecuritySuiteId()
  {
    return SECURITY_ID_MASK & securityControlByte;
  }

  public CipheringMethod getCipheringMethod()
  {
    return cipheringMethod;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final SecurityControlField other = (SecurityControlField)obj;
    if (this.securityControlByte != other.securityControlByte)
    {
      return false;
    }
    if (this.cipheringMethod != other.cipheringMethod)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 41 * hash + this.securityControlByte;
    hash = 41 * hash + (this.cipheringMethod != null ? this.cipheringMethod.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString()
  {
    return "SecurityControlField{" + "securityControlByte=" + securityControlByte + ", cipheringMethod=" +
           cipheringMethod +"( Encrypted="+isEncrypted()+", Authenticated="+isAuthenticated()+", Security suite id="+getSecuritySuiteId() +")}";
  }







}
